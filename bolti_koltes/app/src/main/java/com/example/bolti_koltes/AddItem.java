package com.example.bolti_koltes;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LongSummaryStatistics;

public class AddItem extends AppCompatActivity {

    Button addItemButton, addSaleButton;
    TextView title;
    DatabaseHelper dbHelper;
    AutoCompleteTextView productName, categoryInput;
    ArrayList<Product> listOfProducts;
    ArrayList<Category> categories;
    EditText itemUnitInput, itemPriceInput, itemAmountInput;
    Product product;
    Category productCategory;
    int amount, amountFromExtra, modifyIdx=-1;
    double salePer, saleFromExtra;
    String rawBarcode;
    boolean modifyingModeON = false, saleChosen = false;
    String barcodeFetchedProductNameString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        addItemButton = findViewById(R.id.addItemButton);
        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addButtonHandler();
            }
        });
        addSaleButton = findViewById(R.id.addSale);
        addSaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSaleHandler();
            }
        });

        productName = findViewById(R.id.itemName);
        categoryInput = findViewById(R.id.itemCategory);
        itemUnitInput = findViewById(R.id.itemUnit);
        itemAmountInput = findViewById(R.id.itemAmount);
        itemPriceInput = findViewById(R.id.itemPrice);

        product = null;
        productCategory = null;

        // barcode elkérése
        rawBarcode = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if(extras.getString("rawBarcode") != null)
            {
                rawBarcode = extras.getString("rawBarcode");
            }
            else if(extras.getSerializable("editable") != null)
            {
                modifyingModeON = true;
                product = (Product) extras.getSerializable("editable");
                amountFromExtra = extras.getInt("amount");
                saleFromExtra = extras.getDouble("sale");
                title = findViewById(R.id.addItemTitle);
                title.setText("TERMÉK MÓDOSÍTÁSA");
                addItemButton.setText("MÓDOSÍT");
                productName.setText(product.getName());
                categoryInput.setText(product.getCategory().getName());
                itemUnitInput.setText(product.getUnit());
                itemAmountInput.setText(String.valueOf(amountFromExtra));
                itemPriceInput.setText(String.valueOf(product.getPrice()));
                modifyIdx = extras.getInt("idx");
            }
            //Toast.makeText(this, rawBarcode, Toast.LENGTH_SHORT).show(); //DEBUG
        }

        dbHelper = new DatabaseHelper(getApplicationContext());
        listOfProducts = dbHelper.getProductNamesFromDB();
        categories = dbHelper.getAllCategories();

        if (rawBarcode != null) {// ha barcode beolvasás történt,
            if (dbHelper.barcodeIsInDB(rawBarcode)) { // és a barcode már az adatbázisban van
                //Toast.makeText(this, "PAKK", Toast.LENGTH_SHORT).show(); //DEBUG

                // lekérjük az adatbázisból
                product = dbHelper.getProductFromBarcode(rawBarcode);
                productCategory =  product.getCategory();
                //Toast.makeText(this, product.unfoldToString(), Toast.LENGTH_SHORT).show(); // DEBUG

                // az adatait betöltjük a mezőkbe
                productName.setText(product.getName());
                categoryInput.setText(product.getCategory().getName());
                itemUnitInput.setText(product.getUnit());
                itemPriceInput.setText(String.valueOf(product.getTypicalPrice()));

            } else { // még nincs az adatbázisban, tehát ismeretlen
                new FetchBarcode().execute(); // megkérdezzük az internetet és kitöltjük a név mezőt
            }
        }

        ArrayAdapter<Product> adapterForProducts = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, listOfProducts);
        productName.setAdapter(adapterForProducts);
        ArrayAdapter<Category> adapterForCategories = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        categoryInput.setAdapter(adapterForCategories);
        categoryInput.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                productCategory = (Category) adapterView.getItemAtPosition(i);
                if(product != null)
                {
                    product.setCategory(productCategory);
                }
            }
        });
        productName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                product = (Product) adapterView.getItemAtPosition(i);
                productCategory = product.getCategory();
                categoryInput.setText(productCategory.getName());
                if(product.getTypicalPrice() > 0)
                {
                    itemPriceInput.setText(String.valueOf(product.getTypicalPrice()));
                }
                if(product.getUnit() != null)
                {
                    itemUnitInput.setText(product.getUnit());
                }
            }
        });
        salePer=0;
    }

    private void addSaleHandler() {
        Dialog saleEditor = new Dialog(this);
        saleEditor.setContentView(R.layout.sale_dialog_layout);
        saleEditor.show();

        Button cancelBtn = saleEditor.findViewById(R.id.salesCancelBtn);
        Button okBtn = saleEditor.findViewById(R.id.salesOKBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saleEditor.dismiss();
            }
        });
        NumberPicker salePicker = saleEditor.findViewById(R.id.salePicker);
        ArrayList<String> values = new ArrayList<>();
        for(int i = 0; i<=100; i+=5)
        {
            values.add(String.valueOf(i) + "%");
        }
        salePicker.setDisplayedValues(values.toArray(new String[values.size()]));
        salePicker.setMaxValue(values.size()-1);
        salePicker.setMinValue(0);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(salePicker.getValue()==0) {
                    saleEditor.dismiss();
                }else {
                    salePer = salePicker.getValue()*0.05;
                    saleChosen = true;
                    saleEditor.dismiss();
                }
            }
        });
    }

    private class FetchBarcode extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            org.jsoup.nodes.Document doc = null;
            try {
                Connection connection = Jsoup.connect("https://www.barcodelookup.com/" + rawBarcode);
                connection.userAgent("Mozilla/5.0");
                connection.referrer("http://www.google.com");
                doc = connection.get();
                Elements headlines = doc.getElementsByTag("h4");
                Element barcodeFetchedProductNameElement = headlines.first();
                barcodeFetchedProductNameString = barcodeFetchedProductNameElement.text();
                //itemUnitInput.setText("FETCH SUCCESS. "); //DEBUG
            } catch (IOException e) {
                e.printStackTrace();
                //itemUnitInput.setText(e.getMessage().substring(0,24)); //DEBUG
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            productName.setText(barcodeFetchedProductNameString);
            //itemUnitInput.setText(itemUnitInput.getText() + "DONE."); //DEBUG
        }
    }


    private void addButtonHandler()
    {
        if(!getIncompleteFields().isEmpty())
        {
            for(EditText et : getIncompleteFields())
            {
                et.setError("Töltsd ki a mezőt!");
            }
            return;
        }
        String name = productName.getText().toString();
        String unit = itemUnitInput.getText().toString();
        double price = Double.parseDouble(itemPriceInput.getText().toString());
        double typPrice = price;
        amount = Integer.parseInt(itemAmountInput.getText().toString());
        Product inputProduct;
        if(modifyingModeON)
        {
            inputProduct = new Product(name, productCategory, price, unit, typPrice);
            if(isProductModified(inputProduct) || amount != amountFromExtra || salePer != saleFromExtra)
            {
                showAlertDialog(inputProduct);
            }
        }
        if(isCategoryInDb())
        {
            inputProduct = new Product(name, productCategory, price, unit, typPrice);
        } else
        {
            String catName = categoryInput.getText().toString();
            Category cat = dbHelper.addAndGetCategoryToDb(catName);
            inputProduct = new Product(name, cat, price, unit, typPrice);
        }
        if(rawBarcode != null) {
            inputProduct.setBarcode(rawBarcode);
        }
        if(isProductAlreadyInDB())
        {
            inputProduct.setId(product.getID());
            if(isProductModified(inputProduct))
            {
                showAlertDialog(inputProduct);
            }
            else
            {
                product = inputProduct;
                sendResult();
            }
        }
        else
        {
            int id = dbHelper.addProductToDatabaseAndGetId(inputProduct);
            product = inputProduct;
            product.setId(id);

            sendResult();
        }
    }

    private void sendResult() {
        Intent intent = new Intent(AddItem.this, MainActivity.class);
        intent.putExtra("product", (Serializable) product);
        intent.putExtra("amount", amount);
        intent.putExtra("idx", modifyIdx);
        intent.putExtra("sale",salePer);
        setResult(RESULT_OK, intent);
        finish();
    }

    private boolean isCategoryInDb() {
        return productCategory != null && categoryInput.getText().toString().equals(productCategory.getName());
    }

    private ArrayList<EditText> getIncompleteFields() {
        ArrayList<EditText> et = new ArrayList<>();
        if (TextUtils.isEmpty(productName.getText().toString())) {
            et.add(productName);
        }
        if (TextUtils.isEmpty(itemAmountInput.getText().toString())) {
            et.add(itemAmountInput);
        }
        if (TextUtils.isEmpty(categoryInput.getText().toString())) {
            et.add(categoryInput);
        }
        if (TextUtils.isEmpty(itemUnitInput.getText().toString())) {
            et.add(itemUnitInput);
        }
        if (TextUtils.isEmpty(itemPriceInput.getText().toString())) {
            et.add(itemPriceInput);
        }
        return et;
    }

    private void showAlertDialog(Product input) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Termék mentése");
        builder.setMessage("Szeretné felülírni a termék adatait?");
        builder.setPositiveButton("Igen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        product = input;
                        dbHelper.modifyProduct(product.getID(), product);
                        if(!saleChosen && modifyingModeON)
                        {
                            salePer=saleFromExtra;
                        }
                        sendResult();
                    }
                });
        builder.setNegativeButton("Nem", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        product = input;
                        if(modifyingModeON)
                        {
                            amount = amountFromExtra;
                            salePer = saleFromExtra;
                        }
                        int id = dbHelper.addProductToDatabaseAndGetId(product);
                        product.setId(id);
                        sendResult();
                    }
                });
        builder.setNeutralButton("Mégsem", null);
        builder.setIcon(android.R.drawable.ic_menu_help);
        builder.show();
    }

    private boolean isProductModified(Product input) {
        return !product.equals(input);
    }

    private boolean isProductAlreadyInDB() {
        return product != null && product.getName().equals(productName.getText().toString());
    }
}