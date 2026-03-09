package com.myprojects.unipiaudiostories.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioButton;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.myprojects.unipiaudiostories.models.Story;
import com.myprojects.unipiaudiostories.utils.LanguageHelper;
import com.myprojects.unipiaudiostories.R;
import com.myprojects.unipiaudiostories.models.Record;
import com.myprojects.unipiaudiostories.adapters.RecordAdapter;

import java.util.ArrayList;
import java.util.List;


public class RecordsActivity extends AppCompatActivity {
    private RecyclerView rvRecords;
    private TextView tvSuggestion, tvBestStoryTitle, tvGlobalBestStory, tvGlobalSuggestion;
    private RecordAdapter adapter;
    private List<Record> recordList;
    private DatabaseReference dbRef;
    private String userId;
    private String lang; // Η γλώσσα της εφαρμογής

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageHelper.updateResources(this, LanguageHelper.getSavedLanguage(this));
        super.onCreate(savedInstanceState);
        setContentView(com.myprojects.unipiaudiostories.R.layout.activity_records);

        // Ανάκτηση της γλώσσας από τα SharedPreferences
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        //lang = prefs.getString("My_Lang", "en"); // default τα αγγλικά
        lang = getSharedPreferences("Settings", MODE_PRIVATE).getString("My_Lang", "en");
        lang = LanguageHelper.getSavedLanguage(this);

        //για κάθε χρήστη τα στατιστικά που βλέπει είναι μόνο τα δικά του
        userId = FirebaseAuth.getInstance().getUid();
        initViews();

