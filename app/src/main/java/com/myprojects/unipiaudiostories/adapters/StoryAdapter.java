package com.myprojects.unipiaudiostories.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.myprojects.unipiaudiostories.models.Story;
import com.myprojects.unipiaudiostories.R;
import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder> {
    // για το recycler view της MainActivity
    private List<Story> storyList;
    private OnStoryClickListener listener;
    private String lang; // Η γλώσσα της εφαρμογής για τη δυναμική εμφάνιση τίτλων


    //Interface για το click σε κάθε Story
    public interface OnStoryClickListener {
        void onStoryClick(Story story);
    }

    // Ενημερωμένος constructor που δέχεται και τη γλώσσα
    public StoryAdapter(List<Story> storyList, OnStoryClickListener listener, String lang) {
        this.storyList = storyList;
        this.listener = listener;
        this.lang = lang;
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // αρχικοποίηση του layout με τα αντικείμενα του story_item_list δηλαδή τις ιστορίες
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.story_item_list, parent, false);
        return new StoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {
        Story story = storyList.get(position);

        // Επιλογή τίτλου ανάλογα με τη γλώσσα της εφαρμογής
        String displayTitle;
        if ("el".equals(lang)) {
            displayTitle = story.getTitle_el();
        } else if ("fr".equals(lang)) {
            displayTitle = story.getTitle_fr();
        } else {
            displayTitle = story.getTitle(); // Default (English)
        }

        // Fallback: Αν ο μεταφρασμένος τίτλος είναι κενός, εμφάνισε τον default
        if (displayTitle == null || displayTitle.isEmpty()) {
            displayTitle = story.getTitle();
        }

        holder.tvTitle.setText(displayTitle);
        holder.tvAuthor.setText(story.getAuthor());

        //φορτώνω τις εικόνες στο layout με την Glide
        Glide.with(holder.itemView.getContext())
                .load(story.getImageUrl1())
                .placeholder(R.drawable.circle_background) // Μέχρι να κατέβει η εικόνα
                .into(holder.ivIcon);

        // Listener για μετάβαση στο StoryActivity
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onStoryClick(story);
            }
        });
    }

    @Override
    public int getItemCount() {

        return storyList.size();
    }

    // ο ViewHolder κρατάει τις αναφορές στα views για ταχύτητα
    public static class StoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAuthor;
        ImageView ivIcon;

        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvStoryTitle);
            tvAuthor = itemView.findViewById(R.id.tvStoryAuthor);
            ivIcon = itemView.findViewById(R.id.ivStoryIcon);
        }
    }
}