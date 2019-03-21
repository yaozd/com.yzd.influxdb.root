package com.yzd.influxdb.demo.influxdbV2;

import com.yzd.influxdb.demo.utils.okHttpClientExt.OkHttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BoundParameterQuery;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 版本二：目前推荐使用版本二
 * 参考：Sentinel
 * https://github.com/yaozd/Sentinel/tree/dev-yzd
 */
@Slf4j
@Component
public class InfluxDBUtil {

    private static String url;

    private static String username;

    private static String password;

    private static InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();

    @Value("${influxdb.url}")
    public void setUrl(String url) {
        InfluxDBUtil.url = url;
    }

    @Value("${influxdb.username}")
    public void setUsername(String username) {
        InfluxDBUtil.username = username;
    }

    @Value("${influxdb.password}")
    public void setPassword(String password) {
        InfluxDBUtil.password = password;
    }

    public static void init(String url, String username, String password) {
        InfluxDBUtil.url = url;
        InfluxDBUtil.username = username;
        InfluxDBUtil.password = password;
    }

    public static <T> T process(String database, InfluxDBCallback callback) {
        InfluxDB influxDB = null;
        T t = null;
        try {
            //解决retrofit OKhttp创建大量对外连接时内存溢出:OkHttpClientUtil.generateBuilder()
            influxDB = InfluxDBFactory.connect(url, username, password, OkHttpClientUtil.generateBuilder());
            //influxDB = InfluxDBFactory.connect(url, username, password);
            influxDB.setDatabase(database);
            t = callback.doCallBack(database, influxDB);
        } catch (Exception e) {
            log.error("[process exception]", e);
        } finally {
            if (influxDB != null) {
                try {
                    influxDB.close();
                } catch (Exception e) {
                    log.error("[influxDB.close exception]", e);
                }
            }
        }

        return t;
    }

    public static void insert(String database, InfluxDBInsertCallback influxDBInsertCallback) {
        process(database, new InfluxDBCallback() {
            @Override
            public <T> T doCallBack(String database, InfluxDB influxDB) {
                influxDBInsertCallback.doCallBack(database, influxDB);
                return null;
            }
        });
    }

    public static QueryResult query(String database, InfluxDBQueryCallback influxDBQueryCallback) {
        return process(database, new InfluxDBCallback() {
            @Override
            public <T> T doCallBack(String database, InfluxDB influxDB) {
                QueryResult queryResult = influxDBQueryCallback.doCallBack(database, influxDB);
                return (T) queryResult;
            }
        });
    }

    public static <T> List<T> queryList(String database, String sql, Map<String, Object> paramMap, Class<T> clasz) {
        QueryResult queryResult = query(database, new InfluxDBQueryCallback() {
            @Override
            public QueryResult doCallBack(String database, InfluxDB influxDB) {
                BoundParameterQuery.QueryBuilder queryBuilder = BoundParameterQuery.QueryBuilder.newQuery(sql);
                queryBuilder.forDatabase(database);

                if (paramMap != null && paramMap.size() > 0) {
                    Set<Map.Entry<String, Object>> entries = paramMap.entrySet();
                    for (Map.Entry<String, Object> entry : entries) {
                        queryBuilder.bind(entry.getKey(), entry.getValue());
                    }
                }

                return influxDB.query(queryBuilder.create());
            }
        });

        return resultMapper.toPOJO(queryResult, clasz);
    }

    public static void batchInsert(String database, InfluxDBBatchInsertCallback influxDBBatchInsertCallback) {
        process(database, new InfluxDBCallback() {
            @Override
            public <T> T doCallBack(String database, InfluxDB influxDB) {
                influxDBBatchInsertCallback.doCallBack(database, influxDB);
                return null;
            }
        });
    }

    public interface InfluxDBCallback {
        <T> T doCallBack(String database, InfluxDB influxDB);
    }

    public interface InfluxDBInsertCallback {
        void doCallBack(String database, InfluxDB influxDB);
    }

    public interface InfluxDBBatchInsertCallback {
        void doCallBack(String database, InfluxDB influxDB);
    }

    public interface InfluxDBQueryCallback {
        QueryResult doCallBack(String database, InfluxDB influxDB);
    }
}