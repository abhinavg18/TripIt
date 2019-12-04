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

import DataModels.Trip;
import DataModels.UserModel;

public class TripAdapter extends ArrayAdapter<Trip> {
    UserModel loggedInUserModel;
    public TripAdapter(@NonNull Context context, int resource, @NonNull List<Trip> objects,UserModel loggedInUserModel) {
        super(context, resource, objects);
        if(objects.size()==0)
            Toast.makeText(getContext(),"No trips to display",Toast.LENGTH_SHORT).show();
        this.loggedInUserModel=loggedInUserModel;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final Trip trip= getItem(position);
        ViewHolder viewHolder;
        try{
            if(convertView==null){
                convertView= LayoutInflater.from(getContext()).inflate(R.layout.singletriplayout,parent,false);
                viewHolder = new ViewHolder();
                viewHolder.tv_tripTitle= convertView.findViewById(R.id.textView_singleTripLayout_tripTitle);
                viewHolder.creatorEmail= convertView.findViewById(R.id.textView_singleTripLayout_creatorEmail);
                viewHolder.creatorName= convertView.findViewById(R.id.textView_singleTripLayout_creatorName);
                viewHolder.button_viewDetails= convertView.findViewById(R.id.button_singleTripLayout_viewTripDetails);
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.tv_tripTitle.setText(trip.title);
            viewHolder.creatorEmail.setText(trip.creatorEmail);
            viewHolder.creatorName.setText(trip.creatorName);
            viewHolder.button_viewDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // navigate to details activity
                    //pass trip object
                    Intent i = new Intent(getContext(),TripDetailsActivity.class);
                    i.putExtra(NavigationActivity.currentUserModelKey,loggedInUserModel);
                    i.putExtra(NavigationActivity.tripKey,trip);
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
        TextView tv_tripTitle,creatorName,creatorEmail;
        Button button_viewDetails;
    }
}
