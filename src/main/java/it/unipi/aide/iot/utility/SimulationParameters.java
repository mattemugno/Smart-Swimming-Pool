package it.unipi.aide.iot.utility;

public class SimulationParameters {
    private static boolean manualCommand = false;

    public SimulationParameters() {
    }

    public static boolean isManualCommand() {
        return manualCommand;
    }

    public static void setManualCommand(boolean manualCommand) {
        SimulationParameters.manualCommand = manualCommand;
    }
}
