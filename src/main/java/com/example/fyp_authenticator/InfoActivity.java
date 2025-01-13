package com.example.fyp_authenticator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.fyp_authenticator.Model.ApplicationModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class InfoActivity extends AppCompatActivity {

    ImageView backIcon, logIcon, deleteIcon;
    TextInputLayout appNameLayout, infoLayout, pwdLayout;
    AutoCompleteTextView autoTextview, SHADropDown;
    Button saveBut;

    FirebaseAuth fAuth;
    DatabaseReference reference_connects, reference_keys, reference_logs;

    String UID, OTP_TYPE, CONNECTID, IdKey_GET, SHA_TYPE;
    long hotp_counter;
    String[] item = {"TOTP", "HOTP (Counter-based)"};
    String[] item_SHA = {"SHA-1", "SHA-256", "SHA-384", "SHA-512"};
    boolean isSaved = false;

    ArrayAdapter<String> adapterItem, SHAadapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        backIcon = findViewById(R.id.backIcon_info);
        logIcon = findViewById(R.id.logIcon_info);
        deleteIcon = findViewById(R.id.deleteIcon_info);
        appNameLayout = findViewById(R.id.appNameLayout_info);
        infoLayout = findViewById(R.id.infoLayout_info);
        pwdLayout = findViewById(R.id.pwdLayout_info);
        autoTextview = findViewById(R.id.autoTextview_info);
        SHADropDown = findViewById(R.id.shaTextview_info);
        saveBut = findViewById(R.id.saveBut_info);

        Intent intent = getIntent();
        IdKey_GET = intent.getStringExtra("IdKey");
        Random random = new Random();
        hotp_counter = random.nextInt(100) + 1L;

        fAuth = FirebaseAuth.getInstance();
        UID = fAuth.getCurrentUser().getUid();
        reference_connects = FirebaseDatabase.getInstance().getReference("Connects");
        reference_keys = FirebaseDatabase.getInstance().getReference("Keys");
        reference_logs = FirebaseDatabase.getInstance().getReference("Logs");

        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        logIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent logIntent = new Intent(getApplicationContext(), LogActivity.class);
                logIntent.putExtra("IdKey", IdKey_GET);
                startActivity(logIntent);
            }
        });

        deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disconnectApp();
            }
        });

        saveBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });
        pwdLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String generatedPwd = generateStrongPassword(12);
                pwdLayout.getEditText().setText(generatedPwd);
            }
        });



        //otp type drop box
        adapterItem = new ArrayAdapter<String>(this, R.layout.list_item, item);
        autoTextview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapterView.getItemAtPosition(i).toString();
                OTP_TYPE = item;
            }
        });

        //sha type drop box
        SHAadapter = new ArrayAdapter<String>(this, R.layout.list_item, item_SHA);
        SHADropDown.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapterView.getItemAtPosition(i).toString();
                SHA_TYPE = item;
            }
        });


        loadData(IdKey_GET);
    }

    private void loadData(String IDKEY) {
        byte[] convertedbyteArray = convertTo16Bytes(IdKey_GET);

        reference_connects.child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    ApplicationModel eachConnection = childSnapshot.getValue(ApplicationModel.class);
                    if (eachConnection.getIdKey().equals(IDKEY)) {
                        appNameLayout.getEditText().setText(eachConnection.getAppName());

                        String decrypted_userinfo = decryptData(eachConnection.getUserInfo(),convertedbyteArray);
                        infoLayout.getEditText().setText(decrypted_userinfo);

                        autoTextview.setText(eachConnection.getType());
                        OTP_TYPE = eachConnection.getType();
                        autoTextview.setAdapter(adapterItem);

                        SHADropDown.setText(eachConnection.getSHAalgo());
                        SHA_TYPE = eachConnection.getSHAalgo();
                        SHADropDown.setAdapter(SHAadapter);

                        String PWD = (childSnapshot.child("pwd").getValue(String.class) != null) ? childSnapshot.child("pwd").getValue(String.class) : "-";
                        if(!PWD.equals("-")){
                            String decrypted_pwd = decryptData(PWD,convertedbyteArray);
                            pwdLayout.getEditText().setText(decrypted_pwd);
                        }else{
                            pwdLayout.getEditText().setText(PWD);
                        }


                        CONNECTID = childSnapshot.getKey();
                        hotp_counter = eachConnection.getCounter();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void saveData() {
        String info_update = infoLayout.getEditText().getText().toString();
        String pwd_update = pwdLayout.getEditText().getText().toString();

        byte[] convertedbyteArray = convertTo16Bytes(IdKey_GET);
        String encrypted_userinfo = encryptData(info_update,convertedbyteArray);
        String encrypted_pwd = encryptData(pwd_update,convertedbyteArray);

        Map<String, Object> connectInfo = new HashMap<>();
        connectInfo.put("userInfo", encrypted_userinfo);
        connectInfo.put("pwd", encrypted_pwd);
        connectInfo.put("type", OTP_TYPE);
        connectInfo.put("SHAalgo", SHA_TYPE);
        connectInfo.put("counter",hotp_counter);
        reference_connects.child(fAuth.getCurrentUser().getUid()).child(CONNECTID).updateChildren(connectInfo)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Map<String, Object> keyInfo_update = new HashMap<>();
                            keyInfo_update.put("type", OTP_TYPE);
                            keyInfo_update.put("SHAalgo", SHA_TYPE);
                            keyInfo_update.put("counter",hotp_counter);
                            reference_keys.child(IdKey_GET).updateChildren(keyInfo_update);
                            isSaved = true;
                            Toast.makeText(InfoActivity.this, "Save info successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(InfoActivity.this, "Failed to save info", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    private void disconnectApp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Disconnect this application?");
        builder.setMessage("This action is irreversible.");
        builder.setPositiveButton("Confirm",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        reference_keys.child(IdKey_GET).child("isConnected").setValue(false);
                        reference_connects.child(fAuth.getCurrentUser().getUid()).child(CONNECTID).removeValue();
                        reference_logs.child(IdKey_GET).removeValue();

                        finish();
                        startActivity(new Intent(getApplicationContext(), DashboardActivity.class));

                    }
                });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private String encryptData(String cipher, byte[] KEY) {
        try {
            SecretKey secretKey = new SecretKeySpec(KEY, "AES");
            Cipher cipherObj = Cipher.getInstance("AES");
            cipherObj.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipherObj.doFinal(cipher.getBytes());
            // Convert encrypted bytes to Base64 for storage or transmission
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String decryptData(String encryptedPwd, byte[] KEY) {
        try {
            SecretKey secretKey = new SecretKeySpec(KEY, "AES");
            Cipher cipherObj = Cipher.getInstance("AES");
            cipherObj.init(Cipher.DECRYPT_MODE, secretKey);

            // Decode Base64 string to byte array
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedPwd);

            // Decrypt the data
            byte[] decryptedBytes = cipherObj.doFinal(encryptedBytes);

            // Convert decrypted bytes to String
            return new String(decryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] convertTo16Bytes(String input) {
        // Convert input string to bytes using UTF-8 encoding
        byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);

        // Create a new byte array of length 16
        byte[] result = new byte[16];

        // Copy up to 16 bytes from inputBytes to result
        System.arraycopy(inputBytes, 0, result, 0, Math.min(inputBytes.length, 16));

        // If inputBytes is less than 16 bytes, pad with zeros
        if (inputBytes.length < 16) {
            Arrays.fill(result, inputBytes.length, 16, (byte) 0);
        }

        return result;
    }

    private String generateStrongPassword(int length) {
        final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            password.append(CHARACTERS.charAt(randomIndex));
        }

        return password.toString();
    }
}