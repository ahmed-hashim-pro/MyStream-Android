package com.medoapps.www.onlinequran;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.text.DecimalFormat;

public class ZakatCalculatorActivity extends AppCompatActivity {

    private static final double GOLD_NISAB_GRAMS = 85.0;
    private static final double SILVER_NISAB_GRAMS = 595.0;
    private static final double ZAKAT_RATE = 0.025; // 2.5%

    private TextInputEditText etGoldPrice;
    private TextInputEditText etSilverPrice;
    private TextInputEditText etCash;
    private TextInputEditText etGold;
    private TextInputEditText etSilver;
    private TextInputEditText etInvestments;
    private TextInputEditText etDebts;

    private MaterialCardView cardResult;
    private TextView tvTotalWealth;
    private TextView tvNisabGold;
    private TextView tvNisabSilver;
    private TextView tvNisabStatus;
    private TextView tvZakatAmount;

    private final DecimalFormat df = new DecimalFormat("#,##0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zakat_calculator);

        // ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.zakat_calculator));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Status bar and nav bar
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.background_main));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.background_main));

        // Bind views
        etGoldPrice = findViewById(R.id.et_gold_price);
        etSilverPrice = findViewById(R.id.et_silver_price);
        etCash = findViewById(R.id.et_cash);
        etGold = findViewById(R.id.et_gold);
        etSilver = findViewById(R.id.et_silver);
        etInvestments = findViewById(R.id.et_investments);
        etDebts = findViewById(R.id.et_debts);

        cardResult = findViewById(R.id.card_result);
        tvTotalWealth = findViewById(R.id.tv_total_wealth);
        tvNisabGold = findViewById(R.id.tv_nisab_gold);
        tvNisabSilver = findViewById(R.id.tv_nisab_silver);
        tvNisabStatus = findViewById(R.id.tv_nisab_status);
        tvZakatAmount = findViewById(R.id.tv_zakat_amount);

        MaterialCardView btnCalculate = findViewById(R.id.btn_calculate);
        btnCalculate.setOnClickListener(v -> calculateZakat());
    }

    private double parseField(TextInputEditText field) {
        if (field.getText() == null || field.getText().toString().trim().isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(field.getText().toString().trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void calculateZakat() {
        double goldPrice = parseField(etGoldPrice);
        double silverPrice = parseField(etSilverPrice);
        double cash = parseField(etCash);
        double goldGrams = parseField(etGold);
        double silverGrams = parseField(etSilver);
        double investments = parseField(etInvestments);
        double debts = parseField(etDebts);

        double goldValue = goldGrams * goldPrice;
        double silverValue = silverGrams * silverPrice;
        double totalWealth = cash + goldValue + silverValue + investments - debts;
        if (totalWealth < 0) totalWealth = 0;

        double nisabGoldValue = GOLD_NISAB_GRAMS * goldPrice;
        double nisabSilverValue = SILVER_NISAB_GRAMS * silverPrice;

        // Use the lower nisab if both prices are provided, gold nisab if only gold, silver if only silver
        double nisabThreshold;
        if (goldPrice > 0 && silverPrice > 0) {
            nisabThreshold = Math.min(nisabGoldValue, nisabSilverValue);
        } else if (goldPrice > 0) {
            nisabThreshold = nisabGoldValue;
        } else if (silverPrice > 0) {
            nisabThreshold = nisabSilverValue;
        } else {
            nisabThreshold = 0;
        }

        boolean reachesNisab = nisabThreshold > 0 && totalWealth >= nisabThreshold;
        double zakatAmount = reachesNisab ? totalWealth * ZAKAT_RATE : 0;

        // Display results
        tvTotalWealth.setText(df.format(totalWealth));
        tvNisabGold.setText(goldPrice > 0 ? df.format(nisabGoldValue) : "—"); // em dash
        tvNisabSilver.setText(silverPrice > 0 ? df.format(nisabSilverValue) : "—");

        if (nisabThreshold <= 0) {
            // No prices entered
            tvNisabStatus.setText(getString(R.string.zakat_enter_price_prompt));
            tvNisabStatus.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        } else if (reachesNisab) {
            tvNisabStatus.setText(getString(R.string.zakat_nisab_reached));
            tvNisabStatus.setTextColor(ContextCompat.getColor(this, R.color.gold_accent));
        } else {
            tvNisabStatus.setText(getString(R.string.zakat_nisab_not_reached));
            tvNisabStatus.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        }

        tvZakatAmount.setText(df.format(zakatAmount));

        cardResult.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
