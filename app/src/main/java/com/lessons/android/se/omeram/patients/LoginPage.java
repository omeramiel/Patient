package com.lessons.android.se.omeram.patients;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class LoginPage extends Activity {
    private EditText userNameET,passwordET;
    private SweetAlertDialog wrongPasswordDialog;
    private UsersBL usersBL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        userNameET=(EditText)findViewById(R.id.userNameET);
        passwordET=(EditText)findViewById(R.id.passwordET);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void loginClicked(View view)
    {
        String userNameFromET = userNameET.getText().toString();
        String userPasswordFromET = passwordET.getText().toString();
        if(userNameFromET.equals("")
                || userPasswordFromET.equals(""))
        {
            wrongPasswordAlertInit(getString(R.string.InputMessage));

        }
        else
        {
            int userPassword = Integer.parseInt(passwordET.getText().toString());


                usersBL = new UsersBL();
                usersBL.getBusinessID(this, this, userPassword, userNameFromET);
        }

    }

    private void wrongPasswordAlertInit(String message)
    {
        wrongPasswordDialog=new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
        wrongPasswordDialog.setTitleText(getString(R.string.connectionProblem));
        wrongPasswordDialog.setContentText(message);
        wrongPasswordDialog.setCancelable(true);
        wrongPasswordDialog.show();
    }


    public void Graph(View view)
    {
        Intent graphDisplayIntent=new Intent(this ,graph.class);
        String startDay,startMonth,startYear;
        String endDay,endMonth,endYear;
        startDay="01";
        startMonth="10";
        startYear="1980";
        endDay="02";
        endMonth="11";
        endYear="1980";
        graphDisplayIntent.putExtra("startDay", startDay);
        graphDisplayIntent.putExtra("startMonth", startMonth);
        graphDisplayIntent.putExtra("startYear",startYear);
        graphDisplayIntent.putExtra("endDay", endDay);
        graphDisplayIntent.putExtra("endMonth", endMonth);
        graphDisplayIntent.putExtra("endYear", endYear);
        startActivity(graphDisplayIntent);
    }
}
