package com.completeinnovations.ert.data;

/**
 * Sync Status for the report table.
 * Denotes if a report needs synchronisation or if it is currently
 * being synchronized by the Sync Adapter
 * @author Abhinav Seewoosungkur
 */
public enum SyncStatus {
    /**
     * Currently being synced by the Sync Adapter
     */
    SYNC_IN_PROGRESS,

    /**
     * Report has been saved by the user. It requires sync.
     */
    REQUIRES_SYNC,

    /**
     * Report has been synced and does not need any further syncing
     */
    SYNCED,

    /**
     * Report has been saved. Needs sync.
     */
    SAVED_REPORT,

    /**
     * Report has been edited. Needs sync.
     */
    EDITED_REPORT,

    /**
     * Report has been deleted. Needs sync.
     */
    DELETED_REPORT,

    /**
     * The report status has been changed to pending. Needs sync.
     */
    REPORT_PENDING,

    ;
}
