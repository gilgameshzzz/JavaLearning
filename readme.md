# 问题一：请谈谈你对volatile的理解
一、volatile 是java虚拟机提供的轻量级的同步机制；**有三大特性：** 1.保证可见性；2.不保证原子性；3.禁止指令重排（volatile实现禁止指令重排优化，从而避免多线程环境下程序出现**乱序**执行的现象）
二、JMM(java内存模型java Memory Model) **特性：可见性，原子性，有序性**
*在变量前加上volatile，一个线程对这个变量进行修改，就及时通知其他线程，主物理内存的变量值已经被修改，其他线程的变量值就修改了*

![JMM模型](/java/java线程.png)

本身是一种抽象的概念**并不真实存在**，它描述的是一组规则或规范，通过这组规范定义了程序中各个变量（包括实例字段，静态字段和构成数组对象的元素）的访问方式。
### JMM关于同步的规定：
1、线程解锁前，必须把共享变量的值刷新回主内存；<br>
2、线程加锁前，必须读取主内存的最新值到自己的工作内存；<br>
3、加锁解锁是同一把锁；

由于JVM运行程序的实体是线程，而每个线程创建时JVM都会为其创建一个工作内存（或称栈空间），工作内存是每个线程的私有数据区域，而java内存模型中规定所有变量都存储在**主内存**，主内存是共享内存区域，所有线程都可以访问，**但线程对变量的操作（读取赋值等）必须在工作内存中进行，首先要将变量从主内存拷贝的自己的工作内存空间，然后对变量进行操作，操作完成后再将变量写回主内存**，不能直接操作主内存中的变量，各个线程中的工作内存中存储着主内存中的变量副本拷贝，因此不同的线程无法访问对方的工作内存，线程间的通信必须通过主内存来完成。
### 重排
计算机在执行程序时，为了提高性能，编译器和处理器的常常会对**指令做重排**，分三种：1、单线程环境里面确保程序最终执行结果和代码顺序执行的结果一致；
2、处理器在进行重排序时必须要考虑指令之间的**数据依赖性**；
3、多线程环境中线程*交替执行*，由于编译器优化重排的存在，两个线程中使用的变量能否保证一致性是无法确定的，结果无法预测。

源代码 -->编译器优化的重排 -->指令并行的重排 --> 内存系统的重排 -->最终执行的指令

### 如何禁止指令重排
内存屏障（Memory Barrier）又称内存栅栏，是一个CPU指令，它的作用有两个：
1、保证特定操作的执行顺序；
2、保证某些变量的内存可见性（利用该特性实现volatile的内存可见性）
**通过插入内存屏障就禁止在内存屏障前后的指令执行重排序优化**，内存屏障另一个作用是强制刷出各种CPU的缓存数据，因此任何CPU上的线程都能读取到这些数据的最新版本（保证可见性）

## 在哪些方面用到volatile？
### 单例模式DCL代码
### 单例模式volatile分析

**DCL(双端检锁)机制不一定线程安全，原始是有指令重排序的存在（某一个线程执行到第一次检测，读取到的instance不为bull时，instance的引用对象可能没有*完成初始化*。），加入volatile可以禁止指令重排**
指令重排只会保证串行语义的执行的一致性（单线程），但并不会关系多线程间的语义一致性。**所以当一条线程访问instance不为null时，由于instance实例未必已初始化完成，也就造成了线程安全问题。**


# 问题二：CAS
CAS的全称为 Compare-And-Swap，它是一条CPU并发原语。它的功能是判断内存某个位置的值是否为预期值，如果是则更改为新的值，这个过程是原子的。

**总结**：CAS比较当前工作内存中的值和主内存中的值，如果相同则执行规定操作，否则继续比较直到主内存和工作内存中的值一致为止。

**CAS应用**：CAS有3个操作数，内存值V，旧的预期值A，要修改的更新值B。当且仅当预期值A和内存值V相同时，将内存值V修改为B，否则什么都不做。


