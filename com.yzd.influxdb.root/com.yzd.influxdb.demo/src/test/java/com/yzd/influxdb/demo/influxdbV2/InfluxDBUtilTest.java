package com.yzd.influxdb.demo.influxdbV2;

import com.yzd.influxdb.demo.dataExt.DataRepository;
import lombok.extern.slf4j.Slf4j;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class InfluxDBUtilTest {
    @Rule
    public ContiPerfRule i = new ContiPerfRule();

    //通过PerfTest制定并发线程数和执行时间，如threads=100，duration=15000。这里15000表示15秒。
    @Test
    @PerfTest(threads = 20, duration = 1000000)
    public void batchInsert_performance() throws InterruptedException {
        InfluxDBUtil.init("http://192.168.1.238:8086", "admin", "admin");
        BatchPoints batchPoints = BatchPoints.builder().build();
        ;
        for (int i = 0; i < 1000; i++) {
            //Thread.sleep(10);
            Point point = Point.measurement("cpu33")
                    .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                    .addField("num0", 1)
                    .addField("num1", 1)
                    .addField("num2", 1)
                    .addField("num3", 1)
                    .addField("num4", 1)
                    .addField("num5", 1)
                    .addField("num6", 1)
                    .addField("num7", 1)
                    .tag("pkg", i + "")
                    .tag("statusCode1", "statusCode11")
                    .tag("statusCode2", "statusCode2")
                    .tag("statusCode3", "statusCode3")
                    .tag("statusCode4", "statusCode4")
                    .tag("statusCode5", "statusCode5")
                    .tag("statusCode6", "statusCode6")
                    .build();
            batchPoints.point(point);
        }
        InfluxDBUtil.batchInsert("db-test", new InfluxDBUtil.InfluxDBBatchInsertCallback() {
            @Override
            public void doCallBack(String database, InfluxDB influxDB) {
                influxDB.write(batchPoints);
            }
        });
    }

    @Test
    @PerfTest(threads = 20, duration = 1000000)
    public void batchInsertByBlockingQueue() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        //插入数据
        executorService.execute(() -> {
            for (int i = 0; i < 200; i++) {
                //数据可以通过同步阻塞队列
                DataRepository.PRODUCT.putData(String.valueOf(i));
            }
        });
        //读取数据
        List<String> data = getBatchData();
        log.info("data.size()=" + data.size());
        //转换数据

        //批量插入到influxdb

        //
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    @PerfTest(threads = 20, duration = 1000000)
    public void batchInsertByBlockingQueue2() throws InterruptedException {
        for (int i = 0; i < 200; i++) {
            //数据可以通过同步阻塞队列
            DataRepository.PRODUCT.putData(String.valueOf(i));
        }
        //读取数据
        List<String> data = getBatchData();
        log.info("data.size()=" + data.size());
        //转换数据

        //批量插入到influxdb

    }

    /**
     * 条件：执行时间最大为1秒或者读取1000条数据。
     *
     * @return
     */
    private List<String> getBatchData() {
        List<String> data = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(() -> {
            for (int i = 0; i < 1000; i++) {
                String value = DataRepository.PRODUCT.takeData();
                //数据可以通过同步阻塞队列或者是Redis的消息队列等
                if (value == null) {
                    continue;
                }
                data.add(value);
            }
        });
        //解决线程驻留问题》executorService.shutdown();+executorService.awaitTermination(1,TimeUnit.SECONDS);
        //如果没有执行shutdown就会出现线程驻留无法回收问题。
        executorService.shutdown();
        try {
            executorService.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return data;
    }
}