package starace.learn.com.musicfilter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;

/**
 * Created by mstarace on 5/2/16.
 */
public class CustomSliderButton extends Button implements ScaleGestureDetector.OnScaleGestureListener, View.OnTouchListener{

    public CustomSliderButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    ScaleGestureDetector scaleDetector =
            new ScaleGestureDetector(getContext(), this);

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return false;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (scaleDetector.onTouchEvent(event)){
            return true;
        }
        return super.onTouchEvent(event);
    }
}
