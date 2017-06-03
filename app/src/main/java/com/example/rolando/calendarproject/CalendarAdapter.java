package com.example.rolando.calendarproject;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

//import static android.icu.text.RelativeDateTimeFormatter.Direction.THIS;

/**
 * Created by rolando on 22/4/17.
 */

public class CalendarAdapter extends ArrayAdapter<Calendar> {

    private HashSet<Calendar> workDays;
    private HashSet<Calendar> holidays;
    private HashSet<Calendar> requestedHolidays;
    //what if another set is added for holidays

    public CalendarAdapter(@NonNull Context context, @LayoutRes int resource) {
        super(context, resource);
    }


    //remember when i call the adapter i have to call specifying the resource = R.layout.day_item
    public CalendarAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<Calendar> objects, HashSet<Calendar> workDays) {
        super(context, resource, objects);
        this.workDays = workDays;

    }

    public CalendarAdapter(Context context, ArrayList<Calendar> days, HashSet<Calendar> workDays) {
        super(context, R.layout.day_item, days);
        this.workDays = workDays;
    }

    public CalendarAdapter(Context context, ArrayList<Calendar> days, HashSet<Calendar> workDays, HashSet<Calendar> holidays) {
        super(context, R.layout.day_item, days);
        this.workDays = workDays;
        this.holidays = holidays;
    }

    public CalendarAdapter(Context context, ArrayList<Calendar> days, HashSet<Calendar> workDays,
                           HashSet<Calendar> holidays, HashSet<Calendar> requestedHolidays) {
        super(context, R.layout.day_item, days);
        this.workDays = workDays;
        this.holidays = holidays;
        this.requestedHolidays = requestedHolidays;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Calendar date = getItem(position);//the position of the Calendar in the ArrayAdapter

        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH);
        int day = date.get(Calendar.DAY_OF_MONTH);

        View calView = convertView;
        if (calView == null) {
            calView = LayoutInflater.from(getContext()).inflate(R.layout.day_item, parent, false);
        }
//We're gonna set a different background color for those days that have been touched
        if (workDays != null) {
            for (Calendar workDay : workDays) {
                if (year == workDay.get(Calendar.YEAR) &&
                        month == workDay.get(Calendar.MONTH) &&
                        day == workDay.get(Calendar.DATE)) {
                    switch (workDay.get(Calendar.HOUR_OF_DAY)) {
                        case 7:
                            //calView.setBackgroundColor(0xffFFEB3B);
                            calView.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.morning_color));
                            //calView.setBackgroundResource(R.drawable.day_complete);
                            break;
                        case 15:
                            //calView.setBackgroundColor(0xff00ffff);
                            //calView.setBackgroundColor(0xffD500F9);
                            calView.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.afternoon_color));
                            break;
                        case 23:
                            //calView.setBackgroundColor(0xff00B0FF);
                            calView.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.night_color));
                            break;
                        default:
                            calView.setBackgroundColor(0xffff0000);
                            break;
                    }
                    break;
                }
            }
        }

        if (holidays != null) {
            for (Calendar holiday : holidays) {
                if (year == holiday.get(Calendar.YEAR) &&
                        month == holiday.get(Calendar.MONTH) &&
                        day == holiday.get(Calendar.DATE)) {
                    //PURPLE for accepted holidays
                    //calView.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.purple));
                    calView.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.accepted_holidays_color));
                }
            }
        }

        if (requestedHolidays!= null) {
            for (Calendar holiday : requestedHolidays) {
                if (year == holiday.get(Calendar.YEAR) &&
                        month == holiday.get(Calendar.MONTH) &&
                        day == holiday.get(Calendar.DATE)) {
                    //ORANGE for requested holidays
                    //calView.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.orange));
                    calView.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.requested_holidays_color));
                }
            }
        }
        ((TextView) calView).setText(String.valueOf(date.get(Calendar.DATE)));
        return calView;
    }
}
