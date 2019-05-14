package com.aris.crowdreporting.HelperClasses;

import android.support.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

public class NearPostId {

    @Exclude
    public String BlogPostId;

    public <T extends NearPostId> T withId(@NonNull final String id) {
        this.BlogPostId = id;
        return (T) this;
    }

}