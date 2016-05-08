package starace.learn.com.musicfilter.Spotify.Models;

/**
 * Created by mstarace on 5/6/16.
 */
public class Track {
    private Item[] items;
    private String offset;

    public Track(Item[] items, String offset) {
        this.items = items;
        this.offset = offset;
    }

    public Item[] getItems() {
        return items;
    }

    public String getOffset() {
        return offset;
    }
}