### CAS底层原理
CAS并发原语体现在Java中的sun.misc.Unsafe类中的各个方法。调用Unsafe类中的CAS方法，JVM会实现出CAS汇编指令，完全依赖于**硬件**的功能。**并且原语的执行必须是连续的，在执行过程中不允许被中断，也就是说CAS是一条CPU的原子指令，不会造成所谓的数据不一致问题。**

1、Unsafe 是CAS的核心类，由于Java方法无法直接访问底层系统，需要通过本地（native）方法来访问，Unsafe 相当于一个后门，基于该类可以直接操作特定内存的数据。Unsafe类存在于sun.misc包中，其内部方法操作可以像C的指针一样直接操作内存，因为Java中CAS操作的执行依赖于Unsafe类的方法。
**注意Unsafe类中的所有方法都是native修饰的，也就是说Unsafe类中的方法都直接调用操作系统底层资源执行相应任务**<br>
2、变量valueOffset，表示该变量值在内存中的偏移地址，因为Unsafe就是根据**内存偏移地址获取数据**的。<br>
3、变量value用volatile修饰，保证了多线程之间的内存可见性

![](.\java\1.png)
![](.\java\2.png)
![](.\java\3.png)


### CAS缺点：
1、循环时间长，开销大
2、只能保证一个共享变量的原子操作
3、引出ABA问题

---
CAS --->Unsafe --->CAS底层思想 --->ABA ---> 原子引用更新 ---> 如何规避ABA问题

---

### ABA问题
CAS会导致“ABA问题”
CAS算法实现一个重要前提需要提取内存中某时刻的数据并在当下时刻比较并替换，那么在这个时间差类会导致数据的变化。

比如说：一个线程one从内存位置v取出A，这时候另一个线程two也从内存取出A，并且线程two进行了一些操作将值变为B，然后线程two又将V位置的数据变为A，这个时候线程one进行CAS操作发现内存中仍然是A，然后线程one操作成功。**尽管线程one的CAS操作成功，但不代表这个过程就是没有问题的。**

### 解决ABA问题
--理解原子引用+新增一种机制，就是修改版本号（类似时间戳）--
 
# 问题三 ArrayList线程不安全，请编写一个不安全的case,并给出解决方案。(map，set也不安全)

*HashSet底层是HashMap, set在使用add方法时（实际使用map.put方法），之所以只用传一个参数，是因为传入的值被当作key,而value是一个默认的PRESENT的object。CopyOnWriteArraySet底层是CopyOnWriteArrayList*

解决map线程不安全，可以用ConcurrentHashMap;
之所以不安全，是因为add方法没有加锁
常见异常：java.util.ConcurrentModificationException

### 1、故障现象：
java.util.ConcurrentModificationException
### 2、导致原因:并发争抢修改；
### 3、解决方案
① 使用vector,加了锁，并发性下降： new vector<>();<br>
② 使用Collections.synchronizedList(new ArrayList<>());<br>
③ 使用 new CopyOnWriteArrayList<>();<br>
  CopyOnWrite容器即写时复制的容器，往一个容器添加元素时，先将当前容器进行copy,复制新的容器object[]newElements,然后新的容器里添加元素，添加完成后再将原容器的引用指向新的容器，这样的好处是可以对CopyOnWrite容器进行并发的读，而不需要加锁，因为当前容器不会添加任何元素，所以CopyOnWrite容器也是一种读写分离的思想，读和写不同的容器；
                    

# 问题四 公平锁/非公平锁/可重入锁/递归锁/自旋锁谈谈你的理解，请手写一个自旋锁：

## 公平锁
指多个线程按照申请锁的顺序来获取锁，类似排队打饭，先来后到。
## 非公平锁
指多个线程获取锁的顺序并不是按照申请锁的顺序，有可能后申请的线程比先申请的线程优先获取锁，在高并发的情况下，有可能会造成优先级反转或者饥饿现象（有一个线程一个锁也没有获取到）。

并发包中ReentrantLock的创建可以指定构造函数的Boolean类型来得到公平锁或非公平锁，默认是非公平锁。
### 关于两者的区别
**公平锁**：就是公平，在并发环境中，每个线程在获取锁时会先查看此锁维护并等待队列。<br>
**非公平锁**：上来就直接尝试占有锁，如果尝试失败，就再采用类似公平锁那种方式。
非公平锁的优点在于吞吐量比公平锁大，对于Synchronized而言，也是一种非公平锁。

