package starace.learn.com.musicfilter.Spotify.Models;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by mstarace on 5/6/16.
 */
public class Item {
    private Album album;
    private String[] available_markets;
    private Artist[] artists;
    private String name;
    private String id;
    private String uri;

    public Item(Album album, Artist[] artists, String[] available_markets, String id, String name, String uri) {
        this.album = album;
        this.artists = artists;
        this.available_markets = available_markets;
        this.id = id;
        this.name = name;
        this.uri = uri;
    }

    public Album getAlbum() {
        return album;
    }

    public Artist[] getArtists() {
        return artists;
    }

    public ArrayList<String> getAvailable_markets() {
        ArrayList<String> markets = new ArrayList<>(Arrays.asList(available_markets));
        return markets;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }
}
