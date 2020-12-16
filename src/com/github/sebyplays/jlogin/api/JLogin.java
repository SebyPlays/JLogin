package com.github.sebyplays.jlogin.api;

import com.github.sebyplays.jlogin.LoginAffections;
import com.github.sebyplays.jlogin.User;
import com.github.sebyplays.jlogin.session.SessionThread;
import com.github.sebyplays.jlogin.sql.MySQL;
import de.github.sebyplays.hashhandler.api.HashType;
import de.github.sebyplays.logmanager.api.LogManager;
import de.github.sebyplays.logmanager.api.LogType;

import java.io.IOException;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

public class JLogin {
    public static User user = new User();
    public static String host = "";
    public static int port = 3306;
    public static String database = "";
    public static String username = "";
    public static String password = "";

    public static HashType hashType = HashType.SHA_1;
    public static boolean logHash = true;
    public static boolean hash = false;
    public static boolean limitFalseLogins = false;
    public static int falseLoginLimitation = 1000;
    public static LoginAffections loginAffections = LoginAffections.DISABLE_ACCOUNT;

    public static MySQL mySQL = new MySQL(host, port, database, username, password);

    public JLogin(String host, int port, String database, String username, String password) throws SQLException {
        JLogin.host = host;
        JLogin.port = port;
        JLogin.database = database;
        JLogin.username = username;
        JLogin.password = password;
        mySQL = new MySQL(host, port, database, username, password);
        mySQL.connect();
    }

    public static void logOut() throws IOException, SQLException, NoSuchAlgorithmException {
        SessionThread.interruptionExpectancy = true;
        LogManager.getLogManager("JAuth").log(LogType.NORMAL, "logged out! " + user.getName(), true, true);
        user.sessionThread.stop();
        user.sessionValidator(null, null, 00, null);
    }

    public boolean login(String username, String password) throws NoSuchAlgorithmException, SQLException, IOException {
        return mySQL.authenticate(username, password, hashType, hash, logHash);
    }

    public boolean login(String username, String password, InetAddress inetAddress) throws NoSuchAlgorithmException, SQLException, IOException {
        return mySQL.authenticate(username, password, hashType, hash, logHash, inetAddress);
    }

    public void register(String username, String password, int hierarchy) throws NoSuchAlgorithmException, SQLException, IOException {
        mySQL.register(username, password, hierarchy, hashType, hash, logHash);
    }

    public void options(HashType hashType, boolean hash, boolean logHash, boolean limitFalseLogins, int falseLoginLimitation, LoginAffections loginAffections) {
        JLogin.hashType = hashType;
        JLogin.hash = hash;
        JLogin.logHash = logHash;
        JLogin.limitFalseLogins = limitFalseLogins;
        JLogin.falseLoginLimitation = falseLoginLimitation;
        JLogin.loginAffections = loginAffections;
    }

}
