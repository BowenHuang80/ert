package com.completeinnovations.ert.restapi;

import android.content.ContentValues;

import com.completeinnovations.ert.data.ReportContract.ExpenseEntry;

import java.util.ArrayList;
import java.util.List;

public class ExpenseLineItem {
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getExpenseDate() {
		return expenseDate;
	}
	public void setExpenseDate(String expenseDate) {
		this.expenseDate = expenseDate;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public float getCost() {
		return cost;
	}
	public void setCost(float cost) {
		this.cost = cost;
	}
	public float getTaxes() {
		return taxes;
	}
	public void setTaxes(float taxes) {
		this.taxes = taxes;
	}
	public String getTaxType() {
		return taxType;
	}
	public void setTaxType(String taxType) {
		this.taxType = taxType;
	}
	public float getHst() {
		return hst;
	}
	public void setHst(float hST) {
		hst = hST;
	}
	public float getGst() {
		return gst;
	}
	public void setGst(float gST) {
		gst = gST;
	}
	public float getQst() {
		return qst;
	}
	public void setQst(float qST) {
		qst = qST;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public String getReceiptId() {
		return receiptId;
	}
	public void setReceiptId(String receiptId) {
		this.receiptId = receiptId;
	}
	public String getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}
	public boolean isDeleted() {
		return isDeleted;
	}
	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
	public int getExpenseReportId() {
		return expenseReportId;
	}
	public void setExpenseReportId(int expenseReportId) {
		this.expenseReportId = expenseReportId;
	}
	int id;
	String expenseDate;
	String description;
	String category;
	float cost;
	float taxes;
	String taxType;
	float hst;
	float gst;
	float qst;
	String currency;
	String region;
	String receiptId;
	String createdOn;
	boolean isDeleted;
	int expenseReportId;

    public static ContentValues[] getExpenseLineItemContentValues(
            List<ExpenseLineItem>expenseLineItemList) {

        List<ContentValues> contentValuesList = new ArrayList<ContentValues>();

        for(ExpenseLineItem e : expenseLineItemList) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ExpenseEntry.COLUMN_EXPENSE_ID, e.getId());

            if(e.getExpenseDate() != null)
            contentValues.put(ExpenseEntry.COLUMN_EXPENSE_DATE, e.getExpenseDate());

            if(e.getDescription() != null)
            contentValues.put(ExpenseEntry.COLUMN_DESCRIPTION, e.getDescription());

            if(e.getCategory() != null)
            contentValues.put(ExpenseEntry.COLUMN_CATEGORY, e.getCategory());

            contentValues.put(ExpenseEntry.COLUMN_COST, e.getCost());
            contentValues.put(ExpenseEntry.COLUMN_TAXES, e.getTaxes());

            if(e.getTaxType() != null)
            contentValues.put(ExpenseEntry.COLUMN_TAX_TYPE, e.getTaxType());

            contentValues.put(ExpenseEntry.COLUMN_HST, e.getHst());
            contentValues.put(ExpenseEntry.COLUMN_GST, e.getGst());
            contentValues.put(ExpenseEntry.COLUMN_QST, e.getQst());


            if(e.getCurrency() != null)
            contentValues.put(ExpenseEntry.COLUMN_CURRENCY, e.getCurrency());


            if(e.getReceiptId() != null)
            contentValues.put(ExpenseEntry.COLUMN_REGION, e.getRegion());

            if(e.getReceiptId() != null)
            contentValues.put(ExpenseEntry.COLUMN_RECEIPT_ID, e.getReceiptId());

            if(e.getCreatedOn() != null)
            contentValues.put(ExpenseEntry.COLUMN_CREATED_ON, e.getCreatedOn());

            contentValues.put(ExpenseEntry.COLUMN_IS_DELETED, e.isDeleted());
            contentValues.put(ExpenseEntry.COLUMN_REPORT_ID, e.getExpenseReportId());

            contentValuesList.add(contentValues);
        }

        ContentValues[] contentValuesArray = new
                ContentValues[contentValuesList.size()];
        return contentValuesList.toArray(contentValuesArray);
    }
}
