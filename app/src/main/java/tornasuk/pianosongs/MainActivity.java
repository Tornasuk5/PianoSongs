package tornasuk.pianosongs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference firebasebdd;
    private RecyclerView rvSongs;
    private SongsAdapter songsAdapter;
    private ArrayList<Song> songs;
    private ArrayList<Song> songsOrd;
    private FloatingActionButton fabAddSong;
    private SearchView sv;
    private ProgressBar progBarMain;
    private boolean sortSongs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        songs = new ArrayList<>();
        songsOrd = new ArrayList<>();

        progBarMain = findViewById(R.id.progBarMain);

        rvSongs = findViewById(R.id.rvSongs);
        rvSongs.setHasFixedSize(true);
        rvSongs.setLayoutManager(new LinearLayoutManager(this));

        fabAddSong = findViewById(R.id.fabAddSong);

        fabAddSong.setOnClickListener(v -> {
            NewSong newSong = new NewSong();
            newSong.show(getSupportFragmentManager(), "newSong");
        });

        firebasebdd = FirebaseDatabase.getInstance().getReference();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInAnonymously();

        fabAddSong.setOnLongClickListener(view -> {
            auth.signInWithEmailAndPassword("loki_mousou@hotmail.com", "tornasukpass1234")
                    .addOnSuccessListener(authResult -> {
                        Toast.makeText(getApplicationContext(), "Permiso Tornasuk - Activado", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getApplicationContext(), "No se ha podido iniciar sesión", Toast.LENGTH_SHORT).show());
            return false;
        });

        checkSheetsFolder();

        loadPianoSongs(false);
    }

    private void loadPianoSongs(final boolean sort){
            firebasebdd.orderByChild("id").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    progBarMain.setVisibility(View.VISIBLE);

                    if(!songs.isEmpty())
                        songs.clear();

                    if(!songsOrd.isEmpty())
                        songsOrd.clear();

                    for(DataSnapshot songsSnapshot : snapshot.getChildren()){
                        Song song = songsSnapshot.getValue(Song.class);
                        if (sort) {
                            if (!song.isLearned()) songs.add(song);
                        } else songs.add(song);
                    }

                    for(int i = songs.size()-1; i>=0; i--) { songsOrd.add(songs.get(i)); }

                    songsAdapter = new SongsAdapter(getApplicationContext(), songsOrd);
                    rvSongs.setAdapter(songsAdapter);

                    LayoutAnimationController animController = AnimationUtils.loadLayoutAnimation(rvSongs.getContext(), R.anim.rv_songs_animation);
                    rvSongs.setLayoutAnimation(animController);

                    rvSongs.getRecycledViewPool().setMaxRecycledViews(0, 0);

                    progBarMain.setVisibility(View.INVISIBLE);

                    ItemTouchHelper ith = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                        @Override
                        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                            return false;
                        }

                        @Override
                        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                            firebasebdd.child(songsOrd.get(rvSongs.getChildAdapterPosition(viewHolder.itemView)).getName()).removeValue();
                        }
                    });
                    ith.attachToRecyclerView(rvSongs);

                    songsAdapter.setOnClickVideoListener(v -> {
                        String urlVideo = songsOrd.get(rvSongs.getChildAdapterPosition(v)).getLinkVideo();
                        if (!urlVideo.equals("")) {
                            if (urlVideo.contains("youtu")) {
                                Uri webpage = Uri.parse(urlVideo);
                                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                                startActivity(intent);
                            } else {
                                File pdf = new File(getExternalFilesDir("Sheets").getAbsolutePath(), urlVideo);
                                if (pdf.exists()) {
                                    Uri pdfUri = FileProvider.getUriForFile(getApplicationContext(), "tornasuk.pianosongs.fileprovider", pdf);
                                    Intent intentFile = new Intent(Intent.ACTION_VIEW);
                                    intentFile.setDataAndType(pdfUri, "application/pdf");
                                    intentFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    intentFile.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    try {
                                        startActivity(intentFile);
                                    } catch (ActivityNotFoundException e) {
                                        Toast.makeText(getApplicationContext(), "No se ha podido abrir el archivo pdf", Toast.LENGTH_SHORT).show();
                                    }
                                } else
                                    Toast.makeText(getApplicationContext(), "No se ha encontrado el archivo pdf", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    songsAdapter.setOnClickStateListener(v -> {
                        boolean learned = false;
                        if(!songsOrd.get(rvSongs.getChildAdapterPosition(v)).isLearned())
                            learned = true;

                        firebasebdd.child(songsOrd.get(rvSongs.getChildAdapterPosition(v)).getName()).child("learned").setValue(learned);
                    });

                    songsAdapter.setOnLongClickListener(v -> {
                        EditSong editSong = new EditSong();
                        Bundle args = new Bundle();
                        args.putSerializable("EXTRA_SONG", songsOrd.get(rvSongs.getChildAdapterPosition(v)));
                        editSong.setArguments(args);
                        editSong.show(getSupportFragmentManager(), "editSong");
                        return false;
                    });

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

    }

    private void checkSheetsFolder(){
        boolean isPartiturasCreated = false;
        try {
            String[] dirs = getExternalFilesDir("").list();
            if(dirs.length > 0)
                isPartiturasCreated = true;
        } catch (NullPointerException npx){

        }
        if(!isPartiturasCreated) {
            File pdfsPath = new File(getExternalFilesDir("Sheets").getAbsolutePath());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
            menu.findItem(R.id.bar_search).setVisible(true);

            MenuItem searchPoint = menu.findItem(R.id.bar_search);
            sv = (SearchView) searchPoint.getActionView();

            sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    songsAdapter.getFilter().filter(newText);
                    return false;
                }
            });

        menu.getItem(0).setOnMenuItemClickListener(item -> {
            if(sortSongs) {
                sortSongs = false;
                menu.getItem(0).setIcon(R.drawable.hora2);
                loadPianoSongs(false);
            } else {
                sortSongs = true;
                menu.getItem(0).setIcon(R.drawable.piano2);
                loadPianoSongs(true);
            }

            return false;
        });

        super.onPrepareOptionsMenu(menu);
        return true;
    }
}