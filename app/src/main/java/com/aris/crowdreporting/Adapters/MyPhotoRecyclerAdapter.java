package com.aris.crowdreporting.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.aris.crowdreporting.Activities.DetailActivity;
import com.aris.crowdreporting.HelperClasses.Blog;
import com.aris.crowdreporting.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

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
        String image = blogPost.getImage_uri();
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
                intent.putExtra("imurl",blogPost.getImage_uri());

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
