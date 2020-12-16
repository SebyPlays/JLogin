package com.github.sebyplays.jlogin.session;

import com.github.sebyplays.jlogin.api.JLogin;
import de.github.sebyplays.logmanager.api.LogManager;
import de.github.sebyplays.logmanager.api.LogType;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class SessionThread implements Runnable {
    public static boolean interruptionExpectancy = false;

    @Override
    public void run() {
        try {
            while (SessionToken.compareToken()) {
                LogManager.getLogManager("session-" + JLogin.user.getSession().getToken()).log(LogType.NORMAL, "[" + JLogin.user.getSession().getToken() + "]" + "Session still alive..", true, false);
                TimeUnit.SECONDS.sleep(30);
                if (interruptionExpectancy) {
                    LogManager.getLogManager("session-" + JLogin.user.getSession().getToken()).log(LogType.INFORMATION, "Suspending user/session expectedly.", true, false);
                    JLogin.logOut();
                    interruptionExpectancy = false;
                    break;
                }
            }
            if (!SessionToken.compareToken()) {
                LogManager.getLogManager("session-" + JLogin.user.getSession().getToken()).log(LogType.INFORMATION, "Suspending user/session", true, false);
                LogManager.getLogManager("session-" + JLogin.user.getSession().getToken()).log(LogType.ERROR, "Session interrupted unexpectedly..", true, true);
                JLogin.logOut();

            }
            return;
        } catch (SQLException | InterruptedException | IOException | NoSuchAlgorithmException | NullPointerException e) {
            e.printStackTrace();
        }
    }
}
