package com.jiahaoliuliu.pubnubaschatsystem;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.jiahaoliuliu.pubnubaschatsystem.model.Message;

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

        // Check if the message is valid
        if (!data.containsKey(Message.JSON_FIELD_MESSAGE_KEY) || !data.containsKey(Message.JSON_FIELD_SENDER_KEY)) {
            Log.e(TAG, "The message received is not valid " + data);
            return;
        }

        // Parse the message
        Message messageReceived = new Message();
        messageReceived.setSender(data.getString(Message.JSON_FIELD_SENDER_KEY));
        messageReceived.setMessage(data.getString(Message.JSON_FIELD_MESSAGE_KEY));

        // Discard your own message
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (deviceId.equals(messageReceived.getSender())) {
            Log.v(TAG, "Received its own message push notifications. Do not do anything");
            return;
        }

        // Display the notifications
        displayNotifications(messageReceived);
    }

    private void displayNotifications(Message message) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(getApplicationContext().getString(R.string.push_notification_title))
                        .setContentText(message.getMessage());
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
