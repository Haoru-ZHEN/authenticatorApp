package com.example.fyp_authenticator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {
    TextInputLayout emailLayout, phoneLayout, pwdLayout;
    ImageView backIcon;
    Button signupBut;
    RelativeLayout progressBarLayout;

    FirebaseAuth fAuth;
    DatabaseReference reference_users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        emailLayout = findViewById(R.id.emailLayout_signup);
        phoneLayout = findViewById(R.id.phoneLayout_signup);
        pwdLayout = findViewById(R.id.pwdLayout_signup);
        backIcon = findViewById(R.id.backIcon_signup);
        signupBut = findViewById(R.id.registerBut_signup);
        progressBarLayout = findViewById(R.id.progressBar_signup);

        fAuth = FirebaseAuth.getInstance();

        //default
        progressBarLayout.setVisibility(View.GONE);

        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });
        signupBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String EMAIL = emailLayout.getEditText().getText().toString().trim().toLowerCase();
                String USERNAME = phoneLayout.getEditText().getText().toString().trim();
                String PASSWORD = pwdLayout.getEditText().getText().toString().trim();

                registerUser(EMAIL, USERNAME, PASSWORD);
            }
        });
    }

    private String hashString(String HASHINPUT){
        // Generate a salt for hashing
        String salt = BCrypt.gensalt();
        // Hash the password using the salt
        String hashedPassword = BCrypt.hashpw(HASHINPUT, salt);
        return hashedPassword;
    }

    private void registerUser(String EMAIL, String PHONE, String PASSWORD) {
        progressBarLayout.setVisibility(View.VISIBLE);
        String HASH_OUTPUT = hashString(PASSWORD);

        if (!validateEMAIL(EMAIL) | !validatePassword(PASSWORD) | !validatePHONE(PHONE)) {
            progressBarLayout.setVisibility(View.GONE);
            return;
        }

        fAuth.createUserWithEmailAndPassword(EMAIL, HASH_OUTPUT).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("email", EMAIL);
                userInfo.put("phone", PHONE);
                userInfo.put("password", HASH_OUTPUT);
                userInfo.put("UID", fAuth.getCurrentUser().getUid());
                reference_users = FirebaseDatabase.getInstance().getReference("Users");

                reference_users.child(fAuth.getCurrentUser().getUid()).setValue(userInfo)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(SignupActivity.this, "Sign up successfully", Toast.LENGTH_SHORT).show();

                            }
                        });

                //Toast.makeText(SignUpActivity.this,"Succeed",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //StyleableToast.makeText(getApplicationContext(),"Failed. "+e.getMessage(),R.style.mytoast).show();
                Toast.makeText(SignupActivity.this, "Failed to sign up", Toast.LENGTH_SHORT).show();
                progressBarLayout.setVisibility(View.GONE);
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

    private Boolean validatePHONE(String val) {
        String noWhiteSpace = "\\A\\w{1,16}\\z";
        if (val.length() == 0) {
            phoneLayout.setError("Cannot be empty");
            return false;
        }
        if (!val.matches(noWhiteSpace)) {
            phoneLayout.setError("Digit only");
            return false;
        } else {
            phoneLayout.setError(null);
            phoneLayout.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validatePassword(String val) {
        //String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
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

}