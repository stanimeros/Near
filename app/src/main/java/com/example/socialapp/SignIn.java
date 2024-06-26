package com.example.socialapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

@SuppressWarnings("ALL")
public class SignIn extends AppCompatActivity {
    private EditText phone;
    private EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        try {
            phone = findViewById(R.id.editTextSignInPhone);
            password = findViewById(R.id.editTextSignInPassword);

            phone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (!hasFocus) {
                        hideKeyboard(view);
                    }
                }
            });

            password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (!hasFocus) {
                        hideKeyboard(view);
                    }
                }
            });

            TextView signUp = findViewById(R.id.textViewSignUp);
            signUp.setPaintFlags(signUp.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            signUp.setOnClickListener(view -> goToSignUp());

            Button signIn = findViewById(R.id.buttonSignIn);

            signIn.setOnClickListener(view -> {
                phone.clearFocus();
                password.clearFocus();
                AsyncTaskSignIn SignIn = new AsyncTaskSignIn();
                SignIn.execute();
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void goToSignUp()
    {
        Intent intent = new Intent(this, SignUp.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        finish();
    }

    private void goToFeed(User user)
    {
        SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(SignIn.this).edit();
        prefEditor.putString("phone",user.getPhone());
        prefEditor.putString("username",user.getUsername());
        prefEditor.putString("joinDate",user.getJoinDate());
        prefEditor.putInt("image",user.getImage());
        prefEditor.apply();

        Intent intent = new Intent(this, Feed.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("user", user);
        intent.putExtras(bundle);
        startActivity(intent);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        finish();
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskSignIn extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                User user = HttpHelper.signIn(phone.getText().toString(),password.getText().toString());
                if (user != null){
                    goToFeed(user);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public void onBackPressed() {}
}