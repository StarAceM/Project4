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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import starace.learn.com.musicfilter.MainActivity;
import starace.learn.com.musicfilter.R;
import starace.learn.com.musicfilter.SliderButtonListener;
import starace.learn.com.musicfilter.Spotify.Models.AudioFeatures;
import starace.learn.com.musicfilter.Spotify.Models.Feature;
import starace.learn.com.musicfilter.Spotify.Models.Item;
import starace.learn.com.musicfilter.Spotify.Models.ItemRoot;
import starace.learn.com.musicfilter.Spotify.Models.RootTrack;
import starace.learn.com.musicfilter.Spotify.Retrofit.Filter;
import starace.learn.com.musicfilter.Spotify.Retrofit.SpotifyRetrofitService;

/**
 * Created by mstarace on 5/3/16.
 */
public class SongListFragment extends Fragment implements
        SliderButtonListener.UpdateAdapterOnDoubleTap{

    private static final String TAG_SONG_FRAG = "SongListFragment";
    private List<ItemRoot> songList;
    private View songFragmentView;
    final SpotifyRetrofitService.GenreSearch genreAPI = SpotifyRetrofitService.createGenre();
    SpotifyRetrofitService.FeatureSearch featureAPI;
    SongListAdapter songListAdapter;
    RecyclerView songRecyclerView;
    private String token;
    private boolean isNew;
    private Map<String,Integer> totalSongsGenreMap;
    private Map<String, List<Integer>> offsetMap;
    private Map<String, List<Integer>> offsetLimitMap;
    private List<String> genreListString;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        songFragmentView = inflater.inflate(R.layout.song_list_fragment_main, container, false);
        return songFragmentView;
    }

    public void initSongRecyclerView(boolean isFragment) {
        songList = new ArrayList<>();
        GetStarterData getData = new GetStarterData(this.getResources());
        if(isFragment){
            setUpOffsetMangerMaps();
            songList = getData.getWelcomeList();
        } else {
            songList = getData.getGuideData();
        }

        songRecyclerView = (RecyclerView) songFragmentView.findViewById(R.id.song_list_recycler_view);
        songListAdapter = new SongListAdapter(this, songList, isFragment);
        songRecyclerView.setAdapter(songListAdapter);
        songRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        songRecyclerView.setHasFixedSize(true);

    }

    private String getNavDrawerPreferences() {

        SharedPreferences sharedPreferences = getActivity().
                getSharedPreferences(MainActivity.KEY_SHAREDPREF_FILE, Context.MODE_PRIVATE);

        return sharedPreferences.getString(MainActivity.KEY_SHARED_PREF_NOTIF, "");
    }

    //Magic happens here

    public void getTrackData(final Float tempo, final Float range) {
        Log.d(TAG_SONG_FRAG, "THIS IS TEMP " + tempo + "THIS IS RANGE " + range);
        String commaListGenre = setGenreString(getNavDrawerPreferences());

        Log.d(TAG_SONG_FRAG, "THIS IS THE TOKEN PASSED TO RETROFIT SERICE " + this.token);
        featureAPI = SpotifyRetrofitService.createFeature(this.token);

        Observable<List<Item>> listGenre2 =
                createStringObservable(commaListGenre).subscribeOn(Schedulers.newThread())
                        .flatMap(new Func1<List<String>, Observable<List<Item>>>() {
                            @Override
                            public Observable<List<Item>> call(List<String> listGenre) {

                                List<Observable<Item>> zipObservableList = new ArrayList<>();

                                for (String curGenre : listGenre) {

                                    for (int i = 0; i < offsetMap.get(curGenre).size(); i++) {
                                        int offset = offsetMap.get(curGenre).get(i);
                                        int limit = offsetLimitMap.get(curGenre).get(i);
                                        Log.d(TAG_SONG_FRAG, "THE Mapped OFFSET IS " + offset + " AT LIST POS " + i + " Limit = " + limit);
                                        Log.d(TAG_SONG_FRAG, "THE Genre is " + curGenre);

                                        //add a look to get all tracks of a genre
                                        Observable<RootTrack> genreObservable = genreAPI.tracks("genre:" + curGenre,
                                                String.valueOf(offset), String.valueOf(limit), "track");

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
                    while (listCounter < 100 && itemCounter < items.size()) {

                        strItems.add(items.get(itemCounter).getId());
                        itemCounter += 1;
                        listCounter += 1;
                    }
                    totalItemIdList.add(TextUtils.join(",", strItems));
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

                        onGetTrackCompleted();
                        Log.d(TAG_SONG_FRAG, "Oncmpleted had been called END");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();

                    }

                    @Override
                    public void onNext(List<Item> items) {
                        Log.d(TAG_SONG_FRAG, "OnNext had been called and the number of Items is " + items.size());
                        if (isNew) {
                            songList.clear();
                            Collections.shuffle(items);
                            songList.addAll(items);
                        } else if (items.size() > 0) {
                            for (Item curItem : items) {
                                Log.d(TAG_SONG_FRAG, "This is the song name " + curItem.getName());
                            }
                            Collections.shuffle(items);
                            Log.d(TAG_SONG_FRAG, "This is the size of the songList before adding " + songList.size());
                            songList.addAll(items);
                            Log.d(TAG_SONG_FRAG, "This is the size of the songList after adding " + songList.size());

                        } else {
                            Log.d(TAG_SONG_FRAG, "No items found in OnNext");
                        }
                    }
                });
    }

    private Observable<List<String>> createStringObservable(String list) {
        final List<String> genreList = Arrays.asList(list.split(","));
        return Observable.just(genreList);
    }

    private void onGetTrackCompleted() {
        if (isNew) {
            songRecyclerView.smoothScrollToPosition(0);
            Log.d(TAG_SONG_FRAG, "IS NEW IS TRUE");
        }

        SetSongItemsToMain setSongItemsToMain = (SetSongItemsToMain) getActivity();
        setSongItemsToMain.passSongItemsToMain(castRootAsItem(songList));
        if (songList.size() > 0) {
            songListAdapter.notifyDataSetChanged();
            songListAdapter.notifyItemRangeChanged(0, songList.size() - 1);
            songRecyclerView.invalidate();
        }
        isNew = false;
    }

    private List<Item> castRootAsItem(List<ItemRoot> rootList) {
        List<Item> itemList = new ArrayList<>();
        for(ItemRoot root: rootList){
            itemList.add((Item)root);
        }
        return itemList;
    }

    @Override
    public void updateAdapterOnDoubleTap(float tempo, float range) {
        isNew = true;
        getTrackData(tempo, range);
    }

    public void setTokenFromMain(String token) {
        this.token = token;
        Log.d(TAG_SONG_FRAG, "THE TOKEN IS SET FROM MAIN IN THE FRAGMENT " + token);
    }

    private String setGenreString(String rawGenre) {
        List<String> cleanGenreList = new ArrayList<>();

        if (!rawGenre.equals("")) {
            List<String> rawGenreList = Arrays.asList(rawGenre.split(","));
            for (String curGenre : rawGenreList) {
                if (curGenre.contains(" ")) {
                    cleanGenreList.add("\"" + curGenre + "\"");
                } else {
                    cleanGenreList.add(curGenre);
                }
            }

        }

        return TextUtils.join(",", cleanGenreList);
    }

    public interface SetSongItemsToMain {
        void passSongItemsToMain(List<Item> listItem);
    }

    private void setUpOffsetMangerMaps() {
        offsetMap = new HashMap<>();
        offsetLimitMap = new HashMap<>();
        totalSongsGenreMap = new HashMap<>();
        genreListString = new ArrayList<>();

        String[] rawList = getResources().getStringArray(R.array.genre);
        for (int i = 1; i < rawList.length; i++){
            genreListString.add(rawList[i]);
        }

        Log.d(TAG_SONG_FRAG, "The Loop has been called to get song totals");
        getSongTotalsGenre(genreListString);

    }

    private void setUpOffsetList(String genre, int total) {
        List<Integer> offsetList = new ArrayList<>();
        List<Integer> limitList = new ArrayList<>();
        if (total > 0) {
            if (total/50 != 0) {

                int offsetCounter = 0;
                for (int i = 0; i <= total/50; i++) {
                    offsetCounter = i* 50;
                    if(total%50 ==0 && i == total/50) {

                    } else if(i == total/50) {
                        limitList.add(total - (i*50));
                        offsetList.add(offsetCounter);
                    } else {
                        offsetList.add(offsetCounter);
                        limitList.add(50);
                    }

                }


            } else if(total%50 != 0){
                //handle thee case where the total is under 50
                limitList.add(total % 50);
                offsetList.add(0);

            }

            offsetMap.put(genre,offsetList);
            offsetLimitMap.put(genre,limitList);
            Log.d(TAG_SONG_FRAG, "This is the offsetMap size " + offsetList.size());
            Log.d(TAG_SONG_FRAG, "This is the offsetLimitMap size " + limitList.size());

        }
    }


    private void getSongTotalsGenre(final List<String> genreList){
        //make api calls to get totals for all genres

        String commaListGenre = setGenreString(TextUtils.join(",",genreList));

        Observable<List<Integer>> listGenreTotals =
                createStringObservable(commaListGenre).subscribeOn(Schedulers.newThread())
                        .flatMap(new Func1<List<String>, Observable<List<Integer>>>() {
                            @Override
                            public Observable<List<Integer>> call(List<String> listGenre) {

                                List<Observable<Integer>> zipObservableList = new ArrayList<>();

                                for (String curGenre : listGenre) {
                                    Log.d(TAG_SONG_FRAG, "The roottrack root has bee called at " + curGenre);
                                    Observable<RootTrack> genreObservable = genreAPI.tracks("genre:" + curGenre,
                                            "0", "1", "track");

                                    zipObservableList.add(genreObservable.flatMap(new Func1<RootTrack, Observable<Integer>>() {
                                        @Override
                                        public Observable<Integer> call(RootTrack rootTrack) {
                                            Log.d(TAG_SONG_FRAG, "the int total is " + rootTrack.getTracks().getTotal());
                                            return Observable.just(Integer.valueOf(rootTrack.getTracks().getTotal()));
                                        }
                                    }));
                                }

                                return Observable.merge(zipObservableList).toList();
                            }
                        });

        listGenreTotals
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<List<Integer>>() {
                @Override
                public void onCompleted() {
                    for(String genre : genreList){
                        setUpOffsetList(genre,totalSongsGenreMap.get(genre));
                    }

                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onNext(List<Integer> integers) {
                    for (int i = 0; i < integers.size(); i++) {
                        totalSongsGenreMap.put(genreList.get(i),integers.get(i));
                        Log.d(TAG_SONG_FRAG, "These are the song totals " + integers.get(i));
                    }
                }
            });

    }

}
