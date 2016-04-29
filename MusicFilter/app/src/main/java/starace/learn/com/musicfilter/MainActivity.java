package starace.learn.com.musicfilter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener{
    private static final String TAG_MAIN = "MainActivity";
    private static final int width = 300;
    private static final int height = 150;
    Button sliderButton;
    ProgressBar sliderBar;
    ViewGroup root;
    private int _xDelta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        root = (ViewGroup)findViewById(R.id.root_slider_view);

        sliderButton = (Button) findViewById(R.id.slider_button);
        sliderBar = (ProgressBar) findViewById(R.id.progress_bar_slider);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
        layoutParams.leftMargin = 540;
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        sliderButton.setLayoutParams(layoutParams);
        sliderBar.setProgress(50);
        sliderBar.setProgressTintList(ColorStateList.valueOf(Color.rgb(103, 11, 119)));
        sliderButton.setOnTouchListener(this);

    }

    public boolean onTouch(View view, MotionEvent event) {

        final int X = (int) event.getRawX();

        switch (MotionEventCompat.getActionMasked(event) ){
            case MotionEvent.ACTION_DOWN:
                RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                _xDelta = X - lParams.leftMargin;

                view.setBackground(getResources().getDrawable(R.drawable.button_border,null));
                break;
            case MotionEvent.ACTION_UP:
                view.setBackground(getResources().getDrawable(R.drawable.button_shape, null));
                break;
            case MotionEvent.ACTION_POINTER_DOWN:

                break;
            case MotionEvent.ACTION_POINTER_UP:

                break;
            case MotionEvent.ACTION_MOVE:
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                layoutParams.removeRule(RelativeLayout.CENTER_IN_PARENT);
                layoutParams.leftMargin = X - _xDelta;

                //set limits of movement
                if (X - _xDelta > (root.getWidth() - width)) {
                    layoutParams.leftMargin = root.getWidth() - width;
                } else if (X - _xDelta <= 0) {
                    layoutParams.leftMargin = 0;
                }

                view.setLayoutParams(layoutParams);
                setSliderProgress(view);

                break;
        }
        root.invalidate();
        return true;
    }

    public void setSliderProgress (View button) {
        double left = button.getRight() - (button.getWidth()/2);
        double total = root.getWidth();
        Double dProgress = (left/ total) * 100.0;
        Double red = 3.0;
        Double green = 17.0;
        Double blue = 229.0;

        sliderBar.setProgress(dProgress.intValue());

        red = red + 200.0 * (left/total);
        green = green - 17 * (left/total);
        blue = blue - 220 * (left/total);

        sliderBar.setProgressTintList(ColorStateList.valueOf(Color.rgb(red.intValue(), green.intValue(), blue.intValue())));

    }

}

