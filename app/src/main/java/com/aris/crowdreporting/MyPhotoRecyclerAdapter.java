package com.aris.crowdreporting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.support.constraint.Constraints.TAG;

public class MyPhotoRecyclerAdapter extends RecyclerView.Adapter<MyPhotoRecyclerAdapter.ProfileBlogViewHolder> {

    private Context context;
    private List<Blog> blog_list;

    public MyPhotoRecyclerAdapter(Context context, List<Blog> blog_list){
        this.context = context;
        this.blog_list = blog_list;
    }


    @NonNull
    @Override
    public MyPhotoRecyclerAdapter.ProfileBlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.myphoto_row,parent,false);
        return new ProfileBlogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyPhotoRecyclerAdapter.ProfileBlogViewHolder holder, int position) {

        final Blog blogPost = blog_list.get(holder.getAdapterPosition());

        String title = blogPost.getTitle();
        String image = blogPost.getImage_url();
        String thumb = blogPost.getImage_thumb();

//        GlideLoadImage.loadImage(context,holder.blogImage,thumb,image);
        RequestOptions placeholderOption = new RequestOptions();
        placeholderOption.placeholder(R.drawable.rectangle);

        Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(thumb).into(holder.blogImage);

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra("blog_id",blogPost.BlogPostId);
                intent.putExtra("user_id",blogPost.getUser_id());
                intent.putExtra("imurl",blogPost.getImage_url());

                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return blog_list.size();
    }

    public class ProfileBlogViewHolder extends RecyclerView.ViewHolder{

        private ImageView blogImage;
        private View view;

        public ProfileBlogViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            blogImage = itemView.findViewById(R.id.blog_image_mini);
        }
    }
}
