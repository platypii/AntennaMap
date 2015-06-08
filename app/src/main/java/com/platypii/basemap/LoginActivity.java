package com.platypii.basemap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested
     */
    private AuthTask mAuthTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Check auth
        if(Auth.checkAuth(this)) {
            // Start Main Activity
            final Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
        } else {
            // Auth missing, prompt for password
            showPasswordPrompt();
        }
    }

    private void showPasswordPrompt() {
        final EditText passwordView = new EditText(this);
        new AlertDialog.Builder(this)
            .setTitle(R.string.password_prompt)
            .setView(passwordView)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // Validate password
                    final CharSequence password = passwordView.getText();
                    mAuthTask = new AuthTask(password);
                    mAuthTask.execute();
                }
            })
            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // End application
                    Toast.makeText(LoginActivity.this, R.string.error_password_required, Toast.LENGTH_LONG).show();
                    finish();
                }
            }).show();
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate the user.
     */
    public class AuthTask extends AsyncTask<Void, Void, Boolean> {
        private final CharSequence password;
        AuthTask(CharSequence password) {
            this.password = password;
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            // Post to baseline server, return true iff token checks out
            return Auth.validate(password);
        }
        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            if(success) {
                // Update preferences
                final SharedPreferences.Editor editor = LoginActivity.this.getSharedPreferences("baseline", Context.MODE_PRIVATE).edit();
                editor.putBoolean("basemap_authenticated", true);
                editor.apply();
                // Start Main Activity
                final Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                finish();
                startActivity(intent);
            } else {
                // TODO: Tell user that password was wrong
                Toast.makeText(LoginActivity.this, R.string.error_password_invalid, Toast.LENGTH_LONG).show();
                // Re-show password prompt
                showPasswordPrompt();
            }
        }
        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }
}



