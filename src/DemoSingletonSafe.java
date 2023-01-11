public class DemoSingletonSafe {
    public static void main(String[] args) {
        System.out.println(Singleton._instance.hashCode());
        System.out.println(Singleton._instance.hashCode());
        System.out.println(Singleton._instance.hashCode());
    }

}


// 线程安全的单例模式

/* 饿汉式 - 静态常量 */
//class SingleClassSafe {
//    // 常量需要马上实例化(初始化)
//    private static final SingleClassSafe _instance = new SingleClassSafe();
//    private SingleClassSafe() {}
//    public static SingleClassSafe getInstance() {
//        return _instance;
//    }
//}


/* 饿汉式 - 静态代码块 */
//class SingleClassSafe {
//    private static SingleClassSafe _instance;
//    static {
//        _instance = new SingleClassSafe();
//    }
//    private SingleClassSafe() {}
//    public static SingleClassSafe getInstance() {
//        return _instance;
//    }
//}



/* 懒汉式 - 同步方法 */
//class SingleClassSafe {
//    private static SingleClassSafe _instance;
//    private SingleClassSafe() {}
//    public static synchronized SingleClassSafe getInstance() {
//        if (_instance == null) {
//            _instance = new SingleClassSafe();
//        }
//        return  _instance;
//    }
//}


/* 懒汉式 - 双重检查锁 适合用在多线程操作 - 缺点：代码量大，性能差一点 */
//class SingleClassSafe {
//    private static volatile SingleClassSafe _instance;
//    private SingleClassSafe() {}
//    public static SingleClassSafe getInstance() {
//        // 防止有两个线程同时检查，还没有instance这个实例，那么就会进入到if
//        if (_instance == null) {
//            // 用类的属性作为locker
//            synchronized (SingleClassSafe.class) {
//                // 再次检查
//                if (_instance == null) {
//                    _instance = new SingleClassSafe();
//                }
//            }
//        }
//        return  _instance;
//    }
//}




/* 静态内部类 */
class SingleClassSafe {
    private SingleClassSafe() {}
    private static class InnerSingleton {
        // 内部类可以调用外部类的构造方法
        private static final SingleClassSafe _instance = new SingleClassSafe();
    }
    public static SingleClassSafe getInstance() {
        return InnerSingleton._instance;
    }
}

/* 枚举式 */
// 枚举的本质就是一个单例，不管哪个类去调用他，这些类拿到的都是同一个实例
enum Singleton {
    _instance;
    Singleton() {}
    public void print() {
        System.out.println("do something");
    }
}
