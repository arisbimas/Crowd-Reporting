package com.aris.crowdreporting.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.aris.crowdreporting.Activities.DetailActivity;
import com.aris.crowdreporting.Activities.EditPostActivity;
import com.aris.crowdreporting.HelperClasses.Blog;
import com.aris.crowdreporting.HelperUtils.TimeAgo;
import com.aris.crowdreporting.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import dmax.dialog.SpotsDialog;

public class MyPhotoRecyclerAdapter extends RecyclerView.Adapter<MyPhotoRecyclerAdapter.ProfileBlogViewHolder> {

    private Context context;
    private List<Blog> blog_list;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    public MyPhotoRecyclerAdapter(Context context, List<Blog> blog_list) {
        this.context = context;
        this.blog_list = blog_list;
    }


    @NonNull
    @Override
    public MyPhotoRecyclerAdapter.ProfileBlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.myphoto_row, parent, false);
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return new ProfileBlogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyPhotoRecyclerAdapter.ProfileBlogViewHolder holder, int position) {

        final Blog blogPost = blog_list.get(holder.getAdapterPosition());
        String title = blogPost.getTitle();
        String image = blogPost.getImage_uri();
        String thumb = blogPost.getImage_thumb();
        String desc_data = blogPost.getDesc();

        final String blogPostId = blog_list.get(position).BlogPostId;

        long millisecond = blog_list.get(position).getTimestamp().getTime();
        String dateString = DateFormat.format("MM/dd/yyyy", new Date(millisecond)).toString();

        String timeAgo = TimeAgo.getTimeAgo(millisecond);

//        GlideLoadImage.loadImage(context,holder.blogImage,thumb,image);
        RequestOptions placeholderOption = new RequestOptions();
        placeholderOption.placeholder(R.drawable.rectangle);

        Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(thumb).into(holder.blogImage);

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra("blog_id", blogPost.BlogPostId);
                intent.putExtra("user_id", blogPost.getUser_id());
                intent.putExtra("imurl", blogPost.getImage_uri());
                intent.putExtra("desc", desc_data);
                intent.putExtra("time_post", timeAgo);

                context.startActivity(intent);
            }
        });


        //longpress
        holder.view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                final String currentUserId = firebaseAuth.getCurrentUser().getUid();
                String user_id = blog_list.get(position).getUser_id();

                if (user_id.equals(currentUserId)) {

                    LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View sheetView = li.inflate(R.layout.fragment_bottom_sheet_account, null);

                    holder.mBottomSheetDialog = new BottomSheetDialog(context);
                    holder.mBottomSheetDialog.setContentView(sheetView);
                    holder.mBottomSheetDialog.show();

                    holder.edit = sheetView.findViewById(R.id.bottom_sheet_edit_post);
                    holder.hapus = sheetView.findViewById(R.id.bottom_sheet_delete_post);

                    holder.edit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, EditPostActivity.class);
                            intent.putExtra("blog_id", blogPost.BlogPostId);
                            intent.putExtra("user_id", blogPost.getUser_id());
                            context.startActivity(intent);
                            Toast.makeText(context, "edit " + blogPost.BlogPostId, Toast.LENGTH_SHORT).show();
                            holder.mBottomSheetDialog.dismiss();

                        }
                    });

                    holder.hapus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setMessage("Are you sure to DELETE this post ?")
                                    .setCancelable(false)
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            holder.deletePost(blogPost.BlogPostId, position);
                                            holder.mBottomSheetDialog.dismiss();

                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });

                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();

                            holder.mBottomSheetDialog.dismiss();
                        }
                    });

                } else {

                    LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View sheetView = li.inflate(R.layout.fragment_bottom_sheet_lapor, null);

                    holder.mBottomSheetDialog = new BottomSheetDialog(context);
                    holder.mBottomSheetDialog.setContentView(sheetView);
                    holder.mBottomSheetDialog.show();

                    holder.report = sheetView.findViewById(R.id.bottom_sheet_report);
                    holder.report.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            //Lapor Feature
                            holder.reportPost(blogPostId, currentUserId);

                        }
                    });

                }

                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return blog_list.size();
    }

    public class ProfileBlogViewHolder extends RecyclerView.ViewHolder {

        private ImageView blogImage;
        private View view;
        private LinearLayout edit, hapus, report;
        private BottomSheetDialog mBottomSheetDialog;
        private SpotsDialog spotsDialog;

        public ProfileBlogViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            blogImage = itemView.findViewById(R.id.blog_image_mini);
        }

        public void deletePost(String id, int index) {
            firebaseFirestore.collection("Posts").document(id)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context, "Successfully deleted post!", Toast.LENGTH_SHORT).show();
                            blog_list.remove(index);
                            notifyItemRemoved(index);

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Error deleting post.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        public void reportPost(String blogPostId, String currentUserId) {

            spotsDialog = new SpotsDialog(context, "Please Wait.");
            spotsDialog.show();

            CollectionReference collectionReference = firebaseFirestore.collection("Posts");
            collectionReference.document(blogPostId + "/PostHoax/" + currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.getResult().exists()){

                        spotsDialog.dismiss();
                        Toast.makeText(context, "You has been reported this post!", Toast.LENGTH_SHORT).show();
                        mBottomSheetDialog.dismiss();

                    } else {
                        Map<String, Object> reportsMap = new HashMap<>();
                        reportsMap.put("reports", "true");

                        collectionReference.document(blogPostId).collection("PostHoax").document(currentUserId)
                                .set(reportsMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                spotsDialog.dismiss();
                                Toast.makeText(context, "Thanks for reported this post..", Toast.LENGTH_SHORT).show();

                                Query query = collectionReference.document(blogPostId).collection("PostHoax");
                                query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                        if (queryDocumentSnapshots.size() > 3){
                                            firebaseFirestore.collection("Posts").document(blogPostId).set(reportsMap, SetOptions.merge());
                                        }
                                    }
                                });

                                mBottomSheetDialog.dismiss();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                spotsDialog.dismiss();
                                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                mBottomSheetDialog.dismiss();

                            }
                        });
                    }
                }
            });
        }
    }
}
