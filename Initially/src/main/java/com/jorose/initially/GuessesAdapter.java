package com.jorose.initially;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
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
        ImageView guessImage = (ImageView) convertView.findViewById(R.id.guess_image);
        guessName.setText(guess.name);
        if (!guess.result){
            guessName.setTextColor(getContext().getResources().getColor(R.color.darkred));
        } else {
            guessName.setTextColor(getContext().getResources().getColor(R.color.green));
        }

        String pLink = "https://www.googleapis.com/freebase/v1/image" + guess.personID;

        new DownloadImageTask(guessImage).execute(pLink);

        // Return the completed view to render on screen
        return convertView;
    }

    class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            bmImage.setImageBitmap(result);
        }
    }
}
