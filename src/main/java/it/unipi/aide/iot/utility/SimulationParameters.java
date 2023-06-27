package it.unipi.aide.iot.utility;

public class SimulationParameters {
    private static boolean manualCommandLight = false;
    private static boolean manualCommandHeating = false;
    private static boolean manualCommandWaterPump = false;
    private static boolean manualCommandChlorine = false;

    public SimulationParameters() {
    }

    public static boolean isManualCommandLight() {
        return manualCommandLight;
    }

    public static void setManualCommandLight(boolean manualCommandLight) {
        SimulationParameters.manualCommandLight = manualCommandLight;
    }

    public static boolean isManualCommandHeating() {
        return manualCommandHeating;
    }

    public static void setManualCommandHeating(boolean manualCommandHeating) {
        SimulationParameters.manualCommandHeating = manualCommandHeating;
    }

    public static boolean isManualCommandWaterPump() {
        return manualCommandWaterPump;
    }

    public static void setManualCommandWaterPump(boolean manualCommandWaterPump) {
        SimulationParameters.manualCommandWaterPump = manualCommandWaterPump;
    }

    public static boolean isManualCommandChlorine() {
        return manualCommandChlorine;
    }

    public static void setManualCommandChlorine(boolean manualCommandChlorine) {
        SimulationParameters.manualCommandChlorine = manualCommandChlorine;
    }
}
