package miio.emulator;

import com.google.gson.JsonNull;

public enum FakeResponses {

    P1("power", "on"),
    P2("mode", 1),
    P3("humidity", 22),
    P4("aqi", 22),
    P5("led", "on"),
    P6("buzzer", "off"),
    P7("f1_hour", 33),
    P8("temp_dec", 21),
    UNKNOWN("", JsonNull.INSTANCE);

    private final String command;
    private final Object response;

    private FakeResponses(String command, Object response) {
        this.command = command;
        this.response = response;
    }

    public String getCommand() {
        return command;
    }

    public static FakeResponses getCommand(String commandString) {
        for (FakeResponses mioCmd : FakeResponses.values()) {
            if (mioCmd.getCommand().equals(commandString)) {
                return mioCmd;
            }
        }
        return UNKNOWN;
    }

    public Object getResponse() {

        return response;
    }

}
