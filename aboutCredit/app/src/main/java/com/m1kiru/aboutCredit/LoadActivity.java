package com.m1kiru.aboutCredit;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class LoadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loading);

        new android.os.Handler().postDelayed(
            () -> {
                Intent intent = new Intent(LoadActivity.this, MenuActivity.class);
                startActivity(intent);
            },
            1500
        );
    }
}