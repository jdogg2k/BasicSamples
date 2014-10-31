package com.jorose.initially;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by jrose on 9/29/2014.
 */
public class GuessesAdapter extends ArrayAdapter<Guess> {
    public GuessesAdapter(Context context, ArrayList<Guess> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Guess guess = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.guessrow, parent, false);
        }
        // Lookup view for data population
        TextView guessName = (TextView) convertView.findViewById(R.id.guess_name);
        TextView guessResult = (TextView) convertView.findViewById(R.id.guess_result);
        // Populate the data into the template view using the data object
        guessName.setText(guess.name);
        guessResult.setText(String.valueOf(guess.result));
        if (!guess.result){
            guessResult.setBackgroundColor(getContext().getResources().getColor(R.color.darkred));
        } else {
            guessResult.setBackgroundColor(getContext().getResources().getColor(R.color.green));
        }
        // Return the completed view to render on screen
        return convertView;
    }
}
