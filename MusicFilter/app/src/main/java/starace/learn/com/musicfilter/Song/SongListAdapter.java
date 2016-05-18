package starace.learn.com.musicfilter.Song;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import starace.learn.com.musicfilter.R;
import starace.learn.com.musicfilter.Spotify.Models.Item;
import starace.learn.com.musicfilter.Spotify.Models.ItemRoot;
import starace.learn.com.musicfilter.Spotify.Models.ItemStart;

/**
 * Created by mstarace on 5/4/16.
 */
public class SongListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG_SONG_ADAPTER = "SongAdapter";
    private List<ItemRoot> songList;
    private LayoutInflater inflater;
    private boolean isFirst;
    private Context context;
    RecyclerClickEvent clickEvent;
    SongListFragment songListFragment;


    public SongListAdapter(SongListFragment fragment, List<ItemRoot> songList, boolean isFirst) {
        this.context = fragment.getActivity();
        this.songList = songList;
        this.inflater = LayoutInflater.from(this.context);
        this.isFirst = isFirst;
        this.songListFragment = fragment;
        this.clickEvent = (RecyclerClickEvent) this.context;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View songObjectView = inflater.inflate(R.layout.song_list_recycler_view,parent,false);
        switch (viewType) {
            case 0:
                return new ViewHolderStart(songObjectView);
        }

        return new ViewHolder(songObjectView);
    }

    @Override
    public int getItemViewType(int position) {
        if(songList.get(position) instanceof ItemStart){
            return 0;
        }
        if(songList.get(position) instanceof Item){
            return 1;
        }
        return -1;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final ItemRoot curSong = songList.get(position);

        if (curSong instanceof Item) {
            setItemViews(curSong,holder,position);
        }

        if (curSong instanceof ItemStart) {
            setItemStartViews(curSong,holder,position);
        }

    }

    private void setItemViews(ItemRoot curSong, RecyclerView.ViewHolder holder, final int position){
        ViewHolder viewHolder = (ViewHolder) holder;
        Item curItem = (Item) curSong;

        TextView title = viewHolder.songTitle;
        title.setText(curItem.getName());
        ImageView image = viewHolder.songImage;
        Glide.with(context).load(curItem.getAlbum().getImages()[0].getImageURL())
                .into(image);
        TextView detail = viewHolder.songDetail;
        detail.setText(curItem.getArtists()[0].getName());

        if (isFirst) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG_SONG_ADAPTER, "This item has been clicked " + position);
                    clickEvent.handleRecyclerClickEvent(position);

                }
            });

        } else {
            holder.itemView.setBackgroundColor(context.getColor(R.color.colorPlayedListBackground));
        }
    }

    private void setItemStartViews (ItemRoot curSong, RecyclerView.ViewHolder holder, final int position) {
        ViewHolderStart viewHolderStart = (ViewHolderStart) holder;
        ItemStart curItemStart = (ItemStart) curSong;

        TextView title = viewHolderStart.title;
        title.setText(curItemStart.getTitle());
        TextView description = viewHolderStart.description;
        description.setText(curItemStart.getDescription());
        ImageView image = viewHolderStart.image;
        image.setImageResource(curItemStart.getImage());

    }

    @Override
    public int getItemCount() {
        return songList.size();
    }




    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView songImage;
        public TextView songTitle;
        public TextView songDetail;

        public ViewHolder(View itemView) {
            super(itemView);
            this.songImage = (ImageView) itemView.findViewById(R.id.song_image_recycler);
            this.songTitle = (TextView) itemView.findViewById(R.id.song_title_recycler);
            this.songDetail = (TextView) itemView.findViewById(R.id.song_detail_recycler);

        }
    }

    public static class ViewHolderStart extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView title;
        public TextView description;

        public ViewHolderStart(View itemStartView) {
            super(itemStartView);
            this.image = (ImageView) itemStartView.findViewById(R.id.song_image_recycler);
            this.title = (TextView) itemStartView.findViewById(R.id.song_title_recycler);
            this.description = (TextView) itemStartView.findViewById(R.id.song_detail_recycler);
        }
    }

    public interface RecyclerClickEvent{
        void handleRecyclerClickEvent(int pos);
    }

}
