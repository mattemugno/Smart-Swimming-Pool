package it.unipi.aide.iot.utility;

public class SimulationParameters {
    private static boolean manualCommandLight = false;
    private static boolean manualCommandHeater = false;
    private static boolean manualCommandWaterPump = false;
    private static boolean manualCommandChlorineDisp = false;

    public SimulationParameters() {
    }

    public static boolean isManualCommandLight() {
        return manualCommandLight;
    }

    public static void setManualCommandLight(boolean manualCommandLight) {
        SimulationParameters.manualCommandLight = manualCommandLight;
    }

    public static boolean isManualCommandHeater() {
        return manualCommandHeater;
    }

    public static void setManualCommandHeater(boolean manualCommandHeater) {
        SimulationParameters.manualCommandHeater = manualCommandHeater;
    }

    public static boolean isManualCommandWaterPump() {
        return manualCommandWaterPump;
    }

    public static void setManualCommandWaterPump(boolean manualCommandWaterPump) {
        SimulationParameters.manualCommandWaterPump = manualCommandWaterPump;
    }

    public static boolean isManualCommandChlorineDisp() {
        return manualCommandChlorineDisp;
    }

    public static void setManualCommandChlorineDisp(boolean manualCommandChlorineDisp) {
        SimulationParameters.manualCommandChlorineDisp = manualCommandChlorineDisp;
    }
}
