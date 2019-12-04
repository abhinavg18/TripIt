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

import java.util.ArrayList;
import java.util.Iterator;

import DataModels.UserModel;

public class DiscoverUserActivity extends AppCompatActivity {
    ArrayList<UserModel> arrayList;
    FirebaseFirestore db;

    UserModel loggedInUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover_user);

        if (getIntent().getExtras()!=null){
            loggedInUser= (UserModel) getIntent().getSerializableExtra(NavigationActivity.currentUserModelKey);
        }
        final ListView listView= findViewById(R.id.listview_discover_users);


        db = FirebaseFirestore.getInstance();

        db.collection("User").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
            if(e!=null){
                   Log.w("tripIt","ListenFailed",e);
                   return;
            }
                arrayList=new ArrayList<>();
            for(QueryDocumentSnapshot doc: queryDocumentSnapshots){
                arrayList.add(doc.toObject(UserModel.class));
            }

                Iterator<UserModel> iter = arrayList.iterator();
                while (iter.hasNext()) {
                    UserModel user = iter.next();

                    if (user.email.equals(loggedInUser.email))
                        iter.remove();
                }

                final UserAdapter userAdapter= new UserAdapter(DiscoverUserActivity.this, R.layout.userdiscovered,arrayList,loggedInUser);
                listView.setAdapter(userAdapter);
                userAdapter.notifyDataSetChanged();

            }
        });
    }
}
