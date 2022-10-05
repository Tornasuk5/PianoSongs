package tornasuk.pianosongs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Patterns;
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

public class EditSong extends AppCompatDialogFragment {

    private EditText editName;
    private EditText editAutor;
    private EditText editVideo;
    private RatingBar rb;
    private DatabaseReference firebasebdd;
    private Song song;

    public EditSong(){

    }

    private androidx.appcompat.app.AlertDialog newTranslation(){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireActivity(), R.style.DialogBackground);

        LayoutInflater li = requireActivity().getLayoutInflater();

        View v = li.inflate(R.layout.dialog_new_song, null);
        builder.setView(v);

        song = (Song) getArguments().get("EXTRA_SONG");

        firebasebdd = FirebaseDatabase.getInstance().getReference();

        editName = v.findViewById(R.id.editNameSong);
        editAutor = v.findViewById(R.id.editAutorSong);
        editVideo = v.findViewById(R.id.editVideoSong);

        rb = v.findViewById(R.id.dialogRatingSong);

        ImageButton btnState1 = v.findViewById(R.id.imgBtnState1);
        ImageButton btnState2 = v.findViewById(R.id.imgBtnState2);

        editName.setText(song.getName());
        editAutor.setText(song.getAutor());
        editVideo.setText(song.getLinkVideo());

        rb.setRating(song.getRating());

        btnState1.setOnClickListener(v1 -> firebasebdd.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                editSong(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }));

        btnState2.setOnClickListener(v12 -> firebasebdd.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                editSong(true);
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

    private void editSong(boolean learned){
        String nameSong = editName.getText().toString();
        String autorSong = editAutor.getText().toString();
        String linkVideoSong = editVideo.getText().toString();
        float ratingSong = rb.getRating();

        if(!nameSong.equals("") && !autorSong.equals("")) {
            if (nameSong.contains("."))
                nameSong = nameSong.replace(".", "-");

            Song editedSong = new Song(song.getId(), nameSong, autorSong, linkVideoSong, learned, ratingSong);
            firebasebdd.child(nameSong).setValue(editedSong);

            if (!song.getName().equals(nameSong))
                firebasebdd.child(song.getName()).removeValue();

            dismiss();
        } else
            Toast.makeText(getActivity(), "Inserta datos válidos", Toast.LENGTH_SHORT).show();
    }
}
