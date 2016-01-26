package com.jiahaoliuliu.pubnubaschatsystem;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jiahaoliuliu.pubnubaschatsystem.model.Message;

import java.util.List;

/**
 * Created by Jiahao on 1/25/16.
 */
public class MessagesListAdapter extends RecyclerView.Adapter<MessagesListAdapter.ViewHolder> {

    private enum MessageType {
        MESSAGE_SENT, MESSAGE_RECEIVED;
    }

    /**
     * The unique id of the device. This is used to know if the message belong to this user
     * or another one
     */
    private String mDeviceId;

    /**
     * The list of messages.
     */
    private List<Message> mMessagesList;

    // The viewholder for the images
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mTextView;
        public ViewHolder(View view) {
            super(view);
            mTextView = (TextView)view.findViewById(R.id.message_content_text_view);
        }
    }

    public MessagesListAdapter(String deviceId, List<Message> messageList) {
        super();
        this.mDeviceId = deviceId;
        this.mMessagesList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = mMessagesList.get(position);
        if (message.getSender().equals(mDeviceId)) {
            return MessageType.MESSAGE_SENT.ordinal();
        } else {
            return MessageType.MESSAGE_RECEIVED.ordinal();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MessageType messageType = MessageType.values()[viewType];
        switch (messageType) {
            case MESSAGE_SENT:
                View sentMessageView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.sent_message_layout, parent, false);
                return new ViewHolder(sentMessageView);
            default:
            case MESSAGE_RECEIVED:
                View receivedMessageView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.received_message_layout, parent, false);
                return new ViewHolder(receivedMessageView);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Set the element
        Message message = mMessagesList.get(position);
        holder.mTextView.setText(message.getMessage());
    }

    @Override
    public int getItemCount() {
        return mMessagesList.size();
    }

    public void onMessageReceived(Message message) {
        mMessagesList.add(message);
        notifyDataSetChanged();
    }
}
