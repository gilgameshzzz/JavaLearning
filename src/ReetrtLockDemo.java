import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;

class Phone implements Runnable{
    public synchronized void sendMS() throws Exception {
        System.out.println(Thread.currentThread().getId() + "\t invoked sendMS()");
        sendEmail();
    }

    public synchronized void sendEmail() throws Exception {
        System.out.println(Thread.currentThread().getId() + "\t invoked sendEmail()");
    }

    Lock lock = new ReentrantLock();

    @Override
    public void run() {
        get();
    }
    public void get(){
        lock.lock();
        try {
            System.out.println(Thread.currentThread().getName()+"----get");
            set();
        }finally {
            lock.unlock();
        }
    }
    public void set(){
        lock.lock();
        try{
            System.out.println(Thread.currentThread().getName()+"-------set");
        }finally {
            lock.unlock();
        }
    }
}

//可重入锁
public class ReetrtLockDemo {
    public static void main(String[] args) {
        Phone phone = new Phone();
        new Thread(() -> {
            try {
                phone.sendMS();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "t1").start();

         new Thread(() -> {
            try {
                phone.sendMS();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "t2").start();
         try {
             TimeUnit.SECONDS.sleep(2);
         }catch (InterruptedException e){
             e.printStackTrace();
         }
        System.out.println();
        System.out.println();
         Thread t3 = new Thread(phone);
         Thread t4 = new Thread(phone);
         t3.start();
         t4.start();
    }
}
