package com.jiahaoliuliu.pubnubaschatsystem;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.jiahaoliuliu.pubnubaschatsystem.model.Message;
import com.jiahaoliuliu.pubnubaschatsystem.model.MessagesHistory;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

/**
 * @author <a href="mailto:jiahaoliuliu@gmail.com">Jiahao Liu Liu</a>
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final String DEFAULT_CHANNEL_NAME = "PubNubDefaultChannel1";

    /**
     * The default number of messages. By default the maximum is 100
     */
    private static final int DEFAULT_HISTORICAL_MESSAGES = 100;

    // Views
    private CoordinatorLayout mCoordinatorLayout;
    private RecyclerView mMessagesListRecyclerView;
    private EditText mMessageEditText;
    private Button mSendMessageButton;

    // Internal variables
    private Context mContext;
    private Pubnub mPubNub;
    private MessagesListAdapter mMessagesListAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private String mDeviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Link the views
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_coordinator_layout);
        mMessagesListRecyclerView = (RecyclerView) findViewById(R.id.messages_list_recycler_view);
        mMessageEditText = (EditText) findViewById(R.id.message_edit_text);
        mSendMessageButton = (Button) findViewById(R.id.send_message_button);
        mSendMessageButton.setOnClickListener(mOnClickListener);

        // Internal variables
        mContext = this;
        mPubNub = new Pubnub(APIKeys.PUBNUB_PUBLISH_KEY, APIKeys.PUBNUB_SUBSCRIBE_KEY);
        mLayoutManager = new LinearLayoutManager(this);
        mMessagesListRecyclerView.setLayoutManager(mLayoutManager);
        mDeviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        //      Specify an adapter
        mMessagesListAdapter = new MessagesListAdapter(mDeviceId);
        mMessagesListRecyclerView.setAdapter(mMessagesListAdapter);

        // Trying to enable the gcm token for the default channel
        // Ask the gcm server for the token
        Intent startRegistrationIntentServiceIntent = new Intent(mContext, RegistrationIntentService.class);
        startRegistrationIntentServiceIntent.putExtra(
                RegistrationIntentService.INTENT_KEY_UPDATE_SERVER_TOKEN_CALLBACK, new ResultReceiver(null) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        if (resultData == null) {
                            Log.e(TAG, "Error getting gcm tokens. The result data is null");
                            return;
                        }
                        String gcmToken = resultData.getString(RegistrationIntentService.BUNDLE_KEY_GCM_TOKEN);
                        Log.v(TAG, "Token received " + gcmToken);

                        mPubNub.enablePushNotificationsOnChannel(DEFAULT_CHANNEL_NAME, gcmToken, new Callback() {
                            @Override
                            public void successCallback(String channel, Object message) {
                                Log.v(TAG, "GCM token correctly registered with the channel");
                            }

                            @Override
                            public void errorCallback(String channel, PubnubError error) {
                                Log.e(TAG, "Error registering the gcm token with the channel(" + error.errorCode + "):" +
                                    error.getErrorString());
                            }
                        });

                    }
                });

        mContext.startService(startRegistrationIntentServiceIntent);

        // Get the historical data from the channel
        mPubNub.history(DEFAULT_CHANNEL_NAME, DEFAULT_HISTORICAL_MESSAGES, new Callback(){
            @Override
            public void successCallback(String channel, Object historicalMessages) {
                Log.v(TAG, "Correctly retrieved the historical messages " + historicalMessages);
                try {
                    // Parsing the historical messages
                    MessagesHistory messagesHistory = new MessagesHistory(historicalMessages.toString());
                    for (Message messageReceived : messagesHistory.getMessagesList()) {
                        onNewMessageReceived(messageReceived, false);
                    }
                } catch (IllegalArgumentException exception) {
                    Log.e(TAG, "Error parsing the historical messages. It is not valid. " + historicalMessages.toString());
                }
            }

            @Override
            public void errorCallback(String channel, PubnubError error) {
                Log.v(TAG, "Error retrieving the historical messages(" + error.errorCode + "):" + error.getErrorString());
            }
        });

        // Subscribing to the channel
        try {
            mPubNub.subscribe(DEFAULT_CHANNEL_NAME, new Callback() {
                @Override
                public void successCallback(String channel, Object message) {
                    Log.v(TAG, "Message received from channel " + channel + ": " + message);
                    onNewMessageReceived(message.toString(), true);
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
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.send_message_button:
                    sendMessage();
                    break;
            }
        }
    };

    private void onNewMessageReceived(String messageJson, boolean isHistoricalMessage) {
        try {
            final Message receivedMessage = new Message(messageJson);
            onNewMessageReceived(receivedMessage, isHistoricalMessage);
        } catch (IllegalArgumentException exception) {
            Log.e(TAG, "Error parsing the received message " + messageJson, exception);
        }
    }

    private void onNewMessageReceived(final Message message, boolean isHistoricalMessage) {
        if (message == null || !message.isValid()) {
            Log.w(TAG, "The received message is not valid");
            return;
        }

        // Do not display our own message
        if (isHistoricalMessage && mDeviceId.equals(message.getSender())) {
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMessagesListAdapter.onNewMessage(message);
                //Scroll to the last position
                mMessagesListRecyclerView.scrollToPosition(mMessagesListAdapter.getItemCount());
            }
        });
    }

    private void sendMessage() {
        // Check the content of the message
        String message = mMessageEditText.getText().toString();
        if (TextUtils.isEmpty(message)) {
            mMessageEditText.setError(getString(R.string.error_empty_message));
            return;
        }

        final Message messageToBeSent = new Message();
        messageToBeSent.setMessage(message);
        messageToBeSent.setSender(mDeviceId);

        Log.d(TAG, "Creating the json file for the message " + messageToBeSent.toJsonObject());
        mPubNub.publish(DEFAULT_CHANNEL_NAME, messageToBeSent.toJsonObject(), new Callback() {
            @Override
            public void successCallback(String channel, Object message) {
                Log.v(TAG, "Message sent correctly " + message);
                // This code should run on the main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMessagesListAdapter.onNewMessage(messageToBeSent);
                        mMessagesListRecyclerView.scrollToPosition(mMessagesListAdapter.getItemCount());
                        mMessageEditText.setText("");
                    }
                });
            }

            @Override
            public void errorCallback(String channel, PubnubError error) {
                Log.e(TAG, "Error sending the message (" + error.errorCode + "):" +
                        error.getErrorString() + ". The content is " + messageToBeSent.toJsonObject());
            }
        });
    }

}
