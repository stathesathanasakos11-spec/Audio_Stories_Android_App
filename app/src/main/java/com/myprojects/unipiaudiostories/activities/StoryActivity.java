package com.myprojects.unipiaudiostories.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.myprojects.unipiaudiostories.interfaces.OnStoryProgressListener;
import com.myprojects.unipiaudiostories.R;
import com.myprojects.unipiaudiostories.models.Story;
import com.myprojects.unipiaudiostories.utils.RecordsUpdate;
import com.myprojects.unipiaudiostories.utils.LanguageHelper;
import com.myprojects.unipiaudiostories.utils.StoryDataManager;
import com.myprojects.unipiaudiostories.utils.TTSManager;
import com.myprojects.unipiaudiostories.utils.WaveformView;


public class StoryActivity extends AppCompatActivity implements OnStoryProgressListener {
    private ImageView ivStory;
    private WaveformView waveformView;

    //δύο μετρητές για την σωστή ενημέρωση της waveform progressbar
    private int lastCharIndex = 0;  //σε ποιο χαρακτήρα σταμάτησε το TTS engine
    private int baseIndex = 0; // δείκτης για το pause & resume

    private TextView tvTitle, tvAuthor;
    private ImageButton btnPlayPause, btnBack, btnFav;

    private TTSManager ttsManager;
    private StoryDataManager dataManager;
    private Story currentStory;

    private boolean isPlaying = false;
    private boolean isFavorite = false;
    private String userId;
    private String lang; // Η γλώσσα της εφαρμογής

    //handler για την εναλλαγή των εικόνων της ιστορίας
    private Handler imageHandler = new Handler();
    private int imageToggle = 1;

    private View lessonOverlay;
    private TextView tvLessonText;
    private Button btnDismissLesson;

    // Μεταβλητή για να θυμόμαστε την κατάσταση του overlay κατά το loading
    private boolean shouldShowLessonAfterLoad = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //είτε portrait layout είτε land layout
        setContentView(R.layout.activity_story);

        // Ανάκτηση της γλώσσας από τα SharedPreferences
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        lang = prefs.getString("My_Lang", "en"); // default τα αγγλικά

        lang = getSharedPreferences("Settings", MODE_PRIVATE).getString("My_Lang", "en");

        lang = LanguageHelper.getSavedLanguage(this);


        userId = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        String storyId = getIntent().getStringExtra("STORY_ID");

        initViews();

        dataManager = new StoryDataManager();
        ttsManager = new TTSManager(this, this);

        // επαναφορά κατάστασης μετά από περιστροφή οθόνης
        if (savedInstanceState != null) {
            lastCharIndex = savedInstanceState.getInt("LAST_CHAR_INDEX", 0);
            isPlaying = savedInstanceState.getBoolean("IS_PLAYING", false);
            // Σημειώνουμε ότι πρέπει να εμφανιστεί το μάθημα, αλλά θα το κάνουμε μετά το Load της ιστορίας
            shouldShowLessonAfterLoad = savedInstanceState.getBoolean("lesson_was_open", false);
        }

