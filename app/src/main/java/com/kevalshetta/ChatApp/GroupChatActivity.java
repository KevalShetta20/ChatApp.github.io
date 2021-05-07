package com.kevalshetta.ChatApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kevalshetta.buddiesgram.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class GroupChatActivity extends AppCompatActivity {

    private String groupId, myGroupRole;
    private Toolbar group_chat_toolbar;
    private CircleImageView group_profile_image;
    private TextView group_title_name;
    private RecyclerView groupchatRv;

    private ImageView SendFilesButton, addParticipant;

    private EmojiconEditText MessageInputText;
    private EmojIconActions emojIconActions;
    private ImageView send,emoji;
    private View view;

    private String currentUserId, saveCurrentTime, saveCurrentDate;

    private FirebaseAuth mAuth;
    private DatabaseReference GroupRef;
    
    private ArrayList<GroupMessages> groupMessagesList;
    private GroupMessagesAdapter groupMessagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupid");

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        GroupRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        InitializeController();
        
        loadGroupInfo();

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });
        
        loadGroupMessages();

        loadMyGroupRole();
    }

    private void loadMyGroupRole() {
        GroupRef.child(groupId).child("Participants")
                .orderByChild("uid").equalTo(mAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            myGroupRole = ""+ds.child("role").getValue();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadGroupMessages() {

        groupMessagesList = new ArrayList<>();

        GroupRef.child(groupId).child("Messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupMessagesList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    GroupMessages model = ds.getValue(GroupMessages.class);
                    groupMessagesList.add(model);
                }
                groupMessagesAdapter = new GroupMessagesAdapter(GroupChatActivity.this, groupMessagesList);
                groupchatRv.setAdapter(groupMessagesAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void SendMessage() {
        String messageText = MessageInputText.getText().toString();
        if (TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this, "Can't send empty message...", Toast.LENGTH_SHORT).show();
        }else{
            sendMessage(messageText);
        }
    }

    private void sendMessage(String messageText) {
        String timestamp = ""+System.currentTimeMillis();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", "" + currentUserId);
        hashMap.put("message", "" + messageText);
        hashMap.put("timestamp", "" + timestamp);
        hashMap.put("type", "" + "text");

        GroupRef.child(groupId).child("Messages").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        MessageInputText.setText("");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupChatActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGroupInfo() {
        GroupRef.orderByChild("groupid").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    String grouptitle = ""+ds.child("grouptitle").getValue();
                    String groupdescription = ""+ds.child("groupdescription").getValue();
                    String groupicon = ""+ds.child("groupicon").getValue();
                    String timestamp = ""+ds.child("timestamp").getValue();
                    String createdBy = ""+ds.child("createdBy").getValue();

                    group_title_name.setText(grouptitle);
                    try {
                        Picasso.get().load(groupicon).placeholder(R.drawable.profile).into(group_profile_image);
                    } catch (Exception e) {
                        group_profile_image.setImageResource(R.drawable.profile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void InitializeController() {
        group_chat_toolbar = findViewById(R.id.group_chat_toolbar);
        setSupportActionBar(group_chat_toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        group_profile_image = findViewById(R.id.group_profile_image);
        group_title_name = findViewById(R.id.group_title_name);

        MessageInputText = findViewById(R.id.group_emoji_edit_text);
        send = findViewById(R.id.group_send_icon);
        emoji = findViewById(R.id.group_emoji_icon);
        view = findViewById(R.id.group_root_view);
        SendFilesButton = findViewById(R.id.group_send_files_btn);
        addParticipant = findViewById(R.id.add_group_participant);

        emojIconActions = new EmojIconActions(this,view,MessageInputText,emoji);
        emojIconActions.ShowEmojIcon();

        groupchatRv = (RecyclerView) findViewById(R.id.groupchatRv);

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        addParticipant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupChatActivity.this,GroupParticipantAddActivity.class);
                intent.putExtra("groupid", groupId);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            SendUserToMainActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(GroupChatActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }
}
