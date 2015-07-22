package com.lessons.android.se.omeram.patients;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import cn.pedant.SweetAlert.SweetAlertDialog;

//Login page for users(Doctors) with permissions to access the app through JDBC
public class LoginPage extends Activity {

    private EditText userNameET,passwordET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        userNameET=(EditText)findViewById(R.id.userNameET);
        passwordET=(EditText)findViewById(R.id.passwordET);
    }

    public void loginClicked(View view) {
        String userNameFromET = userNameET.getText().toString();
        String userPasswordFromET = passwordET.getText().toString();
        if(userNameFromET.equals("") || userPasswordFromET.equals("")) {
            wrongPasswordAlertInit(getString(R.string.InputMessage));
        } else {
            int userPassword = Integer.parseInt(passwordET.getText().toString());
            UsersBL usersBL = new UsersBL();
            usersBL.getBusinessID(this, this, userPassword, userNameFromET);
        }
    }

    private void wrongPasswordAlertInit(String message) {
        SweetAlertDialog wrongPasswordDialog = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
        wrongPasswordDialog.setTitleText(getString(R.string.connectionProblem));
        wrongPasswordDialog.setContentText(message);
        wrongPasswordDialog.setCancelable(true);
        wrongPasswordDialog.show();
    }

}