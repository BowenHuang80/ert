package com.completeinnovations.ert.model;

import java.util.Arrays;

/**
 * Configures the access mode of each status.
 *
 */
public enum AccessMode {

    EDIT(Status.SAVED),
    READ(
            Status.ACCEPTED,
            Status.PAID,
            Status.PENDING,
            Status.REJECTED
    ),
    MANAGER(Status.SUBMITTED),
    ;

    private final Status[] status;

    AccessMode(Status... status) {
        this.status = status;
    }

    public Status[] getStatuses() {
        return this.status;
    }

    public static AccessMode getAccessMode(Status status) {

        AccessMode accessMode = null;

        if(status.equals(Status.SAVED)) {
            accessMode = EDIT;
        } else if(Arrays.asList(READ.getStatuses()).contains(status)) {
            accessMode = READ;
        } else if(status.equals(Status.SUBMITTED)) {
            accessMode = MANAGER;
        }

        return accessMode;
    }
}
