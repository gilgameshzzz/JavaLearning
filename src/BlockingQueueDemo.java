//阻塞队列

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 1、阻塞队列有没有好的一面
 * 2、不得不阻塞，如何管理
 */

public class BlockingQueueDemo {
    public static void main(String[] args)  throws Exception{
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(3);
//        blockingQueue.add("a");
//        blockingQueue.add("b");
//        blockingQueue.add("c");
//        blockingQueue.add("d");   报错：java.lang.IllegalStateException: Queue full

        blockingQueue.put("a");
        blockingQueue.put("b");
        blockingQueue.put("c");
        blockingQueue.put("d");
    }
}
