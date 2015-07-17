# 背景 #
> 前段时间在做应用的性能优化时，分析了下整体请求，profile看到90%的时间更多的是一些外部服务的I/O等待，cpu利用率其实不高，在10%以下。 单次请求的响应时间在50ms左右，所以tps也不会太高，测试环境压力测试过程，受限于环境因素撑死只能到200tps，20并发下。

# I/O #

> 目前一般的I/O的访问速度： L1 > L2 > memory -> disk or network
常见的IO：
  * nas上文件 (共享文件存储)
  * output/xxx (磁盘文件)
  * memcache client /  cat client  (cache服务)
  * database (oracle , mysql)  (数据库)
  * dubbo client  (外部服务)
  * search client (搜索引擎)

# 思路 #

正因为考虑到I/O阻塞，长的外部环境单个请求处理基本都是在几十ms，刚开始的第一个思路是页面做ajax处理。
### 使用ajax的几个缺陷： ###
  * 功能代码需进行重构，按照页面需求进行分块处理。 一次ajax请求返回一块的页面数据
  * 数据重复请求。因为代码是分块，两次ajax中获取的member对象等，可能就没法共用，会造成重复请求。
  * ajax加载对seo不优化，公司还是比较注重seo，因为这会给客户带来流量价值，而且是免费的流量。
  * ajax技术本身存在一些磕磕碰碰的点： 跨域问题，返回数据问题，超时处理等。
  * ajax处理需要有嵌入性，每个开发都需要按照ajax特有的规范或者机制进行编码，有一定的约束

顺着ajax的思路，是否有一种方式可以很好的解决I/O阻塞，并且又尽量的透明化，也不存在ajax如上的一些问题。

所以就有了本文的异步并行加载机制的研究。原理其实和ajax的有点类似：
**一般ajax的请求：**
  * request就代表html页面的一次渲染过程
  * 首先给页面的一块区域渲染一块空的div id=A内容和一块div id=B的内容
  * 浏览器继续渲染页面的其他内容
  * 在页面底部执行具体的js时，发起div id=A的请求，等A返回后填充对应的div内容，发起div id=B的请求，返回后同样填充。

说明：不同浏览器有不同的机制，默认执行js都是串行处理。


---

# 诞生 #
结合ajax的思路，用java实现了一套异步并行加载。同时增强了异步加载为并行加载，提升效率。

异步并行机制的优点：
  * 继承了ajax异步加载的优点
  * 增加了并行加载的特性
相比于ajax的其他优势：
  * 同时不会对页面seo有任何的影响，页面输出时都是一次性输出html页面
  * 减少了ajax异步发起的http请求
  * 两块代码的资源不会存在重复请求，允许进行资源共享

一个异步并行加载的例子：
```
ModelA modelA = serviceA.getModel(); //1. 异步发起请求  
ModelB modelB = serviceB.getModel(); //2. 异步发起请求  
// 3. 此时serviceA和serviceB都在各自并行的加载model  
if(modelA.isOk()){//4. 此时依赖了modelA的结果，阻塞等待modeA的异步请求的返回  
    ModelC modelC = servicec.getModel(); //5. 异步发起请求  
}  
// 6.  此时serviceB和serviceC都在各自并行的加载model  
......  
modelB.xxxx() //7. 数据处理，modelB已经异步加载完成，此时不会阻塞等结果了  
modelC.xxxx() //8. 数据处理，modelB已经异步加载完成，此时不会阻塞等结果了 
```

传统代码和异步并行加载的比较： 提升总体响应时间上

![http://dl.javaeye.com/upload/attachment/423274/bc5877e7-a673-32e3-b8d9-e4f8236d8f11.png](http://dl.javaeye.com/upload/attachment/423274/bc5877e7-a673-32e3-b8d9-e4f8236d8f11.png)