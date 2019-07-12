package com.aris.crowdreporting.Fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.aris.crowdreporting.Adapters.BlogRecyclerAdapter;
import com.aris.crowdreporting.Adapters.ChatsUsersAdapter;
import com.aris.crowdreporting.HelperClasses.Blog;
import com.aris.crowdreporting.HelperClasses.User;
import com.aris.crowdreporting.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
import java.util.List;

import javax.annotation.Nullable;

import dmax.dialog.SpotsDialog;

import static android.support.constraint.Constraints.TAG;


public class VPUsersChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout pullToRefresh;

    private ChatsUsersAdapter chatsUsersAdapter;
    private List<User> userList;

    private EditText txtSearchUsers;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;
    private String user_id;
    private SpotsDialog dialog;

    public VPUsersChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_vpusers_chat, container, false);


        userList = new ArrayList<>();

        pullToRefresh = view.findViewById(R.id.pull1);

        pullToRefresh.setColorSchemeColors(Color.CYAN, Color.YELLOW, Color.MAGENTA);
        recyclerView = view.findViewById(R.id.rv_chatsusers);
        txtSearchUsers = view.findViewById(R.id.src_users);

        firebaseAuth = FirebaseAuth.getInstance();

        chatsUsersAdapter = new ChatsUsersAdapter(userList, true);
        recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        recyclerView.setAdapter(chatsUsersAdapter);
        recyclerView.setHasFixedSize(true);

        dialog = new SpotsDialog(getContext(), "Loading..");
        dialog.show();

        if (firebaseAuth.getCurrentUser() != null) {

            firebaseFirestore = FirebaseFirestore.getInstance();
            user_id = firebaseAuth.getCurrentUser().getUid();

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                    if (reachedBottom) {

//                        loadMorePost();

                    }

                }
            });

            Query firstQuery = firebaseFirestore.collection("Users").orderBy("name");
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
                            userList.clear();

                        }


                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                            if (doc.getType() == DocumentChange.Type.ADDED) {


                                String blogPostId = doc.getDocument().getId();
                                User userPost = doc.getDocument().toObject(User.class);

                                if (isFirstPageFirstLoad) {

                                    if (!userPost.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                        userList.add(userPost);
                                    }

                                    chatsUsersAdapter.notifyItemInserted(userList.size());
                                    chatsUsersAdapter.notifyDataSetChanged();

                                } else {


                                    if (!userPost.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                        userList.add(0, userPost);
                                    }

                                    chatsUsersAdapter.notifyItemInserted(0);
                                    chatsUsersAdapter.notifyDataSetChanged();

                                }
                                dialog.dismiss();


                            }
                        }

                        isFirstPageFirstLoad = false;

                    }

                }

            });

        }

        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                chatsUsersAdapter.notifyDataSetChanged();
                pullToRefresh.setRefreshing(false);
            }
        });

//        txtSearchUsers.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//                searchUsers(s.toString());
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });

        // Inflate the layout for this fragment
        return view;
    }

    private void searchUsers(String strng) {

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        Query query = firebaseFirestore.collection("Users").orderBy("name");

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                userList.clear();

                for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()){
                    User user = snapshot.toObject(User.class);

                    if (!user.getUser_id().equals(firebaseUser.getUid())){
                        userList.add(user);
                    }
                }

                chatsUsersAdapter = new ChatsUsersAdapter(userList, false);
                recyclerView.setAdapter(chatsUsersAdapter);
            }
        });

    }

    public void loadMorePost() {

        if (firebaseAuth.getCurrentUser() != null) {

            Query nextQuery = firebaseFirestore.collection("Users")
                    .startAfter(lastVisible)
                    .limit(2);

            nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (!documentSnapshots.isEmpty()) {

                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String blogPostId = doc.getDocument().getId();
                                User userPost = doc.getDocument().toObject(User.class);
                                if (!userPost.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                    userList.add( userPost);
                                }
                                chatsUsersAdapter.notifyItemInserted(userList.size());


                            }

                        }
                    }

                }
            });

        }

    }

}
