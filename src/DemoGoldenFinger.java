import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DemoGoldenFinger {
    public static void main(String[] args) throws AWTException {
        GoldenFinger gf = new GoldenFinger();
        // 雅典娜 精神力球 下后 + A
        Command Athena_SpiritBall = gf.deserializeMoveStr("DLDLaa","pprrpr");
        gf.setCommands('z', Athena_SpiritBall);

        // 雅典娜 闪光水晶波 (前下后)x2 + A

    }
}

// GoldenFinger is invoker
class GoldenFinger {
    private final Map<Character, Command> commands;
    private final Robot robot;
    public GoldenFinger() throws AWTException {
        this.commands = new HashMap<>();
        this.robot = new Robot();
    }

    public Robot getRobot() {
        return this.robot;
    }

    public void setCommands(char key, Command cmd) {
        this.commands.put(key, cmd);
    }

    public void invoke(char key) {
        if (this.commands.containsKey(key)) {
            this.commands.get(key).execute();
        }
    }


    //对 keys[i]要执行actions[i]
    public Command deserializeMoveStr(String keys, String actions) {
        if (keys.length() != actions.length()) {
            return null;
        }
        MacroCommand ret = new MacroCommand();
        for (int i = 0; i < keys.length(); i++) {
            char key = keys.charAt(i);
            char action = actions.charAt(i);
            switch (action) {
                case 'p': // press
                    ret.addCommandToMacro(new KeyPressCommand(this.robot, GoldenFinger.keyMap(key)));
                    break;
                case 'r':
                    ret.addCommandToMacro(new KeyReleaseCommand(this.robot, GoldenFinger.keyMap(key)));
                    break;
            }
            ret.addCommandToMacro(new DelayCommand(this.robot, 50));
        }
        return ret;
    }

    // char -> keycode
    public static int keyMap(char ch) {
        switch (ch) {
            case 'U': // up
                return KeyEvent.VK_NUMPAD8;
            case 'D': // down
                return KeyEvent.VK_NUMPAD2;
            case 'L': // left
                return KeyEvent.VK_NUMPAD4;
            case 'R': // right
                return KeyEvent.VK_NUMPAD6;
            case 'a': // 轻拳
                return KeyEvent.VK_A;
            case 'b': // 轻脚
                return KeyEvent.VK_B;
            case 'c': // 重拳
                return KeyEvent.VK_C;
            case 'd': // 重脚
                return KeyEvent.VK_D;
        }
        return -1;
    }
}

interface Command {
    void execute();
}

class MacroCommand implements Command {
    private final List<Command> commands;
    public MacroCommand() {
        this.commands = new ArrayList<>();
    }
    public void addCommandToMacro(Command cmd) {
        this.commands.add(cmd);
    }

    @Override
    public void execute() {
        for (Command cmd : this.commands) {
            cmd.execute();
        }
    }
}


abstract class RobotCommand implements Command {
    protected final Robot robot;
    public RobotCommand(Robot robot) {
        this.robot = robot;
    }
}

abstract class KeyCommand extends RobotCommand {
    protected final int keyCode;
    public KeyCommand(Robot robot, int keyCode) {
        super(robot);
        this.keyCode = keyCode;
    }
}

class KeyPressCommand extends KeyCommand {
    public KeyPressCommand(Robot robot, int keyCode) {
        super(robot, keyCode);
    }

    public void execute() {
        super.robot.keyPress(super.keyCode);
    }
}


class KeyReleaseCommand extends KeyCommand {
    public KeyReleaseCommand(Robot robot, int keyCode) {
        super(robot, keyCode);
    }

    public void execute() {
        super.robot.keyRelease(super.keyCode);
    }
}


class DelayCommand extends RobotCommand {
    private final int delayTimeSpanMS;
    public DelayCommand(Robot robot, int delayTimeSpanMS) {
        super(robot);
        this.delayTimeSpanMS = delayTimeSpanMS;
    }

    @Override
    public void execute() {
        super.robot.delay(this.delayTimeSpanMS);
    }
}
