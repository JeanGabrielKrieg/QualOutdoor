package com.qualoutdoor.recorder;

import android.util.Log;

/**
 * Static class that can be used to do log conditionnaly. You can then activate
 * some debug modes by changing the corresponding boolean value here.
 * 
 * @author Gaborit Nicolas
 * 
 */
public class Debug {
    public static final boolean debug = true;

    public static final boolean log = true;

    public static void log(String tag, String msg) {
        Log.d(tag, msg);
    }

}
