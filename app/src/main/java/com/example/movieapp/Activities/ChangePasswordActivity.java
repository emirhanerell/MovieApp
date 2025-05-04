package com.example.movieapp.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.movieapp.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {
    private EditText currentPasswordEdit, newPasswordEdit, confirmPasswordEdit;
    private Button updatePasswordBtn;
    private ProgressBar progressBar;
    private ImageView backButton;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        initComponents();
        setupClickListeners();
    }

    private void initComponents() {
        currentPasswordEdit = findViewById(R.id.currentPasswordEditText);
        newPasswordEdit = findViewById(R.id.newPasswordEditText);
        confirmPasswordEdit = findViewById(R.id.confirmPasswordEditText);
        updatePasswordBtn = findViewById(R.id.changePasswordButton);
        progressBar = findViewById(R.id.progressBar);
        backButton = findViewById(R.id.backBtn);
        auth = FirebaseAuth.getInstance();
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        updatePasswordBtn.setOnClickListener(v -> {
            if (validateInputs()) {
                updatePassword();
            }
        });
    }

    private boolean validateInputs() {
        String currentPassword = currentPasswordEdit.getText().toString().trim();
        String newPassword = newPasswordEdit.getText().toString().trim();
        String confirmPassword = confirmPasswordEdit.getText().toString().trim();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, getString(R.string.password_empty), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, getString(R.string.password_short), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, getString(R.string.password_mismatch), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (newPassword.equals(currentPassword)) {
            Toast.makeText(this, getString(R.string.password_same), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void updatePassword() {
        String currentPassword = currentPasswordEdit.getText().toString().trim();
        String newPassword = newPasswordEdit.getText().toString().trim();

        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

            user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        user.updatePassword(newPassword)
                            .addOnCompleteListener(updateTask -> {
                                if (updateTask.isSuccessful()) {
                                    Toast.makeText(ChangePasswordActivity.this, 
                                        getString(R.string.password_changed), Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(ChangePasswordActivity.this, 
                                        getString(R.string.password_change_failed), Toast.LENGTH_SHORT).show();
                                }
                            });
                    } else {
                        Toast.makeText(ChangePasswordActivity.this, 
                            getString(R.string.password_incorrect), Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }
} 