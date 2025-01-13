package com.example.fyp_authenticator.Adapter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp_authenticator.DashboardActivity;
import com.example.fyp_authenticator.InfoActivity;
import com.example.fyp_authenticator.Model.ApplicationModel;
import com.example.fyp_authenticator.R;
import com.example.fyp_authenticator.Utilities.TOTP_Algo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.myViewHolder> {
    private Context context;
    private ArrayList<ApplicationModel> modelList;
    private List<Handler> handlerList = new ArrayList<>();
    String totp = null;

    public ApplicationAdapter(Context context, ArrayList<ApplicationModel> modelList) {
        this.context = context;
        this.modelList = modelList;
    }

    @NonNull
    @Override
    public ApplicationAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appotp, parent, false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ApplicationAdapter.myViewHolder holder, int position) {
        ApplicationModel currentModel = modelList.get(position);
        holder.appNameText.setText(currentModel.getAppName());
        byte[] convertedbyteArray = convertTo16Bytes(currentModel.getIdKey());
        String decrypted_userinfo = decryptData(currentModel.getUserInfo(),convertedbyteArray);
        holder.infoText.setText(decrypted_userinfo);

        //TOTP Generate
        // Encode the string into base64
        String decrypted_key = decryptData(currentModel.getConnectKey(),convertedbyteArray);
        String encodedString = Base64.getEncoder().encodeToString(decrypted_key.getBytes());
        int PERIOD_SECOND = 30000;
        String finalSHA = standardizeSHAFormat(currentModel.getSHAalgo());

        if(currentModel.getType().equals("TOTP")){
            holder.progressBar.setVisibility(View.VISIBLE);
            Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    holder.progressBar.setProgress(100);
                    ObjectAnimator.ofInt(holder.progressBar, "progress", 0).setDuration(PERIOD_SECOND).start();

                    totp = TOTP_Algo.generateTOTP(encodedString,finalSHA);


                    DatabaseReference reference_keys = FirebaseDatabase.getInstance().getReference("Keys");
                    reference_keys.child(currentModel.getIdKey()).child("OTP").setValue(totp);
                    // Insert a space between the third and fourth characters
                    String modifiedString = totp.toString().substring(0, 3) + " " + totp.toString().substring(3);

                    if(!holder.codeText.getText().toString().equals("*** ***")){
                        holder.codeText.setText(modifiedString);
                    }

                    handler.postDelayed(this, PERIOD_SECOND);

                }
            }); // 2000 milliseconds = 2 seconds
            handlerList.add(handler);
        }else{
            holder.progressBar.setVisibility(View.INVISIBLE);
            totp = TOTP_Algo.generateHOTP(encodedString,currentModel.getCounter(),finalSHA);

            DatabaseReference reference_keys = FirebaseDatabase.getInstance().getReference("Keys");
            reference_keys.child(currentModel.getIdKey()).child("OTP").setValue(totp);
            // Insert a space between the third and fourth characters
            String modifiedString = totp.toString().substring(0, 3) + " " + totp.toString().substring(3);

            if(!holder.codeText.getText().toString().equals("*** ***")){
                holder.codeText.setText(modifiedString);
            }
        }


        holder.fingerIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isDigitsOnly(holder.codeText.getText().toString())) {
                    holder.codeText.setText("*** ***");
                } else {
                    String modifiedString = totp.toString().substring(0, 3) + " " + totp.toString().substring(3);
                    holder.codeText.setText(modifiedString);
                }
            }
        });

        holder.containerLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent infoIntent = new Intent(context, InfoActivity.class);
                infoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                infoIntent.putExtra("IdKey", currentModel.getIdKey());
                context.startActivity(infoIntent);
                return false;
            }
        });


    }

    public void removeallhandler(){
        for (Handler handler : handlerList) {
            handler.removeCallbacksAndMessages(null);
        }
        handlerList.clear();
    }

    private String standardizeSHAFormat(String SHA_FORMAT){
        switch (SHA_FORMAT){
            case "SHA-1":
                return "HmacSHA1";
            case "SHA-256":
                return "HmacSHA256";
            case "SHA-384":
                return "HmacSHA384";
            case "SHA-512":
                return "HmacSHA512";
        }
        return "SHA-1";
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


    private boolean isDigitsOnly(String str) {
        String trimmedText = str.replace(" ", "");
        return trimmedText.matches("\\d+");
    }

    private boolean isStarsOnly(String str) {
        String trimmedText = str.replace(" ", "");
        return trimmedText.matches("[*]+");
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    public class myViewHolder extends RecyclerView.ViewHolder {
        TextView appNameText, infoText, codeText;
        ImageView imgApp, fingerIcon;
        ProgressBar progressBar;
        RelativeLayout containerLayout;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            appNameText = itemView.findViewById(R.id.appname_appotp_item);
            infoText = itemView.findViewById(R.id.info_appotp_item);
            codeText = itemView.findViewById(R.id.code_appotp_item);
            imgApp = itemView.findViewById(R.id.imgApp_appotp_item);
            fingerIcon = itemView.findViewById(R.id.fingerIcon_friend_item);
            progressBar = itemView.findViewById(R.id.progressBar_appotp_item);
            containerLayout = itemView.findViewById(R.id.layout_appotp_item);

        }
    }
}
