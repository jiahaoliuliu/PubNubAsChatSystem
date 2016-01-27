package com.jiahaoliuliu.pubnubaschatsystem;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONException;

/**
 *
 * Created by Jiahao on 26/01/2016.
 */
public class SimpleGcmListenerService extends GcmListenerService {

    private static final String TAG = "SimpleGcmListenerService";

    private static final int NOTIFICATION_ID = 1000;

    @SuppressLint("LongLogTag")
    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.d(TAG, "Message received from " + from + ": " + data + "\n");

        // Notify only if the application is not in foreground
        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        // The first in the list of RunningTasks is always the foreground task.
        ActivityManager.RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(0);
        String foregroundTaskPackageName = foregroundTaskInfo .topActivity.getPackageName();
        Log.d(TAG, "The package name of the application in foreground is \"" + foregroundTaskPackageName + "\" " +
                "and the curent package name is \"" + getApplicationContext().getPackageName() + "\"");
        if (getApplicationContext().getPackageName().equals(foregroundTaskPackageName)) {
            Log.v(TAG, "The current application is in foreground. Not displaying the push notification");
            return;
        }


    }
}
