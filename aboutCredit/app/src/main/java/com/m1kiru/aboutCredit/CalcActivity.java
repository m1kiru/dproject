package com.m1kiru.aboutCredit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.LinearLayout;

public class CalcActivity extends AppCompatActivity {

    private EditText input;
    private TextView output;
    private SharedPreferences prefs;

    // Ключи для SharedPreferences
    private static final String PREFS_NAME = "CreditSettings";

    // Кредит
    private static final String KEY_CREDIT_RATE = "credit_rate";
    private static final String KEY_CREDIT_TERM = "credit_term";
    private static final String KEY_CREDIT_INSURANCE = "credit_insurance";

    // Микрозайм
    private static final String KEY_MICRO_DAILY_RATE = "micro_daily_rate";
    private static final String KEY_MICRO_TERM = "micro_term";
    private static final String KEY_MICRO_FEE = "micro_fee";
    private static final String KEY_MICRO_TERM_IN_DAYS = "micro_term_in_days";

    // Ипотека
    private static final String KEY_MORTGAGE_RATE = "mortgage_rate";
    private static final String KEY_MORTGAGE_TERM = "mortgage_term";
    private static final String KEY_MORTGAGE_DOWN = "mortgage_down";
    private static final String KEY_MORTGAGE_INSURANCE = "mortgage_insurance";

    // Значения по умолчанию
    private boolean microTermInDays = true;
    private double creditRate = 15.0;
    private int creditTerm = 24;
    private double creditInsurance = 0.5;

    private double microDailyRate = 1.0;
    private int microTerm = 30;
    private double microFee = 500;

