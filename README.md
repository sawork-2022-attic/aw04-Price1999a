# WebPOS

The demo shows a web POS system , which replaces the in-memory product db in aw03 with a one backed by 京东.


![](jdpos.png)

To run

```shell
mvn clean spring-boot:run
```

Currently, it creates a new session for each user and the session data is stored in an in-memory h2 db. 
And it also fetches a product list from jd.com every time a session begins.

1. Build a docker image for this application and performance a load testing against it.
2. Make this system horizontally scalable by using haproxy and performance a load testing against it.
3. Take care of the **cache missing** problem (you may cache the products from jd.com) and **session sharing** problem (you may use a standalone mysql db or a redis cluster). Performance load testings.

Please **write a report** on the performance differences you notices among the above tasks.

## 任务记录

gatling 脚本配置完成

修复了两个会导致ko的bug 其一是给没有pid的东西重新分配pid 其二是cart里默认放一个东西否则购物车渲染不出来（这是前端问题 前端不会改 只能如此绕过）

另外新增了一个离线模式，防止jd网站不给我数据

docker打包
mvn compile jib:dockerBuild

haproxy安装 运行
haproxy -f [配置文件]

目前程序第一次运行时 会从jd搜索相关信息生成 这里需要使用数据库缓存？
redis-5.0.8/utils/create-cluster> start create

拓展session sharing  使用同一个redis集群存

我们不用redis集群存了，这玩意根本连不上 见鬼去吧！！

现在用一个运行在容器中的redis单点存
docker run -itd --name redis-aw04 --net aw04  -p 6379:6379 redis
docker network create -d bridge aw04

还需要处理的任务

测试水平拓展

## 实验简要报告

这是一次针对系统水平拓展后的压力测试对比。

系统是为每个用户创建一个新会话，并且会话数据存储在一个运行在容器上的`redis`服务器中。
同时系统从`jd.com`中获取的商品数据也会被缓存在此`redis`服务器中。
由于对`jd.com`搜索服务的过度调用导致此服务暂时不可用，增加了一个离线模式 ——
即可以在无法获取搜索结果时返回一个又预先设置的商品构成的商品列表，同时在此模式下加入了一个线程上的`sleep(500)`，模拟网络连接耗时。

实际上，本次实验最开始的想法是使用运行在宿主机上的`redis`集群的。
在宿主机上直接运行一切正常，但是当容器化之后，容器内的应用程序就无法连接宿主机上的`redis`集群了。
尝试的解决方案包括使用其他容器尝试`telnet`宿主机`redis`集群对应端口——正常、
容器使用`host.docker.internal`——似乎被解析到`127.0.0.1`、
容器使用其他容器在相同网络环境中对`host.docker.internal`DNS出的IP地址——无法连接、
容器使用`bridge`模式下的网关`172.17.0.1`——无法连接、
容器使用`host`模式共享宿主机网络栈——无法连接、
`Docker`创建自定义`bridge`模式以获得完整DNS能力——仍无法连接。
基于以上策略的失效，最终换用了一个容器化的`redis`服务器完成实验。

对`redis`集群与`redis`服务器的性能简要分析：

由于实际上两者都是运行在单机上的，因此`redis`集群很难认为在实验中可以得到实质性的性能差别。
具体而言，`redis`涉及到的读写包含两个部分：session与商品数据。
session是需要在每一次处理请求时读写，此时集群将他们通过一个`Hash`算法分开到三个服务器上，而单点服务器将直接读写。
商品数据作为缓存，在集群上实际上仍然是由一台服务器维护的。此时集群与单点没有本质差异。

### 实验环境搭建的简要说明

这次实验中，web服务器与数据库服务是被容器化的。

压力测试采用`Gatling`工具。

水平拓展使用了`haproxy`在tcp层处理。

`Gatling`工具使用3.7.6版，使用的测试脚本在`./testaw04`目录下。
测试脚本模拟50个用户并发访问主页与添加一个购物车——共100个请求。

`haproxy`使用的配置文件即`./haproxy.cfg`，表示将在`8080`端口的请求通过轮询分散到`8081`、`8082`、`8083`、`8084`4个服务器上。

`Docker`容器化采用`Dockerfile`。

### 实验结果

- 单服务器实验


```shell
#使用如下命令运行容器
docker run --net aw04 -p 8080:8080 --cpus=0.5 -d pos-web

================================================================================
---- Global Information --------------------------------------------------------
> request count                                        100 (OK=100    KO=0     )
> min response time                                  13892 (OK=13892  KO=-     )
> max response time                                  18247 (OK=18247  KO=-     )
> mean response time                                 16033 (OK=16033  KO=-     )
> std deviation                                       1422 (OK=1422   KO=-     )
> response time 50th percentile                      16016 (OK=16016  KO=-     )
> response time 75th percentile                      17286 (OK=17286  KO=-     )
> response time 95th percentile                      18102 (OK=18102  KO=-     )
> response time 99th percentile                      18197 (OK=18197  KO=-     )
> mean requests/sec                                   3.03 (OK=3.03   KO=-     )
---- Response Time Distribution ------------------------------------------------
> t < 800 ms                                             0 (  0%)
> 800 ms < t < 1200 ms                                   0 (  0%)
> t > 1200 ms                                          100 (100%)
> failed                                                 0 (  0%)
================================================================================
```

- 4服务器结果

```shell
#docker运行命令：

docker run --net aw04 -p 8081:8080 --cpus=0.5 --name s1 -d pos-web
docker run --net aw04 -p 8082:8080 --cpus=0.5 --name s2 -d pos-web
docker run --net aw04 -p 8083:8080 --cpus=0.5 --name s3 -d pos-web
docker run --net aw04 -p 8084:8080 --cpus=0.5 --name s4 -d pos-web

================================================================================
---- Global Information --------------------------------------------------------
> request count                                        100 (OK=100    KO=0     )
> min response time                                   2465 (OK=2465   KO=-     )
> max response time                                   3043 (OK=3043   KO=-     )
> mean response time                                  2736 (OK=2736   KO=-     )
> std deviation                                        127 (OK=127    KO=-     )
> response time 50th percentile                       2739 (OK=2739   KO=-     )
> response time 75th percentile                       2843 (OK=2843   KO=-     )
> response time 95th percentile                       2907 (OK=2907   KO=-     )
> response time 99th percentile                       2973 (OK=2973   KO=-     )
> mean requests/sec                                 16.667 (OK=16.667 KO=-     )
---- Response Time Distribution ------------------------------------------------
> t < 800 ms                                             0 (  0%)
> 800 ms < t < 1200 ms                                   0 (  0%)
> t > 1200 ms                                          100 (100%)
> failed                                                 0 (  0%)
================================================================================
```

我们可以看到，单服务器上，平均每秒处理3.03个请求，而水平拓展后，这个值大幅上涨到16.667个请求每秒。（重复多次数据类似）

这可能是因为redis缓存发挥了作用。
也可能是因为m1芯片的大小核在不同任务负载下调度所致，可能在高任务负载：水平拓展下，任务会被分配到高性能核心上运行。

## 增补 0328

基于0328老师的要求完善了整个网站购物车的增删，另外对于session share问题，由于sppring boot架构的高度抽象，所以不论性能，redis单服务器与集群在此问题上是等价的。

整体而言，session是参考了sa-spring/spring-session-jdbc项目中，用一个AutoWired的HttpSession来处理这个问题。

## 增补 0329
通过群友 解决了无法使用宿主机redis集群的问题。