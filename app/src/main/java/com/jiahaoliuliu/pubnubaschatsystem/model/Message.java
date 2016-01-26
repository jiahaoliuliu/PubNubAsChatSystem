package com.jiahaoliuliu.pubnubaschatsystem.model;

import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Jiahao on 1/26/16.
 */
public class Message {

    private static final String TAG = "Message";

    private String sender;
    private String message;

    public Message() {
    }

    public Message(String sender, String message) {
        this();
        this.sender = sender;
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Message{" +
                "sender='" + sender + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}

