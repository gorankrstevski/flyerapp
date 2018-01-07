package com.cuponation.android.ui.view;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;

/**
 * Created by goran on 7/19/17.
 */

public class CustomEditText extends AppCompatEditText {

    public CustomEditText(Context context) {
        super(context);
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if(TextUtils.isEmpty(getText())) {
                clearFocus();
                return true;
            }
        }
        // return super.onKeyPreIme(keyCode, event);
        return super.dispatchKeyEvent(event);
    }

}