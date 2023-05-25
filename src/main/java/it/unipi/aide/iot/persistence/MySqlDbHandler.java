package it.unipi.aide.iot.persistence;

import it.unipi.aide.iot.config.ConfigurationParameters;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySqlDbHandler {
    private static MySqlDbHandler instance = null;

    private static String databaseIp;
    private static int databasePort;
    private static String databaseUsername;
    private static String databasePassword;
    private static String databaseName;

    public static MySqlDbHandler getInstance() {
        if(instance == null)
            instance = new MySqlDbHandler();

        return instance;
    }

    public MySqlDbHandler() {
        ConfigurationParameters configurationParameters = ConfigurationParameters.getInstance();
        databaseIp = configurationParameters.getDatabaseIp();
        databasePort = configurationParameters.getDatabasePort();
        databaseUsername = configurationParameters.getDatabaseUsername();
        databasePassword = configurationParameters.getDatabasePassword();
        databaseName = configurationParameters.getDatabaseName();
    }

    public Connection getConnection() throws SQLException {
        System.out.println(databaseIp + " " + databaseName + " " + databasePort + " " + databasePassword + " " + databaseUsername);
        return DriverManager.getConnection("jdbc:mysql://"+ databaseIp + ":" + databasePort +
                        "/" + databaseName + "?zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=CET",
                databaseUsername, databasePassword);
    }
}
