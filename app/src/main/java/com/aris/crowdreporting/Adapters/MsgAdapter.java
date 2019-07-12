package com.aris.crowdreporting.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aris.crowdreporting.HelperClasses.Chats;
import com.aris.crowdreporting.HelperUtils.TimeAgo;
import com.aris.crowdreporting.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.MsgViewHolder> {

    List<Chats> chatsList;
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private Context context;

    public MsgAdapter(List<Chats> chatsList) {
        this.chatsList = chatsList;
    }

    @NonNull
    @Override
    public MsgViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        context = viewGroup.getContext();

        if (i == MSG_TYPE_RIGHT) {

            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_item_right, viewGroup, false);
            return new MsgViewHolder(view);

        } else {

            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_item_left, viewGroup, false);
            return new MsgViewHolder(view);

        }
    }

    @Override
    public void onBindViewHolder(@NonNull MsgViewHolder msgViewHolder, int i) {

        Chats chats = chatsList.get(i);


        //SET MSG
        msgViewHolder.showMsg.setText(chats.getMessage());

        //SET TIME
        try {
            long millisecond = chats.getTimestamp().getTime();
            String dateString = DateFormat.format("HH.mm", new Date(millisecond)).toString();

//            String timeAgo = TimeAgo.getTimeAgo(millisecond);

            msgViewHolder.timeMsg.setText(dateString);
        } catch (Exception e){
            msgViewHolder.timeMsg.setText("just now");
        }



    }

    @Override
    public int getItemCount() {
        return chatsList.size();
    }

    public class MsgViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private CircleImageView profileImage;
        private TextView showMsg, timeMsg, txtSeen;


        public MsgViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            profileImage = mView.findViewById(R.id.profile_chat_img);
            showMsg = mView.findViewById(R.id.show_msg);
            timeMsg = mView.findViewById(R.id.time_msg);

        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatsList.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}
