package starace.learn.com.musicfilter.Spotify.Models;

/**
 * Created by mstarace on 5/9/16.
 */
public class AudioFeatures {
    private Feature[] audio_features;

    public AudioFeatures(Feature[] audio_features) {
        this.audio_features = audio_features;
    }


    public Feature[] getAudio_features() {
        return audio_features;
    }
}
