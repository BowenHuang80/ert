package com.completeinnovations.ert.ui.fragment;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.completeinnovations.ert.R;
import com.completeinnovations.ert.Utility;
import com.completeinnovations.ert.data.ReportContract;
import com.completeinnovations.ert.data.ReportContract.ReportEntry;
import com.completeinnovations.ert.data.SyncStatus;
import com.completeinnovations.ert.model.Status;
import com.completeinnovations.ert.sync.ReportSyncAdapter;
import com.completeinnovations.ert.ui.activity.ManageExpensesActivity;

/**
 * A placeholder fragment containing a simple view.
 */
public class ExpenseReportFragment extends Fragment implements LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = Fragment.class.getSimpleName();
    private View rootView;

    private static final int EMPLOYEE_LOADER = 0;
    private static final int REPORT_DETAILS_LOADER = 1;

    Button btnSave;
    private SimpleCursorAdapter mEmployeeAdapter;

    private String selectedManagerEmail;

    /**
     * The status of the item selected
     */
    private Status status;
    private CursorAdapter mReportDetailsAdapter;

    public ExpenseReportFragment() {
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader = null;
        switch (id) {
            case EMPLOYEE_LOADER: {
                cursorLoader = new CursorLoader(
                        getActivity(),
                        ReportContract.EmployeeEntry.CONTENT_URI,
                        null,
                        null,
                        null,
                        null
                );
                break;
            }

            case REPORT_DETAILS_LOADER: {
                long reportId = new Long(getActivity().getIntent().getIntExtra(Intent
                        .EXTRA_TEXT, 0));
                cursorLoader = new CursorLoader(
                        getActivity(),
                        ReportEntry.buildReportUri(reportId),
                        ReportFragment.REPORT_COLUMNS, null, null, null
                );
            }
            break;
            default:
                throw new UnsupportedOperationException("Unsupported loader: " + cursorLoader.getId());

        }
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case EMPLOYEE_LOADER: {
                mEmployeeAdapter.swapCursor(data);

                if (!isReportNew()) {
                    setManagerEmailSpinnerPosition();
                }
                break;
            }

            case REPORT_DETAILS_LOADER: {
                mReportDetailsAdapter.swapCursor(data);

                if (mReportDetailsAdapter.getCount() > 0) {
                    mReportDetailsAdapter.getCursor().moveToFirst();
                    ReportDetailsAdapter.ViewHolder reportDetailsAdapterViewHolder = new ReportDetailsAdapter.ViewHolder(rootView);
                    reportDetailsAdapterViewHolder.reportNameView.setText(
                            mReportDetailsAdapter.getCursor().getString(ReportFragment.COL_REPORT_NAME));

                    reportDetailsAdapterViewHolder.submitterView.setText(
                            mReportDetailsAdapter.getCursor().getString(ReportFragment.COL_REPORT_SUBMITTER_EMAIL));

                    reportDetailsAdapterViewHolder.commentView.setText(
                            mReportDetailsAdapter.getCursor().getString
                                    (ReportFragment.COL_REPORT_COMMENT));

                    reportDetailsAdapterViewHolder.statusNotesTextView
                            .setText(mReportDetailsAdapter.getCursor()
                                    .getString(ReportFragment
                                            .COL_REPORT_STATUS_NOTES));

                    selectedManagerEmail = mReportDetailsAdapter.getCursor().getString(
                            ReportFragment.COL_REPORT_APPROVER_EMAIL);


                    // this should be put here because setManagerEmailSpinnerPosition depends
                    // on the completion of REPORT_DETAILS_LOADER
                    getLoaderManager().initLoader(EMPLOYEE_LOADER, null, this);
                }

                break;
            }

            default:
                throw new UnsupportedOperationException("Unsupported loader: " + loader.getId());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case EMPLOYEE_LOADER: {
                mEmployeeAdapter.swapCursor(null);
                break;
            }

            case REPORT_DETAILS_LOADER: {
                mReportDetailsAdapter.swapCursor(null);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unsupported loader: " + loader.getId());
        }
    }

    /**
     * A holder of views for this fragment
     */
    public class ViewHolder {
        public final TextView reportNameTextView = (TextView) rootView.findViewById(R.id
                .fragment_expense_report_name_edittext);

        public final TextView submitterEmailTextView = ((TextView) rootView
                .findViewById(R.id
                        .fragment_expense_report_your_email_edittext));

        public final Spinner managerSpinner = (Spinner) rootView.findViewById(
                R.id.fragment_expense_report_manager_email_spinner
        );

        public final TextView commentTextView = ((TextView) rootView.findViewById(R.id
                .fragment_expense_report_comment_edittext));

        public final TextView statusNotes = ((TextView) rootView.findViewById
                (R.id.fragment_expense_report_status_notes_edittext));


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(EMPLOYEE_LOADER, null, this);
        if (!isReportNew()) {
            getLoaderManager().initLoader(REPORT_DETAILS_LOADER, null, this);
        }

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


//    //On pressing save button in Expene report activity
//    public void goToReportList(View v){
//        if(v.getId() == R.id.btnSave){
//            getActivity().finish();
//        }
//    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Use implicit intent to retrieve the required reportBaseId
        // and put it in the Intent.EXTRA_TEXT so that other parts
        // of this fragment can use that to determine the reportBaseId
        Intent intent = getActivity().getIntent();
        if(intent.getData() != null) {
            Uri uri = intent.getData();
            int reportBaseId = Integer.parseInt(uri.getLastPathSegment());
            intent.putExtra(Intent.EXTRA_TEXT, reportBaseId);
        }

        mEmployeeAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.support_simple_spinner_dropdown_item,
                null,
                new String[] {ReportContract.EmployeeEntry.COLUMN_VALUE},
                new int[] {android.R.id.text1},
                0
        );

        rootView = inflater.inflate(R.layout.fragment_expense_report, container, false);

