package com.completeinnovations.ert.ui.fragment;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.completeinnovations.ert.R;

/**
 * Custom cursor adapter for a report detail (Expense Report)
 * @author Abhinav
 */
public class ReportDetailsAdapter extends CursorAdapter{

    public static class ViewHolder {
        public final TextView reportNameView;
        public final TextView submitterView;
        public final TextView commentView;
        public final TextView statusNotesTextView;

        public ViewHolder(View view) {
            reportNameView = (TextView) view.findViewById(R.id.fragment_expense_report_name_edittext);
            submitterView = (TextView) view.findViewById(R.id.fragment_expense_report_your_email_edittext);
            commentView = (TextView) view.findViewById(R.id.fragment_expense_report_comment_edittext);
            statusNotesTextView = ((TextView) view.findViewById(R.id.fragment_expense_report_status_notes_edittext));
        }
    }

    public ReportDetailsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_expense_report, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String reportName = cursor.getString(ReportFragment.COL_REPORT_NAME);
        String submitter = cursor.getString(ReportFragment.COL_REPORT_SUBMITTER_EMAIL);
        String comment = cursor.getString(ReportFragment.COL_REPORT_COMMENT);
        String statusNote = cursor.getString(ReportFragment.COL_REPORT_STATUS_NOTES);

        viewHolder.reportNameView.setText(reportName);
        viewHolder.submitterView.setText(submitter);
        viewHolder.commentView.setText(comment);
        viewHolder.statusNotesTextView.setText(statusNote);
    }
}
