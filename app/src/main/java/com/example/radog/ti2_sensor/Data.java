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

import java.util.ArrayList;
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

    private int INI_LIM_INF_ANG_Y;
    private int INI_LIM_SUP_ANG_Y;
    private int FIN_LIM_INF_ANG_Y;
    private int FIN_LIM_SUP_ANG_Y;

    private int INI_LIM_INF_ANG_Z;
    private int INI_LIM_SUP_ANG_Z;
    private int FIN_LIM_INF_ANG_Z;
    private int FIN_LIM_SUP_ANG_Z;
    //----------------------------------------

    private int REPETITIONS = 0; //conteo de repeticiones
    private final int FINAL_REP = 15;
    private double percent = 0; //eficiencia, fase de pruebas

    private ArrayList<String> resultados = new ArrayList<>();
    private ArrayList<String> lFinalRes = new ArrayList<>();
    private ArrayList<float[]> lValues;
    private boolean fPosInicial;
    private int excersice;
    private float tmpX, tmpY, tmpZ;
    private boolean uFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        ButterKnife.bind(this);

        Bundle data = getIntent().getExtras(); //Usé un bundle por si llegamos a mandar más datos
        excersice = data.getInt("TYPE"); //el tipo de ejercicio fue mandado desde MainActivity
        lValues = new ArrayList<>();
        fPosInicial = false; //desbloqueado
        fIniFlexCodo = true; //bloqueado
        fEndElbowFlex = true; //bloqueado
        tmpX = tmpY = tmpZ = 0; //inicializa
        uFlag = false;

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
        Bundle datos = new Bundle();
        Intent iListado;
        switch (item.getItemId()) {
            case R.id.itmIniResults:
                datos.putStringArrayList("LIST", resultados);
                iListado = new Intent(this, listado_resultados.class);
                iListado.putExtras(datos);
                startActivity(iListado);
                break;
            case R.id.itmFinResults:
                datos.putStringArrayList("LIST", lFinalRes);
                iListado = new Intent(this, listado_resultados.class);
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
        switch (excersice) {
            case 0:
                //flex codo
                tvType.setText("Flexo-Extensión de Codo");
                INI_LIM_INF_ANG_X = X[0] - 5;
                INI_LIM_SUP_ANG_X = INI_LIM_INF_ANG_X + 44;
                INI_LIM_INF_ANG_Y = Y[0] - 5;
                INI_LIM_SUP_ANG_Y = INI_LIM_INF_ANG_Y + 17;
                INI_LIM_INF_ANG_Z = Z[0] - 5;
                INI_LIM_SUP_ANG_Z = INI_LIM_INF_ANG_Z + 27;
                break;
            case 1:
                //flex muñeca
                tvType.setText("Flexo-Extensión de Muñeca");
                INI_LIM_INF_ANG_X = X[0] - 5;
                INI_LIM_SUP_ANG_X = INI_LIM_INF_ANG_X + 34;
                INI_LIM_INF_ANG_Y = Y[0] - 5;
                INI_LIM_SUP_ANG_Y = INI_LIM_INF_ANG_Y + 18;
                INI_LIM_INF_ANG_Z = Z[0] - 5;
                INI_LIM_SUP_ANG_Z = INI_LIM_INF_ANG_Z + 29;
                break;
            case 2:
                //pronosup
                tvType.setText("Pronosupinación");
                INI_LIM_INF_ANG_X = X[0] - 5;
                INI_LIM_SUP_ANG_X = INI_LIM_INF_ANG_X + 11;
                INI_LIM_INF_ANG_Y = Y[0] - 5;
                INI_LIM_SUP_ANG_Y = INI_LIM_INF_ANG_Y + 19;
                INI_LIM_INF_ANG_Z = Z[0] - 5;
                INI_LIM_SUP_ANG_Z = INI_LIM_INF_ANG_Z + 17;
                break;
        }
    }

    /**
     * Asigna los valores para los atributos FIN_LIM_INF e INI_LIM_SUP para los planos XYZ. Para la posición final
     */
    private void iniFinalValues(int X[], int Y[], int Z[]) {
        switch (excersice) {
            case 0:
                //flex codo
                /*FIN_LIM_INF_ANG_X = INI_LIM_INF_ANG_X + 48;
                FIN_LIM_SUP_ANG_X = INI_LIM_SUP_ANG_X + 40;
                FIN_LIM_INF_ANG_Y = INI_LIM_INF_ANG_Y - 16;
                FIN_LIM_SUP_ANG_Y = INI_LIM_SUP_ANG_Y - 9;
                FIN_LIM_INF_ANG_Z = INI_LIM_INF_ANG_Z + 112;
                FIN_LIM_SUP_ANG_Z = INI_LIM_SUP_ANG_Z + 115;*/
                FIN_LIM_INF_ANG_X = 105;
                FIN_LIM_SUP_ANG_X = 140;
                FIN_LIM_INF_ANG_Y = 65;
                FIN_LIM_SUP_ANG_Y = 85;
                FIN_LIM_INF_ANG_Z = 130;
                FIN_LIM_SUP_ANG_Z = 145;
                break;
            case 1:
                //flex muñeca
                FIN_LIM_INF_ANG_X = INI_LIM_INF_ANG_X + 115;
                FIN_LIM_SUP_ANG_X = FIN_LIM_INF_ANG_X + 20;
                FIN_LIM_INF_ANG_Y = INI_LIM_INF_ANG_Y - 10;
                FIN_LIM_SUP_ANG_Y = FIN_LIM_INF_ANG_Y + 19;
                FIN_LIM_INF_ANG_Z = INI_LIM_INF_ANG_Z - 35;
                FIN_LIM_SUP_ANG_Z = FIN_LIM_INF_ANG_Z + 16;
                break;
            case 2:
                //pronosup
                FIN_LIM_INF_ANG_X = INI_LIM_INF_ANG_X - 3;
                FIN_LIM_SUP_ANG_X = FIN_LIM_INF_ANG_X + 17;
                FIN_LIM_INF_ANG_Y = INI_LIM_INF_ANG_Y + 3;
                FIN_LIM_SUP_ANG_Y = FIN_LIM_INF_ANG_Y + 17;
                FIN_LIM_INF_ANG_Z = INI_LIM_INF_ANG_Z + 166;
                FIN_LIM_SUP_ANG_Z = FIN_LIM_INF_ANG_Z + 14;
                break;
        }
    }

    //boolean solo para poder detener la ejecución del código en ciertos casos
    private boolean chExercise() {
        if (!fPosInicial) {
            setInitPosition(); //Aun no estan especificados los valores iniciales
            if (lValues.size() != 3) {
                return false;
            }
        }

        int rX = (int) AnguloX - (int) tmpX;
        int rY = (int) AnguloY - (int) tmpY;
        int rZ = (int) AnguloZ - (int) tmpZ;

        Log.i("RESTAX", AnguloX + " - " + tmpX + " = " + rX);
        Log.i("RESTAY", AnguloY + " - " + tmpY + " = " + rY);
        Log.i("RESTAZ", AnguloZ + " - " + tmpZ + " = " + rZ);
        Log.i("FLAG", uFlag + "");

        if ((rX <= 0) || (0 <= rY) || (rZ <= 0)) {
            Log.i("STATUS", "UP");
            //movimiento hacia arriba
            //los tmp tienen el momento antes de subir
            rX = Math.abs(rX);
            rY = Math.abs(rY);
            rZ = Math.abs(rZ);
            if ((0 > rX || rX > 10) || (0 > rY || rY > 5) || (0 > rZ || rZ > 10)) {
                if (uFlag) {
                    resultados.add("ARRIBA " + tmpX + " " + tmpY + " " + tmpZ);
                    uFlag = false;
                }
            }
        } else if ((0 <= rX) || (rY <= 0) || (0 <= rZ)) {
            Log.i("STATUS", "DOWN");
            //movimiento hacia abajo
            //los tmp tienen el movimiento antes de bajar
            rX = Math.abs(rX);
            rY = Math.abs(rY);
            rZ = Math.abs(rZ);

            if ((0 > rX || rX > 3) && (0 > rY || rY > 5) && (0 > rZ || rZ > 3)) {
                if (!uFlag) {
                    Log.i("ABS", rX + "");
                    Log.i("ABS", rY + "");
                    Log.i("ABS", rZ + "");

                    resultados.add("ABAJO " + tmpX + " " + tmpY + " " + tmpZ);

                    REPETITIONS++;
                    speak(REPETITIONS + "");
                    tvNumRep.setText(REPETITIONS + "");

                    if (REPETITIONS == 15) {
                        Bundle datos = new Bundle();
                        datos.putStringArrayList("LIST", resultados);
                        Intent iListado = new Intent(this, listado_resultados.class);
                        iListado.putExtras(datos);
                        startActivity(iListado);
                    }
                    uFlag = true;
                }
            }
        }
        tmpX = AnguloX;
        tmpY = AnguloY;
        tmpZ = AnguloZ;
        return true;
    }

    private void setInitPosition() {
        boolean flag = true;
        float[] tmpV, objV;
        float menorX, menorY, menorZ;
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
                iniFinalValues(new int[]{(int) menorX}, new int[]{(int) menorY}, new int[]{(int) menorZ});

                tmpX = menorX;
                tmpY = menorY;
                tmpZ = menorZ;
                uFlag = true;

                resultados.add("Inicio: " + tmpX + " " + tmpY + " " + tmpZ);

                speak("Ready");

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
        if ((INI_LIM_INF_ANG_X <= AnguloX && AnguloX <= INI_LIM_SUP_ANG_X) &&
                (INI_LIM_INF_ANG_Y <= AnguloY && AnguloY <= INI_LIM_SUP_ANG_Y) &&
                (INI_LIM_INF_ANG_Z <= AnguloZ && AnguloZ <= INI_LIM_SUP_ANG_Z)) {

            if (!fIniFlexCodo) { //el dispositivo está en la posicíon correcta?
                if (REPETITIONS == 0) {
                    speak("Ready");
                }

                fIniFlexCodo = true; //deshabilita esta parte
                fEndElbowFlex = false;//habilita la siguiente parte

                resultados.add("X: " + AnguloX + "\n" +
                        "Y: " + AnguloY + "\n" +
                        "Z: " + AnguloZ + "\n");

                tmpX = AnguloX;
                tmpY = AnguloY;
                tmpZ = AnguloZ;
            }
        }
        return fIniFlexCodo;
    }

    //Verifica la posición final de flex codo
    private boolean finalPosition() {

        if (fEndElbowFlex) {
            return true;
        }

        //flex codo
        //X VA EN AUMENTO
        //Y VA DISMINUYENDO
        //Z VA EN AUMENTO
        if (tmpX == 0 && tmpY == 0 && tmpZ == 0) {
            tmpX = AnguloX;
            tmpY = AnguloY;
            tmpZ = AnguloZ;

            Log.i("INFO", "INICIO");
            Log.i("INFOX", tmpX + "");
            Log.i("INFOY", tmpY + "");
            Log.i("INFOZ", tmpZ + "");

        } else if (AnguloX < (tmpX - 5) ||
                AnguloY > (tmpY + 5) ||
                AnguloZ < (tmpZ - 5)) {

            lFinalRes.add("X: " + AnguloX + "\n" +
                    "Y: " + AnguloY + "\n" +
                    "Z: " + AnguloZ + "\n");

            fEndElbowFlex = true; //el dispositivo esta en la posición correcta
            ++REPETITIONS;
            tvNumRep.setText(REPETITIONS + "");

            Log.i("INFO", "PLANOS");
            Log.i("INFOX", AnguloX + "");
            Log.i("INFOY", AnguloY + "");
            Log.i("INFOZ", AnguloZ + "");

            Log.i("INFO", "LIMPIA");
            Log.i("INFOX", tmpX + "");
            Log.i("INFOY", tmpY + "");
            Log.i("INFOZ", tmpZ + "");

            tmpX = tmpY = tmpZ = 0;

        } else {

            Log.i("INFO", "ANTES");
            Log.i("INFOX", tmpX + "");
            Log.i("INFOY", tmpY + "");
            Log.i("INFOZ", tmpZ + "");

            tmpX = AnguloX;
            tmpY = AnguloY;
            tmpZ = AnguloZ;

            Log.i("INFO", "DESPUES");
            Log.i("INFOX", tmpX + "");
            Log.i("INFOY", tmpY + "");
            Log.i("INFOZ", tmpZ + "");
        }

        return fEndElbowFlex;
    }

}
