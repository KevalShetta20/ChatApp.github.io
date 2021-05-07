package com.kevalshetta.ChatApp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kevalshetta.buddiesgram.R;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText FullName,Status;
    private Button saveInfo;
    private CircleImageView profileImage;

    private FirebaseAuth mAuth;
    String currentUserID;
    private DatabaseReference UsersRef;
    private StorageReference UserProfileImageRef;

    private ProgressDialog loadingBar;

    private Uri imageUri = null;

    Integer REQUEST_CAMERA=1, SELECT_FILE=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mToolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Profile");

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        FullName = findViewById(R.id.user_fullname);
        FullName.addTextChangedListener(new TextWatcher() {
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

        Status = findViewById(R.id.user_description);
        Status.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        saveInfo = findViewById(R.id.user_save);
        saveInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ValidateAccountInfo();
            }
        });

        profileImage = findViewById(R.id.user_profile_image);
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final CharSequence[] items={"Camera","Gallery", "Cancel"};

                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                builder.setTitle("Select Image");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        if (items[i].equals("Camera")) {
                            if(hasPermission(ProfileActivity.this, Manifest.permission.CAMERA)){
                                Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(camera,1000);
                            }else {
                                // ask for camera permissions
                                ActivityCompat.requestPermissions(ProfileActivity.this,new String[]{Manifest.permission.CAMERA},102);
                            }
                        } else if (items[i].equals("Gallery")) {
                            if(hasPermission(ProfileActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                intent.setType("image/*");
                                startActivityForResult(intent, SELECT_FILE);
                            }else {
                                ActivityCompat.requestPermissions(ProfileActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},103);
                            }

                        } else if (items[i].equals("Cancel")) {
                            loadingBar.dismiss();
                        }
                    }
                });
                builder.show();
            }
        });

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.exists()) && (snapshot.hasChild("profileimage")) && (snapshot.hasChild("fullname"))) {
                    String userName = snapshot.child("fullname").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();
                    String userProfileImage = snapshot.child("profileimage").getValue().toString();

                    FullName.setText(userName);
                    Status.setText(userStatus);
                    Picasso.get().load(userProfileImage).placeholder(R.drawable.profile).into(profileImage);
                }
                else  if ((snapshot.exists()) && (snapshot.hasChild("fullname"))){
                    String userName = snapshot.child("fullname").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();

                    FullName.setText(userName);
                    Status.setText(userStatus);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        loadingBar = new ProgressDialog(this);
    }
    private boolean hasPermission(Context context,String permission){
        return ContextCompat.checkSelfPermission(context,permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 102){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                // permission is granted
                Toast.makeText(this, "Permission is Granted.", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Permission is Denied.", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == 103){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                // permission is granted
                Toast.makeText(this, "Permission is Granted.", Toast.LENGTH_SHORT).show();

            }else {
                Toast.makeText(this, "Permission is Denied.", Toast.LENGTH_SHORT).show();
            }
        }
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            // alert dialog

            AlertDialog.Builder extraInfo = new AlertDialog.Builder(this);
            extraInfo.setTitle("Storage Permission is Required.");
            extraInfo.setMessage("To Run this app, App needs access to storage to save the file.");

            extraInfo.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(ProfileActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},103);
                }
            });

            extraInfo.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(ProfileActivity.this, "Some Feature of App Might not Work.", Toast.LENGTH_SHORT).show();
                }
            });
            extraInfo.create().show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==SELECT_FILE && resultCode==RESULT_OK && data!=null){
            imageUri= data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        else if (requestCode==1000 && resultCode==RESULT_OK && data!=null){
            imageUri= data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK){
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait, while we updating your profile image...");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

                Uri resultUri = result.getUri();
                final StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");
                filePath.putFile(resultUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()){
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            Uri downUri = task.getResult();

                            final String downloadUrl = downUri.toString();
                            UsersRef.child("profileimage").setValue(downloadUrl).
                                    addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(ProfileActivity.this, "Profile Image stored to Database Successfully...", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }else{
                                                String message = task.getException().getMessage();
                                                Toast.makeText(ProfileActivity.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                        }
                                    });
                        }
                    }
                });
            }else{
                Toast.makeText(this, "Error Occured: Image can not be cropped. Try Again.", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }

    private void ValidateAccountInfo() {
        String fullname = FullName.getText().toString();
        String status = Status.getText().toString();

        if (!TextUtils.isEmpty(fullname)){
            if (!TextUtils.isEmpty(status)){

                loadingBar.setTitle("Saving Information");
                loadingBar.setMessage("Please wait, while we are creating your new Account...");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

                HashMap userMap = new HashMap();
                userMap.put("fullname", fullname);
                userMap.put("status", status);
                UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()){
                            SendUserToMainActivity();
                            Toast.makeText(ProfileActivity.this, "your Account is updated Successfully.", Toast.LENGTH_LONG).show();
                            loadingBar.dismiss();
                        }else{
                            saveInfo.setEnabled(true);
                            saveInfo.setTextColor(Color.rgb(0,0,0));

                            String message =  task.getException().getMessage();
                            Toast.makeText(ProfileActivity.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });

            }else{
                Toast.makeText(ProfileActivity.this,"Please Enter Description",Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(ProfileActivity.this,"Please Enter Full Name",Toast.LENGTH_SHORT).show();
        }
    }

    private void checkInputs() {
        if (!TextUtils.isEmpty(FullName.getText().toString())){
            if (!TextUtils.isEmpty(Status.getText().toString())){
                saveInfo.setEnabled(true);
                saveInfo.setTextColor(Color.rgb(0,0,0));
            }else{
                saveInfo.setEnabled(false);
                saveInfo.setTextColor(Color.argb(50,0,0,0));
            }
        }else{
            saveInfo.setEnabled(false);
            saveInfo.setTextColor(Color.argb(50,0,0,0));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home)
        {
            SendUserToMainActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }
}