package com.example.madproject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;

import DataModels.Trip;
import DataModels.UserModel;

public class MyTripsActivity extends AppCompatActivity {
    ArrayList<Trip> tripsArrayList=new ArrayList<>();
    UserModel userModel,loggedInUserModel;
    FirebaseFirestore db;
    String userEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_trips);
        final ListView listView= findViewById(R.id.listview_mytrips);

        if (getIntent().getExtras()!=null){
            loggedInUserModel= (UserModel) getIntent().getExtras().getSerializable(NavigationActivity.currentUserModelKey); // user model of User who is logged in
            userModel= (UserModel) getIntent().getExtras().getSerializable(UserAdapter.tripCreatorUserModelKey); // user model of user who created trips
        }

        if(loggedInUserModel!=null)
            userEmail=loggedInUserModel.email;

        if(userModel!=null)
            userEmail=userModel.email;


        db=FirebaseFirestore.getInstance();

        // meaning user is viewing his own trips
        // get the trips that a user is a part of..
        if(userModel==null && loggedInUserModel!=null){
            db.collection("Trips")
                    .whereArrayContains("userList", userEmail)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w("tripIt", "Listen failed.", e);
                                return;
                            }

                            tripsArrayList= new ArrayList<Trip>();
                            for (QueryDocumentSnapshot doc : value) {
                                tripsArrayList.add(doc.toObject(Trip.class));
                            }
                            final TripAdapter tripAdapter= new TripAdapter(MyTripsActivity.this, R.layout.singletriplayout,tripsArrayList,loggedInUserModel);
                            listView.setAdapter(tripAdapter);
                            tripAdapter.notifyDataSetChanged();
                        }
                    });
        }
        else{
            //get the trips created by a particular user
            db.collection("Trips")
                    .whereEqualTo("creatorEmail",userModel.email)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w("tripIt", "Listen failed.", e);
                                return;
                            }
                            tripsArrayList= new ArrayList<Trip>();
                            for (QueryDocumentSnapshot doc : value) {
                                tripsArrayList.add(doc.toObject(Trip.class));
                            }
                            final TripAdapter tripAdapter= new TripAdapter(MyTripsActivity.this, R.layout.singletriplayout,tripsArrayList,loggedInUserModel);
                            listView.setAdapter(tripAdapter);
                            tripAdapter.notifyDataSetChanged();
                        }
                    });
        }
    }
}
