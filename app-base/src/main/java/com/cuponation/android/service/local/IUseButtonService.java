package com.cuponation.android.service.local;

import com.cuponation.android.callback.UseButtonActionListener;

/**
 * Created by goran on 12/6/17.
 */

public interface IUseButtonService {


    void doGetAction(String merchantId, String pubRef, UseButtonActionListener listener);
}
