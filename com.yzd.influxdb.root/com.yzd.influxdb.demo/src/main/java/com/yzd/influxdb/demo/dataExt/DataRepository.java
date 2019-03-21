package com.yzd.influxdb.demo.dataExt;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ArrayBlockingQueue;

@Slf4j
public enum  DataRepository {
    PRODUCT(1000);

    //--------------------------------------
    //参考：
    //10.并发包阻塞队列之ArrayBlockingQueue
    //
    /**
     * 数据传输
     * @param maxSize 队列传输的最大容量
     */
    DataRepository(int maxSize) {
        this.data =  new ArrayBlockingQueue<String>(maxSize);;
    }
    private ArrayBlockingQueue<String> data;

    /**
     * 插入数据
     * @param data
     */
    public void putData(String data) {
        //offer-非阻塞
        //offer(e)//队列未满时，返回true；队列满时返回false。非阻塞立即返回。
        this.data.offer(data);
        //data.put()-阻塞,如果使用不好，会产生大量线程驻留。
       /* try {
            this.data.put(data);
        } catch (InterruptedException e) {
            //目前默认这里吃掉中断异常
            //log.error("[InterruptedException:]",e);
        }*/
    }

    /**
     * 取数据
     * @return
     */
    public String takeData(){
        //poll-非阻塞
        return this.data.poll();
        //data.take()-阻塞,如果使用不好，会产生大量线程驻留。
        /*String value=null;
        try {
            value=this.data.take();
        } catch (InterruptedException e) {
            //目前默认这里吃掉中断异常
            //log.error("[InterruptedException:]",e);
        }
        return value;*/
    }
}
