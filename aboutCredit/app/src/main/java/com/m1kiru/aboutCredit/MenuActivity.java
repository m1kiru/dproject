package com.m1kiru.aboutCredit;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MenuActivity extends AppCompatActivity {

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {
                    Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                    return insets;
                });

        Button btn_aboutCredit = findViewById(R.id.btn_about_credit);
        Button btn_calc = findViewById(R.id.btn_calc);
        Button btn_journal = findViewById(R.id.btn_journal);


        TextView info = findViewById(R.id.info);
        Intent intent_to_about = new Intent(MenuActivity.this, AboutActivity.class);
        btn_aboutCredit.setOnClickListener(v -> {
            startActivity(intent_to_about);
        });
        Intent intent_to_calc = new Intent(MenuActivity.this, CalcActivity.class);
        btn_calc.setOnClickListener(v -> {
            startActivity(intent_to_calc);
        });
        Intent intent_to_journal = new Intent(MenuActivity.this, JournalActivity.class);
        btn_journal.setOnClickListener(v -> {
            startActivity(intent_to_journal);
        });
    }
}