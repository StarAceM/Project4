package starace.learn.com.musicfilter;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
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

import starace.learn.com.musicfilter.NavigationDrawer.Models.NavigationDivider;
import starace.learn.com.musicfilter.NavigationDrawer.Models.NavigationEntry;
import starace.learn.com.musicfilter.NavigationDrawer.Models.NavigationToggle;
import starace.learn.com.musicfilter.NavigationDrawer.NavigationFragment;
import starace.learn.com.musicfilter.Song.SongListAdapter;
import starace.learn.com.musicfilter.Song.SongListFragment;
import starace.learn.com.musicfilter.Spotify.Models.Item;
import starace.learn.com.musicfilter.Spotify.SpotifyPlayerService;

public class MainActivity extends AppCompatActivity implements SongListAdapter.RecyclerClickEvent, NavigationFragment.NotificationPreferences,
        ConnectionStateCallback, SongListFragment.SetSongItemsToMain, SliderButtonListener.SetBPMRange,
        SliderButtonListener.SetBPMValue, SongListFragment.SetIsSearching{

    private static final String TAG_MAIN = "MainActivity";
    public static String token;
    public static final String CLIENT_ID = "bb65fc78da534d8f801a5db0aaf6e422";
    private static final String REDIRECT_URI = "music-filter-app-callback://callback";
    private static final int REQUEST_CODE_SPOTIFY = 1337;
    private static final int FAILED_LOGIN_CODE = 0;

    public static final String KEY_SHARED_PREF_NOTIF = "NotificationPref";
    public static final String KEY_SHAREDPREF_FILE = "MainSharedPref";
    public static final String KEY_SLIDER_RATIO = "SliderRatio";
    public static final int KEY_DEFAULT_INT = -1;
    public static final String KEY_BUTTON_WIDTH = "RootWidth";
    public static final int widthFixed = 400;
    private static final int height = 150;
    public int width;
    private int leftMargin;
    private String notificationPreferences;
    private SongListFragment songListFragment;
    private SliderButtonListener sliderButtonListener;

    public static final String KEY_SERVICE_TOKEN = "oAuthToken";
    public boolean isBound;
    private boolean isConnected;
    private SpotifyPlayerService playerService;
    private List<Item> itemList;
    private Toolbar toolbar;
    private Button playButton;
    private Button pauseButton;
    private Button skipButton;
    private ImageView nowPlayingImage;
    private TextView nowPlayingTitle;
    private TextView nowPlayingArtist;
    private TextView nowPlayingBPM;
    private TextView bpmRange;
    private TextView bpmValue;
    private ProgressBar searchProgress;
    private boolean isSearching;
    private BroadcastReceiver receiver;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isConnected=checkNetworkConnection();
        initializeProgressBar();
        initializeToolbar();
        setUpBroadcastReceiver();
        setUpSpotifyLogin();
        getSharedPreferencesSlider();
        setUpButtons();
        setButtonOnClickListener(playButton, 0);
        setButtonOnClickListener(pauseButton, 1);
        setButtonOnClickListener(skipButton, 2);
        setBPMViews();
        setUpNowPlayingViews();
        setSongListFragment();

        if(!isConnected){
            buildAlertDialog(FAILED_LOGIN_CODE);
        }

    }

    /**
     * checks for a network connection and returns a boolean
     * @return
     */
    private boolean checkNetworkConnection (){
        ConnectivityManager connectivityManager =
                (ConnectivityManager)  this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * sets the search progress bar to invisible and the the initial state of the
     * isSearching boolean to false
     */
    private void initializeProgressBar(){
        searchProgress = (ProgressBar) findViewById(R.id.double_tap_progress);
        isSearching = false;
        if (searchProgress != null) {
            searchProgress.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * initializes the toolbar with a custom view
     */
    private void initializeToolbar(){
        toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.toolbar, null),
                    new ActionBar.LayoutParams(
                            ActionBar.LayoutParams.WRAP_CONTENT,
                            ActionBar.LayoutParams.MATCH_PARENT,
                            Gravity.CENTER
                    )
            );

            toolbar.setTitleTextColor(getResources().getColor(R.color.colorAccent));
        }
    }

    /**
     * sets up a broadcast reciever that is used to send messages from the
     * SpotifyPlayerService to the MainActivity
     */
    private void setUpBroadcastReceiver(){
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int pos = intent.getIntExtra(SpotifyPlayerService.BROADCAST_MESSAGE,-1);
                updateNowPlayingViews(pos);
                Log.d(TAG_MAIN,"SpotifyPlayerService has been received");
            }
        };

    }

    /**
     * starts the spotify sdk login process. the result is returned in onActivityResult
     */
    private void setUpSpotifyLogin(){
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE_SPOTIFY, request);

    }

    /**
     * builds the alert dialog used when no network is found during the login process
     * @param type
     */
    private void buildAlertDialog(int type){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        switch (type){
            case 0:
                alertBuilder.setTitle("No Network Found");
                alertBuilder.setMessage("No Network Found. Please Connect and Restart BeatBot");
                alertBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        dialog.cancel();
                    }
                });
                break;
            case 1:
                alertBuilder.setTitle("Connection Lost");
                alertBuilder.setMessage("");
                alertBuilder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setUpSpotifyLogin();
                        dialog.cancel();
                    }
                });
        }

        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    /**
     * binds the SpotifyPlayerService passes the token aquired from login process
     * called from onActivityResult
     * @param token
     */
    private void bindSpotifyPlayerService(String token){
        Log.d(TAG_MAIN, "Logged in? " + token);
        songListFragment = new SongListFragment();
        Intent spotifyPlayerIntent = new Intent(this,SpotifyPlayerService.class);
        spotifyPlayerIntent.putExtra(KEY_SERVICE_TOKEN,token);
        this.bindService(spotifyPlayerIntent, spotifyServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * initializes the player buttons
     */
    private void setUpButtons() {
        playButton = (Button) findViewById(R.id.play_button);
        pauseButton = (Button) findViewById(R.id.pause_button);
        skipButton = (Button) findViewById(R.id.skip_button);
    }

    /**
     * initializes the nowPlaying views
     */
    private void setUpNowPlayingViews() {
        nowPlayingImage = (ImageView) findViewById(R.id.song_image);
        nowPlayingTitle = (TextView) findViewById(R.id.song_title);
        nowPlayingArtist = (TextView) findViewById(R.id.song_detail);
        nowPlayingBPM = (TextView) findViewById(R.id.song_bpm);
    }

    /**
     * initalizes the txt views of the player layout
     */
    private void setBPMViews(){
        bpmRange = (TextView) findViewById(R.id.bpm_range);
        bpmValue = (TextView) findViewById(R.id.bpm_setting);

        setInitialBPMValues();
    }

    /**
     * uses the value of the left margin of the slider button to set initial
     * bpm values from shared preferences
     */
    private void setInitialBPMValues(){

        float position = leftMargin + (width/2);
        bpmValue.setText("BPM Value: " +(int)(60.0f + ((position - 200.0f)/4.86f)));
        bpmRange.setText("BPM Range: " + (int) (5.0f + ((width - 400.0f) / 5.44f)));
    }

    /**
     * Recieves a button and uses its type to set onClickListener functionality
     * @param button
     * @param type
     */
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
                        playerService.nextSong();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /**
     * get the slider information from shared preferences
     */
    private void getSharedPreferencesSlider(){

        SharedPreferences sharedPreferences = this.getSharedPreferences(KEY_SHAREDPREF_FILE, Context.MODE_PRIVATE);
        String notificationFromSharedPref = sharedPreferences.getString(KEY_SHARED_PREF_NOTIF, "");
        setNavigationDrawer(createBoolArrayList(notificationFromSharedPref));

        int sliderRatio = sharedPreferences.getInt(KEY_SLIDER_RATIO, KEY_DEFAULT_INT);
        int buttonWidth = sharedPreferences.getInt(KEY_BUTTON_WIDTH, KEY_DEFAULT_INT);
        setUpSlider(sliderRatio, buttonWidth);

    }

    /**
     * sets up the slider button which includes the progress bar behind it
     * sets the size, position, and color of the slider
     * @param sliderRatio
     * @param buttonWidth
     */
    private void setUpSlider(int sliderRatio,int buttonWidth){
        Button sliderButton = (Button) findViewById(R.id.slider_button);
        ProgressBar sliderBar = (ProgressBar) findViewById(R.id.progress_bar_slider);
        ViewGroup root = (ViewGroup)findViewById(R.id.root_slider_view);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        width = setButtonWidth(size, buttonWidth, sliderRatio);

        Log.d(TAG_MAIN, "SIZE OF X " + size.x);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);

        if (sliderRatio == -1) {
            width = 400;
            leftMargin = (size.x / 2) - (width / 2);
            layoutParams.leftMargin = leftMargin;
            if (sliderBar != null) {
                sliderBar.setProgress(50);
                sliderBar.setProgressTintList(ColorStateList.valueOf(Color.rgb(103, 11, 119)));
            }
        } else {
            double sliderPercent = (double) sliderRatio / 100.0;
            Double sliderLeftMarginDouble = sliderPercent * (double) size.x;
            leftMargin = sliderLeftMarginDouble.intValue() - (width/2);
            layoutParams.leftMargin = leftMargin;
            if (sliderBar != null) {
                setSliderColor(sliderPercent, sliderBar);
                sliderBar.setProgress(sliderRatio);
            }
            Log.d(TAG_MAIN, "This is the progress ratio used to set color " + sliderPercent);
        }

        if (sliderButton != null) {
            sliderButton.setLayoutParams(layoutParams);
            sliderButtonListener = new SliderButtonListener(this, null, root, sliderBar);
            sliderButton.setOnTouchListener(sliderButtonListener);
        }


    }

    /**
     * a public method that is called from the SliderButtonListener to update the position and
     * color of the sliderbutton
     * @param button
     * @param root
     * @param sliderBar
     */
    public static void setSliderProgress (View button, View root, ProgressBar sliderBar) {
        double left = button.getLeft() + (button.getWidth()/2);
        double total = root.getWidth();
        double progressRatio = (left/total);
        Double dProgress = progressRatio * 100;

        setSliderColor(progressRatio, sliderBar);
        sliderBar.setProgress(dProgress.intValue());
    }

    /**
     * interface used in the SliderButton listener to set the bpm range values in the
     * nowPlaying layout
     * @param range
     */
    @Override
    public void setRange(float range) {
        bpmRange.setText("BPM Range: " + (int) range);
    }

    /**
     * interface used in the SliderButton listener to set the bpm value in the
     * nowPlaying layout
     * @param value
     */
    @Override
    public void setBPMValue(float value) {
        bpmValue.setText("BPM Value: " + (int) value);
    }

    /**
     * sets the color of the slider progress bar based on a percentage of the total
     * possible value
     * @param progressRatio
     * @param sliderBar
     */
    public static void setSliderColor(Double progressRatio, ProgressBar sliderBar){
        Double red = 3.0;
        Double green = 17.0;
        Double blue = 229.0;

        red = red + 200.0 * progressRatio;
        green = green - 17 * progressRatio;
        blue = blue - 220 * progressRatio;

        sliderBar.setProgressTintList(ColorStateList.valueOf(Color.rgb(red.intValue(), green.intValue(), blue.intValue())));
    }

    /**
     * called from setUpSliderButton sets the current width of the slider button
     * @param size
     * @param buttonWidth
     * @param defaultInt
     * @return
     */
    private static int setButtonWidth(Point size, int buttonWidth, int defaultInt){
        int newWidth;
        int portraitWidth;

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

        if (getSupportActionBar()!= null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            Log.d(TAG_MAIN, "SUPPORT ACTION BAR IS NULL");
        }
        String[] navResourceArray = getResources().getStringArray(R.array.genre);
        List<NavigationEntry> drawerEntries = new ArrayList<>();

        drawerEntries.add(new NavigationDivider());

        for (int i =1; i < navResourceArray.length; i++) {
            drawerEntries.add(new NavigationToggle(navResourceArray[i]));
        }

        NavigationFragment drawerFragment = (NavigationFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_navigation_drawer);

        drawerFragment.initDrawer((android.support.v4.widget.DrawerLayout) findViewById(R.id.drawer_layout_main),
                toolbar, drawerEntries, isCheckedArray);
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

    /**
     * sets two songListFragments which are distinguished by a boolean passed to the songListFragment via
     * initSongRecyclerView method
     */
    private void setSongListFragment() {
        Log.d(TAG_MAIN,"setSongListFragment is called, token is " + token);
        songListFragment = (SongListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_song_list);
        songListFragment.initSongRecyclerView(true);
        sliderButtonListener.setFragmentToListener(songListFragment);
        SongListFragment songListPlayedFragment = (SongListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_song_list_played);
        songListPlayedFragment.initSongRecyclerView(false);

    }

    /**
     * onActivityResult receives the result of the SpotfyLogin request and then calls
     * bindSpotifyPlayerService to bind the service with the token returned
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.d(TAG_MAIN, "On activity result is called");

//        if (requestCode == REQUEST_CODE_SPOTIFY) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
        Log.d(TAG_MAIN,"Response token " + response.getAccessToken());
        Log.d(TAG_MAIN,"Response error " + response.getError());
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {

                SharedPreferences sharedPreferences = this.getSharedPreferences(KEY_SHAREDPREF_FILE, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(KEY_SERVICE_TOKEN, response.getAccessToken());
                editor.apply();
                token = response.getAccessToken();
                songListFragment.setTokenFromMain(token);
                Log.d(TAG_MAIN, "Token is set to class variable after Authenitication " + token);
                bindSpotifyPlayerService(response.getAccessToken());

            }
//        }
    }

    /**
     * sets up the service connection when the service is bound receives IBinder and
     * gets and instance of the service from it
     */
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

    /**
     * call back methods for the SpotifyLogin interface
     */
    @Override
    public void onLoggedIn() {

    }

    @Override
    public void onLoggedOut() {

    }

    @Override
    public void onLoginFailed(Throwable throwable) {
        Log.d(TAG_MAIN, "onLoginFailed has been called");

    }

    @Override
    public void onTemporaryError() {
        Log.d(TAG_MAIN, "onTemporaryError has been called ");

    }

    @Override
    public void onConnectionMessage(String s) {

    }

    /**
     * onStart is used to set up the LocalBroadcastManger and register the receiver
     */
    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,
                new IntentFilter(SpotifyPlayerService.BROADCAST_INTENT));
    }

    /**
     * onStop used to unregister the receiver from the LocalBroadcastManager
     */
    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }

    /**
     * interface from the songListFragment to pass the list of song Item to the MainActivity
     * the list of items is used to Call setQueue in the SpotifyPlayerService
     * @param listItem
     */
    @Override
    public void passSongItemsToMain(List<Item> listItem) {
        itemList = listItem;
        Log.d(TAG_MAIN, "This is the list Item size " + listItem.size());
        if (listItem.size() > 0) {
            Log.d(TAG_MAIN, "PassSongItems to main has been calls");
            playerService.setQueue(listItem);
        } else if (listItem.size() < 1){
            Toast.makeText(this,"Your Filter Didn't Return Any Results. Adjust Your Filter and Try Again",
                    Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * interface from the songListFragment to notify the MainActivity that a search has
     * started or stoped
     * @param isNotSearching
     */
    @Override
    public void setIsSearchingMain(boolean isNotSearching) {
        this.isSearching = !isNotSearching;
        if (isSearching) {
            searchProgress.setVisibility(View.VISIBLE);
        } else {
            searchProgress.setVisibility(View.INVISIBLE);
        }
        playButton.setEnabled(isNotSearching);
        pauseButton.setEnabled(isNotSearching);
        skipButton.setEnabled(isNotSearching);

    }

    /**
     * interface from SongListFragment to call a method in the SpotifyPlayerService
     * to play song clicked in the recycler view
     * @param pos
     */
    @Override
    public void handleRecyclerClickEvent(int pos) {
        playerService.jumpTheQueue(pos);
        Log.d(TAG_MAIN, "jumpTheQueue has been called");
    }

    /**
     * method updates the views in the nowPLaying layout
     * @param pos
     */
    private void updateNowPlayingViews(int pos){
        if (itemList != null && isConnected) {
            if (itemList.size() > pos && !itemList.get(0).getId().equals("isFake")) {
                nowPlayingArtist.setText(itemList.get(pos).getArtists()[0].getName());
                Log.d(TAG_MAIN, "This is the artist name " + itemList.get(pos).getArtists()[0].getName());
                nowPlayingTitle.setText(itemList.get(pos).getName());
                Log.d(TAG_MAIN, "This is the image url " + itemList.get(pos).getAlbum().getImages()[0].getImageURL());
                Glide.with(this).load(itemList.get(pos).getAlbum().getImages()[0].getImageURL())
                        .into(nowPlayingImage);
                nowPlayingBPM.setText("BPM: " + itemList.get(pos).getTempo());
            }
            Log.d(TAG_MAIN, "updateNowPlayingViews is finished");
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

        if(isBound) {
            this.unbindService(spotifyServiceConnection);
        }
        super.onDestroy();
    }


}

