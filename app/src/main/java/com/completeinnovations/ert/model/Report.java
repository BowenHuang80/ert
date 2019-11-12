package com.completeinnovations.ert.model;

import java.util.Date;

public class Report {

    private long idreport;
    private String name;
    private double totalexpense;
    private String submitter;
    private Date datecreated;
    private Date datesubmitted;
    private Status status;

    public Status getStatus() {
        return status;
    }

    public void setStatus(String status) {

        if(Status.ACCEPTED.getValue().equalsIgnoreCase(status)) {
            this.status = Status.ACCEPTED;
            return;
        }

        if(Status.PENDING.getValue().equalsIgnoreCase(status)) {
            this.status = Status.PENDING;
            return;
        }

        if(Status.SAVED.getValue().equalsIgnoreCase(status)) {
            this.status = Status.SAVED;
            return;
        }

        if(Status.PAID.getValue().equalsIgnoreCase(status)) {
            this.status = Status.PAID;
            return;
        }

        if(Status.REJECTED.getValue().equalsIgnoreCase(status)) {
            this.status = Status.REJECTED;
            return;
        }

        if(Status.SUBMITTED.getValue().equalsIgnoreCase(status)) {
            this.status = Status.SUBMITTED;
            return;
        }

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getTotalexpense() {
        return totalexpense;
    }

    public void setTotalexpense(double totalexpense) {
        this.totalexpense = totalexpense;
    }

    public String getSubmitter() {
        return submitter;
    }

    public void setSubmitter(String submitter) {
        this.submitter = submitter;
    }

    public Date getDatecreated() {
        return datecreated;
    }

    public void setDatecreated(Date datecreated) {
        this.datecreated = datecreated;
    }

    public Date getDatesubmitted() {
        return datesubmitted;
    }

    public void setDatesubmitted(Date datesubmitted) {
        this.datesubmitted = datesubmitted;
    }

    public long getIdreport() {
        return idreport;
    }

    public void setIdreport(long idreport) {
        this.idreport = idreport;
    }
}
