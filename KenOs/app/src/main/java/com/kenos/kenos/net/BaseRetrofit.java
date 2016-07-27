package com.kenos.kenos.net;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * User: hxk(huangxikang@520dyw.cn)
 * Date: 2016-07-27
 * Time: 16:57
 * Description:
 */
public abstract class BaseRetrofit {

    protected static final int DEFAULT_TIMEOUT = 10;

    protected abstract String acquireBaseApiUrl();

    protected Retrofit getRetrofit() {

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        return new Retrofit.Builder()
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(acquireBaseApiUrl())
                .build();
    }
}
