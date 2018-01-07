package com.cuponation.android.network.retrofit;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by goran on 7/26/17.
 */

public interface MediaMetrieAPI {

    @GET
    Observable<String> getMediaMetrie(@Url String url);
}
