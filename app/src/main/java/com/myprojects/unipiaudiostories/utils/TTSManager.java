package com.myprojects.unipiaudiostories.utils;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import com.myprojects.unipiaudiostories.interfaces.OnStoryProgressListener;

import java.util.Locale;

public class TTSManager {
    private TextToSpeech tts;
    private OnStoryProgressListener progressListener;
    private boolean isInitialized = false;

    public TTSManager(Context context, OnStoryProgressListener listener) {
        this.progressListener = listener;

        tts = new TextToSpeech(context, status -> {
            if (status != TextToSpeech.ERROR) {
                isInitialized = true;
                // Η γλώσσα ορίζεται δυναμικά μέσω της setLanguage()
                tts.setSpeechRate(0.8f);
                tts.setPitch(1.0f);
            }
        });

        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {}

            @Override
            public void onDone(String utteranceId) {
                if (progressListener != null) {
                    progressListener.onStoryFinished();
                }
            }

            @Override
            public void onError(String utteranceId) {}

            @Override
            public void onRangeStart(String utteranceId, int start, int end, int frame) {
                if (progressListener != null) {
                    progressListener.onProgressUpdate(start);
                }
            }
        });
    }





    public void setLanguage(String langCode) {
        if (!isInitialized) return;

        Locale locale;
        switch (langCode) {
            case "el":
                locale = new Locale("el", "GR");
                break;
            case "fr":
                locale = Locale.FRENCH;
                break;
            default:
                locale = Locale.UK; // Default για τα Αγγλικά
                break;
        }

        int result = tts.setLanguage(locale);
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.e("TTS_ERROR", "Language not supported: " + langCode);
        }
    }





    public void speak(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "story_utterance_id");
    }

    public void stop() {
        if (tts != null) tts.stop();
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}