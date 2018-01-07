package com.cuponation.android.ui.dialog;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cuponation.android.R;
import com.cuponation.android.model.Voucher;
import com.cuponation.android.ui.activity.RetailerVouchersActivity;
import com.cuponation.android.util.Constants;
import com.squareup.picasso.Picasso;

/**
 * Created by goran on 12/5/17.
 */

public class ExpiredVoucherDialog extends DialogFragment {


    private Voucher voucher;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_template, container, false);

        View buttons = inflater.inflate(R.layout.dialog_voucher_expired, null);
        ((ViewGroup) view.findViewById(R.id.dialog_container)).addView(buttons);

        final Dialog dialog = getDialog();

        ((TextView)view.findViewById(R.id.dialog_title))
                .setText(voucher.isCode() ? R.string.cn_app_expire_code_no_voucher : R.string.cn_app_expire_deal_no_voucher);

        Picasso.with(getActivity()).load(voucher.getRetailerLogoUrl()).into((ImageView) view.findViewById(R.id.dialog_image));

        view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent(getActivity(), RetailerVouchersActivity.class);
                intent.putExtra(Constants.EXTRAS_RETAILER_ID, voucher.getRetailerId());
                startActivity(intent);
            }
        });
        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        return view;
    }

    public void setVoucher(Voucher voucher) {
        this.voucher = voucher;
    }
}
