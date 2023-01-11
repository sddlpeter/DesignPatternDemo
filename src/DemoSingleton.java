public class DemoSingleton {
    public static void main(String[] args) {
        SingleClass singleClass1 = SingleClass.getInstance();
        SingleClass singleClass2 = SingleClass.getInstance();
        if (singleClass2.equals(singleClass1)) {
            System.out.println(singleClass1.toString());
        } else {
            System.out.println("They are different instance.");
        }
    }
}

// 线程不安全的单例模式

/* 懒汉式 - 静态变量 */
//class SingleClass {
//    private static SingleClass _instance;
//    private SingleClass() {}
//    public static SingleClass getInstance() {
//        if (_instance == null) {
//            _instance = new SingleClass();
//        }
//        return _instance;
//    }
//    public String toString() {
//        return "This is a Single Instance Class.";
//    }
//}


/* 懒汉式 - 同步代码块 */
class SingleClass {
    private static SingleClass _instance;
    private SingleClass() {}
    public static SingleClass getInstance() {
        if (_instance == null) {
            // 加悲观锁
            synchronized (SingleClass.class) {
                _instance = new SingleClass();
            }
        }
        return _instance;
    }
    public String toString() {
        return "This is a Single Instance Class.";
    }
}
