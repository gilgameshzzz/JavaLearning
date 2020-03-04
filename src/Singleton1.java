/*饿汉式：单例模式
 *  直接创建实例对象，不管是否需要这个对象都会创建
 * 1、构造器私有化
 * 2、自行创建、并且用静态变量保存
 * 向外提供这个实例
 * 强调这是一个单例，可以用final修饰
 * */

class Singleton1 {
    public static final Singleton1 INSTANCE = new Singleton1();

    private Singleton1() {
    }
}

// 枚举类型
enum Singleton2 {
    INSTANCE
}

//懒汉式
class Singleton3 {
    private static volatile Singleton3 instance;

    private Singleton3() {
    }

    public static Singleton3 getInstance() {
        if (instance == null) {
            synchronized (Singleton3.class) {
                if (instance == null) {
                    instance = new Singleton3();
                }
            }
        }
        return instance;
    }
}