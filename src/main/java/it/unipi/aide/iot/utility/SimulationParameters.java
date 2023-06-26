package it.unipi.aide.iot.utility;

public class SimulationParameters {
    private static boolean manualCommandLight = false;

    public SimulationParameters() {
    }

    public static boolean isManualCommandLight() {
        return manualCommandLight;
    }

    public static void setManualCommandLight(boolean manualCommandLight) {
        SimulationParameters.manualCommandLight = manualCommandLight;
    }
}
