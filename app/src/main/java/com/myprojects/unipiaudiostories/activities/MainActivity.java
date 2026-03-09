package com.myprojects.unipiaudiostories.activities;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.myprojects.unipiaudiostories.utils.AuthManager;
import com.myprojects.unipiaudiostories.utils.LanguageHelper;
import com.myprojects.unipiaudiostories.utils.LogoutNow;
import com.myprojects.unipiaudiostories.R;
import com.myprojects.unipiaudiostories.models.Story;
import com.myprojects.unipiaudiostories.adapters.StoryAdapter;
import com.myprojects.unipiaudiostories.utils.VoiceCommandManager;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements StoryAdapter.OnStoryClickListener {
    private RecyclerView rvStories;
    private StoryAdapter adapter;
    private List<Story> storyList;
    private DatabaseReference dbRef;
    private AuthManager authManager; // Προσθήκη AuthManager για τη διαγραφή


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Ενημέρωση των resources βάσει της αποθηκευμένης γλώσσας πριν το super.onCreate
        LanguageHelper.updateResources(this, LanguageHelper.getSavedLanguage(this));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Αρχικοποίηση AuthManager
        authManager = new AuthManager();

        // Λήψη της τρέχουσας γλώσσας για να την περάσουμε στον Adapter
        String currentLang = LanguageHelper.getSavedLanguage(this);

        initViews();

        storyList = new ArrayList<>();
        dbRef = FirebaseDatabase.getInstance().getReference("Story");

        rvStories = findViewById(R.id.rvStories);
        rvStories.setLayoutManager(new LinearLayoutManager(this));

        // Περνάμε τη γλώσσα στον adapter για να ξέρει ποιον τίτλο να δείξει
        adapter = new StoryAdapter(storyList, this, currentLang);
        rvStories.setAdapter(adapter);

        //φόρτωση ιστοριών και εμφάνιση στο recycler view
        loadStories();
    }





    private void initViews() {
        ImageButton btnLogout = findViewById(R.id.btnLogout);
        ImageButton btnRecords = findViewById(R.id.btnRecords);
        ImageButton btnFavs = findViewById(R.id.btnFavs);
        ImageButton btnVoiceCommand = findViewById(R.id.btnVoiceCommand);
        ImageButton btnLanguage = findViewById(R.id.btnLanguage);
        ImageButton btnDeleteAccount = findViewById(R.id.btnDeleteAccount); // Το νέο κουμπί πάνω αριστερά

        // αποσύνδεση από την LogoutNow class
        btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());


        btnRecords.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RecordsActivity.class));
        });


        btnFavs.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, FavsActivity.class));
        });


        btnDeleteAccount.setOnClickListener(v -> showDeleteConfirmationDialog());


        btnVoiceCommand.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                // ο χρήστης πρέπει να δώσει συγκατάθεση αρχικά
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            } else {
                try {
                    Intent intent = VoiceCommandManager.getVoiceIntent();
                    startActivityForResult(intent, 100);
                } catch (ActivityNotFoundException a) {
                    Snackbar.make(findViewById(android.R.id.content), R.string.voice_command_error, Snackbar.LENGTH_SHORT).show();
                }
            }
        });


        btnLanguage.setOnClickListener(v -> showLanguageDialog());
    }







    //alertDialog επιβεβαίωσης αποσύνδεσης χρήστη
    private void showLogoutConfirmationDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setTitle(R.string.logout)
                .setMessage(R.string.confirm_logout)
                .setPositiveButton(R.string.confirm_yes, (dialog, which) -> {
                    LogoutNow.logout(MainActivity.this);
                })
                .setNegativeButton(R.string.confirm_no, null)
                .show();
    }







    //διαγραφή λογαριασμού χρήστη και κλήση του AuthManager
    private void showDeleteConfirmationDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setTitle(R.string.delete_account)
                .setMessage(R.string.confirm_delete_account)
                .setPositiveButton(R.string.confirm_yes, (dialog, which) -> {
                    authManager.deleteAccount(new AuthManager.AuthCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(MainActivity.this, R.string.account_deleted, Toast.LENGTH_LONG).show();
                            // Μετά τη διαγραφή, στέλνουμε τον χρήστη στην AuthActivity και καθαρίζουμε το stack
                            Intent intent = new Intent(MainActivity.this, AuthActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Toast.makeText(MainActivity.this, R.string.try_again, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.confirm_no, null)
                .show();
    }







    private void loadStories() {
        //ValueEventListener για άμεση ενημέρωση του UI με τις αλλαγές της βάσης
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //πάλι ένα clear() για να διασφαλίσω ότι κάθε ιστορία εμφανίζεται μία φορά
                storyList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Story story = ds.getValue(Story.class);
                    if (story != null) {
                        storyList.add(story);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    @Override
    public void onStoryClick(Story story) {
        Intent intent = new Intent(MainActivity.this, StoryActivity.class);
        //το STORY_ID είναι key για μετάβαση σωστά στην StoryActivity
        intent.putExtra("STORY_ID", story.getStoryId());
        startActivity(intent);
    }








    // επιλογή και ορισμός γλώσσας εφαρμογής (string resources) και ιστοριών (model class Story)
    private void showLanguageDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_language, null);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        // θολωμένο background του dialog
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogView.findViewById(R.id.btnEn).setOnClickListener(v -> changeLang("en", dialog));
        dialogView.findViewById(R.id.btnEl).setOnClickListener(v -> changeLang("el", dialog));
        dialogView.findViewById(R.id.btnFr).setOnClickListener(v -> changeLang("fr", dialog));

        dialog.show();
    }

    private void changeLang(String langCode, androidx.appcompat.app.AlertDialog dialog) {
        dialog.dismiss();
        //καλώ τη μέθοδο τoυ LanguageHelper για να ενημερωθεί το Locale και η γλώσσα
        LanguageHelper.setLocale(this, langCode);

        //κάνω μία άμεση επανεκκίνηση της MainActivity για να ενημερωθεί το UI και η γλώσσα
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }








    //φωνητικές εντολές για άνοιγμα άλλων οθονών της εφαρμογής
    // ή άνοιγμα κάποιας ιστορίας με βάση τον τίτλο
    //για τις φωνητικές εντολές και το άνοιγμα ιστοριών
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Έλεγχος απάντησης από το voice recognition
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {

            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            int commandId = VoiceCommandManager.processResult(matches);

            if (commandId == R.string.records) {
                startActivity(new Intent(this, RecordsActivity.class));
            }
            else if (commandId == R.string.favorites) {
                startActivity(new Intent(this, FavsActivity.class));
            }
            else if (commandId == R.string.choose_language) {
                showLanguageDialog();
            }
            else if (commandId == R.string.close_app) {
                // Κλείσε την εφαρμογή εντελώς όλα τα activities
                finishAffinity();
            }
            else if (commandId == R.string.delete_account) {
                showDeleteConfirmationDialog();
            }
            else if (commandId == R.string.logout) {
                showLogoutConfirmationDialog();
            }
            else {
                //αν δεν είναι εντολή για άνοιγμα κάποια οθόνης μπορεί να είναι για εύρεση κάποια ιστορίας
                String foundStoryId = VoiceCommandManager.findStoryByTitle(matches, storyList);

                if (foundStoryId != null) {
                    //η ιστορία βρέθηκε και την σερβίρω στον χρήστη
                    Intent intent = new Intent(MainActivity.this, StoryActivity.class);
                    intent.putExtra("STORY_ID", foundStoryId);
                    startActivity(intent);
                } else {
                    //διαφορετικά το snackbar για το μήνυμα λάθους εντολής
                    showUnknownCommandSnackbar();
                }
            }
        }
    }

    //για το snackbar
    private void showUnknownCommandSnackbar() {
        Snackbar snackbar = Snackbar.make(
                findViewById(android.R.id.content),
                getString(R.string.unknown_voice_command),
                Snackbar.LENGTH_INDEFINITE
        );

        snackbar.setAction(R.string.try_again, v -> {
            Intent intent = VoiceCommandManager.getVoiceIntent();
            startActivityForResult(intent, 100);
        });

        snackbar.setActionTextColor(Color.parseColor("#6200EE"));
        snackbar.show();
    }
}