package com.example.madproject;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import DataModels.ChatModel;
import DataModels.Trip;
import DataModels.UserModel;


public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    public ArrayList<ChatModel>messages;
    public UserModel loggedInUser;
    public Trip tripDetails;
    private static FirebaseFirestore db;
    public ChatAdapter(ArrayList<ChatModel>messages, UserModel loggedInUser, Trip trip){
        this.messages=messages;
        this.loggedInUser=loggedInUser;
        this.tripDetails=trip;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chatwindow,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ChatModel message = messages.get(position);
        if (loggedInUser.email.equals(message.creatorEmail)) {
            holder.tv_sender.setGravity(Gravity.RIGHT);
            holder.tv_msg.setGravity(Gravity.RIGHT);
            holder.tv_sender.setText(message.creatorName);
            holder.tv_msg.setText(message.message);
            holder.tv_time.setGravity(Gravity.RIGHT);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String dateString = format.format(message.getTimestamp());
            holder.tv_time.setText(dateString);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(300, 300);
            layoutParams.gravity = Gravity.RIGHT;
            holder.iv_chatmsg.setLayoutParams(layoutParams);


            if(message.message==null || message.message.equals(""))
                holder.tv_msg.setVisibility(View.GONE);


            if (message.msgImgUrl == null || message.msgImgUrl.equals("")) {
                holder.iv_chatmsg.setVisibility(View.GONE);
            } else
                Picasso.get().load(message.msgImgUrl).into(holder.iv_chatmsg);

        }
        else {

            holder.tv_sender.setGravity(Gravity.LEFT);
            holder.tv_msg.setGravity(Gravity.LEFT);
            holder.tv_sender.setText(message.creatorName);
            holder.tv_msg.setText(message.message);
            holder.tv_time.setGravity(Gravity.LEFT);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String dateString = format.format(message.getTimestamp());
            holder.tv_time.setText(dateString);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(75, 75);
            layoutParams.gravity = Gravity.LEFT;
            holder.iv_chatmsg.setLayoutParams(layoutParams);

            if(message.message==null || message.message.equals(""))
                holder.tv_msg.setVisibility(View.GONE);

            if (message.msgImgUrl == null || message.msgImgUrl.equals("") ) {
                holder.iv_chatmsg.setVisibility(View.GONE);
            } else
                Picasso.get().load(message.msgImgUrl).into(holder.iv_chatmsg);



        }
    }




    @Override
    public int getItemCount() {
        if (messages==null)
            return 0;
        else
            return messages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_sender, tv_msg,tv_time;
        public ImageView iv_chatmsg;
        //ChatModel msgitem;
        //String id;

        public ViewHolder(@NonNull final View itemView ) {
            super(itemView);
            tv_msg=itemView.findViewById(R.id.tv_message);
            tv_sender=itemView.findViewById(R.id.tv_sender);
            iv_chatmsg=itemView.findViewById(R.id.iv_chatmsg);
            tv_time=itemView.findViewById(R.id.textView_chatWindow_time);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    int position=getAdapterPosition();

                    ChatModel message=messages.get(position);


                    if (message.creatorEmail.equals(loggedInUser.email)) {
                        //ChatActivity.messagesChat.remove(msgitem);
                        //Log.d("clicked",msgitem.creatorName);
                        //String s=ChatActivity.pathUID.get(pos);
                        //ChatActivity.pathUID.remove(getAdapterPosition());
                        if(message.msgImgUrl!=null && !message.msgImgUrl.equals("")){
                            //String path= ChatActivity.hashMap.get(msgitem.msgImgUrl);
                            String imageName=message.msgImgUrl.substring(message.msgImgUrl.indexOf('%')+1,message.msgImgUrl.indexOf('?')+1);
                            FirebaseStorage storage = FirebaseStorage.getInstance();
                            StorageReference storageReference= storage.getReference();
                            StorageReference imagesReference=storageReference.child("chatimages/"+imageName);
                            imagesReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("clicked","image delete");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });
                        }
                        db = FirebaseFirestore.getInstance();
                        db.collection("Trips").document(tripDetails.title+"-"+tripDetails.creatorEmail).collection("ChatRoom").document(message.id)
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("clicked", "DocumentSnapshot successfully deleted!");

                                        ChatActivity.mAdapter.notifyDataSetChanged();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("clicked", "Error deleting document", e);
                                    }
                                });
                    }
                    return false;
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }
    }
}
