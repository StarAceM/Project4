package starace.learn.com.musicfilter.Spotify.Models;

/**
 * Created by mstarace on 5/6/16.
 */
public class Track {
    private Item[] items;
    private String offset;
    private String total;

    public Track(Item[] items, String offset, String total) {
        this.items = items;
        this.offset = offset;
        this.total = total;

    }

    public Item[] getItems() {
        return items;
    }

    public String getOffset() {
        return offset;
    }

    public String getTotal() {
        return total;
    }
}
