package com.myprojects.unipiaudiostories.models;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Story {
    private String storyId;
    private String title;
    private String content;
    private String author;
    private String imageUrl1;
    private String imageUrl2;
    //το lesson είναι το ηθικό δίδαγμα κάθε ιστορίας
    private String lesson;

    // Ελληνικά
    private String title_el;
    private String content_el;
    private String lesson_el;

    // Γαλλικά
    private String title_fr;
    private String content_fr;
    private String lesson_fr;


    //κενό constructor για την Firebase
    public Story(){

    }

    //constructor της model class Story
    public Story(String storyId, String title, String author, String content, String imageUrl1, String imageUrl2, String title_el, String content_el, String lesson_el, String title_fr, String content_fr, String lesson_fr) {
        this.storyId = storyId;
        this.title = title;
        this.author = author;
        this.content = content;
        this.imageUrl1 = imageUrl1;
        this.imageUrl2 = imageUrl2;
        // καινούρια πεδία
        this.title_el = title_el;
        this.content_el = content_el;
        this.lesson_el = lesson_el;
        this.title_fr = title_fr;
        this.content_fr = content_fr;
    }


    /*
    public Story(String storyId, String title, String author, String content, String imageUrl1, String imageUrl2, String lesson) {
        this(storyId, title, author, content, imageUrl1, imageUrl2); // Καλώ τον παραπάνω
        //και δίνω και την νέα παράμετρο
        this.lesson = lesson;
    }
    */

    // getters επειδή θα τα χρειαστούμε αυτά στην MainActivity
    public String getStoryId() { return storyId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getContent() { return content; }
    public String getImageUrl1() { return imageUrl1; }
    public String getImageUrl2() { return imageUrl2; }
    public String getLesson() { return lesson; }
    public String getTitle_el() { return title_el; }
    public String getContent_el() { return content_el; }
    public String getLesson_el() { return lesson_el; }
    public String getTitle_fr() { return title_fr; }
    public String getContent_fr() { return content_fr; }
    public String getLesson_fr() { return lesson_fr; }
    public void setLesson(String lesson) { this.lesson = lesson; }
    public void setTitle_el(String title_el) { this.title_el = title_el; }
    public void setContent_el(String content_el) { this.content_el = content_el; }
    public void setLesson_el(String lesson_el) { this.lesson_el = lesson_el; }
    public void setTitle_fr(String title_fr) { this.title_fr = title_fr; }
    public void setContent_fr(String content_fr) { this.content_fr = content_fr; }
    public void setLesson_fr(String lesson_fr) { this.lesson_fr = lesson_fr; }
    public void setStoryId(String storyId) { this.storyId = storyId; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setAuthor(String author) { this.author = author; }
    public void setImageUrl1(String imageUrl1) { this.imageUrl1 = imageUrl1; }
    public void setImageUrl2(String imageUrl2) { this.imageUrl2 = imageUrl2; }
}
