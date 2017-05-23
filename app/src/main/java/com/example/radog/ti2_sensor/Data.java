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

import com.github.lzyzsd.circleprogress.DonutProgress;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Data extends AppCompatActivity implements SensorEventListener, TextToSpeech.OnInitListener {

    private static int INI_MIN_ANG_Z;
    @BindView(R.id.tvNumRep)
    TextView tvNumRep;
    @BindView(R.id.TV_AnguloX)
    TextView TV_AnguloX;
    @BindView(R.id.TV_AnguloY)
    TextView TV_AnguloY;
    @BindView(R.id.TV_AnguloZ)
    TextView TV_AnguloZ;

    @BindView(R.id.D_PB_Bar1)
    DonutProgress PB_Bar1;

    @BindView(R.id.D_PB_Bar2)
    DonutProgress PB_Bar2;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private TextToSpeech textToSpeech; //para la voz
    private float AnguloX, AnguloY, AnguloZ;
    private float AnguloXMax, AnguloYMax, AnguloZMax;
    private boolean fIniFlexCodo, fEndElbowFlex; //para ir viendo la posición del dispositivo

    private final int INI_LIM_INF_ANG_Z = 0;
    private final int INI_LIM_SUP_ANG_Z = 10;
    //----------------------------------------

    private int REPETITIONS = 0; //conteo de repeticiones
    private final int FINAL_REP = 15;
    private final float GlobalInc = 100 / 15;
    private float GlobalScore;
    private double percent = 0; //eficiencia, fase de pruebas

    private ArrayList<String> resultados = new ArrayList<>();
    private ArrayList<float[]> lValues;
    private boolean fPosInicial;
    private boolean InsideInitPos;
    private int excersice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        ButterKnife.bind(this);

        lValues = new ArrayList<>();
        fPosInicial = false;
        fIniFlexCodo = true;
        fEndElbowFlex = true;

        InsideInitPos = true;

        AnguloXMax = 0;
        AnguloYMax = 0;
        AnguloZMax = 0;

        INI_MIN_ANG_Z = 0;
        GlobalScore = 0;

        textToSpeech = new TextToSpeech(this, this);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        PB_Bar1.setMax(90);
        PB_Bar2.setMax(100);
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

        //Obtiene el angulo relativo de z
        AnguloZ -= INI_MIN_ANG_Z;
        //Asegura que el algulo maximo registrado sea 90
        AnguloZ = (AnguloZ < 90) ? AnguloZ : 90;
        AnguloZ = (AnguloZ > 0) ? AnguloZ : 0;
        //Actualiza los angulos maximos
        AnguloXMax = (AnguloXMax > AnguloX) ? AnguloXMax : AnguloX;
        AnguloYMax = (AnguloYMax > AnguloY) ? AnguloYMax : AnguloY;
        AnguloZMax = (AnguloZMax > AnguloZ) ? AnguloZMax : AnguloZ;

        //Actualiza la barra de progreso de cada repeticion
        PB_Bar1.setProgress((int) AnguloZMax);

        TV_AnguloX.setText(String.format("%.2f", AnguloX));
        TV_AnguloY.setText(String.format("%.2f", AnguloY));
        TV_AnguloZ.setText(String.format("%.2f", AnguloZ));

        if (fPosInicial)
            if (InsideInitPos) {
                if (!isInInitialPos()) InsideInitPos = false;
            } else {
                if (isInInitialPos()) {
                    InsideInitPos = true;
                    initialPosition();
                    //Obtiene el porcentaje con respecto al maximo angulo alcanzado
                    GlobalScore += GlobalInc * (AnguloZMax / 90);
                    PB_Bar2.setProgress((int) GlobalScore);
                    REPETITIONS++;
                    speak(String.valueOf(REPETITIONS));
                    AnguloXMax = 0;
                    AnguloYMax = 0;
                    AnguloZMax = 0;
                }
            }

        chExercise();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
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
     * Asigna los valores para los atributos INI_LIM_INF e INI_LIM_SUP para los planos XYZ. Para la posición inicial
     */
    private void iniInitialValues(int[] X, int[] Y, int[] Z) {
        INI_MIN_ANG_Z = Z[0];
    }

    //boolean solo para poder detener la ejecución del código en ciertos casos
    private boolean chExercise() {
        if (!fPosInicial) {
            setInitPosition(); //Aun no estan especificados los valores iniciales
            if (lValues.size() != 3) {
                return false;
            }
        }
        tvNumRep.setText(REPETITIONS + "");
        if (REPETITIONS < FINAL_REP + 1) { //hizo todos las REPETITIONS?
            if (REPETITIONS != FINAL_REP) { //no es la última repetición??
                //speak(REPETITIONS + "");
                fIniFlexCodo = false; //para volver a la posición inicial
            } else {
                REPETITIONS = 0;
                fIniFlexCodo = fEndElbowFlex = true; //deshabilita ambos procesos
                //speak("Points: " + String.format("%.2f", percent));
            }
        }
        return false;
    }

    private void setInitPosition() {
        boolean flag = true;
        float[] tmpV, objV;
        float mayorX, menorX, mayorY, menorY, mayorZ, menorZ;
        lValues.add(new float[]{AnguloX, AnguloY, AnguloZ});

        timer(1); //un segundo

        if (lValues.size() == 3) {
            tmpV = lValues.get(0);
            menorX = tmpV[0];
            menorY = tmpV[1];
            menorZ = tmpV[2];

            for (int i = 1; i < lValues.size() && flag; ++i) {
                objV = lValues.get(i);

                if (Math.abs(tmpV[0] - objV[0]) <= 5 &&
                        Math.abs(tmpV[1] - objV[1]) <= 5 &&
                        Math.abs(tmpV[2] - objV[2]) <= 5) {

                    if (objV[0] < menorX) {
                        menorX = objV[0];
                    } else if (objV[1] < menorY) {
                        menorY = objV[1];
                    } else if (objV[2] < menorZ) {
                        menorZ = objV[2];
                    }
                } else {
                    //hubo un cambio brusco
                    lValues = new ArrayList<>();
                    flag = false;
                }
            }

            if (flag) {
                iniInitialValues(new int[]{(int) menorX}, new int[]{(int) menorY}, new int[]{(int) menorZ});
                fPosInicial = true;
                fIniFlexCodo = false;
            }
        }

    }

    private void timer(final int seconds) {
        Thread timer;
        timer = new Thread() {
            public void run() {
                try {
                    sleep(1000 * seconds);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        timer.start();
    }

    //CHECA LA POSICIÓN INICIAL DEL EJERCICIO
    private boolean initialPosition() {
        if (!fIniFlexCodo) { //el dispositivo está en la posicíon correcta?
            if (REPETITIONS == 0) {
                //speak("Ready");
            }

            fIniFlexCodo = true; //deshabilita esta parte
            fEndElbowFlex = false;//habilita la siguiente parte

            resultados.add("X: " + AnguloXMax + "\n" +
                    "Y: " + AnguloYMax + "\n" +
                    "Z: " + AnguloZMax + "\n");

            Log.println(Log.DEBUG, "tag", "X: " + AnguloXMax + "\n" + "Y: " + AnguloYMax + "\n" + "Z: " + AnguloZMax + "\n");
        }

        return fIniFlexCodo;
    }

    private boolean isInInitialPos() {
        if (InsideInitPos)
            return (INI_LIM_INF_ANG_Z <= AnguloZ && AnguloZ <= INI_LIM_SUP_ANG_Z + 10);
        else
            return (INI_LIM_INF_ANG_Z <= AnguloZ && AnguloZ <= INI_LIM_SUP_ANG_Z);
    }
}
