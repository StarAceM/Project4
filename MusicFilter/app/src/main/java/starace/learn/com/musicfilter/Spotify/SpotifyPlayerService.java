package starace.learn.com.musicfilter.Spotify;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.Spotify;

import java.util.ArrayList;
import java.util.List;

import starace.learn.com.musicfilter.MainActivity;
import starace.learn.com.musicfilter.Spotify.Models.Item;

/**
 * Created by mstarace on 5/5/16.
 */
public class SpotifyPlayerService extends Service implements PlayerNotificationCallback {
    private final static String TAG_PLAYER_SERVICE = "SpotifyPlayerService";
    private final IBinder spotifyBinder = new SpotifyBinder();
    private String token;
    private Player spotifyPlayer;
    private List<String> playList;
    private List<String> curPlayList;
    private boolean isSetQueue;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        token = intent.getExtras().getString(MainActivity.KEY_SERVICE_TOKEN);
        playList = new ArrayList<>();
        curPlayList = new ArrayList<>();
        isSetQueue = false;
        Log.d(TAG_PLAYER_SERVICE, "This is the token in the Player Service " + token);

        Config playerConfig = new Config(this, token, MainActivity.CLIENT_ID);
        Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
            @Override
            public void onInitialized(Player player) {
                spotifyPlayer = player;
                ConnectionStateCallback connectionStateCallback = (ConnectionStateCallback) new MainActivity();
                PlayerNotificationCallback playerNotificationCallback = (PlayerNotificationCallback) new SpotifyPlayerService();
                spotifyPlayer.addConnectionStateCallback(connectionStateCallback);
                spotifyPlayer.addPlayerNotificationCallback(playerNotificationCallback);

                Log.d(TAG_PLAYER_SERVICE, "Player has been called in onBind");

            }

            @Override
            public void onError(Throwable throwable) {

            }
        });

        return spotifyBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public class SpotifyBinder extends Binder {
        public SpotifyPlayerService getService() {
            return SpotifyPlayerService.this;
        }
    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {

    }

    @Override
    public void onPlaybackError(ErrorType errorType, String s) {

    }

    public void playSong (){
        spotifyPlayer.resume();
    }

    public void pauseSong(){
        spotifyPlayer.pause();
    }

    public void nextSong(){
        spotifyPlayer.skipToNext();
    }

    public void setQueue(List<Item> items, Boolean isFirst){
        Log.d(TAG_PLAYER_SERVICE, "setQue has been called");
        playList.clear();
        curPlayList.clear();
        for(Item curItem:items) {
            playList.add(curItem.getUri());
        }
        curPlayList.addAll(playList);
        Log.d(TAG_PLAYER_SERVICE, "This is the size of the curPlayList " + curPlayList.size());
        if(!isFirst){
            Log.d(TAG_PLAYER_SERVICE,"this is the boolena isfirst " + isFirst.toString());
            spotifyPlayer.skipToNext();
        }
        spotifyPlayer.play(curPlayList);
        isSetQueue = true;
    }

    public void jumpTheQueue(int pos){
        Log.d(TAG_PLAYER_SERVICE, "JumptheQueue Has been called");
        Log.d(TAG_PLAYER_SERVICE, "Size of playlist at Jump " + playList.size());
        //check to make sure the queue has already been set
        if (isSetQueue) {
            curPlayList.clear();
            curPlayList.addAll(playList.subList(pos, playList.size()));
            spotifyPlayer.skipToNext();
            spotifyPlayer.play(curPlayList);
            Log.d(TAG_PLAYER_SERVICE,"issetqueue is " + isSetQueue);
        }
        Log.d(TAG_PLAYER_SERVICE, "JumpTheQue has finished");
    }

}
