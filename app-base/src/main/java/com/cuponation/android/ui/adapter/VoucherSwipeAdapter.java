package com.cuponation.android.ui.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cuponation.android.R;
import com.cuponation.android.model.UniqueCode;
import com.cuponation.android.model.Voucher;
import com.cuponation.android.model.eventbus.RetailerVoucherGoToShopEvent;
import com.cuponation.android.model.eventbus.RetailerVoucherOpenEvent;
import com.cuponation.android.service.remote.VoucherService;
import com.cuponation.android.ui.view.AutoResizeTextView;
import com.cuponation.android.util.TimeUtil;
import com.cuponation.android.util.Utils;

import org.greenrobot.eventbus.EventBus;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

/**
 * Created by goran on 3/16/17.
 */

public class VoucherSwipeAdapter extends PagerAdapter {

    private Context context;
    private Voucher voucher;
    private View cardview;
    private boolean isCategoryView;

    public VoucherSwipeAdapter(Context context, Voucher voucher, View cardview, boolean isCategoryView) {
        super();
        this.context = context;
        this.voucher = voucher;
        this.cardview = cardview;
        this.isCategoryView = isCategoryView;
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final View view;
        if (position == 0) {
            if(isCategoryView){
                view = LayoutInflater.from(context).inflate(R.layout.item_list_category_voucher_details, null);
                TextView title = view.findViewById(R.id.voucher_title);
                title.setText(voucher.getShortTitle());
            }else {
                view = LayoutInflater.from(context).inflate(R.layout.item_list_voucher_details, null);
                TextView verified = view.findViewById(R.id.voucher_verified);
                ImageView checkmark = view.findViewById(R.id.voucher_checkmark);

                TextView voucherType = view.findViewById(R.id.voucher_type);
                voucherType.setText(voucher.isCode() ? context.getString(R.string.cn_app_code) : context.getString(R.string.cn_app_deal));

                if (Voucher.TYPE_DEAL.toLowerCase().equals(voucher.getType())) {
                    voucherType.setBackgroundResource(R.drawable.deal_bg);
                    voucherType.setTextColor(ContextCompat.getColor(context, R.color.deal_badge_text_color));

                } else {
                    voucherType.setBackgroundResource(R.drawable.code_bg);
                    voucherType.setTextColor(ContextCompat.getColor(context, R.color.code_badge_text_color));
                }

                if (voucher.getVerified() == null) {
                    verified.setVisibility(View.INVISIBLE);
                    checkmark.setVisibility(View.INVISIBLE);
                } else {
                    verified.setVisibility(View.VISIBLE);
                    checkmark.setVisibility(View.VISIBLE);
                }
            }

            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            view.setLayoutParams(params);

            TextView desc = (TextView) view.findViewById(R.id.voucher_desc);
            TextView endTime = (TextView) view.findViewById(R.id.voucher_end_date);

            desc.setText(voucher.getTitle());
            endTime.setText(TimeUtil.getExpireTime(voucher.getEndDate(), context));

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EventBus.getDefault().post(new RetailerVoucherOpenEvent(voucher, cardview));
                }
            });

        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_list_voucher_actions, null);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            view.setLayoutParams(params);

            Button toTheShop = (Button) view.findViewById(R.id.voucher_copy_and_go_to_shop);
            final TextView voucherCode = (TextView) view.findViewById(R.id.voucher_code);
            if (voucher.isCode()) {
                toTheShop.setText(R.string.cn_app_copy_and_go);
                voucherCode.setVisibility(View.VISIBLE);
                if(!Utils.isUniqueCode(voucher)){
                    voucherCode.setText(voucher.getCode());
                }

            } else {
                toTheShop.setText(R.string.cn_app_to_the_shop);
                voucherCode.setVisibility(View.GONE);
            }
            toTheShop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    voucher.setCode(voucherCode.getText().toString());
                    EventBus.getDefault().post(new RetailerVoucherGoToShopEvent(voucher));
                }
            });

        }
        container.addView(view);
        return view;
    }

    @Override
    public void setPrimaryItem(final ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);

        if (position == 1) {
            if (Utils.isUniqueCode(voucher)) {
                VoucherService.getInstance().getUniqueCode(voucher.getVoucherId(), Utils.getUniqueDeviceId(context, voucher.getEndDate()))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<UniqueCode>() {
                            @Override
                            public void accept(UniqueCode code) throws Exception {
                                ((AutoResizeTextView) container.findViewById(R.id.voucher_code)).setText(code.getCode());
                                ((AutoResizeTextView) container.findViewById(R.id.voucher_code)).resizeText();
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {

                            }
                        });
            }
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
