package com.lessons.android.se.omeram.patients;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by omera_000 on 11/06/2015.
 */
public class PatientCursorAdapter extends CursorAdapter{

    private LayoutInflater layoutInflater;


    public PatientCursorAdapter(Context context, Cursor c) {
        super(context, c);
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return layoutInflater.inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView patientId = (TextView) view.findViewById(R.id.testId);
        TextView patientName = (TextView) view.findViewById(R.id.patientName);
        TextView patientAge = (TextView) view.findViewById(R.id.testDate);
        TextView patientWeight = (TextView) view.findViewById(R.id.patientWeight);
        TextView patientHeight = (TextView) view.findViewById(R.id.patientHeight);

        patientId.setText(cursor.getString(0));
        patientName.setText(cursor.getString(1));
        patientAge.setText("Age: " + cursor.getString(2));
        patientWeight.setText("Weight: " + cursor.getString(3));
        patientHeight.setText("Height: " + cursor.getString(4));
    }
}
