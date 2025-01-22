package com.example.tabunganapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class TransactionAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final ArrayList<String> transactions;
    private final MainActivity mainActivity;

    public TransactionAdapter(Context context, ArrayList<String> transactions, MainActivity mainActivity) {
        super(context, R.layout.list_item_transaction, transactions);
        this.context = context;
        this.transactions = transactions;
        this.mainActivity = mainActivity; // Pastikan ini benar
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item_transaction, parent, false);

        TextView textViewTransaction = rowView.findViewById(R.id.textViewTransaction);
          Button buttonDelete = rowView.findViewById(R.id.buttonDelete);

        textViewTransaction.setText(transactions.get(position));
        buttonDelete.setOnClickListener(v -> mainActivity.deleteTransaction(position));

        return rowView;
    }
}