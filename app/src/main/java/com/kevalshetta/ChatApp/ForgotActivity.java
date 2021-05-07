package com.kevalshetta.ChatApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.transition.TransitionManager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.kevalshetta.buddiesgram.R;

public class ForgotActivity extends AppCompatActivity {

    private Button Reset;
    private TextView GoBack;
    private EditText forgotEmail;
    private FirebaseAuth mAuth;

    private ViewGroup emailIconContainer;
    private ImageView emailIcon;
    private TextView emailIconText;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);

        mAuth = FirebaseAuth.getInstance();

        forgotEmail = findViewById(R.id.forgot_email);
        forgotEmail.addTextChangedListener(new TextWatcher() {
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

        Reset = findViewById(R.id.forgot_reset_button);
        Reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TransitionManager.beginDelayedTransition(emailIconContainer);
                emailIconText.setVisibility(View.GONE);

                TransitionManager.beginDelayedTransition(emailIconContainer);
                emailIcon.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);

                Reset.setEnabled(false);
                Reset.setTextColor(Color.argb(50,0,0,0));

                mAuth.sendPasswordResetEmail(forgotEmail.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {

                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    ScaleAnimation scaleAnimation = new ScaleAnimation(1,0,1,0,emailIcon.getWidth()/2,emailIcon.getHeight()/2);
                                    scaleAnimation.setDuration(100);
                                    scaleAnimation.setInterpolator(new AccelerateInterpolator());
                                    scaleAnimation.setRepeatMode(Animation.REVERSE);
                                    scaleAnimation.setRepeatCount(1);
                                    scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
                                        @Override
                                        public void onAnimationStart(Animation animation) {
                                            emailIconText.setText("Recovery email sent successfully ! check your inbox");
                                            emailIconText.setTextColor(getResources().getColor(R.color.successGreen));
                                            emailIconText.setVisibility(View.VISIBLE);

                                            TransitionManager.beginDelayedTransition(emailIconContainer);
                                            emailIcon.setVisibility(View.VISIBLE);
                                        }

                                        @Override
                                        public void onAnimationEnd(Animation animation) {
                                            emailIcon.setImageResource(R.drawable.mail_sent);
                                        }

                                        @Override
                                        public void onAnimationRepeat(Animation animation) {

                                        }
                                    });

                                    emailIcon.startAnimation(scaleAnimation);
                                }
                                else{
                                    Reset.setEnabled(true);
                                    Reset.setTextColor(Color.rgb(0,0,0));

                                    String message = task.getException().getMessage();

                                    emailIconText.setText(message);
                                    emailIconText.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                                    TransitionManager.beginDelayedTransition(emailIconContainer);
                                    emailIconText.setVisibility(View.VISIBLE);

                                }
                                progressBar.setVisibility(View.GONE);
                            }
                        });
            }
        });

        GoBack = findViewById(R.id.forgot_go_back);
        GoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToLoginActivity();
            }
        });

        emailIconContainer = findViewById(R.id.forgot_password_email_icon_container);
        emailIcon = findViewById(R.id.forgot_password_email_icon);
        emailIconText = findViewById(R.id.forgot_password_email_icon_text);
        progressBar = findViewById(R.id.progressBar);
    }

    private void checkInputs() {
        if (TextUtils.isEmpty(forgotEmail.getText())){
            Reset.setEnabled(false);
            Reset.setTextColor(Color.argb(50,0,0,0));
        }else{
            Reset.setEnabled(true);
            Reset.setTextColor(Color.rgb(0,0,0));
        }
    }

    private void SendUserToLoginActivity() {
        Intent fIntent = new Intent(ForgotActivity.this, LoginActivity.class);
        startActivity(fIntent);
    }
}