package com.qualoutdoor.recorder.recording;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.qualoutdoor.recorder.QualOutdoorRecorderApp;
import com.qualoutdoor.recorder.R;
import com.qualoutdoor.recorder.network.DataSendingManager;
import com.qualoutdoor.recorder.network.SendCompleteListener;
import com.qualoutdoor.recorder.persistent.CollectMeasureException;
import com.qualoutdoor.recorder.persistent.DBSemaphore;
import com.qualoutdoor.recorder.persistent.DataBaseException;
import com.qualoutdoor.recorder.persistent.FileGenerator;
import com.qualoutdoor.recorder.persistent.FileReadyListener;
import com.qualoutdoor.recorder.persistent.SQLConnector;
import com.qualoutdoor.recorder.persistent.Sample;

/**
 * This Handler is used to manage AsyncTask related to the database by sending
 * messages. The possible actions are starting or stopping a record and
 * requesting an upload of the local database.
 * 
 * @author Gaborit Nicolas & Lucas Croixmarie
 */
public class RecordingHandler extends Handler {

    /** Message code for starting recording */
    public static final int MESSAGE_START_RECORD = 101;
    /** Message code for stopping recording */
    public static final int MESSAGE_STOP_RECORD = 102;
    /**
     * Message code for initiating a conversion and upload of the database. This
     * message should always be sent along with the chosen protocol code as
     * arg1. Example :
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
     * Message msg = recordingHandler.obtainMessage(
     * RecordingHandler.MESSAGE_UPLOAD_DATABASE, chosenProtocol, 0);
     * recordingHandler.sendMessage(msg);
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */
    public static final int MESSAGE_UPLOAD_DATABASE = 103;
    /** Message code for a sample action (used only internally) */
    private static final int MESSAGE_SAMPLE = 104;

    /** The recording service using this handler */
    private RecordingService recordingService;

    /** The listeners to the recording state */
    private ArrayList<IRecordingListener> recordingListeners = new ArrayList<IRecordingListener>();
    /** Indicate if the handler is currently recording */
    private boolean isRecording = false;

    /** The number of ongoing sample task */
    private int sampleTaskCount = 0;

    /** The number of ongoing upload database task */
    private int uploadTaskCount = 0;

    /** The delay between samples */
    private int sampleRate;

    /**
     * Indicate if the tasks should close the database when done
     */
    private boolean shouldClose = true;

    /** The SQLConnector used for all the database operations */
    private SQLConnector connector;

    /**
     * Create a new RecordingHandler with the given sampleRate
     * 
     * @param recordingService
     *            The service using this handler
     * @param sampleRate
     *            The initial value of the sample rate
     */
    public RecordingHandler(RecordingService recordingService, int sampleRate) {
        this.sampleRate = sampleRate;
        this.recordingService = recordingService;
        this.connector = new SQLConnector(recordingService);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        // Identify the message code and call the appropriate action
        switch (msg.what) {
        case MESSAGE_START_RECORD:
            actionStartRecord();
            break;
        case MESSAGE_STOP_RECORD:
            actionStopRecord();
            break;
        case MESSAGE_UPLOAD_DATABASE:
            // Read the chosen protocol
            int chosenProtocol = msg.arg1;
            actionUploadDatabase(chosenProtocol);
            break;
        case MESSAGE_SAMPLE:
            actionSample();
            break;
        }
    }

