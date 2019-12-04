    package com.example.madproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import DataModels.UserModel;
import Services.FireStoreDataOp;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    SignInButton button_signWithGoogle;
    GoogleSignInAccount googleUser;
    static String key="userEmail";
    private static final int GOOGLE_SIGNIN = 9001;
    Button button_signInWithEmailPassword,button_signUp;
    EditText et_email,et_password;
    private static FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button_signWithGoogle= findViewById(R.id.button_myGoogle);
        button_signInWithEmailPassword=findViewById(R.id.button_main_signInWithEmailPassword);
        button_signUp=findViewById(R.id.button_main_signUp);
        et_email=findViewById(R.id.et_main_email);
        et_password=findViewById(R.id.et_main_password);

        //check  if user already signed in, if either google user other type of user sgned in, navigate to
        // profile activity;
        navigateIfUserAlreadySignedIn();

        // else let him choose sign in method;



        button_signInWithEmailPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validate()){
                    String email = et_email.getText().toString();
                    String password = et_password.getText().toString();
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        String email=user.getEmail();
                                        getUserDataByEmail(email);
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.d("demo", "signInWithEmail:failure", task.getException());
                                        Toast.makeText(MainActivity.this, "Wrong email/password", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        button_signWithGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGoogle();
            }
        });

        button_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(MainActivity.this,SignupActvity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    public boolean validate(){
        TextInputLayout emailLayout,passwordLayout;
        emailLayout=findViewById(R.id.inputLayout_main_email);
        passwordLayout=findViewById(R.id.inputLayout_main_password);
        emailLayout.setError(null);
        passwordLayout.setError(null);

        if((et_email.getText()!= null && et_password.getText()!= null)
            && (!et_email.getText().toString().equals("") && !et_password.getText().toString().equals("")))
            return true;
        else {
            if (et_email.getText() == null || et_email.getText().toString().equals(""))
                emailLayout.setError("Email is required");
            if (et_password.getText() == null || et_password.getText().toString().equals(""))
                passwordLayout.setError("Password is required");
            return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_SIGNIN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                getUserDataByEmail(account.getEmail());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.d("demo", "Google sign in failed", e);
                // ...
            }
        }
    }

    public void navigateIfUserAlreadySignedIn(){
        mAuth= FirebaseAuth.getInstance();
        FirebaseUser currentUser=mAuth.getCurrentUser();
        //googleUser = GoogleSignIn.getLastSignedInAccount(this);
        String emailString="";

       /* if(currentUser!=null || googleUser!=null){
            if(currentUser!=null)
                emailString=currentUser.getEmail();
            if(googleUser!=null)
                emailString=googleUser.getEmail();
            getUserDataByEmail(emailString);
        }
*/
       if(currentUser!=null){
           emailString=currentUser.getEmail();
           getUserDataByEmail(emailString);
       }
    }

    public void signInWithGoogle(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient= GoogleSignIn.getClient(this, gso );
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_SIGNIN);
    }

    public void getUserDataByEmail(final String email){
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
                                Intent i= new Intent(MainActivity.this,NavigationActivity.class);
                                i.putExtra("user",user);
                                startActivity(i);
                                finish();
                            }
                        }
                    }
                    else{
                        Intent i= new Intent(MainActivity.this,ProfileActivity.class);
                        i.putExtra(key,email);
                        startActivity(i);
                        finish();
                    }
                }
            }
        });
    }




}
