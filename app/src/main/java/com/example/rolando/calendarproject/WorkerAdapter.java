package com.example.rolando.calendarproject;

import android.app.Activity;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by rolando on 15/4/17.
 */

public class WorkerAdapter extends ArrayAdapter<Worker> {

    public WorkerAdapter (Activity context, ArrayList<Worker> workers) {
        super(context,0, workers);
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
// Check if the existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if(listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }

        // Get the {@link AndroidFlavor} object located at this position in the list
        Worker currentWorker = getItem(position);

        // Find the TextView in the list_item.xml layout with the ID version_name
        TextView nameTextView = (TextView) listItemView.findViewById(R.id.name);
        TextView numberTextView = (TextView) listItemView.findViewById(R.id.number_id);

        // Get the version name from the current AndroidFlavor object and
        // set this text on the name TextView
        //nameTextView.setText(currentWorker.getName());

        // Find the TextView in the list_item.xml layout with the ID version_number
        //TextView numberTextView = (TextView) listItemView.findViewById(R.id.number_id);
        // Get the version number from the current AndroidFlavor object and
        // set this text on the number TextView
        if (currentWorker.getNumber_id().equals("X1VNCBi485dm0liBcHbmPHFcAyi1")){//
            numberTextView.setText("");
            nameTextView.setText("General Calendar");
            //listItemView.setBackgroundResource(R.color.linkColor);
        } else {
            numberTextView.setText(currentWorker.getNumber_id());
            nameTextView.setText(currentWorker.getName());

        }


        // Find the ImageView in the list_item.xml layout with the ID list_item_icon
        //ImageView iconView = (ImageView) listItemView.findViewById(R.id.list_item_icon);
        // Get the image resource ID from the current AndroidFlavor object and
        // set the image to iconView
        //iconView.setImageResource(currentAndroidFlavor.getImageResourceId());

        // Return the whole list item layout (containing 2 TextViews and an ImageView)
        // so that it can be shown in the ListView
        return listItemView;    }
}
