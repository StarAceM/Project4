package starace.learn.com.musicfilter.Spotify.Models;

import java.util.List;

/**
 * Created by mstarace on 5/9/16.
 */
public class AudioFeatures {
    private List<Feature> audio_features;

    public AudioFeatures(List<Feature> audio_features) {
        this.audio_features = audio_features;
    }


    public List<Feature> getAudio_features() {
        return audio_features;
    }
}
