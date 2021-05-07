package com.kevalshetta.ChatApp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kevalshetta.buddiesgram.R;

public class LoginFragment extends Fragment {

    private Button LoginButton;
    private EditText UserEmail, UserPassword;
    private TextView  ForgotPassword;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private Boolean emailAddressChecker;

    private DatabaseReference UsersRef;

    private String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+.[a-z]+";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_login, container, false);
        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        UserEmail = (EditText)view.findViewById(R.id.login_email);
        UserEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        UserPassword = (EditText) view.findViewById(R.id.login_password);
        UserPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        LoginButton = (Button)view. findViewById(R.id.login_button);
        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkEmailAndPassword();
            }
        });

        ForgotPassword = (TextView)view. findViewById(R.id.login_forgot_password);
        ForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), ForgotActivity.class));
            }
        });

        loadingBar = new ProgressDialog(getActivity());

        return  view;
    }

    private void checkEmailAndPassword() {
        if (UserEmail.getText().toString().matches(emailPattern)){
            if (UserPassword.length()>=8){

                loadingBar.setTitle("Login");
                loadingBar.setMessage("Please wait, while we are allowing you to login into your Account...");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();

                LoginButton.setEnabled(false);
                LoginButton.setTextColor(Color.argb(50,0,0,0));

                mAuth.signInWithEmailAndPassword(UserEmail.getText().toString(),UserPassword.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){
                                    VerifyEmailAddress();
                                    loadingBar.dismiss();
                                }else{
                                    LoginButton.setEnabled(true);
                                    LoginButton.setTextColor(Color.rgb(0,0,0));

                                    String message = task.getException().getMessage();
                                    Toast.makeText(getActivity(), "Error occured: " + message, Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                            }
                        });
            }else{
                UserPassword.setError("This Email ID doesn't match with our Database record");
            }
        }else{
            UserEmail.setError("This Email ID doesn't match with our Database record");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null)
        {
            SendUserToMainActivity();
        }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(getActivity(), MainActivity.class);
        startActivity(mainIntent);
    }

    private void checkInputs() {
        if (!TextUtils.isEmpty(UserEmail.getText().toString())){
            if (!TextUtils.isEmpty(UserPassword.getText().toString())){
                LoginButton.setEnabled(true);
                LoginButton.setTextColor(Color.rgb(0,0,0));
            }else{
                LoginButton.setEnabled(false);
                LoginButton.setTextColor(Color.argb(50,0,0,0));
            }
        }else{
            LoginButton.setEnabled(false);
            LoginButton.setTextColor(Color.argb(50,0,0,0));
        }
    }

    private void VerifyEmailAddress(){
        FirebaseUser user = mAuth.getCurrentUser();
        emailAddressChecker = user.isEmailVerified();
        if (emailAddressChecker){

            SendUserToMainActivity();
            Toast.makeText(getActivity(), "you are Logged In successfully.", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getActivity(),"Please verify your account first...",Toast.LENGTH_LONG).show();
            mAuth.signOut();
        }
    }

    private void SendUserToSetupActivity() {
        Intent mainIntent = new Intent(getActivity(), SetupActivity.class);
        startActivity(mainIntent);
    }
}