package com.lessons.android.se.omeram.patients;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends AppCompatActivity {


    // Database creation sql statement
    private final String DATABASE_PATIENT_CREATE = "CREATE TABLE IF NOT EXISTS " + Constants.TABLE_PATIENTS + " ("
            + Constants._ID + " INTEGER PRIMARY KEY, "
            + Constants.COLUMN_NAME + " TEXT NOT NULL, "
            + Constants.COLUMN_AGE + " INTEGER NOT NULL, "
            + Constants.COLUMN_WEIGHT + " INTEGER NOT NULL, "
            + Constants.COLUMN_HEIGHT + " INTEGER NOT NULL"
            + ");";

    private final String[] FROM = {Constants._ID, Constants.COLUMN_NAME, Constants.COLUMN_AGE, Constants.COLUMN_WEIGHT, Constants.COLUMN_HEIGHT};
    private final String ORDER_BY = Constants._ID + " ASC";

    //Sqlite db instance
    private PatientDbHelper dbHelper;
    private SQLiteDatabase db;

    private MenuItem menuItem;
    private boolean isSearchOpened = false;
    private EditText searchEditText, idInput, nameInput;
    private NumberPicker ageInput, weightInput, heightInput;
    private ImageView imageViewPatient;
    private PatientCursorAdapter patientCursorAdapter;
    private Cursor mCursor;
    private View positive;
    private static final int REQUEST_CODE = 1;
    private Bitmap bitmap;

    private FloatingActionButton fabRemove;
    private boolean longPressed;

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

    //Display the list of patients
    private void showEvents() {
        dbHelper = new PatientDbHelper(this, DATABASE_PATIENT_CREATE, Constants.TABLE_PATIENTS);
        ListView listView = (ListView) findViewById(R.id.patientList);
        listView.setLongClickable(true);
        db = dbHelper.getWritableDatabase();

        mCursor = getEvents();

        patientCursorAdapter = new PatientCursorAdapter(this, mCursor);
        listView.setAdapter(patientCursorAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intentPatient = new Intent(getApplicationContext(), PatientActivity2.class);
                Cursor cursor = (Cursor) patientCursorAdapter.getItem(position);
                intentPatient.putExtra(Constants.COLUMN_ID, cursor.getString(0));
                intentPatient.putExtra(Constants.COLUMN_NAME, cursor.getString(1));
                intentPatient.putExtra(Constants.COLUMN_AGE, cursor.getInt(2));
                intentPatient.putExtra(Constants.COLUMN_WEIGHT, cursor.getInt(3));
                intentPatient.putExtra(Constants.COLUMN_HEIGHT, cursor.getInt(4));
                startActivity(intentPatient);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View view, final int position, long id) {


                final Cursor cursor = (Cursor) patientCursorAdapter.getItem(position);

                fabRemove = (FloatingActionButton) findViewById(R.id.fabRemovePatient);
                updateView();

                fabRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fabRemove.setVisibility(View.INVISIBLE);
                        longPressed = false;

                        removeQuery(cursor.getString(0));

                        mCursor = getEvents();
                        patientCursorAdapter.changeCursor(mCursor);
                        patientCursorAdapter.notifyDataSetChanged();

                    }
                });
                return true;
            }
        });

    }

    //Popup the remove button after long press on an item in the patient list
    private void updateView() {
        if (!longPressed) {
            fabRemove.setVisibility(View.VISIBLE);
            longPressed = true;
        }
    }

    //Add a patient dialog
    private void showInputDialog() {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.dialog_title)
                .customView(R.layout.dialog_customview, true)
                .positiveText(R.string.add)
                .negativeText(android.R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {

                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        insertQuery(idInput.getText().toString(),
                                nameInput.getText().toString(),
                                ageInput.getValue(),
                                weightInput.getValue(),
                                heightInput.getValue());
                        mCursor = getEvents();
                        patientCursorAdapter.changeCursor(mCursor);
                        patientCursorAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                    }
                }).build();

        idInput = (EditText) dialog.getCustomView().findViewById(R.id.testId);
        nameInput = (EditText) dialog.getCustomView().findViewById(R.id.patientName);
        ageInput = (NumberPicker) dialog.getCustomView().findViewById(R.id.testDate);
        weightInput = (NumberPicker) dialog.getCustomView().findViewById(R.id.patientWeight);
        heightInput = (NumberPicker) dialog.getCustomView().findViewById(R.id.patientHeight);
        imageViewPatient = (ImageView) dialog.getCustomView().findViewById(R.id.imageViewPatient);
        imageViewPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        ageInput.setMinValue(16);
        ageInput.setMaxValue(99);
        weightInput.setMinValue(1);
        weightInput.setMaxValue(200);
        heightInput.setMinValue(40);
        heightInput.setMaxValue(250);

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

    //Remove the given patient from db
    private void removeQuery(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + Constants.TABLE_PATIENTS + " WHERE " + Constants._ID + "='" + id + "'");
    }

    //insert new patient to db
    private void insertQuery(String id, String name, int age, int weight, int height) {
        if (!id.isEmpty() && !name.isEmpty()) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(Constants._ID, id);
            values.put(Constants.COLUMN_NAME, name);
            values.put(Constants.COLUMN_AGE, age);
            values.put(Constants.COLUMN_WEIGHT, weight);
            values.put(Constants.COLUMN_HEIGHT, height);
            db.insertWithOnConflict(Constants.TABLE_PATIENTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    //get patients table cursor
    private Cursor getEvents() {
        return db.query(Constants.TABLE_PATIENTS, FROM, null, null, null, null, ORDER_BY);
    }

    @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        InputStream stream = null;
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK)
            try {
                // recyle unused bitmaps
                if (bitmap != null) {
                    bitmap.recycle();
                }
                stream = getContentResolver().openInputStream(data.getData());
                bitmap = BitmapFactory.decodeStream(stream);

                imageViewPatient.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (stream != null)
                    try {
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
             }

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

    //Search a substring in patient table (id or name)
    private void doSearch(String search) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + Constants.TABLE_PATIENTS
                + " WHERE TRIM(" + Constants._ID + ") LIKE '%" + search.trim()
                + "%' OR TRIM(" + Constants.COLUMN_NAME + ") LIKE '%" + search.trim() + "%'", null);
        cursor.moveToFirst();
        patientCursorAdapter.changeCursor(cursor);
        patientCursorAdapter.notifyDataSetChanged();
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

    @Override
    public void onBackPressed() {

        if (isSearchOpened) {
            handleMenuSearch();
            mCursor = getEvents();
            patientCursorAdapter.changeCursor(mCursor);
            patientCursorAdapter.notifyDataSetChanged();
            return;
        }

        if (longPressed) {
            fabRemove.setVisibility(View.INVISIBLE);
            longPressed = false;
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
