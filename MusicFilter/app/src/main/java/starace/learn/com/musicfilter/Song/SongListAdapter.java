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

/**
 * Created by mstarace on 5/4/16.
 */
public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.ViewHolder> {
    private static final String TAG_SONG_ADAPTER = "SongAdapter";
    private List<Item> songList;
    private LayoutInflater inflater;
    private boolean isFirst;
    private Context context;
    RecyclerClickEvent clickEvent;


    public SongListAdapter(Context context, List<Item> songList, boolean isFirst) {
        this.songList = songList;
        this.inflater = LayoutInflater.from(context);
        this.isFirst = isFirst;
        this.context = context;
        this.clickEvent = (RecyclerClickEvent) context;

    }

    @Override
    public SongListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View songObjectView = inflater.inflate(R.layout.song_list_recycler_view,parent,false);

        return new ViewHolder(songObjectView);
    }

    @Override
    public void onBindViewHolder(final SongListAdapter.ViewHolder holder, final int position) {
        final Item curSong = songList.get(position);

        TextView title = holder.songTitle;
        title.setText(curSong.getName());
        ImageView image = holder.songImage;
        Glide.with(context).load(curSong.getAlbum().getImages()[0].getImageURL())
            .into(image);
        TextView detail = holder.songDetail;
        detail.setText(curSong.getArtists()[0].getName());

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

    public interface RecyclerClickEvent{
        void handleRecyclerClickEvent(int pos);
    }



}
