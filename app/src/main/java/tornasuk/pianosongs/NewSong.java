package tornasuk.pianosongs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NewSong extends AppCompatDialogFragment {

    private EditText editName;
    private EditText editAutor;
    private EditText editVideo;
    private RatingBar rb;
    private DatabaseReference firebasebdd;

    public NewSong(){

    }

    private androidx.appcompat.app.AlertDialog newTranslation(){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireActivity(), R.style.DialogBackground);

        LayoutInflater li = requireActivity().getLayoutInflater();

        View v = li.inflate(R.layout.dialog_new_song, null);
        builder.setView(v);

        firebasebdd = FirebaseDatabase.getInstance().getReference();

        editName = v.findViewById(R.id.editNameSong);
        editAutor = v.findViewById(R.id.editAutorSong);
        editVideo = v.findViewById(R.id.editVideoSong);

        ImageButton btnState1 = v.findViewById(R.id.imgBtnState1);
        ImageButton btnState2 = v.findViewById(R.id.imgBtnState2);

        rb = v.findViewById(R.id.dialogRatingSong);

        btnState1.setOnClickListener(v1 -> firebasebdd.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                addNewSong(snapshot, false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }));

        btnState2.setOnClickListener(v12 -> firebasebdd.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                addNewSong(snapshot, true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }));

        return builder.create();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return newTranslation();
    }

    private void addNewSong(DataSnapshot snapshot, boolean learned){
        String nameSong = editName.getText().toString();
        String autorSong = editAutor.getText().toString();
        String linkVideoSong = editVideo.getText().toString();
        float ratingSong = rb.getRating();

        if(!nameSong.equals("") && !autorSong.equals("")) {
            int idMax = 0;
            for (DataSnapshot songDb : snapshot.getChildren()){
                Song song = songDb.getValue(Song.class);
                int songId = song.getId();

                if (songId > idMax) idMax = songId;
            }

            if (nameSong.contains("."))
                nameSong = nameSong.replace(".", "-");

            Song newSong = new Song(idMax + 1, nameSong, autorSong, linkVideoSong, learned, ratingSong);
            firebasebdd.child(nameSong).setValue(newSong);
            dismiss();
        } else
            Toast.makeText(getActivity(), "Inserta datos válidos", Toast.LENGTH_SHORT).show();
    }

}
