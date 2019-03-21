package com.yzd.influxdb.demo.influxdbV2;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.influxdb.dto.Point;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Data
@NoArgsConstructor
public class InfluxdbEntiy implements Serializable {
    private Map<String,String> tags=new HashMap<>();
    private Map<String,Object> fields=new HashMap<>();

    public InfluxdbEntiy addTage(String key,String value){
        this.tags.put(key,value);
        return this;
    }
    public InfluxdbEntiy addField(String key,Object value){
        this.fields.put(key,value);
        return this;
    }
    public static InfluxdbEntiy build(){
        return new InfluxdbEntiy();
    }
    public static Point toPoint(String measurement,InfluxdbEntiy entiy){
        Point point =Point.measurement(measurement).time(System.currentTimeMillis(), TimeUnit.MILLISECONDS).tag(entiy.getTags()).fields(entiy.getFields()).build();
        return point;
    }
}
