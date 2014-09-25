package com.qualoutdoor.recorder.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.qualoutdoor.recorder.MainActivity;
import com.qualoutdoor.recorder.R;

/**
 * This static class is a utility class allowing the app to manage some
 * predefined user notifications. An exemple of notification is the
 * "ongoing recording" notification which tells the user that the app is
 * currently recording. There might be other notifications in the future.
 * 
 * @author Gaborit Nicolas
 */
public class NotificationCenter {

    /** The id of the background recording notification */
    public final static int BACKGROUND_RECORDING = 1337;

    /* This class is not meant to be instantiated */
    private NotificationCenter() {}

    /** Obtain a notification corresponding to an ongoing recording process */
    public static Notification getRecordingNotification(Context context) {
        // Get a notification builder
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                context);
        // Set the notification small icon
        notificationBuilder.setSmallIcon(R.drawable.notification_icon);
        // Set the notification title
        notificationBuilder.setContentTitle(context.getResources().getText(
                R.string.notification_recording_title));
        // Set the notification text
        notificationBuilder.setContentText(context.getResources().getText(
                R.string.notification_recording_text));
        // Indicate that the notification represent an ongoing process
        notificationBuilder.setOngoing(true);
        // Set the priority of this notification to the minimum
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_LOW);

        // Creates an explicit intent for the settings activity
        Intent notificationIntent = new Intent(context, MainActivity.class);

        // The stack builder object will contain an artificial back stack for
        // the started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // the application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        // Adds the back stack
        stackBuilder.addParentStack(MainActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(notificationIntent);

        // Obtain the pending intent associated to the task constructed so far
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);
        // Attach it to the notification builder
        notificationBuilder.setContentIntent(resultPendingIntent);

        return notificationBuilder.build();
    }

    /**
     * Activate the ongoing recording notification. This is not called manually
     * anymore because the notification is managed directly by the service going
     * foreground (see RecordingService).
     * 
     * @param context
     *            The context used to emit the notification
     */
    public static void notifyBackgroundRecording(Context context) {
        // Get the notification manager from the host activity
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        // Build the notification and notify it with the appropriate tag
        mNotificationManager.notify(BACKGROUND_RECORDING,
                getRecordingNotification(context));
    }

    /**
     * Dismiss the ongoing recording notification
     * 
     * @param context
     *            The context used to access the NotificationManager
     */
    public static void dismissBackgroundRecording(Context context) {
        // Get the notification manager from the host activity
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        // Cancel the notification
        mNotificationManager.cancel(BACKGROUND_RECORDING);
    }

}
