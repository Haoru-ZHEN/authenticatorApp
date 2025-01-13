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
import com.example.fyp_authenticator.Model.LogModel;
import com.example.fyp_authenticator.R;
import com.example.fyp_authenticator.Utilities.TOTP_Algo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.myViewHolder> {
    private Context context;
    private ArrayList<LogModel> modelList;
    String totp = null;

    public LogAdapter(Context context, ArrayList<LogModel> modelList) {
        this.context = context;
        this.modelList = modelList;
    }

    @NonNull
    @Override
    public LogAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log, parent, false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogAdapter.myViewHolder holder, int position) {
        LogModel currentModel = modelList.get(position);
        holder.codeText.setText(currentModel.getOTP());
        holder.appNameText.setText(currentModel.getAppName());

        long timestamp = currentModel.getTimestamp()/1000; // to second
        Instant instant2 = Instant.ofEpochSecond(timestamp);
        LocalDateTime localDateTime = instant2.atZone(ZoneId.systemDefault()).toLocalDateTime();
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm");
        String formattedDateTime2 = localDateTime.format(formatter2);
        holder.dateText.setText(formattedDateTime2);
    }



    @Override
    public int getItemCount() {
        return modelList.size();
    }

    public class myViewHolder extends RecyclerView.ViewHolder {
        TextView appNameText, dateText, codeText;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            appNameText = itemView.findViewById(R.id.appname_log_item);
            dateText = itemView.findViewById(R.id.dateText_log_item);
            codeText = itemView.findViewById(R.id.codeText_log_item);

        }
    }
}
