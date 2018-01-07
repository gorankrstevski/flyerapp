package com.cuponation.android.ui.view;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Created by goran on 7/18/17.
 */

/**
 * Linear layout that intercept touches before they are sent to children view
 */
public class InterceptableRelativeLayout extends RelativeLayout {
    public InterceptableRelativeLayout(Context context) {
        super(context);
    }

    public InterceptableRelativeLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public InterceptableRelativeLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public InterceptableRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(onTouchListener!=null){
            onTouchListener.onTouch(null, ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    private OnTouchListener onTouchListener;

    public void setInterceptableTouchListener(OnTouchListener touchListener){
        this.onTouchListener = touchListener;
    }
}
