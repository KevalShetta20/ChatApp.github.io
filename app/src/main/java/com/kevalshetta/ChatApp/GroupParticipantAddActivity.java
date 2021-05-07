package com.kevalshetta.ChatApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kevalshetta.buddiesgram.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupParticipantAddActivity extends AppCompatActivity {

    private RecyclerView group_friends_Rv;

    private FirebaseAuth mAuth;
    private DatabaseReference GroupRef, FriendsRef, UsersRef;

    private Toolbar group_participant_toolbar;

    private ActionBar actionBar;
    private String groupId;
    private String myGroupRole;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_participant_add);

        group_participant_toolbar = findViewById(R.id.group_participant_toolbar);
        setSupportActionBar(group_participant_toolbar);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Add Participants");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        GroupRef = FirebaseDatabase.getInstance().getReference("Groups");
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        group_friends_Rv = findViewById(R.id.participants_user);
        groupId = getIntent().getStringExtra("groupid");

        loadGroupInfo();
    }

    private void loadGroupInfo() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");

        GroupRef.orderByChild("groupid").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    String groupId = ""+ds.child("groupid").getValue();
                    String grouptitle = ""+ds.child("grouptitle").getValue();
                    String groupdescription = ""+ds.child("groupdescription").getValue();
                    String groupicon = ""+ds.child("groupicon").getValue();
                    String timestamp = ""+ds.child("timestamp").getValue();
                    String createdBy = ""+ds.child("createdBy").getValue();
                    actionBar.setTitle("Add Participants");

                    reference.child(groupId).child("Participants").child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                myGroupRole = ""+snapshot.child("role").getValue();
                                actionBar.setTitle(grouptitle + "(" + myGroupRole +")");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Participants> options = new
                FirebaseRecyclerOptions.Builder<Participants>()
                .setQuery(FriendsRef,Participants.class)
                .build();

        FirebaseRecyclerAdapter<Participants, ParticipantsHolder> adapter = new FirebaseRecyclerAdapter<Participants, ParticipantsHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ParticipantsHolder holder, int position, @NonNull Participants model) {
                final String usersIDs = getRef(position).getKey();
                final String[] userImage = {"default_image"};

                UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild("profileimage"))
                        {
                            userImage[0] = snapshot.child("profileimage").getValue().toString();
                            Picasso.get().load(userImage[0]).placeholder(R.drawable.profile).into(holder.participants_profile_image);
                        }

                        String profileName = snapshot.child("fullname").getValue().toString();
                        String profileStatus = snapshot.child("status").getValue().toString();

                        holder.participants_full_name.setText(profileName);
                        holder.participants_status.setText(profileStatus);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public ParticipantsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                ParticipantsHolder viewHolder = new ParticipantsHolder(view);
                return viewHolder;
            }
        };
        group_friends_Rv.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ParticipantsHolder extends RecyclerView.ViewHolder{

        CircleImageView participants_profile_image;
        TextView participants_full_name, participants_status;

        public ParticipantsHolder(@NonNull View itemView) {
            super(itemView);
            participants_profile_image = itemView.findViewById(R.id.participants_profile_image);
            participants_full_name = itemView.findViewById(R.id.participants_full_name);
            participants_status = itemView.findViewById(R.id.participants_status);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}