        if (userId != null) {
            loadUserStatistics();
            loadGlobalStatistics();
        }
    }

    private void initViews() {
        rvRecords = findViewById(com.myprojects.unipiaudiostories.R.id.rvRecords);
        tvSuggestion = findViewById(com.myprojects.unipiaudiostories.R.id.tvSuggestion);
        tvBestStoryTitle = findViewById(com.myprojects.unipiaudiostories.R.id.tvBestStoryTitle);
        tvGlobalBestStory = findViewById(com.myprojects.unipiaudiostories.R.id.tvGlobalBestStory);
        tvGlobalSuggestion = findViewById(com.myprojects.unipiaudiostories.R.id.tvGlobalSuggestion);
        ImageButton btnBack = findViewById(com.myprojects.unipiaudiostories.R.id.btnBackFromRecords);

        recordList = new ArrayList<>();
        adapter = new RecordAdapter(recordList);
        rvRecords.setLayoutManager(new LinearLayoutManager(this));
        rvRecords.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadUserStatistics() {
        dbRef = FirebaseDatabase.getInstance().getReference("Statistics").child(userId);

        //valueEventListener για realtime αλλαγές στην οθόνη των στατιστικών
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recordList.clear();
                final String[] topStoryTitle = {""};
                final String[] topStoryId = {""};
                final long[] maxPlays = {0};

                // Χρειαζόμαστε έναν μετρητή για να ξέρουμε πότε τελείωσαν όλα τα εσωτερικά queries
                long totalItems = snapshot.getChildrenCount();
                if (totalItems == 0) {
                    updateTopStatsUI("", "");
                    return;
                }

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String storyId = ds.getKey();
                    Long plays = ds.child("playCount").getValue(Long.class);

                    // Cross-reference με τον κόμβο Story για να πάρουμε τον μεταφρασμένο τίτλο
                    FirebaseDatabase.getInstance().getReference("Story").child(storyId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot storySnapshot) {
                                    Story storyObj = storySnapshot.getValue(Story.class);
                                    if (storyObj != null && plays != null) {
                                        // Επιλογή τίτλου βάσει γλώσσας
                                        String translatedTitle;
                                        if (lang.equals("el")) translatedTitle = storyObj.getTitle_el();
                                        else if (lang.equals("fr")) translatedTitle = storyObj.getTitle_fr();
                                        else translatedTitle = storyObj.getTitle();

                                        recordList.add(new Record(translatedTitle, plays));

                                        if (plays > maxPlays[0]) {
                                            maxPlays[0] = plays;
                                            topStoryTitle[0] = translatedTitle;
                                            topStoryId[0] = storyId;
                                        }
                                    }

                                    // Ενημέρωση adapter μόνο όταν φορτωθούν όλα
                                    if (recordList.size() >= 0) {
                                        adapter.notifyDataSetChanged();
                                        updateTopStatsUI(topStoryTitle[0], topStoryId[0]);
                                    }
                                }
                                @Override public void onCancelled(@NonNull DatabaseError error) {}
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (FirebaseAuth.getInstance().getCurrentUser() == null ||
                        error.getCode() == DatabaseError.PERMISSION_DENIED) {
                    android.util.Log.d("Firebase_Logout", "Silent permission denial after logout.");
                } else {
                    Toast.makeText(RecordsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @SuppressLint("StringFormatInvalid")
    private void updateTopStatsUI(String topStoryTitle, String topStoryId) {
        if (!topStoryTitle.isEmpty()) {
            tvBestStoryTitle.setText(topStoryTitle);
            tvSuggestion.setText(getString(com.myprojects.unipiaudiostories.R.string.suggestion_text));
            tvSuggestion.setVisibility(View.VISIBLE);

            // πατώντας στον τίτλο μεταφέρεται στην StoryActivity
            tvBestStoryTitle.setOnClickListener(v -> {
                Intent intent = new Intent(RecordsActivity.this, StoryActivity.class);
                intent.putExtra("STORY_ID", topStoryId);
                startActivity(intent);
            });

            tvSuggestion.setOnClickListener(v -> {
                if (userId != null && !topStoryId.isEmpty()) {
                    DatabaseReference favRef = FirebaseDatabase.getInstance()
                            .getReference("User")
                            .child(userId)
                            .child("favorites")
                            .child(topStoryId);

                    java.util.HashMap<String, Object> favData = new java.util.HashMap<>();
                    favData.put("title", topStoryTitle);
                    favData.put("isFavorite", true);

                    favRef.setValue(favData).addOnSuccessListener(aVoid -> {
                        String message = getString(com.myprojects.unipiaudiostories.R.string.add_fav);
                        Toast.makeText(RecordsActivity.this, message, Toast.LENGTH_SHORT).show();
                        tvSuggestion.setText(com.myprojects.unipiaudiostories.R.string.already_in);
                        tvSuggestion.setEnabled(false);
                    });
                }
            });
        } else {
            tvBestStoryTitle.setText(com.myprojects.unipiaudiostories.R.string.no_stats_yet);
            tvSuggestion.setVisibility(View.GONE);
        }
    }

    private void loadGlobalStatistics() {
        DatabaseReference globalRef = FirebaseDatabase.getInstance().getReference("GlobalStatistics");

        // Query για την πιο δημοφιλή ιστορία από όλη τη βάση
        globalRef.orderByChild("playCount").limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                String storyId = ds.getKey();

                                // Fetch μεταφρασμένου τίτλου για το Global Stat
                                FirebaseDatabase.getInstance().getReference("Story").child(storyId)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot storySnap) {
                                                Story s = storySnap.getValue(Story.class);
                                                if (s != null) {
                                                    String gTitle;
                                                    if (lang.equals("el")) gTitle = s.getTitle_el();
                                                    else if (lang.equals("fr")) gTitle = s.getTitle_fr();
                                                    else gTitle = s.getTitle();

                                                    tvGlobalBestStory.setText(gTitle);
                                                    tvGlobalSuggestion.setText(getString(com.myprojects.unipiaudiostories.R.string.global_suggestion_text));
                                                    tvGlobalSuggestion.setVisibility(View.VISIBLE);

                                                    View.OnClickListener listener = v -> {
                                                        Intent intent = new Intent(RecordsActivity.this, StoryActivity.class);
                                                        intent.putExtra("STORY_ID", storyId);
                                                        startActivity(intent);
                                                    };
                                                    tvGlobalBestStory.setOnClickListener(listener);
                                                    tvGlobalSuggestion.setOnClickListener(listener);
                                                }
                                            }
                                            @Override public void onCancelled(@NonNull DatabaseError error) {}
                                        });
                            }
                        } else {
                            tvGlobalBestStory.setText("-");
                            tvGlobalSuggestion.setVisibility(View.GONE);
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}