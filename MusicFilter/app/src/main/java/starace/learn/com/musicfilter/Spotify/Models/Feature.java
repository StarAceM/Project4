package starace.learn.com.musicfilter.Spotify.Models;

/**
 * Created by mstarace on 5/6/16.
 */
public class Feature {
        private String id;
        private String tempo;
        private String uri;

    public Feature(String id, String tempo, String uri) {
        this.id = id;
        this.tempo = tempo;
        this.uri = uri;
    }

    public String getId() {
        return id;
    }

    public Float getTempo() {

        return Float.valueOf(tempo);
    }

    public String getUri() {
        return uri;
    }
}
