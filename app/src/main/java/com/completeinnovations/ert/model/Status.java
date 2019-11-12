package com.completeinnovations.ert.model;

/**
 * Enum for Status.
 * Central place for storing all strings of Status.
 */
public enum Status {

    /**
     * These statuses below should reflect the same string returned by the api.
     */
    ACCEPTED("Accepted", 1),
    PENDING("Pending", 2),
    SAVED("Saved", 4),
    PAID("Paid", 6),
    REJECTED("Rejected", 3),
    SUBMITTED("Submitted", 5),
    ;
    private final String status;
    private final int statusNumber;

    /**
     * Constructor for Status enums
     * @param status
     */
    Status(String status, int statusNumber) {
        this.status = status;
        this.statusNumber = statusNumber;
    }

    /**
     * Retrieves the string value of a Status
     * @return the string representation of a Status
     */
    public String getValue() {
        return status;
    }

    /**
     * Retrieves the Status number
     * @return number representation of a Status
     */
    public int getNumber() {
        return statusNumber;
    }

    /**
     * A static factory method to create a Status.
     * This method should be updated to accommodate new types of statuses.
     * @param statusString the string as received from the api
     * @return a Status
     */
    public static Status statusFactory(String statusString) {
        Status status = null;
        if(statusString.equalsIgnoreCase(ACCEPTED.getValue())) {
            status = ACCEPTED;
        } else if(statusString.equalsIgnoreCase(PENDING.getValue())) {
            status = PENDING;
        } else if(statusString.equalsIgnoreCase(SAVED.getValue())) {
            status = SAVED;
        } else if(statusString.equalsIgnoreCase(PAID.getValue())) {
            status = PAID;
        } else if(statusString.equalsIgnoreCase(REJECTED.getValue())) {
            status = REJECTED;
        } else if(statusString.equalsIgnoreCase(SUBMITTED.getValue())) {
            status = SUBMITTED;
        }

        return status;
    }

    /**
     * A static factory method to create a Status.
     * @param statusNumber The status number as received from the api
     * @return a Status
     */
    public static Status statusFactory(int statusNumber) {
        Status status = null;
        if(statusNumber == ACCEPTED.getNumber()) {
            status = ACCEPTED;
        } else if(statusNumber == PENDING.getNumber()) {
            status = PENDING;
        } else if(statusNumber == SAVED.getNumber()) {
            status = SAVED;
        } else if(statusNumber == PAID.getNumber()) {
            status = PAID;
        } else if(statusNumber == REJECTED.getNumber()) {
            status = REJECTED;
        } else if(statusNumber == SUBMITTED.getNumber()) {
            status = SUBMITTED;
        }
        return status;
    }

}
