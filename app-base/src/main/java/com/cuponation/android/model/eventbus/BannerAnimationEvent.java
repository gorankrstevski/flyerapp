package com.cuponation.android.model.eventbus;

/**
 * Created by goran on 3/1/17.
 */

public class BannerAnimationEvent {

    public final boolean shouldAnimationPlay;

    public BannerAnimationEvent(boolean shouldAnimationPlay){
        this.shouldAnimationPlay = shouldAnimationPlay;
    }

    public boolean shouldAnimationPlay(){
        return shouldAnimationPlay;
    }

}
