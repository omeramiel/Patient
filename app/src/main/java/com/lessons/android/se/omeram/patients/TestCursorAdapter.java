package com.lessons.android.se.omeram.patients;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

//This class adapt between the sqlite table (patient test) and the test listView in PatientActivity2
public class TestCursorAdapter extends CursorAdapter {

    private LayoutInflater layoutInflater;


    public TestCursorAdapter(Context context, Cursor c) {
        super(context, c);
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return layoutInflater.inflate(R.layout.test_list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView testId = (TextView) view.findViewById(R.id.testId);
        TextView testDate = (TextView) view.findViewById(R.id.testDate);
        TextView testGrade = (TextView) view.findViewById(R.id.testGrade);

        testId.setText("Test number " + cursor.getString(0));
        testDate.setText("Date: " + setDate(cursor.getInt(1), cursor.getInt(2), cursor.getInt(3)));
        testGrade.setText("Grade: " + cursor.getString(4));
    }

    private String setDate(int year, int monthOfYear, int dayOfMonth) {
        return String.valueOf(dayOfMonth) + "/" + String.valueOf(monthOfYear + 1) + "/" + String.valueOf(year);
    }
}