//        Button saveButton = (Button) rootView.findViewById(R.id.fragment_expense_report_save_button);
//
//        saveButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                getActivity().finish();
//            }
//        });

        mReportDetailsAdapter = new ReportDetailsAdapter(getActivity(), null, 0);

        Spinner employeeSpinner = (Spinner) rootView.findViewById(R.id.fragment_expense_report_manager_email_spinner);
        employeeSpinner.setAdapter(mEmployeeAdapter);

        employeeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {


            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                selectedManagerEmail = cursor.getString(cursor.getColumnIndex(ReportContract.EmployeeEntry.COLUMN_VALUE));
                //selectedManagerEmail = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return rootView;
    }

    public void setManagerEmailSpinnerPosition() {
        /*String approverEmail = getActivity().getIntent().getStringExtra(ReportFragment.EXTRA_APPROVER_EMAIL);

        selectedManagerEmail = approverEmail;*/

        int employeeCursorPosition = 0;

        Cursor employeeCursor = mEmployeeAdapter.getCursor();
        if(employeeCursor.getCount() > 0) {
            employeeCursor.moveToFirst();

            do {
                if (employeeCursor.getString(employeeCursor.getColumnIndex(ReportContract.EmployeeEntry.COLUMN_VALUE)).equalsIgnoreCase(selectedManagerEmail)) {

                    employeeCursorPosition = employeeCursor
                            .getPosition();
                    break;
                }
            } while (employeeCursor.moveToNext());
        }

        new ViewHolder().managerSpinner.setSelection(employeeCursorPosition);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.expense_report_activity_action_manage_expenses: {

                if( !validateReport() ) {
                    return true;
                }


                FragmentActivity activity = getActivity();

                /*if(activity.getIntent().getIntExtra(Intent.EXTRA_TEXT, 0) == null) {

                }*/

                Status reportStatus = Utility.getStatus(activity,
                        activity.getIntent().getIntExtra(Intent
                                .EXTRA_TEXT, 0));
                // put a default status of saved.
                String status;
                // status has not been set because it is
                // a freshly created report, so assume
                // saved status
                if(reportStatus != null) {
                    status = reportStatus.getValue();
                } else {
                    status = Status.SAVED.getValue();
                }


                final long reportBaseId = saveOrUpdateReport();

                if(reportBaseId != 0) {

                    Log.d("ertactivity", String.valueOf(reportBaseId));

                    Intent intent = new Intent(activity,
                            ManageExpensesActivity.class);

                    intent.putExtra(IntentKey.STATUS, status);
                    intent.putExtra(Intent.EXTRA_TEXT, reportBaseId);

                    startActivity(intent);
                }
                break;
            }
            case R.id.expense_details_activity_action_save: {
                if( validateReport() ) {
                    getActivity().finish();
                }
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_expense_report, menu);

        //check if intent has data
        if(!isReportNew()) {
            //fillData();
            setPermission(menu);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }


    /**
     * Fill the data in the Expense Report screen using
     * extras found in the intent from the ReportFragment
     * TODO: May be read from the content provider instead of using
     * TODO: so many data from the intent extra. Use the id to
     * TODO: get a cursor from the provider and from this get all the
     * TODO: required data
     */
    private void fillData() {
        Intent intent = getActivity().getIntent();

        ViewHolder viewHolder = new ViewHolder();

        //set report name
        String reportName = intent.getStringExtra(ReportFragment
                .EXTRA_REPORT_NAME);
        viewHolder.reportNameTextView.setText(reportName);

        //set submitter email
        String submitterEmail = intent.getStringExtra(ReportFragment.EXTRA_SUBMITTER_EMAIL);
        viewHolder.submitterEmailTextView.setText(submitterEmail);

        //set comment
        String comment = intent.getStringExtra(ReportFragment.EXTRA_COMMENT);
        viewHolder.commentTextView.setText(comment);

    }

    @Override
    public void onPause() {
        super.onPause();
        //TODO: update date using content provider here
        if(validateReport()) {
            saveOrUpdateReport();
        }

    }

    private long saveOrUpdateReport() {
        Log.d("ertactivity", "onPause");

        ViewHolder viewHolder = new ViewHolder();

        ContentValues contentValues = new ContentValues();

        contentValues.put(ReportEntry.COLUMN_NAME,
                viewHolder.reportNameTextView.getText().toString());


        contentValues.put(ReportEntry.COLUMN_APPROVER_EMAIL, selectedManagerEmail);

        contentValues.put(ReportEntry.COLUMN_COMMENT,
                viewHolder.commentTextView.getText().toString());



        /*contentValues.put(ReportEntry.COLUMN_SYNC_STATUS,
                SyncStatus.REQUIRES_SYNC.toString());*/

        // update report if report exists
        // else create a new report
        if (!isReportNew()) {

            final int reportBaseId = getActivity().getIntent().getIntExtra
                    (Intent
                    .EXTRA_TEXT, 0);

            // don't update status to edited if the status is on delete
            if (!ReportSyncAdapter.getReportSyncStatus(
                    getActivity(), new Long(reportBaseId)).equalsIgnoreCase
                    (SyncStatus.DELETED_REPORT.toString())) {
                contentValues.put(ReportEntry.COLUMN_SYNC_STATUS,
                        SyncStatus.EDITED_REPORT.toString());
            }

            Log.d(LOG_TAG, "SyncStatus is: " + ReportSyncAdapter
                    .getReportSyncStatus(getActivity(), new Long(reportBaseId)));


            int updatedRows = getActivity().getContentResolver().update(
                    ReportEntry.buildReportUri(reportBaseId),
                    contentValues,
                    null,
                    null
            );

            Log.d(LOG_TAG, "SyncStatus after ui update is: " + ReportSyncAdapter
                    .getReportSyncStatus(getActivity(), new Long(reportBaseId)));

            return reportBaseId;

        } else {
            //TODO: create new report
            Uri reportUri = null;
            contentValues.put(ReportEntry.COLUMN_SYNC_STATUS,
                    SyncStatus.SAVED_REPORT.toString());
            contentValues.put(ReportEntry.COLUMN_REPORT_ID, generateTemporaryReportId());
            if(contentValues.getAsString(ReportEntry.COLUMN_NAME).length() != 0) {
                reportUri = getActivity().getContentResolver().insert(
                        ReportEntry.CONTENT_URI, contentValues
                );
                Intent intent = getActivity().getIntent();
                intent.putExtra(Intent.EXTRA_TEXT, generateTemporaryReportId());

                return ContentUris.parseId(reportUri);
            } else {
                return 0;
            }
        }
    }

    private int getReportCount() {
        Cursor cursor = getActivity().getContentResolver().query(
                ReportEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    /**
     * The temporary report id has a negative value to indicate that
     * it is saved locally only.
     * @return
     */
    private int generateTemporaryReportId() {
        if(getReportCount() == 0) {
            return -1;
        } else {
            return (getReportCount()+1) * -1;
        }
    }

    /**
     * Checks if the report is a newly created one or if it exists already.
     * @return <code>true</code> if new report, <code>false</code> otherwise.
     */
    private boolean isReportNew() {
        Intent intent = getActivity().getIntent();
        return !(intent != null && intent.hasExtra(Intent.EXTRA_TEXT));
    }

    /**
     * Sets the permission of data in this fragment.
     * <ul>Will make the following changes if the report
     * does not have a saved status
     * <li>Make the delete icon invisible.</li>
     * <li>Set all text fields and spinners to read only</li>
     * </ul>
     * @param menu The menu from which to manipulate icons
     */
    private void setPermission(Menu menu) {
        Intent intent = getActivity().getIntent();
        int reportId = intent.getIntExtra(Intent.EXTRA_TEXT, 0);

        status = Utility.getStatus(getActivity(), reportId);
        if(!(status == Status.SAVED || status == Status.REJECTED)) {
            ViewHolder viewHolder = new ViewHolder();

            viewHolder.reportNameTextView.setEnabled(false);
            viewHolder.reportNameTextView.setFocusable(false);
            viewHolder.commentTextView.setEnabled(false);
            viewHolder.commentTextView.setFocusable(false);
            viewHolder.managerSpinner.setEnabled(false);
            viewHolder.statusNotes.setEnabled(false);


            MenuItem menuItemSave = menu.findItem(R.id
                    .expense_details_activity_action_save);
            menuItemSave.setVisible(false);
        }
    }


    //validation
    private ReportDetailsAdapter.ViewHolder vh ;

    private boolean validateReport() {
        if( vh == null ) {
            vh = new ReportDetailsAdapter.ViewHolder(rootView);
        }

        boolean result = true;
        String tmpStr;
        tmpStr = vh.reportNameView.getText().toString().trim();
        if( tmpStr == null || tmpStr.isEmpty() ) {
            vh.reportNameView.setError("Report name is required");
            result = false;
        }

        return result;
    }
}
