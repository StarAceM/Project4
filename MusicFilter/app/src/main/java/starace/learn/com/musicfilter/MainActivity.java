package starace.learn.com.musicfilter;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import starace.learn.com.musicfilter.NavigationDrawer.NaviagtionEntry;
import starace.learn.com.musicfilter.NavigationDrawer.NavigationDivider;
import starace.learn.com.musicfilter.NavigationDrawer.NavigationFragment;
import starace.learn.com.musicfilter.NavigationDrawer.NavigationToggle;

public class MainActivity extends AppCompatActivity implements NavigationFragment.NotificationPreferences{
    private static final String TAG_MAIN = "MainActivity";
    public static final String KEY_SHARED_PREF_NOTIF = "NotificationPref";
    public static final String KEY_SHAREDPREF_FILE = "MainSharedPref";
    public static final String KEY_SLIDER_RATIO = "SliderRatio";
    public static final int KEY_DEFAULT_INT = -1;
    public static final String KEY_BUTTON_WIDTH = "RootWidth";
    public static final int widthFixed = 400;
    private static final int height = 150;
    public int width;
    private String notificationPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        SharedPreferences sharedPreferences = this.getSharedPreferences(KEY_SHAREDPREF_FILE, Context.MODE_PRIVATE);
        String notificationFromSharedPref = sharedPreferences.getString(KEY_SHARED_PREF_NOTIF, "");
        setNavigationDrawer(createBoolArrayList(notificationFromSharedPref));

        int sliderRatio = sharedPreferences.getInt(KEY_SLIDER_RATIO, KEY_DEFAULT_INT);
        int buttonWidth = sharedPreferences.getInt(KEY_BUTTON_WIDTH,KEY_DEFAULT_INT);
        setUpSlider(sliderRatio,buttonWidth);

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

        SliderButtonListener sliderButtonListener = new SliderButtonListener(this,null,root,sliderBar);
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
        String[] categories = getResources().getStringArray(R.array.genre);
        ArrayList<Boolean> isCheckedArray = new ArrayList<>();
        String[] arrayNotificationPref = notificationPreferences.split(",");

        for (int i = 0; i < categories.length; i++){
            isCheckedArray.add(false);
            for (String curNotification: arrayNotificationPref) {
                if (categories[i].equals(curNotification)){
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

        for (int i =0; i < navResourceArray.length; i++) {
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
        Log.i(TAG_MAIN, "setNotificationPreferences: " + notificationPreferences);
    }

    public void setSongListFragment() {

    }

    /**
     * Notification preferences are added to sharedPreferences
     */
    @Override
    protected void onDestroy() {
        SharedPreferences sharedPreferences = MainActivity.this.getSharedPreferences(KEY_SHAREDPREF_FILE,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SHARED_PREF_NOTIF, notificationPreferences);
        editor.commit();

        super.onDestroy();
    }

}

