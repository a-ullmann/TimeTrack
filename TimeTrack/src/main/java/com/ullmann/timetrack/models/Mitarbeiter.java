package com.ullmann.timetrack.models;

public class Mitarbeiter {
    private int mitarbeiterID;
    private String vorname;
    private String nachname;
    private String position;
    private String username;
    private String abteilung;

    public Mitarbeiter(int mitarbeiterID, String vorname, String nachname, String position, String abteilung, String username) {
        this.mitarbeiterID = mitarbeiterID;
        this.vorname = vorname;
        this.nachname = nachname;
        this.position = position;
        this.abteilung = abteilung;
        this.username = username;
    }
    public int getMitarbeiterID() {
        return mitarbeiterID;
    }
    public void setMitarbeiterID(int mitarbeiterID) {
        this.mitarbeiterID = mitarbeiterID;
    }
    public String getVorname() {
        return vorname;
    }
    public void setVorname(String vorname) {
        this.vorname = vorname;
    }
    public String getNachname() {
        return nachname;
    }
    public void setNachname(String nachname) {
        this.nachname = nachname;
    }
    public String getPosition() {
        return position;
    }
    public void setPosition(String position) {
        this.position = position;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getAbteilung() {
        return abteilung;
    }
    public void setAbteilung(String abteilung) {
        this.abteilung = abteilung;
    }
}
