package com.example.madproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import DataModels.Trip;
import DataModels.UserModel;

public class NavigationActivity extends AppCompatActivity {

    TextView tv_name, tv_gender, tv_email;
    Button button_discover, button_create, button_trips,button_logout,button_editMyProfile;
    private static FirebaseFirestore db;
    ArrayList<UserModel> arrayList;
    ArrayList<Trip> tripArrayList;
    static String editKey="editUserEmail";
    static String currentUserEmailKey="currentUserEmail";
    static String currentUserModelKey="currentUserModel";
    static String tripCreatorModelKey="tripCreator";
    static String tripKey="tripData";
    UserModel userModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        tv_name= findViewById(R.id.tv_navigation_name_val);
        tv_email= findViewById(R.id.tv_navigation_email_val);
        tv_gender= findViewById(R.id.tv_navigation_gender_val);
        button_editMyProfile=findViewById(R.id.button_navigation_editMyProfile);
        button_logout=findViewById(R.id.button_navigation_logout);
         if (getIntent()!=null && getIntent().getExtras()!=null){
             userModel = (UserModel)getIntent().getExtras().getSerializable("user");
             tv_email.setText(userModel.email);
             tv_gender.setText(userModel.gender);
             tv_name.setText(userModel.name);
         }

         button_create=findViewById(R.id.button_navigate_create);
         button_discover=findViewById(R.id.button_navigate_users);
         button_trips=findViewById(R.id.button_navigate_trips);

         button_discover.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 //call function to get all users
                 //call intent to display users in a list view
                 Intent i= new Intent(NavigationActivity.this, DiscoverUserActivity.class);
                 i.putExtra(currentUserModelKey,userModel);
                 startActivity(i);
             }
         });
         button_trips.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 //call function to get trips of THIS user
                 //call intent to display all the trips of THIS user            //THIS-reference should be emailID as that is our document
                 //have chat option in each of the trips
                 Intent i = new Intent(NavigationActivity.this,MyTripsActivity.class);
                 i.putExtra(NavigationActivity.currentUserModelKey,userModel);
                 startActivity(i);

             }
         });
         button_create.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 //create UI to get value for a trip
                 // save them into the trips collection with creator as THIS user
                 //add the user to that trip
                 Intent i = new Intent(NavigationActivity.this, CreateTripAcitvity.class);
                 i.putExtra(tripCreatorModelKey,userModel);
                 startActivity(i);
             }
         });

         button_editMyProfile.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent intent=new Intent(NavigationActivity.this,ProfileActivity.class);
                 intent.putExtra(editKey,userModel.email);
                 startActivity(intent);
                 finish();
             }
         });

         button_logout.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {

                 FirebaseAuth.getInstance().signOut();

                 Intent i= new Intent(NavigationActivity.this,MainActivity.class);
                 startActivity(i);
                 finish();
             }
         });
    }
}
