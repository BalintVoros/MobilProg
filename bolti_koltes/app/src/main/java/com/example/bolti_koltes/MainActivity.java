package com.example.bolti_koltes;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;


public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    private EditText listName;
    private ImageView addButton, editButton, checkButton;
    ArrayList<ShoppingListItem> listOfProducts;
    TextView welcomeText, inBasketPrice, remainingPrice, sumPrice;
    CustomProductListAdapter adapter;
    ShoppingList currentList;
    DatabaseHelper dbHelper;
    GmsBarcodeScanner scanner;
    CardView basket;
    ListView checklist;
    int sumPriceValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listName = findViewById(R.id.listNameEdit);
        listName.setEnabled(false);
        listOfProducts = new ArrayList<>();
        welcomeText = findViewById(R.id.welcomeText);
        checkButton = findViewById(R.id.checkButton);

        dbHelper = new DatabaseHelper(this);

        // scanner build
        scanner = GmsBarcodeScanning.getClient(this);

        Bundle b = getIntent().getExtras();
        if (b != null)
        {
            currentList = (ShoppingList) b.getSerializable("list");
            welcomeText.setVisibility(View.INVISIBLE);
            showList();
        }
        else
        {
            currentList = null;
        }
        addButton = findViewById(R.id.addButton);
        editButton = findViewById(R.id.editNameButton);

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeListName();
            }
        });

        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listName.setEnabled(false);
                listName.clearFocus();
                editButton.setVisibility(View.VISIBLE);
                checkButton.setVisibility(View.INVISIBLE);
                saveListToDB();
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
                popupMenu.setOnMenuItemClickListener(MainActivity.this);
                popupMenu.inflate(R.menu.add_menu);
                popupMenu.show();
            }
        });

        checklist = findViewById(R.id.checklist);
        if(currentList == null)
        {
            adapter = new CustomProductListAdapter(this, R.layout.product_item_in_list_layout,
                    R.id.checklistItem, listOfProducts);
        } else {
            adapter = new CustomProductListAdapter(this, R.layout.product_item_in_list_layout,
                    R.id.checklistItem, listOfProducts, currentList.getID());
        }
        checklist.setAdapter(adapter);
        sumPriceValue = 0;

        basket = findViewById(R.id.basketCardView);
        inBasketPrice = findViewById(R.id.basketText);
        remainingPrice = findViewById(R.id.leftText);
        sumPrice = findViewById(R.id.sumText);
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkBoxHandler();
            }
        });

        if(currentList!=null && adapter.getCheckedPrice()!=0)
        {
            checkBoxHandler();
        }
    }

    private void changeListName() {
        listName.setEnabled(true);
        listName.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(MainActivity.INPUT_METHOD_SERVICE);
        imm.showSoftInput(listName, InputMethodManager.SHOW_IMPLICIT);
        editButton.setVisibility(View.INVISIBLE);
        checkButton.setVisibility(View.VISIBLE);
    }

    private void checkBoxHandler()
    {
        sumPriceValue = 0;
        for(ShoppingListItem p : listOfProducts)
        {
            sumPriceValue += (p.getProduct().getPrice()*(1-p.getSalePer()) * p.getAmount());
        }
        if(basket.getVisibility() == View.INVISIBLE && adapter.getCheckedPrice() != 0)
        {
            basket.setVisibility(View.VISIBLE);
            inBasketPrice.setText("Kosár tartalma: " + String.valueOf(adapter.getCheckedPrice()) + " Ft");
            remainingPrice.setText("Még hátravan: " + String.valueOf(sumPriceValue - adapter.getCheckedPrice()) + " Ft");
            sumPrice.setText("Összesen: " + String.valueOf(sumPriceValue) + " Ft");
        }
        else if(basket.getVisibility() == View.VISIBLE)
        {
            if(adapter.getCheckedPrice() == 0)
            {
                basket.setVisibility(View.INVISIBLE);
            }
            else
            {
                inBasketPrice.setText("Kosár tartalma: " + String.valueOf(adapter.getCheckedPrice()) + " Ft");
                remainingPrice.setText("Még hátravan: " + String.valueOf(sumPriceValue - adapter.getCheckedPrice()) + " Ft");
                sumPrice.setText("Összesen: " + String.valueOf(sumPriceValue) + " Ft");
            }
        }
    }

    private void showList() {
        listName.setText(currentList.getName());
        listOfProducts = dbHelper.getProductsInList(currentList.getID());
        currentList.setProducts(listOfProducts);
    }

    private void saveListToDB() {
        String name = listName.getText().toString();
        if(currentList == null)
        {
            int id = dbHelper.addListToDBAndGetID(name);
            currentList = new ShoppingList(name, id);
            for(ShoppingListItem p : listOfProducts)
            {
                dbHelper.addProductToList(id, p);
            }
        }
        else
        {
            dbHelper.modifyListName(currentList.getID(),name);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId())
        {
            case R.id.addItemToList:
            {
                Intent intent = new Intent(MainActivity.this, AddItem.class);
                startActivityForResult(intent, 1);
                break;
            }
            case R.id.barcodeReader: {
                scanner
                        .startScan()
                        .addOnSuccessListener(
                                barcode -> {
                                    // Task completed successfully
                                    String rawValue = barcode.getRawValue();
                                    Toast barcodeToast = Toast.makeText(
                                            getApplicationContext(),
                                            rawValue,
                                            Toast.LENGTH_LONG);
                                    //barcodeToast.show();
                                    Intent intent = new Intent(MainActivity.this, AddItem.class);
                                    intent.putExtra("rawBarcode", rawValue);
                                    startActivityForResult(intent, 1);
                                })
                        .addOnCanceledListener(
                                () -> {
                                    // Task canceled
                                    Toast barcodeToast = Toast.makeText(
                                            getApplicationContext(),
                                            "Scanning canceled",
                                            Toast.LENGTH_LONG);
                                    barcodeToast.show();
                                })
                        .addOnFailureListener(
                                e -> {
                                    // Task failed with an exception
                                    Toast barcodeToast = Toast.makeText(
                                            getApplicationContext(),
                                            e.getMessage(),
                                            Toast.LENGTH_LONG);
                                    barcodeToast.show();

                                });
                break;
            }
            case  R.id.openList:
            {
                Intent intent = new Intent(MainActivity.this, ExistingLists.class);
                startActivity(intent);
                break;
            }

        }
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1)
        {
            if(resultCode == RESULT_OK)
            {
                ShoppingListItem result = new ShoppingListItem((Product) data.getSerializableExtra("product"),
                        data.getIntExtra("amount",0), false, data.getDoubleExtra("sale",0));
                if(isAlreadyInList(result))
                {
                    changeProductAmount(result);
                }else
                {
                    listOfProducts.add(result);
                    if(currentList != null)
                    {
                        dbHelper.addProductToList(currentList.getID(), result);
                    }
                }
                adapter.notifyDataSetChanged();
                welcomeText.setText("");
                adapter.registerDataSetObserver(new DataSetObserver() {
                    @Override
                    public void onChanged() {
                        super.onChanged();
                        checkBoxHandler();
                    }
                });
            }
        }
        else if (requestCode == 2)
        {
            if(resultCode == RESULT_OK)
            {
                ShoppingListItem result = new ShoppingListItem((Product) data.getSerializableExtra("product"),
                        data.getIntExtra("amount",0), false, data.getDoubleExtra("sale",0));
                adapter.deletePriceFromChecked(data.getIntExtra("idx",-1));
                swapProduct(data.getIntExtra("idx", -1), result);
                adapter.notifyDataSetChanged();
                welcomeText.setText("");
                adapter.registerDataSetObserver(new DataSetObserver() {
                    @Override
                    public void onChanged() {
                        super.onChanged();
                        checkBoxHandler();
                    }
                });
            }
        }
    }

    private void changeProductAmount(ShoppingListItem result) {
        for(ShoppingListItem spi : listOfProducts) {
            if (spi.equals(result)) {
                result.setAmount(spi.getAmount()+ result.getAmount());
                swapProduct(adapter.getPosition(spi), result);
            }
        }
    }

    private boolean isAlreadyInList(ShoppingListItem result) {
        for(ShoppingListItem spi : listOfProducts) {
            if (spi.equals(result)) {
                return true;
            }
        }
            return false;
    }

    private void swapProduct(int idx, ShoppingListItem result) {
        listOfProducts.set(idx, result);
        if(currentList != null)
        {
            dbHelper.changeProductInList(result.getProduct().getID(), currentList.getID(), result.getAmount(), (int) result.getProduct().getPrice(),
            result.getSalePer());
        }
    }

    @Override
    public void onBackPressed() {
        if(currentList == null && !listOfProducts.isEmpty())
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Nem mentett lista!");
            builder.setMessage("Szeretné menteni a listát?");
            builder.setPositiveButton("Igen", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    changeListName();
                }
            });
            builder.setNegativeButton("Nem", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    MainActivity.super.onBackPressed();
                }
            });
            builder.setNeutralButton("Mégsem", null);
            builder.setIcon(android.R.drawable.ic_menu_help);
            builder.show();
        }
        else
        {
            super.onBackPressed();
        }
    }
}