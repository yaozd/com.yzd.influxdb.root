package com.yzd.influxdb.demo.utils.okHttpClientExt;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.util.concurrent.TimeUnit;

/**
 * 解决retrofit OKhttp创建大量对外连接时内存溢出
 * https://blog.csdn.net/tianyaleixiaowu/article/details/78811488
 * 解决 Address already in use: connect 的错误
 * https://blog.csdn.net/woowindice/article/details/4090678
 */
public class OkHttpClientUtil {

    public static OkHttpClient.Builder generateBuilder() {
        return new OkHttpClient().newBuilder()
                .addInterceptor(chain -> {
                    Request.Builder builder=chain.request().newBuilder()
                            //解决 Address already in use: connect 的错误(目前测试此项配置会减少但不能完全解决)
                            //设置connection为一次即关闭，
                            .addHeader("Connection","close");
                    return chain.proceed(builder.build());
                })
                .connectTimeout(5,TimeUnit.MINUTES)
                .readTimeout(5,TimeUnit.MINUTES)
                .writeTimeout(5,TimeUnit.MINUTES)
                //考虑到每次请求都是一次性的，所以我修改了ConnectionPool的keepAliveDuration时间，让每次连接1秒后就关闭。
                .connectionPool(new ConnectionPool(5, 1, TimeUnit.SECONDS));
    }
}
