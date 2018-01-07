package com.cuponation.android.util;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.provider.Settings;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.cuponation.android.R;
import com.cuponation.android.app.CouponingApplication;
import com.cuponation.android.model.Retailer;
import com.cuponation.android.model.Voucher;
import com.cuponation.android.ui.adapter.GridSpacingItemDecoration;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by goran on 7/21/16.
 */

public class Utils {

    public static final Set<String> UNIQUE_CODES = new HashSet<>();
    static {
        UNIQUE_CODES.add("unique codes");
        UNIQUE_CODES.add("x");
    }

    public static void copyCodeToClipboard(String code) {
        ClipboardManager clipboard = (ClipboardManager)
                CouponingApplication.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("code", code);

        clipboard.setPrimaryClip(clip);
    }

    public static void showFeedback(Context context, int messageId) {
        Toast.makeText(context, messageId, Toast.LENGTH_SHORT).show();
    }

    public static void showFeedback(Context context, String messageId) {
        Toast.makeText(context, messageId, Toast.LENGTH_SHORT).show();
    }

    public static Comparator<Voucher> getVoucherComparator() {
        return new Comparator<Voucher>() {
            @Override
            public int compare(Voucher o1, Voucher o2) {
                if (o1.getType().equals(o2.getType())) {
                    return 0;
                } else {
                    if (o1.getType().equals(Voucher.TYPE_CODE)) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            }
        };
    }

    public static List<Voucher> getCodeVouchers(Retailer retailer) {

        List<Voucher> codes = new ArrayList<>();

        if (retailer.getVouchers() != null && retailer.getVouchers().size() > 0) {
            for (Voucher voucher : retailer.getVouchers()) {
                if (Voucher.TYPE_CODE.equals(voucher.getType())) {
                    codes.add(voucher);
                }
            }
        }

        return codes;
    }

    public static boolean isUniqueCodePresent(Retailer retailer) {
        List<Voucher> codeVouchers = getCodeVouchers(retailer);
        return codeVouchers.size() == 1 && isUniqueCode(codeVouchers.get(0));
    }

    public static boolean isUniqueCode(Voucher voucher) {
        return voucher.getCode() != null && UNIQUE_CODES.contains(voucher.getCode().toLowerCase());
    }

    public static String stripAccentsAndSpaces(String s) {

        if (s != null) {
            s = s.toLowerCase();
            s = Normalizer.normalize(s, Normalizer.Form.NFD);
            s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
            s = s.replace(", ", ",").replace(" ", "_");
            s = s.replaceAll("[,;:\\-\\+]", "_");
        }

        return s;
    }

    public static GridSpacingItemDecoration getItemDecoration(Activity activity, int itemWidthResourceId, int itemMargins, int itemCount) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        float itemSize = activity.getResources().getDimensionPixelSize(itemWidthResourceId);
        float itemMargin = 0;
        if (itemMargins != -1) {
            itemMargin = activity.getResources().getDimensionPixelSize(itemMargins);
        }
        final int space = (int) ((width - (itemCount * (itemSize + 2 * itemMargin))) / itemCount);

        return new GridSpacingItemDecoration(itemCount, space, false);
    }

    public static int getItemCount(Activity activity, int itemWidthResourceId) {

        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;


        float itemSize = activity.getResources().getDimensionPixelSize(itemWidthResourceId);
        return (int) Math.floor(width / itemSize);
    }

    public static String getUniqueDeviceId(Context context, String voucherEndDate) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID) + voucherEndDate;
    }

    public static Bitmap loadBitmapFromVoucherView(Voucher voucher, Context context, int layoutId, int width, int height) {
        final Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(layoutId, null);

        // populate fields with voucher data
        TextView type = (TextView) layout.findViewById(R.id.voucher_type);
        type.setText(voucher.isCode() ? context.getString(R.string.cn_app_code) : context.getString(R.string.cn_app_deal));
        if (Voucher.TYPE_DEAL.toLowerCase().equals(voucher.getType().toLowerCase())) {
            type.setBackgroundResource(R.drawable.deal_bg);
        } else {
            type.setBackgroundResource(R.drawable.code_bg);
        }
        TextView title = (TextView) layout.findViewById(R.id.voucher_text);
        title.setText(voucher.getShortTitle());
        TextView category = (TextView) layout.findViewById(R.id.voucher_category);
        category.setText(voucher.getCategory());
        TextView endDate = (TextView) layout.findViewById(R.id.voucher_date);
        TextView status = (TextView) layout.findViewById(R.id.voucher_status);
        if (voucher.getVerified() == null) {
            endDate.setVisibility(View.VISIBLE);
            if (voucher.getEndDate() != null) {
                endDate.setText(TimeUtil.getExpireTime(voucher.getEndDate(), context));
            }
            status.setVisibility(View.INVISIBLE);
        } else {
            endDate.setVisibility(View.GONE);
            status.setVisibility(View.VISIBLE);
        }


        layout.setDrawingCacheEnabled(true);
        layout.measure(View.MeasureSpec.makeMeasureSpec(canvas.getWidth(), View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(canvas.getHeight(), View.MeasureSpec.EXACTLY));
        layout.layout(0, 0, layout.getMeasuredWidth(), layout.getMeasuredHeight());
        canvas.drawBitmap(layout.getDrawingCache(), 0, 0, new Paint());
        return bmp;
    }

    public static Map<String, Integer> sortByValue(Map<String, Integer> unsortMap) {

        // 1. Convert Map to List of Map
        List<Map.Entry<String, Integer>> list =
                new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

        // 2. Sort list with Collections.sort(), provide a custom Comparator
        //    Try switch the o1 o2 position for a different order
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        // 3. Loop the sorted list and put it into a new insertion order Map LinkedHashMap
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            return;
        }
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

}
