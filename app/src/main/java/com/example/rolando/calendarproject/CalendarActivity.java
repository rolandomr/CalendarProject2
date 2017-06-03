package com.example.rolando.calendarproject;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
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


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;


//import static com.example.rolando.calendarproject.R.id.currentCalendar;

public class CalendarActivity extends AppCompatActivity {


    private static final int CELLS_TO_SHOW = 42;
    private static final int MORNING_SHIFT = 7;
    private static final int AFTERNOON_SHIFT = 15;
    private static final int NIGHT_SHIFT = 23;
    //current displayed month, global variable
    private Calendar currentCalendar = Calendar.getInstance();
    private boolean isAdmin;

    //the list of dates that the worker has to work
    private HashSet<Calendar> workingDays = new HashSet<Calendar>();
    private HashSet<Calendar> holidays = new HashSet<Calendar>();
    private HashSet<Calendar> requestedHolidays = new HashSet<Calendar>();
    private int hourOfDay;
    private FirebaseDatabase mFirebaseDatabase;
    //references a specific part of the database
    private DatabaseReference mDatabaseReference;

    private DatabaseReference mDatabaseReferenceWorkers;

    private DatabaseReference mDatabaseReferenceHolidays;
    private DatabaseReference mDatabaseReferenceRequestedHolidays;
    private DatabaseReference mDatabaseReferenceShifts;
    private ChildEventListener mChildEventListener;
    private ChildEventListener mChildEventListenerDaysRequested;
    private ChildEventListener mChildEventlistenerShifts;
    //declare it now, assign value later
    private TextView monthName;
    private TextView currentYear;
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

        //workingDays = null;//reset on user changes

        hourOfDay = 7; //by default works in morning, 15 is afternoon and 23 is night

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                mUserID = "No userID";
                mUserName = "No userName";
            } else {
                mUserName = extras.getString("name");
                mUserID = extras.getString("userID");
                isAdmin = extras.getBoolean("admin");
            }
        } else {
            mUserName = (String) savedInstanceState.getSerializable("name");
            mUserID = (String) savedInstanceState.getSerializable("userID");
            isAdmin = (boolean) savedInstanceState.getBoolean("admin");
        }

        //mDatabaseReference = mFirebaseDatabase.getReference().child("workers");

        mDatabaseReference = mFirebaseDatabase.getReference().child("workers").child(mUserID);
        //check the databasereferences to see which one is needed when
        mDatabaseReferenceWorkers = mFirebaseDatabase.getReference().child("workers");
        mDatabaseReferenceShifts = mFirebaseDatabase.getReference().child("shifts");
        //mDatabaseReference = mFirebaseDatabase.getReference().child("list_of_workers");

        mDatabaseReferenceHolidays = mFirebaseDatabase.getReference().child("holidays");
        //mDatabaseReferenceRequestedHolidays = mFirebaseDatabase.getReference().child("requested_holidays/"+mUserID);
        mDatabaseReferenceRequestedHolidays = mFirebaseDatabase.getReference().child("requested_holidays");


        GridView daysOfWeek = (GridView) findViewById(R.id.week_days);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, daysNames);
        daysOfWeek.setAdapter(adapter2);

        final Button okButton = (Button) findViewById(R.id.ok_button);
        if (isAdmin){
            okButton.setText("OK");
        } else {
            okButton.setText("REQUEST HOLIDAY");
        }
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //when OK is clicked and before saving it to the database, i have to check
                //that the propper restrictions apply, that is number of hours per week, per year,...
                if (isAdmin) {
                    if (workingDays.size() == 0) {
                        Toast.makeText(getApplicationContext(), "No days have been selected", Toast.LENGTH_SHORT).show();
                    } else {
                        List<Long> listOfWorkingDays = makeListOfDatesLong(workingDays);
                        List<Long> listOfHolidays = makeListOfDatesLong(holidays);

                        if (checkHoursRestrictions(listOfWorkingDays)) {
                            //Worker currentWorker = new Worker(mUserName, mUserID, listOfWorkingDays);
                            Worker currentWorker = new Worker(mUserName, mUserID, listOfWorkingDays, listOfHolidays);
                            //mDatabaseReference.push().setValue(currentWorker);
                            //mDatabaseReference.child(mUserID).setValue(currentWorker);
                            mDatabaseReference.setValue(currentWorker);
                            mDatabaseReferenceShifts.child(mUserID).setValue(listOfWorkingDays);
                            Toast.makeText(getApplicationContext(), "Days added to the database", Toast.LENGTH_SHORT).show();
                            //we are gonna write workingDaysworkingDays to the database
                        }
                    }
                } else {
                    if (requestedHolidays.size() == 0) {
                        Toast.makeText(getApplicationContext(), "No days have been selected", Toast.LENGTH_SHORT).show();
                    } else {
                        List<Long> listOfHolidays = makeListOfDatesLong(requestedHolidays);
                        List<Long> listOfWorkingDays = new ArrayList<Long>();
                        if (workingDays != null) {
                            listOfWorkingDays = makeListOfDatesLong(workingDays);
                        }
                        //Worker currentWorker = new Worker(mUserName, mUserID, listOfHolidays);
                        //why should the worker write the whole worker to the DB, just needs to write requestedHolidays
                        //and set daysRequested to true, which later the administrator will listen to, is it necessary?
                        Worker currentWorker = new Worker(mUserName, mUserID, listOfWorkingDays, listOfHolidays);
                        //maybe should just add it to the holidays node
                        //mDatabaseReference.setValue(currentWorker);this would be forbidden by the rules
                        RequestedHolidays holidays_requested = new RequestedHolidays(listOfHolidays);
                        mDatabaseReferenceWorkers.child(mUserID).child("requestedHolidays").setValue(listOfHolidays);
                        //mDatabaseReferenceRequestedHolidays.child(mUserID).setValue(holidays_requested);
                        mDatabaseReferenceRequestedHolidays.child(mUserID).setValue(listOfHolidays);
                        //mDatabaseReferenceRequestedHolidays.setValue(listOfHolidays);
                        //mDatabaseReference.child("requestedHolidays").setValue(listOfHolidays);
                        //mDatabaseReference.child("daysRequested").setValue(true);
                        Toast.makeText(getApplicationContext(), "Holidays requested", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String estaestuID = user.getUid();
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
                currentCalendar.add(Calendar.MONTH, 1);
                //drawMonth(workingDays);
                //drawMonth(workingDays, holidays);
                drawMonth(workingDays, holidays, requestedHolidays);
            }
        });

        TextView previousMonth = (TextView) findViewById(R.id.previous_month);

        previousMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add a month to the current calendar
                currentCalendar.add(Calendar.MONTH, -1);
                drawMonth(workingDays, holidays, requestedHolidays);

            }
        });

