package com.example.radog.ti2_sensor;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

public class listado_resultados extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager adminLayout;
    private ArrayList<String> lResultados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado_resultados);

        Bundle datos = getIntent().getExtras();
        lResultados = datos.getStringArrayList("LIST");

        recyclerView = (RecyclerView) findViewById(R.id.rvReciclador);
        recyclerView.setHasFixedSize(true);

        adapter = new DataAdapter(lResultados, this);
        recyclerView.setAdapter(adapter);

        adminLayout = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(adminLayout);
    }
}
