package com.example.icasapp.com.example.icasapp.ObjectClasses;

import android.support.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

public class DiscussionPostid {


        @Exclude
        public String DiscussionPostid;

        public <T extends DiscussionPostid> T withId(@NonNull final String id) {
            this.DiscussionPostid = id;
            return (T) this;
        }

    }
