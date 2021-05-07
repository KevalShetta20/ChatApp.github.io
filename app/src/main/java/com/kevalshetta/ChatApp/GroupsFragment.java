package com.kevalshetta.ChatApp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupsFragment extends Fragment {

    private View GroupChatsView;
    private RecyclerView groupList;

    private DatabaseReference GroupsRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    public GroupsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        GroupChatsView = inflater.inflate(R.layout.fragment_groups, container, false);

        groupList = (RecyclerView) GroupChatsView.findViewById(R.id.groupsRv);
        groupList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        GroupsRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        return GroupChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<GroupChatList> options = new
                FirebaseRecyclerOptions.Builder<GroupChatList>()
                .setQuery(GroupsRef, GroupChatList.class)
                .build();

        FirebaseRecyclerAdapter<GroupChatList, HolderGroupChat> adapter = new FirebaseRecyclerAdapter<GroupChatList, HolderGroupChat>(options) {
            @Override
            protected void onBindViewHolder(@NonNull HolderGroupChat holder, int position, @NonNull GroupChatList model) {
                final String usersIDs = getRef(position).getKey();
                final String[] groupImage = {"default_image"};

                GroupsRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child("Participants").child(currentUserID).exists() && snapshot.hasChild("grouptitle")){
                            holder.itemView.setVisibility(View.VISIBLE);

                            holder.itemView.findViewById(R.id.groupTitleTv).setVisibility(View.VISIBLE);
                            holder.itemView.findViewById(R.id.nameTv).setVisibility(View.VISIBLE);
                            holder.itemView.findViewById(R.id.messageTv).setVisibility(View.VISIBLE);
                            holder.itemView.findViewById(R.id.timeTv).setVisibility(View.VISIBLE);
                            holder.itemView.findViewById(R.id.groupIconTv).setVisibility(View.VISIBLE);
                            holder.itemView.findViewById(R.id.mainLL).setVisibility(View.VISIBLE);
                            holder.itemView.findViewById(R.id.leftll).setVisibility(View.VISIBLE);

                            String groupImage = model.getGroupicon();
                            try {
                                Picasso.get().load(groupImage).placeholder(R.drawable.profile).into(holder.groupIconTv);
                            } catch (Exception e) {
                                holder.groupIconTv.setImageResource(R.drawable.profile);
                            }

                            String groupTitle = snapshot.child("grouptitle").getValue().toString();
                            holder.groupTitleTv.setText(groupTitle);

                            holder.nameTv.setText("");
                            holder.timeTv.setText("");
                            holder.messageTv.setText("");

                            loadLastMessage(model, holder);
                        }

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getContext(),GroupChatActivity.class);
                                intent.putExtra("groupid", usersIDs);
                                startActivity(intent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public HolderGroupChat onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_groupchats_list, parent, false);
                return new HolderGroupChat(view);
            }
        };
        groupList.setHasFixedSize(true);
        groupList.setAdapter(adapter);
        adapter.startListening();
    }

    private void loadLastMessage(GroupChatList model, HolderGroupChat holder) {
        GroupsRef.child(model.getGroupid()).child("Messages").limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            String message = ""+ds.child("message").getValue();
                            String timestamp = ""+ds.child("timestamp").getValue();
                            String sender = ""+ds.child("sender").getValue();

                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(timestamp));
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();

                            holder.messageTv.setText(message);
                            holder.timeTv.setText(dateTime);

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
                            ref.orderByChild("currentuserid").equalTo(sender).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds: snapshot.getChildren()){
                                        String name = ""+ds.child("fullname").getValue();
                                        holder.nameTv.setText(name + ":");
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

    public static class HolderGroupChat extends RecyclerView.ViewHolder{

        TextView groupTitleTv, nameTv, messageTv, timeTv;
        CircleImageView groupIconTv;

        public HolderGroupChat(@NonNull View itemView) {
            super(itemView);

            groupTitleTv = itemView.findViewById(R.id.groupTitleTv);
            nameTv = itemView.findViewById(R.id.nameTv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            groupIconTv = itemView.findViewById(R.id.groupIconTv);
        }
    }
}