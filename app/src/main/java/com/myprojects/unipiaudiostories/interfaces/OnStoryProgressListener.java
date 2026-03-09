package com.myprojects.unipiaudiostories.interfaces;

public interface OnStoryProgressListener {
    //το καλώ στην ανάγνωση για την ενημέρωση της μπάρας
    void onProgressUpdate(int progress);
    void onStoryFinished();
}
