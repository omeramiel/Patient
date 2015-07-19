package com.lessons.android.se.omeram.patients;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Calendar;

/**
 * Created by omera_000 on 11/06/2015.
 */
public class PatientActivity extends AppCompatActivity {

    private static String TAG = PatientActivity.class.getSimpleName();

    private SQLiteDatabase db;
    private PatientDbHelper dbHelper;

    private String name;
    private String date;
    private String table;

    private String[] questions;
    private Integer[] answers;

    final Calendar c = Calendar.getInstance();
    int mYear = c.get(Calendar.YEAR);
    int mMonth = c.get(Calendar.MONTH);
    int mDay = c.get(Calendar.DAY_OF_MONTH);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        //id = String.valueOf(getIntent().getIntExtra(Constants.ID, -1));
        name = getIntent().getStringExtra(Constants.NAME).trim();
        date = setDate(mYear, mMonth, mDay);
        table = setTableName(name, mYear, mMonth, mDay);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(name + "'s personal page");
        setSupportActionBar(toolbar);

        DatePicker datePicker = (DatePicker) findViewById(R.id.datePicker);
        datePicker.init(mYear, mMonth, mDay, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                date = setDate(year, monthOfYear, dayOfMonth);
                table = setTableName(name, year, monthOfYear, dayOfMonth);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabAddTest);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog();
            }
        });

        Button buttonAverage = (Button) findViewById(R.id.buttonAverage);
        buttonAverage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initTable(table);
                Cursor cursor = getEvents(table, Constants.COLUMN_ANSWER + " = " + String.valueOf(1));
                double average = ((double)cursor.getCount() / (double)Constants.NUMBER_OF_QUESTIONS * 100);
                Toast.makeText(getApplicationContext(), "Test average on " + date + " is: " + (int) average, Toast.LENGTH_SHORT).show();
            }
        });

        Button buttonResult = (Button) findViewById(R.id.buttonResult);
        buttonResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQuestionDialog();
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

    private String setDate(int year, int monthOfYear, int dayOfMonth) {
        return String.valueOf(dayOfMonth) + "/" + String.valueOf(monthOfYear + 1) + "/" + String.valueOf(year);
    }

    private String setTableName(String name, int year, int monthOfYear, int dayOfMonth) {
        return name.replaceAll("\\s","") + String.valueOf(dayOfMonth) + "" + String.valueOf(monthOfYear + 1) + "" + String.valueOf(year);
    }

    //create new table (name + date)
    private void initTable(final String table) {

        String dbTestCreate = "CREATE TABLE IF NOT EXISTS " + table + " ("
                + Constants._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Constants.COLUMN_ANSWER + " INTEGER NOT NULL);";

        dbHelper = new PatientDbHelper(this, dbTestCreate, table);
        db = dbHelper.getWritableDatabase();
        db.execSQL(dbTestCreate);
    }

    //update the query (1..10, 0)
    private void initQuery(final int question) {
        ContentValues values = new ContentValues();
        values.put(Constants._ID, question);
        values.put(Constants.COLUMN_ANSWER, 0);
        db.insertOrThrow(table, null, values);
    }

    //update the query (1..10, 0..1)
    private void updateQuery(final int question, final int answer) {
        ContentValues values = new ContentValues();
        values.put(Constants._ID, question);
        values.put(Constants.COLUMN_ANSWER, answer);
        db.insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    //sql query
    private Cursor getEvents(final String table, final String where) {
        return db.query(table, null, where, null, null, null, null);
    }

    //new test dialog
    private void showInputDialog() {

        initTable(table);
        Cursor cursor = getEvents(table, null);

        if (cursor.getCount() == 0) {
            questions = initTest();
            answers = new Integer[0];
        } else {
            questions = getQuestions();
            answers = getAnswers();
        }

        new MaterialDialog.Builder(this)
                .title("Test on " + date)
                .items(questions)
                .itemsCallbackMultiChoice(answers, new MaterialDialog.ListCallbackMultiChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {

                        //clear the answers in sql
                        for (int i = 0; i < questions.length; i++) {
                            updateQuery(i + 1, 0);
                        }

                        //dynamically sets checked answers in sql
                        for (Integer aWhich : which) {
                            updateQuery(aWhich + 1, 1);
                        }
                        return true;
                    }
                })
                .alwaysCallMultiChoiceCallback()
                .show();
    }

    //choose question number to receive its result
    private void showQuestionDialog() {

        String[] arr = new String[Constants.NUMBER_OF_QUESTIONS];
        for (int i = 0; i < arr.length; i++)
            arr[i] = String.valueOf(i + 1);

        new MaterialDialog.Builder(this)
                .title("Choose a question")
                .items(arr)
                .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        initTable(table);

                        Cursor cursor = getEvents(table, Constants._ID + " = " + String.valueOf(which + 1));
                        if (cursor.getCount() != 0) {
                            cursor.moveToNext();
                            Toast.makeText(getApplicationContext(), "Qusetion " + (which + 1) + " result is: " + cursor.getInt(1), Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(getApplicationContext(), "No test on " + date, Toast.LENGTH_SHORT).show();

                        return true; // allow selection
                    }
                })
                .positiveText("Choose")
                .show();

    }

    //init test questions (1..10) & answers (0's)
    private String[] initTest() {
        String[] arr = new String[Constants.NUMBER_OF_QUESTIONS];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = String.valueOf(i + 1);
            initQuery(i + 1);
        }
        return arr;
    }

    //get array of questions from sql
    private String[] getQuestions() {
        Cursor cursor = getEvents(table, null);
        String[] arr = new String[cursor.getCount()];
        while (cursor.moveToNext()) {
            arr[cursor.getInt(0) - 1] = String.valueOf(cursor.getInt(0));
        }
        return arr;
    }

    //get array of answers from sql
    private Integer[] getAnswers() {
        Cursor cursor = getEvents(table, Constants.COLUMN_ANSWER + " = " + String.valueOf(1));
        Integer[] arr = new Integer[cursor.getCount()];
        int i = 0;
        while (cursor.moveToNext()) {
            if (cursor.getInt(1) == 1) {
                arr[i] = cursor.getInt(0) - 1;
                i++;
            }
        }
        return arr;
    }

}
