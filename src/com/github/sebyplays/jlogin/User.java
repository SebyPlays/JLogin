package com.github.sebyplays.jlogin;

import com.github.sebyplays.jlogin.session.Session;
import com.github.sebyplays.jlogin.session.SessionThread;

import java.util.UUID;

public class User implements Session {
    public Thread sessionThread = new Thread(new SessionThread());
    private User user;
    private String username;
    private String sessionToken;
    private UUID uuid;
    private int hierarchy;

    public User user() {
        return this;
    }

    @Override
    public boolean sessionValidator(String username, String sessionToken, int hierarchy, UUID uuid) {
        this.username = username;
        this.sessionToken = sessionToken;
        this.hierarchy = hierarchy;
        this.uuid = uuid;
        if (sessionToken != null) {
            if (sessionThread.isAlive()) {
                sessionThread.resume();
                return false;
            }
            sessionThread.start();
            return false;
        }
        return false;
    }

    @Override
    public User getUser() {
        return this.user;
    }

    @Override
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public Session getSession() {
        return this;
    }

    @Override
    public UUID getUUID() {
        return this.uuid;
    }

    @Override
    public String getToken() {
        return this.sessionToken;
    }

    @Override
    public String getName() {
        return this.username;
    }

    @Override
    public int getHierarchy() {
        return this.hierarchy;
    }
}
