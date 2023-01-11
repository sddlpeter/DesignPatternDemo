public class DemoLeastKnowledge {
    public static void main(String[] args) {
        WeatherData wd = new WeatherData();
        System.out.println(wd.getTemperature());
    }
}

/*
class Thermometer {
    public float getTemperature() {
        return 33.1f;
    }
}

class WeatherStation {
    public Thermometer getThermometer(){
        return new Thermometer();
    }

}


*//* 不采用【最少知道原则】
   WeatherData类需要知道WeatherStation和Thermometer这两个类，
   才能获取temperature数据
 *//*
class WeatherData {
    WeatherStation weatherStation;

    public float getTemperature() {
        weatherStation = new WeatherStation();
        Thermometer thermometer = weatherStation.getThermometer();
        return thermometer.getTemperature();
    }
}*/

class Thermometer {
    public float getTemperature() {
        return 33.1f;
    }
}

class WeatherStation {
    public Thermometer getThermometer(){
        return new Thermometer();
    }

    public float getTemperature() {
        return getThermometer().getTemperature();
    }

}


/* 采用【最少知道原则】
   WeatherData类只需要知道WeatherStation这一个类，
   就能获取temperature数据
   需要直到的类变少了，需要的只是也变少了，交互也变少了，耦合度也降低了
   good！
 */
class WeatherData {
    WeatherStation weatherStation;

    public float getTemperature() {
        weatherStation = new WeatherStation();
        return weatherStation.getTemperature();
    }
}
