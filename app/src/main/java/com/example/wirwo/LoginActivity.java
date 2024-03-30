package com.example.wirwo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends Activity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView createAccountTextView;
    private Switch rememberMeSwitch;

    private FirebaseAuth auth;
    private SharedPreferences sharedPreferences;

    private static final String SHARED_PREF_NAME = "login_preferences";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

        // Finding views by their IDs
        usernameEditText = findViewById(R.id.login_username1);
        passwordEditText = findViewById(R.id.login_password1);
        loginButton = findViewById(R.id.login_button);
        createAccountTextView = findViewById(R.id.create);
        rememberMeSwitch = findViewById(R.id.rememberMeSwitch);

        // Set initial state of login button
        loginButton.setEnabled(false);

        // Add TextWatchers to monitor EditText fields
        usernameEditText.addTextChangedListener(textWatcher);
        passwordEditText.addTextChangedListener(textWatcher);

        // Setting click listener for the login button
        loginButton.setOnClickListener(v -> login());

        // Setting click listener for the create account text view
        createAccountTextView.setOnClickListener(v -> {
            // Action to perform when the create account text view is clicked
            // Create an Intent to start the SigninActivity
            Intent intent = new Intent(LoginActivity.this, SigninActivity.class);
            startActivity(intent);
            finish(); // Finish LoginActivity so the user cannot come back to it using the back button
        });

        // Pre-populate fields if "Remember Me" is checked
        String savedUsername = sharedPreferences.getString(KEY_USERNAME, "");
        String savedPassword = sharedPreferences.getString(KEY_PASSWORD, "");
        usernameEditText.setText(savedUsername);
        passwordEditText.setText(savedPassword);

        // Check switch state for debugging purposes (remove later)
        if (rememberMeSwitch.isChecked()) {
            Toast.makeText(LoginActivity.this, "Remember Me is checked", Toast.LENGTH_SHORT).show();
        }
    }

    private void login() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Check if username and password are not empty
        if (!username.isEmpty() && !password.isEmpty()) {
            // Append the domain to the username (modify if needed)
            String email = username + "@wirwo.com";

            // Disable the login button while logging in
            loginButton.setEnabled(false);

            // Perform login asynchronously
            new LoginTask().execute(email, password);
        } else {
            // Username or password is empty, display error message
            Toast.makeText(LoginActivity.this, "Username and password are required", Toast.LENGTH_SHORT).show();
        }
    }

    // TextWatcher to monitor changes in EditText fields
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            // Enable login button only if both username and password fields are not empty
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            loginButton.setEnabled(!username.isEmpty() && !password.isEmpty());

            // Save credentials immediately if "Remember Me" is checked
            if (rememberMeSwitch.isChecked()) {
                saveCredentials(username, password);
            }
        }
    };

    // Save username and password to SharedPreferences
    private void saveCredentials(String username, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_PASSWORD, password);
        editor.apply();
    }

    // AsyncTask to perform login asynchronously
    private class LoginTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String email = params[0];
            String password = params[1];

            Task<AuthResult> signInTask = auth.signInWithEmailAndPassword(email, password);
            try {
                Tasks.await(signInTask);
                return signInTask.isSuccessful(); // Login successful if task is successful
            } catch (Exception e) {
                e.printStackTrace();
                return false; // Login failed
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                // Login successful, show custom toast with success icon
                showCustomToast("Login Successful", true);
                // Navigate to Dashboard activity
                Intent intent = new Intent(LoginActivity.this, Dashboard.class);
                startActivity(intent);
                finish(); // Finish LoginActivity so the user cannot come back to it using the back button
            } else {
                // Login failed, show custom toast with failure icon
                showCustomToast("Login Failed", false);
            }
        }
    }

    // Method to show custom toast with app icon
    private void showCustomToast(String message, boolean isSuccess) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_layout, findViewById(R.id.toast_icon));

        ImageView iconImageView = layout.findViewById(R.id.toast_icon);
        TextView messageTextView = layout.findViewById(R.id.toast_text);

        // Set the app icon based on success or failure
        if (isSuccess) {
            // Set your success icon
            iconImageView.setImageResource(R.drawable.white_wirwo); // Replace with your success icon
        } else {
            // Set your failure icon
            iconImageView.setImageResource(R.drawable.white_wirwo); // Replace with your failure icon
        }

        // Set the message
        messageTextView.setText(message);

        // Create and show the toast
        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
