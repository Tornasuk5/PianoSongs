package tornasuk.pianosongs;

import java.io.Serializable;

public class Song implements Serializable {

    private int id;
    private String name;
    private String autor;
    private String linkVideo;
    private boolean learned;
    private float rating;

    public Song(int id, String name, String autor, String linkVideo, boolean learned, float rating) {
        this.id = id;
        this.name = name;
        this.autor = autor;
        this.linkVideo = linkVideo;
        this.learned = learned;
        this.rating = rating;
    }

    public Song() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getLinkVideo() {
        return linkVideo;
    }

    public void setLinkVideo(String linkVideo) {
        this.linkVideo = linkVideo;
    }

    public boolean isLearned() {
        return learned;
    }

    public void setLearned(boolean learned) {
        this.learned = learned;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}
