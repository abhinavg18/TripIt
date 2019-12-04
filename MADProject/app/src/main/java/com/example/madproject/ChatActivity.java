package com.example.madproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import DataModels.ChatModel;
import DataModels.Trip;
import DataModels.UserModel;
import Services.FireStoreDataOp;

public class ChatActivity extends AppCompatActivity {

    private static FirebaseFirestore db;
    String imageURL;
    Bitmap bitmapUpload = null;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    EditText et;
    Button btn;
    ImageView iv_chat_msg;
    public UserModel loggedInUser;
    public RecyclerView mRecyclerView;
    public static RecyclerView.Adapter mAdapter;
    ArrayList<ChatModel> messages;
    Trip tripDetails;
    RecyclerView.LayoutManager mLayoutManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        db = FirebaseFirestore.getInstance();
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(ChatActivity.this);
        mRecyclerView.setLayoutManager(mLayoutManager);


        messages=new ArrayList<ChatModel>();

        if (getIntent() != null && getIntent().getExtras() != null) {
            loggedInUser = (UserModel) getIntent().getExtras().getSerializable("LoggedInUser");
            tripDetails=(Trip)getIntent().getExtras().getSerializable("TripDetails");
        }
        iv_chat_msg = findViewById(R.id.iv_chat_msg);
        et = findViewById(R.id.et_chatmsg);
        btn = findViewById(R.id.btn_msg);
        iv_chat_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = et.getText().toString();
                ChatModel chatModel = new ChatModel();
                chatModel.timestamp= new Date();
                chatModel.creatorEmail = loggedInUser.email;
                chatModel.message = msg;
                chatModel.creatorName = loggedInUser.name;
                chatModel.msgImgUrl="";
                if(!msg.equals("") && bitmapUpload!=null){
                        uploadImageAndSaveData(bitmapUpload,chatModel);
                }
                else{
                    if(bitmapUpload!=null){
                        uploadImageAndSaveData(bitmapUpload,chatModel);
                    }else if(!msg.equals("")){
                        sendMessage(chatModel);
                    }
                    else{
                        Toast.makeText(ChatActivity.this,"Message/Image is required to send message",Toast.LENGTH_SHORT).show();
                    }
                }
                et.setText("");
                bitmapUpload=null;
                iv_chat_msg.setImageDrawable(getDrawable(R.drawable.common_google_signin_btn_icon_dark_normal_background));
            }
        });

        db.collection("Trips").document(tripDetails.title+"-"+tripDetails.creatorEmail).collection("ChatRoom").orderBy("timestamp")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("demo", "listen:error", e);
                            return;
                        }
                        else{
                            messages= new ArrayList<>();
                            for ( QueryDocumentSnapshot doc:snapshots) {
                                //tripsArrayList.add(doc.toObject(Trip.class));
                                messages.add(doc.toObject(ChatModel.class));
                            }
                            mAdapter = new ChatAdapter(messages,loggedInUser,tripDetails);
                            mRecyclerView.setAdapter(mAdapter);
                            if(messages.size()>1)
                                mRecyclerView.smoothScrollToPosition(messages.size()-1);
                        }
                    }
                });


    }


    public void sendMessage(final ChatModel message){

        db.collection("Trips").document(tripDetails.title+"-"+tripDetails.creatorEmail).collection("ChatRoom").add(message)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("demo", documentReference.getId());
                        message.id=documentReference.getId();
                        Toast.makeText(ChatActivity.this,"Message sent",Toast.LENGTH_SHORT).show();
                        db.collection("Trips").document(tripDetails.title+"-"+tripDetails.creatorEmail).collection("ChatRoom").document(message.id)
                                .update("id",message.id).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });





                        mAdapter.notifyDataSetChanged();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("demo", "Error adding document", e);
                Toast.makeText(ChatActivity.this,"Message not sent",Toast.LENGTH_SHORT).show();
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
            iv_chat_msg.setImageBitmap(imageBitmap);
            bitmapUpload = imageBitmap;
        }
    }

    private Bitmap getBitmapCamera() {
        //If photo not taken from camera...
        if (bitmapUpload == null) {
            return ((BitmapDrawable) iv_chat_msg.getDrawable()).getBitmap();
        }
//        Photo taken from camera...
        return bitmapUpload;
    }

    private void uploadImageAndSaveData(Bitmap photoBitmap, final ChatModel message) {
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference();
        final String path= "chatimages/" + UUID.randomUUID() + ".png";
        final StorageReference imageRepo = storageReference.child(path);

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
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return imageRepo.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
//                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    Log.d("Tag", "Image Download URL" + task.getResult());
                    imageURL = task.getResult().toString();
                    message.msgImgUrl = imageURL;
                    imageURL="";
                    sendMessage(message);
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
}