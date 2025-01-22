package com.example.tabunganapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText editTextAmount;
    private EditText editTextDate;
    private EditText editTextDescription;
    private Spinner spinnerType;
    private TextView textViewBalance;
    private ListView listViewTransactions;

    private ArrayList<String> transactions;
    private TransactionAdapter adapter;
    private double balance = 0;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextAmount = findViewById(R.id.editTextAmount);
        editTextDate = findViewById(R.id.editTextDate);
        editTextDescription = findViewById(R.id.editTextDescription);
        spinnerType = findViewById(R.id.spinnerType);
        textViewBalance = findViewById(R.id.textViewBalance);
        listViewTransactions = findViewById(R.id.listViewTransactions);
        Button buttonAdd = findViewById(R.id.buttonAdd);
        Button buttonSelectDate = findViewById(R.id.buttonSelectDate);

        transactions = new ArrayList<>();
        adapter = new TransactionAdapter(this, transactions, this);
        listViewTransactions.setAdapter(adapter);

        sharedPreferences = getSharedPreferences("TabunganApp", MODE_PRIVATE);
        loadTransactions();

        buttonAdd.setOnClickListener(v -> addTransaction());

        buttonSelectDate.setOnClickListener(v -> showDatePicker());
    }

    private void addTransaction() {
        String amountString = editTextAmount.getText().toString();
        String dateString = editTextDate.getText().toString();
        String descriptionString = editTextDescription.getText().toString();

        if (amountString.isEmpty()) {
            Toast.makeText(this, "Jumlah tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dateString.isEmpty()) {
            Toast.makeText(this, "Tanggal tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        if (descriptionString.isEmpty()) {
            Toast.makeText(this, "Deskripsi tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amountString = amountString.replace(".", "").replace(",", ".");
            amount = Double.parseDouble(amountString);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Jumlah harus berupa angka", Toast.LENGTH_SHORT).show();
            return;
        }

        String type = spinnerType.getSelectedItem().toString();
        String formattedAmount = formatRupiah(amount);

        if (type.equals("Pendapatan")) {
            balance += amount;
            transactions.add("Pendapatan: " + formattedAmount + " - " + descriptionString + " pada " + dateString);
        } else {
            balance -= amount;
            transactions.add("Pengeluaran: " + formattedAmount + " - " + descriptionString + " pada " + dateString);
        }

        Log.d("MainActivity", "Transactions: " + transactions.toString());

        textViewBalance.setText("Saldo: " + formatRupiah(balance));
        editTextAmount.setText("");
        editTextDate.setText("");
        editTextDescription.setText("");
        adapter.notifyDataSetChanged();
        saveTransactions();
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
            editTextDate.setText(date);
        }, year, month, day);
        datePickerDialog.show();
    }

    private String formatRupiah(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return format.format(amount);
    }

    private void saveTransactions() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        StringBuilder transactionsString = new StringBuilder();
        for (String transaction : transactions) {
            transactionsString.append(transaction).append(";");
        }
        editor.putString("transactions", transactionsString.toString());
        editor.putFloat("balance", (float) balance);
        editor.apply();
    }

    private void loadTransactions() {
        String transactionsString = sharedPreferences.getString("transactions", "");
        Log.d("MainActivity", "Loaded transactions string: " + transactionsString);

        if (!transactionsString.isEmpty()) {
            String[] savedTransactions = transactionsString.split(";");
            for (String transaction : savedTransactions) {
                if (!transaction.isEmpty()) {
                    transactions.add(transaction);
                }
            }
        }
        balance = sharedPreferences.getFloat("balance", 0);
        textViewBalance.setText("Saldo: " + formatRupiah(balance));
    }

    public void deleteTransaction(int position) {
        String transaction = transactions.get(position);
        Log.d("MainActivity", "Deleting transaction: " + transaction);

        String[] parts = transaction.split(" - ");

        // Pastikan kita memiliki cukup bagian untuk menghindari ArrayIndexOutOfBoundsException
        if (parts.length < 2) {
            Toast.makeText(this, "Transaksi tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ambil jumlah dari transaksi
        String amountString = parts[0].split(": ")[1].replace("Rp", "").replace(".", "").replace(",", ".").trim(); // Ambil jumlah dan hapus spasi
        double amount = Double.parseDouble(amountString);

        // Perbarui saldo berdasarkan jenis transaksi
        if (transaction.startsWith("Pendapatan")) {
            balance -= amount; // Kurangi saldo jika pendapatan dihapus
        } else {
            balance += amount; // Tambah saldo jika pengeluaran dihapus
        }

        // Hapus transaksi dari daftar
        transactions.remove(position);
        adapter.notifyDataSetChanged();
        saveTransactions();

        // Perbarui tampilan saldo
        textViewBalance.setText("Saldo: " + formatRupiah(balance));
    }
}