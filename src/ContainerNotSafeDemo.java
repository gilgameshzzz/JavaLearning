/* 集合不安全的问题
ArrayList
* */

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ContainerNotSafeDemo {
    public static void main(String[] args) {
//        List<String> list = Arrays.asList("a", "b", "c");
//        list.forEach(System.out::println);
        Set<String> list = new HashSet<>();
        for (int i = 1; i <= 300; i++) {
            new Thread(() -> {
                list.add(UUID.randomUUID().toString().substring(0, 8));
                System.out.println(list);
            }, String.valueOf(i)).start();
        }

            /*1、故障现象：
            java.util.ConcurrentModificationException
                    2、导致原因
              并发争抢修改；
                    3、解决方案
                ① 使用vector,加了锁，并发性下降： new vector<>();
                ② 使用Collections.synchronizedList(new ArrayList<>());
                ③ 使用 new CopyOnWriteArrayList<>();
                CopyOnWrite容器即写时复制的容器，往一个容器添加元素时，先将当前容器进行copy,
                复制新的容器object[] newElements,然后新的容器里添加元素，添加完成后再将原容器的引用
                指向新的容器，这样的好处是可以对CopyOnWrite容器进行并发的读，而不需要加锁，因为
                当前容器不会添加任何元素，所以CopyOnWrite容器也是一种读写分离的思想，读和写不同的容器；
                    4、优化建议
            */
    }
}
