package com.jorose.initially;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by jrose on 9/29/2014.
 */
public class ScoresAdapter extends ArrayAdapter<Score> {
    public ScoresAdapter(Context context, ArrayList<Score> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Score score = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.scorerow, parent, false);
        }
        // Lookup view for data population
        TextView personName = (TextView) convertView.findViewById(R.id.score_person);
        // Populate the data into the template view using the data object
        personName.setText(score.person);
        if (!score.valid){
            personName.setPaintFlags(personName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            personName.setPaintFlags(personName.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
        }
        // Return the completed view to render on screen
        return convertView;
    }
}

