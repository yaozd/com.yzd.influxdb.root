> 版本说明
- V2-InfluxDBUtil(目前推荐此版本-byArvin-2019-03-20)
- V1-InfluxDBConnection

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
- batchInsertByBlockingQueue2