package starace.learn.com.musicfilter.Spotify.Models;

/**
 * Created by mstarace on 5/6/16.
 */
public class Album {
    private Image[] images;
    private String name;

    public Album(Image[] images, String name) {
        this.images = images;
        this.name = name;
    }

    public Image[] getImages() {
        return images;
    }

    public String getName() {
        return name;
    }


}
