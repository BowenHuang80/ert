package com.completeinnovations.ert.ui.fragment;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.completeinnovations.ert.R;
import com.completeinnovations.ert.Utility;

/**
 * Custom Cursor Adapter for expense item.
 * @author Abhinav
 */
public class ExpenseAdapter extends CursorAdapter {

    public static class ViewHolder {
        public final TextView categoryView;
        public final TextView descriptionView;
        public final TextView totalView;
        public final TextView dateView;

        public ViewHolder(View view) {
            categoryView = (TextView) view.findViewById(R.id.list_item_expense_category);
            descriptionView = (TextView) view.findViewById(R.id.list_item_expense_description);
            totalView = (TextView) view.findViewById(R.id.list_item_expense_total);
            dateView = (TextView) view.findViewById(R.id.list_item_expense_date);
        }
    }

    public ExpenseAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_expense, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String category = cursor.getString(ManageExpensesFragment.COL_EXPENSE_CATEGORY);
        String description = cursor.getString(ManageExpensesFragment.COL_EXPENSE_DESCRIPTION);

        double total =
                cursor.getDouble(ManageExpensesFragment.COL_EXPENSE_COST) +
                        cursor.getDouble(ManageExpensesFragment.COL_EXPENSE_HST) +
                        cursor.getDouble(ManageExpensesFragment.COL_EXPENSE_GST) +
                        cursor.getDouble(ManageExpensesFragment.COL_EXPENSE_QST);

        String date = cursor.getString(ManageExpensesFragment.COL_EXPENSE_DATE);

        viewHolder.categoryView.setText(category);
        viewHolder.descriptionView.setText(description);
        viewHolder.totalView.setText(Utility.formatCurrency(context, total));
        viewHolder.dateView.setText(Utility.dateFormatter(date));
    }
}
