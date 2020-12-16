package com.github.sebyplays.jlogin.sql;

import com.github.sebyplays.jlogin.LoginAffections;
import com.github.sebyplays.jlogin.UUIDGenerator;
import com.github.sebyplays.jlogin.api.JLogin;
import com.github.sebyplays.jlogin.session.SessionThread;
import com.github.sebyplays.jlogin.session.SessionToken;
import de.github.sebyplays.hashhandler.api.HashHandler;
import de.github.sebyplays.hashhandler.api.HashType;
import de.github.sebyplays.logmanager.api.LogManager;
import de.github.sebyplays.logmanager.api.LogType;

import java.io.IOException;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class MySQL {

    public String host = "";
    public int port = 3306;
    public String database = "";
    public String username = "";
    public String password = "";

    public Connection connection;

    public MySQL(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public void connect() throws SQLException {
        if (!isConnected() || connection.isClosed()) {
            this.connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database + "?autoReconnect=true", this.username, this.password);
            createNecessaryTables();
        }
    }

    public void disconnect() throws Exception {
        if (this.isConnected()) {
            this.connection.close();
        }
    }

    public boolean isConnected() {
        return connection != null;
    }

    public boolean isRegistered(String username) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT `username` FROM `jlogin_users` WHERE `username`='" + username + "'");
        return resultSet.next();
    }

    public void createNecessaryTables() throws SQLException {
        Statement statement = this.connection.createStatement();
        execU("CREATE TABLE IF NOT EXISTS jlogin_users(id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(255) NOT NULL, username VARCHAR(255), password VARCHAR(255), ip VARCHAR(255), hierarchy INT, sessionToken VARCHAR(255), loginCount INT, failedLoginCount INT,PRIMARY KEY (id))");
        execU("CREATE TABLE IF NOT EXISTS jlogin_uuids(username VARCHAR(255) NOT NULL, uuid VARCHAR(255), registered VARCHAR(255),PRIMARY KEY (username))");

    }

    public Object execQ(String query, String columnLabel) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);
        resultSet.next();
        return resultSet.getObject("");
    }

    public void execU(String query) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(query);
        return;
    }

    public void register(String username, String password, int hierarchy, HashType hashType, boolean hash, boolean log) throws IOException, NoSuchAlgorithmException, SQLException {
        if (isRegistered(username)) {
            LogManager.getLogManager("JAuth").log(LogType.ERROR, "That user is already registered!", true, true);
            return;
        }
        String passwd = password;
        String uniqueUserIdentifier = createUUID();
        if (hash) {
            passwd = new HashHandler(hashType).hash(password, log);
        }
        Statement statement = this.connection.createStatement();
        statement.executeUpdate("INSERT INTO `jlogin_uuids` (`username`, `uuid`, `registered`) VALUES ('" + username.toLowerCase() + "', '" + uniqueUserIdentifier + "', '" + new SimpleDateFormat("MM/dd/yyyy-HH:mm:ss").format(new Date()) + "')");
        statement.executeUpdate("INSERT INTO `jlogin_users` (uuid, username, password, hierarchy, failedLoginCount) VALUES ('" + uniqueUserIdentifier + "', '" + username.toLowerCase() + "', '" + passwd + "', '" + hierarchy + "', '0')");
        LogManager.getLogManager("JAuth").log(LogType.INFORMATION, "User: " + username + "(" + uniqueUserIdentifier + ")" + " with hierarchylevel " + hierarchy + " created successfully", true, true);
    }

    public boolean authenticate(String username, String password, HashType hashType, boolean hash, boolean log) throws IOException, NoSuchAlgorithmException, SQLException {
        if (username.equals(null) || password.equals(null) || isRegistered(username) == false) {
            LogManager.getLogManager("JAuth").log(LogType.ERROR, "Invalid entries given or user is not registered!", true, true);
            return false;
        }

        if (JLogin.limitFalseLogins && getFailedLogonCount(getUUID(username)) > JLogin.falseLoginLimitation) {
            if (JLogin.loginAffections == LoginAffections.DISABLE_ACCOUNT) {
                LogManager.getLogManager("JAuth").log(LogType.ERROR, "Could not login to your account, please contact the system administrator. ERR-" + JLogin.loginAffections.name(), true, true);
                return false;
            }
            if (JLogin.loginAffections == LoginAffections.DELETE_ACCOUNT) {
                execU("DELETE FROM `jlogin_users` WHERE `uuid`='" + getUUID(username) + "'");
                execU("DELETE FROM `jlogin_uuids` WHERE `uuid`='" + getUUID(username) + "'");
                LogManager.getLogManager("JAuth").log(LogType.ERROR, "Your account has been deleted due to too many failed logins. ERR-" + JLogin.loginAffections.name(), true, true);
                return false;
            }
        }
        String hashedPassword = "";
        if (hash) hashedPassword = new HashHandler(hashType).hash(password, log);
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT `uuid` FROM `jlogin_uuids` WHERE `username`='" + username.toLowerCase() + "'");
        resultSet.next();
        ResultSet resultSet1 = statement.executeQuery("SELECT `password` FROM `jlogin_users` WHERE `uuid`='" + resultSet.getString("uuid") + "'");
        resultSet1.next();
        if (hashedPassword.equals(resultSet1.getString("password")) || password.equals(resultSet1.getString("password"))) {
            execU("UPDATE `jlogin_users` SET `loginCount`='" + (getLogonCount(getUUID(username)) + 1) + "' WHERE `uuid`='" + getUUID(username) + "'");
            setSessionID(getUUID(username));
            SessionThread.interruptionExpectancy = false;
            JLogin.user.sessionValidator(username, getSessionID(getUUID(username)), getHierarchy(getUUID(username)), getUUID(username));
            execU("UPDATE `jlogin_users` SET `failedLoginCount`='0' WHERE `uuid`='" + getUUID(username) + "'");
            return true;
        }
        if (JLogin.limitFalseLogins) {
            execU("UPDATE `jlogin_users` SET `failedLoginCount`='" + (getFailedLogonCount(getUUID(username)) + 1) + "' WHERE `uuid`='" + getUUID(username) + "'");
            LogManager.getLogManager("JAuth").log(LogType.INFORMATION, "Failed logon of " + getUUID(username) + " counted! Your current failed logon count is: " + getFailedLogonCount(getUUID(username)) + ". \nif you fail " + (JLogin.falseLoginLimitation - getFailedLogonCount(getUUID(username))) + " more times, your account will be suspended! \nif you log on once successfully, your fail count will be resetted!", true, true);
        }
        return false;
    }

    public boolean authenticate(String username, String password, HashType hashType, boolean hash, boolean log, InetAddress inetAddress) throws IOException, NoSuchAlgorithmException, SQLException {
        if (JLogin.limitFalseLogins && getFailedLogonCount(getUUID(username)) > JLogin.falseLoginLimitation) {
            if (JLogin.loginAffections == LoginAffections.ALLOW_LOCAl_ONLY && inetAddress != InetAddress.getLocalHost()) {
                LogManager.getLogManager("JAuth").log(LogType.ERROR, "Could not login to your account, please contact the system administrator. ERR-" + JLogin.loginAffections.name(), true, true);
                return false;
            }
        }
        if (authenticate(username, password, hashType, hash, log)) {
            execU("UPDATE `jlogin_users` SET `ip`='" + inetAddress + "' WHERE `uuid`='" + getUUID(username) + "'");
            return true;
        }
        return false;
    }

    public void setSessionID(UUID uuid) throws SQLException, IOException {
        Statement statement = connection.createStatement();
        statement.executeUpdate("UPDATE `jlogin_users` SET `sessionToken`= '" + (new SessionToken().generate(uuid)) + "' WHERE `uuid`='" + uuid.toString() + "'");
        LogManager.getLogManager("JAuth").log(LogType.INFORMATION, "SessionToken for user " + getUsername(uuid) + " Updated: (" + getSessionID(uuid) + ")", true, true);
        return;
    }

    public String getSessionID(UUID uuid) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT `sessionToken` FROM `jlogin_users` WHERE `uuid`='" + uuid + "'");
        resultSet.next();
        return resultSet.getString("sessionToken");
    }

    public int getFailedLogonCount(UUID uuid) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT `failedLoginCount` FROM `jlogin_users` WHERE `uuid`='" + uuid.toString() + "'");
        resultSet.next();
        return resultSet.getInt("failedLoginCount");
    }

    public int getLogonCount(UUID uuid) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT `loginCount` FROM `jlogin_users` WHERE `uuid`='" + uuid.toString() + "'");
        resultSet.next();
        return resultSet.getInt("loginCount");
    }

    public int getHierarchy(UUID uuid) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT `hierarchy` FROM `jlogin_users` WHERE `uuid`='" + uuid.toString() + "'");
        resultSet.next();
        return resultSet.getInt("hierarchy");
    }

    public UUID getUUID(String username) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT `uuid` FROM `jlogin_uuids` WHERE `username`='" + username.toLowerCase() + "'");
        resultSet.next();
        return UUID.fromString(resultSet.getString("uuid"));
    }

    public String getUsername(UUID uuid) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT `username` FROM `jlogin_uuids` WHERE `uuid`='" + uuid + "'");
        resultSet.next();
        return resultSet.getString("username");
    }


    public String createUUID() throws SQLException {
        String uuid = UUIDGenerator.generateUUID().toString();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT `uuid` FROM `jlogin_uuids` ORDER BY `uuid`");
        for (int i = 0; i < resultSet.getRow(); i++) {
            resultSet.next();
            if (uuid.equals(resultSet.getString("uuid"))) {
                createUUID();
            }
        }
        return uuid;
    }


}
