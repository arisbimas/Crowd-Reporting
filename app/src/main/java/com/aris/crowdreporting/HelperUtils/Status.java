package com.aris.crowdreporting.HelperUtils;

import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import static android.support.constraint.Constraints.TAG;

public class Status {
    FirebaseFirestore firebaseFirestore;



    public  Status(String status) {

        firebaseFirestore = FirebaseFirestore.getInstance();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        firebaseFirestore.collection("Users").document(FirebaseAuth.getInstance().getUid()).update(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
//                Toast.makeText(MessageActivity.this, "online", Toast.LENGTH_SHORT).show();
                Log.d("STATUSUSER", "USER ONLINE");
            }
        });

    }
}