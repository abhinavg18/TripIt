package com.example.madproject;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import DataModels.UserModel;

public class UserAdapter extends ArrayAdapter<UserModel> {
    public static String tripCreatorUserModelKey="userWhoCreatedTheTrips";
    UserModel loggedInUser;
    public UserAdapter(@NonNull Context context, int resource, @NonNull List<UserModel> objects,UserModel loggedInUser) {
        super(context, resource, objects);
        this.loggedInUser=loggedInUser;
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final UserModel userModel = getItem(position);
        ViewHolder viewHolder;
        try{
            if(convertView==null){
                convertView= LayoutInflater.from(getContext()).inflate(R.layout.userdiscovered,parent,false);
                viewHolder = new ViewHolder();
                viewHolder.tv_name= convertView.findViewById(R.id.tv_user_discovered_name);
                viewHolder.button_viewTripsOfThisUser=convertView.findViewById(R.id.button_user_view_trips);
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.tv_name.setText(userModel.name);
            viewHolder.button_viewTripsOfThisUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i= new Intent(getContext(),MyTripsActivity.class);
                    i.putExtra(NavigationActivity.currentUserModelKey,loggedInUser);
                    i.putExtra(UserAdapter.tripCreatorUserModelKey,userModel);
                    getContext().startActivity(i);
                }
            });
        }
        catch(Exception ex){
            Toast.makeText(getContext(),"Something went wrong!",Toast.LENGTH_SHORT).show();
        }
        return convertView;
    }

    private static class ViewHolder{
        TextView tv_name;
        Button button_viewTripsOfThisUser;
    }
}
