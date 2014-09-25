package com.qualoutdoor.recorder.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Child of Sender Class, implementing file sending with FTP protocol. An FTP
 * connection is limited to a file sending then only sendFile method will be
 * implemented. Attributes of this class are the specific parameters of a FTP
 * connection
 */

public class FtpFileSender implements Sender {

    /** Login for being identified by FTP server */
    private String user;
    /** Password for being identified by FTP server */
    private String password;
    /** The storing path into the client storing space */
    private String storingPath;

    /**
     * Constructor
     */
    public FtpFileSender(String user, String password, String storingPath) {
        this.user = user;
        this.password = password;
        this.storingPath = storingPath;
    }

    /**
     * Implementation of sendFile method : a new connection is opened, file is
     * transmitted then connection is closed
     */
    @Override
    public boolean sendFile(String url, String fileName, InputStream content) {
        // indicated whether transfer has been correctly done
        boolean response;
        try {
            // URL address shaping
            String target = "ftp://" + this.user + ":" + this.password + "@"
                    + url + this.storingPath + fileName + "FTP";
            // URL creation
            URL targetAddress = new URL(target);
            // connection opening
            URLConnection connection = targetAddress.openConnection();
            // enabling connection to handle outputs. Now inputs can't be handle
            // anymore
            connection.setDoOutput(true);
            // Getting the flow to write into connection
            OutputStream os = connection.getOutputStream();

            // Setting Reading/Writing mecanisme : reading from the given
            // content and writing into the new output stream
            // temporary buffer
            byte[] temp = new byte[1024];
            // reading indicator
            int indic;
            while ((indic = content.read(temp)) != -1) {
                os.write(temp, 0, indic);
            }
            // if no exception occurred, transmission is considered successful.
            response = true;
            os.close();
            // if an exception occurred, transmission is considered
            // unsuccessful.
        } catch (MalformedURLException e) {
            response = false;
            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
            response = false;
        }
        // returning transmission state
        return response;
    }

}
