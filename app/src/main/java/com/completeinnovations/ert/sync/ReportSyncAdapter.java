package com.completeinnovations.ert.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.completeinnovations.ert.Constants;
import com.completeinnovations.ert.R;
import com.completeinnovations.ert.Utility;
import com.completeinnovations.ert.authentication.AccountGeneral;
import com.completeinnovations.ert.data.ReportContract;
import com.completeinnovations.ert.data.ReportContract.ExpenseEntry;
import com.completeinnovations.ert.data.ReportContract.ReportEntry;
import com.completeinnovations.ert.data.SyncStatus;
import com.completeinnovations.ert.model.Status;
import com.completeinnovations.ert.receipt.ERTReceipt;
import com.completeinnovations.ert.restapi.ERTRestApi;
import com.completeinnovations.ert.restapi.ERTRestApiException;
import com.completeinnovations.ert.restapi.Employee;
import com.completeinnovations.ert.restapi.ExpenseLineItem;
import com.completeinnovations.ert.restapi.ExpenseReport;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReportSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = ReportSyncAdapter.class.getSimpleName();

    public static final int SYNC_INTERVAL = 10;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    public ReportSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras,
                              String authority, ContentProviderClient
            provider, SyncResult syncResult) {

        Log.d(LOG_TAG, extras.toString());

        Log.d(LOG_TAG, "In onPerformSync");


        ERTRestApi.init(getContext(), getCredentials());


        simulateServerUpdate();
        getAllReports();





        // get all reports


        /*ExpenseReport expenseReport = new ExpenseReport();
        expenseReport.setName("Test");
        expenseReport.setComment("test comment");
        expenseReport.setStatus(Status.SAVED.getValue());

        ExpenseReport[] expenseReports = new ExpenseReport[1];
        expenseReports[0] = expenseReport;
        ContentValues[] contentValueses = ExpenseReport.getContentValues(expenseReports);


        ContentValues contentValues = new ContentValues();

        ContentValues[] contentValuesTest = new ContentValues[1];

        contentValues.put(ReportEntry.COLUMN_NAME, "test");
        contentValues.put(ReportEntry.COLUMN_STATUS, Status.SAVED.getNumber());
        contentValues.put(ReportEntry.COLUMN_COMMENT, "test comment");

        contentValuesTest[0] = contentValues;

        Uri uri = ReportEntry
                .CONTENT_URI.buildUpon()
                .appendQueryParameter(ReportContract.NETWORK_SYNC,
                        "false").build();

        int insertcount = getContext().getContentResolver().bulkInsert(
                uri, contentValueses);

        *//*getContext().getContentResolver().insert(
                uri,contentValues
        );*/



        // notify the observer to SYNC_FINISHED that the
        // sync is finished


    }

    private void getAllReports() {
        try {
            ERTRestApi.apiGetReport(
                    new ERTRestApi.ERTRestApiListener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray jsonArray) {
                            //2015-02-01T22:51:52.477
                            Gson gson = new GsonBuilder().setDateFormat
                                    ("yyyy-MM-dd'T'HH:mm:ss").create();

                            ExpenseReport[] expenseReports = gson.fromJson(
                                    jsonArray.toString(),
                                    ExpenseReport[].class
                            );

                            saveAllReports(expenseReports);
                            saveExpenseLineItems(expenseReports);
                            pushChanges();
                            Intent i = new Intent(Constants.SYNC_FINISHED);
                            getContext().sendBroadcast(i);

                        }
                    },

                    new ERTRestApi.ERTRestApiErrorListener() {
                        @Override
                        public void onErrorResponse(
                                ERTRestApi.ERTRestApiError error) {
                            Log.e(LOG_TAG, error.getMessage());
                            pushChanges();
                            Intent i = new Intent(Constants.SYNC_FINISHED);
                            getContext().sendBroadcast(i);
                        }
                    }
            );
        } catch (ERTRestApiException | JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    /**
     * Save all reports from the server to the device
     * @param expenseReports A list of expense reports
     */
    private void saveAllReports(ExpenseReport[] expenseReports) {
        ContentValues[] expenseReportsContentValues =
                ExpenseReport.getExpenseReportContentValues(expenseReports);


        Uri uri = ReportEntry.CONTENT_URI.buildUpon().appendQueryParameter(
                ReportContract.NETWORK_SYNC, "false").build();

        int rowsInserted = getContext().getContentResolver().bulkInsert(
                        uri, expenseReportsContentValues);
        Log.d(LOG_TAG, "Reports: Bulk inserted " + rowsInserted + " rows");

        deleteReportsFromDevice(expenseReportsContentValues);
    }

    /**
     * If a report has been deleted on the server, delete it from the device.
     * @param expenseReportsContentValues ContentValues from the server.
     */
    private void deleteReportsFromDevice(ContentValues[]
                                                 expenseReportsContentValues) {
        List<Long> reportList = getListOfItemsToDelete(expenseReportsContentValues,
                ReportEntry.COLUMN_REPORT_ID,
                ReportEntry.CONTENT_URI);

        for(Long reportId : reportList) {
            Cursor cursor = getContext().getContentResolver().query(
                    ReportEntry.CONTENT_URI,
                    null,
                    ReportEntry.COLUMN_REPORT_ID + "=" + reportId,
                    null, null
            );
            Uri reportUri = null;
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                reportUri = ReportEntry.buildReportUri(cursor.getLong(cursor.getColumnIndex(ReportEntry._ID)));
            }
            cursor.close();
            if(reportUri != null) {
                getContext().getContentResolver().delete(reportUri, null, null);
            }
        }

    }

    /**
     * Find the difference of reports from the server and the device
     * @param contentValuesArray
     * @param itemIdColumnName The item id column name, for example ReportEntry.COLUMN_REPORT_ID
     * @param itemContentUri The content uri of the item, for example ReportEntry.CONTENT_URI
     * @return
     */
    private List<Long> getListOfItemsToDelete(ContentValues[]
                                                      contentValuesArray,
                                              String itemIdColumnName,
                                              Uri itemContentUri) {


        /** Maybe a report or an expense line item */
        List<Long> serverItemIDList = new ArrayList<>();

        /** Maybe a report or an expense line item */
        List<Long> deviceSyncedItemIDList = new ArrayList<>();
        //list of items from server
        for(ContentValues contentValues : contentValuesArray) {
            serverItemIDList.add(contentValues.getAsLong(itemIdColumnName));
        }

        Cursor cursor = getContext().getContentResolver().query(
                itemContentUri,
                null,
                ReportEntry.COLUMN_SYNC_STATUS + "=?",
                new String[]{SyncStatus.SYNCED.toString()},
                null
        );

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {

                deviceSyncedItemIDList.add(cursor.getLong(cursor
                        .getColumnIndex(itemIdColumnName)));

            } while(cursor.moveToNext());
        }

        List<Long> diffList = new ArrayList<>();

        for(Long deviceReportId : deviceSyncedItemIDList) {
            if(!serverItemIDList.contains(deviceReportId)) {
                diffList.add(deviceReportId);
            }
        }

        return diffList;

    }

    private void saveExpenseLineItems(ExpenseReport[] expenseReports) {

        List<ExpenseLineItem> expenseLineItemList = new ArrayList<>();

        for(ExpenseReport expenseReport : expenseReports) {
            Collections.addAll(expenseLineItemList,
                    expenseReport.getExpenseLineItems());
        }

        ContentValues[] expenseLineItemsValues =
                ExpenseLineItem.getExpenseLineItemContentValues(expenseLineItemList);

        Uri uri = ExpenseEntry.CONTENT_URI.buildUpon().appendQueryParameter(
                ReportContract.NETWORK_SYNC, "false").build();

        int rowsInserted = getContext().getContentResolver().bulkInsert(
                        uri, expenseLineItemsValues);
        Log.d(LOG_TAG, "Expense Line Items: Bulk inserted " + rowsInserted +" rows");

        //TODO: delete expenses from device
        deleteExpenseLineItemsFromDevice(expenseLineItemsValues);
    }

    /**
     * If an expense line item has been delete from the server, delete it
     * from the device.
     * @param expenseLineItemsValues ContentValues from the server.
     */
    private void deleteExpenseLineItemsFromDevice(ContentValues[]
                                                          expenseLineItemsValues) {

        List<Long> expenseLineItemList = getListOfItemsToDelete(expenseLineItemsValues,
                ExpenseEntry.COLUMN_EXPENSE_ID,
                ExpenseEntry.CONTENT_URI
        );

        for(Long expenseLineItemId : expenseLineItemList) {
            Cursor cursor = getContext().getContentResolver().query(
                    ExpenseEntry.CONTENT_URI,
                    null,
                    ExpenseEntry.COLUMN_EXPENSE_ID + "=" + expenseLineItemId,
                    null, null);
            Uri expenseUri = null;
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                expenseUri = ExpenseEntry.buildExpenseUri(cursor.getLong(cursor.getColumnIndex(ExpenseEntry._ID)));
            }
            cursor.close();
            if(expenseUri != null) {
                getContext().getContentResolver().delete(expenseUri, null, null);
            }
        }

    }

    /**
     * Retrieve the account name and password from the Android account manager
     * @return username:password
     */
    private String getCredentials() {
        String credentials = "";
        AccountManager accountManager = AccountManager.get(getContext());

        final Account availableAccounts[] = accountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE);

        //there should be only one ERT account
        if(availableAccounts.length > 0) {
            Account account = availableAccounts[0];
            AccountManagerFuture<Bundle> future = accountManager.getAuthToken(account, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, null, null, null);
            try {
                Bundle bundle = future.getResult();
                String accountName = bundle.getString(AccountManager.KEY_ACCOUNT_NAME);
                String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                credentials = accountName + ":" + token;
            } catch (OperationCanceledException | IOException | AuthenticatorException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
        return credentials;
    }

    private void simulateServerUpdate() {
        Cursor cursor = getContext().getContentResolver().query(
                ReportEntry.CONTENT_URI,
                null,
                ReportEntry.COLUMN_STATUS + "=" + String.valueOf(Status.PENDING.getNumber()),
                null, //new String[] {String.valueOf(Status.PENDING.getNumber())},
                null
                );
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                ContentValues contentValues = new ContentValues();
                contentValues.put(ReportEntry.COLUMN_STATUS, Status.SUBMITTED.getNumber());
                contentValues.put(ReportEntry.COLUMN_NAME, cursor.getString
                        (cursor.getColumnIndex(ReportEntry.COLUMN_NAME)));
                contentValues.put(ReportEntry.COLUMN_SYNC_STATUS, SyncStatus.REPORT_PENDING.toString());
                int reportBaseId = cursor.getInt(cursor.getColumnIndex(ReportEntry._ID));
                getContext().getContentResolver().update(ReportEntry
                        .buildReportUriNoNetworkSync(new Long(reportBaseId)),contentValues, null, null);
            } while(cursor.moveToNext());
        }

        cursor.close();
    }

    public void pushChanges() {
        try {
            retrieveListOfEmployees();
            pushSavedReport();
            pushEditedReport();
            requestDeleteReport();
            requestDeleteExpenseLineItem();
            submitReport();
            pushEditedExpenseLineItem();
        } catch (JSONException | ERTRestApiException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    private void retrieveListOfEmployees() {
        final Gson gson = new Gson();

        try {
            ERTRestApi.apiGetEmployeeList(
                    new ERTRestApi.ERTRestApiListener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            for(int i=0; i<response.length(); i++) {
                                JSONObject keyvaluepair;
                                try {
                                    keyvaluepair = (JSONObject)response.get(i);
                                    Employee employee = gson.fromJson(keyvaluepair.toString(), Employee.class);

                                    saveEmployee(employee);

                                    Log.d(LOG_TAG, employee.toString());
                                } catch (JSONException e) {
                                    Log.e(LOG_TAG, e.getMessage());
                                }
                            }
                            //resultReport
                        }
                    },
                    new ERTRestApi.ERTRestApiErrorListener() {
                        @Override
                        public void onErrorResponse(ERTRestApi.ERTRestApiError error) {
                            Log.d(LOG_TAG, error.getMessage());
                        }
                    });
        } catch (ERTRestApiException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    /**
     * Saves an employee in the local storage
     * @param employee An employee
     */
    private void saveEmployee(Employee employee) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ReportContract.EmployeeEntry.COLUMN_KEY, employee.getKey());
        contentValues.put(ReportContract.EmployeeEntry.COLUMN_VALUE, employee.getValue());

        getContext().getContentResolver().insert(
                ReportContract.EmployeeEntry.CONTENT_URI,
                contentValues
        );

    }

    private void pushEditedExpenseLineItem() throws ERTRestApiException,
            JSONException {
        Cursor cursor = getEditedExpenseLineItemCursor();

        if(cursor.getCount() != 0) {
            cursor.moveToFirst();



            do {
                final Long expenseBaseId = cursor.getLong(
                        cursor.getColumnIndex(ExpenseEntry._ID));
                ExpenseLineItem expenseLineItem =
                        Utility.createExpenseLineItemFromCursor(cursor);
                ERTRestApi.apiEditExpenseLineItem(expenseLineItem,
                        new ERTRestApi.ERTRestApiListener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                updateExpenseLineItemSyncStatus(
                                        expenseBaseId, SyncStatus.SYNCED);
                            }
                        },
                        new ERTRestApi.ERTRestApiErrorListener() {
                            @Override
                            public void onErrorResponse(ERTRestApi.ERTRestApiError error) {
                                Log.e(LOG_TAG, error.getMessage());
                            }
                        }
                        );
                updateExpenseLineItemSyncStatus(expenseBaseId,SyncStatus.SYNC_IN_PROGRESS);


                final String receiptId = cursor.getString(cursor.getColumnIndex
                        (ExpenseEntry.COLUMN_RECEIPT_ID));

                if(receiptId != null) {
                    InputStream imageInputStream = null;

                    try {
                        //imageInputStream = //new FileInputStream(new File(ERTReceipt.getAbsolutePath(receiptId)));

                        imageInputStream = getContext().getContentResolver().openInputStream(ERTReceipt.getUri(receiptId));
                    } catch (FileNotFoundException e) {
                        Log.e(LOG_TAG, e.getMessage());
                    }

                    ERTRestApi.apiUploadImage(
                            cursor.getInt(cursor.getColumnIndex(ExpenseEntry
                                    .COLUMN_EXPENSE_ID)),
                            receiptId,
                            imageInputStream,
                            new ERTRestApi.ERTRestApiListener<String>() {
                                @Override
                                public void onResponse(String response) {

                                    Log.i(LOG_TAG, response);
                                }
                            },
                            new ERTRestApi.ERTRestApiErrorListener() {
                                @Override
                                public void onErrorResponse(ERTRestApi
                                                                    .ERTRestApiError error) {
                                    Log.e(LOG_TAG, error.getMessage());
                                }
                            }
                    );
                }


            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    /**
     * Find all the expense line items and push
     */
    private void pushSavedExpenseLineItem(){
        //Cursor cursor = getExpenseStatusCursor(SyncStatus.SAVED_REPORT);
        Cursor cursor = getSavedExpenseLineItemCursor();

        if(cursor.getCount() != 0) {
            cursor.moveToFirst();

            do {
                final Long expenseBaseId = cursor.getLong(
                        cursor.getColumnIndex(ExpenseEntry._ID));

                ExpenseLineItem expenseLineItem = new ExpenseLineItem();

                expenseLineItem.setDescription(
                        cursor.getString(cursor.getColumnIndex(
                                ExpenseEntry.COLUMN_DESCRIPTION)));

                expenseLineItem.setExpenseReportId(cursor.getInt(
                        cursor.getColumnIndex(ExpenseEntry.COLUMN_REPORT_ID)));

                expenseLineItem.setExpenseDate(
                        cursor.getString(cursor.getColumnIndex(
                                ExpenseEntry.COLUMN_EXPENSE_DATE)));

                expenseLineItem.setCategory(cursor.getString(cursor.getColumnIndex(
                        ExpenseEntry.COLUMN_CATEGORY)));

                expenseLineItem.setCost(cursor.getFloat(cursor.getColumnIndex(
                                ExpenseEntry.COLUMN_COST)));

                expenseLineItem.setHst(cursor.getFloat(cursor.getColumnIndex(
                        ExpenseEntry.COLUMN_HST)));
                expenseLineItem.setGst(cursor.getFloat(cursor.getColumnIndex(
                        ExpenseEntry.COLUMN_GST)));
                expenseLineItem.setQst(cursor.getFloat(cursor.getColumnIndex(
                        ExpenseEntry.COLUMN_QST)));
                expenseLineItem.setCurrency(cursor.getString(cursor.getColumnIndex(
                        ExpenseEntry.COLUMN_CURRENCY)));
                expenseLineItem.setRegion(cursor.getString(cursor.getColumnIndex(
                        ExpenseEntry.COLUMN_REGION)));

                //TODO: receipt

                try {
                    ERTRestApi.apiAddExpenseLineItem(
                            expenseLineItem,
                            new ERTRestApi.ERTRestApiListener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Gson gson = new GsonBuilder().setDateFormat
                                            ("yyyy-MM-dd'T'HH:mm:ss").create();
                                    ExpenseLineItem expenseLineItemResponse =
                                            gson.fromJson(response.toString(),
                                                    ExpenseLineItem.class
                                            );

                                    updateExpenseLineItemSyncStatus(expenseBaseId,
                                            SyncStatus.SYNCED
                                            );

                                    setExpenseLineItemValuesFromServer(
                                            expenseBaseId,
                                            expenseLineItemResponse
                                    );

                                }
                            },

                            new ERTRestApi.ERTRestApiErrorListener() {
                                @Override
                                public void onErrorResponse(ERTRestApi
                                                                    .ERTRestApiError error) {

                                    Log.e(LOG_TAG, error.getMessage());

                                }
                            }
                    );
                } catch (ERTRestApiException | JSONException e) {
                    Log.e(LOG_TAG, e.getMessage());
                }

                updateExpenseLineItemSyncStatus(expenseBaseId,SyncStatus.SYNC_IN_PROGRESS);

            } while (cursor.moveToNext());
        }
        cursor.close();
    }


    /**
     * Find all reports having the SyncStatus report pending and submit
     * all the pending reports.
     * @throws ERTRestApiException
     * @throws JSONException
     */
    private void submitReport() throws ERTRestApiException, JSONException {
        Cursor cursor = getReportStatusCursor(SyncStatus.REPORT_PENDING);

        if(cursor.getCount() != 0) {
            cursor.moveToFirst();


            do {
                final Long reportBaseId = cursor.getLong(
                        cursor.getColumnIndex(ReportEntry._ID)
                );
                ExpenseReport expenseReport = Utility.createExpenseReportFromCursor(cursor);

                ERTRestApi.apiSubmitReport(expenseReport,
                        new ERTRestApi
                                .ERTRestApiListener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d(LOG_TAG, "Report " + reportBaseId + " submitted");
                                updateReportSyncStatus(reportBaseId, SyncStatus.SYNCED);
                            }
                        },
                        new ERTRestApi.ERTRestApiErrorListener() {
                            @Override
                            public void onErrorResponse(ERTRestApi
                                                                .ERTRestApiError error) {
                                ContentValues contentValues = new ContentValues();
                                contentValues.put(ReportEntry.COLUMN_STATUS, Status.SAVED.getValue());

                                getContext().getContentResolver().update(
                                        ReportEntry.buildReportUriNoNetworkSync(reportBaseId),
                                        contentValues,
                                        null,
                                        null
                                );
                                updateReportSyncStatus(reportBaseId, SyncStatus.SYNCED);
                            }
                        }

                );

                updateReportSyncStatus(reportBaseId, SyncStatus.SYNC_IN_PROGRESS);

            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    /**
     * Look for reports having delete status and request a delete
     * from the server. If the delete has been successful on the server,
     * delete it from the local database, else display the report again on
     * the UI by changing the isDelete column to false.
     * TODO: Delete all expense line items
     * @throws ERTRestApiException
     * @throws JSONException
     */
    private void requestDeleteReport() throws ERTRestApiException, JSONException {
        final Cursor cursor = getReportStatusCursor(SyncStatus.DELETED_REPORT);

        if(cursor.getCount() != 0) {
            cursor.moveToFirst();


            do {
                final Long reportBaseId = cursor.getLong(
                        cursor.getColumnIndex(ReportEntry._ID));
                ExpenseReport expenseReport = Utility.createExpenseReportFromCursor
                        (cursor);
                final int reportId = expenseReport.getId();

                // update the status of delete to true in the mean time
                // if there is an error from the server, the delete
                // status should be changed to false
                ContentValues contentValues = new ContentValues();
                contentValues.put(ReportEntry.COLUMN_IS_DELETED, "true");
                getContext().getContentResolver().update(
                        ReportEntry.buildReportUriNoNetworkSync(reportId),
                        contentValues,
                        null,
                        null
                );

                ERTRestApi.apiDeleteReport(
                        reportId,
                        new ERTRestApi.ERTRestApiListener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {

                                // delete all expense line items
                                Cursor expenseLineItemCursor = getContext().getContentResolver().query(
                                        ReportContract.ReportEntry.buildReportExpense(reportBaseId),
                                        null, null, null, null
                                );
                                int expenseLineItemDeleteCount = 0;
                                if(expenseLineItemCursor.getCount() > 0) {
                                    expenseLineItemCursor.moveToFirst();
                                    do {
                                        expenseLineItemDeleteCount += getContext().getContentResolver().delete(
                                                ExpenseEntry.buildExpenseUri(expenseLineItemCursor.getInt(expenseLineItemCursor.getColumnIndex(ExpenseEntry._ID))),
                                                null, null
                                        );
                                    } while (expenseLineItemCursor.moveToNext());
                                }
                                expenseLineItemCursor.close();
                                Log.i(LOG_TAG, "Deleted " + expenseLineItemDeleteCount + " expense line items");

                                getContext().getContentResolver().delete(
                                        ReportEntry.buildReportUriNoNetworkSync
                                                (reportBaseId),
                                        null,
                                        null
                                );
                                Log.d(LOG_TAG, "Report " + reportBaseId + " " +
                                        "deleted");
                            }
                        },
                        new ERTRestApi.ERTRestApiErrorListener() {
                            @Override
                            public void onErrorResponse(ERTRestApi
                                                                .ERTRestApiError error) {
                                // try delete again
                                /*updateReportSyncStatus(reportBaseId,
                                        SyncStatus.DELETED_REPORT);*/

                                // the delete failed, put the report back in
                                // the list
                                ContentValues contentValues = new
                                        ContentValues();
                                contentValues.put(ReportEntry
                                        .COLUMN_IS_DELETED, "false");
                                getContext().getContentResolver().update(
                                        ReportEntry
                                                .buildReportUriNoNetworkSync
                                                        (reportId),
                                        contentValues,
                                        null,
                                        null
                                );

                                Log.e(LOG_TAG, error.getMessage());
                            }
                        }
                );

                updateReportSyncStatus(reportBaseId,SyncStatus.SYNC_IN_PROGRESS);

            } while (cursor.moveToNext());

        }
        cursor.close();
    }

    /**
     * Delete all the deleted Expense Line Items from the server
     * @throws ERTRestApiException
     */
    private void requestDeleteExpenseLineItem() throws ERTRestApiException {
        final Cursor cursor = getContext().getContentResolver().query(
                ExpenseEntry.CONTENT_URI,
                null,
                ExpenseEntry.COLUMN_IS_DELETED + "='true'",
                null, null
        );

        if(cursor.getCount() != 0) {
            cursor.moveToFirst();

            do {

                final int expenseLineItemId = cursor.getInt(cursor
                        .getColumnIndex(ExpenseEntry.COLUMN_EXPENSE_ID));

                final long expenseLineItemBaseId = cursor.getLong(cursor
                        .getColumnIndex(ExpenseEntry._ID));

                ERTRestApi.apiDeleteExpenseLineItem(
                        expenseLineItemId,
                        new ERTRestApi.ERTRestApiListener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                getContext().getContentResolver().delete(
                                        ExpenseEntry.buildExpenseUri(expenseLineItemBaseId),
                                        null, null
                                );
                            }
                        },
                        new ERTRestApi.ERTRestApiErrorListener() {
                            @Override
                            public void onErrorResponse(ERTRestApi.ERTRestApiError error) {
                                ContentValues contentValues = new ContentValues();
                                contentValues.put(ExpenseEntry.COLUMN_IS_DELETED, "false");
                                getContext().getContentResolver().update(
                                        ExpenseEntry.buildExpenseUri(expenseLineItemBaseId),
                                        contentValues, null, null
                                );
                            }
                        }
                );

            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    /**
     * Look for edited reports and push them to the ERT API
     * @throws JSONException
     */
    private void pushEditedReport() throws JSONException {
        Cursor cursor = getEditedReportCursor();

        if(cursor.getCount() != 0) {
            cursor.moveToFirst();



            int updatedRows;

            do {
                final Long reportBaseId = cursor.getLong(
                        cursor.getColumnIndex(ReportEntry._ID));
                final ExpenseReport expenseReport = Utility
                        .createExpenseReportFromCursor(cursor);

                ERTRestApi.apiEditReport(
                        expenseReport,
                        new ERTRestApi.ERTRestApiListener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {

                                pushSavedExpenseLineItem();

                                int updatedRows = updateReportSyncStatus
                                        (reportBaseId,
                                                SyncStatus.SYNCED
                                        );
                                Log.d(LOG_TAG, "SyncStatus after test " +
                                        getReportSyncStatus(getContext(),
                                                reportBaseId));
                                Log.d(LOG_TAG, "Report ID: " + expenseReport.getId());

                            }
                        },
                        new ERTRestApi.ERTRestApiErrorListener() {
                            @Override
                            public void onErrorResponse(ERTRestApi
                                                                .ERTRestApiError error) {

                                int updatedRows = updateReportSyncStatus
                                        (reportBaseId, SyncStatus.EDITED_REPORT);
                                Log.d(LOG_TAG, "updated rows edit report error:" + updatedRows);
                                Log.e(LOG_TAG, error.getMessage());

                            }
                        });

                updatedRows = updateReportSyncStatus(reportBaseId,
                        SyncStatus.SYNC_IN_PROGRESS
                );

                Log.d(LOG_TAG, "updated rows edit report complete:" + updatedRows);

            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    /**
     * Look for saved report and push them to the ERT API
     * @throws JSONException
     */
    private void pushSavedReport() throws JSONException {
        final Cursor cursor = getSavedReportCursor();

        if(cursor.getCount() != 0) {
            cursor.moveToFirst();



            int updatedRows = 0;

            do {
                final Long reportBaseId = cursor.getLong(
                        cursor.getColumnIndex(ReportEntry._ID));
                final int reportId = cursor.getInt(
                        cursor.getColumnIndex(ReportEntry.COLUMN_REPORT_ID));
                ExpenseReport expenseReport = new ExpenseReport();
                expenseReport.setName(cursor.getString(
                        cursor.getColumnIndex(ReportEntry.COLUMN_NAME)));
                expenseReport.setApproverEmail(cursor.getString(
                        cursor.getColumnIndex(ReportEntry.COLUMN_APPROVER_EMAIL)
                ));

                expenseReport.setComment(cursor.getString(cursor.getColumnIndex(
                        ReportEntry.COLUMN_COMMENT)));

                ERTRestApi.apiSaveReport(
                        expenseReport,
                        new ERTRestApi.ERTRestApiListener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Gson gson = new GsonBuilder().setDateFormat
                                        ("yyyy-MM-dd'T'HH:mm:ss").create();
                                ExpenseReport expenseReportResponse =
                                        gson.fromJson(
                                                response.toString(),
                                                ExpenseReport.class
                                        );

                                if(!getReportSyncStatus(getContext(), reportBaseId).equalsIgnoreCase(SyncStatus.EDITED_REPORT.toString())) {

                                    int updatedRows = updateReportSyncStatus
                                            (reportBaseId,
                                                    SyncStatus.SYNCED);
                                }


                                updateExpenseLineItemReportId(reportBaseId,
                                        expenseReportResponse.getId()
                                        );
                                setReportValuesFromServer(reportBaseId, expenseReportResponse);

                                pushSavedExpenseLineItem();


                            }
                        },
                        new ERTRestApi.ERTRestApiErrorListener() {
                            @Override
                            public void onErrorResponse(ERTRestApi.ERTRestApiError error) {
                                int updatedRows = updateReportSyncStatus(reportBaseId, SyncStatus.SAVED_REPORT);
                                Log.d(LOG_TAG, "updated rows save report error:" + updatedRows);
                                Log.e(LOG_TAG, error.getMessage());
                            }
                        }
                        );

                if(getReportSyncStatus(getContext(), reportBaseId).equalsIgnoreCase(SyncStatus.SAVED_REPORT.toString())) {

                    updatedRows = updateReportSyncStatus(reportBaseId,
                            SyncStatus.SYNC_IN_PROGRESS
                    );
                }

                Log.d(LOG_TAG, "updated rows save report:" + updatedRows);

            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    /**
     * Update the expense line item report id with the one from the server.
     * @param reportBaseId The base id of the report
     * @param newReportId The new id from the server
     */
    private void updateExpenseLineItemReportId(Long reportBaseId, int newReportId) {
        Cursor cursor = getContext().getContentResolver().query(
                ReportEntry.buildReportExpense(reportBaseId),
                null, null, null, null
        );

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                ContentValues contentValues = new ContentValues();
                contentValues.put(ExpenseEntry.COLUMN_REPORT_ID, newReportId);
                long expenseBaseId = cursor.getLong(cursor.getColumnIndex(
                        ExpenseEntry._ID
                ));

                getContext().getContentResolver().update(
                        ExpenseEntry.buildExpenseUri(expenseBaseId),
                        contentValues,
                        null, null
                );

            } while(cursor.moveToNext());
        }
        cursor.close();
    }

    /**
     * Update the report using the content provider with the values received
     * from the ERT API server.
     * @param reportBaseId the base id of the report found in sqlite
     * @param er the report response from the server
     * @return int The number of rows updated.
     */
    private int setReportValuesFromServer(Long reportBaseId, ExpenseReport er) {

        ContentValues cv = new ContentValues();

        cv.put(ReportEntry.COLUMN_REPORT_ID, er.getId());

        if(er.getName() != null) {
            cv.put(ReportEntry.COLUMN_NAME, er.getName());
        }

        if(er.getSubmitterEmail() != null) {
            cv.put(ReportEntry.COLUMN_SUBMITTER_EMAIL, er.getSubmitterEmail());
        }

        if(er.getApproverEmail() != null) {
            cv.put(ReportEntry.COLUMN_APPROVER_EMAIL, er.getApproverEmail());
        }

        if(er.getComment() != null) {
            cv.put(ReportEntry.COLUMN_COMMENT, er.getComment());
        }

        if(er.getStatus() != null) {
            cv.put(ReportEntry.COLUMN_STATUS,
                    Status.statusFactory(
                            er.getStatus()).getNumber());
        }

        if(er.getStatusNote() != null) {
            cv.put(ReportEntry.COLUMN_STATUS_NOTE, er.getStatusNote());
        }

        if(er.getText() != null) {
            cv.put(ReportEntry.COLUMN_TEXT, er.getText());
        }

        if(er.getCreatedOn() != null) {
            cv.put(ReportEntry.COLUMN_CREATED_ON, er.getCreatedOn());
        }

        if(er.getSubmittedOn() != null) {
            cv.put(ReportEntry.COLUMN_SUBMITTED_ON, er.getSubmittedOn());
        }

        if(er.getApprovedOn() != null) {
            cv.put(ReportEntry.COLUMN_APPROVED_ON, er.getApprovedOn());
        }

        String isDeleteString;
        if(er.isDeleted()) {
            isDeleteString = "true";
        } else {
            isDeleteString = "false";
        }
        cv.put(ReportEntry.COLUMN_IS_DELETED, isDeleteString);
        cv.put(ReportEntry.COLUMN_FLAG, er.isFlagged());

        return getContext().getContentResolver().update(
               ReportEntry.buildReportUriNoNetworkSync(reportBaseId),
                cv,
                null,
                null
        );
    }


    private int setExpenseLineItemValuesFromServer(
            Long expenseBaseId, ExpenseLineItem eli) {

        ContentValues cv = new ContentValues();

        cv.put(ExpenseEntry.COLUMN_EXPENSE_ID, eli.getId());

        if(eli.getExpenseDate() != null) {
            String properDate = Utility.dateFormatter(eli.getExpenseDate());
            cv.put(ExpenseEntry.COLUMN_EXPENSE_DATE, properDate);
        }

        if(eli.getDescription() != null) {
            cv.put(ExpenseEntry.COLUMN_DESCRIPTION, eli.getDescription());
        }

        if(eli.getCategory() != null) {
            cv.put(ExpenseEntry.COLUMN_CATEGORY, eli.getCategory());
        }

        cv.put(ExpenseEntry.COLUMN_COST, eli.getCost());
        cv.put(ExpenseEntry.COLUMN_TAXES, eli.getTaxes());

        if(eli.getTaxType() != null) {
            cv.put(ExpenseEntry.COLUMN_TAX_TYPE, eli.getTaxType());
        }

        cv.put(ExpenseEntry.COLUMN_HST, eli.getHst());
        cv.put(ExpenseEntry.COLUMN_GST, eli.getGst());
        cv.put(ExpenseEntry.COLUMN_QST, eli.getQst());

        if(eli.getCurrency() != null) {
            cv.put(ExpenseEntry.COLUMN_CURRENCY, eli.getCurrency());
        }

        if(eli.getRegion() != null) {
            cv.put(ExpenseEntry.COLUMN_REGION, eli.getRegion());
        }

        if(eli.getReceiptId() != null) {
            cv.put(ExpenseEntry.COLUMN_RECEIPT_ID, eli.getReceiptId());
        }

        if(eli.getCreatedOn() != null) {
            cv.put(ExpenseEntry.COLUMN_CREATED_ON, eli.getCreatedOn());
        }

        cv.put(ExpenseEntry.COLUMN_IS_DELETED, eli.isDeleted());

        return getContext().getContentResolver().update(
                ExpenseEntry.buildExpenseUriNoNetworkSync(expenseBaseId),
                cv,
                null,
                null
        );

    }



    /**
     * Set the report id received from the ERT API
     * @param reportBaseId the base id of the report entry
     * @param id The id from the ERT API
     * @return An integer indicating the number of rows updated
     *         A return value greater than one indicates a successful update
     *         0 means no update has been done.
     */
    private int setReportId(Long reportBaseId, int id) {

        ContentValues contentValues = new ContentValues();

        contentValues.put(ReportEntry.COLUMN_REPORT_ID, id);

        return getContext().getContentResolver().update(
                ReportEntry.buildReportUriNoNetworkSync(reportBaseId),
                contentValues,
                null,
                null);
    }

    /**
     * Update the report with the syncStatus
     * @param reportBaseId The base id of the report
     * @param syncStatus The sync status to set the report to
     */
    public int updateReportSyncStatus(Long reportBaseId, SyncStatus syncStatus) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ReportEntry.COLUMN_SYNC_STATUS,
                syncStatus.toString());

        // update the report and set the sync status to SYNCED
        // build a report uri with the base id
       return getContext().getContentResolver().update(
                ReportEntry.buildReportUriNoNetworkSync(reportBaseId),
                contentValues,
                null,
                null
        );
    }

    public int updateExpenseLineItemSyncStatus(Long expenseBaseId, SyncStatus syncStatus) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ExpenseEntry.COLUMN_SYNC_STATUS,
                syncStatus.toString());

        return getContext().getContentResolver().update(
                ExpenseEntry.buildExpenseUriNoNetworkSync(expenseBaseId),
                contentValues,
                null,
                null
        );

    }

    public static String getReportSyncStatus(Context context, Long reportBaseId) {
        Cursor cursor = context.getContentResolver().query(
                ReportEntry.buildReportUri(reportBaseId),
                new String[] {ReportEntry.COLUMN_SYNC_STATUS},
                null,
                null,
                null
        );

        String syncStatus;

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            syncStatus = cursor.getString(0);
        } else syncStatus = "No sync status has been set";

        if(syncStatus == null) {
            syncStatus = SyncStatus.SYNCED.toString();
        }
        cursor.close();
        return syncStatus;
    }


    /**
     * Get a cursor to all newly saved reports
     * @return A cursor
     */
    public Cursor getSavedReportCursor() {
        return getContext().getContentResolver().query(
                ReportEntry.CONTENT_URI,
                null,
                //ReportEntry.COLUMN_SYNC_STATUS + " =? OR " +
                //ReportEntry.COLUMN_SYNC_STATUS + " =? " +
                        //ReportEntry.COLUMN_SYNC_STATUS + " =? " +
                        /*" AND " +*/ ReportEntry.COLUMN_REPORT_ID + " LIKE ?",
                new String[] {
                        //SyncStatus.SAVED_REPORT.toString(),
                        //SyncStatus.EDITED_REPORT.toString(),
                        //Constants.INTERNAL_REPORT_ID
                        "-%"
                       // SyncStatus.SYNC_IN_PROGRESS.toString(),
                },
                null
        );
    }

    public Cursor getSavedExpenseLineItemCursor() {
        return getContext().getContentResolver().query(
                ExpenseEntry.CONTENT_URI,
                null,
                ExpenseEntry.COLUMN_SYNC_STATUS + " =? OR " +
                        ExpenseEntry.COLUMN_SYNC_STATUS + " =? " +
                        //ReportEntry.COLUMN_SYNC_STATUS + " =? " +
                        " AND " + ExpenseEntry.COLUMN_EXPENSE_ID + "=?",
                new String[] {
                        SyncStatus.SAVED_REPORT.toString(),
                        SyncStatus.EDITED_REPORT.toString(),
                        Constants.INTERNAL_EXPENSE_LINE_ITEM_ID
                        // SyncStatus.SYNC_IN_PROGRESS.toString(),
                },
                null
        );
    }

    /**
     * Get a cursor to all newly edited reports
     * @return A cursor
     */
    public Cursor getEditedReportCursor() {
        return getContext().getContentResolver().query(
                ReportEntry.CONTENT_URI,
                null,
                ReportEntry.COLUMN_SYNC_STATUS + " =? " +
                        //ReportEntry.COLUMN_SYNC_STATUS + " =? " +
                        " AND " + ReportEntry.COLUMN_REPORT_ID + " NOT LIKE ?",
                new String[] {
                        SyncStatus.EDITED_REPORT.toString(),
                        "-%"
                        // SyncStatus.SYNC_IN_PROGRESS.toString(),
                        //Constants.INTERNAL_REPORT_ID
                },
                null
        );
    }

    /**
     * Get a cursor to all newly edited reports
     * @return A cursor
     */
    public Cursor getEditedExpenseLineItemCursor() {
        return getContext().getContentResolver().query(
                ExpenseEntry.CONTENT_URI,
                null,
                ExpenseEntry.COLUMN_SYNC_STATUS + " =? " +
                        //ReportEntry.COLUMN_SYNC_STATUS + " =? " +
                        " AND " + ExpenseEntry.COLUMN_EXPENSE_ID + "!=?",
                new String[] {
                        SyncStatus.EDITED_REPORT.toString(),
                        // SyncStatus.SYNC_IN_PROGRESS.toString(),
                        Constants.INTERNAL_EXPENSE_LINE_ITEM_ID
                },
                null
        );
    }

    /**
     * Get a cursor to reports having syncStatus
     * @param syncStatus The sync status of the report
     * @return a Cursor
     */
    public Cursor getReportStatusCursor(SyncStatus syncStatus) {
        return getContext().getContentResolver().query(
                ReportEntry.CONTENT_URI,
                null,
                ReportEntry.COLUMN_SYNC_STATUS + " =? ",
                new String[] {syncStatus.toString()},
                null
        );
    }

    /**
     * Get a cursor to expenses having syncStatus
     * @param syncStatus The sync status of the epxnese
     * @return a Cursor
     */
    public Cursor getExpenseStatusCursor(SyncStatus syncStatus) {
        return getContext().getContentResolver().query(
                ExpenseEntry.CONTENT_URI,
                null,
                ExpenseEntry.COLUMN_SYNC_STATUS + " =? ",
                new String[] {syncStatus.toString()},
                null
        );
    }


    public Cursor getCursorFromBaseId(long baseId) {
        return getContext().getContentResolver().query(
                ReportEntry.buildReportUri(baseId),
                null,
                null,
                null,
                null
                );
    }


    public static void configurePeriodicSync(Context context,
                                             int syncInterval, int flexTime) {

        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SyncRequest syncRequest = new SyncRequest.Builder().syncPeriodic
                    (syncInterval, flexTime).setSyncAdapter(account,
                    authority).setExtras(new Bundle()).build();
            ContentResolver.requestSync(syncRequest);
        } else {
            ContentResolver.addPeriodicSync(account, authority, new Bundle(),
                    syncInterval);
        }

    }

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    public static Account getSyncAccount(Context context) {
        AccountManager accountManager = (AccountManager) context
                .getSystemService(Context.ACCOUNT_SERVICE);

        /*Account newAccount = new Account(context.getString(R.string.app_name)
                , context.getString(R.string.sync_account_type));*/

        Account availableAccounts[] = AccountManager.get(context)
                .getAccountsByType(AccountGeneral.ACCOUNT_TYPE);

        Account account = availableAccounts[0];

        if (null == accountManager.getPassword(availableAccounts[0])) {
            if (!accountManager.addAccountExplicitly(account, "", null)) {
                return null;
            }
            onAccountCreated(account, context);
        }

        return account;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        ReportSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL,
                SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount,
                context.getString(R.string.content_authority), true);

        //syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        ContentResolver.setIsSyncable(getSyncAccount(context), context.getString(R.string.content_authority), 1);
        ContentResolver.setSyncAutomatically(getSyncAccount(context),
                context.getString(R.string.content_authority), true);
    }
}