//Have to differenciate between admin choosing shifts and worker choosing holidays
        calendarGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                TextView day_tv = (TextView) v;
                String goodDate = (String) day_tv.getText();
                int theDay = Integer.parseInt(goodDate);
                //Don't allow to change shifts today or before today
                Calendar datecuen = new GregorianCalendar(currentCalendar.get(Calendar.YEAR)
                        , currentCalendar.get(Calendar.MONTH), theDay, hourOfDay, 0);
                if (datecuen.get(Calendar.DAY_OF_YEAR) <= Calendar.getInstance().get(Calendar.DAY_OF_YEAR)) {
                    Toast.makeText(getApplicationContext(), "Cannot set shifts before today", Toast.LENGTH_SHORT).show();
                } else {
                    if (isAdmin) {
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
                        //if (workingDays != null) {//CAREFULL WITH THIS BECAUSE THE ADMINISTRATOR HAS TO BE ABLE TO SET THE DATES
                            if ((position < 7 && theDay > 24) || (position > 27 && theDay < 15)) {
                                Toast.makeText(getApplicationContext(), "Change month to pick this date", Toast.LENGTH_SHORT).show();
                            } else {//i could add/remove here from the database, or after the OK button, more logical
                                if (workingDays.contains(date)) {//if was previusly selected, deselect
                                    workingDays.remove(date);
                                } else if (workingDays.contains(dateMorning)) {
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
                                drawMonth(workingDays, holidays, requestedHolidays);
                                //drawMonth(workingDays);
                            }
                        //} //else workingDays.add(date);I ADDED THIS WHEN IT DIDN'T WORK WITH EWA and added above the if working!=null
                    } else {//is the user
                        Calendar date = new GregorianCalendar(currentCalendar.get(Calendar.YEAR)
                                , currentCalendar.get(Calendar.MONTH), theDay, hourOfDay, 0);
                        if (holidays != null) {//CAREFULL WITH THIS BECAUSE THE ADMINISTRATOR HAS TO BE ABLE TO SET THE DATES
                            if ((position < 7 && theDay > 24) || (position > 27 && theDay < 15)) {
                                Toast.makeText(getApplicationContext(), "Change month to pick this date", Toast.LENGTH_SHORT).show();
                            } else {//i could add/remove here from the database, or after the OK button, more logical
                                if (requestedHolidays.contains(date)) {//if was previusly selected, deselect
                                    requestedHolidays.remove(date);
                                } else {
                                    requestedHolidays.add(date);
                                }
                                drawMonth(workingDays, holidays,requestedHolidays);
                            }
                        } //else workingDays.add(date);I ADDED THIS WHEN IT DIDN'T WORK WITH EWA and added above the if working!=null
                    }
                }
            }
        });



        //mDatabaseReference.addChildEventListener(mChildEventListener);

        //mDatabaseReference.addChildEventListener(mChildEventListener);

        drawMonth(workingDays, holidays, requestedHolidays);//problably will need to add to workingDays the dates read from the database
        //drawMonth(workingDays);//problably will need to add to workingDays the dates read from the database
        //attachDatabaseReference_requested_holidays();
        attachDatabaseReference_workers();
        attachDatabaseReference_shifts();

        if (isAdmin){
            //attachDatabaseReference_generalCalendar();
        }
    }

    private void attachDatabaseReference_generalCalendar() {

    }

    private void attachDatabaseReference_shifts() {
        if (mChildEventlistenerShifts == null){
            mChildEventlistenerShifts = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Log.v("ChildAdded key",dataSnapshot.getKey().toString());
                    Log.v("ChildAdded value",dataSnapshot.getValue().toString());
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
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            mDatabaseReferenceShifts.addChildEventListener(mChildEventlistenerShifts);
        }
    }

    private void attachDatabaseReference_workers() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                //this one is called when days are added and also the first time the database is called
                //therefore should bring all the workingdays from the database
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    //Worker worker = dataSnapshot.getValue(Worker.class);
                    //Worker worker = dataSnapshot.child(mUserID).getValue(Worker.class);
                    Object object = dataSnapshot.getValue();
                    if (dataSnapshot.getKey().equals("workInts")) {
                        List<Long> longList = (List) dataSnapshot.getValue();
                        workingDays = ConvertHashToList(longList);
                    }
                    if (dataSnapshot.getKey().equals("holidays")) {
                        List<Long> longList = (List) dataSnapshot.getValue();
                        workingDays = ConvertHashToList(longList);
                    }
                    if (dataSnapshot.getKey().equals("requested_holidays")) {
                        List<Long> longList = (List) dataSnapshot.getValue();
                        workingDays = ConvertHashToList(longList);
                    }
                    if (dataSnapshot.getKey().equals("requestedHolidays")) {
                        List<Long> longList = (List) dataSnapshot.getValue();
                        requestedHolidays = ConvertHashToList(longList);
                    }
                    //drawMonth(workingDays, holidays, requestedHolidays);//here i should have the daya from the database
                    Worker worker = dataSnapshot.getValue(Worker.class);
                    String laestring = dataSnapshot.getKey();//i think this is the id
                    if (worker != null) {
                        if (laestring.equals(mUserID)) {
                            //if (worker.getNumber_id().equals(mUserID)) {
                            List<Long> longList = worker.getWorkInts();
                            List<Long> longListHolidays = worker.getHolidays();
                            List<Long> longRequested = worker.getRequestedHolidays();
                            workingDays = ConvertHashToList(longList);
                            holidays = ConvertHashToList(longListHolidays);
                            requestedHolidays = ConvertHashToList(longRequested);
                            drawMonth(workingDays, holidays, requestedHolidays);//here i should have the daya from the database
                        }
                    }

                    if (isAdmin) {

                        // 1. Instantiate an AlertDialog.Builder with its constructor
                        AlertDialog.Builder builder = new AlertDialog.Builder(CalendarActivity.this);
                        // 2. Chain together various setter methods to set the dialog characteristics
                        builder.setMessage(R.string.dialog_message).setTitle(R.string.dialog_title);

                        // 3. Get the AlertDialog from create()
                        //AlertDialog dialog = builder.create();
                        // Add the buttons
                        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User clicked OK button
                                Toast.makeText(getApplicationContext(),"Holidays accepted", Toast.LENGTH_SHORT).show();
                            }
                        });
                        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });
