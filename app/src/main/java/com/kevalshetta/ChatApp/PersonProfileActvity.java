package com.kevalshetta.ChatApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kevalshetta.buddiesgram.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActvity extends AppCompatActivity {

    private EditText FullName,Status;
    private Button SendFriendRequest, DeclineFriendButton;
    private CircleImageView profileImage;

    private DatabaseReference UserRef, FriendReqRef, FriendRef, NotificationRef;
    private FirebaseAuth mAuth;

    private String receiverUserID, senderUserID, Current_State, saveCurrentDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile_actvity);

        mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendReqRef = FirebaseDatabase.getInstance().getReference().child("FriendRequest");
        FriendRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        senderUserID = mAuth.getCurrentUser().getUid();

        InitializeFields();

        UserRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.exists()) && (snapshot.hasChild("profileimage")) && (snapshot.hasChild("fullname"))) {
                    String userName = snapshot.child("fullname").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();
                    String userProfileImage = snapshot.child("profileimage").getValue().toString();

                    FullName.setText(userName);
                    Status.setText(userStatus);
                    Picasso.get().load(userProfileImage).placeholder(R.drawable.profile).into(profileImage);

                    MaintananceButton();
                }
                else  if ((snapshot.exists()) && (snapshot.hasChild("fullname"))){
                    String userName = snapshot.child("fullname").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();

                    FullName.setText(userName);
                    Status.setText(userStatus);

                    MaintananceButton();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DeclineFriendButton.setVisibility(View.INVISIBLE);
        DeclineFriendButton.setEnabled(false);

        if (!senderUserID.equals(receiverUserID)){
            SendFriendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SendFriendRequest.setEnabled(false);
                    if (Current_State.equals("not_friends"))
                    {
                        SendChatRequest();
                    }
                    if (Current_State.equals("request_sent"))
                    {
                        CancelChatRequest();
                    }
                    if (Current_State.equals("request_received"))
                    {
                        AcceptChatRequest();
                    }
                    if (Current_State.equals("friends"))
                    {
                        UnfriendPerson();
                    }
                }
            });
        }else
        {
            DeclineFriendButton.setVisibility(View.INVISIBLE);
            SendFriendRequest.setVisibility(View.INVISIBLE);
        }
    }

    private void UnfriendPerson() {
        FriendRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            FriendRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                SendFriendRequest.setEnabled(true);
                                                Current_State = "not_friends";
                                                SendFriendRequest.setText("Send Friend Request");

                                                DeclineFriendButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptChatRequest() {

        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        FriendRef.child(senderUserID).child(receiverUserID)
                .child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            FriendRef.child(receiverUserID).child(senderUserID)
                                    .child("date").setValue(saveCurrentDate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                FriendReqRef.child(senderUserID).child(receiverUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    FriendReqRef.child(receiverUserID).child(senderUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()){
                                                                                        SendFriendRequest.setEnabled(true);
                                                                                        Current_State = "friends";
                                                                                        SendFriendRequest.setText("Unfriend this Person");

                                                                                        DeclineFriendButton.setVisibility(View.INVISIBLE);
                                                                                        DeclineFriendButton.setEnabled(false);
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void CancelChatRequest() {
        FriendReqRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            FriendReqRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                SendFriendRequest.setEnabled(true);
                                                Current_State = "not_friends";
                                                SendFriendRequest.setText("Send Friend Request");

                                                DeclineFriendButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void MaintananceButton() {
        FriendReqRef.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(receiverUserID)){

                    String request_type = snapshot.child(receiverUserID).child("request_type").getValue().toString();
                    
                    if (request_type.equals("sent"))
                    {
                        Current_State = "request_sent";
                        SendFriendRequest.setText("Cancel Friend Request");

                        DeclineFriendButton.setVisibility(View.INVISIBLE);
                        DeclineFriendButton.setEnabled(false);
                    }
                    else if (request_type.equals("received"))
                    {
                        Current_State = "request_received";
                        SendFriendRequest.setText("Accept Friend Request");

                        DeclineFriendButton.setVisibility(View.VISIBLE);
                        DeclineFriendButton.setEnabled(true);

                        DeclineFriendButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view)
                            {
                                CancelChatRequest();
                            }
                        });
                    }
                }
                else
                {
                    FriendRef.child(senderUserID)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot)
                                {
                                    if (dataSnapshot.hasChild(receiverUserID))
                                    {
                                        Current_State = "friends";
                                        SendFriendRequest.setText("Unfriend this Person");
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void SendChatRequest() {
        FriendReqRef.child(senderUserID).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            FriendReqRef.child(receiverUserID).child(senderUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                                /*if (task.isSuccessful()){
                                                    HashMap<String, String> chatNotificationMap = new HashMap<>();
                                                    chatNotificationMap.put("from", senderUserID);
                                                    chatNotificationMap.put("to", receiverUserID);
                                                    chatNotificationMap.put("type", "request");

                                                     NotificationRef.child(receiverUserID).push()
                                                            .setValue(chatNotificationMap)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task)
                                                                { */
                                                                    if (task.isSuccessful())
                                                                    {
                                                                        SendFriendRequest.setEnabled(true);
                                                                        Current_State = "request_sent";
                                                                        SendFriendRequest.setText("Cancel Friend Request");

                                                                        DeclineFriendButton.setVisibility(View.INVISIBLE);
                                                                        DeclineFriendButton.setEnabled(false);
                                                                    }
                                                                /*}
                                                            }); */
                                        }
                                    });
                        }
                    }
                });
    }

    private void InitializeFields() {
        FullName = findViewById(R.id.friends_fullname);
        Status = findViewById(R.id.friends_description);
        profileImage = findViewById(R.id.friends_profile_image);
        SendFriendRequest = findViewById(R.id.send_friend_request);
        DeclineFriendButton = findViewById(R.id.decline_friend_request);

        Current_State = "not_friends";
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
        Intent mainIntent = new Intent(PersonProfileActvity.this, MainActivity.class);
        startActivity(mainIntent);
    }
}