    /**
     * Action performed when a MESSAGE_START_RECORD is received. Might fail due
     * to connector failing to open.
     */
    private void actionStartRecord() {
        // If not already recording
        if (!isRecording) {
            try {
                // If we need to open the database
                if (!connector.isOpen())
                    connector.open();
                // Start the sampling now
                this.sendEmptyMessage(MESSAGE_SAMPLE);
                // We are now recording
                setNotifyRecording(true);
                // Thus we don't want the database to be closed
                shouldClose = false;
            } catch (SQLException exc) {
                Log.e("RecordingService", "Can't open SQLConnector", exc);
                // Toast the user that recording won't be available
                Toast.makeText(recordingService,
                        R.string.error_open_sql_connector, Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    /**
     * Action performed when a MESSAGE_STOP_RECORD is received.
     */
    private void actionStopRecord() {
        // If actually recording
        if (isRecording) {
            // We will stop the recording process
            setNotifyRecording(false);
            // Thus we should close the database when the task are done
            shouldClose = false;
            // Clear any remaining sample message
            removeMessages(MESSAGE_SAMPLE);
            // Finish recording if
            finishRecording();
        }
    }

    /**
     * Action performed when a MESSAGE_UPLOAD_DATABASE is received. Convert the
     * whole database to a custom CSV file and try to upload this file with the
     * chosen protocol
     * 
     * @param chosenProtocol
     *            The protocol used for the upload
     */
    private void actionUploadDatabase(int chosenProtocol) {
        // Open database if needed
        if (!connector.isOpen())
            connector.open();
        // Increment the number of upload task
        uploadTaskCount++;
        // Create a callback that will upload the generated file when done
        FileReadyListener writingCallback = new WritingCallbackPreferences(
                chosenProtocol);
        // Define the comment added at the beginning of the file
        String comments = "...comments about file...";
        // Create a writer that will convert the database into a file
        FileGenerator writer = new FileGenerator(connector, comments,
                writingCallback);
        // Start conversion
        writer.execute();
    }

    /** Action performed when a MESSAGE_SAMPLE is received */
    private void actionSample() {
        // Should we stop the recording ?
        if (isRecording) {
            // Try to make a sample
            try {
                Sample sample = recordingService.sample();
                // Insert the sample in the database
                new InsertSampleTask().execute(sample);
            } catch (SampleFailedException e) {} finally {
                // Sample again later
                this.sendEmptyMessageDelayed(MESSAGE_SAMPLE, sampleRate);
            }
        } else {
            // Finish recording
            finishRecording();
        }
    }

    /**
     * Check that no task will use the database in the future and close it if
     * needed
     */
    private void checkCloseDatabase() {
        // Check that no task are remaining and that we should close the
        // database
        if (shouldClose && (sampleTaskCount + uploadTaskCount == 0)) {
            // Close the database
            if (connector.isOpen())
                connector.close();
        }
    }

    /**
     * Close the database if needed then stop the RecordingService if no task
     * are remaining in order to finish the recording process.
     */
    private void finishRecording() {

        Log.d("RecordingHandler", "trying to finishRecording");
        // Check and close database
        checkCloseDatabase();
        // If no more sampling task are waiting
        if (sampleTaskCount == 0) {
            // Indicate that the recording service does not need to run in
            // foreground anymore and remove notification
            recordingService.stopForeground(true);
            // Stop the recording service
            recordingService.stopSelf();
            Log.d("RecordingHandler", "Stopped Recording");
        }
    }

    /** Indicate if a recording process is in progress */
    public boolean isRecording() {
        return this.isRecording;
    }

    /**
     * Add a recording listener
     * 
     * @param listener
     *            The listener to register
     */
    public void register(IRecordingListener listener) {
        // Add it to the list
        recordingListeners.add(listener);
        // Notify it immediatly
        listener.onRecordingChanged(isRecording);
    }

    /**
     * Remove a recording listener
     * 
     * @param listener
     *            The listener to remove
     */
    public void unregister(IRecordingListener listener) {
        // Remove it from the list
        recordingListeners.remove(listener);
    }

    /**
     * Change and notify that the recording process state has changed
     * 
     * @param state
     *            The new recording state
     */
    private void setNotifyRecording(boolean state) {
        // Update the recording state
        isRecording = state;
        // Notify every listener
        for (IRecordingListener listener : recordingListeners) {
            // For each listener, notify
            listener.onRecordingChanged(state);
        }
    }

    /**
     * Modify the sampling rate of the recording.
     * 
     * @param millis
     *            The number of milliseconds between each sample
     */
    public void setSamplingRate(int millis) {
        sampleRate = millis;
    }

    /**
     * This task insert a given Sample in the database
     */
    private class InsertSampleTask extends AsyncTask<Sample, Void, Void> {
        @Override
        protected void onPreExecute() {
            // Increment the number of sample task running
            sampleTaskCount++;
        }

        @Override
        protected Void doInBackground(Sample... params) {
            // Get the passed parameters
            Sample parameters = params[0];
            // Insert the measure in the database
            try {
                // Acquire access to database
                DBSemaphore.ref.acquire();
                // Insert the sample data
                connector.insertMeasure(parameters.measureContext,
                        parameters.data, parameters.latitude,
                        parameters.longitude);
                // Release access
                DBSemaphore.ref.release();
                Log.d("SampleTask",
                        "Insertion effectuée :\n" + parameters.data.toString());
            } catch (DataBaseException e) {
                Log.e("SampleTask", "DataBaseException", e);
            } catch (CollectMeasureException e) {
                Log.e("SampleTask", "CollectMeasureException", e);
            } catch (InterruptedException e) {
                Log.e("SampleTaks", "InterruptedException", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // The task is over
            sampleTaskCount--;
            // Should we stop the recording ?
            if (!isRecording) {
                // Finish recording
                finishRecording();
            }
        }
    }

    /**
     * This object defines the action to be performed when the database has been
     * converted and it should be uploaded
     */
    private class WritingCallbackPreferences implements FileReadyListener {

        private int chosenProtocol;

        public WritingCallbackPreferences(int protocol) {
            this.chosenProtocol = protocol;
        }

        @Override
        public void onFileReady(ByteArrayOutputStream file) {

            // If some data were converted
            if (file != null) {
                // generating file name with timestamp to preserve unicity
                String name = "file" + System.currentTimeMillis();
                // adding file to archive
                try {
                    addFileToArchive(name, file);
                } catch (IOException e) {
                    Log.e(RecordingHandler.class.toString(),
                            "Add to archive failed", e);
                }
            }

            // The possibly existing archive file
            File archive = new File(recordingService.getFilesDir(),
                    QualOutdoorRecorderApp.ARCHIVE_NAME);

            // If there is no data to send
            if (!archive.exists()) {
                // No data waiting to be uploaded : toast it
                Toast.makeText(recordingService,
                        R.string.error_no_data_to_upload, Toast.LENGTH_SHORT)
                        .show();
                // Don't upload anything
                return;
            }
            // Else continue...

            // Creation of a sending CallBack : called when one sending is
            // done : if file had not been send it is stored into app file
            // systeme
            SendCompleteListener sendingCallback = new SendCompleteListener() {
                @Override
                public void onTaskCompleted(String protocol, File fileSent,
                        boolean success) {
                    if (!success) {// if files can't be send, it's stored
                                   // into internal storage:
                        Toast.makeText(recordingService,
                                R.string.error_sending_file, Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        Toast.makeText(recordingService,
                                R.string.information_upload_succeeded,
                                Toast.LENGTH_SHORT).show();
                        // archive is deleted
                        fileSent.delete();
                    }

                    // The upload task is over
                    uploadTaskCount--;
                    // Check if we should close the database
                    checkCloseDatabase();
                }
            };

            // sending archive
            if (this.chosenProtocol == QualOutdoorRecorderApp.UPLOAD_PROTOCOL_HTTP) {
                // setting server URL : normaly feching if from constant
                // Class
                String url = QualOutdoorRecorderApp.URL_SERVER_HTTP;
                // creation and execution of a DataSendingManager : printing
                // widget has to be resolved
                DataSendingManager managerHTTP = new DataSendingManager(url,
                        archive, "http", sendingCallback);
                managerHTTP.execute();
            } else if (this.chosenProtocol == QualOutdoorRecorderApp.UPLOAD_PROTOCOL_FTP) {
                // setting server URL : normaly feching if from constant
                // Class
                String url = QualOutdoorRecorderApp.URL_SERVER_FTP;
                // creation and execution of a DataSendingManager : printing
                // widget has to be resolved
                DataSendingManager managerFTP = new DataSendingManager(url,
                        archive, "ftp", sendingCallback);
                managerFTP.execute();

            }
        }
    }

    private static final byte[] BUFFER = new byte[4096 * 1024];

    /**
     * Add file into the archive destined for the pending uploads
     * 
     * @param fileName
     *            The name of the file to add
     * @param fileContent
     *            The content of the file to add
     * @throws IOException
     */
    public void addFileToArchive(String fileName,
            ByteArrayOutputStream fileContent) throws IOException {
        // The possibly already existing zip file
        File archiveFile1 = new File(recordingService.getFilesDir(),
                QualOutdoorRecorderApp.ARCHIVE_NAME);

        // The new temporary zip file
        File archiveFile2 = new File(recordingService.getFilesDir(),
                QualOutdoorRecorderApp.ARCHIVE_NAME + "2");

        // For reading the entries of the existing archive
        ZipFile archive1 = null;
        // For writing into the temporary archive
        ZipOutputStream zos2 = null;

        try {
            // Open a stream into the temporary archive
            FileOutputStream archiveStream2;
            archiveStream2 = new FileOutputStream(archiveFile2);
            zos2 = new ZipOutputStream(new BufferedOutputStream(archiveStream2));

            // Check if there was actually an existing zip file
            if (archiveFile1.exists()) {
                // Open a ZipFile from the existing archive
                archive1 = new ZipFile(archiveFile1);

                // Get the entries in the existing zip file
                Enumeration<? extends ZipEntry> previousEntries = archive1
                        .entries();
                // Copy every previous entry into the new zip file
                while (previousEntries.hasMoreElements()) {
                    ZipEntry entry = previousEntries.nextElement();
                    zos2.putNextEntry(entry);
                    // If not a directory, copy the content
                    if (!entry.isDirectory()) {
                        // Copy the entry input stream into our output stream
                        InputStream inputStream1 = archive1
                                .getInputStream(entry);
                        int bytesRead;
                        while ((bytesRead = inputStream1.read(BUFFER)) != -1) {
                            zos2.write(BUFFER, 0, bytesRead);
                        }
                    }
                    zos2.closeEntry();
                }
            }

            // Add our new content
            String filename = fileName;
            byte[] bytes = (fileContent.toByteArray());
            ZipEntry entry = new ZipEntry(filename);
            zos2.putNextEntry(entry);
            zos2.write(bytes);
            zos2.closeEntry();

        } catch (IOException exc) {
            Log.e("RecordingHandler", "addFileToArchive", exc);
        } finally {
            // Close the open files
            if (archive1 != null)
                archive1.close();
            if (zos2 != null)
                zos2.close();
        }

        // Delete the previous archive
        Log.d("RecordingHandler", "Delete " + archiveFile1.delete());
        // Rename the new zip file to replace the first zip file
        Log.d("RecordingHandler",
                "Rename " + archiveFile2.renameTo(archiveFile1));

    }
}
