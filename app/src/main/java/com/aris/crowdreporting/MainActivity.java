package com.aris.crowdreporting;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.aris.crowdreporting.Fragment.AccountFragment;
import com.aris.crowdreporting.Fragment.HomeFragment;
import com.aris.crowdreporting.Fragment.NearFragment;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    private Toolbar main_toolbar;

    private String current_user_id;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        main_toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(main_toolbar);
        getSupportActionBar().setTitle("Crowd Reporting");

        Firebase.setAndroidContext(this);
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null){
            // kita set default nya Home Fragment
            loadFragment(new NearFragment());
            // inisialisasi BottomNavigaionView
            BottomNavigationView bottomNavigationView = findViewById(R.id.button_navigation);
            // beri listener pada saat item/menu bottomnavigation terpilih
            bottomNavigationView.setOnNavigationItemSelectedListener(this);

//            BottomNavigationViewEx bottomNavigationView = (BottomNavigationViewEx) findViewById(R.id.bottomnav);

            // beri listener pada saat item/menu bottomnavigation terpilih
//            bottomNavigationView.setOnNavigationItemSelectedListener(this);
//            bottomNavigationView.enableAnimation(true);
//            bottomNavigationView.setTextSize(10);
//            bottomNavigationView.setItemHeight(100);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null){

            sendToLogin();

        } else {

            current_user_id = mAuth.getCurrentUser().getUid();
            String usr = "Users";
            firebaseFirestore.collection(usr).document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()){

                        if (!task.getResult().exists()){

                            Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                            startActivity(setupIntent);
                            finish();

                        }

                    } else {

                        String errMsg = task.getException().getMessage();
                        Toast.makeText(MainActivity.this, ""+ errMsg, Toast.LENGTH_SHORT).show();

                    }
                }

            });

        }
    }

    private void sendToLogin() {

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();

    }

    private boolean loadFragment(Fragment fragment){
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.act_add) {
            Intent intent = new Intent(MainActivity.this, NewPostActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Fragment fragment = null;
        switch (menuItem.getItemId()){
            case R.id.nav_home:
                fragment = new HomeFragment();
                break;
            case R.id.nav_near:
                fragment = new NearFragment();
                break;
            case R.id.nav_account:
                fragment = new AccountFragment();
                break;
        }
        return loadFragment(fragment);
    }


}
