import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class ReSortSeqDemo {
    int a = 0;
    boolean flag = false;

    public void method1() {
        a = 1;
        flag = true;
    }

    /*多线程环境中线程*交替执行*，由于编译器优化重排的存在，
    两个线程中使用的变量能否保证一致性是无法确定的，结果无法预测
     */
    public void method2() {
        if (flag) {
            a += 5;
            System.out.println("*****reValue:" + a);
        }
    }

};

//单例模式
class SingletonDemo {
    private static volatile SingletonDemo instance = null;

    private SingletonDemo() {
        System.out.println(Thread.currentThread().getName() + "\t 构造方法SingletonDemo()");
    }

    // DCL(Double Check Lock 双端检锁机制)
    public static SingletonDemo getInstance() {
        if (instance == null) {
            synchronized (SingletonDemo.class) {
                if (instance == null) {
                    instance = new SingletonDemo();
                }
            }
        }
        return instance;
    }
}


class Mydata {
    //    int number = 0;  程序运行3s后，下面的while循环依然会一直循环运行
    volatile int number = 0;

    public void addTo60() {
        this.number = 60;
    }

    public void addPlusPlus() {
        number++;
    }

    AtomicInteger atomicInteger = new AtomicInteger();

    public void addAtomic() {
        atomicInteger.getAndIncrement();
    }
}

/* 验证volatile 的可见性
1加入int number= 0;number变量之前根本没有添加volatile关键字修饰，没有可见性
        加入voLatile，解决可见性
2 验证volatile 不保证原子性
        不可分割，完整性，要么同时成功，要么同时失败
     如何解决？ 1、加sync 2、使用AtomicInteger
* */
public class volatileDemo {
    public static void main(String[] args) {
        //单线程
//        System.out.println(SingletonDemo.getInstance() == SingletonDemo.getInstance());
//        System.out.println(SingletonDemo.getInstance() == SingletonDemo.getInstance());
//        System.out.println(SingletonDemo.getInstance() == SingletonDemo.getInstance());

        // 并发多线程后，情况发生很大的变化
        for (int i = 1; i <= 10; i++) {
            new Thread(() -> {
                SingletonDemo.getInstance();
            }, String.valueOf(i)).start();
        }
    }

    private static void yuanzixing() {
        Mydata mydata = new Mydata();
        for (int i = 1; i <= 10; i++) {
            new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    mydata.addPlusPlus();
                    mydata.addAtomic();
                }
            }, String.valueOf(i)).start();
        }
        //获取最终值
        while (Thread.activeCount() > 2) {
            Thread.yield();
        }
        System.out.println(Thread.currentThread().getName() + "\t int type finally number value:" + mydata.number);
        System.out.println(Thread.currentThread().getName() + "\t AtomicInteger finally number value:" + mydata.atomicInteger);
    }

    // volatile保证可见性，及时通知其他线程，主物理内存的值已经被修改
    private static void seeOkByVolatile() {
        Mydata mydata = new Mydata();
        new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + "\t come in");
            //暂停一会线程
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mydata.addTo60();
            System.out.println(Thread.currentThread().getName() + "\t update number" + mydata.number);
        }, "AAA").start();

        while (mydata.number == 0) {
            //main线程就一直再这里等待循环，直到number不再等于0
        }
        System.out.println(Thread.currentThread().getName() + "\t mission is over");
    }
}
