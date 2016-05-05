package starace.learn.com.musicfilter.Song;

import java.net.URL;

/**
 * Created by mstarace on 5/4/16.
 */
public class Song {
    private String Title;
    private String detail;
    private int image;
    private URL song;

    public Song() {
    }

    public Song(String title,String detail, int image, URL song) {
        this.detail = detail;
        this.image = image;
        this.song = song;
        this.Title = title;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public URL getSong() {
        return song;
    }

    public void setSong(URL song) {
        this.song = song;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

}
