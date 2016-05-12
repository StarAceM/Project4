package starace.learn.com.musicfilter.Song;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import starace.learn.com.musicfilter.MainActivity;
import starace.learn.com.musicfilter.R;
import starace.learn.com.musicfilter.SliderButtonListener;
import starace.learn.com.musicfilter.Spotify.Models.Album;
import starace.learn.com.musicfilter.Spotify.Models.Artist;
import starace.learn.com.musicfilter.Spotify.Models.AudioFeatures;
import starace.learn.com.musicfilter.Spotify.Models.Feature;
import starace.learn.com.musicfilter.Spotify.Models.Image;
import starace.learn.com.musicfilter.Spotify.Models.Item;
import starace.learn.com.musicfilter.Spotify.Models.RootTrack;
import starace.learn.com.musicfilter.Spotify.Retrofit.Filter;
import starace.learn.com.musicfilter.Spotify.Retrofit.SpotifyRetrofitService;

/**
 * Created by mstarace on 5/3/16.
 */
public class SongListFragment extends Fragment implements
        SliderButtonListener.UpdateAdapterOnDoubleTap {

    private static final String TAG_SONG_FRAG = "SongListFragment";
    private List<Item> songList;
    private View songFragmentView;
    final SpotifyRetrofitService.GenreSearch genreAPI = SpotifyRetrofitService.createGenre();
    SpotifyRetrofitService.FeatureSearch featureAPI;
    SongListAdapter songListAdapter;
    RecyclerView songRecyclerView;
    private String token;
    SongListAdapter.RecyclerClickEvent recyclerClickEvent;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        songFragmentView = inflater.inflate(R.layout.song_list_fragment_main, container, false);
        recyclerClickEvent = new SongListAdapter.RecyclerClickEvent() {
            @Override
            public void handleRecyclerClickEvent(int pos) {

            }
        };
        return songFragmentView;
    }

    //fake data setup
    private void setUpFakeData() {
        Image fakeImage = new Image("https://i.scdn.co/image/97d34ddb81c34eca1d033fa423381d0d9bd2a03b", "width");
        Artist fakeArtist = new Artist("Fake Artist Name");
        Item fakeItem = new Item(new Album(new Image[]{fakeImage}, "fake album name"), new Artist[]{fakeArtist},
                new String[]{"US"}, "id", "Fake Song Name", "fake uri");
        songList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            songList.add(fakeItem);
        }
    }

    public void initSongRecyclerView(boolean isFragment) {
        setUpFakeData();

        songRecyclerView = (RecyclerView) songFragmentView.findViewById(R.id.song_list_recycler_view);
        songListAdapter = new SongListAdapter(getActivity(), songList, isFragment);
        songRecyclerView.setAdapter(songListAdapter);
        songRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        songRecyclerView.setHasFixedSize(true);

    }


    public void getTrackData(final Float tempo, final Float range) {
        Log.d(TAG_SONG_FRAG, "THIS IS TEMP " + tempo + "THIS IS RANGE " + range);
        String commaListGenre = setGenreString();

        Log.d(TAG_SONG_FRAG, "THIS IS THE TOKEN PASSED TO RETROFIT SERICE " + this.token);
        featureAPI = SpotifyRetrofitService.createFeature(this.token);

        Observable<List<Item>> listGenre2 =
                createStringObservable(commaListGenre).subscribeOn(Schedulers.newThread())
            .flatMap(new Func1<List<String>, Observable<List<Item>>>() {
                @Override
                public Observable<List<Item>> call(List<String> listGenre) {

                    List<Observable<Item>> zipObservableList = new ArrayList<>();


                    for (String curGenre : listGenre) {
                        int offset = randomOffsetInt();
                        Log.d(TAG_SONG_FRAG, "THE RANDOM OFFSET IS " + offset);
                        Observable<RootTrack> genreObservable = genreAPI.tracks("genre:" + curGenre,
                                String.valueOf(offset), "track");

                        zipObservableList.add(genreObservable.flatMap(new Func1<RootTrack, Observable<Item>>() {
                            @Override
                            public Observable<Item> call(RootTrack rootTrack) {

                                return Observable.from(rootTrack.getTracks().getItems());
                            }
                        }).filter(new Func1<Item, Boolean>() {
                            @Override
                            public Boolean call(Item item) {
                                return item.getAvailable_markets().contains("US");
                            }
                        }));
                    }
                    return Observable.merge(zipObservableList).toList();
                }
            });
        // use ids from list to get BPM
        listGenre2.flatMap(new Func1<List<Item>, Observable<List<Item>>>() {
            @Override
            public Observable<List<Item>> call(final List<Item> items) {
                Log.d(TAG_SONG_FRAG, "Audio Features Called!");
                Log.d(TAG_SONG_FRAG, "Size of Item list before feature call " + items.size());
                //handle over 100 items
                List<String> totalItemIdList = new ArrayList<>();
                int itemCounter = 0;
                int listCounter;
                while (itemCounter < items.size()) {
                    listCounter = 0;
                    List<String> strItems = new ArrayList<>();
                    while (listCounter < 100 && itemCounter < items.size()){

                        strItems.add(items.get(itemCounter).getId());
                        itemCounter +=1;
                        listCounter +=1;
                    }
                    totalItemIdList.add(TextUtils.join(",",strItems));
                }
                Log.d(TAG_SONG_FRAG, "The size of the string list before feature call " + totalItemIdList.size());

                List<Observable<Item>> zipObservableItemList = new ArrayList<>();

                for (String strIds : totalItemIdList) {

                    Observable<AudioFeatures> rootFeatures = featureAPI.features(strIds);
                    Log.d(TAG_SONG_FRAG, "AudioFeatures has been created");
                    zipObservableItemList.add(rootFeatures.flatMap(new Func1<AudioFeatures,
                            Observable<Feature>>() {
                        @Override
                        public Observable<Feature> call(AudioFeatures audioFeatures) {
                            return Observable.from(audioFeatures.getAudio_features());
                        }
                    }).filter(new Func1<Feature, Boolean>() {
                        @Override
                        public Boolean call(Feature feature) {
                            return feature.getTempo() >= (tempo - range) && feature.getTempo() <= (tempo + range);
                        }
                    }).toList()
                            .flatMap(new Func1<List<Feature>, Observable<Item>>() {
                                @Override
                                public Observable<Item> call(List<Feature> features) {
                                    return Observable.from((Filter.filterLists(items, features)));
                                }
                            }));
                }

                return Observable.merge(zipObservableItemList).toList();

            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Item>>() {
                    @Override
                    public void onCompleted() {
                        SetSongItemsToMain setSongItemsToMain = (SetSongItemsToMain) getActivity();
                        setSongItemsToMain.passSongItemsToMain(songList);
                        songListAdapter.notifyItemRangeChanged(0, songList.size() - 1);
                        Log.d(TAG_SONG_FRAG, "Oncmpleted had been called END");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();

                    }

                    @Override
                    public void onNext(List<Item> items) {
                        Log.d(TAG_SONG_FRAG, "OnNext had been called");
                        songList.clear();
                        Collections.shuffle(items);
                        songList.addAll(items);

                    }
                });
    }

    private Observable<List<String>> createStringObservable(String list) {
        final List<String> genreList = Arrays.asList(list.split(","));
        return Observable.just(genreList);
    }

    @Override
    public void updateAdapterOnDoubleTap(float tempo, float range) {
        getTrackData(tempo, range);
    }

    public void setTokenFromMain(String token) {
        Log.d(TAG_SONG_FRAG, "THE TOKEN IS SET FROM MAIN " + token);
        this.token = token;
        Log.d(TAG_SONG_FRAG, "THE TOKEN IS SET FROM MAIN IN THE FRAGMENT " + token);
    }

    private int randomOffsetInt(){
         return (int) (Math.random() * 100.0);
    }

    private String setGenreString(){
        List<String> cleanGenreList = new ArrayList<>();

        SharedPreferences sharedPreferences = getActivity().
                getSharedPreferences(MainActivity.KEY_SHAREDPREF_FILE, Context.MODE_PRIVATE);

        String rawGenre = sharedPreferences.getString(MainActivity.KEY_SHARED_PREF_NOTIF,"");

        if (!rawGenre.equals("")) {
            List<String> rawGenreList = Arrays.asList(rawGenre.split(","));
            for (String curGenre: rawGenreList){
                if (curGenre.contains(" ")){
                    cleanGenreList.add("\"" + curGenre + "\"");
                } else {
                    cleanGenreList.add(curGenre);
                }
            }

        }

        return TextUtils.join(",",cleanGenreList);
    }

    public interface SetSongItemsToMain{
        void passSongItemsToMain(List<Item> listItem);
    }
//
//    public interface SetSongOnClick{
//        void setSongOnClick(int pos);
//    }

//    private void startPlayerFromPos(int pos){
//        SetSongOnClick setSongOnClick;
//        setSongOnClick = (SetSongOnClick)getActivity();
//        setSongOnClick.setSongOnClick(pos);
//    }
}
