package com.example.rolando.calendarproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static com.example.rolando.calendarproject.R.id.calendar_grid;
import static java.security.AccessController.getContext;


//import static com.example.rolando.calendarproject.R.id.currentCalendar;

public class CalendarActivity extends AppCompatActivity {


    private static final int CELLS_TO_SHOW = 42;
    private static final int MORNING_SHIFT = 7;
    private static final int AFTERNOON_SHIFT = 15;
    private static final int NIGHT_SHIFT = 23;
    //current displayed month, global variable
    private Calendar currentCalendar = Calendar.getInstance();

    //the list of dates that the worker has to work
    private HashSet<Calendar> workingDays = new HashSet<Calendar>();
    private int hourOfDay;
    private FirebaseDatabase mFirebaseDatabase;
    //references a specific part of the database
    private DatabaseReference mDatabaseReference;

    private DatabaseReference mDatabaseReferenceWorkers;

    private ChildEventListener mChildEventListener;
    //declare it now, assign value later
    private TextView monthName;
    private  TextView currentYear;
    //int month = currentCalendar.get(Calendar.MONTH);
    //int year = currentCalendar.get(Calendar.YEAR);
    int day = currentCalendar.get(Calendar.DAY_OF_MONTH);
    //String[] tal = populateMonth(month);

    private String mUserID;
    private String mUserName;

    //Log.v("El mes", "El valor de get(1) es "+month);
    private final String[] daysNames = new String[]{"M", "Tu", "W", "Th", "Fr", "Sa", "Su"};
    GridView calendarGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        calendarGrid = (GridView) findViewById(R.id.calendar_grid);

        mFirebaseDatabase = FirebaseDatabase.getInstance();

