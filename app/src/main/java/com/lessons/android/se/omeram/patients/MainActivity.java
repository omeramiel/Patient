package com.lessons.android.se.omeram.patients;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;


public class MainActivity extends AppCompatActivity {

    private static String TAG = MainActivity.class.getSimpleName();

    // Database creation sql statement
    private static final String DATABASE_PATIENT_CREATE = "CREATE TABLE IF NOT EXISTS " + Constants.TABLE_PATIENTS + " ("
            + Constants._ID + " INTEGER PRIMARY KEY, "
            + Constants.COLUMN_NAME + " TEXT NOT NULL);";

    private PatientDbHelper dbHelper;
    private static String[] FROM = {Constants._ID, Constants.COLUMN_NAME};
    private static String ORDER_BY = Constants._ID + " ASC";
    private SQLiteDatabase db;

    private MenuItem menuItem;
    private boolean isSearchOpened = false;
    private EditText searchEditText, idInput, nameInput;
    private PatientCursorAdapter patientCursorAdapter;
    private Cursor mCursor;
    private View positive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        showEvents();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabAddPatient);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog();
            }
        });
    }

    //display the list of patients
    private void showEvents() {
        dbHelper = new PatientDbHelper(this, DATABASE_PATIENT_CREATE, Constants.TABLE_PATIENTS);
        ListView listView = (ListView) findViewById(R.id.patientList);
        db = dbHelper.getWritableDatabase();

        mCursor = getEvents();

        patientCursorAdapter = new PatientCursorAdapter(this, mCursor);
        listView.setAdapter(patientCursorAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intentPatient = new Intent(getApplicationContext(), PatientActivity.class);
                Cursor cursor = (Cursor) patientCursorAdapter.getItem(position);
                intentPatient.putExtra(Constants.ID, cursor.getInt(0));
                intentPatient.putExtra(Constants.NAME, cursor.getString(1));
                startActivity(intentPatient);
            }
        });
    }

    //insert new patient to db
    private void insertQuery(String id, String name) {
        if (!id.isEmpty() && !name.isEmpty()) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(Constants._ID, id);
            values.put(Constants.COLUMN_NAME, name);
            db.insertWithOnConflict(Constants.TABLE_PATIENTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    //get patient table cursor
    private Cursor getEvents() {
        return db.query(Constants.TABLE_PATIENTS, FROM, null, null, null, null, ORDER_BY);
    }

    //add a patient dialog
    private void showInputDialog() {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.dialog_title)
                .customView(R.layout.dialog_customview, true)
                .positiveText(R.string.add)
                .negativeText(android.R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        insertQuery(idInput.getText().toString(), nameInput.getText().toString());
                        mCursor = getEvents();
                        patientCursorAdapter.changeCursor(mCursor);
                        patientCursorAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                    }
                }).build();
        
        idInput = (EditText) dialog.getCustomView().findViewById(R.id.patientId);
        nameInput = (EditText) dialog.getCustomView().findViewById(R.id.patientName);
        positive = dialog.getActionButton(DialogAction.POSITIVE);
        positive.setEnabled(false);

        idInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!idInput.getText().toString().isEmpty())
                    positive.setEnabled(true);
                else
                    positive.setEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {

            case R.id.action_search:
                handleMenuSearch();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menuItem = menu.findItem(R.id.action_search);
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * methods hides the keyboard
     *
     * @param view
     */
    private void hideKeyboard(View view) {
        //hides the keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * methods show the keyboard
     *
     * @param view
     */
    private void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * method handles the search logic
     */
    private void handleMenuSearch() {
        ActionBar action = getSupportActionBar(); //get the actionbar
        if (isSearchOpened) { //test if the search is open
            action.setDisplayShowCustomEnabled(false); //disable a custom view inside the actionbar
            action.setDisplayShowTitleEnabled(true); //show the title in the action bar
            hideKeyboard(searchEditText);
            //add the search icon in the action bar
            menuItem.setIcon(getResources().getDrawable(R.drawable.abc_ic_search_api_mtrl_alpha));
            isSearchOpened = false;
        } else { //open the search entry
            action.setDisplayShowCustomEnabled(true);
            //enable it to display a
            // custom view in the action bar.
            action.setCustomView(R.layout.search_bar);//add the custom view
            action.setDisplayShowTitleEnabled(false); //hide the title
            searchEditText = (EditText) action.getCustomView().findViewById(R.id.edtSearch); //the text editor

            //this is a listener to do a search when the user clicks on search button
            searchEditText.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                    doSearch(cs.toString());

                }

                @Override
                public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }

                @Override
                public void afterTextChanged(Editable arg0) { }
            });

            searchEditText.requestFocus();
            //open the keyboard focused in the edtSearch
            showKeyboard(searchEditText);
            //add the close icon
            menuItem.setIcon(getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha));
            isSearchOpened = true;
        }
    }

    private void doSearch(String search) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + Constants.TABLE_PATIENTS
                + " WHERE TRIM(" + Constants._ID + ") LIKE '%" + search.trim()
                + "%' OR TRIM(" + Constants.COLUMN_NAME + ") LIKE '%" + search.trim() + "%'", null);
        cursor.moveToFirst();
        patientCursorAdapter.changeCursor(cursor);
        patientCursorAdapter.notifyDataSetChanged();
      }

    @Override
    public void onBackPressed() {
        if (isSearchOpened) {
            handleMenuSearch();
            mCursor = getEvents();
            patientCursorAdapter.changeCursor(mCursor);
            patientCursorAdapter.notifyDataSetChanged();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't forget to close database connection
        if (dbHelper != null)
            dbHelper.closeDB();
    }

}
