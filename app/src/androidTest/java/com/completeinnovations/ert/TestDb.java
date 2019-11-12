package com.completeinnovations.ert;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.completeinnovations.ert.data.ReportContract.EmployeeEntry;
import com.completeinnovations.ert.data.ReportContract.ExpenseEntry;
import com.completeinnovations.ert.data.ReportContract.ReportEntry;
import com.completeinnovations.ert.data.ReportDbHelper;

import java.util.Map;
import java.util.Set;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    public void testCreateDb() {
        mContext.deleteDatabase(ReportDbHelper.DATABASE_NAME);

        SQLiteDatabase db = new ReportDbHelper(this.mContext)
                .getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadDb() {


        ReportDbHelper reportDbHelper = new ReportDbHelper(mContext);
        SQLiteDatabase db = reportDbHelper.getWritableDatabase();


        ContentValues createReportValues = createReportValues(1);

        long reportRowId;
        reportRowId = db.insert(ReportEntry.TABLE_NAME, null, createReportValues);

        //assertTrue(reportRowId != -1);

        Log.d(LOG_TAG, "New row id: " + reportRowId);

        String[] columns = {
                ReportEntry.COLUMN_REPORT_ID,
                ReportEntry.COLUMN_NAME,
                ReportEntry.COLUMN_SUBMITTER_EMAIL,
                ReportEntry.COLUMN_APPROVER_EMAIL,
                ReportEntry.COLUMN_COMMENT,
                ReportEntry.COLUMN_STATUS,
                ReportEntry.COLUMN_STATUS_NOTE,
                ReportEntry.COLUMN_TEXT,
                ReportEntry.COLUMN_CREATED_ON,
                ReportEntry.COLUMN_APPROVED_ON,
                ReportEntry.COLUMN_SUBMITTED_ON,
                ReportEntry.COLUMN_IS_DELETED,
                ReportEntry.COLUMN_FLAG
        };

        Cursor cursor = db.query(
                ReportEntry.TABLE_NAME,
                columns,
                null,
                null,
                null,
                null,
                null
        );

//        validateCursor(cursor, createReportValues);

        ContentValues expenseValues1 = createExpenseValues(1);
        ContentValues expenseValues2 = createExpenseValues(2);
        ContentValues expenseValues3 = createExpenseValues(2);

        long ExpenseItemId1 = db.insert(ExpenseEntry.TABLE_NAME, null, expenseValues1);
        assertTrue(ExpenseItemId1 != -1);

        long ExpenseItemId2 = db.insert(ExpenseEntry.TABLE_NAME, null, expenseValues2);
        assertTrue(ExpenseItemId2 != -1);

        // will check on conflict replace
        long ExpenseItemId3 = db.insert(ExpenseEntry.TABLE_NAME, null, expenseValues3);
        assertTrue(ExpenseItemId3 != -1);

        // A cursor is your primary interface to the query results.
        Cursor expenseCursor = db.query(
                ExpenseEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        // A cursor is your primary interface to the query results.
        Cursor relationship = db.query(
                ExpenseEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                //null, // cols for "where" clause
                ExpenseEntry.COLUMN_REPORT_ID + "=?",
                //null, // values for "where" clause
                new String[] {"1"},
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );


        // join the two tables to check for relationships
        Cursor reportsAndExpense = db.query(
                ReportEntry.TABLE_NAME + " LEFT OUTER JOIN " +
                        ExpenseEntry.TABLE_NAME +
                        " ON " +
                        ReportEntry.TABLE_NAME + "." + ReportEntry.COLUMN_REPORT_ID + " = " +
                        ExpenseEntry.TABLE_NAME + "." + ExpenseEntry.COLUMN_REPORT_ID,
                null,
                ReportEntry.TABLE_NAME + "." + ReportEntry.COLUMN_REPORT_ID + "=?",
                new String[] {"1"},
                null,
                null,
                null
        );

        reportsAndExpense.moveToFirst();

        // because we added 2 expenses under 1 report, we check
        // if the query has returned two rows
        //assertEquals(2, reportsAndExpense.getCount());

        validateCursor(expenseCursor, expenseValues1);

        ContentValues employeeContentValues = createEmployeeValues();
        long employeeId = db.insert(EmployeeEntry.TABLE_NAME,
                null, employeeContentValues);
        assertTrue(employeeId != -1);


        Cursor employeeCursor = db.query(
                EmployeeEntry.TABLE_NAME, null, null, null, null, null, null);

        validateCursor(employeeCursor, employeeContentValues);

        employeeCursor.close();

        // add same key and test if the row has been overwritten
        createEmployeeValues();
        db.insert(EmployeeEntry.TABLE_NAME,
                null, employeeContentValues);
        Cursor employeeCursorOverwrite = db.query(
                EmployeeEntry.TABLE_NAME, null, null, null, null, null, null);


        int cursorCount = employeeCursorOverwrite.getCount();
        assertEquals(cursorCount, 1);

        employeeCursorOverwrite.close();


        ContentValues employeeContentValues2 = createEmployeeValues2();
        long employee2Id = db.insert(EmployeeEntry.TABLE_NAME,
                null, employeeContentValues2);
        assertTrue(employee2Id != -1);

        Cursor employeeCursor2 = db.query(
                EmployeeEntry.TABLE_NAME, null, null, null, null, null, null);

        assertEquals(employeeCursor2.getCount(), 2);


        reportDbHelper.close();

    }

    /**
     * Create content values with mock data
     * @return ContentValue
     */
    public static ContentValues createReportValues(int reportId) {

        //int reportId = 1;
        String name = "AndroidReport";
        String submitterEmail = "shashankpangam@gmail.com";
        String approverEmail = "abhinav@gmail.com";
        String comment = "TEST";
        String status = "4";
        String statusNote = "TEST";
        String text = "text";
        String createdOn = "2015-01-24T19:34:35.037";//System.currentTimeMillis()/1000;
        String submittedOn = "2015-01-25T19:34:35.037";//System.currentTimeMillis()/1000;
        String approvedOn = null;
        int isDeleted = 0;
        int flag = 0;

        ContentValues contentValues = new ContentValues();
        contentValues.put(ReportEntry.COLUMN_REPORT_ID, reportId);
        contentValues.put(ReportEntry.COLUMN_NAME, name);
        contentValues.put(ReportEntry.COLUMN_SUBMITTER_EMAIL, submitterEmail);
        contentValues.put(ReportEntry.COLUMN_APPROVER_EMAIL, approverEmail);
        contentValues.put(ReportEntry.COLUMN_COMMENT, comment);
        //contentValues.put(ReportEntry.COLUMN_STATUS, status);
        contentValues.put(ReportEntry.COLUMN_STATUS_NOTE, statusNote);
        contentValues.put(ReportEntry.COLUMN_TEXT,text);
        contentValues.put(ReportEntry.COLUMN_CREATED_ON,createdOn);
        contentValues.put(ReportEntry.COLUMN_SUBMITTED_ON,submittedOn);
        contentValues.put(ReportEntry.COLUMN_APPROVED_ON,approvedOn);
        contentValues.put(ReportEntry.COLUMN_IS_DELETED,isDeleted);
        contentValues.put(ReportEntry.COLUMN_FLAG,flag);

        return contentValues;
    }

    public static ContentValues createExpenseValues(int expenseID)  {

        //int expenseID = 1;
        int reportID = 1;
        String expenseDate = "TEXT";//System.currentTimeMillis()/1000;
        String description = "This is for the Meals";
        String category = "Meals";
        double cost = 20.3;
        double taxes = 0.13;
        String taxType = "HST";
        double hst = 0.13;
        double gst = 0.05;
        double qst = 0.03;
        String currency = "CAD";
        String region = "Toronto";
        String receiptID = "http://assistly-production.s3.amazonaws.com/breadcrumbpos/portal_attachments/21831/customer%20receipt_original.jpg?AWSAccessKeyId=AKIAJNSFWOZ6ZS23BMKQ&Expires=1422262873&Signature=iMubmkJ8vqwD6jEWsWws456DIXE%3D&response-content-disposition=filename%3D%22customer%20receipt.jpg%22&response-content-type=image%2Fjpeg";
        String createdOn = "TEXT";//System.currentTimeMillis()/1000;
        int isDeleted = 0;

        ContentValues contentValues = new ContentValues();
        contentValues.put(ExpenseEntry.COLUMN_EXPENSE_ID,expenseID);
        contentValues.put(ExpenseEntry.COLUMN_REPORT_ID,reportID);
        contentValues.put(ExpenseEntry.COLUMN_EXPENSE_DATE,expenseDate);
        contentValues.put(ExpenseEntry.COLUMN_DESCRIPTION,description);
        contentValues.put(ExpenseEntry.COLUMN_CATEGORY,category);
        contentValues.put(ExpenseEntry.COLUMN_COST,cost);
        contentValues.put(ExpenseEntry.COLUMN_TAXES,taxes);
        contentValues.put(ExpenseEntry.COLUMN_TAX_TYPE,taxType);
        contentValues.put(ExpenseEntry.COLUMN_HST,hst);
        contentValues.put(ExpenseEntry.COLUMN_GST,gst);
        contentValues.put(ExpenseEntry.COLUMN_QST,qst);
        contentValues.put(ExpenseEntry.COLUMN_CURRENCY,currency);
        contentValues.put(ExpenseEntry.COLUMN_REGION,region);
        contentValues.put(ExpenseEntry.COLUMN_RECEIPT_ID,receiptID);
        contentValues.put(ExpenseEntry.COLUMN_CREATED_ON,createdOn);
        contentValues.put(ExpenseEntry.COLUMN_IS_DELETED,isDeleted);

        return contentValues;
    }

    public static ContentValues createEmployeeValues() {
        ContentValues contentValues = new ContentValues();

        contentValues.put(EmployeeEntry.COLUMN_KEY, "Abhinav");
        contentValues.put(EmployeeEntry.COLUMN_VALUE, "abhinav1587@gmail.com");

        return contentValues;
    }

    public static ContentValues createEmployeeValues2() {
        ContentValues contentValues = new ContentValues();

        contentValues.put(EmployeeEntry.COLUMN_KEY, "Bo");
        contentValues.put(EmployeeEntry.COLUMN_VALUE, "brian80@gmail.com");

        return contentValues;
    }

    static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {

        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx == -1);
            if(entry.getValue() != null) {
                String expectedValue = entry.getValue().toString();
                assertEquals(expectedValue, valueCursor.getString(idx));
            }
        }
        valueCursor.close();
    }

}
