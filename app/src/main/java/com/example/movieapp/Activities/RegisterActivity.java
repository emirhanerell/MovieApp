package com.example.movieapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.movieapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    TextInputEditText editTextName, editTextSurname, editTextEmail, editTextPassword;
    Button buttonRegister;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    ProgressBar progressBar;
    TextView loginView;

    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initcomponents();

        setupLoginButtonClick(); // login sayfasına yönlendir

        handleRegisterButtonClick();
    }

    private void handleRegisterButtonClick() {
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

                String name, surname, email, password;
                name = String.valueOf(editTextName.getText()).trim();
                surname = String.valueOf(editTextSurname.getText()).trim();
                email = String.valueOf(editTextEmail.getText()).trim();
                password = String.valueOf(editTextPassword.getText()).trim();

                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(RegisterActivity.this, "İsim giriniz", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if (TextUtils.isEmpty(surname)) {
                    Toast.makeText(RegisterActivity.this, "Soyisim giriniz", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(RegisterActivity.this, "Email giriniz", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if (TextUtils.isEmpty(password)){
                    Toast.makeText(RegisterActivity.this, "Şifre giriniz", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if (password.length() < 6){
                    Toast.makeText(RegisterActivity.this, "Şifre 6 karakterden küçük olamaz", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                // Firebase Authentication ile kullanıcı kaydı
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser(); // kayıt edilen kullanıcının bilgilerini al
                                    if (user != null) {
                                        // Kullanıcı bilgilerini Firestore'a kaydetme
                                        String userId = user.getUid();
                                        Map<String, Object> userMap = new HashMap<>();
                                        userMap.put("name", name);
                                        userMap.put("surname", surname);
                                        userMap.put("email", email);
                                        userMap.put("uid", userId);

                                        db.collection("Users").document(userId)
                                                .set(userMap)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) { // oluşturduğumuz userMap'i firebase e kaydeiypruz
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(RegisterActivity.this, "Kayıt başarılı!", Toast.LENGTH_SHORT).show();
                                                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        } else {
                                                            Toast.makeText(RegisterActivity.this, "Kayıt başarısız oldu!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }
                                } else {
                                    Toast.makeText(RegisterActivity.this, "Kayıt başarısız oldu", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    private void setupLoginButtonClick() {
        loginView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initcomponents() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Firestore bağlantısı

        editTextName = findViewById(R.id.nameTxt);
        editTextSurname = findViewById(R.id.surnameTxt);
        editTextEmail = findViewById(R.id.emailTxt);
        editTextPassword = findViewById(R.id.passwordTxt);
        buttonRegister = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progressBar);
        loginView = findViewById(R.id.loginNow);
    }
}