## 可重入锁（又名递归锁）
指同一线程外层函数获得锁之后，内层递归函数仍然能获取该锁的代码，在同一个线程在外层方法获取锁的时候，在进入内层方法会自动获取锁，也就是说**线程可以进入任何一个它已经拥有的锁所同步着的代码块。**
ReentrantLock/Synchronized就是一个典型的可重入锁，可重入锁最大的作用是避免死锁。（加锁几次，解锁几次，程序不会报错，解锁少一次程序就会卡死）

## 自旋锁(Spinlock)
指尝试获取锁的线程不会立即阻塞，而是采用循环的方式去尝试获取锁，这样的好处是减少线程上下文切换的消耗，缺点是循环会消耗CPU.(CAS就是自旋锁)
```java
//Unsafe.getAndAddInt
public final int getAndAddInt(Object var1, long var2, int var4){
  int var5
  do{
    var5 = this.getIntVolatile(var1, var2);
  }while(!this.compareAndSwapInt(var1,var2,var5,var5+var4));
  return var5
}
```


## 独占锁（写锁）/共享锁（读锁）/互斥锁
**独占锁**：指该锁一次只能被一个线程所持有。对ReentrantLock和Synchronized而言都是独占锁。<br>
**共享锁**：指该锁可被多个线程所持有。对ReentrantReadWriteLock其读锁是共享锁，其写锁是独占锁。读锁、共享锁可保证并发读是非常高效的，读写，写读，写写的过程是互斥的。


# 问题五 CountDownLatch/CyclicBarrier/Semaphore使用过吗？

**CountDownLatch**:让一些线程阻塞直到另一些线程完成一系列操作后才被唤醒；
CountDownLatch主要有两个方法，当一个或多个线程调用await方法时，调用线程会被阻塞。其他线程调用countDown方法会将计数器减1（调用counDown方法的线程不会阻塞），当计数器值为0时，因调用await方法被阻塞的线程就会被唤醒，继续执行。

**CyclicBarrier**:字面意思是可循环（Cylic）使用的屏障（Barrier）。它要做的事情是，让一组线程到达一个屏障（也可以叫同步点）时被阻塞，直到最后一个线程到达屏障时，屏障才会开门，所有被屏障拦截的线程才会继续干活，线程进入屏障通过CyclicBarrier的await()方法。（与CountDownLatch相反）

**Semaphore**:信号量主要用于两个目的，一个是用于多个共享资源的互斥使用，另一个用于并发线程数的控制。

# 问题六：阻塞队列
**ArrayBlockingQueue**:是一个基于数组结构的有界限阻塞队列，此队列按FIFO(先进先出)原则对元素进行排序。

**LinkedBlockingQueue**:是一个基于链表结构的阻塞队列，此队列按FIFO排序元素，吞吐量通常要高于ArrayBlockingQueue。

**SynchronousQueue**:一个不存储元素的阻塞队列。每个插入操作必须等到另一个线程调用移除操作，否则插入操作一直处于阻塞状态，吞吐量通常要高于LinkedBlockingQueue。

**阻塞队列**：
顾名思义，首先它是一个队列，一个阻塞队列在数据结构作用如下图：
![](.\java\BlockingQueue.png)
当阻塞队列是空时，从队列获取元素的操作将会被阻塞(试图从空的阻塞队列中获取元素的线程将会被阻塞，直到其他的线程往空的队列插入新的元素)；

当阻塞队列是满时，往队列添加元素的操作将会被阻塞(试图往已满的阻塞队列添加新元素的线程同样也会被阻塞，直到其他的线程从队列删除一个或多个元素或者清空队列，使队列变得空闲后新增)。

## 为什么需要BlockingQueue
好处是我们不需要关心什么时候需要阻塞线程，什么时候需要唤醒线程，因为这一切BlockingQueue都一手包办了。不需要兼顾效率和线程安全。

