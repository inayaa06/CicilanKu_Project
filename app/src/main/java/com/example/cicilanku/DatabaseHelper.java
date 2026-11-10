package com.example.cicilanku;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "cicilanku.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE loans (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "principal REAL NOT NULL," +
                "interest REAL NOT NULL," +
                "tenor INTEGER NOT NULL," +
                "created_at TEXT DEFAULT CURRENT_TIMESTAMP)");
        db.execSQL("CREATE TABLE installments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "loan_id INTEGER NOT NULL," +
                "amount REAL NOT NULL," +
                "due_date TEXT NOT NULL," +
                "paid INTEGER DEFAULT 0," +
                "FOREIGN KEY(loan_id) REFERENCES loans(id))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS installments");
        db.execSQL("DROP TABLE IF EXISTS loans");
        onCreate(db);
    }

    public long addLoan(String name, double principal, double interest, int tenor) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues loan = new ContentValues();
        loan.put("name", name);
        loan.put("principal", principal);
        loan.put("interest", interest);
        loan.put("tenor", tenor);
        long loanId = db.insert("loans", null, loan);

        double total = principal + (principal * interest / 100.0);
        double installment = total / tenor;
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        for (int i = 0; i < tenor; i++) {
            calendar.add(java.util.Calendar.MONTH, 1);
            ContentValues item = new ContentValues();
            item.put("loan_id", loanId);
            item.put("amount", installment);
            item.put("due_date", String.format(java.util.Locale.getDefault(), "%04d-%02d-%02d",
                    calendar.get(java.util.Calendar.YEAR),
                    calendar.get(java.util.Calendar.MONTH) + 1,
                    calendar.get(java.util.Calendar.DAY_OF_MONTH)));
            item.put("paid", 0);
            db.insert("installments", null, item);
        }
        return loanId;
    }

    public Cursor getLoans() {
        return getReadableDatabase().rawQuery("SELECT id, name, principal, interest, tenor FROM loans ORDER BY id DESC", null);
    }

    public Cursor getInstallments(boolean paidOnly, boolean unpaidOnly) {
        String where = paidOnly ? "WHERE i.paid=1" : (unpaidOnly ? "WHERE i.paid=0" : "");
        return getReadableDatabase().rawQuery(
                "SELECT i.id, l.name, i.amount, i.due_date, i.paid " +
                "FROM installments i JOIN loans l ON i.loan_id=l.id " + where +
                " ORDER BY i.due_date ASC", null);
    }

    public void setPaid(long id, boolean paid) {
        ContentValues values = new ContentValues();
        values.put("paid", paid ? 1 : 0);
        getWritableDatabase().update("installments", values, "id=?", new String[]{String.valueOf(id)});
    }

    public double getTotalPrincipal() {
        Cursor c = getReadableDatabase().rawQuery("SELECT COALESCE(SUM(principal),0) FROM loans", null);
        double result = 0;
        if (c.moveToFirst()) result = c.getDouble(0);
        c.close();
        return result;
    }

    public int getUnpaidCount() {
        Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM installments WHERE paid=0", null);
        int result = 0;
        if (c.moveToFirst()) result = c.getInt(0);
        c.close();
        return result;
    }

    public double getCurrentMonthAmount() {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT COALESCE(SUM(amount),0) FROM installments " +
                "WHERE paid=0 AND substr(due_date,1,7)=strftime('%Y-%m','now','localtime')", null);
        double result = 0;
        if (c.moveToFirst()) result = c.getDouble(0);
        c.close();
        return result;
    }
}
