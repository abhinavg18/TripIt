package Services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import DataModels.Trip;
import DataModels.UserModel;

public class FireStoreDataOp {
    private static FirebaseFirestore db;

    public static void UploadData(UserModel user){
        db = FirebaseFirestore.getInstance();

        /*uo.photoUrl=user.getPhotoUrl();*/
        db.collection("User").document(user.email).set(user);

    }

    public static void UploadDataTrip(Trip trip){
        db = FirebaseFirestore.getInstance();

        /*uo.photoUrl=user.getPhotoUrl();*/
        db.collection("Trips").document(trip.title+"-"+trip.creatorEmail).set(trip);
        // creating a chat sub collection
        db.collection("Trips").document(trip.title+"-"+trip.creatorEmail).collection("ChatRoom");
    }
    public static UserModel getUserDatabyEmail(final String email){
       final UserModel user=new UserModel();
        db = FirebaseFirestore.getInstance();
        db.collection("User").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot document: task.getResult()){
                        if(document.getId().equals(email)){
                            user.authToken=document.getString("authToken");
                            user.name=document.getString("name");
                            user.email=document.getString("email");
                            user.photoUrl= (document.getString("photoUrl")==null? "":document.getString("photoUrl"));
                            Log.d("demo",user.email);

                        }
                    }
                }
            }
        });
        return user;

    }

}