// Set other dialog properties
//...

// Create the AlertDialog
                        AlertDialog dialog = builder.create();

                        dialog.show();
                    }
                    //drawMonth(workingDays,holidays, requestedHolidays);//here i should have the daya from the database
                }
                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    String key = dataSnapshot.getKey();
                    Object data = dataSnapshot.getValue();
                    Toast.makeText(getApplicationContext(), "onChildChanged in Calendar Activity", Toast.LENGTH_LONG).show();
                    Log.i("**********", "childChanged " + dataSnapshot.toString());
                }
                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Toast.makeText(getApplicationContext(), "onChildRemoved in Calendar Activity", Toast.LENGTH_LONG).show();
                    Log.i("**********", "childRemoved " + dataSnapshot.toString());
                }
                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    Toast.makeText(getApplicationContext(), "onChildMoved in Calendar Activity", Toast.LENGTH_LONG).show();
                    Log.i("**********", "childMoved " + dataSnapshot.toString());
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {//normally means you don't have permission to read the data
                    Toast.makeText(getApplicationContext(), "onCancelled in Calendar Activity", Toast.LENGTH_LONG).show();
                    Log.v("*********",databaseError.toString());
                    //FirebaseCrash.report(databaseError.toException());
                    //FirebaseCrash.report(databaseError.toException());
                }
            };
            mDatabaseReferenceWorkers.addChildEventListener(mChildEventListener);
        }
    }


    private void attachDatabaseReference_requested_holidays() {
        //This childeventlistener will manage the operation when holidays have been requested
        if (mChildEventListenerDaysRequested == null) {
            mChildEventListenerDaysRequested = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Log.v("***********", "There has beem an addition in daysRequested");
                    //List<Long> longList = (List) dataSnapshot.getValue();
                    if (dataSnapshot.getKey().equals(mUserID)) {//check if it's the current user branch
                        RequestedHolidays reques =  new RequestedHolidays();
                        //reques = dataSnapshot.getValue(RequestedHolidays.class);
                        GenericTypeIndicator<List<Long>> t =
                                new GenericTypeIndicator<List<Long>>() {};

                        Object objecto = dataSnapshot.getValue();
                        //List<Long> messages = dataSnapshot.getValue(t);
                        //makeMapHash(objecto);
                        //HashMap<String, Long> map = new HashMap<String, Long>();
                        //map.put (1, "Mark");
                        //map.put (2, "Tarryn");
                        //map = (HashMap) dataSnapshot.getValue();
                        //List<Long> list = new ArrayList<Long>(map.values());

                        //List<Long> longList = (List) dataSnapshot.getValue();
                        List<Long> longList = (List<Long>) dataSnapshot.getValue();
                        requestedHolidays = ConvertHashToList(longList);
                        drawMonth(workingDays, holidays, requestedHolidays);
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Log.i("***********", "There has beem a change in daysRequested");
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.i("***********", "There has beem a removed in daysRequested");

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    Log.i("***********", "There has beem a moved in daysRequested");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(),"ONCANCELLED in /requested_holidays",Toast.LENGTH_LONG).show();
                    Log.i("***********", "ONCANCELLED IN /requested_holidays");
                }
            };
            //when days are added to the requestedHolidays in the DB this will trigger
            //mDatabaseReferenceHolidays.addChildEventListener(mChildEventListenerDaysRequested);
            mDatabaseReferenceRequestedHolidays.addChildEventListener(mChildEventListenerDaysRequested);
            //mDatabaseReference.addChildEventListener(mChildEventListenerDaysRequested);
        }
    }

    private boolean checkHoursRestrictions(List<Long> daysworked) {
        //40 hours per week
        //no more than 8 nights per month
        boolean isOk = true;
        if (daysworked.size() >= 225) {
            //this worker has
            Toast.makeText(getApplicationContext(), mUserName + " has more than 225 work shifts", Toast.LENGTH_SHORT).show();
            Toast.makeText(getApplicationContext(), mUserName + " has more than 225 work shifts", Toast.LENGTH_SHORT).show();
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
            if (pivotWeek == weekNumber) {
                if (shiftsInTheSameWeek > 5) {
                    Toast.makeText(getApplicationContext(), mUserName + " has more than 40 hours in week" + weekNumber, Toast.LENGTH_SHORT).show();
                    isOk = false;
                    break;//
                }
                pivotWeek = weekNumber;
            } else {
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
        for (Calendar date : workDays) {
            //SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY");
            days.add(sdf.format(date.getTime()));
        }

        return days;
    }

    private List<Long> makeListOfDatesLong(HashSet<Calendar> workDays) {
        //SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY");
        //SimpleDateFormat sdf = new SimpleDateFormat();
        List<Long> days = new ArrayList<>();
        if (workDays!= null) {
        for (Calendar date : workDays) {
            //SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY");
            //days.add(sdf.format(date.getTime()));
            days.add(date.getTimeInMillis());
        }
        }
        return days;
    }

    private HashSet<Calendar> makeMapHash(HashMap<String,Long> mapWithLongs) {
        HashSet<Calendar> dates = new HashSet<Calendar>();
        for (Long date : mapWithLongs.values()) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(date);
            dates.add(cal);
        }
        return dates;
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
        drawMonth(null, null, null);
    }

    //private String[] drawMonth(int year, int month) {
    private void drawMonth(HashSet<Calendar> workDays, HashSet<Calendar> holidays, HashSet<Calendar> requested) {
        //private void drawMonth(GridView calendar_grid) {
        Log.v("INSIDE DRAWYMONTH", "SEEMS TO BE WORKING OK");
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
        //CalendarAdapter adapter = new CalendarAdapter(this, days, workDays);

        //if (isAdmin){
          //  CalendarAdapterAdmin adapter = new CalendarAdapterAdmin(this, allWorkers);
        //} else {
            CalendarAdapter adapter = new CalendarAdapter(this, days, workDays, holidays, requested);
        //}
        //Can make another adapter for the Admin calendar that provides different type of day_items
        calendarGrid.setAdapter(adapter);
    }

    //need to fix this method, this is too ugly, list of months could be better,
    //even using the strings.xml in values should be better to allow different languages
    public String getMonthName(int monthNumber) {
        String month = "";
        switch (monthNumber) {
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
        switch (view.getId()) {
            case R.id.morning:
                if (checked)
                    hourOfDay = 7;
                break;
            case R.id.afternoon:
                if (checked)
                    hourOfDay = 15;
                break;
            case R.id.night:
                if (checked)
                    hourOfDay = 23;
                break;
        }
    }
}


/*
{
        "rules": {
        //The user the that uid is the admin, can read and write the entire DB
        ".read": "auth != null && auth.uid == 'X1VNCBi485dm0liBcHbmPHFcAyi1' ",
        ".write": "auth != null && auth.uid == 'X1VNCBi485dm0liBcHbmPHFcAyi1' ",

        "list_of_workers" : {
        "$user_id" : {
        ".read" : "$user_id === auth.uid",
        ".write": "$user_id === auth.uid"
        }
        }
        ,

        "workers" : {
        "$user_id" : {
        ".read" : "$user_id === auth.uid",
        //actually the users cannot write to this part of the database
        //they cannot modify their shifts, or can they?->Holidays
        "requestedHolidays" :{
        ".write": "$user_id === auth.uid"
        }
        }
        },
        "generalCalendar":{
        ".read" : "auth != null",
        ".write" : "auth != null"
        }
        }
        }

*/

/*
{
        "rules": {
        //The user the that uid is the admin, can read and write the entire DB
        ".read": "auth != null && auth.uid == 'X1VNCBi485dm0liBcHbmPHFcAyi1' ",
        ".write": "auth != null && auth.uid == 'X1VNCBi485dm0liBcHbmPHFcAyi1' ",

        "list_of_workers" : {
        "$user_id" : {
        ".read" : "$user_id === auth.uid",
        ".write": "$user_id === auth.uid"
        }
        }
        ,

        "workers" : {
        "$user_id" : {
        ".read" : "$user_id === auth.uid",
        "workInts" : {
        ".read" :"$user_id === auth.uid",
        },
        "holidays" :{
        ".read":"$user_id === auth.uid",
        },
        "requestedHolidays" :{
        ".write": "$user_id === auth.uid"
        }
        }
        },
        "generalCalendar":{
        ".read" : "auth != null",
        ".write" : "auth != null"
        }
        }
        }
*/

