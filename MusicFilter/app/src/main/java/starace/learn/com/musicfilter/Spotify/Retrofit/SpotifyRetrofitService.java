package starace.learn.com.musicfilter.Spotify.Retrofit;

import java.io.IOException;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import starace.learn.com.musicfilter.MainActivity;
import starace.learn.com.musicfilter.Spotify.Models.Feature;
import starace.learn.com.musicfilter.Spotify.Models.RootTrack;

import static okhttp3.logging.HttpLoggingInterceptor.Level;

/**
 * Created by mstarace on 5/8/16.
 */
public class SpotifyRetrofitService {

    public static final String spotifyGenre_URL = "https://api.spotify.com/v1/";
    public static final String spotifyFeature_URL = "https://api.spotify.com/v1/audio-features/";
    private static final String token = MainActivity.token;
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
    private static final HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

    public interface GenreSearch {
        @GET("search")
        Observable<RootTrack> tracks (
                @Query("query") String query,
                @Query("offset") String offset,
                @Query("type") String type);
    }

    public static GenreSearch createGenre(){
        // set desired log level
        logging.setLevel(Level.BODY);

        httpClient.addInterceptor(logging);

        return new Retrofit.Builder()
                .baseUrl(spotifyGenre_URL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build()
                .create(SpotifyRetrofitService.GenreSearch.class);
    }

    public interface FeatureSearch{
        @GET("")
        Observable<List<Feature>> features(
                @Query("ids") String idString);
    }

    public static FeatureSearch createFeature(){
        if (token != null) {
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Interceptor.Chain chain) throws IOException {
                    Request original = chain.request();

                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Accept", "application/json")
                            .header("Authorization",
                                    "BEARER" + " " + token)
                            .method(original.method(), original.body());

                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            });
        }

        OkHttpClient client = httpClient.build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(spotifyFeature_URL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client).build();
        return retrofit.create(SpotifyRetrofitService.FeatureSearch.class);
    }

}