        workingDays = null;//reset on user changes
        hourOfDay = 7; //by default works in morning, 15 is afternoon and 23 is night

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                mUserID = "No userID";
                mUserName = "No userName";
            } else {
                mUserName = extras.getString("name");
                mUserID = extras.getString("userID");
            }
        } else {
            mUserName= (String) savedInstanceState.getSerializable("name");
            mUserID= (String) savedInstanceState.getSerializable("userID");
        }

        //mDatabaseReference = mFirebaseDatabase.getReference().child("workers");

        mDatabaseReference = mFirebaseDatabase.getReference().child("workers").child(mUserID);
        //check the databasereferences to see which one is needed when
        mDatabaseReferenceWorkers = mFirebaseDatabase.getReference().child("workers");

        GridView daysOfWeek = (GridView) findViewById(R.id.week_days);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, daysNames);
        daysOfWeek.setAdapter(adapter2);

        Button okButton = (Button) findViewById(R.id.ok_button);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //when OK is clicked and before saving it to the database, i have to check
                //that the propper restrictions apply, that is number of hours per week, per year,...
                List<Long> listOfWorkingDays = makeListOfDatesLong(workingDays);
                if (checkHoursRestrictions(listOfWorkingDays)) {
                    Worker currentWorker = new Worker(mUserName,mUserID,listOfWorkingDays);
                    //mDatabaseReference.push().setValue(currentWorker);
                    //mDatabaseReference.child(mUserID).setValue(currentWorker);
                    mDatabaseReference.setValue(currentWorker);
                    Toast.makeText(getApplicationContext(),"Days added to the database", Toast.LENGTH_SHORT).show();
                    //we are gonna write workingDaysworkingDays to the database
                }
            }
        });

        monthName = (TextView) findViewById(R.id.current_month);
        currentYear = (TextView) findViewById(R.id.current_year);
        //int elMes = currentCalendar.get(Calendar.MONTH);
        //monthName.setText(getMonthName(elMes));
        //monthName.setText(getMonthName(currentCalendar.get(Calendar.MONTH)));


        //when next or previous month are clicked i have to draw the correct month (current_month+1)
        //passing the workingDays in case we go back and forth to a month where some days
        //have already been selected, or in case the come from the database

        TextView nextMonth = (TextView) findViewById(R.id.next_month);

        nextMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add a month to the current calendar
                currentCalendar.add(Calendar.MONTH,1);
                drawMonth(workingDays);
            }
        });

        TextView previousMonth = (TextView) findViewById(R.id.previous_month);

        previousMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add a month to the current calendar
                currentCalendar.add(Calendar.MONTH,-1);
                drawMonth(workingDays);
            }
        });


        calendarGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                TextView day_tv = (TextView) v;
                String goodDate = (String) day_tv.getText();
                int theDay = Integer.parseInt(goodDate);

                Calendar date = new GregorianCalendar(currentCalendar.get(Calendar.YEAR)
                        , currentCalendar.get(Calendar.MONTH), theDay, hourOfDay, 0);

                Calendar dateMorning = new GregorianCalendar(currentCalendar.get(Calendar.YEAR)
                        , currentCalendar.get(Calendar.MONTH), theDay, 7, 0);

                Calendar dateAfternoon = new GregorianCalendar(currentCalendar.get(Calendar.YEAR)
                        , currentCalendar.get(Calendar.MONTH), theDay, 15, 0);

                Calendar dateNight = new GregorianCalendar(currentCalendar.get(Calendar.YEAR)
                        , currentCalendar.get(Calendar.MONTH), theDay, 23, 0);

                //Morning, Afternoon and Night i can also add a time to Calendar date depending
                //And in the adapter check which one it is to paint it accordingly
                if ((position < 7 && theDay > 24) || (position > 27 && theDay < 15)) {
                    Toast.makeText(getApplicationContext(),"Change month to pick this date", Toast.LENGTH_SHORT).show();
                } else {//i could add/remove here from the database, or after the OK button, more logical
                    if (workingDays.contains(date)) {//if was previusly selected, deselect
                        workingDays.remove(date);
                    } else if (workingDays.contains(dateMorning)){
                        workingDays.remove(dateMorning);
                        workingDays.add(date);
                    } else if (workingDays.contains(dateAfternoon)) {
                        workingDays.remove(dateAfternoon);
                        workingDays.add(date);
                    } else if (workingDays.contains(dateNight)) {
                        workingDays.remove(dateNight);
                        workingDays.add(date);
                    } else {
                        workingDays.add(date);
                    }
                    drawMonth(workingDays);
                }

            }
        });
        mChildEventListener = new ChildEventListener() {
            @Override
            //this one is called when days are added and also the first time the database is called
            //therefore should bring all the workingdays from the database
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                //Worker worker = dataSnapshot.getValue(Worker.class);
                //Worker worker = dataSnapshot.child(mUserID).getValue(Worker.class);
                Worker worker = dataSnapshot.getValue(Worker.class);
                if (worker != null) {
                    if (worker.getNumber_id().equals(mUserID)) {
                        List<Long> longList = worker.getWorkInts();

                        workingDays = ConvertHashToList(longList);
                    }
                }
                drawMonth(workingDays);//here i should have the daya from the database
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {//normally means you don't have permission to read the data
            }
        };
        //mDatabaseReference.addChildEventListener(mChildEventListener);
        mDatabaseReferenceWorkers.addChildEventListener(mChildEventListener);

        drawMonth(workingDays);//problably will need to add to workingDays the dates read from the database
    }

    private boolean checkHoursRestrictions(List<Long> daysworked) {
        //40 hours per week
        //no more than 8 nights per month
        boolean isOk = true;
        if (daysworked.size() >= 225) {
            //this worker has
            Toast.makeText(getApplicationContext(),mUserName + " has more than 225 work shifts", Toast.LENGTH_SHORT).show();
            Toast.makeText(getApplicationContext(),mUserName + " has more than 225 work shifts", Toast.LENGTH_SHORT).show();
            isOk = false;
        }
        Collections.sort(daysworked);
        int shiftsInTheSameWeek = 0;
        int pivotWeek = -1;
        int weekNumber = -1;
        for (Long day : daysworked) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(day);
            weekNumber = cal.get(Calendar.WEEK_OF_YEAR);
            shiftsInTheSameWeek++;
            if (pivotWeek == weekNumber){
                if (shiftsInTheSameWeek>5) {
                    Toast.makeText(getApplicationContext(),mUserName + " has more than 40 hours in week"+weekNumber, Toast.LENGTH_SHORT).show();
                    isOk = false;
                    break;//
                }
                pivotWeek = weekNumber;
            } else{
                shiftsInTheSameWeek = 1;
                pivotWeek = weekNumber;
            }
        }
        return isOk;
    }


    //just turns the HashSet<Calendar> into List<String> to be able to store it in the database
    private List<String> makeListOfDates(HashSet<Calendar> workDays) {
        //SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY");
        SimpleDateFormat sdf = new SimpleDateFormat();
        List<String> days = new ArrayList<>();
        for(Calendar date : workDays) {
            //SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY");
            days.add(sdf.format(date.getTime()));
        }

        return days;
    }

    private List<Long> makeListOfDatesLong(HashSet<Calendar> workDays) {
        //SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY");
        //SimpleDateFormat sdf = new SimpleDateFormat();
        List<Long> days = new ArrayList<>();
        for(Calendar date : workDays) {
            //SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY");
            //days.add(sdf.format(date.getTime()));
            days.add(date.getTimeInMillis());
        }
        return days;
    }


    private HashSet<Calendar> ConvertHashToList(List<Long> datesinDB) {
        HashSet<Calendar> dates = new HashSet<Calendar>();
        //Calendar cal = Calendar.getInstance();
        //Calendar cal = new GregorianCalendar(currentCalendar.get(Calendar.YEAR)
          //      , currentCalendar.get(Calendar.MONTH), currentCalendar.get(Calendar.DAY_OF_MONTH));
        for (Long date : datesinDB) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(date);
            //Long ago = cal.getTimeInMillis();//this is to actually change the cal value
            dates.add(cal);
        }
        return dates;
    }


    private void drawMonth() {
        drawMonth(null);
    }
    //private String[] drawMonth(int year, int month) {
    private void drawMonth(HashSet<Calendar> workDays) {
        //private void drawMonth(GridView calendar_grid) {

        ArrayList<Calendar> days = new ArrayList<>();

        //final Calendar currentCalendar = Calendar.getInstance();
        //year = currentCalendar.get(Calendar.YEAR);
        //month = currentCalendar.get(Calendar.MONTH);
        Calendar auxCal = (Calendar) currentCalendar.clone();
        auxCal.setFirstDayOfWeek(Calendar.MONDAY);
        auxCal.set(Calendar.DAY_OF_MONTH, 1);

        int monthStartCell = auxCal.get(Calendar.DAY_OF_WEEK) - 2;

        auxCal.add(Calendar.DAY_OF_MONTH, -monthStartCell);

        for (int i = 0; i < CELLS_TO_SHOW; i++) {
            days.add((Calendar) auxCal.clone());
            auxCal.add(Calendar.DAY_OF_MONTH, 1);
        }
        //need to update the current month as well
        monthName.setText(getMonthName(currentCalendar.get(Calendar.MONTH)));

        //Very ugly work in the plane
        Integer theYear = new Integer(currentCalendar.get(Calendar.YEAR));
        currentYear.setText(theYear.toString());


        CalendarAdapter adapter = new CalendarAdapter(this, days, workDays);

        calendarGrid.setAdapter(adapter);


    }

    //need to fix this method, this is too ugly, list of months could be better,
    //even using the strings.xml in values should be better to allow different languages
    public String getMonthName(int monthNumber){
        String month = "";
        switch (monthNumber){
            case 0:
                month = "January";
                break;
            case 1:
                month = "February";
                break;
            case 2:
                month = "March";
                break;
            case 3:
                month = "April";
                break;
            case 4:
                month = "May";
                break;
            case 5:
                month = "June";
                break;
            case 6:
                month = "July";
                break;
            case 7:
                month = "August";
                break;
            case 8:
                month = "September";
                break;
            case 9:
                month = "October";
                break;
            case 10:
                month = "November";
                break;
            case 11:
                month = "December";
                break;
            default:
                month = "Invalid Month";
                break;
        }
        return month;

    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?

        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.morning:
                if (checked)
                    hourOfDay = 7;
                    break;
            case R.id.afternoon:
                if (checked)
                    hourOfDay = 15;
                    break;
            case R.id.night:
                if(checked)
                    hourOfDay = 23;
                    break;
        }
    }

}
