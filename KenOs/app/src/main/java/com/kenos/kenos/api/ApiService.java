package com.kenos.kenos.api;

import com.kenos.kenos.model.beauty.BeautyResult;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by shidai on 2016/4/24.
 */
public interface ApiService {
    @GET("/data/imgs?tag=%e5%85%a8%e9%83%a8&from=1&rn=20&col=%e7%be%8e%e5%a5%b3")
    Observable<BeautyResult> getBeautyResult(
            @Query("pn") int pn
    );
}
