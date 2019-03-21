package com.yzd.influxdb.demo.utils.okHttpClientExt;

import com.sun.org.apache.regexp.internal.RE;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

/**
 * 解决retrofit OKhttp创建大量对外连接时内存溢出
 * https://blog.csdn.net/tianyaleixiaowu/article/details/78811488
 */
public class OkHttpClientUtil {

    public static OkHttpClient.Builder generateBuilder(){
        return new OkHttpClient().newBuilder()
                //考虑到每次请求都是一次性的，所以我修改了ConnectionPool的keepAliveDuration时间，让每次连接1秒后就关闭。
                .connectionPool(new ConnectionPool(5,1,TimeUnit.SECONDS));
    }
}
