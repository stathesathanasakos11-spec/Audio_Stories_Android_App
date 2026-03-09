package com.myprojects.unipiaudiostories.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import java.util.Locale;

public class LanguageHelper {
    private static final String PREFS_NAME = "LanguagePrefs";  //αρχείο shared preferences
    private static final String KEY_LANG = "selected_lang";

    public static void setLocale(Context context, String langCode) {
        // αποθήκευση της επιλογής και ενημέρωση της γλώσσας στην εφαρμογή του χρήστη
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANG, langCode).apply();

        updateResources(context, langCode);
    }



    public static void updateResources(Context context, String langCode) {
        // το Locale καθορίζει ποιο string.xml θα διαβαστεί
        // είναι ένα αντικείμενο της Java
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);


        // ορίζω το επιλεγμένο σετ γλώσσας
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    public static String getSavedLanguage(Context context) {
        // έτσι βλέπω τι επέλεξε ο χρήστης την προηγούμενη φορά για να μην
        // ξαναμοεί η default γλώσσα που είναι τα αγγλικά
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANG, "en");
    }
}
