package com.jiahaoliuliu.pubnubaschatsystem;

import android.content.Context;
import android.os.Bundle;
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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.jiahaoliuliu.pubnubaschatsystem.model.Message;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author <a href="mailto:jiahaoliuliu@gmail.com">Jiahao Liu Liu</a>
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final String DEFAULT_CHANNEL_GROUP_NAME = "PubNubDefaultChannelGroup";
    private static final String DEFAULT_CHANNEL_NAME = "PubNubDefaultChannel";

    private static final int MAXIMUM_NUMBER_RETRY_SUBSCRIBE_CHANNEL_GROUP = 3;

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
    private Gson mGson;
    private int mNumberRetrySubscribeChannelGroup;

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
        mPubNub.setUUID(mDeviceId);
        mPubNub.setAuthKey(mDeviceId);
        mGson = new Gson();

        //      Specify an adapter
        mMessagesListAdapter = new MessagesListAdapter(mDeviceId);
        mMessagesListRecyclerView.setAdapter(mMessagesListAdapter);

        // Subscribe to a channel group

        mPubNub.channelGroupAddChannel(DEFAULT_CHANNEL_GROUP_NAME, DEFAULT_CHANNEL_NAME, new Callback() {
            @Override
            public void successCallback(String channel, Object message) {
                Log.v(TAG, "Channel group correctly added. Trying to subscribe it again");
            }

            @Override
            public void errorCallback(String channel, PubnubError error) {
                Log.v(TAG, "Error adding the channel to the channels group (" + error.errorCode + "): " +
                    error.getErrorString());
            }
        });

        final Callback subscribeToChannelGroupCallback = new Callback() {
            @Override
            public void successCallback(String channel, Object message) {
                Log.v(TAG, "Message received from channel " + channel + ": " + message);
                try {
                    final Message receivedMessage = mGson.fromJson(message.toString(), Message.class);

                    // Do not display our own message
                    if (receivedMessage.getSender() != null && receivedMessage.getSender().equals(mDeviceId)) {
                        return;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mMessagesListAdapter.onNewMessage(receivedMessage);
                            //Scroll to the last position
                            mMessagesListRecyclerView.scrollToPosition(mMessagesListAdapter.getItemCount());
                        }
                    });
                } catch (JsonSyntaxException exception) {
                    Log.e(TAG, "Error pasing the received message " + message, exception);
                }
            }

            @Override
            public void errorCallback(String channel, PubnubError error) {
                Log.e(TAG, "Error callback(" + error.errorCode + "):" + error.getErrorString());
                // If there is any timeout error, is it possible the user has not channel in the channels
                // group
                if (error.errorCode == PubnubError.PNERR_TIMEOUT) {
//                    mPubNub.channelGroupAddChannel(DEFAULT_CHANNEL_GROUP_NAME, DEFAULT_CHANNEL_NAME, new Callback() {
//                        @Override
//                        public void successCallback(String channel, Object message) {
//                            Log.v(TAG, "Channel group correctly added. Trying to subscribe it again");
//                            if (mNumberRetrySubscribeChannelGroup < MAXIMUM_NUMBER_RETRY_SUBSCRIBE_CHANNEL_GROUP) {
//                                mPubNub.channelGroupSubscribe(DEFAULT_CHANNEL_GROUP_NAME, subscribeToChannelGroupCallback);
//                            }
//                        }
//
//                        @Override
//                        public void errorCallback(String channel, PubnubError error) {
//                            super.errorCallback(channel, error);
//                        }
//                    });
                }
            }

            @Override
            public void connectCallback(String channel, Object message) {
                Log.v(TAG, "Connect callback");
                Snackbar.make
                        (mCoordinatorLayout,
                                "Correctly subscribed to the default channel group",
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
        };

        try {
            mPubNub.channelGroupSubscribe(DEFAULT_CHANNEL_GROUP_NAME, subscribeToChannelGroupCallback);
            mNumberRetrySubscribeChannelGroup ++;
        } catch (PubnubException e) {
            Log.e(TAG, "Error subscribing to the channels group", e);
        }

//        // Subscribing to the channel
//        try {
//            mPubNub.subscribe(DEFAULT_CHANNEL_NAME, new Callback() {
//                @Override
//                public void successCallback(String channel, Object message) {
//                    Log.v(TAG, "Message received from channel " + channel + ": " + message);
//                    try {
//                        final Message receivedMessage = mGson.fromJson(message.toString(), Message.class);
//
//                        // Do not display our own message
//                        if (receivedMessage.getSender() != null && receivedMessage.getSender().equals(mDeviceId)) {
//                            return;
//                        }
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                mMessagesListAdapter.onNewMessage(receivedMessage);
//                                //Scroll to the last position
//                                mMessagesListRecyclerView.scrollToPosition(mMessagesListAdapter.getItemCount());
//                            }
//                        });
//                    } catch (JsonSyntaxException exception) {
//                        Log.e(TAG, "Error pasing the received message " + message, exception);
//                    }
//                }
//
//                @Override
//                public void errorCallback(String channel, PubnubError error) {
//                    Log.v(TAG, "Error callback(" + error.errorCode + "):" + error.getErrorString());
//                }
//
//                @Override
//                public void connectCallback(String channel, Object message) {
//                    Log.v(TAG, "Connect callback");
//                    Snackbar.make
//                            (mCoordinatorLayout,
//                                    "Correctly subscribed to the default channel",
//                                    Snackbar.LENGTH_LONG).show();
//                }
//
//                @Override
//                public void reconnectCallback(String channel, Object message) {
//                    Log.v(TAG, "Reconnect callback");
//                }
//
//                @Override
//                public void disconnectCallback(String channel, Object message) {
//                    Log.v(TAG, "Disconnect callback");
//                }
//            });
//        } catch (PubnubException pubnubException) {
//            Log.e(TAG, "Error with the channel channel in PubNub");
//        }
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

        JSONObject messageToBeSentJson = null;
        try {
            messageToBeSentJson = new JSONObject(mGson.toJson(messageToBeSent));
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing the message to be sent to json", e);
            return;
        }

        Log.v(TAG, "Message to be published \"" + messageToBeSentJson + "\"");
        mPubNub.publish(DEFAULT_CHANNEL_NAME, messageToBeSentJson, new Callback() {
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
                        error.getErrorString() + ". The content is " +
                        mGson.toJson(messageToBeSent) + ". ");
            }
        });
    }
}
