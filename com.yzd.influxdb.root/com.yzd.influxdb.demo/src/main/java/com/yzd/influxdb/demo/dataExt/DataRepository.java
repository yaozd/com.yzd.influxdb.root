package com.yzd.influxdb.demo.dataExt;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ArrayBlockingQueue;
@Slf4j
public enum  DataRepository {
    PRODUCT(1000);

    //--------------------------------------
    DataRepository(int maxSize) {
        this.data =  new ArrayBlockingQueue<String>(maxSize);;
    }
    private ArrayBlockingQueue<String> data;

    /**
     * 插入数据
     * @param data
     */
    public void putData(String data) {
        if(this.data.remainingCapacity()==0){
            log.info("data is full");
            return;
        }
        try {
            this.data.put(data);
        } catch (InterruptedException e) {
            //目前默认这里吃掉中断异常
            //log.error("[InterruptedException:]",e);
        }
    }

    /**
     * 取数据
     * @return
     */
    public String takeData(){
        String value=null;
        try {
            value=this.data.take();
        } catch (InterruptedException e) {
            //目前默认这里吃掉中断异常
            //log.error("[InterruptedException:]",e);
        }
        return value;
    }
}
