package com.myprojects.unipiaudiostories.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.widget.Button;
import com.myprojects.unipiaudiostories.utils.AuthManager;
import com.myprojects.unipiaudiostories.utils.LanguageHelper;
import com.myprojects.unipiaudiostories.R;


public class AuthActivity extends AppCompatActivity {
    private EditText etUsername, etEmail, etPassword;
    private Button btnAction;
    private TextView tvSwitchAuth, tvForgotPassword, tvDeleteAccount; // Προσθήκη tvDeleteAccount
    //boolean flag που δείχνει αν θα έχουμε sign-in ή sign-up
    private boolean isLoginModeActive = true;
    private AuthManager authManager;


    @Override
    protected void onStart() {
        super.onStart();
        authManager = new AuthManager();

        //αν ο χρήστης έχει συνδεθεί ήδη από την συσκευή αυτή τότε το login παραλείπεται
        if (authManager.isUserLoggedIn()) {
            startMainActivity();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //από την util class LanguageHelper παίρνω το κατάλληλο string resource
        // για να χρησιμοποιηθεί το σωστό string resource file και να φορτωθεί το UI σωστά
        LanguageHelper.updateResources(this, LanguageHelper.getSavedLanguage(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        authManager = new AuthManager();

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnAction = findViewById(R.id.btnLogin);
        tvSwitchAuth = findViewById(R.id.tvSwitchAuth);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);


        // ενημέρωση της φόρμας της οθόνης για login ή sign-up
        updateAuthMode();

        tvSwitchAuth.setOnClickListener(v -> {
            isLoginModeActive = !isLoginModeActive;
            // ενημέρωση της φόρμας της οθόνης για login ή sign-up
            updateAuthMode();
        });

        // εδώ ο χρήστης πάτησε είτε για να ολοκληρώσει την σύνδεση ή την εγγραφή
        btnAction.setOnClickListener(v -> handleAuth());


        tvForgotPassword.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (email.isEmpty()) {
                // σε ποιο email να στείλουμε το link
                Toast.makeText(this, R.string.enter_email_address, Toast.LENGTH_SHORT).show();
                return;
            }

            authManager.resetPassword(email, new AuthManager.AuthCallback() {
                @Override
                public void onSuccess() {
                    // να κοιτάξει ο χρήστης στα εισερχόμενα
                    Toast.makeText(AuthActivity.this, R.string.reset_password_email_sent, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(AuthActivity.this, R.string.email_sent_failure, Toast.LENGTH_LONG).show();
                }
            });
        });
    }






    private void updateAuthMode() {
        if (isLoginModeActive) {
            etUsername.setVisibility(View.GONE); // στο login δεν χρειάζεται το όνομα χρήστη
            btnAction.setText(R.string.login);
            tvForgotPassword.setVisibility(View.VISIBLE);
            tvSwitchAuth.setText(R.string.signup);
        } else {
            etUsername.setVisibility(View.VISIBLE);
            tvForgotPassword.setVisibility(View.GONE);
            btnAction.setText(R.string.signup);
            tvSwitchAuth.setText(R.string.login);
        }
    }







    private void handleAuth() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.fill_all_fields_message, Toast.LENGTH_SHORT).show();
            return;
        }

        // έλεγχος σωστής σύνταξης email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.invalid_email_format));
            return;
        }

        AuthManager.AuthCallback authCallback = new AuthManager.AuthCallback() {
            @Override
            public void onSuccess() {
                if (isLoginModeActive) {
                    // Αν είναι Login και ήρθε Success, σημαίνει ότι πέρασε και το verification
                    startMainActivity();
                } else {
                    //Μήνυμα για έλεγχο email
                    Toast.makeText(AuthActivity.this, R.string.verify_email_notice, Toast.LENGTH_LONG).show();

                    // Μήνυμα ότι η εγγραφή ολοκληρώθηκε (θα εμφανιστεί μετά το παραπάνω)
                    Toast.makeText(AuthActivity.this, R.string.verify_email_notice, Toast.LENGTH_LONG).show();


                    authManager.signOut();

                    // 3. Μετάβαση στη login φόρμα
                    isLoginModeActive = true;
                    updateAuthMode();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                // ΔΙΟΡΘΩΣΗ: Ενημέρωση του χρήστη αν η αποτυχία οφείλεται σε έλλειψη verification
                if (errorMessage.equals("Email not verified")) {
                    Toast.makeText(AuthActivity.this, R.string.verify_email_notice, Toast.LENGTH_LONG).show();
                } else {
                    int msgRes = isLoginModeActive ? R.string.login_failed : R.string.registration_failed;
                    Toast.makeText(AuthActivity.this, getString(msgRes), Toast.LENGTH_LONG).show();
                }
            }
        };

        if (isLoginModeActive) {
            // σύνδεση μέσω του AuthManager
            authManager.signIn(email, password, authCallback);
        } else {
            String username = etUsername.getText().toString().trim();
            // πρέπει να συμπληρηθεί το όνομα χρήστη υποχρεωτικά
            if (username.isEmpty()) {
                Toast.makeText(this, R.string.username_required, Toast.LENGTH_SHORT).show();
                return;
            }
            // εγγραφή μέσω του AuthManager
            authManager.signUp(email, password, username, authCallback);
        }
    }







    private void startMainActivity() {
        //το login ή register ολοκληρώθηκε με επιτυχία άρα ανακατευθύνω στην MainActivity
        startActivity(new Intent(AuthActivity.this, MainActivity.class));
        finish();
    }
}