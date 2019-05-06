package com.aris.crowdreporting;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.support.constraint.Constraints.TAG;


public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    public List<Blog> blog_list;

    public Context context;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    public BlogRecyclerAdapter(List<Blog> blog_list){

        this.blog_list = blog_list;


    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_row, parent, false);
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.setIsRecyclable(false);

        final String blogPostId = blog_list.get(position).BlogPostId;
        final String currentUserId = firebaseAuth.getCurrentUser().getUid();

        String desc_data = blog_list.get(position).getDesc();
        holder.setDescText(desc_data);

        String image_url = blog_list.get(position).getImage_url();
        String thumbUri = blog_list.get(position).getImage_thumb();
        holder.setBlogImage(image_url, thumbUri);

        String user_id = blog_list.get(position).getUser_id();

//        if (user_id.equals(currentUserId)){
//            holder.deleteBtn.setEnabled(true);
//            holder.deleteBtn.setVisibility(View.VISIBLE);
//        }

        //User Data will be retrieved here...
        firebaseFirestore.collection("Users").document(user_id)
                .addSnapshotListener((Activity) context,new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "listen:error", e);
                            return;
                        }
                        if (documentSnapshot.exists()) {

                            String userImageUrl = documentSnapshot.get("image").toString();
                            String userNameData = documentSnapshot.get("name").toString();
                            if (userNameData.isEmpty()) {
                                userNameData = "No name";
                            }
                            holder.blogUserName.setText(userNameData);
//                            GlideLoadImage.loadSmallImage(context,holder.blogUserImage,userImageUrl,userImageUrl);
                            RequestOptions placeholderOption = new RequestOptions();
                            placeholderOption.placeholder(R.drawable.profile_placeholder);

                            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(userImageUrl).into(holder.blogUserImage);

                        } else {
                            Log.w("USER_DETAIL", "Empty DOC");
                        }
                    }
                });




        long millisecond = blog_list.get(position).getTimestamp().getTime();
            String dateString = DateFormat.format("MM/dd/yyyy", new Date(millisecond)).toString();
            holder.setTime(dateString);


        //Get Likes Count
        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").addSnapshotListener((Activity) context, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "listen:error", e);
                    return;
                }

                if(!documentSnapshots.isEmpty()){

                    int count = documentSnapshots.size();

                    holder.updateLikesCount(count);

                } else {

                    holder.updateLikesCount(0);

                }

            }
        });


        //Get Likes
        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).addSnapshotListener((Activity) context, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if(documentSnapshot.exists()){

                    holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.dilike));

                } else {

                    holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.like_gray));

                }

            }
        });

        //Likes Feature
        holder.blogLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if(!task.getResult().exists()){

                            Map<String, Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());

                            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).set(likesMap);

                        } else {

                            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).delete();
                            //coba
                        }

                    }
                });
            }
        });

        holder.blogCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent commentIntent = new Intent(context, CommentsActivity.class);
                commentIntent.putExtra("blog_post_id", blogPostId);
                context.startActivity(commentIntent);

            }
        });

        //Get Comments Count
        firebaseFirestore.collection("Posts/" + blogPostId + "/Comments").addSnapshotListener((Activity) context, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if(!documentSnapshots.isEmpty()){

                    int count = documentSnapshots.size();

                    holder.updateCommentsCount(count);

                } else {

                    holder.updateCommentsCount(0);

                }

            }
        });

