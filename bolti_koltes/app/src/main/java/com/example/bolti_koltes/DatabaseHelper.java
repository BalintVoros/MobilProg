package com.example.bolti_koltes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Pair;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private Context mycontext;
    private String DB_PATH;

    private static String DB_NAME = "shopping.db";
    public SQLiteDatabase myDataBase;


    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
        this.mycontext = context;
        DB_PATH = context.getDataDir().getAbsolutePath() + "/databases/";
        boolean dbexist = checkdatabase();
        if (dbexist) {
            opendatabase();
        } else {
            System.out.println("Database doesn't exist");
            createdatabase();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {


    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void createdatabase() {
        boolean dbexist = checkdatabase();
        if(!dbexist) {
            this.getReadableDatabase();
            try {
                copydatabase();
            } catch(IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    private boolean checkdatabase() {

        boolean checkdb = false;
        try {
            String myPath = DB_PATH + DB_NAME;
            File dbfile = new File(myPath);
            checkdb = dbfile.exists();
        } catch(SQLiteException e) {
            System.out.println("Database doesn't exist");
        }
        return checkdb;
    }

    private void copydatabase() throws IOException {
        //Open your local db as the input stream
        InputStream myinput = mycontext.getAssets().open(DB_NAME);

        // Path to the just created empty db
        String outfilename = DB_PATH + DB_NAME;

        //Open the empty db as the output stream
        OutputStream myoutput = new FileOutputStream(outfilename);

        // transfer byte to inputfile to outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myinput.read(buffer))>0) {
            myoutput.write(buffer,0,length);
        }

        //Close the streams
        myoutput.flush();
        myoutput.close();
        myinput.close();
    }

    public void opendatabase() throws SQLException {
        //Open the database
        String mypath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(mypath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    public synchronized void close() {
        if(myDataBase != null) {
            myDataBase.close();
        }
        super.close();
    }

    public ArrayList<Product> getProductNamesFromDB()
    {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM Products p JOIN Categories c  ON " +
                "p.productCategoryId=c.id", null);
        ArrayList<Product> list = new ArrayList<>();
        if(c.moveToFirst())
        {
            do {
                int id = c.getInt(0);
                String name = c.getString(1);
                int category = c.getInt(2);
                int typPrice = c.getInt(3);
                String unit = c.getString(4);
                String barcode = c.getString(5);
                String catName = c.getString(7);
                Product p = new Product(name, unit, barcode, new Category(category, catName), 0, typPrice, id);
                list.add(p);
            }while (c.moveToNext());
        }
        return list;
    }

    public int addListToDBAndGetID(String name) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO Lists (name) VALUES(?)", new String[]{name});

        Cursor c = db.rawQuery("SELECT last_insert_rowid()", null);
        c.moveToLast();
        int id = c.getInt(0);
        return id;
    }

    public ArrayList<ShoppingList> getAllExistingLists()
    {
        ArrayList<ShoppingList> existingLists = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM Lists", null);

        if(c.moveToFirst())
        {
            do {
                int id = c.getInt(0);
                String name = c.getString(1);
                existingLists.add(new ShoppingList(name, id));
            }while(c.moveToNext());
        }

        return existingLists;
    }

    public ArrayList<ShoppingListItem> getProductsInList(int id) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM Products p JOIN Product_In_List pil ON p.id=pil.productId JOIN" +
                " Categories c ON c.id=p.productCategoryId  WHERE pil.listId=?";
        Cursor c = db.rawQuery(query, new String[]{String.valueOf(id)});

        ArrayList<ShoppingListItem> list = new ArrayList<>();

        if(c.moveToFirst())
        {
            do {
                int productId = c.getInt(0);
                String name = c.getString(1);
                int catId = c.getInt(2);
                int typPrice = c.getInt(3);
                String unit = c.getString(4);
                String barcode = c.getString(5);
                int amount = c.getInt(8);
                int actualPrice = c.getInt(9);
                boolean isChecked = (c.getInt(10) == 1);
                double sale = c.getDouble(11);
                String categoryName = c.getString(13);
                Product p = new Product(name, unit, barcode, new Category(catId,categoryName),
                        actualPrice, typPrice, productId);
                list.add(new ShoppingListItem(p, amount, isChecked, sale));
            }while (c.moveToNext());
        }
        return list;
    }

    public boolean barcodeIsInDB(String barcode) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM Products WHERE barcode=?", new String[]{barcode});
        return c.moveToFirst();
    }

    public Product getProductFromBarcode(String barcode) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM Products p JOIN Categories c ON p.productCategoryId=c.id WHERE barcode=?", new String[]{barcode});
        Product p = null;

        if(c.moveToFirst())
        {
            int id = c.getInt(0);
            String name = c.getString(1);
            int category = c.getInt(2);
            int typPrice = c.getInt(3);
            String unit = c.getString(4);
            String catName = c.getString(7);
            p = new Product(name, unit, barcode, new Category(category, catName), 0, typPrice, id);
        }
        return p;
    }

    public ArrayList<Category> getAllCategories() {
        ArrayList<Category> categories = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM Categories", null);

        if(c.moveToFirst())
        {
            do {
                int id = c.getInt(0);
                String name = c.getString(1);
                Category cat = new Category(id, name);
                categories.add(cat);
            }while(c.moveToNext());
        }
        return categories;
    }

    public Category addAndGetCategoryToDb(String name) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO Categories (categoryName) VALUES(?)", new String[]{name});

        Cursor c = db.rawQuery("SELECT id FROM Categories WHERE categoryName=?", new String[]{name});
        c.moveToLast();
        int id = c.getInt(0);
        return new Category(id, name);
    }

    public void modifyProduct(int id, Product product) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE Products SET " +
                "productName = ? ," +
                "productCategoryId = ?," +
                "productTypicalPrice = ?," +
                "productUnit = ?," +
                "barcode = ? " +
                "WHERE id = ?", new String[]{product.getName(), String.valueOf(product.getCategory().getId()),
                String.valueOf(product.getTypicalPrice()), product.getUnit(), product.getBarcode(), String.valueOf(id)});
    }
    public void changeProductInList(int productId, int listId, int amount, int price, double per)
    {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE Product_In_List SET " +
                "productAmount = ? ," +
                "productActualPrice= ?" +
                "WHERE productId=? AND listId=? AND salePer=?", new String[] {String.valueOf(amount),
                String.valueOf(price), String.valueOf(productId), String.valueOf(listId), String.valueOf(per)});
    }

    public int addProductToDatabaseAndGetId(Product product) {
        SQLiteDatabase db = getWritableDatabase();
        String barcode = product.getBarcode();
        db.execSQL("INSERT INTO Products (productName, productCategoryId, productTypicalPrice, productUnit, barcode)" +
                "VALUES (?,?,?,?,?)", new String[]{
                product.getName(),
                String.valueOf(product.getCategory().getId()),
                String.valueOf(product.getPrice()),
                product.getUnit(),
                barcode
        });

        Cursor c = db.rawQuery("SELECT last_insert_rowid()", null);
        c.moveToLast();
        int id = c.getInt(0);
        return id;
    }

    public void addProductToList(int id, ShoppingListItem p) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO Product_In_List " +
                "VALUES (?,?,?,?,?,?)", new  String[] {
                String.valueOf(p.getProduct().getID()),
                String.valueOf(id),
                String.valueOf(p.getAmount()),
                String.valueOf(p.getProduct().getPrice()),
                String.valueOf(p.isChecked() ? 1 : 0),
                String.valueOf(p.getSalePer())
        });
    }

    public void modifyListName(int id, String name) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE Lists SET name=? WHERE id=?", new String[]{
                name, String.valueOf(id)
        });
    }

    public void deleteProductFromList(int productId, int listId, double per)
    {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM Product_In_List WHERE productId=? AND listId=? AND salePer=?",
                new String[]{String.valueOf(productId), String.valueOf(listId),
                String.valueOf(per)});
    }

    public void deleteListFromDb(int id)
    {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM Product_In_List WHERE listId=?", new String[]{String.valueOf(id)});
        db.execSQL("DELETE FROM Lists WHERE id=?", new String[]{String.valueOf(id)});

    }

    public void setChecked(int id, int listId, boolean b) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE Product_In_List SET " +
                "isChecked = ?" +
                "WHERE productId=? AND listId=?",
                new String[]{String.valueOf(b? 1 : 0), String.valueOf(id), String.valueOf(listId)});
    }
}
