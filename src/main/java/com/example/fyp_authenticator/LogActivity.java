package com.example.fyp_authenticator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import com.example.fyp_authenticator.Adapter.ApplicationAdapter;
import com.example.fyp_authenticator.Adapter.LogAdapter;
import com.example.fyp_authenticator.Model.ApplicationModel;
import com.example.fyp_authenticator.Model.LogModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class LogActivity extends AppCompatActivity {
    ImageView backIcon;
    RecyclerView recView;


    DatabaseReference reference_log;

    ArrayList<LogModel> authenticationlogList;
    LogAdapter logAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        backIcon = findViewById(R.id.backIcon_log);
        recView = findViewById(R.id.recView_log);

        Intent intent = getIntent();
        String IdKey_GET = intent.getStringExtra("IdKey");
        //default
        reference_log = FirebaseDatabase.getInstance().getReference("Logs");

        authenticationlogList = new ArrayList<>();
        recView.setHasFixedSize(true);
        recView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        logAdapter = new LogAdapter(getApplicationContext(), authenticationlogList);
        recView.setAdapter(logAdapter);


        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        loadData(IdKey_GET);
    }

    private void loadData(String IDKEY){
        reference_log.child(IDKEY).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    LogModel eachLog = childSnapshot.getValue(LogModel.class);
                    authenticationlogList.add(eachLog);
                }
                logAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}