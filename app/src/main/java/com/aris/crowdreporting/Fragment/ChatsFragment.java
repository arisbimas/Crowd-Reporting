package com.aris.crowdreporting.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.aris.crowdreporting.Activities.VerificationEmailActivity;
import com.aris.crowdreporting.HelperUtils.Status;
import com.aris.crowdreporting.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatsFragment extends DialogFragment {

    private final static String TAG = "CHATS_ACTIVITY";

    private EditText editTextTest;
    private Button btnTest;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FragmentManager fragmentManager;

    private FirebaseFirestore firebaseFirestore;

    private Status status;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_chats, container, false);


        tabLayout = view.findViewById(R.id.tablay);
        viewPager = view.findViewById(R.id.view_pager);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        viewPagerAdapter.addFragment(new VPChatsFragment(), "Chats");
        viewPagerAdapter.addFragment(new VPUsersChatFragment(), "Users");

        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        return view;
    }

    class ViewPagerAdapter extends FragmentPagerAdapter{

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        @Override
        public Fragment getItem(int i) {
            return fragments.get(i);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment, String title){
            fragments.add(fragment);
            titles.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }


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