**种类分析**：<br>
**ArrayBlockingQueue**:有数组结构组成的有界阻塞队列；<br>
**LinkedBlockingQueue**：由链表结构组成的有界（但大小默认值为Integer.MAX_VALUE）阻塞队列；<br>
PriorityBlockingQueue:支持优先级排序的无界阻塞队列；<br>
DelayQueue：使用优先级队列实现的延迟无界阻塞队列；<br>
**SynchronousQueue**：不存储元素的阻塞队列，也即单个元素的队列；<br>
LinkedTransferQueue:由链表结构组成的无界阻塞队列；<br>
LinkedBlockingDeque:由链表结构组成的双向阻塞队列。

BlockingQueue核心方法:
![](.\java\BlockingQueueMethod.png)
![](.\java\4.png)

### SynchronousQueue
SynchronousQueue没有容量，与其他BlockingQueue不同，SynchronousQueue是一个不存储元素的BlockingQueue。每一个put操作必须要等待一个take操作，否则不能继续添加元素，反之亦然。

### 用在哪里

#### 生产者消费模式
多线程的判断用while判断，用if会出现虚假唤醒现象。


# 问题：Synchronized和lock有什么区别，用新的lock有什么好处？

**1、原始构成：**<br>synchronized是关键字属于JVM层面，monitorenter(底层是通过monitor对象来完成，其实wait/notify等方法也依赖于monitor)<br>
Lock是具体类（Java.util.concurrent.locks.lock）是api层面的锁.<br>
**2、使用方法：**<br>synchronized 不需要用户去手动释放锁，当synchronized代码执行完成后系统会自动让线程释放对锁的占用。<br>ReentrantLock则需要手动释放锁，若没有主动释放锁，就有可能导致出现死锁现象，需要lock()和unLock()方法配合try/finally语句块来完成。<br>
**3、等待是否可中断：**<br>synchronized不可中断，除非抛出异常或者正常运行完成<br>ReentrantLock可中断：①设置超时方法trylock(long timeout,TimeUnit unit)。②lockInterruptibly()放代码块中，调用interrupt()方法可中断<br>
**4、加锁是否公平：**<br>
synchronized非公平锁<br>
ReentrantLock两者都可以，默认非公平锁，构造方法可以传入Boolean值，true为公平锁，false为非公平锁。<br>
**5、锁绑定多个条件Condition**<br>
synchronized没有<br>
ReentrantLock用来实现分组唤醒需要唤醒的线程们，可以精确唤醒，而不是像synchronized要么随机唤醒一个线程，要么全部唤醒。

# 线程池
线程池做的工作主要是控制运行线程的数量，**处理过程中将任务放入队列**，然后在线程创建后启动这些任务，**如果线程数量超过了最大数量的线程，超出的线程就要排队等候**，等待其他线程执行完毕，再从队列中取出任务来执行。<br>
线程池主要特点或优势：**线程复用，控制最大并发数量，管理线程**<br>
或者:<br>
①：降低资源消耗。通过重复利用已创建的线程降低线程创建和销毁造成的消耗。<br>
②：提高响应速度。当任务到达时，任务可以不需要等到线程创建，能立即执行。<br>
③：提高线程的可管理性，线程是稀缺资源，如果无限制的创建，不仅会消耗系统资源，还会降低系统的稳定性，使用线程池可以进行统一的分配，调优和监控。<br>
java中的线程池是通过Executor框架实现的，该框架中用到了Executor，Executors,ExecutorService,ThreadPoolExecutor这几个类。（底类是ThreadPoolExecutor）<br>

## java 使用多线程的方式:
①继承线程类，②使用Runable接口(没有返回值，不抛异常)，③使用Callable接口(有返回值，会抛异常)，④使用线程池<br>

### Executors重点方法：
Executors.newFixedThreadPool(),自己写开多少个线程,常用于执行长期的任务，性能好很多<br>
Executors.newSingleThreadExecutor只开启一个线程，常用于一个任务一个任务执行的场景<br>
Executors.newCachedThreadPool(),系统自己决定开多少线程。常用于执行很多短期异步的小程序或者负载较轻的服务。
