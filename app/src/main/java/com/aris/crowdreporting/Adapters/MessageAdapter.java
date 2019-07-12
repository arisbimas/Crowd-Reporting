package com.aris.crowdreporting.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aris.crowdreporting.Activities.MessageActivity;
import com.aris.crowdreporting.HelperClasses.Chats;
import com.aris.crowdreporting.HelperClasses.User;
import com.aris.crowdreporting.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    public List<Chats> mChats;
    public Context context;
    public String chatUserImage;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    public MessageAdapter(List<Chats> mChats, String chatUserImage){


        this.mChats = mChats;
        this.chatUserImage = chatUserImage;

    }

    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        context = parent.getContext();

        if (viewType == MSG_TYPE_RIGHT) {

            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false);
            return new MessageAdapter.ViewHolder(view);

        } else {

            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);
            return new MessageAdapter.ViewHolder(view);

        }
    }

    @Override
    public void onBindViewHolder(final MessageAdapter.ViewHolder holder, int position) {
        holder.setIsRecyclable(false);

        Chats chats = mChats.get(position);

        holder.showMsg.setText(chats.getMessage());

        RequestOptions placeholderOption = new RequestOptions();
        placeholderOption.placeholder(R.drawable.profile_placeholder);
        Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(chatUserImage).into(holder.profileImage);

    }


    @Override
    public int getItemCount() {

        return mChats.size();

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;

        private CircleImageView profileImage;
        private TextView showMsg, txtSeen;


        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            profileImage = mView.findViewById(R.id.profile_chat_img);
            showMsg = mView.findViewById(R.id.show_msg);
            txtSeen = mView.findViewById(R.id.txt_seen);

        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mChats.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}
