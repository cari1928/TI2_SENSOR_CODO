package com.example.luisespino.demosensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements SensorEventListener, TextToSpeech.OnInitListener {

    @BindView(R.id.MA_TV_AnguloX)
    TextView TV_AnguloX;

    @BindView(R.id.MA_TV_AnguloY)
    TextView TV_AnguloY;

    @BindView(R.id.MA_TV_AnguloZ)
    TextView TV_AnguloZ;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private TextToSpeech textToSpeech;
    private float AnguloX, AnguloY, AnguloZ;
    //private Handler handler;
    private boolean fIniFlexCodo, fEndElbowFlex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        fIniFlexCodo = fEndElbowFlex = false;

        //handler = new Handler();
        textToSpeech = new TextToSpeech(this, this);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
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

        flexCodo();
    }

    //boolean solo para poder detener la ejecusión del código en ciertos casos
    private boolean flexCodo() {
        //puede modificar la bandera fIniFlexCodo
        if (!iniElbowFlex()) {
            return false;
        }

        //el brazo está en la posición correcta
        if (!endElbowFlex()) {
            return false;
        }

        return false;

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
                //speak("Initiating elbow flexoextension. Get in position");
            }

        } else {
            Log.e("TextToSpeach", "Inicialización del lenguaje fallida");
        }
    }

    private void speak(String msg) {
        textToSpeech.speak(msg, TextToSpeech.QUEUE_FLUSH, null);
    }

    //checa la posición inicial para el ejercicio flexoextension de codo
    private boolean iniElbowFlex() {
        if ((47 <= AnguloX && AnguloX <= 91) &&
                (77 <= AnguloY && AnguloY <= 94) &&
                (17 <= AnguloZ && AnguloZ <= 44)) {

            /*handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!fIniFlexCodo) { //el brazo está en la posicíon correcta?
                        speak("You are ready!!");
                        fIniFlexCodo = true; //si
                    }
                }
            }, 2000);*/

            if (!fIniFlexCodo) { //el brazo está en la posicíon correcta?
                speak("You are ready!!");
                fIniFlexCodo = true; //si
            }
        }
        return fIniFlexCodo;
    }

    private boolean endElbowFlex() {
        if ((95 <= AnguloX && AnguloX <= 131) &&
                (61 <= AnguloY && AnguloY <= 85) &&
                (129 <= AnguloZ && AnguloZ <= 159)) {

            if (!fEndElbowFlex) {
                speak("You are doing well");
                fEndElbowFlex = true;
            }
        }
        return fEndElbowFlex;
    }
}
