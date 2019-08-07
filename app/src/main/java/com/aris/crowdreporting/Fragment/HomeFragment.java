package com.aris.crowdreporting.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.aris.crowdreporting.Activities.MainActivity;
import com.aris.crowdreporting.Activities.NewPostActivity;
import com.aris.crowdreporting.Adapters.ChatsUsersAdapter;
import com.aris.crowdreporting.HelperClasses.Blog;
import com.aris.crowdreporting.Adapters.BlogRecyclerAdapter;
import com.aris.crowdreporting.HelperUtils.Status;
import com.aris.crowdreporting.R;
import com.aris.crowdreporting.HelperClasses.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import dmax.dialog.SpotsDialog;

import static android.support.constraint.Constraints.TAG;


public class HomeFragment extends Fragment implements SearchView.OnQueryTextListener{

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
        //nampilin tambahan menu di actionbar pada fragment
        setHasOptionsMenu(true);

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


            firstQ();

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

    private void firstQ() {
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

                                if (blogPost.getDesc().contains("bekasi")){
                                    blog_list.add(blogPost);
                                    Collections.sort(blog_list, new Comparator<Blog>() {
                                        @Override
                                        public int compare(Blog o1, Blog o2) {
                                            return o2.getTimestamp().compareTo(o1.getTimestamp());
                                        }
                                    });
                                    blogRecyclerAdapter.notifyItemInserted(blog_list.size());
                                    blogRecyclerAdapter.notifyDataSetChanged();
                                }



                            } else {

                                if (blogPost.getDesc().contains("bekasi")) {
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

                            }
                            dialog.dismiss();
                        }
                    }

                    isFirstPageFirstLoad = false;

                }

            }

        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.action_bar_search, menu);

        MenuItem menuItem = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView)menuItem.getActionView();
        searchView.setOnQueryTextListener(this);

        menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {

                blog_list.clear();
                firstQ();

                return true;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        Query query = firebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING);
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                blog_list.clear();

                for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()){

                    String blogPostId = doc.getDocument().getId();
                    Blog blogPost = doc.getDocument().toObject(Blog.class).withId(blogPostId);

                    if (blogPost.getDesc().toLowerCase().contains(s)){
                        blog_list.add(blogPost);
                    }
                }

                blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list);
                blog_list_view.setAdapter(blogRecyclerAdapter);
            }
        });
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {

        return false;
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