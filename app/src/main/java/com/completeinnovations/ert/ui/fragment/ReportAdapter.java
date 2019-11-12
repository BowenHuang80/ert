package com.completeinnovations.ert.ui.fragment;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.completeinnovations.ert.R;
import com.completeinnovations.ert.Utility;
import com.completeinnovations.ert.data.ReportContract;

/**
 * Custom Cursor Adapter for report item.
 * @author Abhinav
 */
public class ReportAdapter extends CursorAdapter{

    public static class ViewHolder {
        public final TextView reportNameView;
        public final TextView submitterView;
        public final TextView dateCreatedView;
        public final ImageView statusIconView;
        public final TextView totalExpense;

        public ViewHolder(View view) {
            reportNameView = (TextView) view.findViewById(R.id.list_item_report_textview);
            submitterView = (TextView) view.findViewById(R.id.list_item_report_submitter_textview);
            dateCreatedView = (TextView) view.findViewById(R.id.list_item_report_datecreated_textview);
            statusIconView = (ImageView) view.findViewById(R.id.list_item_report_status_icon);
            totalExpense = (TextView) view.findViewById(R.id.list_item_report_totalexpense_textview);
        }
    }

    public ReportAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_report, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String reportName = cursor.getString(ReportFragment.COL_REPORT_NAME);
        String submitter = cursor.getString(ReportFragment.COL_REPORT_SUBMITTER_EMAIL);
        String dateCreated = cursor.getString(ReportFragment.COL_REPORT_CREATED_ON);
        String imageIcon = cursor.getString(ReportFragment.COL_REPORT_STATUS);


        String dateFormatted = "";
        if(dateCreated != null) {
            dateFormatted = Utility.dateFormatter(dateCreated);
        }

        viewHolder.reportNameView.setText(reportName);
        viewHolder.submitterView.setText(submitter);
        viewHolder.dateCreatedView.setText(dateFormatted);
        viewHolder.statusIconView.setImageResource(Utility.getStatusImageDrawable
                (imageIcon));
        viewHolder.totalExpense.setText(getTotalExpense(context, cursor));
    }


    private String getTotalExpense(Context context, Cursor cursor) {
        double total = 0;
        long id = cursor.getLong(ReportFragment.COL_BASE_REPORT_ID);

        Cursor reportCursor = context.getContentResolver().query(
                ReportContract.ReportEntry.buildReportExpense(id),
                null,null,null,null
        );

        if(reportCursor.getCount() > 0) {
            reportCursor.moveToFirst();
            do {
                total +=
                        reportCursor.getDouble(reportCursor.getColumnIndex(ReportContract.ExpenseEntry.COLUMN_COST)) +
                        reportCursor.getDouble(reportCursor.getColumnIndex(ReportContract.ExpenseEntry.COLUMN_HST)) +
                        reportCursor.getDouble(reportCursor.getColumnIndex(ReportContract.ExpenseEntry.COLUMN_GST)) +
                        reportCursor.getDouble(reportCursor.getColumnIndex(ReportContract.ExpenseEntry.COLUMN_QST));
            } while (reportCursor.moveToNext());
        }

        return Utility.formatCurrency(context, total);
    }


}
