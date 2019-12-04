package com.example.madproject;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import DataModels.Trip;

import DataModels.UserModel;
import Services.FireStoreDataOp;

public class CreateTripAcitvity extends AppCompatActivity {
    ImageView iv_cover;
    EditText et_title, et_lat, et_longi;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    Button button_create_trip;
    Bitmap bitmapUpload = null;
     Trip trip=new Trip();
    UserModel creator;
    TextInputLayout nameLayout,nameLayout1,nameLayout2;

    String imageURL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_trip_acitvity);

        iv_cover=findViewById(R.id.iv_create_trip_cover);
        et_lat=findViewById(R.id.et_create_trip_lat);
        et_longi=findViewById(R.id.et_create_trip_longi);
        et_title=findViewById(R.id.et_create_trip_title);
        button_create_trip=findViewById(R.id.button_create_trip);
        nameLayout=findViewById(R.id.inputlayout_profile_nameLayout);
        nameLayout1=findViewById(R.id.inputlayout_profile_nameLayout1);
        nameLayout2=findViewById(R.id.inputlayout_profile_nameLayout2);


        if(getIntent()!=null && getIntent().getExtras()!=null){
            creator=(UserModel) getIntent().getSerializableExtra(NavigationActivity.tripCreatorModelKey);
            creator=(UserModel) getIntent().getSerializableExtra("editTripuser");
            trip= (Trip)getIntent().getSerializableExtra("editTripdetail");
            if (trip!=null){

                et_lat.setText(Double.toString(trip.lat));
                et_longi.setText(Double.toString(trip.longi));
                et_title.setText(trip.title);
                et_title.setEnabled(false);
                Picasso.get().load(trip.tripPhotoUrl).into(iv_cover);
            }
        }
        iv_cover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });


        button_create_trip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Trip trip1=validateTrip();
               if (trip1!=null){
                   if(bitmapUpload==null)
                       Toast.makeText(CreateTripAcitvity.this, "Please upload an image", Toast.LENGTH_SHORT).show();
                else {
                    trip=trip1;
                    uploadImage(getBitmapCamera(),trip1.title);
                   }}
            }
        });

    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        Camera Callback........
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            iv_cover.setImageBitmap(imageBitmap);
            bitmapUpload = imageBitmap;
        }
    }
    private Bitmap getBitmapCamera() {
        //If photo not taken from camera...
        if (bitmapUpload == null){
            return ((BitmapDrawable) iv_cover.getDrawable()).getBitmap();
        }
//        Photo taken from camera...
        return bitmapUpload;
    }
    private void uploadImage(Bitmap photoBitmap,String title){
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference();

        final StorageReference imageRepo = storageReference.child("tripimages/"+title+".png");

//        Converting the Bitmap into a bytearrayOutputstream....
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        photoBitmap.compress(Bitmap.CompressFormat.PNG, 50, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = imageRepo.putBytes(data);


        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
//                return null;
                //      progressDialog.show();
                if (!task.isSuccessful()){
                    throw task.getException();
                }

                return imageRepo.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
//                progressDialog.dismiss();
                if (task.isSuccessful()){
                    Log.d("Tag", "Image Download URL"+ task.getResult());
                    imageURL = task.getResult().toString();
                    trip.tripPhotoUrl=imageURL;
                    FireStoreDataOp.UploadDataTrip(trip);
                    finish();
/*

                    Intent i= new Intent(ProfileActivity.this,NavigationActivity.class);
                    i.putExtra("trip",trip);
                    startActivity(i);
*/

                    //  Picasso.get().load(imageURL).into(iv_uploadedPhoto);
                    Toast.makeText(CreateTripAcitvity.this, "uploaded image", Toast.LENGTH_SHORT).show();
                }
            }
        });

//        ProgressBar......

        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                /*progressDialog.setProgress((int) progress);*/
                System.out.println("Loading" + progress + "% done");
            }
        });

    }
        Trip validateTrip(){
            Trip trip=new Trip();
            nameLayout.setError(null);
            nameLayout1.setError(null);
            nameLayout2.setError(null);
            if(et_title.getText()==null||et_title.getText().toString().equals("")){
                nameLayout.setError("Trip title is required");
                return null;
            }
            else
                trip.title=et_title.getText().toString();
            if(et_lat.getText()==null||et_lat.getText().toString().equals("")){
                nameLayout1.setError("Trip latitude is required");
                return null;
            }
            else
                trip.lat=Double.parseDouble(et_lat.getText().toString());
            if(et_longi.getText()==null||et_longi.getText().toString().equals("")){
                nameLayout2.setError("Trip longitude is required");
                return null;
            }
            else
                trip.longi=Double.parseDouble(et_longi.getText().toString());

            trip.creatorEmail=creator.email;
            trip.creatorName=creator.name;
            trip.userList=new ArrayList<>();
            trip.userList.add(creator.email);
            return  trip;
        }
}
