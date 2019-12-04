package com.example.madproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
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
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import DataModels.UserModel;
import Services.FireStoreDataOp;

public class ProfileActivity extends AppCompatActivity {
    EditText et_profile_name;
    TextView tv_email,tv_title;
    Button button_profile_save,button_profile_cancel,button_profile_logout;
    RadioGroup rg_gender;
    RadioButton rb_male,rb_female;
    private static FirebaseFirestore db;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView iv_userphoto;
    ProgressDialog progressDialog;
    TextInputLayout nameLayout;
    String imageURL;
    Bitmap bitmapUpload = null;
    boolean flag=false;
    boolean flag1=false;
    UserModel user1=new UserModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        rg_gender=findViewById(R.id.radiogroup_profile_gender);
        rb_male=findViewById(R.id.radioButton_profile_male);
        rb_female=findViewById(R.id.radioButton_profile_female);
        et_profile_name=findViewById(R.id.et_profile_name);
        button_profile_save= findViewById(R.id.button_profile_save);
        button_profile_cancel=findViewById(R.id.button_profile_cancel);
        button_profile_logout=findViewById(R.id.button_profile_logout);
        iv_userphoto=findViewById(R.id.iv_userphoto);
        tv_email=findViewById(R.id.textView_profile_email);
        nameLayout=findViewById(R.id.inputlayout_profile_nameLayout);
        tv_title=findViewById(R.id.textView_profile_title);
        if (getIntent()!=null && getIntent().getExtras()!=null) {
            String email = getIntent().getExtras().getString(MainActivity.key);
            if(email!=null){
                populateUserData(email);
                tv_title.setText("Complete your profile");
                flag1=true;
            }

            String emailOfUserToEdit=getIntent().getExtras().getString(NavigationActivity.editKey);
            if(emailOfUserToEdit!=null){
                populateUserData(emailOfUserToEdit);
                tv_title.setText("Edit our profile");
                flag=true;
            }

        }

        iv_userphoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });


        button_profile_save=findViewById(R.id.button_profile_save);

        button_profile_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserModel user=validateUser();
                if(user!=null){

                    uploadImageAndSaveData(getBitmapCamera(),user);
                    //FireStoreDataOp.UploadData(user);
                }
            }
        });
        button_profile_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flag) {
                    Intent i= new Intent(ProfileActivity.this,NavigationActivity.class);
                    i.putExtra("user",user1);
                    startActivity(i);
                    finish();
                }
                if (flag1){
                    finish();
                }
            }
        });
        button_profile_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();

                Intent i= new Intent(ProfileActivity.this,MainActivity.class);
                startActivity(i);
                finish();
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
            iv_userphoto.setImageBitmap(imageBitmap);
            bitmapUpload = imageBitmap;
        }
    }
    private Bitmap getBitmapCamera() {
        //If photo not taken from camera...
        if (bitmapUpload == null){
            return ((BitmapDrawable) iv_userphoto.getDrawable()).getBitmap();
        }
//        Photo taken from camera...
        return bitmapUpload;
    }
    private void uploadImageAndSaveData(Bitmap photoBitmap, final UserModel userModel){
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference();
        final StorageReference imageRepo = storageReference.child("images/"+userModel.email+".png");

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
                    userModel.photoUrl=imageURL;
                    FireStoreDataOp.UploadData(userModel);
                    Intent i= new Intent(ProfileActivity.this,NavigationActivity.class);
                    i.putExtra("user",userModel);
                    startActivity(i);
                    //  Picasso.get().load(imageURL).into(iv_uploadedPhoto);
                    Toast.makeText(ProfileActivity.this, "Saved Successfully", Toast.LENGTH_SHORT).show();
                    finish();
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

    /*public void checkAfterLogin(final String email){
        db = FirebaseFirestore.getInstance();
        db.collection("User").whereEqualTo("email",email).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    // size>0 indicated user is already present in the database, therefore navigate him to
                    // dashboard
                    if(task.getResult().size()>0){
                        for (QueryDocumentSnapshot document: task.getResult()){
                            if(document.getId().equals(email)){
                                UserModel user=new UserModel();
                                user.name=document.getString("name");
                                user.email=document.getString("email");
                                user.photoUrl= (document.getString("photoUrl")==null? "":document.getString("photoUrl"));
                                user.gender= (document.getString("gender")==null? "":document.getString("gender"));
                                Log.d("demo",user.email);
                                Intent i= new Intent(ProfileActivity.this,NavigationActivity.class);
                                i.putExtra("user",user);
                                startActivity(i);
                                finish();
                            }
                        }
                    }
                    else{
                        tv_title.setText("Complete your profile");
                        tv_email.setText(email);
                        rb_male.setChecked(true);
                    }
                }
            }
        });
    }*/

    public void populateUserData(final String email){
        db = FirebaseFirestore.getInstance();
        db.collection("User").whereEqualTo("email",email).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    //populate data to edit the user;
                    if(task.getResult().size()>0){
                        for (QueryDocumentSnapshot document: task.getResult()){
                            if(document.getId().equals(email)){
                                tv_title.setText("Edit My Profile");
                                UserModel user=new UserModel();
                                switch (document.getString("gender")){
                                    case "male": rb_male.setChecked(true);
                                                user1.gender="male";
                                                break;
                                    case "female": rb_female.setChecked(true);
                                        user1.gender="female";
                                }
                                tv_email.setText(document.getString("email"));
                                et_profile_name.setText(user.name=document.getString("name"));
                                user1.name=document.getString("name");
                                user1.email=document.getString("email");

                                imageURL=document.getString("photoUrl");
                                Picasso.get().load(imageURL).into(iv_userphoto);
                            }
                        }
                    }
                    else
                    {
                        rb_male.setChecked(true);
                        tv_email.setText(email);
                    }
                }
            }
        });
    }

    public UserModel validateUser(){
        nameLayout.setError(null);
        UserModel userModel=new UserModel();
        if(et_profile_name.getText()==null||et_profile_name.getText().toString().equals("")){
            nameLayout.setError("Name is required");
            return null;
        }
        userModel.name=et_profile_name.getText().toString();
        if(rg_gender.getCheckedRadioButtonId()==R.id.radioButton_profile_male)
            userModel.gender="male";
        if(rg_gender.getCheckedRadioButtonId()==R.id.radioButton_profile_female)
            userModel.gender="female";
        userModel.email=tv_email.getText().toString();
        return userModel;
    }


}
