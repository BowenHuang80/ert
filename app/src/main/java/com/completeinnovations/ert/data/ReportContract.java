package com.completeinnovations.ert.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.Objects;

public class ReportContract {

    public static final String CONTENT_AUTHORITY = "com.completeinnovations" +
            ".ert";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" +
            CONTENT_AUTHORITY);
    public static final String PATH_REPORT = "report";
    public static final String PATH_EXPENSE = "expense";
    public static final String PATH_EMPLOYEE = "employee";

    /**
     * URI query parameter to denote if a content provider
     * change originates from the sync adapter.
     */
    public static final String NETWORK_SYNC = "networkSync";

    public static final class ReportEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_REPORT).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" +
                CONTENT_AUTHORITY + "/" + PATH_REPORT;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor" +
                ".item" + CONTENT_AUTHORITY + "/" + PATH_REPORT;

        public static final String TABLE_NAME = "report";
        // ID Stored As Integer
        public static final String COLUMN_REPORT_ID = "reportID";
        // Report Name Stored as String
        public static final String COLUMN_NAME = "name";
        // Submitter Email Stored as String
        public static final String COLUMN_SUBMITTER_EMAIL = "submitterEmail";
        // Approver Email Stored as String
        public static final String COLUMN_APPROVER_EMAIL = "approverEmail";
        // Comment Stored as String
        public static final String COLUMN_COMMENT = "comment";
        // Status Stored as Strinng
        public static final String COLUMN_STATUS = "status";
        // Status Note Stored as String
        public static final String COLUMN_STATUS_NOTE = "statusNote";
        // Text Stored as String
        public static final String COLUMN_TEXT = "text";
        // CreatedOn Stored as DateTime
        public static final String COLUMN_CREATED_ON = "createdOn";
        // SubmittedOn is stored as DateTime
        public static final String COLUMN_SUBMITTED_ON = "submittedOn";
        // ApprovedOn Stored as DateTime
        public static final String COLUMN_APPROVED_ON = "approvedOn";
        // IsDeleted Stored as Bool
        public static final String COLUMN_IS_DELETED = "isDeleted";
        // Flag is stored as Bool
        public static final String COLUMN_FLAG = "flag";

        /**
         * This is used by the sync adapter to check if the
         * report needs to synced or not.
         */
        public static final String COLUMN_SYNC_STATUS = "syncStatus";

        public static Uri buildReportUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        /**
         * Builds a Uri along with network sync query parameter
         * to denote that the Uri is being called from the
         * sync adapter.
         * @param id The id
         * @return a Uri
         */
        public static Uri buildReportUriNoNetworkSync(long id) {
            return buildReportUri(id).buildUpon().appendQueryParameter(
                    NETWORK_SYNC, "false").build();
        }

        /**
         * Builds a Uri for a list of expense from a report reportBaseId.
         * @param reportBaseId The basedId or a report
         * @return Uri content://com.completeinnovations.ert/report/1/expense
         */
        public static Uri buildReportExpense(long reportBaseId) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(reportBaseId))
                    .appendPath(PATH_EXPENSE).build();
        }
    }

    public static final class ExpenseEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_EXPENSE).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" +
                CONTENT_AUTHORITY + "/" + PATH_EXPENSE;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor" +
                ".item" + CONTENT_AUTHORITY + "/" + PATH_EXPENSE;

        public static final String TABLE_NAME = "expense";
        // Expense ID is stored as Integer
        public static final String COLUMN_EXPENSE_ID = "expenseID";
        // Report ID is stored as Integer
        public static final String COLUMN_REPORT_ID = "reportID";
        // ExpenseDate is Stored as DateTime
        public static final String COLUMN_EXPENSE_DATE = "expenseDate";
        //Description is stored as String
        public static final String COLUMN_DESCRIPTION = "description";
        // Category is stored as String
        public static final String COLUMN_CATEGORY = "category";
        // Cost is stored as Float
        public static final String COLUMN_COST = "cost";
        // Taxes are stored as Float
        public static final String COLUMN_TAXES = "taxes";
        // Tax Type is stored as string
        public static final String COLUMN_TAX_TYPE = "taxType";
        // HST is stored as float
        public static final String COLUMN_HST = "hst";
        // GST is stored as float
        public static final String COLUMN_GST = "gst";
        // QST is stored as float
        public static final String COLUMN_QST = "qst";
        // Currency is stored as string
        public static final String COLUMN_CURRENCY = "currency";
        // Region is stored as string
        public static final String COLUMN_REGION = "region";
        // ReceiptID is stored as string
        public static final String COLUMN_RECEIPT_ID = "receiptID";
        // CreatedOn is stored as DateTime
        public static final String COLUMN_CREATED_ON = "createdOn";
        // IsDeleted is stored as Bool
        public static final String COLUMN_IS_DELETED = "isDeleted";

        /**
         * This is used by the sync adapter to check if the
         * expense needs to synced or not.
         */
        public static final String COLUMN_SYNC_STATUS = "syncStatus";

        public static Uri buildExpenseUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildExpenseUriNoNetworkSync(Long expenseBaseId) {
            return buildExpenseUri(expenseBaseId).buildUpon().
                    appendQueryParameter(NETWORK_SYNC, "false").build();
        }
    }

    public static final class EmployeeEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_EMPLOYEE).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" +
                CONTENT_AUTHORITY + "/" + PATH_EXPENSE;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor" +
                ".item" + CONTENT_AUTHORITY + "/" + PATH_EXPENSE;

        public static final String TABLE_NAME = "employee";

        public static final String COLUMN_KEY = "key";

        public static final String COLUMN_VALUE = "value";

        public static Uri buildEmployeeUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }
}
