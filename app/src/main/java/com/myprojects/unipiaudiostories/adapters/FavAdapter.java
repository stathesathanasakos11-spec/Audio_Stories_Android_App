package com.myprojects.unipiaudiostories.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.myprojects.unipiaudiostories.models.Story;
import com.myprojects.unipiaudiostories.interfaces.OnFavClickListener;
import com.myprojects.unipiaudiostories.R;
import java.util.List;


public class FavAdapter extends RecyclerView.Adapter<FavAdapter.FavViewHolder> {
    private List<Story> favList;
    private OnFavClickListener listener;
    private String lang; // Η γλώσσα για τη δυναμική εμφάνιση τίτλων


    // Ενημερωμένος constructor με 3 παραμέτρους
    public FavAdapter(List<Story> favList, OnFavClickListener listener, String lang) {
        this.favList = favList;
        this.listener = listener;
        this.lang = lang;
    }

    @NonNull
    @Override
    public FavViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fav_story, parent, false);
        return new FavViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FavViewHolder holder, int position) {
        Story story = favList.get(position);

        // Επιλογή τίτλου ανάλογα με τη γλώσσα της εφαρμογής
        String displayTitle;
        if ("el".equals(lang)) {
            displayTitle = story.getTitle_el();
        } else if ("fr".equals(lang)) {
            displayTitle = story.getTitle_fr();
        } else {
            displayTitle = story.getTitle(); // Default (English)
        }

        // Fallback: Αν ο μεταφρασμένος τίτλος είναι κενός στη βάση, εμφάνισε τον default αγγλικό
        if (displayTitle == null || displayTitle.isEmpty()) {
            displayTitle = story.getTitle();
        }

        //τίτλος και συγγραφέας για το UI τη συμπλήρωση του item_fav_story
        holder.tvTitle.setText(displayTitle);
        holder.tvAuthor.setText(story.getAuthor());

        //βιβλιοθήκη Glide για αναπαρωγή των φωτο της ιστορίας
        Glide.with(holder.itemView.getContext())
                .load(story.getImageUrl1())
                .placeholder(R.drawable.circle_background)
                .into(holder.ivIcon);

        // ανακατεύθυνση στην StoryActivity με όρισμα την ιστορία από την επιλεγμένη κάρτα
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onStoryClick(story);
        });


        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) listener.onRemoveClick(story);
        });
    }

    @Override
    public int getItemCount() {
        return favList.size();
    }

    static class FavViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAuthor;
        ImageView ivIcon;
        ImageButton btnRemove;

        FavViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvFavStoryTitle);
            tvAuthor = itemView.findViewById(R.id.tvFavStoryAuthor);
            ivIcon = itemView.findViewById(R.id.ivFavStoryIcon);
            btnRemove = itemView.findViewById(R.id.btnRemoveFav);
        }
    }
}
