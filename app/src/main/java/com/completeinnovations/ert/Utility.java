package com.completeinnovations.ert;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;

import com.completeinnovations.ert.data.ReportContract;
import com.completeinnovations.ert.data.ReportContract.ExpenseEntry;
import com.completeinnovations.ert.model.Status;
import com.completeinnovations.ert.restapi.ExpenseLineItem;
import com.completeinnovations.ert.restapi.ExpenseReport;

import java.text.NumberFormat;
import java.util.Locale;

public class Utility {

    private static final String IMPLICIT_HOST_BASE_URI = "http://ert" +
            ".completeinnovations.com";

    private static final String IMPLICIT_ACTION = "com.completeinnovations.ert" +
            ".SEND";

    /**
     * Find the status of a report.
     * @see com.completeinnovations.ert.model.Status
     * @param activity the activity associated with the a fragment
     * @param reportId the id of the report selected
     * @return A Status for this report id
     */
    public static Status getStatus(FragmentActivity activity, int reportId) {
        Status status = null;
        Intent intent = activity.getIntent();
        if(intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            Cursor cursor = activity.getContentResolver().query(
                    ReportContract.ReportEntry.buildReportUri(reportId),
                    null,
                    null,
                    null,
                    null
            );
            if (cursor.moveToFirst()) {
                status = Status.statusFactory(
                        Integer.parseInt(cursor.getString(cursor
                                .getColumnIndex(ReportContract
                                        .ReportEntry.COLUMN_STATUS)))
                );
            }
            cursor.close();
        }
        return status;
    }

    /**
     * Create an Expense Report from a cursor
     * @param c The cursor
     * @return an Expense Report
     */
    public static ExpenseReport createExpenseReportFromCursor(Cursor c) {
        //c.moveToFirst();
        ExpenseReport er = new ExpenseReport();

        er.setId(c.getInt(c.getColumnIndex(ReportContract.ReportEntry
                .COLUMN_REPORT_ID)));
        er.setName(c.getString(c.getColumnIndex(ReportContract.ReportEntry.COLUMN_NAME)));

        er.setSubmitterEmail(c.getString(c.getColumnIndex(ReportContract
                .ReportEntry
                .COLUMN_SUBMITTER_EMAIL)));

        er.setApproverEmail(c.getString(c.getColumnIndex(ReportContract
                .ReportEntry
                .COLUMN_APPROVER_EMAIL)));

        er.setComment(c.getString(c.getColumnIndex(ReportContract.ReportEntry
                .COLUMN_COMMENT)));

        er.setStatus(
                Status.statusFactory(
                        c.getInt(
                                c.getColumnIndex(ReportContract.ReportEntry
                                        .COLUMN_STATUS)
                        )
                ).getValue()
        );

        er.setStatusNote(c.getString(c.getColumnIndex(ReportContract.ReportEntry
                .COLUMN_STATUS_NOTE)));

        er.setText(c.getString(c.getColumnIndex(ReportContract.ReportEntry
                .COLUMN_TEXT)));

        er.setCreatedOn(c.getString(c.getColumnIndex(ReportContract.ReportEntry
                .COLUMN_CREATED_ON)));

        er.setSubmittedOn(c.getString(c.getColumnIndex(ReportContract
                .ReportEntry
                .COLUMN_SUBMITTED_ON)));

        er.setApprovedOn(c.getString(c.getColumnIndex(ReportContract.ReportEntry
                .COLUMN_APPROVED_ON)));


        er.setDeleted(stringToBoolean(c.getString(c.getColumnIndex
                (ReportContract.ReportEntry
                .COLUMN_IS_DELETED))));

        er.setFlagged(stringToBoolean(c.getString(c.getColumnIndex
                (ReportContract.ReportEntry.COLUMN_CREATED_ON))));

        return er;
    }

    public static ExpenseLineItem createExpenseLineItemFromCursor(Cursor c) {
        ExpenseLineItem eli = new ExpenseLineItem();
        //c.moveToFirst();


        eli.setExpenseDate(c.getString(c.getColumnIndex(ExpenseEntry
                .COLUMN_EXPENSE_DATE)));

        eli.setDescription(c.getString(c.getColumnIndex(ExpenseEntry
                .COLUMN_DESCRIPTION)));

        eli.setCategory(c.getString(c.getColumnIndex(ExpenseEntry
                .COLUMN_CATEGORY)));

        eli.setCost(c.getFloat(c.getColumnIndex(ExpenseEntry
                .COLUMN_COST)));

        eli.setTaxes(c.getFloat(c.getColumnIndex(ExpenseEntry.COLUMN_TAXES)));

        eli.setTaxType(c.getString(c.getColumnIndex(ExpenseEntry
                .COLUMN_TAX_TYPE)));

        eli.setHst(c.getFloat(c.getColumnIndex(ExpenseEntry.COLUMN_HST)));
        eli.setGst(c.getFloat(c.getColumnIndex(ExpenseEntry.COLUMN_GST)));
        eli.setQst(c.getFloat(c.getColumnIndex(ExpenseEntry.COLUMN_QST)));

        eli.setCurrency(c.getString(c.getColumnIndex(ExpenseEntry
                .COLUMN_CURRENCY)));

        eli.setRegion(c.getString(c.getColumnIndex(ExpenseEntry
                .COLUMN_REGION)));

        eli.setReceiptId(c.getString(c.getColumnIndex(ExpenseEntry
                .COLUMN_RECEIPT_ID)));

        eli.setCreatedOn(c.getString(c.getColumnIndex(
                ExpenseEntry.COLUMN_CREATED_ON)));

        eli.setDeleted(stringToBoolean(c.getString(
                c.getColumnIndex(ExpenseEntry.COLUMN_IS_DELETED))));

        eli.setExpenseReportId(c.getInt(c.getColumnIndex(
                ExpenseEntry.COLUMN_REPORT_ID)));
        eli.setId(c.getInt(c.getColumnIndex(ExpenseEntry.COLUMN_EXPENSE_ID)));

        return eli;
    }

