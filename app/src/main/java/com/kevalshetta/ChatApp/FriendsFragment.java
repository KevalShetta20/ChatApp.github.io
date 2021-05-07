package com.kevalshetta.ChatApp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsFragment extends Fragment {
    private View FriendsView;
    private RecyclerView myFriendsList;

    private DatabaseReference FriendsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FriendsView = inflater.inflate(R.layout.fragment_friends, container, false);

        myFriendsList = (RecyclerView) FriendsView.findViewById(R.id.friends_list);
        myFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();


        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUserID);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        return FriendsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(FriendsRef, Friends.class)
                        .build();

        final FirebaseRecyclerAdapter<Friends,ContactsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Friends, ContactsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull ContactsViewHolder holder, int position, @NonNull Friends model) {

                        holder.itemView.findViewById(R.id.send_message_from_friend).setVisibility(View.VISIBLE);
                        holder.itemView.findViewById(R.id.online_status).setVisibility(View.INVISIBLE);

                        final String userIDs = getRef(position).getKey();
                        final String[] userImage = {"default_image"};

                        UsersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){

                                    if (snapshot.child("userState").hasChild("state"))
                                    {
                                        String state = snapshot.child("userState").child("state").getValue().toString();
                                        String date = snapshot.child("userState").child("date").getValue().toString();
                                        String time = snapshot.child("userState").child("time").getValue().toString();

                                        if (state.equals("online"))
                                        {
                                            holder.online.setVisibility(View.VISIBLE);
                                        }
                                        else if (state.equals("offline"))
                                        {
                                            holder.online.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                    else
                                    {
                                        holder.online.setVisibility(View.INVISIBLE);
                                    }

                                    if (snapshot.hasChild("profileimage"))
                                    {
                                        userImage[0] = snapshot.child("profileimage").getValue().toString();
                                        Picasso.get().load(userImage[0]).placeholder(R.drawable.profile).into(holder.profileImage);
                                    }
                                    String profileName = snapshot.child("fullname").getValue().toString();
                                    String profileStatus = snapshot.child("status").getValue().toString();

                                    holder.userName.setText(profileName);
                                    holder.userStatus.setText(profileStatus);

                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            Intent profileIntent = new Intent(getContext(), PersonProfileActvity.class);
                                            profileIntent.putExtra("visit_user_id", userIDs);
                                            startActivity(profileIntent);
                                        }
                                    });

                                    holder.itemView.findViewById(R.id.send_message_from_friend).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                            chatIntent.putExtra("visit_user_id",userIDs);
                                            chatIntent.putExtra("visit_user_name",profileName);
                                            chatIntent.putExtra("visit_image", userImage[0]);
                                            startActivity(chatIntent);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                        ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                        return viewHolder;
                    }
                };
        myFriendsList.setAdapter(adapter);
        adapter.startListening();
    }
    public static class ContactsViewHolder extends RecyclerView.ViewHolder{

        TextView userName, userStatus;
        CircleImageView profileImage;
        ImageView sendMessage,online;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.find_user_full_name);
            userStatus = itemView.findViewById(R.id.find_user_status);
            profileImage = itemView.findViewById(R.id.find_users_profile_image);
            sendMessage = itemView.findViewById(R.id.send_message_from_friend);
            online = itemView.findViewById(R.id.online_status);
        }
    }
}