        if (storyId != null) {
            loadStory(storyId);
            checkInitialFavoriteStatus(storyId);
        } else {
            Toast.makeText(this, "Error: Story ID missing", Toast.LENGTH_SHORT).show();
            finish();
        }
    }







    private void initViews() {
        ivStory = findViewById(R.id.ivStoryLargeImage);
        waveformView = findViewById(R.id.storyWaveform);
        tvTitle = findViewById(R.id.tvStoryTitleDisplay);
        tvAuthor = findViewById(R.id.tvStoryAuthor);
        btnPlayPause = findViewById(R.id.btnTogglePlay);
        btnBack = findViewById(R.id.btnBack);
        btnFav = findViewById(R.id.btnFavStory);
        lessonOverlay = findViewById(R.id.lessonOverlay);
        tvLessonText = findViewById(R.id.tvLessonText);
        btnDismissLesson = findViewById(R.id.btnDismissLesson);

        btnBack.setOnClickListener(v -> finish());
        btnPlayPause.setOnClickListener(v -> handlePlayback());

        //ταυτόχρονη ενημέρωση του favoriteIcon με την προσθήκη/αφαίρεση ιστοράις από τα αγαπημένα
        btnFav.setOnClickListener(v -> {
            if (currentStory != null) {
                isFavorite = !isFavorite;
                dataManager.toggleFavorite(currentStory.getStoryId(), isFavorite);
                updateFavoriteIcon();
                String msg = isFavorite ? getString(R.string.add_fav) : getString(R.string.remove_fav);
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        btnDismissLesson.setOnClickListener(v -> {
            // επιστροφή στην MainActivity
            finish();
            // animation μετάβασης στην αρχική οθόνη
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);});
    }







    private void checkInitialFavoriteStatus(String storyId) {
        dataManager.checkIsFavorite(storyId, new ValueEventListener() {
            //realtime ενημέρωση του favoriteIcon
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isFavorite = snapshot.exists();
                updateFavoriteIcon();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }






    private void loadStory(String storyId) {
        dataManager.getStoryReference(storyId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentStory = snapshot.getValue(Story.class);
                //παίρνω τις πληροφορίες και το περιεχομένου κάθε ιστορίας από τον κόμβο Story
                if (currentStory != null) {

                    // Δυναμική επιλογή τίτλου βάσει γλώσσας
                    String displayTitle;
                    if (lang.equals("el")) displayTitle = currentStory.getTitle_el();
                    else if (lang.equals("fr")) displayTitle = currentStory.getTitle_fr();
                    else displayTitle = currentStory.getTitle(); // αγγλικά

                    tvTitle.setText(displayTitle);

                    if (currentStory.getAuthor() != null) {
                        tvAuthor.setText(currentStory.getAuthor());
                    } else {
                        tvAuthor.setText(R.string.app_name);
                    }

                    //default εικόνα κάθε ιστορίας η imageUrl1 και μετά γίνεται rotation
                    updateImage(currentStory.getImageUrl1());
                    startImageRotation();

                    //αν έγινε rotation της οθόνης και υπάρχει lastCharIndex πρέπει να ενημερώσω το Waveform
                    // Πλέον καλούμε την updateWaveformManually σε κάθε περίπτωση για να αρχικοποιηθεί το σωστό μήκος κειμένου
                    updateWaveformManually();

                    // ΔΙΟΡΘΩΣΗ: Αν η Activity ξαναδημιουργήθηκε και το μάθημα ήταν ανοιχτό, το δείχνουμε τώρα που έχουμε το currentStory
                    if (shouldShowLessonAfterLoad) {
                        showLessonCustom();
                        shouldShowLessonAfterLoad = false; // Reset
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }









    private void handlePlayback() {
        if (currentStory == null) return;

        if (!isPlaying) {
            // Δυναμική επιλογή περιεχομένου βάσει γλώσσας
            String fullContent;
            if (lang.equals("el")) fullContent = currentStory.getContent_el();
            else if (lang.equals("fr")) fullContent = currentStory.getContent_fr();
            else fullContent = currentStory.getContent();

            ttsManager.setLanguage(lang);

            if (fullContent == null) return;

            String remainingText = fullContent.substring(lastCharIndex);

            //αν υπάρχουν ακόμα χαρακτήρες άρα το TTS engine δεν έχει τελειώσει
            if (!remainingText.isEmpty()) {
                baseIndex = lastCharIndex;

                // Ενημέρωση της γλώσσας στον TTSManager πριν την εκφώνηση
                ttsManager.setLanguage(lang);

                ttsManager.speak(remainingText);
                btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                isPlaying = true;
            }
        } else {
            ttsManager.stop();
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
            isPlaying = false;
        }
    }








    @Override
    public void onProgressUpdate(int charIndex) {
        //χαρακτήρας εκφώνησης στο κείμενο
        this.lastCharIndex = baseIndex + charIndex;
        runOnUiThread(this::updateWaveformManually);
    }

    private void updateWaveformManually() {
        if (currentStory != null) {
            // Επιλογή του σωστού content για τον υπολογισμό του μήκους
            String content;
            if (lang.equals("el")) content = currentStory.getContent_el();
            else if (lang.equals("fr")) content = currentStory.getContent_fr();
            else content = currentStory.getContent();

            // Fallback αν το μεταφρασμένο content είναι null
            if (content == null) content = currentStory.getContent();

            if (content != null) {
                int textLength = content.length();
                if (textLength > 0) {
                    // το lastCharIndex να μην ξεπερνά το μήκος του κειμένου
                    int safeIndex = Math.min(lastCharIndex, textLength);

                    //το waveform progressBar ενημερώνεται παράλληλα με το ποσοστό ολοκλήρωσης εκφώνησης του content της ιστορίας
                    float progress = ((float) safeIndex / textLength) * 100;

                    // Ενημέρωση του UI στοιχείου για κάθε χαρακτήρα της εκφώνησης κάθε φορά
                    waveformView.setProgress(progress);
                }
            }
        }
    }








    @Override
    public void onStoryFinished() {
        //αρχικοποίηση δεικτών για επόμενη αφήγηση
        lastCharIndex = 0;
        baseIndex = 0;

        if (currentStory != null) {
            //ενημέρωση πεδίου playCount στον κόμβο Statistics για τον συγκεκριμένο χρήστη
            RecordsUpdate.updateStatistics(userId, currentStory);
            // ενημέρωση playCount στον κόμβο GlobalStatistics για όλους του χρήστες
            RecordsUpdate.updateGlobalStatistics(currentStory);
        }

        //μόνο το Main Thread έχει πρόσβαση στα views και επειδή το Text-To-Speech
        //χρησιμοποιεί δικό του thread πρέπει να καλέσω ξανά το UIThread (main thread)
        runOnUiThread(() -> {
            isPlaying = false;
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
            waveformView.setProgress(0);
            //ο handler επίσης δίνει οδηγίες στο main thread της εφαρμογής
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                if (!isFinishing()) {
                    showLessonCustom();
                }
            }, 1000);
        });
    }








    private void startImageRotation() {
        imageHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentStory != null && currentStory.getImageUrl2() != null && isPlaying) {
                    String url = (imageToggle == 1) ? currentStory.getImageUrl2() : currentStory.getImageUrl1();
                    updateImage(url);
                    imageToggle = (imageToggle == 1) ? 2 : 1;
                }
                //αλλαγή εικόνας ανά 7 sec
                imageHandler.postDelayed(this, 7000);
            }
        }, 7000);
    }

    private void updateImage(String url) {
        if (!isDestroyed()) {
            //η Glide δρα στο backround thread και όχι στο Main Thread
            Glide.with(this).load(url).placeholder(R.drawable.circle_background).into(ivStory);
        }
    }







    private void updateFavoriteIcon() {
        btnFav.setColorFilter(isFavorite ? 0xFFFF0000 : 0xFFFFFFFF);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ttsManager != null) ttsManager.shutdown();
        imageHandler.removeCallbacksAndMessages(null);
    }







    //κάθε ιστορία έχει ηθικό δίδαγμα
    // κόμβος Story πεδίο lesson
    private void showLessonCustom() {
        if (currentStory != null) {
            // Δυναμική επιλογή διδάγματος βάσει γλώσσας που όρισε ο χρήστης
            String lesson;
            if (lang.equals("el")) lesson = currentStory.getLesson_el();
            else if (lang.equals("fr")) lesson = currentStory.getLesson_fr();
            else lesson = currentStory.getLesson();

            if (lesson != null) {
                tvLessonText.setText(lesson);
                lessonOverlay.setAlpha(0f);
                lessonOverlay.setVisibility(View.VISIBLE);
                lessonOverlay.animate().alpha(1f).setDuration(500).start();
            }

        }
    }

    // στο rotation της οθόνης η Activity καταστρέφεται και ξαναδημιουργείται
    //πρέπει να κρατάω κάθε φορά την κατάσταση του TTS engine
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("LAST_CHAR_INDEX", lastCharIndex);
        outState.putBoolean("IS_PLAYING", isPlaying);
        // Αποθηκεύω αν το overlay είναι ορατό αυτή τη στιγμή
        // για να παραμένει το παράθυρο ανοιχτό κατά το rotation
        if (lessonOverlay != null && lessonOverlay.getVisibility() == View.VISIBLE) {
            outState.putBoolean("lesson_was_open", true);
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Η επαναφορά του overlay γίνεται πλέον μέσα στην loadStory() για να έχουμε τα δεδομένα
    }

}