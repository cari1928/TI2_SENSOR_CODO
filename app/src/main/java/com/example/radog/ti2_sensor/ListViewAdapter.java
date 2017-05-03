package com.example.radog.ti2_sensor;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by radog on 01/05/2017.
 */

public class ListViewAdapter extends BaseAdapter {

    private ArrayList<Item_Resultado> alItems;
    private Activity con;
    private LayoutInflater layoutInflater;


    public ListViewAdapter(ArrayList<Item_Resultado> alItems, Activity con) {
        this.alItems = alItems;
        this.con = con;
    }

    @Override
    public int getCount() {
        return alItems.size();
    }

    @Override
    public Object getItem(int position) {
        return alItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        layoutInflater = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = layoutInflater.inflate(R.layout.item_listview, null);

        TextView tvNumRepe = (TextView) convertView.findViewById(R.id.tvNumRepe);
        TextView tvRepColor = (TextView) convertView.findViewById(R.id.tvRepColor);

        tvNumRepe.setText(alItems.get(position).getRepe() + "");
        tvRepColor.setBackgroundColor(Color.parseColor(alItems.get(position).getEfi()));
        tvRepColor.setText(alItems.get(position).getDato());

        return convertView;
    }
}
