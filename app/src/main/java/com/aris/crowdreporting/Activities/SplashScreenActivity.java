package com.aris.crowdreporting.Activities;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.aris.crowdreporting.BuildConfig;
import com.aris.crowdreporting.R;

import gr.net.maroulis.library.EasySplashScreen;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String versionName = BuildConfig.VERSION_NAME;


        EasySplashScreen easySplashScreen = new EasySplashScreen(SplashScreenActivity.this)
                .withFullScreen()
                .withTargetActivity(MainActivity.class)
                .withSplashTimeOut(3000)
                .withBackgroundResource(R.drawable.gradient3)
                .withLogo(R.drawable.logoutama)
                .withFooterText("Version "+versionName)
                .withAfterLogoText("By: Aris Bimas Nugroho");

        //add custom font
        Typeface typeface = ResourcesCompat.getFont(this, R.font.comfortaa);
        easySplashScreen.getAfterLogoTextView().setTypeface(typeface);
        easySplashScreen.getFooterTextView().setTypeface(typeface);

        easySplashScreen.getAfterLogoTextView().setTextColor(Color.BLACK);
        easySplashScreen.getFooterTextView().setTextColor(Color.WHITE);
        easySplashScreen.getLogo().setMaxHeight(550);
        easySplashScreen.getLogo().setMaxWidth(550);
        View view = easySplashScreen.create();
        setContentView(view);

    }
}
