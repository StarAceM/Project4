package starace.learn.com.musicfilter.Spotify.Models;

/**
 * Created by mstarace on 5/18/16.
 */
public class ItemStart extends ItemRoot {
    private int image;
    private String title;
    private String description;

    public ItemStart( int image, String title, String description) {
        this.description = description;
        this.image = image;
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
