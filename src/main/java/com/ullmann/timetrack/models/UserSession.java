package com.ullmann.timetrack.models;

public class UserSession {
    private static UserSession instance;
    private Mitarbeiter loggedInUser;
    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }
    public void setLoggedInUser(Mitarbeiter loggedInUser) {
        this.loggedInUser = loggedInUser;
    }
    public Mitarbeiter getLoggedInUser() {
        return loggedInUser;
    }
    public void logout() {
        loggedInUser = null;
        instance = null;
    }
}
