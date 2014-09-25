package com.qualoutdoor.recorder.network;

import java.io.File;
import java.util.regex.Pattern;

import com.qualoutdoor.recorder.QualOutdoorRecorderApp;
import com.qualoutdoor.recorder.recording.RecordingService;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

/**
 * Class enabling file sending using email
 * */

public class EmailFileSender {

    public static void sendFileByEmail(Context context, String dest,
            RecordingService recordingService) {
        // email verifying pattern
        final Pattern rfc2822 = Pattern
                .compile("^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$");
        // checking given email format
        if (!rfc2822.matcher(dest).matches()) {
            Toast toast = Toast.makeText(context, "invalid email address",
                    Toast.LENGTH_SHORT);
            toast.show();
        } else {
            // if format is right : preparing mail intent
            Intent email = new Intent(Intent.ACTION_SEND);
            // setting destination
            email.putExtra(Intent.EXTRA_EMAIL, new String[] {
                dest
            });
            // setting subject
            email.putExtra(Intent.EXTRA_SUBJECT, "QualOutdoor : measures file");

            
            //fetching file from memory
            File file=new File(recordingService.getFilesDir(),
                    QualOutdoorRecorderApp.ARCHIVE_NAME);
            Uri uri =Uri.fromFile(file);
            //setting email attachment
            email.putExtra(Intent.EXTRA_STREAM,uri);
            
            
            // setting mail content with readed string
            email.putExtra(Intent.EXTRA_TEXT,
                    "find attached measures file I generated");
            // setting mail type
            email.setType("message/rfc822");
            // launching intent
            context.startActivity(Intent.createChooser(email,
                    "Choose an Email client :"));

        }
    }

}
