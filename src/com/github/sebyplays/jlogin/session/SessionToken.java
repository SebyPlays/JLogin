package com.github.sebyplays.jlogin.session;


import com.github.sebyplays.jlogin.api.JLogin;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class SessionToken {

    //EXAMPLE TOKEN STRUCTURE "{Uuid(lastpart)dateAndTimeOfLogOnTimesLoggedOn}"

    public SessionToken() {

    }

    public static boolean compareToken() throws SQLException {
        return JLogin.mySQL.getSessionID(JLogin.user.getUUID()).equals(JLogin.user.getSession().getToken());
    }

    public String generate(UUID uuid) throws SQLException, IOException {
        return uuid.toString().split("-")[uuid.toString().split("-").length - 1] + new SimpleDateFormat("MMddyyyyHHmmss").format(new Date()) + JLogin.mySQL.getLogonCount(uuid);
    }

}
