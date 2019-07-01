# askForBaidu
Java多线程大量调用百度地图的搜索功能，来做课程作业数据准备
## 读取xls
采用了apache.poi,说实话，使用体验上来说肯定没有python的pandas还用，
## 多线程共享资源
主要共享的就是一个sheet对象和访问列标row_number，由于对数据表只是读操作，写操作在单独对一列，所以觉得应该没有脏读对问题，所以对于sheet对象没有使用对象锁，
对于row_number因为我在代码里多次用到，包括读写，判断，自增操作，一开始打算把这个变量的自增操作写成方法，然后用全局锁做互斥，这个时候也把int变量标记为volatile...
之后又改用了AtomicInteger，确实好用多了。每条线程的request请求相互独立，写操作也是相互独立，所有线程共同遍历一个数组，当遍历完成时跳出，判断完成原来打算使用futureTask.isDone()去判断，但是后来发现这是非阻塞的，主线程每次会循环判断，很吃资源，所以该用get()，阻塞式的就不会，只执行一次。
## 仍存在的问题
因为百度地图并发量的计算是按每秒钟的访问量来定的，所以即使只开了10个线程，也很容易超并发量（只有50）；所以可能需要写一个反馈器来控制并发在50以下。resr.xls放着试验数据，NAN表示超并发量了，返回的是无用的数据。（太多了，头疼）
