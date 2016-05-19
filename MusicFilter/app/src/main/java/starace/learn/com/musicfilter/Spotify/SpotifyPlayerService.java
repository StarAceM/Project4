package starace.learn.com.musicfilter.Spotify;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
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
    public final static String BROADCAST_INTENT = "BroadcastIntent";
    public final static String BROADCAST_MESSAGE = "BroadcastMessage";
    private final IBinder spotifyBinder = new SpotifyBinder();
    private String token;
    private Player spotifyPlayer;
    private List<String> playList;
    private List<String> curPlayList;
    private boolean isSetQueue;
    private int trackCounter;
    private boolean isJump;
    private LocalBroadcastManager broadcaster;

    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        token = intent.getExtras().getString(MainActivity.KEY_SERVICE_TOKEN);
        playList = new ArrayList<>();
        curPlayList = new ArrayList<>();
        broadcaster = LocalBroadcastManager.getInstance(this);
        isSetQueue = false;
        isJump = false;
        Log.d(TAG_PLAYER_SERVICE, "This is the token in the Player Service " + token);
        Config playerConfig = new Config(this, token, MainActivity.CLIENT_ID);
        Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
            @Override
            public void onInitialized(Player player) {
                spotifyPlayer = player;
                ConnectionStateCallback connectionStateCallback = new MainActivity();
                PlayerNotificationCallback playerNotificationCallback = SpotifyPlayerService.this;

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
        Log.d(TAG_PLAYER_SERVICE,"THIS IS THE PLAYBACK EVENT " + eventType +", PlayerState is " + playerState.toString());
        switch (eventType){
            case AUDIO_FLUSH:
                Log.d(TAG_PLAYER_SERVICE, "Track_Changed handled Audio Flush and isJump " + isJump);
                if (isJump)
                sendMessage(trackCounter);
                isJump = false;
                break;
            case TRACK_CHANGED:
                Log.d(TAG_PLAYER_SERVICE, "Track_Changed handled Track Changed and isJump " + isJump);
                if (!isJump){
                    trackCounter +=1;
                    sendMessage(trackCounter);
                }
            default:
                break;
        }
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
        jumpTheQueue(trackCounter + 1);
    }

    public void setQueue(List<Item> items){
        trackCounter = 0;
        isJump = true;
        Log.d(TAG_PLAYER_SERVICE, "setQue has been called");
        playList.clear();
        curPlayList.clear();
        for(Item curItem:items) {
            playList.add(curItem.getUri());
        }
        curPlayList.addAll(playList);
        Log.d(TAG_PLAYER_SERVICE, "This is the size of the curPlayList " + curPlayList.size());
        spotifyPlayer.skipToNext();
        spotifyPlayer.play(curPlayList);
        isSetQueue = true;
    }

    public void jumpTheQueue(int pos){
        trackCounter = pos;
        isJump = true;
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

    public void sendMessage(int pos){
        Log.d(TAG_PLAYER_SERVICE, "this is the pos of BROADCAST INTENT");
        Intent playerIntent = new Intent(BROADCAST_INTENT);
        playerIntent.putExtra(BROADCAST_MESSAGE,pos);
        broadcaster.sendBroadcast(playerIntent);
    }

}
