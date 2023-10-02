package com.ullmann.timetrack.models;

public class Anwesenheit {
    private int anwesenheitID;
    private int mitarbeiterID;
    private String checkIn;
    private String checkOut;
    public Anwesenheit(int anwesenheitID, String checkIn, String checkOut, int mitarbeiterID) {
        this.anwesenheitID = anwesenheitID;
        this.mitarbeiterID = mitarbeiterID;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }
    public int getAnwesenheitID() {
        return anwesenheitID;
    }
    public void setAnwesenheitID(int anwesenheitID) {
        this.anwesenheitID = anwesenheitID;
    }
    public int getMitarbeiterID() {
        return mitarbeiterID;
    }
    public void setMitarbeiterID(int mitarbeiterID) {
        this.mitarbeiterID = mitarbeiterID;
    }
    public String getCheckIn() {
        return checkIn;
    }
    public void setCheckIn(String checkIn) {
        this.checkIn = checkIn;
    }
    public String getCheckOut() {
        return checkOut;
    }
    public void setCheckOut(String checkOut) {
        this.checkOut = checkOut;
    }
}
