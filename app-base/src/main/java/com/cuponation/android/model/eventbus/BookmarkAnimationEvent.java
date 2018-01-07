package com.cuponation.android.model.eventbus;

import android.graphics.Bitmap;

/**
 * Created by goran on 3/14/17.
 */

public class BookmarkAnimationEvent {

    private final Bitmap bitmap;

    public BookmarkAnimationEvent(Bitmap bitmap){
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
