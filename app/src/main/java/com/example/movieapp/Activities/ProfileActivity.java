package com.example.movieapp.Activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;

import com.example.movieapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.movieapp.Utils.ThemeHelper;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {
    private TextView userNameText, userEmailText;
    private CardView changePasswordCard, privacyPolicyCard, termsCard, contactCard, logoutCard;
    private SwitchCompat darkModeSwitch, languageSwitch;
    
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Dil ayarını uygula
        SharedPreferences sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE);
        boolean isTurkish = sharedPreferences.getBoolean("isTurkish", false);
        Locale locale = isTurkish ? new Locale("tr") : new Locale("en");
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.setLocale(locale);
        res.updateConfiguration(conf, dm);

        setContentView(R.layout.activity_profile);
        initializeViews();
        setupFirebase();
        displayUserInfo();
        setupClickListeners();
        setupDarkModeSwitch();
        setupLanguageSwitch();
    }

    private void initializeViews() {
        userNameText = findViewById(R.id.profileUserName);
        userEmailText = findViewById(R.id.profileUserEmail);
        changePasswordCard = findViewById(R.id.changePasswordCard);
        privacyPolicyCard = findViewById(R.id.privacyPolicyCard);
        termsCard = findViewById(R.id.termsCard);
        contactCard = findViewById(R.id.contactCard);
        logoutCard = findViewById(R.id.logoutCard);
        darkModeSwitch = findViewById(R.id.darkModeSwitch);
        languageSwitch = findViewById(R.id.languageSwitch);
    }

    private void setupFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void displayUserInfo() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            userEmailText.setText(user.getEmail());

            db.collection("Users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String surname = documentSnapshot.getString("surname");
                            if (name != null && surname != null) {
                                userNameText.setText(name + " " + surname);
                            }
                        }
                    });
        }
    }

    private void setupClickListeners() {
        changePasswordCard.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, ChangePasswordActivity.class));
        });

        privacyPolicyCard.setOnClickListener(v -> {
            Toast.makeText(this, "Gizlilik Politikası yakında!", Toast.LENGTH_SHORT).show();
        });

        termsCard.setOnClickListener(v -> {
            Toast.makeText(this, "Hizmet Şartları yakında!", Toast.LENGTH_SHORT).show();
        });

        contactCard.setOnClickListener(v -> {
            Toast.makeText(this, "Yakında bizimle iletişime geçin!", Toast.LENGTH_SHORT).show();
        });

        logoutCard.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
        });

        languageSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(this, "Language: " + (isChecked ? "EN" : "TR"), 
                         Toast.LENGTH_SHORT).show();
            // Dil değiştirme işlevselliği daha sonra eklenecek
        });
    }

    private void setupDarkModeSwitch() {
        // Dark mode durumunu kontrol et ve switch'i ayarla
        boolean isDarkMode = ThemeHelper.isDarkMode(this);
        darkModeSwitch.setChecked(isDarkMode);


        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ThemeHelper.saveThemeMode(this, isChecked);
            ThemeHelper.applyTheme(isChecked);
            recreate(); // Aktif temayı uygulamak için aktiviteyi yeniden oluştur
        });
    }

    private void setupLanguageSwitch() {
        SwitchCompat languageSwitch = findViewById(R.id.languageSwitch);
        SharedPreferences sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE);
        boolean isTurkish = sharedPreferences.getBoolean("isTurkish", false);
        languageSwitch.setChecked(isTurkish);

        languageSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isTurkish", isChecked);
            editor.apply();

            // Dil ayarını
            Locale newLocale = isChecked ? new Locale("tr") : new Locale("en");
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            conf.setLocale(newLocale);
            res.updateConfiguration(conf, dm);

            // MainActivity'yi yeniden başlatt
            Intent refresh = new Intent(this, MainActivity.class);
            refresh.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(refresh);
            finish();
        });
    }
} 