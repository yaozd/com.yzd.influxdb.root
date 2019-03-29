package com.yzd.influxdb.demo.influxdbV2;

import com.yzd.influxdb.demo.dataExt.DataRepository;
import com.yzd.influxdb.demo.influxdbExt.InfluxDBConnection;
import com.yzd.influxdb.demo.utils.fastJsonExt.FastJsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class InfluxDBUtil4BatchInsertTest {

    public InfluxDBUtil4BatchInsertTest() {
        log.info("InfluxDBUtil.init");
        InfluxDBUtil.init("http://192.168.1.238:8086", "admin", "admin");
    }

    @Rule
    public ContiPerfRule i = new ContiPerfRule();
    @Test
    public void influxdbEntityToJson(){
        InfluxdbEntiy item= InfluxdbEntiy.build()
                .addTage("t1","t1")
                .addTage("t2","t2")
                .addField("f1",1);
        String itemJson= FastJsonUtil.serialize(item);
        InfluxdbEntiy itemNew=FastJsonUtil.deserialize(itemJson,InfluxdbEntiy.class);
        BatchPoints batchPoints = BatchPoints.builder().build();
        batchPoints.point(InfluxdbEntiy.toPoint("tb-yzd",itemNew));
        InfluxDBUtil.batchInsert("db-test", new InfluxDBUtil.InfluxDBBatchInsertCallback() {
            @Override
            public void doCallBack(String database, InfluxDB influxDB) {
                influxDB.write(batchPoints);
            }
        });
    }
    @Test
    @PerfTest(threads = 20, duration = 1000000000)
    //@PerfTest(threads = 20,invocations = 10000000)
    public void batchInsert(){
        for (int i = 0; i < 200; i++) {
            InfluxdbEntiy item= InfluxdbEntiy.build()
                    //pkg=保证批量数据全部插入到数据库
                    //批量数据导入:必须设置addTage("pkg",i+"")，才可以保证数据全部插入，否则数据会有遗漏
                    .addTage("pkg",DataRepository.PRODUCT.incrementAndGet()+"")
                    .addTage("t1","t1")
                    .addTage("t2","t2")
                    .addField("f1",1)
                    .addField("f2",i);
            String itemJson= FastJsonUtil.serialize(item);
            //数据可以通过同步阻塞队列
            DataRepository.PRODUCT.putData(itemJson);
        }
        //读取数据
        List<String> data = DataRepository.PRODUCT.batchData(100,1);
        log.info("data size="+data.size());
        //转换数据
        BatchPoints batchPoints = BatchPoints.builder().build();
        for (String itemJson:data) {
            InfluxdbEntiy item=FastJsonUtil.deserialize(itemJson,InfluxdbEntiy.class);
            batchPoints.point(InfluxdbEntiy.toPoint("tb-yzd",item));
        }
        InfluxDBUtil.batchInsert("db-test", new InfluxDBUtil.InfluxDBBatchInsertCallback() {
            @Override
            public void doCallBack(String database, InfluxDB influxDB) {
                influxDB.write(batchPoints);
            }
        });

    }
}
