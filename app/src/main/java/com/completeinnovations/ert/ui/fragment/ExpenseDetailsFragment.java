package com.completeinnovations.ert.ui.fragment;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.completeinnovations.ert.Constants;
import com.completeinnovations.ert.R;
import com.completeinnovations.ert.Utility;
import com.completeinnovations.ert.data.ReportContract.ExpenseEntry;
import com.completeinnovations.ert.data.ReportContract.ReportEntry;
import com.completeinnovations.ert.data.SyncStatus;
import com.completeinnovations.ert.model.Status;
import com.completeinnovations.ert.receipt.ERTReceipt;
import com.completeinnovations.ert.receipt.ERTReceiptPicker;
import com.completeinnovations.ert.restapi.ExpenseLineItem;
import com.completeinnovations.ert.restapi.ExpenseReport;

import java.io.IOException;

//import com.fourmob.datetimepicker.date.DatePickerDialog;

/**
 * A placeholder fragment containing a simple view.
 */

public class ExpenseDetailsFragment extends Fragment implements DatePickerDialog.OnDateSetListener {
    private static final String LOG_TAG = ExpenseDetailsFragment.class.getSimpleName();
    private View rootView;


    public static final String DATEPICKER_TAG = "datepicker";

    /**
     * Initialized after onCreate view has been called
     */
    private ViewHolder viewHolder;
    private int expensedetailsbaseid = 0;

    /**
     * For the receipt
     */
    private ERTReceiptPicker receiptPicker = null;
    private String receiptId = null;
    private static final int SELECT_PICTURE = 1;
    private boolean SAVE_EXPENSE_DETAILS = true;
    private int expenseErtId;
    private Menu menu;

    public ExpenseDetailsFragment(){
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        viewHolder.datePickerEditText.setText(year + "-" + (month + 1) + "-" + day);
    }


