package com.myprojects.unipiaudiostories.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StoryDataManager {
    private DatabaseReference dbRef;
    private String userId;

    // ο ρόλος της κλάσης είναι η επικοινωνία firebase & εφαρμογής
    public StoryDataManager() {
        dbRef = FirebaseDatabase.getInstance().getReference();
        // κάθε χρήστης έχει μοναδικό UID για να ξέρει η εφαρμογή τι δεδομένα θα αναπαράγει
        userId = FirebaseAuth.getInstance().getUid();
    }



    public DatabaseReference getStoryReference(String storyId) {
        // στον κόμβο στόρυ βρίσκω μια συγκεκριμένη ιστορία με βάση το storyId πχ s3
        return dbRef.child("Story").child(storyId);
    }



    public void checkIsFavorite(String storyId, ValueEventListener listener) {
        // η εφαρμογή απαιτεί login οπότε αν δεν έχω το UID δεν δείχνω τίποτα
        if (userId == null) return;
        // στον κόμβο User για κάθε χρήστη υπάρχει το πεδίο favorites
        //στο οποίο μπαίνουν ιστορίες με το storyId τους
        dbRef.child("User").child(userId).child("favorites").child(storyId)
                .addListenerForSingleValueEvent(listener);
    }



    public void toggleFavorite(String storyId, boolean isFavorite) {
        if (userId == null) return;
        DatabaseReference favRef = dbRef.child("User").child(userId).child("favorites").child(storyId);
        if (isFavorite) {
            favRef.setValue(true);
        } else {
            //διαγραφή της ιστορίας από τον κόμβο
            favRef.removeValue();
        }
    }
}
