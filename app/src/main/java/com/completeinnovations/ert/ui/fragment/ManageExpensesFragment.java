package com.completeinnovations.ert.ui.fragment;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.completeinnovations.ert.R;
import com.completeinnovations.ert.Utility;
import com.completeinnovations.ert.data.ReportContract.ExpenseEntry;
import com.completeinnovations.ert.data.ReportContract.ReportEntry;
import com.completeinnovations.ert.data.SyncStatus;
import com.completeinnovations.ert.model.Report;
import com.completeinnovations.ert.model.Status;
import com.completeinnovations.ert.ui.activity.ExpenseDetailsActivity;
import com.completeinnovations.ert.ui.activity.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A placeholder fragment containing a simple view.
 */
public class ManageExpensesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, ReportRejectionFragment.StatusNoteDialogListener{

    private static final String LOG_TAG = ManageExpensesFragment.class.getSimpleName();


    private ExpenseAdapter mExpenseAdapter;

    private static final int EXPENSE_LOADER = 0;

    public static final String EXTRA_EXPENSE_CATEGORY = "extra_melf_expense_category";
    public static final String EXTRA_EXPENSE_DESCRIPTION = "extra_melf_expense_description";
    public static final String EXTRA_EXPENSE_TOTAL = "extra_melf_expense_total";
    public static final String EXTRA_EXPENSE_DATE = "extra_melf_expense_date";

    //Columns to be shown
    private static final String[] EXPENSE_COLUMNS = {
            ExpenseEntry.TABLE_NAME + "." + ExpenseEntry._ID,
            ExpenseEntry.TABLE_NAME + "." + ExpenseEntry.COLUMN_REPORT_ID,
            ExpenseEntry.TABLE_NAME + "." + ExpenseEntry.COLUMN_EXPENSE_DATE,
            ExpenseEntry.TABLE_NAME + "." + ExpenseEntry.COLUMN_DESCRIPTION,
            ExpenseEntry.TABLE_NAME + "." + ExpenseEntry.COLUMN_CATEGORY,
            ExpenseEntry.TABLE_NAME + "." + ExpenseEntry.COLUMN_COST,
            ExpenseEntry.TABLE_NAME + "." + ExpenseEntry.COLUMN_CREATED_ON,
            ExpenseEntry.TABLE_NAME + "." + ExpenseEntry.COLUMN_HST,
            ExpenseEntry.TABLE_NAME + "." + ExpenseEntry.COLUMN_GST,
            ExpenseEntry.TABLE_NAME + "." + ExpenseEntry.COLUMN_QST,
            ExpenseEntry.TABLE_NAME + "." + ExpenseEntry.COLUMN_IS_DELETED,
            ReportEntry.TABLE_NAME + "." + ReportEntry.COLUMN_SUBMITTED_ON,
            ReportEntry.TABLE_NAME + "." + ReportEntry.COLUMN_SUBMITTER_EMAIL,
    };
    //Indices of the above column from the selection
    public static final int COL_EXPENSE_ID = 0;
    public static final int COL_REPORT_ID = 1;
    public static final int COL_EXPENSE_DATE = 2;
    public static final int COL_EXPENSE_DESCRIPTION = 3;
    public static final int COL_EXPENSE_CATEGORY = 4;
    public static final int COL_EXPENSE_COST = 5;
    public static final int COL_EXPENSE_CREATED_ON = 6;
    public static final int COL_EXPENSE_HST = 7;
    public static final int COL_EXPENSE_GST = 8;
    public static final int COL_EXPENSE_QST = 9;
    public static final int COL_EXPENSE_DELETED = 10;
    public static final int COL_REPORT_SUBMITTED_ON = 11;
    public static final int COL_REPORT_SUBMITTER = 12;
    private View rootView;
    private ListView expenseListView;

    public ManageExpensesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        mExpenseAdapter = new ExpenseAdapter(getActivity(), null, 0);