    /**
    *A holder of views for this fragment
    */
    public class ViewHolder {
        //public final DatePicker expenseDate = (DatePicker) rootView.findViewById(R.id.expense_details_datepicker_editText);
        public final TextView expenseDescription = (TextView) rootView.findViewById(R.id.expense_details_description_editText);
        public final Spinner expenseCategory = (Spinner) rootView.findViewById(R.id.expense_details_category_spinner);
        public final TextView expenseTotalCost = (TextView) rootView.findViewById(R.id.expense_details_cost_editText);
        public final TextView expenseHST = (TextView) rootView.findViewById(R.id.expense_details_hst_editText);
        public final TextView expenseGST = (TextView) rootView.findViewById(R.id.expense_details_gst_editText);
        public final TextView expenseQST = (TextView) rootView.findViewById(R.id.expense_details_qst_editText);
        public final Spinner expenseCurrency = (Spinner) rootView.findViewById(R.id.expense_details_currency_spinner);
        public final Spinner expenseRegion = (Spinner) rootView.findViewById(R.id.expense_details_region_spinner);
        public final EditText datePickerEditText = (EditText) rootView.findViewById(R.id.expense_details_datepicker_editText);
        public final Button receiptBtn= (Button) rootView.findViewById(R.id.expense_details_receipt_btn);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        SAVE_EXPENSE_DETAILS = true;
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {

        //int expensedetailsbaseid = 0;
        // Use implicit intent to retrieve the required reportBaseId
        // and put it in the Intent.EXTRA_TEXT so that other parts
        // of this fragment can use that to determine the reportBaseId
        Intent intent = getActivity().getIntent();
        if(intent.getData() != null) {
            Uri uri = intent.getData();
            expensedetailsbaseid = Integer.parseInt(uri.getLastPathSegment());
            intent.putExtra(IntentKey.MANAGE_EXPENSE_ITEM, new Long(expensedetailsbaseid));
        }

        rootView = inflater.inflate(R.layout.fragment_expense_details, container, false);

        viewHolder = new ViewHolder();

        configureSpinner(viewHolder.expenseCategory, R.array.category);
        configureSpinner(viewHolder.expenseCurrency, R.array.currency);
        configureSpinner(viewHolder.expenseRegion, R.array.region);

//        final Calendar calendar = Calendar.getInstance();
//        final DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity().getApplicationContext());
//
//
//        datePickerDialog.setStyle(DialogFragment., R.style.AppTheme);

        final DatePickerDialog.OnDateSetListener tmpHolder= this;

        viewHolder.datePickerEditText.setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog  = new DatePickerDialog(v.getContext(),
                        tmpHolder,
                        2019, 12, 05);
                //datePickerDialog.setYearRange(1985, 2028);
                //datePickerDialog.
                //datePickerDialog.setCloseOnSingleTapDay(false);
                datePickerDialog.show();//getFragmentManager(), DATEPICKER_TAG);
            }
        });

        viewHolder.receiptBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Button btn = (Button) v;
                if (receiptId == null || receiptId.isEmpty()) { //pick up a receipt
                    if (receiptPicker == null) {
                        receiptPicker = new ERTReceiptPicker();
                    }

                    Intent rp = receiptPicker.createReceiptPicker();
                    SAVE_EXPENSE_DETAILS = false;
                    startActivityForResult(rp, SELECT_PICTURE);

                } else { //display the receipt image
                    ERTReceipt.loadReceipt(expenseErtId, receiptId, new ERTReceipt.ReceiptReadyListener() {
                        @Override
                        public void onReceiptReady(Uri receiptUri) {
                            if (receiptUri != null) {
                                Intent viewReceipt = new Intent(Intent.ACTION_VIEW);
                                viewReceipt.setDataAndType(receiptUri, "image/*");
                                startActivity(viewReceipt);
                            } else {
                                Toast.makeText(getActivity(), "Unable to open" +
                                        " specified image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, getActivity());
                }
            }
        });

        viewHolder.receiptBtn.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                if (receiptPicker == null) {
                    receiptPicker = new ERTReceiptPicker();
                }

                Intent rp = receiptPicker.createReceiptPicker();
                SAVE_EXPENSE_DETAILS = false;
                startActivityForResult(rp, SELECT_PICTURE);

                return true;
            }
        });

        return rootView;
    }

    /**
     * Make all fields read only
     */
    private void setPermission() {
        if(!isExpenseNew()) {
            Status status = getReportStatus();
            if(!(status == Status.SAVED || status == Status.REJECTED)) {
                viewHolder.datePickerEditText.setEnabled(false);
                viewHolder.datePickerEditText.setFocusable(false);

                viewHolder.expenseDescription.setEnabled(false);
                viewHolder.expenseDescription.setFocusable(false);

                viewHolder.expenseCategory.setEnabled(false);
                viewHolder.expenseCategory.setFocusable(false);

                viewHolder.expenseTotalCost.setEnabled(false);
                viewHolder.expenseTotalCost.setFocusable(false);

                viewHolder.expenseHST.setEnabled(false);
                viewHolder.expenseHST.setFocusable(false);

                viewHolder.expenseGST.setEnabled(false);
                viewHolder.expenseGST.setFocusable(false);

                viewHolder.expenseQST.setEnabled(false);
                viewHolder.expenseQST.setFocusable(false);

                viewHolder.expenseCurrency.setEnabled(false);
                viewHolder.expenseCurrency.setFocusable(false);

                viewHolder.expenseRegion.setEnabled(false);
                viewHolder.expenseRegion.setFocusable(false);

                if(viewHolder.receiptBtn.getText().toString().equalsIgnoreCase(getString(R.string.uploadreceipt))) {
                    viewHolder.receiptBtn.setVisibility(View.GONE);
                }
                viewHolder.receiptBtn.setLongClickable(false);


                MenuItem menuItemSave = menu.findItem(R.id.expense_details_activity_action_save);
                menuItemSave.setVisible(false);

                MenuItem menuItemUndo = menu.findItem(R.id.expense_details_activity_undo);
                menuItemUndo.setVisible(false);

            }
        }
    }

    /**
     * Retrive the status of the report of this expense line item
     * @return a Status
     * @see com.completeinnovations.ert.model.Status
     */
    private Status getReportStatus() {
        long expenseBaseId = getActivity().getIntent().getLongExtra(
                IntentKey.MANAGE_EXPENSE_ITEM, 0);
        Cursor cursor = getActivity().getContentResolver().query(
                ExpenseEntry.buildExpenseUri(expenseBaseId),
                null,
                null,
                null,
                null
        );

        Status reportStatus = Status.SAVED;
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            long reportId = cursor.getLong(cursor.getColumnIndex(ExpenseEntry.COLUMN_REPORT_ID));

            Cursor reportCursor = getActivity().getContentResolver().query(
                    ReportEntry.CONTENT_URI,
                    null,
                    ReportEntry.COLUMN_REPORT_ID + "=" + reportId,
                    null, null
            );
            if(reportCursor.getCount() > 0) {
                reportCursor.moveToFirst();
                reportStatus = Status.statusFactory(reportCursor.getInt(reportCursor.getColumnIndex(ReportEntry.COLUMN_STATUS)));
            }
            reportCursor.close();
        }
        cursor.close();

        return reportStatus;
    }

    @Override
    public void onResume() {
        super.onResume();
        SAVE_EXPENSE_DETAILS = true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode != SELECT_PICTURE) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        else if(resultCode == android.app.Activity.RESULT_OK) {
            try {
                this.receiptId = receiptPicker.getSelectedReceipt(getActivity().getApplicationContext(), data);
                viewHolder.receiptBtn.setText(this.receiptId);
            } catch (IOException e1) {
                Log.d("RECEIPT", "Opening receipt failed" + e1.getMessage());
            }
        } else if (resultCode == android.app.Activity.RESULT_CANCELED) {
            // User cancelled the image capture
            if( receiptId == null || receiptId.isEmpty() ) {
                Toast.makeText(getActivity(), "You haven't chose the receipt", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Image capture failed, advise user
            Log.d("RECEIPT", String.format("Select receipt failed with code %d", resultCode));
            Toast.makeText(getActivity(), "Unable to open receipt image", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_expense_details, menu);

        if(!isExpenseNew()) {
            fillData();
        }

        this.menu = menu;
        setPermission();

        super.onCreateOptionsMenu(menu, inflater);
    }

/*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {

            case R.id.expense_details_activity_action_save: {
                if( validateExpenseDetails() ) {
                    getActivity().finish();
                }
                return true;
            }
            case R.id.expense_details_activity_undo: {
                ViewHolder viewHolder = new ViewHolder();

                viewHolder.expenseDescription.setText("");
                viewHolder.expenseTotalCost.setText("");
                viewHolder.expenseHST.setText("");
                viewHolder.expenseGST.setText("");
                viewHolder.expenseQST.setText("");
                return true;
            }

        }

        return super.onOptionsItemSelected(item);
    }

    private void fillData() {
        /*Intent intent = getActivity().getIntent();
        Long expenseId = intent.getLongExtra(IntentKey.MANAGE_EXPENSE_ITEM, 0);*/


        Cursor cursor = getActivity().getContentResolver().query(
                ExpenseEntry.buildExpenseUri(expensedetailsbaseid),
                null,
                null,
                null,
                null
        );

        cursor.moveToFirst();


        String date = cursor.getString(
                cursor.getColumnIndex(ExpenseEntry.COLUMN_EXPENSE_DATE));
        viewHolder.datePickerEditText.setText(Utility.dateFormatter(date));

        viewHolder.expenseDescription.setText(
                cursor.getString(cursor.getColumnIndex(ExpenseEntry
                        .COLUMN_DESCRIPTION))
        );

        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                getActivity(),
                R.array.category,
                android.R.layout.simple_spinner_item);

        viewHolder.expenseCategory.setSelection(categoryAdapter.getPosition(
                cursor.getString(cursor.getColumnIndex(ExpenseEntry.COLUMN_CATEGORY))
        ));

        viewHolder.expenseTotalCost.setText(cursor.getString(
                cursor.getColumnIndex(ExpenseEntry.COLUMN_COST)));

        viewHolder.expenseHST.setText(cursor.getString(
                cursor.getColumnIndex(ExpenseEntry.COLUMN_HST)));

        viewHolder.expenseGST.setText(cursor.getString(
                cursor.getColumnIndex(ExpenseEntry.COLUMN_GST)));

        viewHolder.expenseQST.setText(cursor.getString(
                cursor.getColumnIndex(ExpenseEntry.COLUMN_QST)));

        ArrayAdapter<CharSequence> currencyAdapter = ArrayAdapter.createFromResource(
                getActivity(),
                R.array.currency,
                android.R.layout.simple_spinner_item);

        viewHolder.expenseCurrency.setSelection(
                currencyAdapter.getPosition(cursor.getString(
                        cursor.getColumnIndex(ExpenseEntry.COLUMN_CURRENCY))));

        ArrayAdapter<CharSequence> regionAdapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.region,android.R.layout.simple_spinner_item);

        viewHolder.expenseRegion.setSelection(regionAdapter.getPosition(
                cursor.getString(cursor.getColumnIndex(
                        ExpenseEntry.COLUMN_REGION))));

        //cursor.close();

        receiptId = cursor.getString(cursor.getColumnIndex(ExpenseEntry.COLUMN_RECEIPT_ID));
        if( receiptId != null && !receiptId.isEmpty() ) {
            viewHolder.receiptBtn.setText(receiptId);
        }
        expenseErtId = cursor.getInt(cursor.getColumnIndex(ExpenseEntry.COLUMN_EXPENSE_ID));

        cursor.close();
    }

    private boolean isExpenseNew() {
        Intent intent = getActivity().getIntent();
        return !(intent != null && intent.hasExtra(IntentKey.MANAGE_EXPENSE_ITEM));
    }

    /**
     * Sets an array adapter to a spinner from a resource
     * @param spinner The spinner view
     * @param resourceId An array resource id
     */
    private void configureSpinner(Spinner spinner, int resourceId) {

        // Create an ArrayAdapter using the string array and a default
        // spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(),
                resourceId,
                android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
    }

    @Override
    public void onPause() {
        super.onPause();

        if(SAVE_EXPENSE_DETAILS && validateExpenseDetails() ) {

            ContentValues contentValues = new ContentValues();

            contentValues.put(ExpenseEntry.COLUMN_REPORT_ID,
                    getExpenseReportIdFromIntent());

            contentValues.put(ExpenseEntry.COLUMN_EXPENSE_DATE,
                    viewHolder.datePickerEditText.getText().toString());

            contentValues.put(
                    ExpenseEntry.COLUMN_DESCRIPTION,
                    viewHolder.expenseDescription.getText().toString());

            contentValues.put(ExpenseEntry.COLUMN_CATEGORY,
                    viewHolder.expenseCategory.getSelectedItem().toString());

            contentValues.put(ExpenseEntry.COLUMN_COST,
                    viewHolder.expenseTotalCost.getText().toString());

            contentValues.put(ExpenseEntry.COLUMN_HST,
                    viewHolder.expenseHST.getText().toString());

            contentValues.put(ExpenseEntry.COLUMN_GST,
                    viewHolder.expenseGST.getText().toString());

            contentValues.put(ExpenseEntry.COLUMN_QST,
                    viewHolder.expenseQST.getText().toString());

            contentValues.put(ExpenseEntry.COLUMN_CURRENCY,
                    viewHolder.expenseCurrency.getSelectedItem().toString());

            contentValues.put(ExpenseEntry.COLUMN_REGION,
                    viewHolder.expenseRegion.getSelectedItem().toString());

            contentValues.put(ExpenseEntry.COLUMN_RECEIPT_ID,
                    receiptId);


            if (isExpenseNew()) {
                contentValues.put(ExpenseEntry.COLUMN_SYNC_STATUS,
                        SyncStatus.SAVED_REPORT.toString()
                );
                contentValues.put(ExpenseEntry.COLUMN_EXPENSE_ID,
                        Constants.INTERNAL_EXPENSE_LINE_ITEM_ID);
                //save only if description is not empty
                if (viewHolder.expenseDescription.getText().toString().length() != 0) {

                    getActivity().getContentResolver().insert(
                            ExpenseEntry.CONTENT_URI, contentValues
                    );
                    getActivity().finish();
                }
            } else {
                contentValues.put(ExpenseEntry.COLUMN_SYNC_STATUS,
                        SyncStatus.EDITED_REPORT.toString()
                );
                getActivity().getContentResolver().update(
                        ExpenseEntry.buildExpenseUri(expensedetailsbaseid),
                        contentValues,
                        null,
                        null
                );
            }
        }

    }

    public long getExpenseReportIdFromIntent() {
        long reportBaseId;
        long expenseReportId = 0;
        long expenseBaseId = 0;


        if(isExpenseNew()) {
            reportBaseId = getActivity().getIntent().getLongExtra(
                    Intent.EXTRA_TEXT, 0);
            Cursor cursor = getActivity().getContentResolver().query(
                    ReportEntry.buildReportUri(reportBaseId),
                    null,
                    null,
                    null,
                    null
            );

            cursor.moveToFirst();

            ExpenseReport expenseReport = Utility.createExpenseReportFromCursor(cursor);
            expenseReportId = expenseReport.getId();
        } else {
            expenseBaseId = getActivity().getIntent().getLongExtra(
                    IntentKey.MANAGE_EXPENSE_ITEM, 0);
            Cursor cursor = getActivity().getContentResolver().query(
                    ExpenseEntry.buildExpenseUri(expenseBaseId),
                    null,
                    null,
                    null,
                    null
            );

            // if item has not been deleted
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                expenseReportId = cursor.getLong(cursor.getColumnIndex(ExpenseEntry.COLUMN_REPORT_ID));

            }
            cursor.close();
        }

        return expenseReportId;
    }



    private long backPressCnt = 0;

    /**
     *
     */
    public boolean onBackPressed() {


        if( validateExpenseDetails() ) {
            return true;
        }
        else { //errors exist
            long backPressNow = System.currentTimeMillis();

            if( (backPressCnt == 0 ) || (1500 < (backPressNow - backPressCnt)) ) { //Back pressed 1st time
                Toast.makeText(getActivity(), "Press Back again to abort edit", Toast.LENGTH_SHORT).show();
                backPressCnt = backPressNow;
                return false; //Back canceled
            }
            else { //press Back twice within 1.5S to abort the report and quit
                Toast.makeText(getActivity(), "Edit aborted", Toast.LENGTH_SHORT).show();
                return true;    //Back confirmed
            }
        }
    }

    private boolean validateExpenseDetails() {

        boolean isValid = true;

        String tmpStr = this.viewHolder.datePickerEditText.getText().toString();

        if( tmpStr == null || tmpStr.isEmpty() ) {
            this.viewHolder.datePickerEditText.setError("Invalid Date");
            isValid = false;
        }

        tmpStr = this.viewHolder.expenseDescription.getText().toString();
        if( tmpStr == null || tmpStr.isEmpty() ) {
            this.viewHolder.expenseDescription.setError("Invalid Description");
            isValid = false;
        }

        tmpStr = this.viewHolder.expenseTotalCost.getText().toString();
        if( tmpStr == null || tmpStr.isEmpty() ) {
            this.viewHolder.expenseTotalCost.setError("Please input Cost ");
            isValid = false;
        }

        tmpStr = this.viewHolder.expenseHST.getText().toString();
        if( tmpStr == null || tmpStr.isEmpty() ) {
            this.viewHolder.expenseHST.setError("Please input HST");
            isValid = false;
        }

        tmpStr = this.viewHolder.expenseGST.getText().toString();
        if( tmpStr == null || tmpStr.isEmpty() ) {
            this.viewHolder.expenseGST.setError("Please input GST");
            isValid = false;
        }

        tmpStr = this.viewHolder.expenseQST.getText().toString();
        if( tmpStr == null || tmpStr.isEmpty() ) {
            this.viewHolder.expenseQST.setError("Please input QST");
            isValid = false;
        }

        return isValid;
    }

    //Model
    private ExpenseLineItem exItem;
    private void bindModel()  throws Exception {
        if( null == exItem ) {
            exItem = new ExpenseLineItem();
        }

        exItem.setExpenseDate( this.viewHolder.datePickerEditText.getText().toString() );
        exItem.setDescription(this.viewHolder.expenseDescription.getText().toString());
        exItem.setCategory( this.viewHolder.expenseCategory.getSelectedItem().toString() );
        exItem.setCost( Float.parseFloat( this.viewHolder.expenseTotalCost.getText().toString() ) );
        exItem.setHst( Float.parseFloat( this.viewHolder.expenseHST.getText().toString()));
        exItem.setGst( Float.parseFloat( this.viewHolder.expenseGST.getText().toString()));
        exItem.setQst( Float.parseFloat( this.viewHolder.expenseQST.getText().toString()));
        exItem.setCurrency( this.viewHolder.expenseCurrency.getSelectedItem().toString() );
        exItem.setRegion( this.viewHolder.expenseRegion.getSelectedItem().toString() );
    }
}
