package com.cuponation.android.service;

import android.content.Context;

import com.cuponation.android.callback.UseButtonActionListener;
import com.cuponation.android.service.local.IUseButtonService;
import com.usebutton.sdk.Button;
import com.usebutton.sdk.models.AppAction;


/**
 * Created by goran on 12/6/17.
 */

public class UseButtonService implements IUseButtonService {

    private Context context;

    public UseButtonService(Context context){
        this.context = context;
    }

    @Override
    public void doGetAction(String merchantId, String pubRef, final UseButtonActionListener listener) {
        Button.getButton(context).doGetAction(merchantId, pubRef, new Button.ActionListener() {
            @Override
            public void onAction(AppAction appAction) {
                if (appAction != null) {
                    appAction.invokeAction(context);
                }else{
                    listener.notifyAll();
                }
            }

            @Override
            public void onNoAction() {
                listener.onNoAction();
            }
        });
    }
}
