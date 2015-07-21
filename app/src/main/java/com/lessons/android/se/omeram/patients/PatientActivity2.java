package com.lessons.android.se.omeram.patients;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Calendar;

/**
 * Created by omera_000 on 20/07/2015.
 */
public class PatientActivity2 extends AppCompatActivity {

    private static String TAG = PatientActivity2.class.getSimpleName();

    private SQLiteDatabase db;
    private PatientDbHelper dbHelper;

    private String name, id;
    private int age, weight, height;
    private View positive;
    private TestCursorAdapter testCursorAdapter;
    private Cursor mCursor;
    private String date;

    final Calendar c = Calendar.getInstance();
    int mYear = c.get(Calendar.YEAR);
    int mMonth = c.get(Calendar.MONTH);
    int mDay = c.get(Calendar.DAY_OF_MONTH);

    private String[] questions;
    private Integer[] answers;
    private DatePicker datePicker;
    private String table;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        id = getIntent().getStringExtra(Constants.COLUMN_ID).trim();
        name = getIntent().getStringExtra(Constants.COLUMN_NAME).trim();
        age = getIntent().getIntExtra(Constants.COLUMN_AGE, 0);
        weight = getIntent().getIntExtra(Constants.COLUMN_WEIGHT, 0);
        height = getIntent().getIntExtra(Constants.COLUMN_HEIGHT, 0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(name + "'s personal page");
        setSupportActionBar(toolbar);

        TextView patientAge = (TextView) findViewById(R.id.personalAge);
        TextView patientWeight = (TextView) findViewById(R.id.personalWeight);
        TextView patientHeight = (TextView) findViewById(R.id.personalHeight);

        patientAge.setText(age + " years old");
        patientWeight.setText("Weight (Kg): " + weight);
        patientHeight.setText("Height (Cm): " + height);

        date = setDate(mYear, mMonth, mDay);
        table = name.replaceAll("\\s","") + id;

        initTable(table);

        showTestList(table);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabAddTest);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateDialog();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't forget to close database connection
        if (dbHelper != null)
            dbHelper.closeDB();
    }

    private void initTable(final String table) {
        String dbTestCreate = "CREATE TABLE IF NOT EXISTS " + table + " ("
                + Constants._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Constants.COLUMN_YEAR + " TEXT NOT NULL, "
                + Constants.COLUMN_MONTH + " TEXT NOT NULL, "
                + Constants.COLUMN_DAY + " TEXT NOT NULL, "
                + Constants.COLUMN_GRADE + " INTEGER, "
                + Constants.COLUMN_ANSWER_ONE + " INTEGER, "
                + Constants.COLUMN_ANSWER_TWO + " INTEGER, "
                + Constants.COLUMN_ANSWER_THREE + " INTEGER, "
                + Constants.COLUMN_ANSWER_FOUR + " INTEGER, "
                + Constants.COLUMN_ANSWER_FIVE + " INTEGER, "
                + Constants.COLUMN_ANSWER_SIX + " INTEGER, "
                + Constants.COLUMN_ANSWER_SEVEN + " INTEGER, "
                + Constants.COLUMN_ANSWER_EIGHT + " INTEGER, "
                + Constants.COLUMN_ANSWER_NINE + " INTEGER, "
                + Constants.COLUMN_ANSWER_TEN + " INTEGER);";

        dbHelper = new PatientDbHelper(this, dbTestCreate, table);
        db = dbHelper.getWritableDatabase();
        db.execSQL(dbTestCreate);
    }

    private String setDate(int year, int monthOfYear, int dayOfMonth) {
        return String.valueOf(dayOfMonth) + "/" + String.valueOf(monthOfYear + 1) + "/" + String.valueOf(year);
    }

    //display the list of test
    private void showTestList(String table) {
        ListView listView = (ListView) findViewById(R.id.testList);
        listView.setLongClickable(true);

        String[] from = {Constants._ID, Constants.COLUMN_YEAR, Constants.COLUMN_MONTH, Constants.COLUMN_DAY, Constants.COLUMN_GRADE};
        String order = Constants.COLUMN_YEAR + ", " + Constants.COLUMN_MONTH + ", " + Constants.COLUMN_DAY + " ASC";

        mCursor = getTestCursor(table, from, null, order);

        testCursorAdapter = new TestCursorAdapter(this, mCursor);
        listView.setAdapter(testCursorAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }

    //date dialog
    private void showDateDialog() {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.date_dialog_title)
                .customView(R.layout.dialog_customview_patient, true)
                .positiveText(R.string.next)
                .negativeText(android.R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {

                    @Override
                    public void onPositive(MaterialDialog dialog) {

                        showInputDialog();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                    }
                }).build();


        positive = dialog.getActionButton(DialogAction.POSITIVE);
        positive.setEnabled(true);

        datePicker = (DatePicker) dialog.getCustomView().findViewById(R.id.datePicker);
        datePicker.init(mYear, mMonth, mDay, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                date = setDate(year, monthOfYear, dayOfMonth);
                mYear = year;
                mMonth = monthOfYear;
                mDay = dayOfMonth;
                Log.e(TAG, date);
            }
        });

        dialog.show();
    }

    //new test dialog
    private void showInputDialog() {

        questions = new String[10];
        answers = new Integer[0];
        insertTestQuery();

        new MaterialDialog.Builder(this)
                .title("Add test on " + date)
                .items(questions)
                .positiveText(R.string.set)
                .itemsCallbackMultiChoice(answers, new MaterialDialog.ListCallbackMultiChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {

                        int count = 0;
                        //dynamically sets checked answers in sql
                        for (Integer aWhich : which) {
                            count++;
                            updateTestQuestion(aWhich + 1, 1);
                        }
                        updateTestGrade(count*10);
                        return true;
                    }

                })
                .show();
    }

    //get test table cursor
    private Cursor getTestCursor(final String table,final String[] from,  final String where, final String order) {
        return db.query(table, from, where, null, null, null, order);
    }

    //update the query (1..10, 0..1)
    private void insertTestQuery() {
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_YEAR, mYear);
        values.put(Constants.COLUMN_MONTH, mMonth);
        values.put(Constants.COLUMN_DAY, mDay);
        db.insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    //update the query (1..10, 0..1)
    private void updateTestQuestion(final int question, final int answer) {
        String where = Constants.COLUMN_YEAR + " = ? AND " + Constants.COLUMN_MONTH + " = ? AND "  + Constants.COLUMN_DAY + " = ?";
        ContentValues values = new ContentValues();
        values.put(getQuestionString(question), answer);
        String[] args = new String[]{String.valueOf(mYear), String.valueOf(mMonth), String.valueOf(mDay)};
        db.update(table, values, where, args);
    }

    //update the query (1..10, 0..1)
    private void updateTestGrade(final int grade) {
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_GRADE, grade);
        String where = Constants.COLUMN_YEAR + " = ? AND " + Constants.COLUMN_MONTH + " = ? AND "  + Constants.COLUMN_DAY + " = ?";
        String[] args = new String[]{String.valueOf(mYear), String.valueOf(mMonth), String.valueOf(mDay)};
        db.update(table, values, where, args);
    }

    private String getQuestionString(int question) {
        switch (question) {
            case 1:
                return Constants.COLUMN_ANSWER_ONE;
            case 2:
                return Constants.COLUMN_ANSWER_TWO;
            case 3:
                return Constants.COLUMN_ANSWER_THREE;
            case 4:
                return Constants.COLUMN_ANSWER_FOUR;
            case 5:
                return Constants.COLUMN_ANSWER_FIVE;
            case 6:
                return Constants.COLUMN_ANSWER_SIX;
            case 7:
                return Constants.COLUMN_ANSWER_SEVEN;
            case 8:
                return Constants.COLUMN_ANSWER_EIGHT;
            case 9:
                return Constants.COLUMN_ANSWER_NINE;
            case 10:
                return Constants.COLUMN_ANSWER_TEN;
        }
        return null;
    }

}
