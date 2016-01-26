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
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.jiahaoliuliu.pubnubaschatsystem.model.Message;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final String DEFAULT_CHANNEL_NAME = "PubNubDefaultChannel";

    // Views
    private CoordinatorLayout mCoordinatorLayout;
    private RecyclerView mMessagesListRecyclerView;
    private EditText mMessageEditText;
    private Button mSendMessageButton;

    // Internal variables
    private Context mContext;
    private Pubnub mPubNub;
    private RecyclerView.Adapter mMessagesListAdapter;
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

        // Internal variables
        mContext = this;
        mPubNub = new Pubnub(APIKeys.PUBNUB_PUBLISH_KEY, APIKeys.PUBNUB_SUBSCRIBE_KEY);
        mLayoutManager = new LinearLayoutManager(this);
        mMessagesListRecyclerView.setLayoutManager(mLayoutManager);
        mDeviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        //      Specify an adapter
        final List<Message> simulatedMessages = createSimulatedMesages();
        mMessagesListAdapter = new MessagesListAdapter(mDeviceId, simulatedMessages);
        mMessagesListRecyclerView.setAdapter(mMessagesListAdapter);

        // Subscribing to the channel
        try {
            mPubNub.subscribe(DEFAULT_CHANNEL_NAME, new Callback() {
                @Override
                public void successCallback(String channel, Object message) {
                    Log.v(TAG, "Message received from channel " + channel + ": " + message);

                    //Scroll to the last position
                    mMessagesListRecyclerView.scrollToPosition(simulatedMessages.size());
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

    private List<Message> createSimulatedMesages() {
        List<Message> simulatedMessages = new ArrayList<Message>();

        // Simulate sent message
        Message sentMessage = new Message();
        sentMessage.setSender(mDeviceId);
        sentMessage.setMessage("This is a sent message");

        // Simulate received message
        Message receivedMessage = new Message();
        receivedMessage.setSender("AnotherSender");
        receivedMessage.setMessage("This is a received message");

        simulatedMessages.add(sentMessage);
        simulatedMessages.add(receivedMessage);
        simulatedMessages.add(sentMessage);
        simulatedMessages.add(receivedMessage);
        simulatedMessages.add(receivedMessage);
        simulatedMessages.add(receivedMessage);
        simulatedMessages.add(sentMessage);
        simulatedMessages.add(sentMessage);
        simulatedMessages.add(receivedMessage);

        return simulatedMessages;
    }
}
