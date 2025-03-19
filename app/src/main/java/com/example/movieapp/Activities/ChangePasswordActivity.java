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

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        currentPasswordEdit = findViewById(R.id.currentPasswordEdit);
        newPasswordEdit = findViewById(R.id.newPasswordEdit);
        confirmPasswordEdit = findViewById(R.id.confirmPasswordEdit);
        updatePasswordBtn = findViewById(R.id.updatePasswordBtn);
        progressBar = findViewById(R.id.progressBar);
        backButton = findViewById(R.id.backBtn);
        auth = FirebaseAuth.getInstance();
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        updatePasswordBtn.setOnClickListener(v -> {
            String currentPassword = currentPasswordEdit.getText().toString().trim();
            String newPassword = newPasswordEdit.getText().toString().trim();
            String confirmPassword = confirmPasswordEdit.getText().toString().trim();

            if (validateInputs(currentPassword, newPassword, confirmPassword)) {
                updatePassword(currentPassword, newPassword);
            }
        });
    }

    private boolean validateInputs(String currentPassword, String newPassword, String confirmPassword) {
        if (currentPassword.isEmpty()) {
            currentPasswordEdit.setError("Enter current password");
            return false;
        }

        if (newPassword.isEmpty()) {
            newPasswordEdit.setError("Enter new password");
            return false;
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordEdit.setError("Confirm new password");
            return false;
        }

        if (newPassword.length() < 6) {
            newPasswordEdit.setError("Password must be at least 6 characters");
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordEdit.setError("Passwords do not match");
            return false;
        }

        return true;
    }

    private void updatePassword(String currentPassword, String newPassword) {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseUser user = auth.getCurrentUser();
        
        if (user != null && user.getEmail() != null) {
            // Önce mevcut kimlik bilgilerini doğrula
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
            
            user.reauthenticate(credential)
                    .addOnSuccessListener(aVoid -> {
                        // Kimlik doğrulama başarılı, şifreyi güncelle
                        user.updatePassword(newPassword)
                                .addOnSuccessListener(aVoid1 -> {
                                    Toast.makeText(ChangePasswordActivity.this, 
                                            "Password updated successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(ChangePasswordActivity.this,
                                            "Failed to update password: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ChangePasswordActivity.this,
                                "Current password is incorrect",
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }
} 