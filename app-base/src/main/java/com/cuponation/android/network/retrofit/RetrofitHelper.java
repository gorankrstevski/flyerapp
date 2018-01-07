package com.cuponation.android.network.retrofit;

import android.content.Context;
import android.util.Log;

import com.cuponation.android.util.CountryUtil;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.schedulers.Schedulers;
import okhttp3.Cache;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by goran on 2/22/17.
 */

public class RetrofitHelper {

    private static final String TAG = RetrofitHelper.class.getSimpleName();

    private static final int CONNECTION_TIMEOUT = 60;
    private static final long HTTP_RESPONSE_DISK_CACHE_MAX_SIZE = 10 * 1024 * 1024; // 10MB

    public static Retrofit getRetrofitAdapter(Context context){
        return new Retrofit.Builder()
                .baseUrl(CountryUtil.getCountryBaseUrl())
                .client(getOkHttpClient(context))
                .addConverterFactory(new NullOnEmptyConverterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build();
    }

    public static Retrofit getSimpleRetrofitAdapter(Context context){
        return new Retrofit.Builder()
                .baseUrl(CountryUtil.getCountryBaseUrl())
                .client(getOkHttpClient(context))
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build();
    }

    private static OkHttpClient getOkHttpClient(Context context) {
        OkHttpClient.Builder okClientBuilder = new OkHttpClient.Builder();
        okClientBuilder.addInterceptor(headerAuthorizationInterceptor);
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel( HttpLoggingInterceptor.Level.BASIC);
        okClientBuilder.addInterceptor(httpLoggingInterceptor);
        final File baseDir = context.getCacheDir();
        if (baseDir != null) {
            final File cacheDir = new File(baseDir, "HttpResponseCache");
            okClientBuilder.cache(new Cache(cacheDir, HTTP_RESPONSE_DISK_CACHE_MAX_SIZE));
        }
        okClientBuilder.connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS);
        okClientBuilder.readTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS);
        okClientBuilder.writeTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS);
        return okClientBuilder.build();
    }

    private static final Interceptor headerAuthorizationInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            try {
                Headers headers = request.headers().newBuilder().add("Authorization", "").build();
                request = request.newBuilder().headers(headers).build();
                Response response = chain.proceed(request);
                response.header("Cache-Control", String.format("max-age=%d, only-if-cached, max-stale=%d", 120, 0));
                return response;
            } catch (Throwable throwable) {
                Log.d(TAG, throwable.getClass() + " : " + throwable.getMessage());
            }

            return new Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(404)
                    .body(ResponseBody.create(MediaType.parse("application/json"), ""))
                    .build();
        }
    };
}
