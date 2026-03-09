package com.myprojects.unipiaudiostories.models;

public class Record {
    //γισ να έχω τα στατιστικά κάθε ιστορίας χρειάζομαι απλά τον τίτλο της και μια μεταβλητή
    //για το πλήθος των φορών που αναπαράχθηκε
    private String storyTitle;
    private long playCount;

    //για Firebase
    public Record() {}

    public Record(String storyTitle, long playCount) {
        this.storyTitle = storyTitle;
        this.playCount = playCount;
    }

    public String getStoryTitle() { return storyTitle; }
    public long getPlayCount() { return playCount; }
}
