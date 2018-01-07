package com.cuponation.android.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;

import java.io.IOException;
import java.net.URL;

/**
 * Created by goran on 3/23/17.
 */

public class BitmapUtil {

    public static Bitmap getBitmap(String url) {
        try {
            return BitmapFactory.decodeStream(new URL(url).openConnection().getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap overlay(Bitmap bmp2, int scalledSize) {
        int size = bmp2.getWidth();
        int offset = (size - bmp2.getHeight()) / 2 ;
        Bitmap bmOverlay = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bmp2, 0, offset, null);
        return Bitmap.createScaledBitmap(bmOverlay, scalledSize, scalledSize, false);
    }
}
