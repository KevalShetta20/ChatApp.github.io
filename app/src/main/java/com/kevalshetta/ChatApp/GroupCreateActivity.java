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
import android.content.ContentValues;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kevalshetta.buddiesgram.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupCreateActivity extends AppCompatActivity{

    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    String currentUserID;
    private DatabaseReference UsersRef,GroupRef;

    private CircleImageView groupImage;
    private EditText groupTitle,groupDescription;
    private Button save;

    private StorageReference GroupProfileImageRef;

    final static int Gallery_Pick = 1;

    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    private Uri imageUri = null, resultUri=null;
    private String  saveCurrentTime;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_create);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        GroupRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        mToolbar = (Toolbar) findViewById(R.id.group_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Create Group");

        groupTitle = findViewById(R.id.group_title);
        groupTitle.addTextChangedListener(new TextWatcher() {
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

        groupDescription = findViewById(R.id.group_description);
        groupDescription.addTextChangedListener(new TextWatcher() {
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

        groupImage = findViewById(R.id.group_image);

        save = findViewById(R.id.group_save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveAccountSetupInformation();
            }
        });

        groupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });

        loadingBar = new ProgressDialog(this);

        GroupProfileImageRef= FirebaseStorage.getInstance().getReference().child("Group Profile Images");

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());
    }

    private void SaveAccountSetupInformation() {
        String grouptitle = groupTitle.getText().toString();
        String groupdescription = groupDescription.getText().toString();
        String g_timestamp = ""+System.currentTimeMillis();

        if (!TextUtils.isEmpty(grouptitle)){
            createGroup(""+g_timestamp,
                    ""+grouptitle,
                    ""+groupdescription,
                    ""
            );
        }else{
            Toast.makeText(GroupCreateActivity.this,"Enter group title",Toast.LENGTH_SHORT).show();
        }

    }

    private void createGroup(String g_timestamp, String grouptitle, String groupdescription, String groupicon) {
        HashMap userMap = new HashMap();
        userMap.put("groupid", ""+g_timestamp);
        userMap.put("grouptitle", ""+grouptitle);
        userMap.put("groupdescription", ""+groupdescription);
        userMap.put("groupicon", ""+groupicon);
        userMap.put("timestamp", ""+saveCurrentTime);
        userMap.put("createdBy", ""+currentUserID);

        GroupRef.child(g_timestamp).setValue(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                HashMap<String, Object> userMap1 = new HashMap<>();
                userMap1.put("uid", currentUserID);
                userMap1.put("role","creator");
                userMap1.put("timestamp", g_timestamp);

                GroupRef.child(g_timestamp).child("Participants").child(mAuth.getUid()).setValue(userMap1).
                        addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    SendUserToMainActivity();
                                    Toast.makeText(GroupCreateActivity.this, "Group created Successfully.", Toast.LENGTH_LONG).show();
                                    loadingBar.dismiss();
                                }else{
                                    save.setEnabled(true);
                                    save.setTextColor(Color.rgb(0,0,0));

                                    String message =  task.getException().getMessage();
                                    Toast.makeText(GroupCreateActivity.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                            }
                        });
            }
        });
    }

    private void checkInputs() {
        if (!TextUtils.isEmpty(groupTitle.getText().toString())){
            save.setEnabled(true);
            save.setTextColor(Color.rgb(0,0,0));
        }else{
            save.setEnabled(false);
            save.setTextColor(Color.argb(50,0,0,0));
        }
    }

    private void showImagePickDialog() {
        final CharSequence[] items={"Camera","Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(GroupCreateActivity.this);
        builder.setTitle("Select Image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (items[i].equals("Camera")) {
                    if(hasPermission(GroupCreateActivity.this, Manifest.permission.CAMERA)){
                        pickFromCamera();
                    }else {
                        // ask for camera permissions
                        ActivityCompat.requestPermissions(GroupCreateActivity.this,new String[]{Manifest.permission.CAMERA},102);
                    }
                } else if (items[i].equals("Gallery")) {
                    if(hasPermission(GroupCreateActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                        pickFromGallery();
                    }else {
                        ActivityCompat.requestPermissions(GroupCreateActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},103);
                    }

                } else if (items[i].equals("Cancel")) {
                    loadingBar.dismiss();
                }
            }
        });
        builder.show();
    }

    private boolean hasPermission(Context context, String permission){
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
                    ActivityCompat.requestPermissions(GroupCreateActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},103);
                }
            });

            extraInfo.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(GroupCreateActivity.this, "Some Feature of App Might not Work.", Toast.LENGTH_SHORT).show();
                }
            });
            extraInfo.create().show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        String grouptitle = groupTitle.getText().toString();
        String groupdescription = groupDescription.getText().toString();
        String g_timestamp = ""+System.currentTimeMillis();

        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode== IMAGE_PICK_GALLERY_CODE && resultCode==RESULT_OK && data!=null){
            imageUri= data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        else if (requestCode==IMAGE_PICK_CAMERA_CODE && resultCode==RESULT_OK && data!=null){
            imageUri= data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK)if(resultCode == RESULT_OK){
                loadingBar.setTitle("Group Profile Image");
                loadingBar.setMessage("Please wait, while we updating your group profile image...");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

                Uri resultUri = result.getUri();
                final StorageReference filePath = GroupProfileImageRef.child(g_timestamp + currentUserID +".jpg");
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

                            if (!TextUtils.isEmpty(grouptitle)){
                                createGroup(""+g_timestamp,
                                        ""+grouptitle,
                                        ""+groupdescription,
                                        ""+downloadUrl
                                );
                            }else{
                                Toast.makeText(GroupCreateActivity.this,"Please enter group title first",Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                            }
                        }
                    }
                });
            }
            else{
                Toast.makeText(this, "Error Occured: Image can not be cropped. Try Again.", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }

    private void pickFromCamera() {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE,"Group Image Icon");
        cv.put(MediaStore.Images.Media.DESCRIPTION,"Group Image Icon Description");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
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
        Intent mainIntent = new Intent(GroupCreateActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }
}