import java.io.Serializable;

public class ControlCommand implements Serializable {
    private static final long serialVersionUID = 1L;

    private String commandType;
    private double value;

    public ControlCommand(String commandType, double value) {
        this.commandType = commandType;
        this.value = value;
    }

    public String getCommandType() {
        return commandType;
    }

    public double getValue() {
        return value;
    }
}