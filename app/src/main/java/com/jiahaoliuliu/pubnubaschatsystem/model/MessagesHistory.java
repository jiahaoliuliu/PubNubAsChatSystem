package com.jiahaoliuliu.pubnubaschatsystem.model;

/**
 * Created by Jiahao on 1/27/16.
 */

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * A class used to save the historical messages received from
 * PubNub.
 *
 * Created by Jiahao on 12/1/15.
 */
public class MessagesHistory {

    private static final String TAG = "ChatHistoricalMessages";

    private static final int MESSAGES_LIST_POSITION = 0;
    private List<Message> messagesList;

    private static final int START_TIME_POSITION = 1;
    private long startTime;

    private static final int END_TIME_POSITION = 2;
    private long endTime;

    public MessagesHistory() {
    }

    /**
     * Create json historical messages from a json array string.
     * The json array string has three elements
     * 1. The json array of the messages
     * 2. The start time of the first message
     * 3. The finish time of the last message
     *
     * @param jsonArrayString
     *      The json array string where get the data from
     */
    public MessagesHistory(String jsonArrayString) {
        this();
        try {
            JSONArray jsonArray = new JSONArray(jsonArrayString);

            // Got the list of messages
            this.messagesList = new ArrayList<Message>();
            JSONArray messagesJsonArray = jsonArray.getJSONArray(MESSAGES_LIST_POSITION);
            for (int i = 0; i < messagesJsonArray.length(); i++) {
                try {
                    Message message = new Message(messagesJsonArray.getString(i));
                    this.messagesList.add(message);
                } catch (IllegalArgumentException illegalArgumentException) {
                    Log.e(TAG, "Error getting the chat message. Skipping it." +
                            messagesJsonArray.getString(i));
                }
            }

            // Got the start time
            this.startTime = jsonArray.getLong(START_TIME_POSITION);

            // Got the end time
            this.endTime = jsonArray.getLong(END_TIME_POSITION);

        } catch (JSONException e) {
            Log.e(TAG, "Error getting the historical messages");
            throw new IllegalArgumentException(
                    "The json object is not correctly formatted. " + jsonArrayString, e);
        }
    }

    public MessagesHistory(long startTime, long endTime, List<Message> messagesList) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.messagesList = messagesList;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public List<Message> getMessagesList() {
        return messagesList;
    }

    public void setMessagesList(List<Message> messagesList) {
        this.messagesList = messagesList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessagesHistory that = (MessagesHistory) o;

        if (startTime != that.startTime) return false;
        if (endTime != that.endTime) return false;
        return !(messagesList != null ? !messagesList.equals(that.messagesList) : that.messagesList != null);

    }

    @Override
    public int hashCode() {
        int result = messagesList != null ? messagesList.hashCode() : 0;
        result = 31 * result + (int) (startTime ^ (startTime >>> 32));
        result = 31 * result + (int) (endTime ^ (endTime >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "MessagesHistory{" +
                "messagesList=" + messagesList +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
