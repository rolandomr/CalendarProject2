package com.example.rolando.calendarproject;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;

//will define the constant myself
//import static com.firebase.ui.auth.ui.AcquireEmailHelper.RC_SIGN_IN;

public class MainActivity extends AppCompatActivity {

    private static final String ANONYMOUS = "anonymous";
    private static final String ADMIN_ID = "X1VNCBi485dm0liBcHbmPHFcAyi1";
    public static final int RC_SIGN_IN = 1;
    private boolean isAdmin;
    //Firebase instance variables
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference paralapruebadelosworkers;
    private FirebaseAuth mFirebaseAuth;
    private ChildEventListener mChildEventListener;
    private ChildEventListener childeventparaworkers;

    private WorkerAdapter mWorkerAdapter;
    private ArrayList<Worker> list_of_workers = new ArrayList<Worker>();
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private String mUsername;
    private String mUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUsername = ANONYMOUS;
        mUserID = "Initial userID";
        //Initialize Firebase Components
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference().child("list_of_workers");
        paralapruebadelosworkers = mFirebaseDatabase.getReference().child("workers");
        mFirebaseAuth = FirebaseAuth.getInstance();

        //unless there is already a list of people logged in, probably there is but i need to store
        //them for long term service, not just currently connected


        LinearLayout rootview = (LinearLayout) findViewById(R.id.rootView);
        TextView persona = (TextView) findViewById(R.id.abre_calendario);


        mWorkerAdapter = new WorkerAdapter(this, list_of_workers);
        ListView listView = (ListView) findViewById(R.id.list);
        //I have to fill the listView with the names of the workers, i should have a JSON object with a list
        //no need to implement a listener, as the list has to be created when the app is opened
        listView.setAdapter(mWorkerAdapter);
        //when a worker from the list is pressed open calendar
        //we should know what ID this person is, to gather the correct calendar dates from the database
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(MainActivity.this, CalendarActivity.class);
                View biew = view;
                int pocion =  position;
                //need to pass the user ID that is actually clicked
                TextView touchedID = (TextView)view.findViewById(R.id.number_id);

                TextView touchedName = (TextView)view.findViewById(R.id.name);
                String lusuarioID = (String) touchedID.getText();
                String lusuarioName = (String) touchedName.getText();
                i.putExtra("name", lusuarioName);//have to check if this is called with the proper valus
                i.putExtra("userID", lusuarioID);
                if (mUserID.equals(ADMIN_ID)) {
                    isAdmin = true;
                } else {
                    isAdmin = false;
                }
                i.putExtra("admin", isAdmin);

                startActivity(i);
            }
        });


        persona.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, CalendarActivity.class);
                i.putExtra("name", mUsername);//have to check if this is called with the proper valus
                i.putExtra("userID", mUserID);
                startActivity(i);
            }
        });
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //user is signed in
                    //onSignedInInitialize(user.getDisplayName());
                    onSignedInInitialize(user.getDisplayName(), user.getUid());
                    String userID = user.getUid();
                    Log.v("USERID", userID);
                } else {
                    //user is signed out
                    onSignedOutCleanUp();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setProviders(
                                            AuthUI.EMAIL_PROVIDER,
                                            AuthUI.GOOGLE_PROVIDER)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };


    }
    //so that when the log in is presented, the back button exits the app
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Already signed in", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Signed in cancelled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        detachDatabaseReadListener();
        mWorkerAdapter.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //private void onSignedInInitialize(String username) {
    private void onSignedInInitialize(String username, String userID) {
        mUsername = username;//mUsername member variable is pushed above to the database, not really anymore...
        mUserID = userID;
        //i should here add the username to the database
        //mDatabaseReference.push().setValue(username);
        Worker currela = new Worker(username, userID);
        //mDatabaseReference.push().setValue(currela);

        //mDatabaseReference.setValue(currela);
        mDatabaseReference.child(mUserID).setValue(currela);
        //Setting the username and pulling the workers list when they are actually logged in
        attachDatabaseReadListener();
        attachlonuevodelosworkers();
    }

    private void attachlonuevodelosworkers() {
        if (childeventparaworkers == null) {
            childeventparaworkers = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Object obtejo = dataSnapshot.getValue();
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
            paralapruebadelosworkers.addChildEventListener(childeventparaworkers);
        }
    }

    private void onSignedOutCleanUp() {
        mUsername = ANONYMOUS;
        mWorkerAdapter.clear();
        detachDatabaseReadListener();
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }

    }

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                //gets called whenever a new user is inserted in the list
                //also retreives all the users when the listener is first attached
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    //String workername = (String) dataSnapshot.getValue();
                    Worker workerintheDatabase = dataSnapshot.getValue(Worker.class);
                    //could add it directly to the adapter...
                    //list_of_workers.add(workerintheDatabase);
                    //Lyla adds directly to the ADAPTER
                    mWorkerAdapter.add(workerintheDatabase);
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
                //typically you don't have permission to read the data
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(),"ONCANCELLED in MainActivity",Toast.LENGTH_LONG).show();
                    Log.v("***********", "onCancelled called in list_of_workers");

                    //isAdmin = false;//not the best way to differenciate worker and adming
                    //will hardcode the user ID on the code just for now
                }
            };
            mDatabaseReference.addChildEventListener(mChildEventListener);

            Log.v("ATTACHDATABASEREAD","In theory");

        }
    }
}
