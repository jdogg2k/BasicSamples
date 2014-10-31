package com.jorose.initially;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by jrose on 10/30/2014.
 */
public class FinalScoreDialogFragment extends DialogFragment {

    public interface FinalScoreDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    FinalScoreDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (FinalScoreDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // This example shows how to add a custom layout to an AlertDialog
        LayoutInflater factory = getActivity().getLayoutInflater();

        final View scoreView = factory.inflate(R.layout.final_score, null);
        ListView playerList=(ListView)scoreView.findViewById(R.id.playerList);
        ListView opponentList = (ListView)scoreView.findViewById(R.id.opponentList);
        TextView pName = (TextView)scoreView.findViewById(R.id.playerName);
        TextView oName = (TextView)scoreView.findViewById(R.id.opponentName);
        TextView pScoreBox = (TextView)scoreView.findViewById(R.id.playerScore);
        TextView oScoreBox = (TextView)scoreView.findViewById(R.id.opponentScore);
        ImageView pWinner = (ImageView)scoreView.findViewById(R.id.winnerP);
        ImageView oWinner = (ImageView)scoreView.findViewById(R.id.winnerO);

        pName.setText(getArguments().getString("pName"));
        oName.setText(getArguments().getString("oName"));

        ArrayList<String> rawGuessesP = getArguments().getStringArrayList("pGuesses");
        ArrayList<String> rawGuessesO = getArguments().getStringArrayList("oGuesses");

        ArrayList<Score> pScores = new ArrayList<Score>();
        ArrayList<Score> oScores = new ArrayList<Score>();

        for(String s : rawGuessesP)  {
            Score score = new Score(s, true);
            if (rawGuessesO.contains(s)){
                score.valid = false;
            }
            pScores.add(score);
        }

        for(String s : rawGuessesO)  {
            Score score = new Score(s, true);
            if (rawGuessesP.contains(s)){
                score.valid = false;
            }
            oScores.add(score);
        }

        int pFinalScore = getArguments().getInt("pFinal");
        int oFinalScore = getArguments().getInt("oFinal");

        if (pFinalScore > oFinalScore) {
            pWinner.setVisibility(View.VISIBLE);
        } else if (oFinalScore > pFinalScore) {
            oWinner.setVisibility(View.VISIBLE);
        } else {
            pWinner.setVisibility(View.VISIBLE);
            oWinner.setVisibility(View.VISIBLE);
        }

        pScoreBox.setText(String.valueOf(pFinalScore));
        oScoreBox.setText(String.valueOf(oFinalScore));

        ScoresAdapter playerArrayAdapter = new ScoresAdapter(getActivity(), pScores );
        ScoresAdapter opponentArrayAdapter = new ScoresAdapter(getActivity(), oScores );

        playerList.setAdapter(playerArrayAdapter);
        opponentList.setAdapter(opponentArrayAdapter);

        return new AlertDialog.Builder(getActivity())
                .setTitle("Match Recap")
                .setView(scoreView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mListener.onDialogPositiveClick(FinalScoreDialogFragment.this);
                    }
                })
                .create();

    }
}