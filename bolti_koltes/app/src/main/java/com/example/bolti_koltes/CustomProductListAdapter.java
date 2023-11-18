package com.example.bolti_koltes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.w3c.dom.Text;

import java.util.List;

public class CustomProductListAdapter extends ArrayAdapter<ShoppingListItem> {
    private int checkedprice;
    private DatabaseHelper dbHelper;
    private int listId;
    private boolean creation = false;
    public CustomProductListAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<ShoppingListItem> objects, int listId) {
        super(context, resource, textViewResourceId, objects);
        this.checkedprice = 0;
        this.dbHelper = new DatabaseHelper(context);
        this.listId = listId;
        this.creation = true;
        for(ShoppingListItem spi : objects)
        {
            if(spi.isChecked())
            {
                checkedprice+=spi.getProduct().getPrice()*spi.getAmount()*(1-spi.getSalePer());
            }
        }
    }
    public CustomProductListAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<ShoppingListItem> objects) {
        super(context, resource, textViewResourceId, objects);
        this.checkedprice = 0;
        this.dbHelper = new DatabaseHelper(context);
        this.listId = -1;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Product item = getItem(position).getProduct();
        int amount = getItem(position).getAmount();
        double per = getItem(position).getSalePer();
        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.product_item_in_list_layout, parent, false);
        }

        final CheckBox lst_txt = (CheckBox) convertView.findViewById(R.id.checklistItem);
        boolean isChecked = getItem(position).isChecked();
        lst_txt.setOnCheckedChangeListener(null);
        lst_txt.setChecked(isChecked);
        if(isChecked)
        {
            lst_txt.setPaintFlags(lst_txt.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else
        {
            lst_txt.setPaintFlags(lst_txt.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }
        TextView price = convertView.findViewById(R.id.priceTag);
        price.setText("("+amount+" x "+item.getPrice()*(1-per)+" Ft)");
        TextView sale = convertView.findViewById(R.id.saleText);
        if(per > 0)
        {
            sale.setText(String.valueOf((int)(per*100) )+"%");
        }else
        {
            sale.setText(null);
        }

        ImageView editBtn = convertView.findViewById(R.id.editProductBtn);

        ImageView deleteBtn = convertView.findViewById(R.id.deleteProductBtn);

        lst_txt.setText(item.getName());

        editBtn.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {
                Intent intent = new Intent(getContext(), AddItem.class);
                intent.putExtra("editable", item);
                intent.putExtra("amount", amount);
                intent.putExtra("idx", position);
                intent.putExtra("sale", per);
                ((Activity)getContext()).startActivityForResult(intent, 2);
            }
        });
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomProductListAdapter.super.remove(getItem(position));
                dbHelper.deleteProductFromList(item.getID(), listId, per);
                if(isChecked)
                {
                    checkedprice-=(item.getPrice()*(1-per) * amount);
                }
                notifyDataSetChanged();
            }
        });
       lst_txt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                getItem(position).setChecked(b);
                if(b)
                {
                    checkedprice += (item.getPrice()*(1-per) * amount);
                }
                else
                {
                    checkedprice -= (item.getPrice()*(1-per) * amount);
                }
                notifyDataSetChanged();
                if(listId != -1)
                {
                    dbHelper.setChecked(item.getID(),listId, b);
                }
            }
        });
        return convertView;
    }

    public int getCheckedPrice()
    {
        return checkedprice;
    }

    @Override
    public int getPosition(@Nullable ShoppingListItem item) {
        return super.getPosition(item);
    }

    public void deletePriceFromChecked(int idx) {
        if(idx != -1)
        {
            checkedprice -= getItem(idx).getProduct().getPrice() * (1-getItem(idx).getSalePer()) * getItem(idx).getAmount();
        }
    }
}
