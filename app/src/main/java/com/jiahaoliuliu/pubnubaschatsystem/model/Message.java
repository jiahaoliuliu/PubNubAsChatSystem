package com.jiahaoliuliu.pubnubaschatsystem.model;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

/**
 * @author <a href="mailto:jiahaoliuliu@gmail.com">Jiahao Liu Liu</a>
 */
public class Message {

    private static final String TAG = "Message";

    public static final String JSON_FIELD_SENDER_KEY = "sender";
    private String sender;

    public static final String JSON_FIELD_MESSAGE_KEY = "message";
    private String message;

    // Extra keys
    private static final String PUBNUB_GCM_KEY = "pn_gcm";
    private static final String PUBNUB_GCM_DATA_KEY = "data";

    public Message() {
    }

    public Message (String jsonObjectString) {
        if (!jsonObjectString.contains(JSON_FIELD_SENDER_KEY) ||
                !jsonObjectString.contains(JSON_FIELD_MESSAGE_KEY)) {
            throw new IllegalArgumentException("The json object is not valid: " + jsonObjectString);
        }

        try {
            JSONObject jsonObject = new JSONObject(jsonObjectString);
            this.sender = jsonObject.getString(JSON_FIELD_SENDER_KEY);
            this.message = jsonObject.getString(JSON_FIELD_MESSAGE_KEY);
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing the json object");
            throw new IllegalArgumentException("The json object is not valid:" + jsonObjectString);
        }
    }


    public Message(String sender, String message) {
        this();
        this.sender = sender;
        this.message = message;
    }

    /**
     * Check if the current message is valid
     * @return
     */
    public boolean isValid() {
        return !TextUtils.isEmpty(getSender()) &&
                !TextUtils.isEmpty(getMessage());
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

    public JSONObject toJsonObject() {
        try {
            JSONObject jsonObject = new JSONObject();

            // All the needed field to send the message
            jsonObject.put(JSON_FIELD_SENDER_KEY, getSender());
            jsonObject.put(JSON_FIELD_MESSAGE_KEY, getMessage());

            // GCM data
            JSONObject gcmDataJsonObject = new JSONObject();
            gcmDataJsonObject.put(JSON_FIELD_SENDER_KEY, getSender());
            gcmDataJsonObject.put(JSON_FIELD_MESSAGE_KEY, getMessage());

            JSONObject gcmJsonObject = new JSONObject();
            gcmJsonObject.put(PUBNUB_GCM_DATA_KEY, gcmDataJsonObject);
            jsonObject.put(PUBNUB_GCM_KEY, gcmJsonObject);

            // TODO: To send push tokens to iOS devices, special fields should be attached to the
            // json object

            return jsonObject;
        } catch (JSONException e) {
            // This should never happen
            Log.e(TAG, "Error transforming the data into json object");
            return null;
        }
    }

    @Override
    public String toString() {
        return "Message{" +
                "sender='" + sender + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}

