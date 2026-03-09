package com.myprojects.unipiaudiostories.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.myprojects.unipiaudiostories.R;
import com.myprojects.unipiaudiostories.adapters.FavAdapter;
import com.myprojects.unipiaudiostories.interfaces.OnFavClickListener;
import com.myprojects.unipiaudiostories.models.Story;
import com.myprojects.unipiaudiostories.utils.LanguageHelper;
import java.util.ArrayList;
import java.util.List;



public class FavsActivity extends AppCompatActivity implements OnFavClickListener {

    private RecyclerView rvFavs;
    private CardView cardEmptyFavs;
    private FavAdapter adapter;
    private List<Story> favStoriesList;
    private DatabaseReference dbRef;
    private String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //φορτώνω τη γλώσσα πριν το setContentView για να ξεκινήσω σωστά το UI
        LanguageHelper.updateResources(this, LanguageHelper.getSavedLanguage(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favs);

        //τα στοιχεία του χρήστη για να δει μόνο τα προσωπικά του στατιστικά
        userId = FirebaseAuth.getInstance().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference();

        initViews();
        if (userId != null) {
            // αν ο χρήστης βρεθεί στην βάση τότε οκ
            loadFavorites();
        }
    }





    private void initViews() {
        rvFavs = findViewById(R.id.rvFavs);
        cardEmptyFavs = findViewById(R.id.cardEmptyFavs);
        ImageButton btnBack = findViewById(R.id.btnBackFromFavs);
        favStoriesList = new ArrayList<>();
        //Δίνω τη λίστα στον adapter και αναλαμβάνει να στήσει το UI
        adapter = new FavAdapter(favStoriesList, this, LanguageHelper.getSavedLanguage(this));
        rvFavs.setLayoutManager(new LinearLayoutManager(this));
        rvFavs.setAdapter(adapter);
        // back button στην μπάρα της εφαρμογής
        btnBack.setOnClickListener(v -> finish());
    }



    private void loadFavorites() {
        //τα favorites κάθε χρήστη είναι στον κόμβο User μέσα στο πεδίο favorites
        dbRef.child("User").child(userId).child("favorites")
                .addValueEventListener(new ValueEventListener() {
                    //valueEventListener για να υπάρχει πάντα realtime δραστηριότητα
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //προληπτικά θα την καθαρίζω πάντα για να περιέχει 1 φορά την κάθε ιστορία μετά
                        favStoriesList.clear();

                        //αν δεν υπάρχουν αγαπημένα
                        if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                            updateUI(true);
                            return;
                        }

                        // για κάθε ιστορία πρέπει να έχω το storyId της
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String storyId = ds.getKey();
                            fetchStoryDetails(storyId);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }



    //ακολουθεί data normalization δηλαδή με τα storyId πάω τώρα στον κόμβο story για
    // να πάρω τις υπόλοιπες πληροφορίες κάθε ιστορίας όπως πχ author, imageUrl1, title
    private void fetchStoryDetails(String storyId) {
        // Αναζήτηση ιστορίας στον κόμβο "Story" με βάση το storyId
        dbRef.child("Story").child(storyId).addListenerForSingleValueEvent(new ValueEventListener() {
            //listenerForSingleEventValueEvent γιατί οι πληροφορίες μίας ιστορίας αλλάζουν σπάνια
            //και σίγουρα όχι από τον χρήστη
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Story story = snapshot.getValue(Story.class);
                if (story != null) {
                    favStoriesList.add(story);
                    //κρύβεται το μύνημα άδειας λίστας αγαπημένων και εμφανίζεται η λίστα
                    updateUI(false);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }



    //βοηθητική μέθοδος για το UI
    private void updateUI(boolean isEmpty) {
        if (isEmpty) {
            cardEmptyFavs.setVisibility(View.VISIBLE);
            rvFavs.setVisibility(View.GONE);
        } else {
            cardEmptyFavs.setVisibility(View.GONE);
            rvFavs.setVisibility(View.VISIBLE);
        }
    }




    @Override
    public void onStoryClick(Story story) {
        // ανακατεύθυνση στην StoryActivity με την επιλεγμένη ιστορία
        Intent intent = new Intent(this, StoryActivity.class);
        intent.putExtra("STORY_ID", story.getStoryId());
        startActivity(intent);
    }

    @Override
    public void onRemoveClick(Story story) {
        if (userId != null) {
            dbRef.child("User").child(userId).child("favorites")
                    .child(story.getStoryId()).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        //εμφάνιση μηνύματος από τα string resources
                        Toast.makeText(this, R.string.remove_fav, Toast.LENGTH_SHORT).show();
                    });
        }
    }
}