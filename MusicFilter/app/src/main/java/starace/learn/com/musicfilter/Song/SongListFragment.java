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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import starace.learn.com.musicfilter.R;

/**
 * Created by mstarace on 5/3/16.
 */
public class SongListFragment extends Fragment implements SongListAdapter.RecyclerClickEvent{
    private static final String TAG_SONG_FRAG = "SongListFragment";
    private List<Song> songList;
    private View songFragmentView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        songFragmentView = inflater.inflate(R.layout.song_list_fragment_main,container,false);
        return songFragmentView;
    }

    //fake data setup
    private void setUpfakeData(){
        songList = new ArrayList<>();
        for (int i =0; i < 10; i++){
            songList.add(new Song("OUCH","The detail of the song will definitely go here",R.drawable.ouch, null));
        }
    }

    public void initSongRecyclerView(boolean isFragment){
        setUpfakeData();

        RecyclerView songRecyclerView = (RecyclerView) songFragmentView.findViewById(R.id.song_list_recycler_view);
        SongListAdapter songListAdapter = new SongListAdapter(getActivity(), songList,  isFragment);
        songRecyclerView.setAdapter(songListAdapter);
        songRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        songRecyclerView.setHasFixedSize(true);

    }


    @Override
    public void handleRecyclerClickEvent(URL song) {
        Log.d(TAG_SONG_FRAG, "This is a recycler click event in the SongFragment " + song);
    }
}
