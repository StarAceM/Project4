package starace.learn.com.musicfilter;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by mstarace on 4/28/16.
 */
public class SliderButtonListener extends View  implements View.OnTouchListener {
    private static final int width = 300;
    private static final int height = 150;
    private int _xDelta;

    public SliderButtonListener(Context context, AttributeSet attrs, View root) {
        super(context, attrs);
    }

    public boolean onTouch(View view, MotionEvent event, View root) {

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
                MainActivity.setSliderProgress(view);

                break;
        }
        root.invalidate();
        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}
