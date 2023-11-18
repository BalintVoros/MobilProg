package com.example.bolti_koltes;

import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends ArrayAdapter<ShoppingList> {

    private DatabaseHelper dbHelper;
    public CustomAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<ShoppingList> objects) {
        super(context, resource, textViewResourceId, objects);
        this.dbHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ShoppingList item = getItem(position);

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_layout, parent, false);
        }

        final TextView lst_txt = (TextView) convertView.findViewById(R.id.listItemText);

        ImageView openBtn = convertView.findViewById(R.id.openListFromList);

        ImageView deleteBtn = convertView.findViewById(R.id.deleteListButton);

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAdapter.super.remove(getItem(position));
                dbHelper.deleteListFromDb(item.getID());
                notifyDataSetChanged();
            }
        });

        lst_txt.setText(item.getName());

        openBtn.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {
                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.putExtra("list", (Serializable) item);
                getContext().startActivity(intent);
            }
        });
        lst_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.putExtra("list", (Serializable) item);
                getContext().startActivity(intent);
            }
        });
        return convertView;
    }
}

