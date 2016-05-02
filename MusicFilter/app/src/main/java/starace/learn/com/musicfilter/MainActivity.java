package starace.learn.com.musicfilter;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {
    private static final String TAG_MAIN = "MainActivity";
    public static final String KEY_SHAREDPREF_FILE = "MainSharedPref";
    public static final String KEY_SLIDER_RATIO = "SliderRatio";
    public static final int KEY_DEFAULT_INT = -1;
    public static final String KEY_ROOT_WIDTH = "RootWidth";
    public static final int widthFixed = 300;
    public int width;
    private static final int height = 150;
    private Button sliderButton;
    private ProgressBar sliderBar;
    private ViewGroup root;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = this.getSharedPreferences(KEY_SHAREDPREF_FILE, Context.MODE_PRIVATE);
        int sliderRatio = sharedPreferences.getInt(KEY_SLIDER_RATIO, KEY_DEFAULT_INT);
        int rootWidth = sharedPreferences.getInt(KEY_ROOT_WIDTH,KEY_DEFAULT_INT);
        setUpSlider(sliderRatio,rootWidth);
    }

    private void setUpSlider(int sliderRatio,int rootWidth){
        root = (ViewGroup)findViewById(R.id.root_slider_view);
        sliderButton = (Button) findViewById(R.id.slider_button);
        sliderBar = (ProgressBar) findViewById(R.id.progress_bar_slider);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        Double calcWidth = ((double) widthFixed * size.x) / 1080;
        width = calcWidth.intValue();

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

        Log.d(TAG_MAIN, "THIS is the layout before setting" + layoutParams.leftMargin);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

