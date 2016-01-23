package com.jiahaoliuliu.pubnubaschatsystem;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final String DEFAULT_CHANNEL_NAME = "PubNubDefaultChannel";

    // Views
    private CoordinatorLayout mCoordinatorLayout;

    // Internal variables
    private Pubnub mPubNub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Link the views
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_coordinator_layout);

        // Internal variables
        mPubNub = new Pubnub(APIKeys.PUBNUB_PUBLISH_KEY, APIKeys.PUBNUB_SUBSCRIBE_KEY);

        // Subscribing to the channel
        try {
            mPubNub.subscribe(DEFAULT_CHANNEL_NAME, new Callback() {
                @Override
                public void successCallback(String channel, Object message) {
                    Log.v(TAG, "Success callback");
                }

                @Override
                public void errorCallback(String channel, PubnubError error) {
                    Log.v(TAG, "Error callback(" + error.errorCode + "):" + error.getErrorString());
                }

                @Override
                public void connectCallback(String channel, Object message) {
                    Log.v(TAG, "Connect callback");

                    Snackbar.make
                            (mCoordinatorLayout,
                                    "Correctly subscribed to the default channel",
                                    Snackbar.LENGTH_LONG).show();
                }

                @Override
                public void reconnectCallback(String channel, Object message) {
                    Log.v(TAG, "Reconnect callback");
                }

                @Override
                public void disconnectCallback(String channel, Object message) {
                    Log.v(TAG, "Disconnect callback");
                }
            });
        } catch (PubnubException pubnubException) {
            Log.e(TAG, "Error with the channel channel in PubNub");
        }

        // Publish a simple message
        mPubNub.publish(DEFAULT_CHANNEL_NAME, "Simple message", new Callback() {
            @Override
            public void successCallback(String channel, Object message) {
                Log.v(TAG, "Message correctly published");
                Snackbar.make(mCoordinatorLayout, "Message correctly published", Snackbar.LENGTH_SHORT);
            }

            @Override
            public void errorCallback(String channel, PubnubError error) {
                Log.e(TAG, "Error publishing the message(" + error.errorCode + "):" + error.getErrorString());
            }
        });
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }


}
