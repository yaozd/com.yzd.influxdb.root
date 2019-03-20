package com.yzd.influxdb.demo.influxdbExt;

import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class InfluxDBConnectionTest {

    @Test
    public void insert() {
        InfluxDBConnection influxDBConnection = new InfluxDBConnection("admin", "admin", "http://192.168.1.238:8086", "db-test", null);
        Map<String, String> tags = new HashMap<String, String>();
        tags.put("tag1", "标签值");
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("field1", "String类型");
        // 数值型，InfluxDB的字段类型，由第一天插入的值得类型决定
        fields.put("field2", 3.141592657);
        // 时间使用毫秒为单位
        influxDBConnection.insert("tb", tags, fields, System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * 方式一：通过BatchPoints组装数据后，循环插入数据库。
     * PS:使用这两种种方式，要就这两条数据都写入到同一数据库下且tag相同,若tag不相同，需将它们放到不同的BatchPoint对象中，否则会出现数据写入错乱问题。
     */
    @Test
    public void batchInsert_type_1() {
        InfluxDBConnection influxDBConnection = new InfluxDBConnection("admin", "admin", "http://192.168.1.238:8086", "db-test", null);
        Map<String, String> tags = new HashMap<String, String>();
        tags.put("tag1", "标签值");
        Map<String, Object> fields1 = new HashMap<String, Object>();
        fields1.put("field1", "abc");
        // 数值型，InfluxDB的字段类型，由第一天插入的值得类型决定
        fields1.put("field2", 123456.0);
        Map<String, Object> fields2 = new HashMap<String, Object>();
        fields2.put("field1", "String类型");
        fields2.put("field2", 3.141592657);
        // 一条记录值
        Point point1 = influxDBConnection.pointBuilder("tb", System.currentTimeMillis(), tags, fields1);
        Point point2 = influxDBConnection.pointBuilder("tb", System.currentTimeMillis(), tags, fields2);
        // 将两条记录添加到batchPoints中
        BatchPoints batchPoints1 = BatchPoints.database("db-test").tag("tag1", "标签值1").consistency(InfluxDB.ConsistencyLevel.ALL).build();
        BatchPoints batchPoints2 = BatchPoints.database("db-test").tag("tag2", "标签值2").consistency(InfluxDB.ConsistencyLevel.ALL).build();
        batchPoints1.point(point1);
        batchPoints2.point(point2);
        // 将两条数据批量插入到数据库中
        influxDBConnection.batchInsert(batchPoints1);
        influxDBConnection.batchInsert(batchPoints2);
    }

    /**
     *
     */
    @Test
    public void batchInsert_type_2() {
        InfluxDBConnection influxDBConnection = new InfluxDBConnection("admin", "admin", "http://192.168.1.238:8086", "db-test", null);
        Map<String, String> tags1 = new HashMap<String, String>();
        tags1.put("tag1", "标签值");
        Map<String, String> tags2 = new HashMap<String, String>();
        tags2.put("tag2", "标签值");
        Map<String, Object> fields1 = new HashMap<String, Object>();
        fields1.put("field1", "abc");
        // 数值型，InfluxDB的字段类型，由第一天插入的值得类型决定
        fields1.put("field2", 123456.0);
        Map<String, Object> fields2 = new HashMap<String, Object>();
        fields2.put("field1", "String类型");
        fields2.put("field2", 3.141592657);
        // 一条记录值
        Point point1 = influxDBConnection.pointBuilder("tb", System.currentTimeMillis(), tags1, fields1);
        Point point2 = influxDBConnection.pointBuilder("tb", System.currentTimeMillis(), tags2, fields2);
        BatchPoints batchPoints1 = BatchPoints.database("db-test").tag("tag1", "标签值1")
                .consistency(InfluxDB.ConsistencyLevel.ALL).build();
        // 将两条记录添加到batchPoints中
        batchPoints1.point(point1);
        BatchPoints batchPoints2 = BatchPoints.database("db-test").tag("tag2", "标签值2")
                .consistency(InfluxDB.ConsistencyLevel.ALL).build();
        // 将两条记录添加到batchPoints中
        batchPoints2.point(point2);
        // 将不同的batchPoints序列化后，一次性写入数据库，提高写入速度
        List<String> records = new ArrayList<String>();
        records.add(batchPoints1.lineProtocol());
        records.add(batchPoints2.lineProtocol());
        // 将两条数据批量插入到数据库中
        influxDBConnection.batchInsert("db-test", null, InfluxDB.ConsistencyLevel.ALL, records);
    }

}