package com.example.fyp_authenticator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.content.Intent;
import android.hardware.biometrics.BiometricManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;


import com.example.fyp_authenticator.Utilities.SessionManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {
    Button loginBut, signupBut;
    TextInputLayout emailLayout, pwdLayout;
    RelativeLayout progressBarLayout;

    FirebaseAuth fAuth;

    BiometricPrompt biometricPrompt;
    BiometricPrompt.PromptInfo promptInfo;

    DatabaseReference reference_users;
    String PASSWORD_SIGN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //remove bar
        //getSupportActionBar().hide();

        signupBut = findViewById(R.id.signupBut_login);
        loginBut = findViewById(R.id.loginBut_login);
        emailLayout = findViewById(R.id.emailLayout_login);
        pwdLayout = findViewById(R.id.pwdLayout_login);
        progressBarLayout = findViewById(R.id.progressBar_login);

        fAuth = FirebaseAuth.getInstance();
        //default
        progressBarLayout.setVisibility(View.GONE);
        //System.out.println(hashString("123456"));

        checkIsKeeplogged("","");

        signupBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SignupActivity.class));
            }
        });
        loginBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String EMAIL = emailLayout.getEditText().getText().toString().trim().toLowerCase();
                String PASSWORD = pwdLayout.getEditText().getText().toString().trim();
                checkIsKeeplogged(EMAIL,PASSWORD);
                //loginUser(EMAIL, PASSWORD);
            }
        });

    }

    private Boolean validateEMAIL(String val) {
        String noWhiteSpace = "(?=\\s+$)";
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if (val.length() == 0) {
            emailLayout.setError("Cannot be empty");
            return false;
        }
        if (!val.matches(emailPattern)) {
            emailLayout.setError("Wrong email format?");
            return false;

        } else if (val.matches(noWhiteSpace)) {
            emailLayout.setError("No white spaces");
            return false;
        } else {
            emailLayout.setError(null);
            emailLayout.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validatePassword(String val) {
        //String passwordPattern = "^"+"(?=.*[a-zA-Z])"+"(?=\\s+$)"+".{3,25}"+"$";
        String noWhiteSpace = "\\A\\w{1,15}\\z";

        if (val.length() <= 5) {
            pwdLayout.setError("At least 6 characters");
            return false;
        } else if (!val.matches(noWhiteSpace)) {
            pwdLayout.setError("No White Spaces");
            return false;
        } else {
            pwdLayout.setError(null);
            pwdLayout.setErrorEnabled(false);
            return true;
        }
    }


    private void loginUser(String EMAIL, String PASSWORD) {
        progressBarLayout.setVisibility(View.VISIBLE);
        PASSWORD_SIGN = "";

        reference_users = FirebaseDatabase.getInstance().getReference("Users");
        reference_users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isAuthorised = false;
                if (snapshot.exists()) {
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        String EMAIL_GET = childSnapshot.child("email").getValue(String.class);
                        String PASSWORD_GET = childSnapshot.child("password").getValue(String.class);

                        if (EMAIL_GET.equals(EMAIL) && BCrypt.checkpw(PASSWORD, PASSWORD_GET)) {
                            isAuthorised = true;
                            PASSWORD_SIGN = PASSWORD_GET;
                        }
                    }
                }

                if (isAuthorised) {
                    fAuth.signInWithEmailAndPassword(EMAIL, PASSWORD_SIGN).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                            SessionManager sm = new SessionManager(getApplicationContext(), SessionManager.SESSION_KEEPLOGGED);
                            sm.insertData(EMAIL, PASSWORD);
                            startActivity(intent);
                            progressBarLayout.setVisibility(View.GONE);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Wrong FAUTH", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    emailLayout.setError("Incorrect email or password");
                    pwdLayout.setError("Incorrect email or password");
                }

                progressBarLayout.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkIsKeeplogged(String EMAIL,String PWD) {
        SessionManager sm = new SessionManager(getApplicationContext(), SessionManager.SESSION_KEEPLOGGED);
        SessionManager sm_ba = new SessionManager(getApplicationContext(), SessionManager.SESSION_BIOMETRIC);

        if (sm.checkIsLoggedIn()) {
            HashMap<String, String> keepLogDetails = sm.getData();
            String sessionEMAIL = keepLogDetails.get(SessionManager.KEY_EMAIL);
            String sessionPWD = keepLogDetails.get(SessionManager.KEY_PASSWORD);


            if (sm_ba.checkIsBiometricEnabled() && sm_ba.getBAEmail().equals(sessionEMAIL)) {
                //BA
                androidx.biometric.BiometricManager biometricManager = androidx.biometric.BiometricManager.from(this);
                switch (biometricManager.canAuthenticate()) {
                    case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                        Toast.makeText(getApplicationContext(), "No BA device detected", Toast.LENGTH_SHORT).show();

                        break;
                    case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                        Toast.makeText(getApplicationContext(), "Error BA not available", Toast.LENGTH_SHORT).show();

                        break;
                    case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                        Toast.makeText(getApplicationContext(), "Error BA not enrolled", Toast.LENGTH_SHORT).show();

                        break;
                }

                Executor executor = ContextCompat.getMainExecutor(this);
                biometricPrompt = new androidx.biometric.BiometricPrompt(LoginActivity.this, executor, new androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull androidx.biometric.BiometricPrompt.AuthenticationResult result) {
                        loginUser(sessionEMAIL, sessionPWD);

                        super.onAuthenticationSucceeded(result);
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                    }
                });

                promptInfo = new BiometricPrompt.PromptInfo.Builder().setTitle("FYP_Authenticator").
                        setDescription("Setup your biometric authentication")
                        .setDeviceCredentialAllowed(true)
                        .setConfirmationRequired(true)
                        .build();
                biometricPrompt.authenticate(promptInfo);

            } else if (!sm_ba.checkIsBiometricEnabled()) {
                loginUser(sessionEMAIL, sessionPWD);
            }
        }else{
            if(EMAIL.isEmpty() | PWD.isEmpty()){
                return;
            }
            if (!validateEMAIL(EMAIL) | !validatePassword(PWD)) {
                progressBarLayout.setVisibility(View.GONE);
                return;
            }

            if(!sm_ba.checkIsBiometricEnabled()){
                loginUser(EMAIL, PWD);
            }else if(sm_ba.checkIsBiometricEnabled() && !sm_ba.getBAEmail().equals(EMAIL)){
                loginUser(EMAIL, PWD);
            }else if(sm_ba.checkIsBiometricEnabled() && sm_ba.getBAEmail().equals(EMAIL)){
                //BA
                androidx.biometric.BiometricManager biometricManager = androidx.biometric.BiometricManager.from(this);
                switch (biometricManager.canAuthenticate()) {
                    case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                        Toast.makeText(getApplicationContext(), "No BA device detected", Toast.LENGTH_SHORT).show();

                        break;
                    case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                        Toast.makeText(getApplicationContext(), "Error BA not available", Toast.LENGTH_SHORT).show();

                        break;
                    case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                        Toast.makeText(getApplicationContext(), "Error BA not enrolled", Toast.LENGTH_SHORT).show();

                        break;
                }

                Executor executor = ContextCompat.getMainExecutor(this);
                biometricPrompt = new androidx.biometric.BiometricPrompt(LoginActivity.this, executor, new androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull androidx.biometric.BiometricPrompt.AuthenticationResult result) {
                        loginUser(EMAIL, PWD);

                        super.onAuthenticationSucceeded(result);
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                    }
                });

                promptInfo = new BiometricPrompt.PromptInfo.Builder().setTitle("FYP_Authenticator").
                        setDescription("Setup your biometric authentication")
                        .setDeviceCredentialAllowed(true)
                        .setConfirmationRequired(true)
                        .build();
                biometricPrompt.authenticate(promptInfo);
            }
        }

    }
}