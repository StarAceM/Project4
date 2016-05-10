package starace.learn.com.musicfilter.Spotify.Models;

/**
 * Created by mstarace on 5/9/16.
 */
public class RootTrack {
    private Track tracks;

    public RootTrack(Track tracks) {
        this.tracks = tracks;
    }

    public Track getTracks() {
        return tracks;
    }
}
