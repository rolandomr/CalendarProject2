package com.example.rolando.calendarproject;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Calendar;
import java.util.HashSet;

/**
 * Created by rolando on 31/5/17.
 */

class CalendarAdapterAdmin extends ArrayAdapter<Calendar>{

    private HashSet<Calendar> allWorkingDays;

    public CalendarAdapterAdmin(@NonNull Context context, @LayoutRes int resource) {
        super(context, resource);
    }

    public CalendarAdapterAdmin(@NonNull Context context, @LayoutRes int resource, @IdRes int textViewResourceId, HashSet<Calendar> allWorkingDays) {
        super(context, resource, textViewResourceId);
        this.allWorkingDays = allWorkingDays;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //return super.getView(position, convertView, parent);
        Calendar date = getItem(position);//the position of the Calendar in the ArrayAdapter

        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH);
        int day = date.get(Calendar.DAY_OF_MONTH);
        View calView = convertView;

        if (calView == null) {
            calView = LayoutInflater.from(getContext()).inflate(R.layout.day_item, parent, false);
        }


        ((TextView) calView).setText(String.valueOf(date.get(Calendar.DATE)));

        return calView;
    }

}
