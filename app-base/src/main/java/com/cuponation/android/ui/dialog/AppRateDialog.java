package com.cuponation.android.ui.dialog;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cuponation.android.R;
import com.cuponation.android.app.CouponingApplication;
import com.cuponation.android.util.SharedPreferencesUtil;

/**
 * Created by goran on 12/5/17.
 */

public class AppRateDialog extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_template,container,false);

        View buttons = inflater.inflate(R.layout.rate_dialog_buttons, null);
        ((ViewGroup)view.findViewById(R.id.dialog_container)).addView(buttons);
        final SharedPreferencesUtil preferences = SharedPreferencesUtil.getInstance();
        final Dialog dialog = getDialog();

        ((TextView)view.findViewById(R.id.dialog_title))
                .setText(String.format(getString(R.string.cn_app_rate_dialog_title), getString(R.string.app_name)));

        view.findViewById(R.id.btn_maybe_later).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                preferences.setAppRateRemindTimestamp(System.currentTimeMillis() + (DateUtils.DAY_IN_MILLIS*14));
            }
        });
        view.findViewById(R.id.btn_no_thanks).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                preferences.setAppRate(true);
            }
        });
        view.findViewById(R.id.btn_rate_app).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                SharedPreferencesUtil.getInstance().setAppRate(true);
                Uri uri = Uri.parse("market://details?id=" + CouponingApplication.getContext().getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + CouponingApplication.getContext().getPackageName())));
                }
            }
        });

        return view;
    }

    View.OnClickListener doneAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        }
    };

}
