package com.qualoutdoor.recorder;

import android.app.Application;
import android.content.res.Resources;

/**
 * Extending Application in order to hold some global constants that can't be
 * stored in the XML files, and giving global access to application resources.
 * 
 * It is now possible to access the resources easily from a static context as
 * follows :
 * 
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * static Resources res = QualOutdoorRecorderApp.getAppResources();
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * 
 * @author Gaborit Nicolas
 */
public class QualOutdoorRecorderApp extends Application {

    /** Store a reference to this application for global access */
    private static QualOutdoorRecorderApp thisApp = null;
    /*
     * Initialized in the constructor of the Application, before any other
     * component is initialized
     */
    

    /*
     * In a non-prototype context, these values should be replaced by real value
     * obtained from signing in.
     */
    public static final int user = 11;
    public static final int group = 22;

    /* The metrics identifiers in the database */
    /** Metric index of the cell id */
    public static final int FIELD_CELL_ID = 1;
    /** Metric index of the signal strength */
    public static final int FIELD_SIGNAL_STRENGTH = 2;
    /** Metric index of the call test */
    public static final int FIELD_CALL = 3;
    /** Metric index of the upload test */
    public static final int FIELD_UPLOAD = 4;
    /** Metric index of the download test */
    public static final int FIELD_DOWNLOAD = 5;

    /** URL of the HTTP server */
    public static final String URL_SERVER_HTTP = "http://192.168.0.4:8080/upload";
    /** URL of the FTP server */
    public static final String URL_SERVER_FTP = "192.168.0.4";

    /** Number of milliseconds in a second */
    public static final int MILLIS_IN_SECOND = 1000;
    /** Number of seconds in a minutes */
    public static final int SECONDS_IN_MINUTE = 60;
    
    /** Strings identifiers of sending protocol to use */
    public static final int UPLOAD_PROTOCOL_HTTP = 1;
    public static final int UPLOAD_PROTOCOL_FTP = 2;

    /** Archive file name */
    public static final String ARCHIVE_NAME = "pendingFiles";

    @Override
    public void onCreate() {
        // Initialize this application reference
        thisApp = this;
    }

    /**
     * Provide a global access to the application resources
     * 
     * @return The resources relative to the application context
     */
    public static Resources getAppResources() {
        return thisApp.getApplicationContext().getResources();
    }
}