    /**
     * Convert a string representation of a boolean to a
     * java boolean value
     * @param value The string representation
     * @return <code>true</code> if value is "true", <code>false</code>
     * if value is "false", otherwise false.
     */
    private static boolean stringToBoolean(String value) {
        if(value == null) {
            return false;
        }

        if (value.equalsIgnoreCase("true")) {
            return true;
        }

        if (value.equalsIgnoreCase("false")) {
            return false;
        }

        return false;
    }

    /**
     * Formats the date to display on the UI
     * @param date yyyy-MM-DDTHH:MM:SS
     * @return The formatted date yyyy-MM-DD
     */
    public static String dateFormatter(String date) {
        if(date != null) {
            return date.split("T")[0];
        } else return date;
    }

    /**
     * Builds an intent for an expense report
     * @param id The report base id of a report
     * @return an Intent
     */
    public static Intent buildExpenseReportIntent(int id) {
        Intent intent = new Intent();
        intent.setAction(IMPLICIT_ACTION);
        Uri uri = Uri.parse(IMPLICIT_HOST_BASE_URI + "/report/" + id + "/");
        intent.setData(uri);
        return intent;
    }

    /**
     * Builds an intent for an expense detail
     * @param id The expense detail base id
     * @return an Intent
     */
    public static Intent buildExpenseDetailsIntent(int id) {
        Intent intent = new Intent();
        intent.setAction(IMPLICIT_ACTION);
        Uri uri = Uri.parse(IMPLICIT_HOST_BASE_URI + "/expensedetails/" + id + "/");
        intent.setData(uri);
        return intent;
    }

    /**
     * Send simple notification using the NotificationCompat API.
     * @param context The originating activity.
     * @param reportBaseId The report base id from the db: ReportEntry._ID
     */
    public static void sendNotification(Context context,
                                        int reportBaseId) {

        Cursor cursor = context.getContentResolver().query(
                ReportContract.ReportEntry.buildReportUri(reportBaseId),
                null, null, null, null
        );

        if(cursor.getCount()>0) {
            cursor.moveToFirst();
        }

        String reportName = cursor.getString(cursor.getColumnIndex(ReportContract.ReportEntry.COLUMN_NAME));

        Status status = Status.statusFactory(cursor.getInt(cursor.getColumnIndex(ReportContract.ReportEntry.COLUMN_STATUS)));

        String statusString = status.getValue();
        String notificationTitle = reportName + " " + statusString;

        int statusInt = status.getNumber();

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Use NotificationCompat.Builder to set up our notification.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        //icon appears in device notification bar and right hand corner of notification
        builder.setSmallIcon(getStatusImageDrawable(String.valueOf(statusInt)));

        // This intent is fired when notification is clicked
        Intent intent = Utility.buildExpenseReportIntent(reportBaseId);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        // Set the intent that will fire when the user taps the notification.
        builder.setContentIntent(pendingIntent);

        // Large icon appears on the left of the notification
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                getStatusImageDrawable(String.valueOf(statusInt))));

        // Content title, which appears in large type at the top of the notification
        builder.setContentTitle(notificationTitle);

        // Content text, which appears in smaller text below the title
        // builder.setContentText(contentText);

        // The subtext, which appears under the text on newer devices.
        // This will show-up in the devices with Android 4.2 and above only
        // builder.setSubText("Tap to view documentation about notifications.");
        builder.setSound(sound);
        builder.setGroup("ert.completeinnovations.com.notificationgroup");
        builder.setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Will display the notification in the notification bar
        notificationManager.notify(reportBaseId, builder.build());
        cursor.close();
    }

    public static int getStatusImageDrawable(String imageStatusString) {
        switch (Status.statusFactory(Integer.parseInt(imageStatusString))) {
            case ACCEPTED: {
                return R.drawable.approved;
            }
            case PENDING: {
                return R.drawable.pending;
            }
            case SAVED: {
                return R.drawable.saved;
            }
            case PAID: {
                return R.drawable.paid;
            }
            case REJECTED: {
                return R.drawable.rejected;
            }

            case SUBMITTED: {
                return R.drawable.approval_request;
            }
            default:
                return 0;
        }
    }


    /**
     * Formats the currency into US
     * @param context
     * @param currencyAmount The currency amount
     */
    static public String formatCurrency(Context context, Double currencyAmount) {
        Locale currentLocale = context.getResources().getConfiguration().locale;
        NumberFormat currencyFormatter =
                NumberFormat.getCurrencyInstance(currentLocale);

        return currencyFormatter.format(currencyAmount);
    }

    /**
     * Determine the connectivity to the internet
     * @param context The context
     * @return <code>true</code> if the device has network connectivity, <code>false</code> otherwise
     */
    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }
}
