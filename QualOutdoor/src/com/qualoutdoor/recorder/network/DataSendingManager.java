package com.qualoutdoor.recorder.network;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.os.AsyncTask;

/**
 * AsyncTask able to send a specified file using a specified sending protocol.
 */
public class DataSendingManager extends AsyncTask<Void, Void, Boolean> {

    /** server address to reach */
    private String target;
    /** File to Send to server */
    private File fileToUpload;
    /** Protocol to use for sending */
    private String protocole;
    /** object to call when sending is over */
    private SendCompleteListener callback;

    /**
     * Constructor of the class:
     * 
     * @param url
     *            The address to send data
     * @param filesToUpload
     *            The file to be upload
     * @param proto
     *            The protocol to use for sending
     * @param cb
     *            The callback to call when asynchronous task ends
     */
    public DataSendingManager(String url, File file, String proto,
            SendCompleteListener cb) {
        this.target = url;
        this.fileToUpload = file;
        this.protocole = proto;
        this.callback = cb;
    }

    /**
     * Sending action will be executed in a background thread
     */
    protected Boolean doInBackground(Void... params) {
        try {
            // sender objet initialisation
            Sender sender = null;
            // if http protocol is chosen for sending
            if (this.protocole.equals("http")) {
                // set name of the input of the file in HTTP post formular that
                // will be sent
                sender = new HttpFileSender("uploadedFile");
            }
            // if ftp protocol is chosen
            else if (this.protocole.equals("ftp")) {
                // set login, password, and distant path required by FTP server
                sender = new FtpFileSender("client", "alsett", "/myUploads/");
            }
            // Once sender is set, it sends given file
            boolean result;
            result = sender.sendFile(this.target, this.fileToUpload.getName(),
                    new FileInputStream(fileToUpload));
            // notify to post execution action whether file has been correctly
            // sent or not
            return result;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }

    }

    /**
     * When sending is over, callback is called in passing the sent file and in
     * indicating if sending was successful or not
     * */
    @Override
    protected void onPostExecute(Boolean result) {
        this.callback
                .onTaskCompleted(this.protocole, this.fileToUpload, result);

    }

}
