package com.example.bolti_koltes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class ExistingLists extends AppCompatActivity {

    ListView listView;
    ArrayAdapter<ShoppingList> adapter;
    ArrayList<ShoppingList> list;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_existing_lists);

        dbHelper = new DatabaseHelper(this);

        list = dbHelper.getAllExistingLists();
        adapter = new CustomAdapter(this, R.layout.list_item_layout, R.id.listItemText, list);

        listView = findViewById(R.id.listViewOfLists);
        listView.setAdapter(adapter);
    }

}