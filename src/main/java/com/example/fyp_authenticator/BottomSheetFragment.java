package com.example.fyp_authenticator;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BottomSheetFragment extends BottomSheetDialogFragment {



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.popup_add, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RelativeLayout qrscanBut_popup_add = view.findViewById(R.id.qrscanBut_popup_add);
        qrscanBut_popup_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getContext(),"To qr code",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getContext(),QRSetupActivity.class));
            }
        });

        RelativeLayout keyConnectBut = view.findViewById(R.id.keyConnect_popup_add);
        keyConnectBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getContext(),"To key code",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getContext(),KeySetupActivity.class));
            }
        });

    }
}