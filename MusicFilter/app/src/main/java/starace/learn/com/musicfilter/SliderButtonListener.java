package starace.learn.com.musicfilter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

/**
 * Created by mstarace on 4/28/16.
 */
public class SliderButtonListener extends View implements View.OnTouchListener {
    private static final String TAG_LISTENER = "SliderButtonListener";
    private int _xDelta;
    private ProgressBar sliderBar;
    private ViewGroup root;
    private Context context;
    private ScaleGestureDetector scaleGestureDetector;
    private View buttonView;

    public SliderButtonListener(Context context, AttributeSet attrs, ViewGroup root, ProgressBar sliderBar) {
        super(context, attrs);
        this.root = root;
        this.sliderBar = sliderBar;
        this.context = context;
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    public boolean onTouch(View view, MotionEvent event) {
        buttonView = view;
        final int action = MotionEventCompat.getActionMasked(event);
        final int X = (int) event.getRawX();

        scaleGestureDetector.onTouchEvent(event);

        if(checkTouchLocation(view,(int)event.getRawX(),(int)event.getRawY())) {

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                    _xDelta = X - lParams.leftMargin;

                    view.setBackground(getResources().getDrawable(R.drawable.button_border, null));

                    break;

                case MotionEvent.ACTION_UP:
                    RelativeLayout.LayoutParams endParams = (RelativeLayout.LayoutParams) view.getLayoutParams();

                    SharedPreferences sharedPreferences = context
                            .getSharedPreferences(MainActivity.KEY_SHAREDPREF_FILE, Context.MODE_PRIVATE);
                    sharedPreferences.edit().putInt(MainActivity.KEY_SLIDER_RATIO, sliderBar.getProgress())
                            .putInt(MainActivity.KEY_BUTTON_WIDTH, view.getWidth())
                            .apply();
                    Log.d(TAG_LISTENER, "THis is the progress bar position " + sliderBar.getProgress());
                    Log.d(TAG_LISTENER, "This is position of left margin " + endParams.leftMargin);
                    Log.d(TAG_LISTENER, "THIS IS THE BUTTON WIDTH " + view.getWidth());
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

                    if (X - _xDelta > (root.getWidth() - view.getWidth())) {
                        layoutParams.leftMargin = root.getWidth() - view.getWidth();
                    } else if (X - _xDelta <= 0) {
                        layoutParams.leftMargin = 0;
                    }

                    view.setLayoutParams(layoutParams);
                    MainActivity.setSliderProgress(view, root, sliderBar);

                    break;
            }

        } else {
            view.setBackground(getResources().getDrawable(R.drawable.button_shape, null));
        }

            return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float spanX = detector.getPreviousSpanX() - detector.getCurrentSpanX();
            Log.d(TAG_LISTENER, "thIS IS THE SPANX " + spanX);
            setButtonSize(spanX);
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            buttonView.setBackground(getResources().getDrawable(R.drawable.button_shape, null));
            super.onScaleEnd(detector);

        }
    }

    private void setButtonSize(float spanX){
        int curWidth = buttonView.getWidth();
        int curLeftMargin = buttonView.getLeft();
        int newWidth;

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) buttonView.getLayoutParams();
        newWidth = curWidth - (int) spanX;
        Log.d(TAG_LISTENER, "this is the newWidth " + newWidth);
        layoutParams.leftMargin = curLeftMargin - (int) (spanX /2.0f);

        if (newWidth >= root.getWidth()){
            layoutParams.width = root.getWidth();
        } else if (newWidth < 400){
            layoutParams.width = 400;
        } else {
            layoutParams.width = newWidth;
        }

        Log.d(TAG_LISTENER, "This is the width of the button " + layoutParams.width);
        MainActivity.setSliderProgress(buttonView, root, sliderBar);
        buttonView.setLayoutParams(layoutParams);

    }

    private boolean checkTouchLocation(View view, int x, int y) {
        Rect outRect = new Rect();
        int[] location = new int[2];

        view.getDrawingRect(outRect);
        view.getLocationOnScreen(location);
        outRect.offset(location[0], location[1]);
        return outRect.contains(x, y);

    }
}
