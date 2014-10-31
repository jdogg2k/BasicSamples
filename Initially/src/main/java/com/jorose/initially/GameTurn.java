package com.jorose.initially;

/**
 * Created by jrose on 10/30/2014.
 */

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class GameTurn {


    public static final String TAG = "EBTurn";

    public String initials = "";
    public String correctGuesses = "";
    public int turnCounter;

    public GameTurn() {
    }

    // This is the byte array we will write out to the TBMP API.
    public byte[] persist() {
        JSONObject retVal = new JSONObject();

        try {
            retVal.put("initials", initials);
            retVal.put("correctGuesses", correctGuesses);
            retVal.put("turnCounter", turnCounter);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String st = retVal.toString();

        Log.d(TAG, "==== PERSISTING\n" + st);

        return st.getBytes(Charset.forName("UTF-16"));
    }

    // Creates a new instance of SkeletonTurn.
    static public GameTurn unpersist(byte[] byteArray) {

        if (byteArray == null) {
            Log.d(TAG, "Empty array---possible bug.");
            return new GameTurn();
        }

        String st = null;
        try {
            st = new String(byteArray, "UTF-16");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
            return null;
        }

        Log.d(TAG, "====UNPERSIST \n" + st);

        GameTurn retVal = new GameTurn();

        try {
            JSONObject obj = new JSONObject(st);

            if (obj.has("initials")) {
                retVal.initials = obj.getString("initials");
            }
            if (obj.has("correctGuesses")) {
                retVal.correctGuesses = obj.getString("correctGuesses");
            }
            if (obj.has("turnCounter")) {
                retVal.turnCounter = obj.getInt("turnCounter");
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return retVal;
    }
}
