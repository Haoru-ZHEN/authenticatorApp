package com.example.fyp_authenticator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;

public class QRSetupActivity extends AppCompatActivity {

    CodeScannerView codeScannerView;
    ImageView backIcon;
    private CodeScanner mCodeScanner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrsetup);

        codeScannerView = findViewById(R.id.scannerView_qrsetup);
        backIcon = findViewById(R.id.backIcon_qrsetup);

        mCodeScanner = new CodeScanner(this, codeScannerView);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(checkIsSecretKey(result.getText())){
                            //Toast.makeText(QRSetupActivity.this, result.getText(), Toast.LENGTH_SHORT).show();
                            Intent keysetupIntent = new Intent(getApplicationContext(), KeySetupActivity.class);
                            keysetupIntent.putExtra("secretkey", result.getText());
                            startActivity(keysetupIntent);
                        }else{
                            Toast.makeText(QRSetupActivity.this, "Improper key format", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });
        codeScannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });

        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        int Camera_RQ =102;
        CheckForPermissions(android.Manifest.permission.CAMERA,"Camera",Camera_RQ);
    }

    private boolean checkIsSecretKey(String KEY){
        if(KEY.length() == 8){
            return true;
        }else{
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }

    private void CheckForPermissions(String Permission, String Name, int RequestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Permission) == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(getApplicationContext(), Name + " permission is granted", Toast.LENGTH_SHORT).show();
            } else if (shouldShowRequestPermissionRationale(Permission)) {
                showDialog(Permission, Name, RequestCode);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Permission}, RequestCode);
            }
        }
    }

    private void showDialog(String permission, String name, int requestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Permission to access your " + name + " is required to use this app")
                .setTitle("Permission Required")
                .setPositiveButton("ok", (dialog, which) -> {
                    ActivityCompat.requestPermissions(QRSetupActivity.this, new String[]{permission}, requestCode);
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


}