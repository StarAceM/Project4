package starace.learn.com.musicfilter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import starace.learn.com.musicfilter.Song.SongListFragment;

/**
 * Created by mstarace on 4/28/16.
 */
public class SliderButtonListener extends View implements View.OnTouchListener{
    private static final String TAG_LISTENER = "SliderButtonListener";
    private int _xDelta;
    private ProgressBar sliderBar;
    private ViewGroup root;
    private Context context;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetectorCompat doubleTapDetector;
    private View buttonView;
    private SongListFragment songListFragment;
    private int width;
    private int curLeftMargin;

    /**
     * slider button listener constructor
     * @param context
     * @param attrs
     * @param root
     * @param sliderBar
     */
    public SliderButtonListener(Context context, AttributeSet attrs, ViewGroup root, ProgressBar sliderBar) {
        super(context, attrs);
        this.root = root;
        this.sliderBar = sliderBar;
        this.context = context;
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        doubleTapDetector = new GestureDetectorCompat(context,new DoubleTapDetector());
    }

    /**
     * onTouch method handles all the touch events
     * scaleGestureDetector handles all events that scale the button
     * doubleTapDetector handles the double tap events
     * the switch(action) handles the moving and updating of the button state in realtime
     * using public methods of the MainActivity
     * @param view
     * @param event
     * @return
     */
    public boolean onTouch(View view, MotionEvent event) {
        buttonView = view;
        width = buttonView.getWidth();
        RelativeLayout.LayoutParams startParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        curLeftMargin = startParams.leftMargin;
        setBPMValueForMain();
        final int action = MotionEventCompat.getActionMasked(event);
        final int X = (int) event.getRawX();

        scaleGestureDetector.onTouchEvent(event);
        doubleTapDetector.onTouchEvent(event);

        if(checkTouchLocation(view,(int)event.getRawX(),(int)event.getRawY())) {

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();

                    _xDelta = X - lParams.leftMargin;

                    view.setBackground(getResources().getDrawable(R.drawable.button_border, null));

                    break;

                case MotionEvent.ACTION_UP:
                    RelativeLayout.LayoutParams endParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                    this.width = view.getWidth();
                    this.curLeftMargin = endParams.leftMargin;

                    SharedPreferences sharedPreferences = context
                            .getSharedPreferences(context.getString(R.string.key_shared_pref_file), Context.MODE_PRIVATE);
                    sharedPreferences.edit().putInt(context.getString(R.string.key_slider_ratio), sliderBar.getProgress())
                            .putInt(context.getString(R.string.key_button_width), width)
                            .apply();

                    Log.d(TAG_LISTENER, "THis is the progress bar position " + sliderBar.getProgress());
                    Log.d(TAG_LISTENER, "This is position of left margin " + curLeftMargin);
                    Log.d(TAG_LISTENER, "THIS IS THE BUTTON WIDTH " + width);
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
                    curLeftMargin = layoutParams.leftMargin;
                    view.setLayoutParams(layoutParams);
                    MainActivity.setSliderProgress(view, root, sliderBar);
                    setBPMValueForMain();
                    break;
            }

        } else {
            view.setBackground(getResources().getDrawable(R.drawable.button_shape, null));
        }

            return true;
    }

    /**
     * ScaleListener takes in a touch event and determines if it is a scale event then
     * calculates the change and updates the size in realtime
     */
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

    /**
     * Catches the double tap event starts a search using the current range and tempo settings
     */
    private class DoubleTapDetector extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            songListFragment.updateAdapterOnDoubleTap(calcTempo(), calcRange());
            Log.d(TAG_LISTENER, "double tap has occured");
            return true;
        }
    }

    /**
     * sets the button size based on input from the scaleGestureDetector
     * @param spanX
     */
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
        this.width = layoutParams.width;
        Log.d(TAG_LISTENER, "This is the width of the button " + layoutParams.width);
        MainActivity.setSliderProgress(buttonView, root, sliderBar);
        SetBPMRange setBPMRange = (SetBPMRange) context;
        setBPMRange.setRange(calcRange());
        buttonView.setLayoutParams(layoutParams);

    }

    /**
     * Determines if the current touch had occurred in the area of the slider button
     * @param view
     * @param x
     * @param y
     * @return
     */
    private boolean checkTouchLocation(View view, int x, int y) {
        Rect outRect = new Rect();
        int[] location = new int[2];

        view.getDrawingRect(outRect);
        view.getLocationOnScreen(location);
        outRect.offset(location[0], location[1]);
        return outRect.contains(x, y);

    }

    /**
     * interface to send temp and range from the listener to the SongListFragment
     */
    public interface UpdateAdapterOnDoubleTap{
       void updateAdapterOnDoubleTap(float tempo, float range);
    }

    /**
     * used to pass the correct fragment to the SliderButtonListener
     * @param songListFragment
     */
    public void setFragmentToListener(SongListFragment songListFragment){
        Log.d(TAG_LISTENER, "The Fragment has been passed to the SliderButtonListener");
        this.songListFragment = songListFragment;
    }

    /**
     * calculates the tempo from with and leftMargin
     * @return
     */
    private float calcTempo(){
        float flWidth = this.width;
        float flLeftMargin = this.curLeftMargin;

        float position = flLeftMargin + (flWidth/2);

        return 60.0f + ((position - 200.0f)/4.86f);
    }

    /**
     * calculate the range from the button width
     * @return
     */
    private float calcRange(){
        float flWidth = this.width;

        return 5.0f +((flWidth - 400.0f)/5.44f);

    }

    /**
     * sends the bpm range to the main activity to update
     * nowPlaying layout
     */
    public interface SetBPMRange{
        void setRange(float range);
    }

    /**
     * sends the bpm value to the main activity to update
     * nowPlaying Layout
     */
    public interface SetBPMValue{
        void setBPMValue(float value);
    }

    /**
     * method to trigger the sending of bpm values to MainActivity
     */
    public void setBPMValueForMain() {
        SetBPMValue setBPMValue = (SetBPMValue) context;
        setBPMValue.setBPMValue(calcTempo());
    }

}
