package starace.learn.com.musicfilter.Spotify.Retrofit;

import android.util.Log;

import java.io.IOException;

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
import starace.learn.com.musicfilter.Spotify.Models.AudioFeatures;
import starace.learn.com.musicfilter.Spotify.Models.RootTrack;

import static okhttp3.logging.HttpLoggingInterceptor.Level;

/**
 * Created by mstarace on 5/8/16.
 */
public class SpotifyRetrofitService {

    public static final String spotifyGenre_URL = "https://api.spotify.com/v1/";
    public static final String spotifyFeature_URL = "https://api.spotify.com/v1/";
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
    private static final HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

    public interface GenreSearch {
        @GET("search")
        Observable<RootTrack> tracks (
                @Query("query") String query,
                @Query("offset") String offset,
                @Query("limit") String limit,
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
        @GET("audio-features")
        Observable<AudioFeatures> features(
                @Query("ids") String idString);
    }

    public static FeatureSearch createFeature(String token){
        final String featureToken = token;
        if (featureToken != null) {

            httpClient.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Interceptor.Chain chain) throws IOException {
                    Request original = chain.request();
                    Log.d("RETROFIT", "THIS IS THE TOKEN " + featureToken);
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Accept", "application/json")
                            .header("Authorization",
                                    "Bearer" + " " + featureToken)
                            .method(original.method(), original.body());

                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            });
            logging.setLevel(Level.BODY);
            httpClient.addInterceptor(logging);

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
