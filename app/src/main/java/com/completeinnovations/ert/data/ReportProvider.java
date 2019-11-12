package com.completeinnovations.ert.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.completeinnovations.ert.Utility;
import com.completeinnovations.ert.data.ReportContract.EmployeeEntry;
import com.completeinnovations.ert.data.ReportContract.ExpenseEntry;
import com.completeinnovations.ert.data.ReportContract.ReportEntry;
import com.completeinnovations.ert.model.Report;
import com.completeinnovations.ert.model.Status;

public class ReportProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private ReportDbHelper mOpenHelper;

    private static final int REPORT = 100;
    private static final int REPORT_ID = 101;
    private static final int REPORT_ID_EXPENSE = 102;

    private static final int EXPENSE = 200;
    private static final int EXPENSE_ID = 201;

    private static final int EMPLOYEE = 300;



    private static UriMatcher buildUriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ReportContract.CONTENT_AUTHORITY;

        uriMatcher.addURI(authority, ReportContract.PATH_REPORT, REPORT);
        uriMatcher.addURI(authority, ReportContract.PATH_REPORT + "/*", REPORT_ID);

        uriMatcher.addURI(authority, ReportContract.PATH_EXPENSE, EXPENSE);
        uriMatcher.addURI(authority, ReportContract.PATH_EXPENSE + "/*", EXPENSE_ID);

        uriMatcher.addURI(authority, ReportContract.PATH_REPORT + "/*/" +
        ReportContract.PATH_EXPENSE, REPORT_ID_EXPENSE);

        uriMatcher.addURI(authority, ReportContract.PATH_EMPLOYEE, EMPLOYEE);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new ReportDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        Cursor cursor;
        switch (sUriMatcher.match(uri)) {

            // "report"
            case REPORT: {
                cursor = mOpenHelper.getReadableDatabase().query(
                        ReportContract.ReportEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            case REPORT_ID: {
                cursor = mOpenHelper.getReadableDatabase().query(
                        ReportContract.ReportEntry.TABLE_NAME,
                        projection,
                        ReportContract.ReportEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            // "expense"

            case EXPENSE: {
                cursor = mOpenHelper.getReadableDatabase().query(
                    ReportContract.ExpenseEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            case EXPENSE_ID: {
                cursor = mOpenHelper.getReadableDatabase().query(
                        ReportContract.ExpenseEntry.TABLE_NAME,
                        projection,
                        ReportContract.ExpenseEntry._ID + " = '" +
                                ContentUris.parseId(uri) + "'",
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            case REPORT_ID_EXPENSE: {
                cursor = mOpenHelper.getReadableDatabase().query(
                        ReportEntry.TABLE_NAME + " INNER " +
                                "JOIN " +
                                ExpenseEntry.TABLE_NAME +
                                " ON " +
                                ReportEntry.TABLE_NAME + "." +
                                ReportEntry.COLUMN_REPORT_ID +
                                " = " +
                                ExpenseEntry.TABLE_NAME + "."
                                + ExpenseEntry.COLUMN_REPORT_ID,
                        projection,
                        ReportEntry.TABLE_NAME + "." +
                                ReportEntry._ID + " = '" +
                                uri.getPathSegments().get(1) + "'" +
                                " AND " + ExpenseEntry.TABLE_NAME + "." +
                                ExpenseEntry.COLUMN_IS_DELETED + "!='true'",
                        // 1 -> get the value of * from the
                        // REPORT_ID_EXPENSE URI
                        null,
                        null,
                        null,
                        sortOrder
                );

                break;
            }

            case EMPLOYEE: {
                cursor = mOpenHelper.getReadableDatabase().query(
                        EmployeeEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null, null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;

    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        //final SQLiteDatabase sqLiteDatabase = mOpenHelper.getWritableDatabase();
        Uri returnUri;

        switch (sUriMatcher.match(uri)) {

            case REPORT: {
                long id = mOpenHelper.getWritableDatabase().insert(
                        ReportContract.ReportEntry.TABLE_NAME,
                        null,
                        values
                );

                if(id > 0) {
                    returnUri = ReportContract.ReportEntry.buildReportUri(id);

                    // manager is receiving a submitted report
                    if(values.containsKey(ReportEntry.COLUMN_STATUS)) {
                        if (values.getAsInteger(ReportEntry.COLUMN_STATUS) == Status
                                .SUBMITTED.getNumber()) {
                            Utility.sendNotification(getContext(),
                                    Integer.parseInt(String.valueOf(ContentUris.parseId(returnUri))));
                        }
                    }

                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            }

            case EXPENSE: {
                long id = mOpenHelper.getWritableDatabase().insert(
                        ReportContract.ExpenseEntry.TABLE_NAME,
                        null,
                        values
                );

                if(id > 0) {
                    returnUri = ReportContract.ExpenseEntry.buildExpenseUri(id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            }

            case EMPLOYEE: {
                long id = mOpenHelper.getWritableDatabase().insert(
                        EmployeeEntry.TABLE_NAME,
                        null,
                        values
                );

                if(id > 0) {
                    returnUri = EmployeeEntry.buildEmployeeUri(id);
                } else {
                    throw new SQLException("Failed to insert row into" + uri);
                }
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        //cursor.setNotificationUri(getContext().getContentResolver(), uri);
        getContext().getContentResolver().notifyChange(uri, null, networkSync(uri));
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case REPORT_ID : {
                rowsDeleted = db.delete(
                        ReportEntry.TABLE_NAME,
                        ReportEntry._ID + "= ?",
                        new String[] {String.valueOf(ContentUris.parseId(uri))}
                );
                break;
            }

            case EXPENSE_ID : {
                rowsDeleted = db.delete(
                        ExpenseEntry.TABLE_NAME,
                        ExpenseEntry._ID + "= ? ",
                        new String[] {String.valueOf(ContentUris.parseId(uri))}
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if(rowsDeleted != 0) {
            //getContext().getContentResolver().notifyChange(uri, null, true);
            getContext().getContentResolver().notifyChange(uri, null, networkSync(uri));
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        int rowsUpdated;

        switch (sUriMatcher.match(uri)) {
            case REPORT_ID: {
                // Notify only when previous status was pending

                Cursor cursor = getContext().getContentResolver().query(
                        ReportEntry.buildReportUri(ContentUris.parseId(uri)),
                        null, null, null, null
                );
                String previousStatus = Status.SAVED.getValue();
                if(cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    previousStatus = Status.statusFactory(cursor.getInt(cursor.getColumnIndex(ReportEntry.COLUMN_STATUS))).getValue();
                }
                rowsUpdated = mOpenHelper.getWritableDatabase().update(
                        ReportEntry.TABLE_NAME,
                        values,
                        ReportEntry._ID + "= ?",
                        new String[] {String.valueOf(ContentUris.parseId(uri))}
                );

                //Use Android Notification to notify status change
                // User has submitted a report and is waiting for an approval
                if(previousStatus.equalsIgnoreCase(Status.PENDING.getValue())) {
                    if(values.containsKey(ReportEntry.COLUMN_STATUS)) {
                        if (values.getAsInteger(ReportEntry.COLUMN_STATUS) == Status
                                .ACCEPTED.getNumber() ||
                                values.getAsInteger(ReportEntry.COLUMN_STATUS) == Status
                                        .REJECTED.getNumber()
                                ) {
                            Utility.sendNotification(getContext(),
                                    Integer.parseInt(String.valueOf(ContentUris.parseId(uri))));
                        }
                    }
                }

                // The manager has rejected a report and is expecting that
                // the user will submit again
                if(previousStatus.equalsIgnoreCase(Status.REJECTED.getValue())) {
                    if(values.containsKey(ReportEntry.COLUMN_STATUS)) {
                        if (values.getAsInteger(ReportEntry.COLUMN_STATUS) == Status
                                .SUBMITTED.getNumber()) {
                            Utility.sendNotification(getContext(),
                                    Integer.parseInt(String.valueOf(ContentUris.parseId(uri))));
                        }
                    }
                }
                cursor.close();
                break;
            }

            case EXPENSE_ID: {
                rowsUpdated = mOpenHelper.getWritableDatabase().update(
                        ExpenseEntry.TABLE_NAME,
                        values,
                        ExpenseEntry._ID + " = ?",
                        new String[] {String.valueOf(ContentUris.parseId(uri))}
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null, networkSync(uri));
        }

        return rowsUpdated;
    }

    /**
     * TODO: implement bulk insert for expenses and employees
     * Bulk insert is expected to come from the sync adapter,
     * hence it uses no network sync to prevent sync loop
     * @param uri
     * @param values
     * @return
     */
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {

        final SQLiteDatabase sqLiteDatabase = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case REPORT: {
                sqLiteDatabase.beginTransaction();
                int returnCount = 0;
                try {
                    for(ContentValues value : values) {

                        // check if report is found before inserting
                        // prevents the base id from changing

                        Cursor cursor = getContext().getContentResolver().query(
                                ReportEntry.CONTENT_URI,
                                null,
                                ReportEntry.COLUMN_REPORT_ID + "="
                                        + value.getAsInteger(ReportEntry.COLUMN_REPORT_ID),
                                //+ " AND " + ReportEntry.COLUMN_SYNC_STATUS + "='" + SyncStatus.SYNCED.toString() + "'",
                                null,
                                null
                        );
                        // it means that the record is found, update it
                        if(cursor.getCount() > 0) {
                            cursor.moveToFirst();
                            // update synced data only. non-synced ones need to be pushed by the device to the server
                            if(cursor.getString(cursor.getColumnIndex(ReportEntry.COLUMN_SYNC_STATUS)).equalsIgnoreCase(SyncStatus.SYNCED.toString())) {

                                getContext().getContentResolver().update(
                                        ReportEntry.buildReportUriNoNetworkSync(cursor.getInt(
                                                cursor.getColumnIndex
                                                        (ReportEntry
                                                        ._ID))),
                                        value, null, null);
                            }
                        } else { // else insert a new record
                            long _id = sqLiteDatabase.insert(ReportContract
                                    .ReportEntry.TABLE_NAME, null, value);
                            if (_id != -1) {
                                returnCount++;
                            }
                        }
                        cursor.close();
                    }
                    sqLiteDatabase.setTransactionSuccessful();
                } finally {
                    sqLiteDatabase.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null,
                        networkSync(uri));
                return returnCount;
            }

            case EXPENSE: {
                sqLiteDatabase.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {

                        // check if expense report is found before inserting
                        // prevents base id from changing
                        Cursor cursor = getContext().getContentResolver().query(
                                ExpenseEntry.CONTENT_URI,
                                null,
                                ExpenseEntry.COLUMN_EXPENSE_ID + "="
                                + value.getAsInteger(ExpenseEntry.COLUMN_EXPENSE_ID),
                                null,
                                null
                        );

                        // it means that the record is found, update it
                        if(cursor.getCount() > 0) {
                            cursor.moveToFirst();
                            if(cursor.getString(cursor.getColumnIndex(ExpenseEntry.COLUMN_SYNC_STATUS)).equalsIgnoreCase(SyncStatus.SYNCED.toString())) {

                                getContext().getContentResolver().update(
                                        ExpenseEntry
                                                .buildExpenseUriNoNetworkSync(
                                                cursor.getLong(cursor.getColumnIndex(ExpenseEntry._ID))),
                                        value, null, null);
                            }
                        } else {
                            long _id = sqLiteDatabase.insert(ExpenseEntry
                                    .TABLE_NAME, null, value);
                            if (_id != -1) {
                                returnCount++;
                            }
                        }
                        cursor.close();
                    }
                    sqLiteDatabase.setTransactionSuccessful();
                } finally {
                    sqLiteDatabase.endTransaction();
                }
                getContext().getContentResolver().notifyChange(
                        uri, null, networkSync(uri));
                return returnCount;
            }

            default: {
                return super.bulkInsert(uri, values);
            }
        }
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case REPORT:
                return ReportContract.ReportEntry.CONTENT_TYPE;

            case REPORT_ID:
                return ReportContract.ReportEntry.CONTENT_ITEM_TYPE;

            case EXPENSE:
                return ReportContract.ExpenseEntry.CONTENT_TYPE;

            case EXPENSE_ID:
                return ReportContract.ExpenseEntry.CONTENT_ITEM_TYPE;

            case REPORT_ID_EXPENSE:
                return ReportContract.ExpenseEntry.CONTENT_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * Determine if the uri contains the network sync
     * query parameter. If it contains, then it means
     * that the call to the content provider was made
     * from the Sync Adapter.
     * @param uri The uri
     * @return <code>false</code> if the call was made
     * from the Sync Adapter, <code>true</code> otherwise
     */
    public boolean networkSync(Uri uri) {
        if(uri.getQueryParameter(ReportContract.NETWORK_SYNC) != null) {
            return false;
        } else {
            return true;
        }
    }
}
