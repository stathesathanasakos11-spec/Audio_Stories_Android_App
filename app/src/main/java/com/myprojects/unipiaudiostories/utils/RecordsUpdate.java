package com.myprojects.unipiaudiostories.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import android.util.Log;
import android.widget.Toast;
import com.myprojects.unipiaudiostories.activities.StoryActivity;
import com.myprojects.unipiaudiostories.models.Story;
import com.myprojects.unipiaudiostories.models.Record;


public class RecordsUpdate {
    // για τα ΣΤΑΤΙΣΤΙΚΑ χρησιμοποιώ runTransaction() και όχι setValue()
    // για πολλαπλές ταυτόχρονες εγγραφές στη ΒΔ όταν πχ 2 χρήστες ολοκληρώνουν μία ιστορία ταυτόχρονα

    public static void updateStatistics(String userId, Story story) {
        if (userId == null || story == null) return;

        DatabaseReference statsRef = FirebaseDatabase.getInstance()
                .getReference("Statistics")
                .child(userId)
                .child(story.getStoryId());

        statsRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer count = mutableData.child("playCount").getValue(Integer.class);
                if (count == null) {
                    mutableData.child("playCount").setValue(1);
                    mutableData.child("title").setValue(story.getTitle());
                } else {
                    mutableData.child("playCount").setValue(count + 1);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean b, DataSnapshot snap) {
                if (error != null) Log.e("RECORDS_UPDATE", "User Stats Error: " + error.getMessage());
            }
        });
    }





    // η ενημέρωση του globalStatistics node γίνεται πάντα ανεξαρτήτως χρήστη
    public static void updateGlobalStatistics(Story story) {
        if (story == null) return;

        DatabaseReference globalRef = FirebaseDatabase.getInstance()
                .getReference("GlobalStatistics")
                .child(story.getStoryId());

        globalRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer count = mutableData.child("playCount").getValue(Integer.class);
                if (count == null) {
                    mutableData.child("playCount").setValue(1);
                    mutableData.child("title").setValue(story.getTitle());
                } else {
                    mutableData.child("playCount").setValue(count + 1);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean b, DataSnapshot snap) {
                if (error != null) Log.e("RECORDS_UPDATE", "Global Stats Error: " + error.getMessage());
            }
        });
    }
}
