package com.m1kiru.aboutCredit;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class JournalActivity extends AppCompatActivity {

    private RecyclerView rvJournal;
    private TextView tvEmpty;
    private Button btnClear;
    private JournalAdapter adapter;
    private List<CalculationRecord> history = new ArrayList<>();
    private SharedPreferences prefs;
    private static final String KEY_HISTORY = "calc_history";
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);

        prefs = getSharedPreferences("CreditSettings", MODE_PRIVATE);
        rvJournal = findViewById(R.id.rv_journal);
        tvEmpty = findViewById(R.id.tv_empty);
        btnClear = findViewById(R.id.btn_clear);

        rvJournal.setLayoutManager(new LinearLayoutManager(this));
        adapter = new JournalAdapter();
        rvJournal.setAdapter(adapter);
        loadHistory();
        updateUI();

        btnClear.setOnClickListener(v -> showClearDialog());
    }

    private void loadHistory() {
        history.clear();
        String json = prefs.getString(KEY_HISTORY, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            // Загружаем в обратном порядке: новые сверху
            for (int i = arr.length() - 1; i >= 0; i--) {
                JSONObject obj = arr.getJSONObject(i);
                history.add(new CalculationRecord(
                        obj.getLong("timestamp"),
                        obj.getString("type"),
                        obj.getDouble("amount"),
                        obj.getDouble("monthly"),
                        obj.getDouble("overpay"),
                        obj.getDouble("apr"),
                        obj.getString("term")
                ));
            }
        } catch (Exception e) {
            history.clear();
        }
    }

    // Метод сохранения всего списка в SharedPreferences
    private void saveHistory() {
        try {
            JSONArray arr = new JSONArray();
            // Сохраняем в прямом порядке (старые внизу), чтобы при загрузке инверсией получить новые сверху
            for (int i = history.size() - 1; i >= 0; i--) {
                CalculationRecord rec = history.get(i);
                JSONObject obj = new JSONObject();
                obj.put("timestamp", rec.timestamp);
                obj.put("type", rec.type);
                obj.put("amount", rec.amount);
                obj.put("monthly", rec.monthlyPayment);
                obj.put("overpay", rec.overpayment);
                obj.put("apr", rec.apr);
                obj.put("term", rec.termInfo);
                arr.put(obj);
            }
            prefs.edit().putString(KEY_HISTORY, arr.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Метод удаления одной записи
    private void deleteRecord(int position) {
        if (position < 0 || position >= history.size()) return;

        CalculationRecord removed = history.get(position);
        history.remove(position);
        adapter.notifyItemRemoved(position);
        saveHistory(); // Сохраняем изменения

        updateUI();
        Toast.makeText(this, "🗑 Запись удалена", Toast.LENGTH_SHORT).show();
    }

    private void updateUI() {
        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(history.isEmpty() ? View.VISIBLE : View.GONE);
        btnClear.setVisibility(history.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void showClearDialog() {
        if (history.isEmpty()) return;
        new AlertDialog.Builder(this)
                .setTitle("🗑 Очистка истории")
                .setMessage("Удалить все сохранённые расчёты?")
                .setPositiveButton("Да", (d, w) -> {
                    prefs.edit().remove(KEY_HISTORY).apply();
                    history.clear();
                    updateUI();
                    Toast.makeText(this, "✅ История очищена", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    // Статический метод добавления записи (для вызова из других активити)
    public static void addRecord(Context ctx, CalculationRecord record) {
        try {
            SharedPreferences prefs = ctx.getSharedPreferences("CreditSettings", Context.MODE_PRIVATE);
            String json = prefs.getString(KEY_HISTORY, "[]");
            JSONArray arr = new JSONArray(json);

            JSONObject obj = new JSONObject();
            obj.put("timestamp", record.timestamp);
            obj.put("type", record.type);
            obj.put("amount", record.amount);
            obj.put("monthly", record.monthlyPayment);
            obj.put("overpay", record.overpayment);
            obj.put("apr", record.apr);
            obj.put("term", record.termInfo); // Обратите внимание: поле в record должно называться termInfo

            arr.put(obj);
            prefs.edit().putString(KEY_HISTORY, arr.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- АДАПТЕР ---
    private class JournalAdapter extends RecyclerView.Adapter<JournalAdapter.VH> {

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_journal, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            CalculationRecord rec = history.get(position);
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

            holder.tvType.setText(rec.type);
            holder.tvDate.setText(sdf.format(rec.timestamp));
            holder.tvAmount.setText(String.format(Locale.getDefault(), "Сумма: %,.0f ₽", rec.amount));
            holder.tvMonthly.setText(String.format(Locale.getDefault(), "Платёж: %,.2f ₽/мес", rec.monthlyPayment));
            holder.tvOverpay.setText(String.format(Locale.getDefault(), "Переплата: %,.2f ₽", rec.overpayment));
            holder.tvApr.setText(String.format(Locale.getDefault(), "Эфф. ставка: %.2f%% | Срок: %s", rec.apr, rec.termInfo));

            // Обработчик долгого нажатия для удаления
            holder.itemView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(JournalActivity.this)
                        .setTitle("Удалить запись?")
                        .setMessage("Вы уверены, что хотите удалить этот расчёт из истории?")
                        .setPositiveButton("Удалить", (dialog, which) -> {
                            deleteRecord(holder.getAdapterPosition());
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
                return true; // событие обработано
            });

            // Обычный клик (опционально, если нужно показать детали)
            holder.itemView.setOnClickListener(v -> {
                // TODO: Показать детали расчёта или скопировать данные
                Toast.makeText(JournalActivity.this, "Нажмите и удерживайте для удаления", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return history.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvType, tvDate, tvAmount, tvMonthly, tvOverpay, tvApr;
            VH(View v) {
                super(v);
                tvType = v.findViewById(R.id.tv_type);
                tvDate = v.findViewById(R.id.tv_date);
                tvAmount = v.findViewById(R.id.tv_amount);
                tvMonthly = v.findViewById(R.id.tv_monthly);
                tvOverpay = v.findViewById(R.id.tv_overpay);
                tvApr = v.findViewById(R.id.tv_apr);
            }
        }
    }
}