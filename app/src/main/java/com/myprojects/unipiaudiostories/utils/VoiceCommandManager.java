package com.myprojects.unipiaudiostories.utils;

import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;
import com.myprojects.unipiaudiostories.R;
import com.myprojects.unipiaudiostories.models.Story;

import java.util.List;
import java.util.Locale;
import java.util.ArrayList;


public class VoiceCommandManager {

    public static Intent getVoiceIntent() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        //να αναγνωρίζει και τις 3 γλώσσες της εφαρμογής
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, R.string.speak_now);
        return intent;
    }



    public static int processResult(ArrayList<String> matches) {
        if (matches == null || matches.isEmpty()) return -1;

        String spokenText = matches.get(0).toLowerCase();

        if (spokenText.contains("statistics") || spokenText.contains("στατιστικά") || spokenText.contains("statistiques") || spokenText.contains("στατιστικών")) {
            return R.string.records;
        }

        if (spokenText.contains("favorites") || spokenText.contains("αγαπημένα") || spokenText.contains("favoris") || spokenText.contains("αγαπημένων")){
            return R.string.favorites;
        }

        if (spokenText.contains("language") || spokenText.contains("γλώσσα") || spokenText.contains("langue") || spokenText.contains("γλώσσας")) {
            return R.string.choose_language;
        }

        if (spokenText.contains("close") || spokenText.contains("close the app") || spokenText.contains("fermer l'application") || spokenText.contains("κλείσε την εφαρμογή") || spokenText.contains("κλείσιμο") || spokenText.contains("fermer") || spokenText.contains("βγες από την εφαρμογή") ||spokenText.contains("κλείστη")) {
            return R.string.close_app;
        }

        if (spokenText.contains("delete the account") || spokenText.contains("delete") || spokenText.contains("διαγραφή λογαριασμού") || spokenText.contains("να διαγράψω") || spokenText.contains("suppression") || spokenText.contains("radiation") || spokenText.contains("account deletion") || spokenText.contains("radier") || spokenText.contains("supprimer")) {
            return R.string.delete_account;
        }

        if (spokenText.contains("logout") || spokenText.contains("log out") || spokenText.contains("αποσύνδεση") || spokenText.contains("να αποσυνδεθώ") || spokenText.contains("déconnexion") || spokenText.contains("déconnection") || spokenText.contains("déconnecter")){
            return R.string.logout;
        }

        return -1;
    }




    //παίρνει το κείμενο από το μικρόφωνο και την λίστα των διαθέσιμων ιστοριών
    public static String findStoryByTitle(ArrayList<String> matches, List<Story> stories){
        if (matches == null || stories == null) return null;

        for (String result : matches) { // Για κάθε πιθανή εκδοχή που άκουσε το μικρόφωνο
            String lowerResult = result.toLowerCase().trim();

            for (Story story : stories) {
                // Ελέγχουμε τον τίτλο σε όλες τις γλώσσες
                if (isMatch(lowerResult, story.getTitle()) ||
                        isMatch(lowerResult, story.getTitle_el()) ||
                        isMatch(lowerResult, story.getTitle_fr())) {

                    return story.getStoryId(); // επιστροφή του storyId στην MainActivity
                }
            }
        }
        return null; // δεν βρέθηκε κάποια ιστορία
    }


    private static boolean isMatch(String voiceInput, String storyTitle) {
        if (storyTitle == null || voiceInput == null) return false;

        //preprocess κειμένου ηχογράφησης
        String i = voiceInput.toLowerCase().trim();
        String t = storyTitle.toLowerCase().trim();

        //αν το κείμενο ηχογράφησης είναι ο τίτλος
        if (i.contains(t) || t.contains(i)) return true;

        String[] titleWords = t.replaceAll("\\b(ο|η|το|τον|την|του|της|τα|τους|the|le|la)\\b", "").trim().split("\\s+");
        String[] inputWords = i.replaceAll("\\b(ο|η|το|τον|την|του|της|τα|τους|the|le|la)\\b", "").trim().split("\\s+");

        int matches = 0;
        for (String tWord : titleWords) {
            if (tWord.length() < 3) continue; // Αγνοούμε μικρές λέξεις

            for (String iWord : inputWords) {
                // αν υπάρχει ομοιότητα 80%+ μεταξύ των λέξεων του τίτλου και του κειμένου ηχογράφησης
                if (calculateSimilarity(tWord, iWord) > 0.75) {
                    matches++;
                    break;
                }
            }
        }

        // Αν βρέθηκαν οι περισσότερες λέξεις του τίτλου
        return matches >= (titleWords.length / 2.0);
    }



    // Αλγόριθμος ομοιότητας κειμένου
    private static double calculateSimilarity(String s1, String s2) {
        int distance = levenshteinDistance(s1, s2);
        return 1.0 - ((double) distance / Math.max(s1.length(), s2.length()));
    }

    private static int levenshteinDistance(String a, String b) {
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++) costs[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }
}
