package com.cuponation.android.util;

import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

/**
 * Created by goran on 3/14/17.
 */

public class AnimationUtil {


    public static AnimationSet getGrowAndBounceAnimation() {

        ScaleAnimation scaleAnimation = new ScaleAnimation(0.3f, 1.0f, 0.3f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(300);
        scaleAnimation.setFillAfter(false);
        scaleAnimation.setInterpolator(new OvershootInterpolator());

        AlphaAnimation alphaAnimation = new AlphaAnimation(0.3f, 1.0f);
        alphaAnimation.setDuration(300);

        AnimationSet animationSet = new AnimationSet(false);
        animationSet.addAnimation(alphaAnimation);
        animationSet.addAnimation(scaleAnimation);
        animationSet.setDuration(300);

        return animationSet;
    }

    public static Animation getGrowAndBackToOriginal(){
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 1.2f, 1.0f, 1.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(300);
        scaleAnimation.setRepeatMode(Animation.REVERSE);
        scaleAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleAnimation.setFillAfter(false);

        return scaleAnimation;
    }

    public static Animation getGrowToFullSize(){
        ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(300);
        scaleAnimation.setInterpolator(new AccelerateInterpolator());
        scaleAnimation.setFillAfter(false);

        return scaleAnimation;
    }

    public static Animation getScaleDownToBottom(long offset){
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0f, 1f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1f);
        scaleAnimation.setDuration(400);
        scaleAnimation.setFillAfter(true);
        scaleAnimation.setStartOffset(offset);
        scaleAnimation.setInterpolator(new AccelerateInterpolator());

        return scaleAnimation;
    }

    public static void startDiagonalMoveAnimation(View animatedView) {
        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.15f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.15f,
                Animation.RELATIVE_TO_SELF, 0.0f);
        translateAnimation.setRepeatMode(Animation.REVERSE);
        translateAnimation.setRepeatCount(Animation.INFINITE);
        translateAnimation.setDuration(600);
        translateAnimation.setFillAfter(false);
        translateAnimation.setInterpolator(new LinearInterpolator());
        animatedView.startAnimation(translateAnimation);
    }
}
