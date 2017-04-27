package com.example.radog.ti2_sensor;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Data extends AppCompatActivity implements SensorEventListener, TextToSpeech.OnInitListener {

    @BindView(R.id.tvNumRep)
    TextView tvNumRep;
    @BindView(R.id.tvType)
    TextView tvType;
    @BindView(R.id.TV_AnguloX)
    TextView TV_AnguloX;
    @BindView(R.id.TV_AnguloY)
    TextView TV_AnguloY;
    @BindView(R.id.TV_AnguloZ)
    TextView TV_AnguloZ;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private TextToSpeech textToSpeech; //para la voz
    private float AnguloX, AnguloY, AnguloZ;
    private boolean fIniFlexCodo, fEndElbowFlex; //para ir viendo la posición del dispositivo

    //LIMITES PARA LOS 3 PLANOS------------
    private int INI_LIM_INF_ANG_X;
    private int INI_LIM_SUP_ANG_X;
    private int FIN_LIM_INF_ANG_X;
    private int FIN_LIM_SUP_ANG_X;
    //fase de pruebas
    private int MOV_LIM_INF_ANG_X;
    private int MOV_LIM_SUP_ANG_X;

    private int INI_LIM_INF_ANG_Y;
    private int INI_LIM_SUP_ANG_Y;
    private int FIN_LIM_INF_ANG_Y;
    private int FIN_LIM_SUP_ANG_Y;
    //fase de pruebas
    private int MOV_LIM_INF_ANG_Y;
    private int MOV_LIM_SUP_ANG_Y;

    private int INI_LIM_INF_ANG_Z;
    private int INI_LIM_SUP_ANG_Z;
    private int FIN_LIM_INF_ANG_Z;
    private int FIN_LIM_SUP_ANG_Z;
    //fase de pruebas
    private int MOV_LIM_INF_ANG_Z;
    private int MOV_LIM_SUP_ANG_Z;
    //----------------------------------------

    private int REPETITIONS = 0; //conteo de repeticiones
    private final int FINAL_REP = 15;
    private double percent = 0; //eficiencia, fase de pruebas

    private ArrayList<String> resultados = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        ButterKnife.bind(this);

        iniValues();

        fIniFlexCodo = false; //el dispositivo no está ni en la posición inicial ni en la final
        fEndElbowFlex = true;

        textToSpeech = new TextToSpeech(this, this);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itmResultados:
                Bundle datos = new Bundle();
                datos.putStringArrayList("LIST", resultados);
                Intent iListado = new Intent(this, listado_resultados.class);
                iListado.putExtras(datos);
                startActivity(iListado);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final float A = event.values[0];
        final float B = event.values[1];
        final float C = event.values[2];
        final float k = (float) (1 / Math.sqrt(A * A + B * B + C * C));

        AnguloX = (float) (Math.acos(k * A) * 180f / Math.PI);
        AnguloY = (float) (Math.acos(k * B) * 180f / Math.PI);
        AnguloZ = (float) (Math.acos(k * C) * 180f / Math.PI);

        TV_AnguloX.setText(String.format("%.2f", AnguloX));
        TV_AnguloY.setText(String.format("%.2f", AnguloY));
        TV_AnguloZ.setText(String.format("%.2f", AnguloZ));

        chExercise();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onInit(int status) {
        int lenguaje;

        if (status == TextToSpeech.SUCCESS) {
            lenguaje = textToSpeech.setLanguage(Locale.getDefault()); //para que tome el idioma del dispositivo

            if (lenguaje == TextToSpeech.LANG_NOT_SUPPORTED || lenguaje == TextToSpeech.LANG_MISSING_DATA) {
                Log.e("TexToSpeech", "Este lenguaje no es soportado");
            } else {
                //speak(""); //se podría poner un mensaje al cargar este objeto
            }
        } else {
            Log.e("TextToSpeach", "Inicialización del lenguaje fallida");
        }
    }

    /**
     * Reproduce un sonido de voz correspondiente al mensaje enviado como parámetro
     *
     * @param msg Mensaje
     */
    private void speak(String msg) {
        textToSpeech.speak(msg, TextToSpeech.QUEUE_FLUSH, null);
    }

    /**
     * Inicializa los atributos de límite inferior y superior correspondientes al tipo de ejercicio:
     * Flexoextensión de codo = 0
     * Flexoextensión de muñeca = 1
     * Pronosupinación = 2
     */
    private void iniValues() {
        Bundle data = getIntent().getExtras(); //Usé un bundle por si llegamos a mandar más datos
        int res = data.getInt("TYPE"); //el tipo de ejercicio fue mandado desde MainActivity
        iniInitialValues(res); //coloca los límites superiores e inferiores para los 3 planos en la posición inicial
        //iniMoveValues(res); //fase de pruebas
        iniFinalValues(res); //coloca los límites superiores e inferiores para los 3 planos en la posición final
    }

    /**
     * Asigna los valores para los atributos INI_LIM_INF e INI_LIM_SUP para los planos XYZ. Para la posición inicial
     *
     * @param type 0 = flex codo, 1 = flex muñeca, 2 = pronosup
     */
    private void iniInitialValues(int type) {
        switch (type) {
            case 0:
                //flex codo
                tvType.setText("Flexo-Extensión de Codo");
                INI_LIM_INF_ANG_X = 47;
                INI_LIM_SUP_ANG_X = 91;
                INI_LIM_INF_ANG_Y = 77;
                INI_LIM_SUP_ANG_Y = 94;
                INI_LIM_INF_ANG_Z = 17;
                INI_LIM_SUP_ANG_Z = 44;
                break;
            case 1:
                //flex muñeca
                tvType.setText("Flexo-Extensión de Muñeca");
                INI_LIM_INF_ANG_X = 36;
                INI_LIM_SUP_ANG_X = 70;
                INI_LIM_INF_ANG_Y = 71;
                INI_LIM_SUP_ANG_Y = 89;
                INI_LIM_INF_ANG_Z = 126;
                INI_LIM_SUP_ANG_Z = 155;
                break;
            case 2:
                //pronosup
                tvType.setText("Pronosupinación");
                INI_LIM_INF_ANG_X = 84;
                INI_LIM_SUP_ANG_X = 95;
                INI_LIM_INF_ANG_Y = 72;
                INI_LIM_SUP_ANG_Y = 91;
                INI_LIM_INF_ANG_Z = 0;
                INI_LIM_SUP_ANG_Z = 17;
                break;
        }
    }

    /**
     * Asigna los valores para los atributos FIN_LIM_INF e INI_LIM_SUP para los planos XYZ. Para la posición final
     *
     * @param type 0 = flex codo, 1 = flex muñeca, 2 = pronosup
     */
    private void iniFinalValues(int type) {
        switch (type) {
            case 0:
                //flex codo
                FIN_LIM_INF_ANG_X = 95;
                FIN_LIM_SUP_ANG_X = 131;
                FIN_LIM_INF_ANG_Y = 61;
                FIN_LIM_SUP_ANG_Y = 85;
                FIN_LIM_INF_ANG_Z = 129;
                FIN_LIM_SUP_ANG_Z = 159;
                break;
            case 1:
                //flex muñeca
                FIN_LIM_INF_ANG_X = 151;
                FIN_LIM_SUP_ANG_X = 171;
                FIN_LIM_INF_ANG_Y = 69;
                FIN_LIM_SUP_ANG_Y = 88;
                FIN_LIM_INF_ANG_Z = 95;
                FIN_LIM_SUP_ANG_Z = 111;
                break;
            case 2:
                //pronosup
                FIN_LIM_INF_ANG_X = 81;
                FIN_LIM_SUP_ANG_X = 98;
                FIN_LIM_INF_ANG_Y = 75;
                FIN_LIM_SUP_ANG_Y = 92;
                FIN_LIM_INF_ANG_Z = 166;
                FIN_LIM_SUP_ANG_Z = 180;
                break;
        }
    }

    /**
     * Asigna los valores para los atributos MOV_LIM_INF e INI_LIM_SUP para los planos XYZ. Para el movimiento del ejercicio
     * Fase de Pruebas
     *
     * @param type 0 = flex codo, 1 = flex muñeca, 2 = pronosup
     * @deprecated
     */
    private void iniMoveValues(int type) {
        switch (type) {
            case 0:
                //flex codo
                MOV_LIM_INF_ANG_X = 64;
                MOV_LIM_SUP_ANG_X = 165;
                MOV_LIM_INF_ANG_Y = 55;
                MOV_LIM_SUP_ANG_Y = 88;
                MOV_LIM_INF_ANG_Z = 7;
                MOV_LIM_SUP_ANG_Z = 159;
                break;
            case 1:
                //flex muñeca
                //TODO, OBTENER LOS VALORES
                break;
            case 2:
                //pronosup
                //TODO, OBTENER LOS VALORES
                break;
        }
    }

    //boolean solo para poder detener la ejecución del código en ciertos casos
    private boolean chExercise() {

        if (!initialPosition()) { //puede modificar la bandera fIniFlexCodo
            return false;
        }
        //habilita la siguiente parte
        fEndElbowFlex = false; //el brazo está en la posición correcta

        //chMovement(); //por ahora no es necesario

        if (!finalPosition()) { //puede modificar la bandera fEndElbowFlex
            return false;
        }

        tvNumRep.setText(REPETITIONS + "");
        if (REPETITIONS < FINAL_REP + 1) { //hizo todos las REPETITIONS?
            if (REPETITIONS != FINAL_REP) { //no es la última repetición??
                speak(REPETITIONS + "");
                fIniFlexCodo = false; //para volver a la posición inicial
            } else {
                REPETITIONS = 0;
                fIniFlexCodo = fEndElbowFlex = true; //deshabilita ambos procesos
                speak("You finished this excercise. Total Points: " + String.format("%.2f", percent));
            }
        }
        return false;
    }

    //CHECA LA POSICIÓN INICIAL DEL EJERCICIO
    private boolean initialPosition() {
        if ((INI_LIM_INF_ANG_X <= AnguloX && AnguloX <= INI_LIM_SUP_ANG_X) &&
                (INI_LIM_INF_ANG_Y <= AnguloY && AnguloY <= INI_LIM_SUP_ANG_Y) &&
                (INI_LIM_INF_ANG_Z <= AnguloZ && AnguloZ <= INI_LIM_SUP_ANG_Z)) {

            if (!fIniFlexCodo) { //el dispositivo está en la posicíon correcta?
                if (REPETITIONS == 0) {
                    speak("You are ready!!");
                } else {
                    speak("continue");
                }
                ++REPETITIONS;
                fIniFlexCodo = true; //deshabilita esta parte

                resultados.add("X: " + AnguloX + "\n" +
                        "Y: " + AnguloY + "\n" +
                        "Z: " + AnguloZ + "\n");
                /*Toast.makeText(this, "X: " + AnguloX + "\n" +
                        "Y: " + AnguloY + "\n" +
                        "Z: " + AnguloZ + "\n", Toast.LENGTH_SHORT).show();*/
            }
        }
        return fIniFlexCodo;
    }

    //Verifica la posición final de flex codo
    private boolean finalPosition() {
        if ((FIN_LIM_INF_ANG_X <= AnguloX && AnguloX <= FIN_LIM_SUP_ANG_X) &&
                (FIN_LIM_INF_ANG_Y <= AnguloY && AnguloY <= FIN_LIM_SUP_ANG_Y) &&
                (FIN_LIM_INF_ANG_Z <= AnguloZ && AnguloZ <= FIN_LIM_SUP_ANG_Z)) {

            if (!fEndElbowFlex) {
                resultados.add("X: " + AnguloX + "\n" +
                        "Y: " + AnguloY + "\n" +
                        "Z: " + AnguloZ + "\n");
                /*Toast.makeText(this, "X: " + AnguloX + "\n" +
                        "Y: " + AnguloY + "\n" +
                        "Z: " + AnguloZ + "\n", Toast.LENGTH_SHORT).show();*/

                fEndElbowFlex = true; //el dispositivo esta en la posición correcta
            }
        }
        return fEndElbowFlex;
    }

    /**
     * Verifica la eficiencia
     * Fase de pruebas
     *
     * @deprecated
     */
    private void chMovement() {
        if ((64 <= AnguloX && AnguloX <= 165) &&
                (55 <= AnguloY && AnguloY <= 88) &&
                (7 <= AnguloZ && AnguloZ <= 159)) {
            if (percent < 100) {
                percent += 6.7;
            }
        } else {
            if (percent != 0) {
                percent -= 6.7;
            }
        }
    }

}