//        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                firebaseFirestore.collection("Posts")
//                        .document(blogPostId)
//                        .delete()
//                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//
//                            blog_list.remove(position);
//
//                        }
//                    });
//
//            }
//        });


        //POPUP
        holder.popUpHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popupMenu =  new PopupMenu(context, holder.popUpHome, Gravity.CENTER, 0, R.style.PopupMenuMoreCentralized);
                popupMenu.setGravity(Gravity.CENTER);

                popupMenu.getMenuInflater().inflate(R.menu.popup_menu_home, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.report:

                                //Lapor Feature
                                firebaseFirestore.collection("Posts").document(blogPostId + "reports").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (!task.getResult().exists()){
                                            Map<String, Object> reportsMap = new HashMap<>();
                                            reportsMap.put("desc", "");
                                            reportsMap.put("reports", "true");

                                            firebaseFirestore.collection("Posts").document(blogPostId).set(reportsMap, SetOptions.merge());
                                            Toast.makeText(context, "Thanks for repoting this post..", Toast.LENGTH_SHORT).show();
                                            holder.hideItem.setVisibility(View.VISIBLE);
                                            
                                        } else {
                                            Toast.makeText(context, "Anda telah melaporkan post ini!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.show();

            }
        });

        holder.blogImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra("blog_id",blogPostId);
                intent.putExtra("user_id",user_id);
                intent.putExtra("imurl",image_url);

                context.startActivity(intent);
            }
        });

        holder.blogUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.showDataPopUpProfile(blog_list.get(position).getUser_id());
                holder.mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                holder.mDialog.show();
            }
        });

    }


    @Override
    public int getItemCount() {
        return blog_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;

        private TextView descView;
        private ImageView blogImageView;
        private CircularImageView popUpUserImage;
        private TextView blogDate;

        private TextView blogUserName, popUpUserName, popUpUserEmail, popUpUserPhone;
        private CircularImageView blogUserImage;

        private ImageView blogLikeBtn, popUpHome;
        private TextView blogLikeCount, hideItem;

        private ImageView blogCommentBtn;

        private Button deleteBtn;

        private Dialog mDialog;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            hideItem = mView.findViewById(R.id.hideitem);
            blogLikeBtn = mView.findViewById(R.id.btn_like);
            blogCommentBtn = mView.findViewById(R.id.btn_comment);
//            deleteBtn = mView.findViewById(R.id.btn_delete);
            popUpHome = mView.findViewById(R.id.popup_home);
            blogUserName = mView.findViewById(R.id.blog_username);
            blogUserImage = mView.findViewById(R.id.blog_user_image);

            mDialog = new Dialog(context);
            mDialog.setContentView(R.layout.popup_detail_profile);

        }

        public void setDescText(String descText){

            descView = mView.findViewById(R.id.blog_desc);
            descView.setText(descText);

        }

        public void setBlogImage(String downloadUri, String thumbUri){

            blogImageView = mView.findViewById(R.id.blog_image);

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.image_placeholder);

            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(downloadUri).thumbnail(
                    Glide.with(context).load(thumbUri)
            ).into(blogImageView);

        }

        public void setTime(String date) {

            blogDate = mView.findViewById(R.id.blog_date);
            blogDate.setText(date);

        }

//        public void setUserData(String name, String image){
//
//            blogUserImage = mView.findViewById(R.id.blog_user_image);
//            blogUserName = mView.findViewById(R.id.blog_username);
//
//            blogUserName.setText(name);
//
//            RequestOptions placeholderOption = new RequestOptions();
//            placeholderOption.placeholder(R.drawable.profile_placeholder);
//
//            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(image).into(blogUserImage);
//
//        }

        public void updateLikesCount(int count){

            blogLikeCount = mView.findViewById(R.id.like_count);
            blogLikeCount.setText(count +"");

        }

        public void updateCommentsCount(int count){

            blogLikeCount = mView.findViewById(R.id.comment_count);
            blogLikeCount.setText(count +"");

        }

        public void showDataPopUpProfile(String popupName){
            popUpUserName = mDialog.findViewById(R.id.popup_username);
            popUpUserEmail = mDialog.findViewById(R.id.popup_user_email);
            popUpUserPhone = mDialog.findViewById(R.id.popup_user_phone);
            popUpUserImage = mDialog.findViewById(R.id.popup_user_image);

            firebaseFirestore.collection("Users").document(popupName).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshot.exists()){
                        String popupusername = documentSnapshot.get("name").toString();
                        String popupuseremail = documentSnapshot.get("email").toString();
                        String popupuserphone = documentSnapshot.get("phone").toString();
                        String popupuserimg = documentSnapshot.get("image").toString();

                        popUpUserName.setText(popupusername);
                        popUpUserEmail.setText(popupuseremail);
                        popUpUserPhone.setText(popupuserphone);
                        Glide.with(context).load(popupuserimg).into(popUpUserImage);
                    }
                }
            });
        }
    }

}