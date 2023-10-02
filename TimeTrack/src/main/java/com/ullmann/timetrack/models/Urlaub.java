package com.ullmann.timetrack.models;

public class Urlaub {
    private int requestID;
    private int mitarbeiterID;
    private String startDate;
    private String endDate;
    private String status;
    private String requestDate;
    private boolean statusChanged = false;

    public Urlaub(int requestID, int mitarbeiterID, String startDate, String endDate, String status, String requestDate) {
        this.requestID = requestID;
        this.mitarbeiterID = mitarbeiterID;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.requestDate = requestDate;
    }
    public int getRequestID() {
        return requestID;
    }
    public void setRequestID(int urlaubID) {
        this.requestID = requestID;
    }
    public String getStartDate() {
        return startDate;
    }
    public void setStartDate(String anfangsDatum) {
        this.startDate = anfangsDatum;
    }
    public String getEndDate() {
        return endDate;
    }
    public void setEndDate(String endDatum) {
        this.endDate = endDatum;
    }
    public int getMitarbeiterID() {
        return mitarbeiterID;
    }
    public void setMitarbeiterID(int mitarbeiterID) {
        this.mitarbeiterID = mitarbeiterID;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public boolean isStatusChanged() {
        return statusChanged;
    }
    public void setStatusChanged(boolean statusChanged) {
        this.statusChanged = statusChanged;
    }
    public String getRequestDate() {
        return requestDate;
    }
    public void setRequestDate(String requestDate) {
        this.requestDate = requestDate;
    }
}
