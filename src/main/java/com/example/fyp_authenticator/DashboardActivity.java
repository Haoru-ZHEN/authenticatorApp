package com.example.fyp_authenticator;


import static java.security.AccessController.getContext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.hardware.biometrics.BiometricManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fyp_authenticator.Adapter.ApplicationAdapter;
import com.example.fyp_authenticator.Model.ApplicationModel;
import com.example.fyp_authenticator.Utilities.SessionManager;
import com.example.fyp_authenticator.Utilities.TOTP_Algo;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.concurrent.Executor;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


public class DashboardActivity extends AppCompatActivity {

    FloatingActionButton addBut;
    RecyclerView recView;

    ArrayList<ApplicationModel> connectionAppList;
    ApplicationAdapter applicationAdapter;

    FirebaseAuth fAuth;
    DatabaseReference reference_connects;

    String UID;
    boolean isBAAvailable = true;
    SessionManager sm;

    androidx.biometric.BiometricPrompt biometricPrompt;
    androidx.biometric.BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar_item);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Applications");

        addBut = findViewById(R.id.floatingAddBut_dashboard);
        recView = findViewById(R.id.recView_dashboard);

        //default
        connectionAppList = new ArrayList<>();
        recView.setHasFixedSize(true);
        recView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        applicationAdapter = new ApplicationAdapter(getApplicationContext(), connectionAppList);
        recView.setAdapter(applicationAdapter);
        sm = new SessionManager(getApplicationContext(), SessionManager.SESSION_KEEPLOGGED);


        fAuth = FirebaseAuth.getInstance();
        UID = fAuth.getCurrentUser().getUid();
        reference_connects = FirebaseDatabase.getInstance().getReference("Connects");
        BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();

        showConnectionsApp();

        addBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetFragment.show(getSupportFragmentManager(), "BottomSheetDialog");

            }
        });

        //BA
        androidx.biometric.BiometricManager biometricManager = androidx.biometric.BiometricManager.from(this);
        switch (biometricManager.canAuthenticate()){
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                isBAAvailable  = false;
                Toast.makeText(getApplicationContext(), "No BA device detected",Toast.LENGTH_SHORT).show();

                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                isBAAvailable  = false;
                Toast.makeText(getApplicationContext(), "Error BA not available",Toast.LENGTH_SHORT).show();

                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                isBAAvailable  = false;
                Toast.makeText(getApplicationContext(), "Error BA not enrolled",Toast.LENGTH_SHORT).show();

                break;
        }

        Executor executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new androidx.biometric.BiometricPrompt(DashboardActivity.this, executor, new androidx.biometric.BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull androidx.biometric.BiometricPrompt.AuthenticationResult result) {
                SessionManager sm = new SessionManager(getApplicationContext(), SessionManager.SESSION_BIOMETRIC);
                sm.insertSetupBAData(true);
                Toast.makeText(getApplicationContext(), "Setup successfully",Toast.LENGTH_SHORT).show();

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

    }


    private void showConnectionsApp() {
        reference_connects.child(UID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                connectionAppList.clear();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    ApplicationModel eachApp = childSnapshot.getValue(ApplicationModel.class);
                    connectionAppList.add(eachApp);
                }
                applicationAdapter.removeallhandler();
                applicationAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.setupBA_menu){
            if(isBAAvailable){
                biometricPrompt.authenticate(promptInfo);
            }else{
                Toast.makeText(this, "Biometric Authentication is not available for your device",Toast.LENGTH_SHORT).show();

            }


        }else if(item.getItemId() == R.id.setting_menu){
            startActivity(new Intent(getApplicationContext(), SettingActivity.class));

        }else if(item.getItemId() == R.id.logout_menu){
            Toast.makeText(this, "Log out",Toast.LENGTH_SHORT).show();
            fAuth.signOut();
            sm.clearData();
            finish();
        }else{
            Toast.makeText(this, "others",Toast.LENGTH_SHORT).show();
        }

        return  true;
    }

}