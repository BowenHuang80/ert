package com.completeinnovations.ert.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.completeinnovations.ert.data.ReportContract.EmployeeEntry;
import com.completeinnovations.ert.data.ReportContract.ExpenseEntry;
import com.completeinnovations.ert.data.ReportContract.ReportEntry;

public class ReportDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "report.db";
    private static final int DATABASE_VERSION = 8;
    private static final String TEXT_NOT_NULL = " TEXT NOT NULL";
    private static final String REAL_NOT_NULL = " REAL NOT NULL";
    private static final String INTEGER_NOT_NULL = " INTEGER NOT NULL";
    private static final String COMMA = ", ";

    public ReportDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_REPORT_TABLE = "CREATE TABLE " + ReportEntry
                .TABLE_NAME + " (" +
                ReportEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ReportEntry.COLUMN_REPORT_ID + COMMA +
                ReportEntry.COLUMN_NAME + TEXT_NOT_NULL + COMMA +
                ReportEntry.COLUMN_SUBMITTER_EMAIL + COMMA +
                ReportEntry.COLUMN_APPROVER_EMAIL + COMMA +
                ReportEntry.COLUMN_COMMENT + COMMA +
                ReportEntry.COLUMN_STATUS + /*INTEGER_NOT_NULL +*/ " DEFAULT 4 " + /*CHECK (" + ReportEntry.COLUMN_STATUS + " BETWEEN 1 AND 6)" +*/ COMMA +
                ReportEntry.COLUMN_STATUS_NOTE  + COMMA +
                ReportEntry.COLUMN_TEXT + " TEXT" + COMMA +
                ReportEntry.COLUMN_CREATED_ON + COMMA +
                ReportEntry.COLUMN_SUBMITTED_ON  + COMMA +
                ReportEntry.COLUMN_APPROVED_ON + " TEXT" + COMMA +
                ReportEntry.COLUMN_IS_DELETED + " DEFAULT false " + /*"CHECK (" + ReportEntry.COLUMN_IS_DELETED + " in ('false', 'true'))" +*/ COMMA +
                ReportEntry.COLUMN_FLAG + COMMA + //" DEFAULT false CHECK (" + ReportEntry.COLUMN_FLAG + " in (false,true))" + COMMA +
                ReportEntry.COLUMN_SYNC_STATUS + " DEFAULT " + SyncStatus.SYNCED.toString() + COMMA +
                " UNIQUE (" + ReportEntry.COLUMN_REPORT_ID + ") ON CONFLICT REPLACE" +
                ");";

        final String SQL_CREATE_EXPENSE_TABLE = "CREATE TABLE " + ExpenseEntry.TABLE_NAME + "(" +
                ExpenseEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ExpenseEntry.COLUMN_EXPENSE_ID + /*INTEGER_NOT_NULL +*/ COMMA +
                ExpenseEntry.COLUMN_REPORT_ID + /*INTEGER_NOT_NULL +*/ COMMA +
                ExpenseEntry.COLUMN_EXPENSE_DATE + /*TEXT_NOT_NULL +*/ COMMA +
                ExpenseEntry.COLUMN_DESCRIPTION + /*TEXT_NOT_NULL +*/ COMMA +
                ExpenseEntry.COLUMN_CATEGORY + /*TEXT_NOT_NULL +*/ COMMA +
                ExpenseEntry.COLUMN_COST + /*REAL_NOT_NULL +*/ COMMA +
                ExpenseEntry.COLUMN_TAXES + /*REAL_NOT_NULL +*/ COMMA +
                ExpenseEntry.COLUMN_TAX_TYPE + /*TEXT_NOT_NULL +*/ COMMA +
                ExpenseEntry.COLUMN_HST + " REAL" + COMMA +
                ExpenseEntry.COLUMN_GST + " REAL" + COMMA +
                ExpenseEntry.COLUMN_QST + " REAL" + COMMA +
                ExpenseEntry.COLUMN_CURRENCY + /*TEXT_NOT_NULL +*/ COMMA +
                ExpenseEntry.COLUMN_REGION + /*TEXT_NOT_NULL +*/ COMMA +
                ExpenseEntry.COLUMN_RECEIPT_ID + /*TEXT_NOT_NULL +*/ COMMA +
                ExpenseEntry.COLUMN_CREATED_ON + /*TEXT_NOT_NULL +*/ COMMA +
                ExpenseEntry.COLUMN_IS_DELETED + /*INTEGER_NOT_NULL +*/ " DEFAULT false " + /*CHECK (" + ExpenseEntry.COLUMN_IS_DELETED + " in (0,1))" +*/ COMMA +
                ExpenseEntry.COLUMN_SYNC_STATUS + " DEFAULT " + SyncStatus.SYNCED.toString() + COMMA +
                " FOREIGN KEY (" + ExpenseEntry.COLUMN_REPORT_ID + ") REFERENCES " + ReportEntry.TABLE_NAME + "(" + ReportEntry.COLUMN_REPORT_ID + ")" +
                //" UNIQUE (" + ExpenseEntry.COLUMN_EXPENSE_ID + ") ON CONFLICT REPLACE" +  //TODO: this should be removed, as the expense id will be associated with a report.
                ");";

        final String SQL_CREATE_EMPLOYEE_TABLE = "CREATE TABLE " + EmployeeEntry.TABLE_NAME + " (" +
                EmployeeEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                EmployeeEntry.COLUMN_KEY + COMMA +
                EmployeeEntry.COLUMN_VALUE + COMMA +
                " UNIQUE (" + EmployeeEntry.COLUMN_KEY + ") ON CONFLICT REPLACE" +
                ");";

        sqLiteDatabase.execSQL(SQL_CREATE_REPORT_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_EXPENSE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_EMPLOYEE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion,
                          int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ExpenseEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ReportEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
