public class DemoSimpleFactory {
    public static void main(String[] args) {
        Toy dog = ToyFactory.getToy("dog");
        dog.talk();
        Toy cat = ToyFactory.getToy("cat");
        cat.talk();
    }
}

interface Toy {
    void talk();
}

class Cat implements Toy {
    public void talk() {
        System.out.println("mew");
    }
}

class Dog implements Toy {
    public void talk() {
        System.out.println("woof");
    }
}

class ToyFactory {
    public static Toy getToy(String type) {
        if (type.equals("dog")) {
            return new Dog();
        } else if (type.equals("cat")) {
            return new Cat();
        }
        return null;
    }
}

