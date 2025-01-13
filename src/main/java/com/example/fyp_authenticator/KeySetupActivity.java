package com.example.fyp_authenticator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class KeySetupActivity extends AppCompatActivity {
    String[] item = {"TOTP", "HOTP (Counter-based)"};
    String[] item_SHA = {"SHA-1", "SHA-256","SHA-384","SHA-512"};
    AutoCompleteTextView autoCompleteTextView,SHADropDown;
    ArrayAdapter<String> adapterItem,SHAadapter;

    ImageView backIcon,doneIcon;
    TextView connectingText;
    ProgressBar progressBar;
    CardView connectingBox;
    Button connectBut;
    TextInputLayout infoInput, keycodeInput;
    DatabaseReference reference_key, reference_connect;
    FirebaseAuth fAuth;

    String OTP_TYPE,SHA_TYPE;
    Map<String, Object> keyInfo;
    ArrayList<String> appList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keysetup);

        backIcon = findViewById(R.id.backIcon_keysetup);
        connectBut = findViewById(R.id.connectBut_keysetup);
        infoInput = findViewById(R.id.infoLayout_keysetup);
        keycodeInput = findViewById(R.id.keycodeLayout_keysetup);
        connectingBox = findViewById(R.id.connectingBox_keysetup);
        connectingText = findViewById(R.id.connectingText_keysetup);
        doneIcon = findViewById(R.id.doneIcon_keysetup);
        progressBar = findViewById(R.id.progressBar_keysetup);

        reference_key = FirebaseDatabase.getInstance().getReference("Keys");
        reference_connect = FirebaseDatabase.getInstance().getReference("Connects");
        fAuth = FirebaseAuth.getInstance();
        OTP_TYPE = "TOTP";
        SHA_TYPE = "SHA-1";
        keyInfo = new HashMap<>();
        doneIcon.setVisibility(View.GONE);
        connectingBox.setVisibility(View.GONE);
        appList = new ArrayList<>();

        Intent intent = getIntent();
        String secretKey_get = intent.getStringExtra("secretkey");
        keycodeInput.getEditText().setText(secretKey_get);

        getApplist();

        //otp type drop box
        autoCompleteTextView = findViewById(R.id.autoTextview_keysetup);
        adapterItem = new ArrayAdapter<String>(this, R.layout.list_item, item);
        autoCompleteTextView.setText(item[0]);
        autoCompleteTextView.setAdapter(adapterItem);

        //sha type drop box
        SHADropDown = findViewById(R.id.shaTextview_keysetup);
        SHAadapter = new ArrayAdapter<String>(this, R.layout.list_item, item_SHA);
        SHADropDown.setText(item_SHA[0]);
        SHADropDown.setAdapter(SHAadapter);

        SHADropDown.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapterView.getItemAtPosition(i).toString();
                SHA_TYPE = item;
            }
        });
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapterView.getItemAtPosition(i).toString();
                OTP_TYPE = item;
            }
        });

        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                //startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
            }
        });
        connectBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String keyCode = keycodeInput.getEditText().getText().toString().trim();

                checkConnections(keyCode);
            }
        });
    }


    private void connectApp() {
        String userInfo = infoInput.getEditText().getText().toString();
        String keyCode = keycodeInput.getEditText().getText().toString().trim();

        byte[] convertedbyteArray = convertTo16Bytes(keyInfo.get("idKey").toString());
        String encrypted_userinfo = encryptData(userInfo,convertedbyteArray);
        String encrypted_connectKey = encryptData(keyCode,convertedbyteArray);

        Random random = new Random();
        long randomCounter = random.nextInt(100) + 1L; // Adding 1 to avoid 0, between 1 and 100
        connectingBox.setVisibility(View.VISIBLE);

        long timestamp = System.currentTimeMillis();
        Map<String, Object> connectInfo = new HashMap<>();
        connectInfo.put("userID", fAuth.getCurrentUser().getUid());
        connectInfo.put("userInfo", encrypted_userinfo);
        connectInfo.put("connectKey", encrypted_connectKey);
        connectInfo.put("webEmail", keyInfo.get("webEmail").toString());
        connectInfo.put("UIDweb", keyInfo.get("UIDweb").toString());
        connectInfo.put("idKey", keyInfo.get("idKey").toString());
        connectInfo.put("appName", keyInfo.get("appName").toString());
        connectInfo.put("type", OTP_TYPE);
        connectInfo.put("SHAalgo", SHA_TYPE);
        connectInfo.put("timestamp", timestamp);
        connectInfo.put("counter", randomCounter);

        String pushKey = reference_connect.child(fAuth.getCurrentUser().getUid()).push().getKey();

        reference_connect.child(fAuth.getCurrentUser().getUid()).child(pushKey).setValue(connectInfo)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            doneIcon.setImageResource(R.drawable.baseline_check_24);
                            doneIcon.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            connectingText.setText("Connected");

                            reference_key.child(keyInfo.get("idKey").toString()).child("isConnected").setValue(true);
                            reference_key.child(keyInfo.get("idKey").toString()).child("isConnected").setValue(true);
                            reference_key.child(keyInfo.get("idKey").toString()).child("type").setValue(OTP_TYPE);
                            reference_key.child(keyInfo.get("idKey").toString()).child("SHAalgo").setValue(SHA_TYPE);
                            reference_key.child(keyInfo.get("idKey").toString()).child("counter").setValue(randomCounter);
                        } else {
                            doneIcon.setImageResource(R.drawable.baseline_close_24);
                            doneIcon.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            connectingText.setText("Failed to connect");
                        }
                        // Create a Handler
                        Handler handler = new Handler();

                        // Post a delayed runnable to hide the View after 2 seconds
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Hide the View after 2 seconds
                                connectingBox.setVisibility(View.GONE);
                                finish();
                                //startActivity(new Intent(getApplicationContext(), DashboardActivity.class));

                            }
                        }, 2000); // 2000 milliseconds = 2 seconds
                    }
                });

    }


    private void checkConnections(String val) {
        String userInfo = infoInput.getEditText().getText().toString().trim();
        if (!validateInfo(userInfo) | !validateKey(val)) {
            return;
        }

        reference_key.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isConnected = false;
                boolean isExist = false;
                if (snapshot.exists()) {
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        String CONNECTKEY_GET = childSnapshot.child("idKey").getValue(String.class);
                        String KEY_GET = childSnapshot.child("connectKey").getValue(String.class);
                        byte[] convertedbyteArray = convertTo16Bytes(CONNECTKEY_GET);
                        String decrypted_connectKey = decryptData(KEY_GET,convertedbyteArray);
                        boolean ISCONNECTED_GET = childSnapshot.child("isConnected").getValue(Boolean.class);

                        if(decrypted_connectKey.equals(val)){
                            keyInfo.put("appName", childSnapshot.child("appName").getValue(String.class));
                            keyInfo.put("webEmail", childSnapshot.child("webEmail").getValue(String.class));
                            keyInfo.put("UIDweb", childSnapshot.child("UIDweb").getValue(String.class));
                            keyInfo.put("connectKey", childSnapshot.child("connectKey").getValue(String.class));
                            keyInfo.put("idKey", childSnapshot.child("idKey").getValue(String.class));

                            if(ISCONNECTED_GET){
                               isConnected = true;
                            }
                            isExist = true;

                        }
                    }
                }

                boolean isExistAppName = false;

                if(isExist){
                    if(isConnected){
                        Toast.makeText(KeySetupActivity.this, "The key has connected", Toast.LENGTH_SHORT).show();
                    }else{

                        if(appList.contains(keyInfo.get("appName").toString())){
                            Toast.makeText(KeySetupActivity.this, "Can't connect multiple accounts for an app", Toast.LENGTH_SHORT).show();
                            isExistAppName  =true;
                        }

                        if(!isExistAppName){
                            connectApp();
                        }
                    }
                }else{
                    keycodeInput.setError("Please enter a valid key.");
                    Toast.makeText(KeySetupActivity.this, "The key is not valid", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getApplist(){
        reference_connect.child(fAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        String APPNAME_GET = childSnapshot.child("appName").getValue(String.class);
                        appList.add(APPNAME_GET);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    /*
    private void checkKey(String val) {
        String userInfo = infoInput.getEditText().getText().toString().trim();
        ArrayList<String> justNameList = new ArrayList<>();
        ArrayList<String> justKeysList = new ArrayList<>();


        reference_key.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    boolean isValid = false;


                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        String KEY_GET = childSnapshot.child("key").getValue(String.class);

                        if(KEY_GET.equals(val)){
                            System.out.println("isValid");
                            keyInfo.put("webEmail", childSnapshot.child("email").getValue(String.class));
                            keyInfo.put("UIDweb", childSnapshot.child("UIDweb").getValue(String.class));
                            keyInfo.put("appName", childSnapshot.child("appName").getValue(String.class));
                            keyInfo.put("key", childSnapshot.child("key").getValue(String.class));
                            keyInfo.put("idkey", childSnapshot.child("idkey").getValue(String.class));
                            isValid = true;
                        }
                    }

                    if(isValid){
                        connectApp();
                    }else{
                        keycodeInput.setError("Please enter a valid key.");
                        Toast.makeText(KeySetupActivity.this, "The key is not valid", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
*/
    private Boolean validateInfo(String val) {
        String noWhiteSpace = "\\A\\w{1,16}\\z";

        if (val.length() == 0) {
            infoInput.setError("Please enter your user info.");
            return false;
        } else {
            infoInput.setError(null);
            infoInput.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validateKey(String val) {
        //String passwordPattern = "^"+"(?=.*[a-zA-Z])"+"(?=\\s+$)"+".{3,25}"+"$";
        String noWhiteSpace = "\\A\\w{1,15}\\z";

        if (val.length() == 0) {
            keycodeInput.setError("Please enter the provided key.");
            return false;
        } else {
            keycodeInput.setError(null);
            keycodeInput.setErrorEnabled(false);
            return true;
        }
    }

    private String encryptData(String cipher, byte[] KEY) {
        try {
            SecretKey secretKey = new SecretKeySpec(KEY, "AES");
            Cipher cipherObj = Cipher.getInstance("AES");
            cipherObj.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipherObj.doFinal(cipher.getBytes());
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
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedPwd);
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
}