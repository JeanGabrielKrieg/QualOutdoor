package com.qualoutdoor.recorder.persistent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Class that generates text file from the content of reference tree
 * architecture is conserved. it asks SQLgenerator object for details about
 * leaves
 */
public class FileGenerator extends AsyncTask<Void, Void, ByteArrayOutputStream> {

    /** file to write in database content */
    private ByteArrayOutputStream file;
    /** connector for having leaves' details */
    private SQLConnector connecteur;
    /** object to call when file is ready */
    private FileReadyListener callback;
    /** comments to add at the beginning of the text */
    private String comments;

    public FileGenerator(SQLConnector conn, String com, FileReadyListener cb) {
        this.file = new ByteArrayOutputStream();
        this.comments = com;
        this.connecteur = conn;
        this.callback = cb;
    }

    /**
     * Writing database content into file, tree is read from the given
     * managerWriter leaves' details will be asked to the SQLconnector
     */
    public void tablesRetransciption(DataBaseTreeManager managerWriter) {
        try {
            // new line is read
            while (managerWriter.moveToNextLine()) {
                // if it is a leaf
                if (managerWriter.getCursor().getLevel() == 7) {
                    // asking details to connector
                    ArrayList<String> details = connecteur
                            .getLeafDetails(managerWriter.getCursor()
                                    .getReference());
                    // and writing them into the file
                    this.file
                            .write((managerWriter.getCursor().getLevel() + "/")
                                    .getBytes());
                    int compteurslash1 = 1;
                    for (String field : details) {
                        this.file.write(field.getBytes());
                        if (compteurslash1 != details.size()) {
                            this.file.write("/".getBytes());
                        }
                        compteurslash1++;
                    }
                    this.file.write(";".getBytes());
                    // if it's not a leaf
                } else {
                    // writing level and reference of the node into the file
                    int refNode = managerWriter.getCursor().getReference();
                    int levelNode = managerWriter.getCursor().getLevel();
                    this.file.write((levelNode + "/" + refNode + "$")
                            .getBytes());
                }
            }
        } catch (DataBaseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Function that initializes tablesRetranscirption function, checks if tree
     * is not empty, inserts comments in file,
     */
    public void completeRetranscription(String comments,
            DataBaseTreeManager managerWriter) throws DataBaseException {
        try {
            // check if tree is not empty
            if (this.connecteur.hasLeaf()) {
                // writing comments
                this.file.write(("#" + comments + "#").getBytes());
                // calling tablesRetranscription
                this.tablesRetransciption(managerWriter);
                // flushing storage system
                this.connecteur.completeReset();
            } else {
                throw new DataBaseException("no leaf to be write!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** background task that generate the reading file. */
    @Override
    protected ByteArrayOutputStream doInBackground(Void... params) {
        try {
            // Acquire the access to the database
            DBSemaphore.ref.acquire();
        } catch (InterruptedException e) {
            Log.e("FileGenerator", "Interrupted Exception", e);
            return null;
        }
        
        try {
            completeRetranscription(this.comments,
                    this.connecteur.prepareManager());
        } catch (DataBaseException e) {
            Log.e("FileGenerator", "Database Exception", e);
            return null;
        } finally {
            // We are done with the database
            DBSemaphore.ref.release();
        }
        
        return this.file;

    }

    /** Calling callback with the generated file */
    @Override
    protected void onPostExecute(ByteArrayOutputStream result) {
        if (result == null) {
            Log.d("DEBUG FILE GENERATOR", "NO TEXT GENERATED");
        } else {
            Log.d("DEBUG FILE GENERATOR", result.toString());
        }
        this.callback.onFileReady(result);
    }

}
