package com.myprojects.unipiaudiostories.interfaces;

import com.myprojects.unipiaudiostories.models.Story;

public interface OnFavClickListener {
    //για άνοιγμα της StoryActivity με μία ιστορία
    void onStoryClick(Story story);

    // για την αφαίρεση από τα αγαπημένα
    void onRemoveClick(Story story);
}
