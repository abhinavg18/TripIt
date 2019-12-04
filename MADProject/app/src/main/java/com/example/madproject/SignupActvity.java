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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignupActvity extends AppCompatActivity {


    FirebaseAuth mAuth;
    EditText email,password,confirmPassword;
    Button button_signUp,button_cancel;
    TextInputLayout emailLayout,passwordLayout,confirmPasswordLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_actvity);
        email=findViewById(R.id.editText_signup_email);
        password=findViewById(R.id.editText_signup_password);
        confirmPassword=findViewById(R.id.editText_signup_confirmpassword);
        emailLayout=findViewById(R.id.inputlayout_signup_email);
        passwordLayout=findViewById(R.id.inputlayout_singup_password);
        confirmPasswordLayout=findViewById(R.id.inputlayout_signup_confirmPasswordLayout);
        mAuth=FirebaseAuth.getInstance();
        button_signUp=findViewById(R.id.button_signup_singup);
        button_cancel=findViewById(R.id.button_signup_cancel);
        button_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateInputs()){
                    String emailString=email.getText().toString();
                    String passwordString=password.getText().toString();
                    mAuth.createUserWithEmailAndPassword(emailString, passwordString)
                            .addOnCompleteListener(SignupActvity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SignupActvity.this, "Sign Up Successful", Toast.LENGTH_SHORT).show();
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        Intent intent = new Intent(SignupActvity.this, ProfileActivity.class);
                                        intent.putExtra(MainActivity.key,user.getEmail());
                                        startActivity(intent);
                                        finish();

                                    } else {
                                        Toast.makeText(SignupActvity.this, "Sign Up failed.",Toast.LENGTH_SHORT).show();
                                        Log.d("demo", "createUserWithEmail:failure", task.getException());
                                    }
                                }
                            });
                }
            }
        });

        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i= new Intent(SignupActvity.this,MainActivity.class);
                startActivity(i);
                finish();
            }
        });

    }

    public boolean validateInputs(){
        emailLayout.setError(null);
        confirmPasswordLayout.setError(null);
        passwordLayout.setError(null);

        if(email.getText()!=null && password.getText()!=null
                && confirmPassword.getText()!=null
                && !email.getText().toString().equals("") && !password.getText().toString().equals("")
                && !confirmPassword.getText().toString().equals("") && password.getText().toString().equals(confirmPassword.getText().toString()))
            if(password.getText().toString().length()>=6)
                return true;
            else return false;
        else{
            if(email.getText()==null || email.getText().toString().equals(""))
                emailLayout.setError("Email is required");

            if(password.getText()==null || password.getText().toString().equals(""))
                passwordLayout.setError("Password is required");
            if(confirmPassword.getText()==null || confirmPassword.getText().toString().equals(""))
                confirmPasswordLayout.setError("Confirm Password is required");



            if(!password.getText().toString().equals("") && !confirmPassword.getText().toString().equals(""))
            {
                if(password.getText().toString().length()<6)
                    passwordLayout.setError("Password must at least be 6 characters");

                if(!password.getText().toString().equals(confirmPassword.getText().toString()))
                {
                    confirmPasswordLayout.setError("Confirm password does not match");
                }
            }
          return false;
        }
    }

}
