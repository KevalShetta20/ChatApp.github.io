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

import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hbb20.CountryCodePicker;
import com.kevalshetta.buddiesgram.R;

public class RegisterFragment extends Fragment {

    private EditText UserEmail, UserPassword, UserConfirmPassword, UserPhone;
    private CountryCodePicker CountryCode;
    private Button CreateAccountButton;
    private String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+.[a-z]+";

    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    public static final String TAG = "TAG";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_register, container, false);
        mAuth = FirebaseAuth.getInstance();

        UserEmail = (EditText) view. findViewById(R.id.register_email);
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

        UserPassword = (EditText) view. findViewById(R.id.register_password);
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

        UserConfirmPassword = (EditText)view. findViewById(R.id.register_confirm_password);
        UserConfirmPassword.addTextChangedListener(new TextWatcher() {
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

        CreateAccountButton = (Button)view. findViewById(R.id.register_create_account);
        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkEmailAndPassword();
            }
        });
        loadingBar = new ProgressDialog(getActivity());

        return  view;
    }

    private void checkEmailAndPassword() {
        if (UserEmail.getText().toString().matches(emailPattern)){
            if (UserPassword.getText().toString().equals(UserConfirmPassword.getText().toString())){

                loadingBar.setTitle("Creating New Account");
                loadingBar.setMessage("Please wait, while we are creating your new Account...");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

                CreateAccountButton.setEnabled(false);
                CreateAccountButton.setTextColor(Color.argb(50,0,0,0));

                mAuth.createUserWithEmailAndPassword(UserEmail.getText().toString(),UserPassword.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    SendEmailverificationMessage();
                                    loadingBar.dismiss();
                                }else{
                                    CreateAccountButton.setEnabled(true);
                                    CreateAccountButton.setTextColor(Color.rgb(0,0,0));

                                    String message = task.getException().getMessage();
                                    Toast.makeText(getActivity(), "Error Occured: " + message, Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                            }
                        });

            }else{
                UserPassword.setError("Password Doesn't match ");
            }
        }else{
            UserEmail.setError("Invalid Email");
        }
    }

    private void checkInputs(){
        if (!TextUtils.isEmpty(UserEmail.getText().toString())){
            if (!TextUtils.isEmpty(UserPassword.getText()) && UserPassword.length()>=8){
                if (!TextUtils.isEmpty(UserConfirmPassword.getText().toString())){
                    CreateAccountButton.setEnabled(true);
                    CreateAccountButton.setTextColor(Color.rgb(0,0,0));
                }else{
                    CreateAccountButton.setEnabled(false);
                    CreateAccountButton.setTextColor(Color.argb(50,0,0,0));
                }
            }else{
                CreateAccountButton.setEnabled(false);
                CreateAccountButton.setTextColor(Color.argb(50,0,0,0));
            }
        }else{
            CreateAccountButton.setEnabled(false);
            CreateAccountButton.setTextColor(Color.argb(50,0,0,0));
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

    private void SendEmailverificationMessage(){
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null){
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(getActivity(), "Registration Successful, we've send you a mail. Please check and verify your account in login...", Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                    }else{
                        String error = task.getException().getMessage();
                        Toast.makeText(getActivity(),"Error occurred:" + error,Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                    }
                }
            });
        }
    }
}