package com.cuponation.android.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;

import com.cuponation.android.R;
import com.google.android.instantapps.InstantApps;

/**
 * Created by goran on 11/7/16.
 */

public class DialogUtil {

    public static void showAlertDialog(Context context) {

        new AlertDialog.Builder(context).setMessage(R.string.cn_app_no_cupons_found)

                .setPositiveButton(R.string.cn_app_settings_alert_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();

    }


    public static void showNoConnectionDialog(final Context context) {

        new AlertDialog.Builder(context).setMessage(R.string.cn_app_check_your_connection)

                .setPositiveButton(R.string.cn_app_settings_alert_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.cn_app_settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                })
                .create().show();

    }

    public static void showInstallAppDialog(final Activity context){
        new AlertDialog.Builder(context)
                .setTitle("Get more with the App")
                .setMessage("Want to receive offers and deals instantly?")

                .setPositiveButton(R.string.cn_app_install_full_app, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        InstantApps.showInstallPrompt(context, 101, "InstallApiActivity");
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.cn_app_settings_alert_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create().show();
    }
}
