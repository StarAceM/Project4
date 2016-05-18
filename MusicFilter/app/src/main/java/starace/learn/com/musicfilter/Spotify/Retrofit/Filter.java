package starace.learn.com.musicfilter.Spotify.Retrofit;

import java.util.ArrayList;
import java.util.Collection;

import starace.learn.com.musicfilter.Spotify.Models.Feature;
import starace.learn.com.musicfilter.Spotify.Models.Item;

/**
 * Created by mstarace on 5/7/16.
 */

public class Filter {

    public static Collection<Item> filterLists(Collection<Item> items, Collection<Feature> features){
        Collection<Item> result = new ArrayList<>();
        for(Item curItem:items) {
            for(Feature curFeature: features){
                if (curItem.getId().equals(curFeature.getId())){
                    result.add(curItem);
                    curItem.setTempo(curFeature.getTempo());
                }
            }
        }

        return result;
    }


}
