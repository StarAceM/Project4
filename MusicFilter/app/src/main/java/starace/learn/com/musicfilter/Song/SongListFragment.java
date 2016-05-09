package starace.learn.com.musicfilter.Song;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import starace.learn.com.musicfilter.R;
import starace.learn.com.musicfilter.SliderButtonListener;
import starace.learn.com.musicfilter.Spotify.Models.Album;
import starace.learn.com.musicfilter.Spotify.Models.Artist;
import starace.learn.com.musicfilter.Spotify.Models.Feature;
import starace.learn.com.musicfilter.Spotify.Models.Image;
import starace.learn.com.musicfilter.Spotify.Models.Item;
import starace.learn.com.musicfilter.Spotify.Models.RootTrack;
import starace.learn.com.musicfilter.Spotify.Retrofit.Filter;
import starace.learn.com.musicfilter.Spotify.Retrofit.SpotifyRetrofitService;

/**
 * Created by mstarace on 5/3/16.
 */
public class SongListFragment extends Fragment implements SongListAdapter.RecyclerClickEvent,
        SliderButtonListener.UpdateAdapterOnDoubleTap{
    private static final String TAG_SONG_FRAG = "SongListFragment";
    private List<Item> songList;
    private View songFragmentView;
    final SpotifyRetrofitService.GenreSearch genreAPI = SpotifyRetrofitService.createGenre();
    final SpotifyRetrofitService.FeatureSearch featureAPI = SpotifyRetrofitService.createFeature();
    List<Item> itemList = new ArrayList<>();
    SongListAdapter songListAdapter;
    RecyclerView songRecyclerView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        songFragmentView = inflater.inflate(R.layout.song_list_fragment_main,container,false);
        return songFragmentView;
    }

    //fake data setup
    private void setUpFakeData(){
        Image fakeImage = new Image("https://i.scdn.co/image/97d34ddb81c34eca1d033fa423381d0d9bd2a03b","width");
        Artist fakeArtist = new Artist("Fake Artist Name");
        Item fakeItem = new Item(new Album(new Image[]{fakeImage},"fake album name"),new Artist[]{fakeArtist},
                new String[]{"US"},"id","Fake Song Name","fake uri");
        songList = new ArrayList<>();
        for (int i =0; i < 10; i++){
            songList.add(fakeItem);
        }
    }

    public void initSongRecyclerView(boolean isFragment){
        setUpFakeData();

        songRecyclerView = (RecyclerView) songFragmentView.findViewById(R.id.song_list_recycler_view);
        songListAdapter = new SongListAdapter(getActivity(), songList,  isFragment);
        songRecyclerView.setAdapter(songListAdapter);
        songRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        songRecyclerView.setHasFixedSize(true);

    }


    @Override
    public void handleRecyclerClickEvent(String song) {
        Log.d(TAG_SONG_FRAG, "This is a recycler click event in the SongFragment " + song);
    }

//

    public void getTrackData(){

        //todo need to make this work for a hashmap
        //need to setadpater and queue for the player
        //start player

        //create fake input for retrofit
        List<String> strGenres = new ArrayList<>();
        strGenres.add("electronic");
        strGenres.add("rock");
        List<String> strOffsets = new ArrayList<>();
        strOffsets.add("0");
        strOffsets.add("1");

        Map<String,Integer> genreMap = new HashMap<>();
        genreMap.put("electronic", 0);
        genreMap.put("rock", 1);

        //want to use a hashMap of values instead of a single entry here

        Observable<RootTrack> genreObservable = genreAPI.tracks("genre:electronic","1","track");
        genreObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Func1<RootTrack, Observable<List<Item>>>() {
                    @Override
                    public Observable<List<Item>> call(RootTrack rootTrack) {
                        List<Item> trackItems = new ArrayList<>();
                        for (Item curItem : rootTrack.getTracks()[0].getItems()) {
//                      //filter items for us market
                            if (curItem.getAvailable_markets().contains("US")) {
                                trackItems.add(curItem);
                            }
                        }
                        itemList = trackItems;
                        return Observable.just(trackItems);

                    }
                })
                        // use ids from list to get BPM
                .flatMap(new Func1<List<Item>, Observable<List<Feature>>>() {
                    @Override
                    public Observable<List<Feature>> call(List<Item> items) {
                        String strIds = "";
                        for (int i = 0; i < items.size(); i++) {
                            if (i < items.size() - 1) {
                                strIds = strIds + items.get(i).getId() + ",";
                            } else {
                                strIds = strIds + items.get(i).getId();
                            }
                        }
                        Observable<List<Feature>> featureObservable = featureAPI.features(strIds)
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread());

                        return featureObservable;
                    }
                })
                .flatMap(new Func1<List<Feature>, Observable<List<Item>>>() {
                    @Override
                    public Observable<List<Item>> call(List<Feature> features) {
                        List<Item> filteredItemList = new ArrayList<Item>();
                        Collection<Feature> filteredFeatureList = Filter.isCorrectTempo(features);
                        filteredItemList = (List) Filter.filterLists(itemList, filteredFeatureList);

                        return Observable.just(filteredItemList);
                    }
                })
                .subscribe(new Subscriber<List<Item>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<Item> items) {
                        Log.d(TAG_SONG_FRAG,"The subscriber onNext has been called");
                        songList.clear();
                        songList.addAll(items);
                        songListAdapter.notifyDataSetChanged();
                    }
                });

    }

    @Override
    public void updateAdapterOnDoubleTap() {
        getTrackData();
    }
}
