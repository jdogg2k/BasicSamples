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
    public String p1_guesses = "";
    public String p2_guesses = "";
    public String p1_name = "";
    public String p2_name = "";
    public String winner = "";
    public int turnCounter;
    public int winScore;
    public int loseScore;

    public GameTurn() {
    }

    // This is the byte array we will write out to the TBMP API.
    public byte[] persist() {
        JSONObject retVal = new JSONObject();

        try {
            retVal.put("initials", initials);
            retVal.put("turnCounter", turnCounter);
            retVal.put("winScore", winScore);
            retVal.put("loseScore", loseScore);
            retVal.put("p1_guesses", p1_guesses);
            retVal.put("p2_guesses", p2_guesses);
            retVal.put("p1_name", p1_name);
            retVal.put("p2_name", p2_name);
            retVal.put("winner", winner);

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
            if (obj.has("turnCounter")) {
                retVal.turnCounter = obj.getInt("turnCounter");
            }
            if (obj.has("loseScore")) {
                retVal.loseScore = obj.getInt("loseScore");
            }
            if (obj.has("winScore")) {
                retVal.winScore = obj.getInt("winScore");
            }
            if (obj.has("p1_guesses")) {
                retVal.p1_guesses = obj.getString("p1_guesses");
            }
            if (obj.has("p2_guesses")) {
                retVal.p2_guesses = obj.getString("p2_guesses");
            }
            if (obj.has("p1_name")) {
                retVal.p1_name = obj.getString("p1_name");
            }
            if (obj.has("p2_name")) {
                retVal.p2_name = obj.getString("p2_name");
            }
            if (obj.has("winner")) {
                retVal.winner = obj.getString("winner");
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return retVal;
    }
}
