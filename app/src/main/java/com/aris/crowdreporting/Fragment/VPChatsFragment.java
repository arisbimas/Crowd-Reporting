package com.aris.crowdreporting.Fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aris.crowdreporting.Adapters.ChatsUsersAdapter;

import com.aris.crowdreporting.HelperClasses.Chatlist;
import com.aris.crowdreporting.HelperClasses.Chats;
import com.aris.crowdreporting.HelperClasses.Comments;
import com.aris.crowdreporting.HelperClasses.User;
import com.aris.crowdreporting.Notifications.Token;
import com.aris.crowdreporting.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import static android.support.constraint.Constraints.TAG;

public class VPChatsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ChatsUsersAdapter chatsUsersAdapter;

    private List<User> mUsers;
    private List<Chatlist> userList;

    FirebaseUser firebaseUser;
    FirebaseFirestore firebaseFirestore;



    public VPChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_vpchats, container, false);

        recyclerView = view.findViewById(R.id.rv_vpchat);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        userList = new ArrayList<>();

        Query query = firebaseFirestore.collection("Chatlist/" + firebaseUser.getUid() + "/" +firebaseUser.getUid());
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

//                userList.clear();
                for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                    Chatlist chatlist = doc.getDocument().toObject(Chatlist.class);
                    userList.add(chatlist);

                }
                chatList();

            }
        });

        updateToken(FirebaseInstanceId.getInstance().getToken());



        return view;
    }

    private void updateToken(String token){
        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("Tokens").document(firebaseUser.getUid());

        Token token1 = new Token(token);
        documentReference.set(token1);
    }

    private void chatList() {

        mUsers = new ArrayList<>();

        Query query1 = firebaseFirestore.collection("Users");
        query1.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                mUsers.clear();
                for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {

                    User user = doc.toObject(User.class);
                    for (Chatlist chatlist : userList){
                        if (user.getUser_id().equals(chatlist.getId())){
                            mUsers.add(user);
                        }
                    }
                }
                chatsUsersAdapter = new ChatsUsersAdapter(mUsers, true);
                recyclerView.setAdapter(chatsUsersAdapter);

            }
        });

    }


//    private void chatlist() {
//        mUsers = new ArrayList<>();
//
//        firebaseFirestore.collection("Users").addSnapshotListener(new EventListener<QuerySnapshot>() {
//            @Override
//            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
//                for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()){
//                    User user = snapshot.toObject(User.class);
//
//                    for (Chatlist chatlist : userList){
//                        mUsers.add(user);
//                    }
//                }
//
//                chatsUsersAdapter = new ChatsUsersAdapter(mUsers, true);
//                recyclerView.setAdapter(chatsUsersAdapter);
//            }
//        });
//    }


}

