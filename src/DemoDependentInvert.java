import java.util.HashMap;
import java.util.Map;

public class DemoDependentInvert {
    public static void main(String[] args) {
        IShape s1 = new Rectangle(5, 10);
        IShape s2 = new Triangle(5, 6);
        // 依赖注入：构造方法
        AreaCalculatorDependentInvert acdi = new AreaCalculatorDependentInvert(s1);
        System.out.println(acdi.calArea());

        // 依赖注入：set方法
        acdi.setShape(s2);
        System.out.println(acdi.calArea());


        // 依赖查找：用id作为key查找
        AreaCalculatorDependentLookUp acdl = new AreaCalculatorDependentLookUp();
        acdl.addShape(s1);
        acdl.addShape(s2);
        System.out.println(acdl.area(0));
        System.out.println(acdl.area(1));
    }
}

// AreaCalculatorDependentInvert 是一个低层组件，IShape是一个高层组件
class AreaCalculatorDependentInvert {
    private IShape shape;

    // 依赖注入：构造方法注入
    public AreaCalculatorDependentInvert(IShape shape) {
        this.shape = shape;
    }

    // 依赖注入：setter注入
    public void setShape(IShape shape) {
        this.shape = shape;
    }
    public double calArea() {
        return this.shape.area();
    }
}

class AreaCalculatorDependentLookUp {
    private int shapeId;
    private Map<Integer, IShape> lookup;
    public AreaCalculatorDependentLookUp() {
        this.shapeId = 0;
        this.lookup = new HashMap<>();
    }

    public void addShape(IShape shape) {
        lookup.put(shapeId++, shape);
    }

    public double area(int key) {
        return lookup.get(key).area();
    }
}


interface IShape {
    public double area();
}

class Rectangle implements IShape {
    private int length;
    private int height;
    public Rectangle(int length, int height) {
        this.length = length;
        this.height = height;
    }

    @Override
    public double area() {
        return this.length * this.height;
    }
}

class Triangle implements IShape {
    private int length;
    private int height;
    public Triangle(int length, int height) {
        this.length = length;
        this.height = height;
    }

    @Override
    public double area () {
        return this.length * this.height / 2.0;
    }
}
