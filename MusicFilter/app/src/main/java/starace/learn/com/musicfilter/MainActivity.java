package starace.learn.com.musicfilter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.ConnectionStateCallback;

import java.util.ArrayList;
import java.util.List;

import starace.learn.com.musicfilter.NavigationDrawer.NaviagtionEntry;
import starace.learn.com.musicfilter.NavigationDrawer.NavigationDivider;
import starace.learn.com.musicfilter.NavigationDrawer.NavigationFragment;
import starace.learn.com.musicfilter.NavigationDrawer.NavigationToggle;
import starace.learn.com.musicfilter.Song.SongListAdapter;
import starace.learn.com.musicfilter.Song.SongListFragment;
import starace.learn.com.musicfilter.Spotify.Models.Item;
import starace.learn.com.musicfilter.Spotify.SpotifyPlayerService;

public class MainActivity extends AppCompatActivity implements SongListAdapter.RecyclerClickEvent, NavigationFragment.NotificationPreferences,
        ConnectionStateCallback, SongListFragment.SetSongItemsToMain{
    private static final String TAG_MAIN = "MainActivity";

    public static String token;
    public static final String CLIENT_ID = "bb65fc78da534d8f801a5db0aaf6e422";
    private static final String REDIRECT_URI = "music-filter-app-callback://callback";
    private static final int REQUEST_CODE_SPOTIFY = 1337;

    public static final String KEY_SHARED_PREF_NOTIF = "NotificationPref";
    public static final String KEY_SHAREDPREF_FILE = "MainSharedPref";
    public static final String KEY_SLIDER_RATIO = "SliderRatio";
    public static final int KEY_DEFAULT_INT = -1;
    public static final String KEY_BUTTON_WIDTH = "RootWidth";
    public static final int widthFixed = 400;
    private static final int height = 150;
    public int width;
    private SongListFragment songListFragment;
    private SliderButtonListener sliderButtonListener;

    private String notificationPreferences;

    private Intent spotifyPlayerIntent;
    public static final String KEY_SERVICE_TOKEN = "oAuthToken";
    public boolean isBound;
    private SpotifyPlayerService playerService;
    private List<Item> itemList;
    private Button playButton;
    private Button pauseButton;
    private Button stopButton;
    private ImageView nowPlayingImage;
    private TextView nowPlayingTitle;
    private TextView nowPlayingArtist;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        setUpSpotifyLogin();
        getSharedPreferencesSlider();

        setUpButtons();
        setButtonOnClickListener(playButton, 0);
        setButtonOnClickListener(pauseButton, 1);
        setButtonOnClickListener(stopButton,2);

        setUpNowPlayingViews();

        setSongListFragment();

    }

    private void setUpSpotifyLogin(){
        SharedPreferences sharedPreferences = this.getSharedPreferences(KEY_SHAREDPREF_FILE, Context.MODE_PRIVATE);
        token = sharedPreferences.getString(KEY_SERVICE_TOKEN,"");
        if (token.equals("")){

            AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                    AuthenticationResponse.Type.TOKEN,
                    REDIRECT_URI);
            builder.setScopes(new String[]{"user-read-private", "streaming"});
            AuthenticationRequest request = builder.build();
            AuthenticationClient.openLoginActivity(this, REQUEST_CODE_SPOTIFY, request);

        } else {

            bindSpotifyPlayerService(token);

        }
    }

    private void bindSpotifyPlayerService(String token){
        Log.d(TAG_MAIN, "Logged in? " + token);
        songListFragment = new SongListFragment();
        spotifyPlayerIntent = new Intent(this,SpotifyPlayerService.class);
        spotifyPlayerIntent.putExtra(KEY_SERVICE_TOKEN,token);
        this.bindService(spotifyPlayerIntent, spotifyServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setUpButtons() {
        playButton = (Button) findViewById(R.id.play_button);
        pauseButton = (Button) findViewById(R.id.pause_button);
        stopButton = (Button) findViewById(R.id.stop_button);
    }

    private void setUpNowPlayingViews() {
        nowPlayingImage = (ImageView) findViewById(R.id.song_image);
        nowPlayingTitle = (TextView) findViewById(R.id.song_title);
        nowPlayingArtist = (TextView) findViewById(R.id.song_detail);
    }

    private void setButtonOnClickListener(Button button,final int type){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (type) {
                    case 0:
                        playerService.playSong();
                        break;
                    case 1:
                        playerService.pauseSong();
                        break;
                    case 2:
                        playerService.stopSong();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void getSharedPreferencesSlider(){

        SharedPreferences sharedPreferences = this.getSharedPreferences(KEY_SHAREDPREF_FILE, Context.MODE_PRIVATE);
        String notificationFromSharedPref = sharedPreferences.getString(KEY_SHARED_PREF_NOTIF, "");
        setNavigationDrawer(createBoolArrayList(notificationFromSharedPref));

        int sliderRatio = sharedPreferences.getInt(KEY_SLIDER_RATIO, KEY_DEFAULT_INT);
        int buttonWidth = sharedPreferences.getInt(KEY_BUTTON_WIDTH,KEY_DEFAULT_INT);
        setUpSlider(sliderRatio, buttonWidth);

    }

    private void setUpSlider(int sliderRatio,int buttonWidth){
        Button sliderButton = (Button) findViewById(R.id.slider_button);
        ProgressBar sliderBar = (ProgressBar) findViewById(R.id.progress_bar_slider);
        ViewGroup root = (ViewGroup)findViewById(R.id.root_slider_view);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        width = setButtonWidth(size,buttonWidth,sliderRatio);

        Log.d(TAG_MAIN, "SIZE OF X " + size.x);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);

        if (sliderRatio == -1) {
            layoutParams.leftMargin = (size.x / 2) - (width / 2);
            sliderBar.setProgress(50);
            sliderBar.setProgressTintList(ColorStateList.valueOf(Color.rgb(103, 11, 119)));
        } else {
            double sliderPercent = (double) sliderRatio / 100.0;
            Double sliderLeftMarginDouble = sliderPercent * (double) size.x;
            layoutParams.leftMargin = sliderLeftMarginDouble.intValue() - (width/2);
            setSliderColor(sliderPercent, sliderBar);
            sliderBar.setProgress(sliderRatio);
            Log.d(TAG_MAIN, "This is the progress ratio used to set color " + sliderPercent);
        }

        sliderButton.setLayoutParams(layoutParams);

        sliderButtonListener = new SliderButtonListener(this,null,root,sliderBar);
        sliderButton.setOnTouchListener(sliderButtonListener);

    }

    public static void setSliderProgress (View button, View root, ProgressBar sliderBar) {
        double left = button.getLeft() + (button.getWidth()/2);
        double total = root.getWidth();
        double progressRatio = (left/total);
        Double dProgress = progressRatio * 100;

        setSliderColor(progressRatio, sliderBar);
        sliderBar.setProgress(dProgress.intValue());
    }

    public static void setSliderColor(Double progressRatio, ProgressBar sliderBar){
        Double red = 3.0;
        Double green = 17.0;
        Double blue = 229.0;

        red = red + 200.0 * progressRatio;
        green = green - 17 * progressRatio;
        blue = blue - 220 * progressRatio;

        sliderBar.setProgressTintList(ColorStateList.valueOf(Color.rgb(red.intValue(), green.intValue(), blue.intValue())));
    }

    public static int setButtonWidth(Point size, int buttonWidth, int defaultInt){
        int newWidth = 0;
        int portraitWidth = 0;

        if(size.x > size.y){
            portraitWidth = size.y;
        } else {
            portraitWidth = size.x;
        }

        if (defaultInt == -1) {
            newWidth = widthFixed;
        } else {
            Double calcWidth = ((double) buttonWidth * size.x) / portraitWidth;
            newWidth = calcWidth.intValue();
        }

        return newWidth;
    }

    /**
     * Creates an arrayList of booleans from a string of notification preferences
     * @param notificationPreferences
     * @return
     */
    private ArrayList<Boolean> createBoolArrayList(String notificationPreferences){
        String[] genres = getResources().getStringArray(R.array.genre);
        ArrayList<Boolean> isCheckedArray = new ArrayList<>();
        String[] arrayNotificationPref = notificationPreferences.split(",");

        for (int i = 0; i < genres.length; i++){
            isCheckedArray.add(false);
            for (String curNotification: arrayNotificationPref) {
                if (genres[i].equals(curNotification)){
                    isCheckedArray.set(i,true);
                }
            }
        }

        return isCheckedArray;
    }

    /**
     * navigation drawer is set up and items are added
     * @param isCheckedArray
     */
    private void setNavigationDrawer(ArrayList<Boolean> isCheckedArray) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!= null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            Log.d(TAG_MAIN, "SUPPORT ACTION BAR IS NULL");
        }
        String[] navResourceArray = getResources().getStringArray(R.array.genre);
        List<NaviagtionEntry> drawerEntries = new ArrayList<>();

        drawerEntries.add(new NavigationDivider());

        for (int i =1; i < navResourceArray.length; i++) {
            drawerEntries.add(new NavigationToggle(navResourceArray[i]));
        }

        NavigationFragment drawerFragment = (NavigationFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_navigation_drawer);

        drawerFragment.initDrawer((android.support.v4.widget.DrawerLayout) findViewById(R.id.drawer_layout_main),
                toolbar, drawerEntries,isCheckedArray);
        Log.d(TAG_MAIN, "THE initDrawer HAS BEEN CALLED ON MAIN");
    }

    /**
     * interface method is implemented to receive notification preferences from
     * Nav Drawer fragment
     * @param notificationPreferences
     */
    @Override
    public void setNotificationPreferences(String notificationPreferences) {
        this.notificationPreferences = notificationPreferences;
        SharedPreferences sharedPreferences = this.getSharedPreferences(KEY_SHAREDPREF_FILE, Context.MODE_PRIVATE);
        sharedPreferences.edit()
                .putString(KEY_SHARED_PREF_NOTIF,notificationPreferences)
                .apply();

        Log.i(TAG_MAIN, "setNotificationPreferences: " + notificationPreferences);
    }

    public void setSongListFragment() {
        songListFragment = (SongListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_song_list);
        songListFragment.initSongRecyclerView(true);
        sliderButtonListener.setFragmentToListener(songListFragment);
        songListFragment.setTokenFromMain(token);

        SongListFragment songListPlayedFragment = (SongListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_song_list_played);
        songListPlayedFragment.initSongRecyclerView(false);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_CODE_SPOTIFY) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {

                SharedPreferences sharedPreferences = this.getSharedPreferences(KEY_SHAREDPREF_FILE, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(KEY_SERVICE_TOKEN, response.getAccessToken());
                editor.apply();
                token = response.getAccessToken();
                bindSpotifyPlayerService(response.getAccessToken());
            }
        }
    }

    protected ServiceConnection spotifyServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            SpotifyPlayerService.SpotifyBinder spotifyBinder = (SpotifyPlayerService.SpotifyBinder) service;
            playerService = spotifyBinder.getService();
            isBound = true;
            Log.d(TAG_MAIN,"SERVICE IS CONNECTED");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG_MAIN,"SERVICE IS DISCONNECTED");
            isBound = false;
        }
    };

    //Authentication call back interface methods

    @Override
    public void onLoggedIn() {

    }

    @Override
    public void onLoggedOut() {

    }

    @Override
    public void onLoginFailed(Throwable throwable) {

    }

    @Override
    public void onTemporaryError() {

    }

    @Override
    public void onConnectionMessage(String s) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        //ToDo need to save desired info to database here to restore views upon resume
    }

    @Override
    public void passSongItemsToMain(List<Item> listItem) {
        itemList = listItem;
        Log.d(TAG_MAIN, "This is the list Item size " + listItem.size());
        if (listItem.size() > 0) {
            playerService.setQueue(listItem);
            updateNowPlayingViews(0);
        } else {
            Toast.makeText(this,"Your Filter Didn't Any Results. Adjust Your Filter and Try Again",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void handleRecyclerClickEvent(int pos) {
        playerService.jumpTheQueue(pos);
        updateNowPlayingViews(pos);
        Log.d(TAG_MAIN, "jumpTheQueue has been called");
        //set view here
    }

    private void updateNowPlayingViews(int pos){
        if (itemList.size() > pos) {
            nowPlayingArtist.setText(itemList.get(pos).getArtists()[0].getName());
            Log.d(TAG_MAIN, "This is the artist name " + itemList.get(pos).getArtists()[0].getName());
            nowPlayingTitle.setText(itemList.get(pos).getName());
            Log.d(TAG_MAIN, "This is the image url " + itemList.get(pos).getAlbum().getImages()[0].getImageURL());
            Glide.with(this).load(itemList.get(pos).getAlbum().getImages()[0].getImageURL())
                    .into(nowPlayingImage);
        }
    }

    /**
     * Notification preferences are added to sharedPreferences
     */
    @Override
    protected void onDestroy() {
        SharedPreferences sharedPreferences = MainActivity.this.getSharedPreferences(KEY_SHAREDPREF_FILE,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SHARED_PREF_NOTIF, notificationPreferences);
        editor.apply();

        this.unbindService(spotifyServiceConnection);

        super.onDestroy();
    }


}

