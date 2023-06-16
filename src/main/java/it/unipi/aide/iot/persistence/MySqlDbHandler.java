package it.unipi.aide.iot.persistence;

import it.unipi.aide.iot.bean.samples.ChlorineSample;
import it.unipi.aide.iot.bean.samples.PresenceSample;
import it.unipi.aide.iot.bean.samples.TemperatureSample;
import it.unipi.aide.iot.bean.samples.WaterLevelSample;
import it.unipi.aide.iot.config.ConfigurationParameters;

import java.sql.*;
import java.text.SimpleDateFormat;

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

    public void insertTemperatureSample(TemperatureSample temperatureSample) {
        try (
                Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement("INSERT INTO temperature (timestamp, node, degrees) VALUES (?, ?, ?)")
        )
        {
            statement.setTimestamp(1, temperatureSample.getTimestamp());
            statement.setInt(2, temperatureSample.getNodeId());
            statement.setFloat(3, temperatureSample.getTemperature());
            statement.executeUpdate();
        }
        catch (final SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void insertPresenceSample(PresenceSample presenceSample) {
        try (
                Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement("INSERT INTO temperature (node, presence) VALUES (?, ?)")
        )
        {
            statement.setInt(1, presenceSample.getNodeId());
            statement.setBoolean(2, presenceSample.isPresence());
            statement.executeUpdate();
        }
        catch (final SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void insertWaterLevelSample(WaterLevelSample waterLevelSample) {
        try (
                Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement("INSERT INTO temperature (timestamp, node, height) VALUES (?, ?, ?)")
        )
        {
            statement.setTimestamp(1, waterLevelSample.getTimestamp());
            statement.setInt(2, waterLevelSample.getNodeId());
            statement.setFloat(3, waterLevelSample.getHeight());
            statement.executeUpdate();
        }
        catch (final SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void insertChlorineSample(ChlorineSample chlorineSample) {
        try (
                Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement("INSERT INTO temperature (timestamp, node, chlorine_level) VALUES (?, ?, ?)")
        )
        {
            statement.setTimestamp(1, chlorineSample.getTimestamp());
            statement.setInt(2, chlorineSample.getNodeId());
            statement.setFloat(3, chlorineSample.getChlorineLevel());
            statement.executeUpdate();
        }
        catch (final SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void insertNewDevice(String ip, String device) {
        try (
                Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement("INSERT INTO actuators (ip, name, installation_date, status) VALUES (?, ?, ?, ?)")
        )
        {
            statement.setString(1, ip);
            statement.setString(2, device);
            Date sqlDate = new java.sql.Date(System.currentTimeMillis());
            statement.setDate(3, sqlDate);
            statement.setString(4, "active");
            statement.executeUpdate();
        }
        catch (final SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void removeDevice(String ip, String device) {
        try (
                Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement("DELETE FROM actuators WHERE ip = ? and name = ?")
        )
        {
            statement.setString(1, ip);
            statement.setString(2, device);
            statement.executeUpdate();
        }
        catch (final SQLException e)
        {
            e.printStackTrace();
        }
    }
}