        rootView = inflater.inflate(R.layout.fragment_manage_expenses, container, false);
        expenseListView = (ListView) rootView.findViewById(R.id.listview_expenses);
        expenseListView.setAdapter(mExpenseAdapter);

        expenseListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {

                        Cursor cursor = mExpenseAdapter.getCursor();
                        cursor.moveToPosition(position);

                        /*Intent intent = new Intent(getActivity(),
                                ExpenseDetailsActivity.class);

                        intent.putExtra(IntentKey.MANAGE_EXPENSE_ITEM,
                                cursor.getLong(COL_EXPENSE_ID));*/

                        startActivity(Utility.buildExpenseDetailsIntent(cursor.getInt(COL_EXPENSE_ID)));
                        cursor.close();
                    }
                }
        );

        expenseListView.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {

                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                        final Cursor CURSOR = mExpenseAdapter.getCursor();
                        CURSOR.moveToPosition(position);


                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

                        // set title
                        alertDialogBuilder.setTitle("Are you sure you want to delete this expense?");

                        // set dialog message
                        alertDialogBuilder
                                .setCancelable(true)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // if this button is clicked, delete
                                        // the expense


                                        Log.i(LOG_TAG, "Expense ID : " + CURSOR.getInt(COL_EXPENSE_ID));
                                        Uri expenseUri = ExpenseEntry.buildExpenseUri(CURSOR.getInt(COL_EXPENSE_ID));

                                        ContentValues contentValues = new ContentValues();
                                        contentValues.put(ExpenseEntry.COLUMN_IS_DELETED, "true");

                                        // prevent the pull from the server from overwriting the COLUMN_IS_DELETED value
                                        contentValues.put(ExpenseEntry.COLUMN_SYNC_STATUS, SyncStatus.REQUIRES_SYNC.toString());
                                        getActivity().getContentResolver().update(
                                                expenseUri,
                                                contentValues,
                                                null, null
                                        );

                                        Toast toast = Toast.makeText(getActivity(), "Expense " +
                                                "Deleted", Toast.LENGTH_SHORT);
                                        toast.show();
                                        //getActivity().finish();
                                        Log.d("Expense-delete", String.valueOf(CURSOR.getInt(COL_EXPENSE_ID)));

                                        //expenseListView.notifyDataSetChanged();

                                        // use this to reload the mExpenseAdapter
                                        // can't find another way to do that. Not sure if it's the
                                        // best way
                                        onResume();

                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // if this button is clicked, just close
                                        // the dialog box and do nothing
                                        dialog.cancel();
                                    }
                                });

                        // create alert dialog
                        AlertDialog alertDialog = alertDialogBuilder.create();

                        // show it
                        alertDialog.show();

                        return true;
                    }
                });

        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.manage_expenses_activity_action_new_expense_details: {
                Intent intent = new Intent(getActivity(),ExpenseDetailsActivity.class);

                final long reportBaseId = getActivity().getIntent().getLongExtra(Intent
                        .EXTRA_TEXT, 0);

                intent.putExtra(Intent.EXTRA_TEXT, reportBaseId);
                startActivity(intent);
                break;
            }

            case R.id.manage_expenses_activity_action_submit_report : {


                Cursor reportCursor = mExpenseAdapter.getCursor();
                if(reportCursor.getCount() > 0) {
                    reportCursor.moveToFirst();

                    // - (negative) in report id means temporary id stored on device only. not yet synced to server
                    if (reportCursor.getString(COL_REPORT_ID).contains("-")) {
                        Toast toast = Toast.makeText(getActivity(),
                                "Sync saved report first before submitting.", Toast.LENGTH_LONG);
                        toast.show();
                        break;
                    }
                }

                // don't submit unless there are expenses
                if(!(mExpenseAdapter.getCursor().getCount() > 0)) {
                    Toast toast = Toast.makeText(getActivity(),
                            "Cannot submit a report without any expense", Toast.LENGTH_LONG);
                    toast.show();
                } else {

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    // set title
                    alertDialogBuilder.setTitle("Are you sure you want to submit this report?");

                    alertDialogBuilder
                            .setCancelable(true)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    submitReport();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }

                break;

            }

            case R.id.manage_expenses_activity_action_accept : {
                final long reportBaseId = getActivity().getIntent().getLongExtra(Intent
                        .EXTRA_TEXT, 0);

                ContentValues contentValues = new ContentValues();
                contentValues.put(ReportEntry.COLUMN_SYNC_STATUS, SyncStatus.EDITED_REPORT.toString());
                contentValues.put(ReportEntry.COLUMN_STATUS, Status.ACCEPTED.getNumber());

                getActivity().getContentResolver().update(
                        ReportEntry.buildReportUri(reportBaseId),
                        contentValues,
                        null,
                        null
                );

                Intent intent = new Intent(getActivity(),MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                getActivity().finish();
                break;
            }

            case R.id.manage_expenses_activity_action_reject : {
                // open dialog fragment here and save status notes.

                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                ReportRejectionFragment reportRejectionFragment = ReportRejectionFragment.newInstance();
                reportRejectionFragment.show(fragmentManager,
                        "report_rejection_fragment");
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Submits the report
     */
    public void submitReport() {

        Date dateNow = new Date();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");

        final long reportBaseId = getActivity().getIntent().getLongExtra(Intent
                .EXTRA_TEXT, 0);
        ContentValues contentValues = new ContentValues();
        contentValues.put(ReportEntry.COLUMN_SYNC_STATUS, SyncStatus.REPORT_PENDING.toString());
        contentValues.put(ReportEntry.COLUMN_STATUS, Status.PENDING.getNumber());
        contentValues.put(ReportEntry.COLUMN_SUBMITTED_ON, simpleDateFormat.format(dateNow));

        getActivity().getContentResolver().update(
                ReportEntry.buildReportUri(reportBaseId),
                contentValues,
                null,
                null
        );

        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        getActivity().finish();

        String toastMessage;
        if (!Utility.isConnectedToInternet(getActivity())) {
            toastMessage = "Report will be submitted when network connectivity is established.";

        } else {
            toastMessage = "Report submitted";
        }

        Toast toast = Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_LONG);
        toast.show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_manage_expenses, menu);

        Intent intent = getActivity().getIntent();

        MenuItem menuItemAccept = menu.findItem(R.id
                .manage_expenses_activity_action_accept);
        MenuItem menuItemReject = menu.findItem(R.id
                .manage_expenses_activity_action_reject);
        MenuItem addExpenseItem = menu.findItem(R.id
                .manage_expenses_activity_action_new_expense_details);
        MenuItem submitReportItem = menu.findItem(R.id
                .manage_expenses_activity_action_submit_report);

        // set permission on view
        if(intent != null && intent.hasExtra(IntentKey.STATUS)) {
            if (intent.getStringExtra(IntentKey.STATUS).equalsIgnoreCase
                    (Status.SAVED.getValue()) || intent.getStringExtra
                    (IntentKey.STATUS).equalsIgnoreCase
                    (Status.REJECTED.getValue())) {
                menuItemAccept.setVisible(false);
                menuItemReject.setVisible(false);
                LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id
                        .fragment_manage_expenses_submitter_details);
                linearLayout.removeAllViewsInLayout();

                View view = rootView.findViewById(R.id.fragment_manage_expenses_submitter_details_horizontal_separator);
                view.setVisibility(View.GONE);


            } else if(intent.getStringExtra(IntentKey.STATUS).equalsIgnoreCase
                    (Status.SUBMITTED.getValue())) {
                addExpenseItem.setVisible(false);
                submitReportItem.setVisible(false);
                expenseListView.setLongClickable(false);
            } else {
                menuItemAccept.setVisible(false);
                menuItemReject.setVisible(false);
                addExpenseItem.setVisible(false);
                submitReportItem.setVisible(false);
                expenseListView.setLongClickable(false);
            }
        }

        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(EXPENSE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        final long reportBaseId = getActivity().getIntent().getLongExtra(Intent
                .EXTRA_TEXT, 0);

        return new CursorLoader(
                getActivity(),
                ReportEntry.buildReportExpense(reportBaseId),
                EXPENSE_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        mExpenseAdapter.swapCursor(data);
        refreshTotals();
        fillSubmitDetails();
    }

    private void fillSubmitDetails() {

        Cursor cursor = mExpenseAdapter.getCursor();

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            String submittedOn = cursor.getString(COL_REPORT_SUBMITTED_ON);
            String submitter = cursor.getString(COL_REPORT_SUBMITTER);

            TextView submittedOnTextView = (TextView) rootView.findViewById(R.id.fragment_manage_expenses_submitted_on);
            if(submittedOnTextView != null) {
                submittedOnTextView.setText("Submitted on: " + Utility.dateFormatter(submittedOn));
            }

            TextView submitterTextView = (TextView) rootView.findViewById(R.id.fragment_manage_expenses_submitter);

            if(submitterTextView != null) {
                submitterTextView.setText("Submitter: " + submitter);
            }
        }

    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        mExpenseAdapter.swapCursor(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        //restart the loader whenever the activity has resumed
        getLoaderManager().restartLoader(EXPENSE_LOADER, null, this);
    }


    public void refreshTotals() {
        Cursor cursor = mExpenseAdapter.getCursor();
        double total = 0;
        double gst = 0;
        double hst = 0;
        double qst = 0;

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                total += cursor.getDouble(COL_EXPENSE_COST) +
                        cursor.getDouble(COL_EXPENSE_HST) +
                        cursor.getDouble(COL_EXPENSE_GST) +
                        cursor.getDouble(COL_EXPENSE_QST);
                gst += cursor.getDouble(COL_EXPENSE_GST);
                hst += cursor.getDouble(COL_EXPENSE_HST);
                qst += cursor.getDouble(COL_EXPENSE_QST);
            } while(cursor.moveToNext());
        }

        TextView totalTextView = (TextView) rootView.findViewById(R.id.fragment_expenses_total);
        totalTextView.setText("Subtotal: " + Utility.formatCurrency(getActivity(), total)); //TODO: put in strings.xml

        TextView hstTotalTextView = (TextView) rootView.findViewById(R.id.fragment_expenses_hst_total);
        hstTotalTextView.setText("HST Total: " + Utility.formatCurrency(getActivity(), hst));

        TextView gstTotalTextView = (TextView) rootView.findViewById(R.id.fragment_expenses_gst_total);
        gstTotalTextView.setText("GST Total: " + Utility.formatCurrency(getActivity(), gst));

        TextView qstTotalTextView = (TextView) rootView.findViewById(R.id.fragment_expenses_qst_total);
        qstTotalTextView.setText("QST Total: " + Utility.formatCurrency(getActivity(), qst));
    }

    @Override
    public void onFinishEditDialog(String inputText) {
        final long reportBaseId = getActivity().getIntent().getLongExtra(Intent
                .EXTRA_TEXT, 0);
        ContentValues contentValues = new ContentValues();
        contentValues.put(ReportEntry.COLUMN_SYNC_STATUS, SyncStatus.EDITED_REPORT.toString());
        contentValues.put(ReportEntry.COLUMN_STATUS, Status.REJECTED.getNumber());
        contentValues.put(ReportEntry.COLUMN_STATUS_NOTE, inputText);

        getActivity().getContentResolver().update(
                ReportEntry.buildReportUri(reportBaseId),
                contentValues,
                null,
                null
        );

        Intent intent = new Intent(getActivity(),MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}
