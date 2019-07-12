package com.aris.crowdreporting.Fragment;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.aris.crowdreporting.HelperClasses.Blog;
import com.aris.crowdreporting.Adapters.BlogRecyclerAdapter;
import com.aris.crowdreporting.HelperUtils.Status;
import com.aris.crowdreporting.R;
import com.aris.crowdreporting.HelperClasses.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import dmax.dialog.SpotsDialog;

import static android.support.constraint.Constraints.TAG;


public class HomeFragment extends Fragment {

    SwipeRefreshLayout pullToRefresh;

    private RecyclerView blog_list_view;
    private List<Blog> blog_list;
    private List<User> user_list;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private BlogRecyclerAdapter blogRecyclerAdapter;

    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;

    private SpotsDialog dialog;
    private String user_id;

    private Status status;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        blog_list = new ArrayList<>();
        user_list = new ArrayList<>();



        blog_list_view = view.findViewById(R.id.blog_list_view);
        pullToRefresh = view.findViewById(R.id.pull);

        pullToRefresh.setColorSchemeColors(Color.CYAN, Color.YELLOW, Color.MAGENTA);

        firebaseAuth = FirebaseAuth.getInstance();

        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list);
        blog_list_view.setLayoutManager(new LinearLayoutManager(container.getContext()));
        blog_list_view.setAdapter(blogRecyclerAdapter);
        blog_list_view.setHasFixedSize(true);

        dialog = new SpotsDialog(getContext(), "Loading..");
        dialog.show();

        if(firebaseAuth.getCurrentUser() != null) {

            firebaseFirestore = FirebaseFirestore.getInstance();
            user_id = firebaseAuth.getCurrentUser().getUid();

            blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                    if(reachedBottom){

//                        loadMorePost();

                    }

                }
            });

            String s = "lapor";

            CollectionReference collectionReference = firebaseFirestore.collection("Posts");

            Query firstQuery = collectionReference.whereEqualTo("reports", "false");
            firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (e != null) {
                        Log.w(TAG, "listen:error", e);
                        return;
                    }

                    if (!documentSnapshots.isEmpty()) {

                        if (isFirstPageFirstLoad) {

                            lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                            blog_list.clear();

                        }

                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                            if (doc.getType() == DocumentChange.Type.ADDED) {


                                String blogPostId = doc.getDocument().getId();
                                Blog blogPost = doc.getDocument().toObject(Blog.class).withId(blogPostId);

                                if (isFirstPageFirstLoad) {

                                    blog_list.add(blogPost);
                                    Collections.sort(blog_list, new Comparator<Blog>() {
                                        @Override
                                        public int compare(Blog o1, Blog o2) {
                                            return o2.getTimestamp().compareTo(o1.getTimestamp());
                                        }
                                    });
                                    blogRecyclerAdapter.notifyItemInserted(blog_list.size());
                                    blogRecyclerAdapter.notifyDataSetChanged();


                                } else {

                                    blog_list.add(0, blogPost);
                                    Collections.sort(blog_list, new Comparator<Blog>() {
                                        @Override
                                        public int compare(Blog o1, Blog o2) {
                                            return o2.getTimestamp().compareTo(o1.getTimestamp());
                                        }
                                    });
                                    blogRecyclerAdapter.notifyItemInserted(0);
                                    blogRecyclerAdapter.notifyDataSetChanged();

                                }
                                dialog.dismiss();


                            }
                        }

                        isFirstPageFirstLoad = false;

                    }

                }

            });

            pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    blogRecyclerAdapter.notifyDataSetChanged();
                    pullToRefresh.setRefreshing(false);
                }
            });

        }

        // Inflate the layout for this fragment
        return view;
    }

//    public void loadMorePost(){
//
//        if(firebaseAuth.getCurrentUser() != null) {
//
//            Query nextQuery = firebaseFirestore.collection("Posts")
//                    .whereGreaterThanOrEqualTo("desc", "bekasi")
//                    .limit(3);
//
//            nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
//                @Override
//                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
//
//                    if (e != null) {
//                        Log.w(TAG, "listen:error", e);
//                        return;
//                    }
//
//                    if (!documentSnapshots.isEmpty()) {
//
//                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
//                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
//
//                            if (doc.getType() == DocumentChange.Type.ADDED) {
//
//                                String blogPostId = doc.getDocument().getId();
//                                Blog blogPost = doc.getDocument().toObject(Blog.class).withId(blogPostId);
//                                blog_list.add(blogPost);
//
//                                blogRecyclerAdapter.notifyItemInserted(blog_list.size());
//                                blogRecyclerAdapter.notifyDataSetChanged();
//                            }
//
//                        }
//                    }
//
//                }
//            });
//
//        }
//
//    }


    @Override
    public void onResume() {
        super.onResume();
        status = new Status("online");
    }

    @Override
    public void onPause() {
        super.onPause();
        status = new Status("offline");
    }

    @Override
    public void onStop() {
        super.onStop();
        status = new Status("offline");
    }

}