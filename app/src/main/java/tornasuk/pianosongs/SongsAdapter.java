package tornasuk.pianosongs;

import android.content.Context;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SongViewHolder> implements Filterable {

    private ArrayList<Song> songs;
    private ArrayList<Song> songsFilterList;
    private Context context;
    private View.OnClickListener clickVideoListener;
    private View.OnClickListener clickStateListener;
    private View.OnLongClickListener longClickListener;

    public SongsAdapter(Context context, ArrayList<Song> songs2) {
        this.songs = songs2;
        this.context = context;
        songsFilterList = new ArrayList<>(songs);
    }

    @Override
    public SongsAdapter.SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { // INFLA EL LAYOUT
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.list_songs, parent, false);
        return new SongsAdapter.SongViewHolder(v);
    }

    public void setOnClickVideoListener(View.OnClickListener clickVideoListener){
        this.clickVideoListener = clickVideoListener;
    }

    public void setOnClickStateListener(View.OnClickListener clickStateListener){
        this.clickStateListener = clickStateListener;
    }

    public void setOnLongClickListener(View.OnLongClickListener longClickListener){
        this.longClickListener = longClickListener;
    }

    @Override
    public void onBindViewHolder(SongsAdapter.SongViewHolder holder, int i) { // LEE LOS DATOS DE CADA PUNTO Y LOS MUESTRA EN EL LAYOUT
        Song songData = songs.get(i);
        String name = songData.getName();
        String link = songData.getLinkVideo();
        if(name.contains("-"))
            name = name.replace("-",".");

        holder.name.setText(name);
        holder.autor.setText(songData.getAutor());
        holder.rb.setRating(songData.getRating());

        if(link.trim().equals("")) {
            holder.btnVideo.setImageResource(R.drawable.cancelar);
            holder.btnVideo.setEnabled(false);
        } else if (!link.contains("youtu"))
            holder.btnVideo.setImageResource(R.drawable.partitura2);

        if(songData.isLearned())
            holder.btnState.setImageResource(R.drawable.libro1);
        else
            holder.btnState.setImageResource(R.drawable.hora2);

        holder.cardSong.setBackground(ContextCompat.getDrawable(context, R.drawable.ripple_songs));
    }

    @Override
    public int getItemCount() { // DEVUELVE EL Nº DE DATOS QUE HAY EN EL ARRAYLIST
        return songs.size();
    }


    public class SongViewHolder extends RecyclerView.ViewHolder { // MÉTODO QUE LEE LOS DATOS DE LOS POINTS
        TextView name, autor;
        ImageView imgSong;
        ImageButton btnVideo, btnState;
        RatingBar rb;
        RelativeLayout rl;
        CardView cardSong;

        public SongViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.nameSong);
            autor = itemView.findViewById(R.id.autorSong);
            imgSong = itemView.findViewById(R.id.imgSong);
            btnVideo = itemView.findViewById(R.id.videoSong);
            btnState = itemView.findViewById(R.id.btnStateSong);
            rb = itemView.findViewById(R.id.ratingBarSong);
            rl = itemView.findViewById(R.id.rlSong);
            cardSong = itemView.findViewById(R.id.cardSong);

            cardSong.setOnLongClickListener(v -> {
                if(longClickListener != null)
                    longClickListener.onLongClick(rl);
                return false;
            });

            btnVideo.setOnClickListener(v -> {
                if(clickVideoListener != null)
                    clickVideoListener.onClick(rl);
            });

            btnState.setOnClickListener(v -> {
                if(clickStateListener != null)
                    clickStateListener.onClick(rl);
            });
        }
    }
    private Filter songsFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<Song> filteredList = new ArrayList<>();
            if(constraint == null || constraint.length() == 0){
                filteredList.addAll(songsFilterList);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for(Song song : songsFilterList){
                    if(song.getName().toLowerCase().contains(filterPattern) || song.getAutor().toLowerCase().contains(filterPattern))
                        filteredList.add(song);

                    }
                }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if(songs != null){
                songs.clear();
                songs.addAll((ArrayList) results.values);
                notifyDataSetChanged();
            }
        }
    };

    @Override
    public Filter getFilter() {
        return songsFilter;
    }
}
