package com.jiahaoliuliu.pubnubaschatsystem;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

/**
 * Intent service used to retrieve and save the registration token needed
 * Extracted from here
 * https://github.com/googlesamples/google-services/blob/master/android/gcm/app/src/main/java/gcm/play/android/samples/com/gcmquickstart/RegistrationIntentService.java
 */
public class RegistrationIntentService extends IntentService {

    public static final String TAG = "RegistrationIntentService";
    public static final String INTENT_KEY_UPDATE_SERVER_TOKEN_CALLBACK =
            "com.jiahaoliuliu.pubnubaschatsystem.RegistrationIntentService.INTENT_KEY_UPDATE_SERVER_TOKEN_CALLBACK";

    private ResultReceiver mResultReceiver;
    public static final String BUNDLE_KEY_GCM_TOKEN =
            "com.jiahaoliuliu.pubnubaschatsystem.RegistrationIntentService.BUNDLE_KEY_GCM_TOKEN";

    public RegistrationIntentService() {
        super(TAG);
    }

    @SuppressLint("LongLogTag")
    @Override
    protected void onHandleIntent(Intent intent) {
        // Get the result receiver
        Bundle extras = intent.getExtras();
        if (extras != null && extras.containsKey(INTENT_KEY_UPDATE_SERVER_TOKEN_CALLBACK)) {
            mResultReceiver = (ResultReceiver)extras.get(INTENT_KEY_UPDATE_SERVER_TOKEN_CALLBACK);
        }

        try {
            InstanceID instanceId = InstanceID.getInstance(this);
            String token = instanceId.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.v(TAG, "token got from the GCM " + token);

            if (mResultReceiver != null) {
                Bundle bundle = new Bundle();
                bundle.putString(BUNDLE_KEY_GCM_TOKEN, token);
                mResultReceiver.send(0, bundle);
            } else {
                // TODO: See what to do in this case.
                Log.e(TAG, "Error sending the token registered to the server");
            }

        } catch (Exception e) {
            Log.e(TAG, "Failed to complete token refresh", e);
        }
    }
}
