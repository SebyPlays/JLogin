package com.github.sebyplays.jlogin.session;

import com.github.sebyplays.jlogin.User;

import java.util.UUID;

public interface Session {

    boolean sessionValidator(String username, String sessionToken, int hierarchy, UUID uuid);

    User getUser();

    void setUser(User user);

    Session getSession();

    UUID getUUID();

    String getToken();

    String getName();

    int getHierarchy();
}
