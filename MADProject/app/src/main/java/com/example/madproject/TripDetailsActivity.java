package com.example.madproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import DataModels.Trip;
import DataModels.UserModel;

public class TripDetailsActivity extends AppCompatActivity {
    UserModel loggedInUser,tripCreator;
    Trip trip;
    TextView tv_tripTitle,tv_creatorName,tv_creatorEmail,tv_latitude,tv_longitude, tv_triptitleval;
    ImageView iv_tripPhoto;
    Button button_joinTrip,button_removeTrip,button_enterChatRoom,button_editTrip;
    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);
        tv_creatorEmail=findViewById(R.id.textView_tripDetails_tripCreatorEmail);
        tv_creatorName=findViewById(R.id.textView_tripDetails_tripCreatorName);
        tv_latitude=findViewById(R.id.textView_tripDetails_latitude);
        tv_longitude=findViewById(R.id.textView_tripDetails_longitude);
        tv_tripTitle=findViewById(R.id.textView_tripDetails_Title);
        iv_tripPhoto=findViewById(R.id.imageView_tripDetails_tripPhoto);
        tv_triptitleval=findViewById(R.id.textView_tripDetails_tripTitle);
        button_enterChatRoom=findViewById(R.id.button_tripDetails_enterChatRoom);
        button_joinTrip=findViewById(R.id.button_tripDetails_joinTrip);
        button_removeTrip=findViewById(R.id.button_tripDetails_removeTrip);
        button_editTrip=findViewById(R.id.button_tripDetails_edittrip);
        button_enterChatRoom.setVisibility(View.INVISIBLE);
        //button_joinTrip.setVisibility(View.INVISIBLE);
        button_removeTrip.setVisibility(View.INVISIBLE);
        db=FirebaseFirestore.getInstance();

        if(getIntent()!=null && getIntent().getSerializableExtra(NavigationActivity.currentUserModelKey)!=null){
            loggedInUser= (UserModel) getIntent().getSerializableExtra(NavigationActivity.currentUserModelKey);
            trip=(Trip) getIntent().getSerializableExtra(NavigationActivity.tripKey);

            if (!loggedInUser.email.equals(trip.creatorEmail))
                button_editTrip.setVisibility(View.INVISIBLE);
        }

        tv_triptitleval.setText(trip.title);
        tv_tripTitle.setText(trip.title);
        tv_longitude.setText(String.valueOf(trip.longi));
        tv_latitude.setText(String.valueOf(trip.lat));
        tv_creatorName.setText(trip.creatorName);
        tv_creatorEmail.setText(trip.creatorEmail);
        Picasso.get().load(trip.tripPhotoUrl).into(iv_tripPhoto);

        if(trip.userList.contains(loggedInUser.email)){
            button_enterChatRoom.setVisibility(View.VISIBLE);
            button_removeTrip.setVisibility(View.VISIBLE);
            button_joinTrip.setVisibility(View.INVISIBLE);
        }
        button_editTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(TripDetailsActivity.this,CreateTripAcitvity.class);
                i.putExtra("editTripuser",loggedInUser);
                i.putExtra("editTripdetail",trip);
                startActivity(i);
                finish();
            }
        });

        button_enterChatRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(TripDetailsActivity.this,ChatActivity.class);
                i.putExtra("LoggedInUser",loggedInUser);
                i.putExtra("TripDetails",trip);
                startActivity(i);
            }
        });


        button_joinTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //trip.userList.add(loggedInUser.email);
                final Map<String, Object> addUserToArrayMap = new HashMap<>();
                // FieldValue.arrayUnion(mAuth.getCurrentUser().getUid())
                addUserToArrayMap.put("userList", FieldValue.arrayUnion(loggedInUser.email));

                db.collection("Trips").document(trip.title+"-"+trip.creatorEmail).update(addUserToArrayMap);
                button_enterChatRoom.setVisibility(View.VISIBLE);
                button_removeTrip.setVisibility(View.VISIBLE);
                button_joinTrip.setVisibility(View.INVISIBLE);
                Toast.makeText(TripDetailsActivity.this,"Trip joint successfully",Toast.LENGTH_SHORT).show();
            }
        });

        button_removeTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if(loggedInUser.email.equals(trip.creatorEmail)){
                //delete the entire trip and close activity;

               db.collection("Trips").document(trip.title+"-"+trip.creatorEmail).collection("ChatRoom")
                       .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                   @Override
                   public void onComplete(@NonNull Task<QuerySnapshot> task) {
                       if(task.isSuccessful()){
                           for(QueryDocumentSnapshot messageDoc :task.getResult()){
                               db.collection("Trips").document(trip.title+"-"+trip.creatorEmail).collection("ChatRoom").document(messageDoc.getId()).delete();
                           }

                           db.collection("Trips").document(trip.title+"-"+trip.creatorEmail)
                                   .delete()
                                   .addOnSuccessListener(new OnSuccessListener<Void>() {
                                       @Override
                                       public void onSuccess(Void aVoid) {
                                           //Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                           Toast.makeText(TripDetailsActivity.this,"Trip removed successfully",Toast.LENGTH_SHORT).show();
                                           finish();
                                       }
                                   })
                                   .addOnFailureListener(new OnFailureListener() {
                                       @Override
                                       public void onFailure(@NonNull Exception e) {
                                           //Log.w(TAG, "Error deleting document", e);
                                           Toast.makeText(TripDetailsActivity.this,"Oops something went wrong",Toast.LENGTH_SHORT).show();
                                       }
                                   });
                       }
                   }
               });
                // will need to delete the chat collections as well

            }
            else
            {
                // only remove the user from UserList
                final Map<String, Object> addUserToArrayMap = new HashMap<>();
                // FieldValue.arrayUnion(mAuth.getCurrentUser().getUid())
                addUserToArrayMap.put("userList", FieldValue.arrayRemove(loggedInUser.email));
                db.collection("Trips").document(trip.title+"-"+trip.creatorEmail).update(addUserToArrayMap);
                button_enterChatRoom.setVisibility(View.INVISIBLE);
                button_removeTrip.setVisibility(View.INVISIBLE);
                button_joinTrip.setVisibility(View.VISIBLE);
                Toast.makeText(TripDetailsActivity.this,"Trip removed successfully",Toast.LENGTH_SHORT).show();

            }
            }
        });



    }


}
