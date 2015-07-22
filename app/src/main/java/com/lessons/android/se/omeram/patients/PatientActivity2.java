package com.lessons.android.se.omeram.patients;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.Calendar;

public class PatientActivity2 extends AppCompatActivity {

    private final String[] FROM = {Constants._ID, Constants.COLUMN_YEAR, Constants.COLUMN_MONTH, Constants.COLUMN_DAY, Constants.COLUMN_GRADE};
    private final String ORDER_BY_DATE = "CAST(" + Constants.COLUMN_YEAR + " AS int), CAST(" + Constants.COLUMN_MONTH + " AS int), CAST(" + Constants.COLUMN_DAY + " AS int) ASC";
    private final String ORDER_BY_GRADE = Constants.COLUMN_GRADE + " ASC";

    private SQLiteDatabase db;
    private PatientDbHelper dbHelper;
    private TestCursorAdapter testCursorAdapter;
    private Cursor mCursor;

    private String date;
    private final Calendar c = Calendar.getInstance();
    private int mYear = c.get(Calendar.YEAR);
    private int mMonth = c.get(Calendar.MONTH);
    private int mDay = c.get(Calendar.DAY_OF_MONTH);

    private String table;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        String id = getIntent().getStringExtra(Constants.COLUMN_ID).trim();
        String name = getIntent().getStringExtra(Constants.COLUMN_NAME).trim();
        int age = getIntent().getIntExtra(Constants.COLUMN_AGE, 0);
        int weight = getIntent().getIntExtra(Constants.COLUMN_WEIGHT, 0);
        int height = getIntent().getIntExtra(Constants.COLUMN_HEIGHT, 0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(name + "'s tests");
        setSupportActionBar(toolbar);

        TextView patientAge = (TextView) findViewById(R.id.personalAge);
        TextView patientWeight = (TextView) findViewById(R.id.personalWeight);
        TextView patientHeight = (TextView) findViewById(R.id.personalHeight);

        patientAge.setText(age + " years old");
        patientWeight.setText("Weight (Kg): " + weight);
        patientHeight.setText("Height (Cm): " + height);

        date = setDate(mYear, mMonth, mDay);
        table = name.replaceAll("\\s", "") + id;

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

    //Init a patient table (name+id)
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

    //Build a proper date (dd/mm/yy) to display
    private String setDate(int year, int monthOfYear, int dayOfMonth) {
        return String.valueOf(dayOfMonth) + "/" + String.valueOf(monthOfYear + 1) + "/" + String.valueOf(year);
    }

    //Display the list of test
    private void showTestList(String table) {
        ListView listView = (ListView) findViewById(R.id.testList);
        listView.setLongClickable(true);

        mCursor = getTestCursor(table, FROM, null, ORDER_BY_DATE);

        testCursorAdapter = new TestCursorAdapter(this, mCursor);
        listView.setAdapter(testCursorAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }

    //Display - date selection dialog
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


        View positive = dialog.getActionButton(DialogAction.POSITIVE);
        positive.setEnabled(true);

        DatePicker datePicker = (DatePicker) dialog.getCustomView().findViewById(R.id.datePicker);
        datePicker.init(mYear, mMonth, mDay, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                date = setDate(year, monthOfYear, dayOfMonth);
                mYear = year;
                mMonth = monthOfYear;
                mDay = dayOfMonth;
            }
        });

        dialog.show();
    }

    //Display - add new test dialog
    private void showInputDialog() {
        Integer[] answers = new Integer[0];
        insertTestQuery();

        new MaterialDialog.Builder(this)
                .title("Add test on " + date)
                .items(R.array.questions)
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
                        updateTestGrade(count * 10);

                        mCursor = getTestCursor(table, FROM, null, ORDER_BY_DATE);
                        testCursorAdapter.changeCursor(mCursor);
                        testCursorAdapter.notifyDataSetChanged();

                        return true;
                    }

                })
                .show();
    }

    //Display - sort test list dialog
    private void showSortDialog() {
        new MaterialDialog.Builder(this)
                .title("Sort By..")
                .items(R.array.sort)
                .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {

                        switch (which) {
                            case 0:
                                mCursor = getTestCursor(table, FROM, null, ORDER_BY_DATE);
                                break;
                            case 1:
                                mCursor = getTestCursor(table, FROM, null, ORDER_BY_GRADE);
                                break;
                        }
                        testCursorAdapter.changeCursor(mCursor);
                        testCursorAdapter.notifyDataSetChanged();

                        return true; // allow selection
                    }
                })
                .positiveText(R.string.sort2)
                .show();
    }

    //Get test table cursor
    private Cursor getTestCursor(final String table, final String[] from,  final String where, final String order) {
        return db.query(table, from, where, null, null, null, order);
    }

    //Insert new test to db
    private void insertTestQuery() {
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_YEAR, mYear);
        values.put(Constants.COLUMN_MONTH, mMonth);
        values.put(Constants.COLUMN_DAY, mDay);
        db.insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    //Update test with the patient answers
    private void updateTestQuestion(final int question, final int answer) {
        String where = Constants.COLUMN_YEAR + " = ? AND " + Constants.COLUMN_MONTH + " = ? AND "  + Constants.COLUMN_DAY + " = ?";
        ContentValues values = new ContentValues();
        values.put(getQuestionString(question), answer);
        String[] args = new String[]{String.valueOf(mYear), String.valueOf(mMonth), String.valueOf(mDay)};
        db.update(table, values, where, args);
    }

    //Update the patient test's grade
    private void updateTestGrade(final int grade) {
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_GRADE, grade);
        String where = Constants.COLUMN_YEAR + " = ? AND " + Constants.COLUMN_MONTH + " = ? AND "  + Constants.COLUMN_DAY + " = ?";
        String[] args = new String[]{String.valueOf(mYear), String.valueOf(mMonth), String.valueOf(mDay)};
        db.update(table, values, where, args);
    }

    //Convert questions codes
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_patient, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();
        switch (id) {

            case R.id.action_sort:
                showSortDialog();
                return true;

            case R.id.action_graphs:
                ArrayList<String> years = new ArrayList<>();
                ArrayList<String> months = new ArrayList<>();
                ArrayList<String> days = new ArrayList<>();
                ArrayList<String> grades = new ArrayList<>();

                mCursor = getTestCursor(table, FROM, null, ORDER_BY_DATE);

                while (mCursor.moveToNext()) {
                    years.add(mCursor.getString(1));
                    months.add(mCursor.getString(2));
                    days.add(mCursor.getString(3));
                    grades.add(mCursor.getString(4));
                }

                Intent intentGraph = new Intent(getApplicationContext(), GraphActivity.class);
                intentGraph.putExtra(Constants.COLUMN_YEAR, years);
                intentGraph.putExtra(Constants.COLUMN_MONTH, months);
                intentGraph.putExtra(Constants.COLUMN_DAY, days);
                intentGraph.putExtra(Constants.COLUMN_GRADE, grades);
                startActivity(intentGraph);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //menuItem = menu.findItem(R.id.action_sort);
        return super.onPrepareOptionsMenu(menu);
    }

}
