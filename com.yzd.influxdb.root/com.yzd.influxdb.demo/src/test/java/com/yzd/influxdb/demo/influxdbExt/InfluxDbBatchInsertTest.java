package com.yzd.influxdb.demo.influxdbExt;

import lombok.extern.slf4j.Slf4j;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
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
public class InfluxDbBatchInsertTest {
    @Rule
    public ContiPerfRule i = new ContiPerfRule();
    //通过PerfTest制定并发线程数和执行时间，如threads=100，duration=15000。这里15000表示15秒。
    @Test
    @PerfTest(threads = 20,duration=1000000)
    public void batchInsert_performance() throws InterruptedException {
        InfluxDBConnection influxDBConnection = new InfluxDBConnection("admin", "admin", "http://192.168.1.238:8086", "db-test", null);
        BatchPoints batchPoints=BatchPoints.database("db-test").build();
        for (int i = 0; i< 1000; i++){
            //Thread.sleep(10);
            Point point = Point.measurement("cpu33")
                    .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                    .addField("num0",1)
                    .addField("num1",1)
                    .addField("num2",1)
                    .addField("num3",1)
                    .addField("num4",1)
                    .addField("num5",1)
                    .addField("num6",1)
                    .addField("num7",1)
                    .tag("pkg",i+"")
                    .tag("statusCode1","statusCode")
                    .tag("statusCode2","statusCode")
                    .tag("statusCode3","statusCode")
                    .tag("statusCode4","statusCode")
                    .tag("statusCode5","statusCode")
                    .tag("statusCode6","statusCode")
                    .build();
            batchPoints.point(point);
        }
        try{
            influxDBConnection.batchInsert(batchPoints);
        }finally {
            influxDBConnection.close();
        }
    }
    //通过PerfTest制定并发线程数和invocations指定调用的次数。
   /* @Test
    //@PerfTest(threads = 1,invocations=10000)
    @PerfTest(threads = 1,duration=1000000)
    public void readBatchData() throws InterruptedException {
        List<String> data=getBatchData();
       log.info("data.size()="+data.size());
    }
    *//**
     * 条件：执行时间最大为1秒或者读取1000条数据。
     * 因使用阻塞队列take而产生大量线程驻留，导致内存溢出 问题
     * @return
     *//*
    private List<String> getBatchData(){
        List<String> data=new ArrayList<>();
        //因使用阻塞队列take而产生大量线程驻留，导致内存溢出 问题
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(() -> {
            for (int i = 0; i < 1000; i++) {
                //数据可以通过同步阻塞队列或者是Redis的消息队列等
                data.add(String.valueOf(i));
            }
        });
        //解决线程驻留问题》executorService.shutdown();+executorService.awaitTermination(1,TimeUnit.SECONDS);
        //如果没有执行shutdown就会出现线程驻留无法回收问题。
        executorService.shutdown();
        try {
            executorService.awaitTermination(1,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return data;
    }*/
}
