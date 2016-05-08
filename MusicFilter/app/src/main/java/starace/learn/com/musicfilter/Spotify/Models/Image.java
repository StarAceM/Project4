package starace.learn.com.musicfilter.Spotify.Models;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by mstarace on 5/6/16.
 */
public class Image {
    private String url;
    private String width;
    private URL imageURL;

    public Image(String url, String width) {
        this.url = url;
        this.width = width;
    }

    public URL getImageURL() {
        try {
            this.imageURL = new URL(url);
        } catch (MalformedURLException e){
            e.printStackTrace();
        }
        return imageURL;
    }

    public String getWidth() {
        return width;
    }
}
