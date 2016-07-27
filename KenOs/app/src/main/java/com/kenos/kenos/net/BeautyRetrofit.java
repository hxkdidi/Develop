package com.kenos.kenos.net;

import com.kenos.kenos.api.ApiService;
import com.kenos.kenos.api.ConfigKey;
import com.kenos.kenos.model.beauty.BeautyResult;
import com.kenos.kenos.model.beauty.Img;

import java.util.List;

import retrofit2.Retrofit;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by shidai on 2016/5/7.
 */
public class BeautyRetrofit extends BaseRetrofit {
    private Retrofit retrofit;
    private ApiService apiService;

    private static final BeautyRetrofit INSTANCE = new BeautyRetrofit();

    public static BeautyRetrofit getSingleton() {
        return INSTANCE;
    }

    private BeautyRetrofit() {
        retrofit = getRetrofit();
        apiService = retrofit.create(ApiService.class);
    }

    /**
     * 获取妹子资源
     *
     * @param subscriber
     * @param pn
     */
    public void getBeautyPageList(Subscriber<List<Img>> subscriber, int pn) {
        apiService.getBeautyResult(pn)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<BeautyResult, List<Img>>() {
                    @Override
                    public List<Img> call(BeautyResult meiZhiResult) {
                        return meiZhiResult.getImgs();
                    }
                }).subscribe(subscriber);
    }

    @Override
    protected String acquireBaseApiUrl() {
        return ConfigKey.BEAUTY_URL;
    }
}
