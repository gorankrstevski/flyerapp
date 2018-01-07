package com.cuponation.android.ui.view;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.cuponation.android.R;

/**
 * Created by goran on 2/27/17.
 */

public class AddMoreRetailersButton extends LinearLayout {

    private OnClickListener onClickListener;

    public AddMoreRetailersButton(Context context, OnClickListener onClickListener) {
        super(context);
        this.onClickListener = onClickListener;
        initView(context);
    }

    public AddMoreRetailersButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public AddMoreRetailersButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public AddMoreRetailersButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context){
        this.setOrientation(LinearLayout.VERTICAL);
        View view = LayoutInflater.from(context).inflate(R.layout.view_add_more_retailers, this);
        view.findViewById(R.id.add_more_retailers_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onClickListener !=null){
                    onClickListener.onClick(v);
                }
            }
        });
    }

}