    private double mortgageRate = 8.5;
    private int mortgageTerm = 240;
    private double mortgageDown = 20; // процентов
    private double mortgageInsurance = 0.5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calc);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        loadSettings();

        input = findViewById(R.id.input);
        output = findViewById(R.id.output);

        Button btnCredit = findViewById(R.id.b1_calc);
        Button btnMicro = findViewById(R.id.b2_calc);
        Button btnMortgage = findViewById(R.id.b3_calc);

        // Обычный клик - расчёт
        View.OnClickListener calcListener = v -> calculateByType(v.getId());
        btnCredit.setOnClickListener(calcListener);
        btnMicro.setOnClickListener(calcListener);
        btnMortgage.setOnClickListener(calcListener);

        // Долгий клик - настройки
        View.OnLongClickListener settingsListener = v -> {
            showSettingsDialog(v.getId());
            return true;
        };
        btnCredit.setOnLongClickListener(settingsListener);
        btnMicro.setOnLongClickListener(settingsListener);
        btnMortgage.setOnLongClickListener(settingsListener);
        Button btnJournal = findViewById(R.id.btn_journal); // добавьте в layout
        btnJournal.setOnClickListener(v -> {
            Intent intent = new Intent(CalcActivity.this, JournalActivity.class);
            startActivity(intent);
        });
    }

    private void loadSettings() {
        creditRate = prefs.getFloat(KEY_CREDIT_RATE, 15.0f);
        creditTerm = prefs.getInt(KEY_CREDIT_TERM, 24);
        creditInsurance = prefs.getFloat(KEY_CREDIT_INSURANCE, 0.5f);

        microDailyRate = prefs.getFloat(KEY_MICRO_DAILY_RATE, 1.0f);
        microTerm = prefs.getInt(KEY_MICRO_TERM, 30);
        microTermInDays = prefs.getBoolean(KEY_MICRO_TERM_IN_DAYS, true); // НОВОЕ
        microFee = prefs.getFloat(KEY_MICRO_FEE, 500);

        mortgageRate = prefs.getFloat(KEY_MORTGAGE_RATE, 8.5f);
        mortgageTerm = prefs.getInt(KEY_MORTGAGE_TERM, 240);
        mortgageDown = prefs.getFloat(KEY_MORTGAGE_DOWN, 20);
        mortgageInsurance = prefs.getFloat(KEY_MORTGAGE_INSURANCE, 0.5f);
    }

    private void saveSettings() {
        prefs.edit()
                .putFloat(KEY_CREDIT_RATE, (float) creditRate)
                .putInt(KEY_CREDIT_TERM, creditTerm)
                .putFloat(KEY_CREDIT_INSURANCE, (float) creditInsurance)
                .putFloat(KEY_MICRO_DAILY_RATE, (float) microDailyRate)
                .putInt(KEY_MICRO_TERM, microTerm)
                .putBoolean(KEY_MICRO_TERM_IN_DAYS, microTermInDays) // НОВОЕ
                .putFloat(KEY_MICRO_FEE, (float) microFee)
                .putFloat(KEY_MORTGAGE_RATE, (float) mortgageRate)
                .putInt(KEY_MORTGAGE_TERM, mortgageTerm)
                .putFloat(KEY_MORTGAGE_DOWN, (float) mortgageDown)
                .putFloat(KEY_MORTGAGE_INSURANCE, (float) mortgageInsurance)
                .apply();
    }

    private void showSettingsDialog(int btnId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("⚙️ Настройки");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_settings, null);
        builder.setView(dialogView);

        EditText editRate = dialogView.findViewById(R.id.edit_rate);
        EditText editTerm = dialogView.findViewById(R.id.edit_term);
        EditText editExtra1 = dialogView.findViewById(R.id.edit_extra1);
        EditText editExtra2 = dialogView.findViewById(R.id.edit_extra2);
        TextView labelExtra1 = dialogView.findViewById(R.id.label_extra1);
        TextView labelExtra2 = dialogView.findViewById(R.id.label_extra2);

        // Заполняем текущими значениями в зависимости от типа
        if (btnId == R.id.b1_calc) {
            builder.setTitle("⚙️ Настройки кредита");
            editRate.setText(String.valueOf(creditRate));
            editTerm.setText(String.valueOf(creditTerm));
            labelExtra1.setText("Страховка (% годовых):");
            editExtra1.setText(String.valueOf(creditInsurance));
            labelExtra2.setVisibility(View.GONE);
            editExtra2.setVisibility(View.GONE);
        } else if (btnId == R.id.b2_calc) {
            builder.setTitle("⚙️ Настройки микрозайма");

            RadioGroup radioGroup = new RadioGroup(this);
            radioGroup.setOrientation(LinearLayout.HORIZONTAL);
            radioGroup.setPadding(0, 0, 0, 30);

            RadioButton radioDays = new RadioButton(this);
            radioDays.setText("Дни");
            radioDays.setId(View.generateViewId());

            RadioButton radioMonths = new RadioButton(this);
            radioMonths.setText("Месяцы");
            radioMonths.setId(View.generateViewId());

            radioGroup.addView(radioDays);
            radioGroup.addView(radioMonths);

            if (microTermInDays) {
                radioDays.setChecked(true);
            } else {
                radioMonths.setChecked(true);
            }

            LinearLayout linearLayout = (LinearLayout) dialogView;
            linearLayout.addView(radioGroup, 0);

            editRate.setText(String.valueOf(microDailyRate));
            editTerm.setText(String.valueOf(microTerm));
            labelExtra1.setText("Дневная ставка (%):");
            editExtra1.setText(String.valueOf(microDailyRate));
            labelExtra2.setText("Комиссия (₽):");
            editExtra2.setText(String.valueOf(microFee));


            radioGroup.setOnCheckedChangeListener((group, checkedId) -> microTermInDays = (checkedId == radioDays.getId()));
        } else {
            builder.setTitle("⚙️ Настройки ипотеки");
            editRate.setText(String.valueOf(mortgageRate));
            editTerm.setText(String.valueOf(mortgageTerm));
            labelExtra1.setText("Первый взнос (%):");
            editExtra1.setText(String.valueOf(mortgageDown));
            labelExtra2.setText("Страховка (%):");
            editExtra2.setText(String.valueOf(mortgageInsurance));
        }

        builder.setPositiveButton("💾 Сохранить", (dialog, which) -> {
            try {
                double rate = Double.parseDouble(editRate.getText().toString());
                int term = Integer.parseInt(editTerm.getText().toString());

                if (btnId == R.id.b1_calc) {
                    creditRate = rate;
                    creditTerm = term;
                    if (editExtra1.getVisibility() == View.VISIBLE) {
                        creditInsurance = Double.parseDouble(editExtra1.getText().toString());
                    }
                } else if (btnId == R.id.b2_calc) {
                    microDailyRate = rate;
                    microTerm = term;
                    if (editExtra1.getVisibility() == View.VISIBLE) {
                        microDailyRate = Double.parseDouble(editExtra1.getText().toString());
                    }
                    if (editExtra2.getVisibility() == View.VISIBLE) {
                        microFee = Double.parseDouble(editExtra2.getText().toString());
                    }
                    // microTermInDays уже установлен через RadioGroup
                } else {
                    mortgageRate = rate;
                    mortgageTerm = term;
                    if (editExtra1.getVisibility() == View.VISIBLE) {
                        mortgageDown = Double.parseDouble(editExtra1.getText().toString());
                    }
                    if (editExtra2.getVisibility() == View.VISIBLE) {
                        mortgageInsurance = Double.parseDouble(editExtra2.getText().toString());
                    }
                }

                saveSettings();
                Toast.makeText(this, "✅ Настройки сохранены", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "⚠️ Ошибка в данных", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("❌ Отмена", null);
        builder.show();
    }

    private void calculateByType(int buttonId) {
        String rawText = input.getText().toString().trim().replace(',', '.');
        if (rawText.isEmpty()) {
            output.setText("⚠️ Введите сумму займа");
            return;
        }

        double principal;
        try {
            principal = Double.parseDouble(rawText);
            if (principal <= 0) throw new IllegalArgumentException();
        } catch (Exception e) {
            output.setText("⚠️ Введите корректное число");
            return;
        }

        CreditCalculator calc = createCalculator(principal, buttonId);
        if (calc == null || !calc.isValid()) {
            output.setText("⚠️ Ошибка параметров расчёта");
            return;
        }

        String title;
        if (buttonId == R.id.b1_calc) {
            title = "🏦 КРЕДИТ";
        } else if (buttonId == R.id.b2_calc) {
            title = "💸 МИКРОЗАЙМ";
        } else {
            title = "🏠 ИПОТЕКА";
        }

        // Формируем отображение срока
        String termDisplay;
        if (buttonId == R.id.b2_calc) {
            if (microTermInDays) {
                termDisplay = String.format(Locale.getDefault(), "%d дн.", microTerm);
            } else {
                termDisplay = String.format(Locale.getDefault(), "%d мес. (~%d дн.)",
                        microTerm, microTerm * 30);
            }
        } else {
            termDisplay = String.format(Locale.getDefault(), "%d мес.", calc.getTermMonths());
        }

        // Основной результат
        StringBuilder result = new StringBuilder();
        result.append(String.format(Locale.getDefault(),
                "%s\n" +
                        "💳 Ежемесячный платёж: %,.2f ₽\n" +
                        "💸 Полная переплата: %,.2f ₽\n" +
                        "📈 Эффективная ставка: %,.2f%%\n" +
                        "📅 Срок: %s\n",
                title,
                calc.getMonthlyPayment(),
                calc.getTotalOverpayment(),
                calc.getEffectiveAnnualRate(),
                termDisplay
        ));

        // Для микрозайма добавляем ежедневный платёж
        if (buttonId == R.id.b2_calc) {
            double dailyPayment = calc.getDailyPayment();
            double totalPaymentDaily = dailyPayment * (microTermInDays ? microTerm : microTerm * 30) + microFee;

            result.append(String.format(Locale.getDefault(),
                    "\n💰 Ежедневный платёж: %,.2f ₽\n" +
                            "📊 Всего к возврату: %,.2f ₽\n",
                    dailyPayment,
                    totalPaymentDaily
            ));
        }

        result.append("\n💡 Удерживайте кнопку для изменения настроек");

        output.setText(result.toString());

        // 🔥 СОХРАНЯЕМ В ЖУРНАЛ
        String journalTerm = termDisplay;
        String journalType = title;

        // Для микрозайма добавляем информацию о дневном платеже в тип
        if (buttonId == R.id.b2_calc) {
            double dailyPayment = calc.getDailyPayment();
            journalType = String.format(Locale.getDefault(), "%s (день: %,.0f ₽)",
                    title, dailyPayment);
        }

        CalculationRecord record = new CalculationRecord(
                System.currentTimeMillis(),
                journalType,
                principal,
                calc.getMonthlyPayment(),
                calc.getTotalOverpayment(),
                calc.getEffectiveAnnualRate(),
                journalTerm
        );
        JournalActivity.addRecord(this, record);
    }

    private CreditCalculator createCalculator(double principal, int btnId) {
        if (btnId == R.id.b1_calc) {
            CreditCalculator calc = new CreditCalculator(principal, creditRate, creditTerm,
                    CreditCalculator.LoanType.CREDIT, CreditCalculator.PaymentType.ANNUITY);
            calc.setInsuranceRate(creditInsurance);
            return calc;
        }
        else if (btnId == R.id.b2_calc) {
            // Конвертируем в месяцы если нужно
            int termInMonths = microTermInDays ? microTerm : microTerm * 30;

            CreditCalculator micro = new CreditCalculator(principal, 0, termInMonths,
                    CreditCalculator.LoanType.MICROLOAN, CreditCalculator.PaymentType.ANNUITY);
            micro.setDailyInterestRate(microDailyRate);
            micro.setProcessingFee(microFee);
            return micro;
        }
        else if (btnId == R.id.b3_calc) {
            CreditCalculator mortgage = new CreditCalculator(principal, mortgageRate, mortgageTerm,
                    CreditCalculator.LoanType.MORTGAGE, CreditCalculator.PaymentType.ANNUITY);
            mortgage.setDownPayment(principal * mortgageDown / 100);
            mortgage.setInsuranceRate(mortgageInsurance);
            return mortgage;
        }
        return null;
    }

}