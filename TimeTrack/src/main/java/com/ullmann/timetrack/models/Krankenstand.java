package com.ullmann.timetrack.models;

public class Krankenstand {
    private int krankenstandID;
    private int mitarbeiterID;
    private String anfangsDatum;
    private String endeDatum;
    private String grund;

    public Krankenstand(int krankenstandID, int mitarbeiterID, String anfangsDatum, String endeDatum, String grund) {
        this.krankenstandID = krankenstandID;
        this.mitarbeiterID = mitarbeiterID;
        this.anfangsDatum = anfangsDatum;
        this.endeDatum = endeDatum;
        this.grund = grund;
    }


    public int getKrankenstandID() {
        return krankenstandID;
    }
    public void setKrankenstandID(int krankenstandID) {
        this.krankenstandID = krankenstandID;
    }
    public int getMitarbeiterID() {
        return mitarbeiterID;
    }
    public void setMitarbeiterID(int mitarbeiterID) {
        this.mitarbeiterID = mitarbeiterID;
    }
    public String getGrund() {
        return grund;
    }
    public void setGrund(String grund) {
        this.grund = grund;
    }
    public String getAnfangsDatum() {
        return anfangsDatum;
    }
    public void setAnfangsDatum(String anfangsDatum) {
        this.anfangsDatum = anfangsDatum;
    }
    public String getEndeDatum() {
        return endeDatum;
    }
    public void setEndeDatum(String endeDatum) {
        this.endeDatum = endeDatum;
    }
}
