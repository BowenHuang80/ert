package com.completeinnovations.ert;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.completeinnovations.ert.data.ReportContract;
import com.completeinnovations.ert.data.ReportContract.ExpenseEntry;
import com.completeinnovations.ert.data.ReportContract.ReportEntry;
import com.completeinnovations.ert.data.ReportDbHelper;


public class TestProvider extends AndroidTestCase {
    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    public void testDeleteDb() throws Throwable {
        mContext.deleteDatabase(ReportDbHelper.DATABASE_NAME);
    }

    public void testGetType() {
        String type = mContext.getContentResolver().getType(ReportEntry
                .CONTENT_URI);
        assertEquals(ReportEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(ReportEntry.buildReportUri(1L));
        assertEquals(ReportEntry.CONTENT_ITEM_TYPE, type);

        type = mContext.getContentResolver().getType(ReportContract.ExpenseEntry.CONTENT_URI);
        assertEquals(ReportContract.ExpenseEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(ReportContract
                .ExpenseEntry.buildExpenseUri(1L));
        assertEquals(ReportContract.ExpenseEntry.CONTENT_ITEM_TYPE, type);
    }

    public void teestInsertReadProvider() {
        ContentValues reportValues = TestDb.createReportValues(1);

        Uri reportUri = mContext.getContentResolver().insert(ReportEntry
                .CONTENT_URI, reportValues);


        long reportRowId = ContentUris.parseId(reportUri);

        assertTrue(reportRowId != -1);

        ContentValues reportValues2 = TestDb.createReportValues(2);

        Uri reportUri2 = mContext.getContentResolver().insert(ReportEntry
                .CONTENT_URI, reportValues2);


        long reportRowId2 = ContentUris.parseId(reportUri2);

        assertTrue(reportRowId2 != -1);

        Log.d(LOG_TAG, "New row id: " + reportUri2);

        Cursor cursor = mContext.getContentResolver().query(
                ReportEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        TestDb.validateCursor(cursor, reportValues);


        cursor = mContext.getContentResolver().query(
                ReportEntry.buildReportUri(reportRowId),
                null,
                null,
                null,
                null
        );

        TestDb.validateCursor(cursor, reportValues);


        Uri reportInsertUri = mContext.getContentResolver().insert(
                ReportEntry.CONTENT_URI, reportValues);

        assertTrue(reportInsertUri != null);
    }

    public void teestInsertReadExpenseProvider() {
        ContentValues expenseValues = TestDb.createExpenseValues(1);

        Uri expenseUri = mContext.getContentResolver().insert(
                ExpenseEntry.CONTENT_URI, expenseValues);

        long expenseRowId = ContentUris.parseId(expenseUri);

        assertTrue(expenseRowId != -1);

        Cursor cursor = mContext.getContentResolver().query(
                ExpenseEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        TestDb.validateCursor(cursor, expenseValues);

        cursor = mContext.getContentResolver().query(
                ExpenseEntry.buildExpenseUri(expenseRowId),
                null,
                null,
                null,
                null
        );

        TestDb.validateCursor(cursor, expenseValues);

        ContentValues reportValues = TestDb.createReportValues(1);

        /**
         * Test query with this uri
         * content://com.completeinnovations.ert/report/1/expense
         */
        Uri reportUri = mContext.getContentResolver().insert(ReportEntry
                .CONTENT_URI, reportValues);

        cursor = mContext.getContentResolver().query(
                ReportEntry.buildReportExpense(1L),
                null,
                null,
                null,
                null
        );
        TestDb.validateCursor(cursor, expenseValues);
    }

    public void testInsertReadEmployeeProvider() {
        ContentValues contentValues1 = TestDb.createEmployeeValues();
        Uri employeeUri1 = mContext.getContentResolver().insert(
                ReportContract.EmployeeEntry.CONTENT_URI, contentValues1);

        long employeeRowId = ContentUris.parseId(employeeUri1);

        assertTrue(employeeRowId != -1);

        Cursor cursor = mContext.getContentResolver().query(
                ReportContract.EmployeeEntry.CONTENT_URI,
                null, null, null, null);

        TestDb.validateCursor(cursor, contentValues1);
    }

}
