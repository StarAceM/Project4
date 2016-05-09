package starace.learn.com.musicfilter.Spotify.Models;

/**
 * Created by mstarace on 5/6/16.
 */
public class Image {
    private String url;
    private String width;

    public Image(String url, String width) {
        this.url = url;
        this.width = width;
    }

    public String getImageURL() {
       return this.url;
    }

    public String getWidth() {
        return width;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
