package com.aris.crowdreporting.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aris.crowdreporting.Activities.MessageActivity;
import com.aris.crowdreporting.HelperClasses.User;
import com.aris.crowdreporting.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsUsersAdapter extends RecyclerView.Adapter<ChatsUsersAdapter.ViewHolder> {

    public List<User> userList;
    public Context context;
    public boolean isChat;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    public ChatsUsersAdapter(List<User> userList, boolean isChat){

        this.userList = userList;
        this.isChat = isChat;

    }

    @Override
    public ChatsUsersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.userchats_row, parent, false);
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return new ChatsUsersAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ChatsUsersAdapter.ViewHolder holder, int position) {

        holder.setIsRecyclable(false);

        final User user = userList.get(position);

        String image = user.getImage();
        String name = user.getName();

//        holder.setUserData(name, image);
        holder.setChatUserImage(image);
        holder.setChatUserName(name);

        //start chat
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intentSC = new Intent(context, MessageActivity.class);
                intentSC.putExtra("user_id_msg", user.getUser_id());
                context.startActivity(intentSC);
            }
        });

        if (isChat){
            if (user.getStatus().equals("online")){
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            } else {
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.VISIBLE);
            }
        } else {

            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);
        }



    }


    @Override
    public int getItemCount() {

        if(userList != null) {

            return userList.size();

        } else {

            return 0;

        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;

        private CircleImageView chatUserImage, img_on, img_off;
        private TextView chatUserName;
        private RelativeLayout relativeLayout;


        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            chatUserImage = mView.findViewById(R.id.chat_userimage);
            img_on = mView.findViewById(R.id.img_on);
            img_off = mView.findViewById(R.id.img_off);
            chatUserName = mView.findViewById(R.id.chat_username);
            relativeLayout = mView.findViewById(R.id.rl_userchatrow);
        }
        

        public void setChatUserImage(String image){

            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.profile_placeholder);

            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(image).into(chatUserImage);

        }

        public void setChatUserName(String name){
            
            chatUserName.setText(name);
        }
        

    }

}