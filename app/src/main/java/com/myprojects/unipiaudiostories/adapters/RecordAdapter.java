package com.myprojects.unipiaudiostories.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myprojects.unipiaudiostories.models.Record;
import com.myprojects.unipiaudiostories.R;

import java.util.List;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.RecordViewHolder> {
    //έφτιαξα αυτόν τον adapter για επικοινωνία ΒΔ και UI στατιστικών μέσω των πεδίων της model class Record
    private List<Record> recordList;

    public RecordAdapter(List<Record> recordList) {
        this.recordList = recordList;
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // χρηση viewHolder για να για να κρατάει τα views μία φορά και όχι να πρέπει
        //κάθε φορά να κάνει ξανά αναζήτηση
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_record, parent, false);
        return new RecordViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        // στήσιμο UI
        Record record = recordList.get(position);
        //τίτλος ιστορίας που μόλις ολοκληρώθηκε
        holder.tvTitle.setText(record.getStoryTitle());
        String playsText = holder.itemView.getContext().getString(R.string.times_played) + ": " + record.getPlayCount();
        holder.tvCount.setText(playsText);
    }



    @Override
    public int getItemCount() {
        //πλήθος των εγγραφών στη λίστα στατιστικών
        return recordList.size();
    }

    static class RecordViewHolder extends RecyclerView.ViewHolder {
        // για γρήγορο στήσιμο UI του item_record και δημιουργία της λίστας
        TextView tvTitle, tvCount;
        RecordViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvRecordTitle);
            tvCount = itemView.findViewById(R.id.tvPlayCount);
        }
    }
}
