package com.example.radog.ti2_sensor;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.lvExercises)
    ListView lvExercises;

    private ListAdapter listAdapter;
    private String[] exercises = new String[]{
            "Flexoextensión de codo",
            "Flexoextensión de muñeca",
            "Pronosupinación"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, exercises);
        lvExercises.setAdapter(listAdapter);
        listViewListener();
    }

    private void listViewListener() {
        lvExercises.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent iData = new Intent(MainActivity.this, Data.class);
                Bundle data = new Bundle();

                data.putInt("TYPE", position);
                iData.putExtras(data);
                startActivity(iData);
            }
        });
    }
}
