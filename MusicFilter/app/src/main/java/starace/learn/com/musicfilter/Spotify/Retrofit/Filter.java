package starace.learn.com.musicfilter.Spotify.Retrofit;

import com.android.internal.util.Predicate;

import java.util.ArrayList;
import java.util.Collection;

import starace.learn.com.musicfilter.MainActivity;
import starace.learn.com.musicfilter.Spotify.Models.Feature;
import starace.learn.com.musicfilter.Spotify.Models.Item;

/**
 * Created by mstarace on 5/7/16.
 */

public class Filter {
    private static final String currentMarket = "US";
    private static final float range = MainActivity.range;
    private static final float tempo = MainActivity.tempo;

    public static Collection<Item> isPlayable (Collection<Item> items){

        return filter(items, isPlayable);
    }

    private static Predicate<Item> isPlayable = new Predicate<Item>() {
        @Override
        public boolean apply(Item items) {
            return items.getAvailable_markets().contains(currentMarket);
        }
    };

    public static Collection<Feature> isCorrectTempo (Collection<Feature> features){
        return filter(features,isCorrectTempo);
    }

    private static Predicate<Feature> isCorrectTempo = new Predicate<Feature>() {
        @Override
        public boolean apply(Feature features) {
            return features.getTempo() >= (tempo - range) && features .getTempo() <= (tempo + range) ;
        }
    };


    public static <T> Collection<T> filter(Collection<T> target, Predicate<T> predicate) {
        Collection<T> result = new ArrayList<T>();
        for (T element: target) {
            if (predicate.apply(element)) {
                result.add(element);
            }
        }
        return result;
    }

    public static Collection<Item> filterLists(Collection<Item> items, Collection<Feature> features){
        Collection<Item> result = new ArrayList<>();
        for(Item curItem:items) {
            for(Feature curFeature: features){
                if (curItem.getId().equals(curFeature.getId())){
                    result.add(curItem);
                }
            }
        }

        return result;
    }


}
