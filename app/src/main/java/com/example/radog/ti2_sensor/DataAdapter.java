package com.example.radog.ti2_sensor;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by radog on 27/04/2017.
 */

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.DataViewHolder> {

    private List<String> lResultados;
    private Context con;

    public DataAdapter(List<String> lResultados, Context con) {
        this.lResultados = lResultados;
        this.con = con;
    }

    @Override
    public DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tarjeta_resultado, parent, false);
        return new DataViewHolder(v);
    }

    @Override
    public void onBindViewHolder(DataViewHolder holder, int position) {
        holder.tvInicio.setText(lResultados.get(position));
    }

    @Override
    public int getItemCount() {
        return lResultados.size();
    }


    public static class DataViewHolder extends RecyclerView.ViewHolder {

        public TextView tvInicio;

        public DataViewHolder(View itemView) {
            super(itemView);
            tvInicio = (TextView) itemView.findViewById(R.id.txtInicio);
        }
    }
}
