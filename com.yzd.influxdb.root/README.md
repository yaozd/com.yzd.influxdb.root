> 版本说明
- V2-InfluxDBUtil(目前推荐此版本-byArvin-2019-03-20)
- V1-InfluxDBConnection

> 使用方法
- 批量数据导入-InfluxDBUtil4BatchInsertTest.batchInsert()
```

```

> 参考
- [10.并发包阻塞队列之ArrayBlockingQueue](http://www.cnblogs.com/yulinfeng/p/6986975.html)
```
Java并发包中的阻塞队列一共7个，当然他们都是线程安全的。 
　　ArrayBlockingQueue：一个由数组结构组成的有界阻塞队列。 
　　LinkedBlockingQueue：一个由链表结构组成的有界阻塞队列。 
　　PriorityBlockingQueue：一个支持优先级排序的无界阻塞队列。 
　　DealyQueue：一个使用优先级队列实现的无界阻塞队列。 
　　SynchronousQueue：一个不存储元素的阻塞队列。 
　　LinkedTransferQueue：一个由链表结构组成的无界阻塞队列。 
　　LinkedBlockingDeque：一个由链表结构组成的双向阻塞队列。（摘自《Java并发编程的艺术》）
```
>问题一：
> 解决批量插入中因使用阻塞队列take而产生大量线程驻留问题
- InfluxDBUtilTest.batchInsertByBlockingQueue2

### InfluxDB 插入数据 “数据丢失”
> 问题二：如何保证批量数据全部插入到数据库-避免数据丢失-addTage("pkg",i+"")
```
批量数据导入必须设置addTage("pkg",i+"")，才可以保证数据全部插入，否则数据会有遗漏
Pkg的作用主要是用于防止influxdb,批量导入数据丢失问题
------
数据丢失问题的原因：
influxdb 插入数据格式为 insert measurement,tag=value field=value timestamp
是按照时间存储的
对于 measurement tagkey, tagvalue 和 timestamp 一样的，field 会被最新的值替换
（可以理解为“更新”操作）
------
解决方案：
DataRepository.incrementAndGet
```
> 问题三：[解决retrofit OKhttp创建大量对外连接时内存溢出](https://blog.csdn.net/tianyaleixiaowu/article/details/78811488)
```
通过各方查证，有的说，请求header里的Connection，写的是keep alive，导致了长连接，所以我把构建retrofit的header的地方改成了Connection为close，然而没什么卵用。依旧是上面的问题，很快线程数超过就崩溃了。 
后来开始调查OkHttpClient的ConnectionPool，这个就是OkHttp网络请求的线程池，在OkHttpClient源码中可以看到

public OkHttpClient.Builder connectionPool(ConnectionPool connectionPool) {
            if (connectionPool == null) {
                throw new NullPointerException("connectionPool == null");
            } else {
                this.connectionPool = connectionPool;
                return this;
            }
        }

在OkHttpClient的源码中，默认的构造方法里可以看到默认最大线程空闲数是5，keepAlive时间为5分钟。也就是发起一次网络连接后，5分钟内不会断开连接。
那么问题就出在这里了，我在短时间内发起了大量网络连接，每个是一个线程，而且每个都默认保存5分钟，很快线程数就超标了。 
考虑到我的每次请求都是一次性的，所以我修改了ConnectionPool的keepAliveDuration时间，让每次连接1秒后就关闭。
之后再次运行程序，发现OK了，线程数最大也没超过200，程序也没再抛出过outofmemery异常。
----------------------------------------------------------------
public class OkHttpClientUtil {

    public static OkHttpClient.Builder generateBuilder(){
        return new OkHttpClient().newBuilder()
                .connectionPool(new ConnectionPool(5,1,TimeUnit.SECONDS));
    }
}
---------------------------------------------------------------- 
--------------------- 
作者：天涯泪小武 
来源：CSDN 
原文：https://blog.csdn.net/tianyaleixiaowu/article/details/78811488 
版权声明：本文为博主原创文章，转载请附上博文链接！
```