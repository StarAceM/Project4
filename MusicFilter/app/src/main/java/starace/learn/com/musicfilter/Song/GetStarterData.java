package starace.learn.com.musicfilter.Song;

import android.content.res.Resources;

import java.util.ArrayList;
import java.util.List;

import starace.learn.com.musicfilter.R;
import starace.learn.com.musicfilter.Spotify.Models.ItemRoot;
import starace.learn.com.musicfilter.Spotify.Models.ItemStart;

/**
 * Created by mstarace on 5/18/16.
 */
public class GetStarterData{
    private Resources res;

    public GetStarterData(Resources res) {
        this.res = res;
    }

    /**
     * List of item roots to initially set the top recyclerView with
     * welcome data
     * @return
     */
    public   List<ItemRoot> getWelcomeList(){
        List<ItemRoot> rootList = new ArrayList<>();
        rootList.add(new ItemStart(R.drawable.welcome_image, res.getString(R.string.welcome_title),
                res.getString(R.string.welcome_description)));
        return rootList;
    }

    /**
     * List of item roots to initially set the bottom recyclerView
     * with user guide information
     * @return
     */
    public    List<ItemRoot> getGuideData(){
        List<ItemRoot> rootList = new ArrayList<>();
        rootList.add(new ItemStart(R.drawable.nav_image,res.getString(R.string.guide_nav_title),res.getString(R.string.guide_nav_description)));
        rootList.add(new ItemStart(R.drawable.small_button_arrows,res.getString(R.string.bpm_value_title),res.getString(R.string.bpm_value_description)));
        rootList.add(new ItemStart(R.drawable.big_button_arrows,res.getString(R.string.bpm_range_title),res.getString(R.string.bpm_range_description)));
        rootList.add(new ItemStart(R.drawable.button_double_click,res.getString(R.string.bpm_start_title),res.getString(R.string.bpm_start_description)));

        return rootList;
    }
}
