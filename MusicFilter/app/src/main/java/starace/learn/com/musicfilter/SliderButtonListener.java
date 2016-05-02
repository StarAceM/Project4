package starace.learn.com.musicfilter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

/**
 * Created by mstarace on 4/28/16.
 */
public class SliderButtonListener extends View  implements View.OnTouchListener {
    private static final String TAG_LISTENER = "SliderButtonListener";
    private static final int INVALID_POINTER = -2;
    private int curPointer;
    private int _xDelta;
    private ProgressBar sliderBar;
    private ViewGroup root;
    private Context context;

    public SliderButtonListener(Context context, AttributeSet attrs, ViewGroup root, ProgressBar sliderBar) {
        super(context, attrs);
        this.root = root;
        this.sliderBar = sliderBar;
        this.context = context;
    }

    public boolean onTouch(View view, MotionEvent event) {

        final int X = (int) event.getRawX();

        switch (MotionEventCompat.getActionMasked(event) ){
            case MotionEvent.ACTION_DOWN:

                curPointer = event.getActionIndex();
                RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                _xDelta = X - lParams.leftMargin;

                view.setBackground(getResources().getDrawable(R.drawable.button_border,null));

                curPointer = MotionEventCompat.getPointerId(event,0);
                break;

            case MotionEvent.ACTION_UP:
                RelativeLayout.LayoutParams endParams = (RelativeLayout.LayoutParams) view.getLayoutParams();

                SharedPreferences sharedPreferences = context
                        .getSharedPreferences(MainActivity.KEY_SHAREDPREF_FILE, Context.MODE_PRIVATE);
                sharedPreferences.edit().putInt(MainActivity.KEY_SLIDER_RATIO,sliderBar.getProgress())
                        .putInt(MainActivity.KEY_ROOT_WIDTH,root.getWidth())
                        .apply();
                Log.d(TAG_LISTENER, "THis is the progress bar position " + sliderBar.getProgress());
                Log.d(TAG_LISTENER,"This is position of left margin " + endParams.leftMargin);
                view.setBackground(getResources().getDrawable(R.drawable.button_shape, null));
                curPointer = INVALID_POINTER;
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
                if (X - _xDelta > (root.getWidth() - view.getWidth())) {
                    layoutParams.leftMargin = root.getWidth() - view.getWidth();
                } else if (X - _xDelta <= 0) {
                    layoutParams.leftMargin = 0;
                }

                view.setLayoutParams(layoutParams);
                MainActivity.setSliderProgress(view,root,sliderBar);

                break;
        }
        root.invalidate();
        return true;
    }

}
