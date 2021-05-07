package com.kevalshetta.ChatApp;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kevalshetta.buddiesgram.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class GroupMessagesAdapter extends RecyclerView.Adapter<GroupMessagesAdapter.HolderGroupMessages>{
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private ArrayList<GroupMessages> modelGroupChatsList;

    private FirebaseAuth mAuth;

    public GroupMessagesAdapter(Context context, ArrayList<GroupMessages> modelGroupChatsList) {
        this.context = context;
        this.modelGroupChatsList = modelGroupChatsList;

        mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderGroupMessages onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_right, parent,false);
            return new HolderGroupMessages(view);
        }else{
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_left, parent,false);
            return new HolderGroupMessages(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupMessages holder, int position) {

        GroupMessages model = modelGroupChatsList.get(position);

        String timestamp = model.getTimestamp();
        String message = model.getMessage();
        String senderUid = model.getSender();

        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();

        holder.lmessageTv.setText(message);
        holder.ltimeTv.setText(dateTime);

        setUserName(model, holder);
    }

    private void setUserName(GroupMessages model, HolderGroupMessages holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
        ref.orderByChild("currentuserid").equalTo(model.getSender()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    String name = ""+ds.child("fullname").getValue();

                    holder.lnameTv.setText(name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return modelGroupChatsList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (modelGroupChatsList.get(position).getSender().equals(mAuth.getUid())){
            return MSG_TYPE_RIGHT;
        }else{
            return MSG_TYPE_LEFT;
        }
    }

    class HolderGroupMessages extends RecyclerView.ViewHolder{

        TextView lnameTv, lmessageTv, ltimeTv;

        public HolderGroupMessages(@NonNull View itemView) {
            super(itemView);

            lnameTv = itemView.findViewById(R.id.lnameTv);
            lmessageTv = itemView.findViewById(R.id.lmessageTv);
            ltimeTv = itemView.findViewById(R.id.ltimeTv);
        }
    }
}
