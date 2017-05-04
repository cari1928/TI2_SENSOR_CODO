package com.example.radog.ti2_sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.DonutProgress;

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
    @BindView(R.id.donut_progress)
    DonutProgress dpProgress;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private TextToSpeech textToSpeech; //para la voz
    private float AnguloX, AnguloY, AnguloZ;

    //por ahora no se ocupan
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
        tvType.setText("Flexo-Extensión de Codo");
        INI_LIM_INF_ANG_X = X[0] - 5;
        INI_LIM_SUP_ANG_X = INI_LIM_INF_ANG_X + 44;
        INI_LIM_INF_ANG_Y = Y[0] - 5;
        INI_LIM_SUP_ANG_Y = INI_LIM_INF_ANG_Y + 17;
        INI_LIM_INF_ANG_Z = Z[0] - 5;
        INI_LIM_SUP_ANG_Z = INI_LIM_INF_ANG_Z + 27;

    }

    /**
     * Asigna los valores para los atributos FIN_LIM_INF e INI_LIM_SUP para los planos XYZ. Para la posición final
     */
    private void iniFinalValues(int X[], int Y[], int Z[]) {
        FIN_LIM_INF_ANG_X = INI_LIM_INF_ANG_X + 48;
        FIN_LIM_SUP_ANG_X = INI_LIM_SUP_ANG_X + 40;
        FIN_LIM_INF_ANG_Y = INI_LIM_INF_ANG_Y - 16;
        FIN_LIM_SUP_ANG_Y = INI_LIM_SUP_ANG_Y - 9;
        FIN_LIM_INF_ANG_Z = INI_LIM_INF_ANG_Z + 112;
        FIN_LIM_SUP_ANG_Z = INI_LIM_SUP_ANG_Z + 115;
    }

    //boolean solo para poder detener la ejecución del código en ciertos casos
    private boolean chExercise() {
        if (!fPosInicial) {
            setInitPosition(); //especifica los valores iniciales
            if (lValues.size() != 3) {
                return false;
            }
        }

        int rX = (int) AnguloX - (int) tmpX;
        int rY = (int) AnguloY - (int) tmpY;
        int rZ = (int) AnguloZ - (int) tmpZ;

        //para pruebas
        /*Log.i("RESTAX", AnguloX + " - " + tmpX + " = " + rX);
        Log.i("RESTAY", AnguloY + " - " + tmpY + " = " + rY);
        Log.i("RESTAZ", AnguloZ + " - " + tmpZ + " = " + rZ);
        Log.i("FLAG", uFlag + "");*/

        if ((rX <= 0) || (0 <= rY) || (rZ <= 0)) {
            //para pruebas
            //Log.i("STATUS", "UP");

            //movimiento hacia arriba
            //los tmp tienen el momento antes de subir
            rX = Math.abs(rX);
            rY = Math.abs(rY);
            rZ = Math.abs(rZ);
            if ((0 > rX || rX > 10) || (0 > rY || rY > 5) || (0 > rZ || rZ > 10)) {
                if (uFlag) {
                    resultados.add(tmpX + " " + tmpY + " " + tmpZ); //arriba
                    uFlag = false;
                }
            }
        } else if ((0 <= rX) || (rY <= 0) || (0 <= rZ)) {
            //para pruebas
            //Log.i("STATUS", "DOWN");

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

                    resultados.add(tmpX + " " + tmpY + " " + tmpZ); //abajo

                    if (REPETITIONS < FINAL_REP) {
                        REPETITIONS++;
                        speak(REPETITIONS + "");
                        tvNumRep.setText(REPETITIONS + "");
                        calcEficiencia();
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

    private boolean calcEficiencia() {
        String[] parts = resultados.get(resultados.size() - 1).split(" "); //xyz fin
        String[] parts2 = resultados.get(resultados.size() - 2).split(" "); //xyz inicio
        float calif = (((chIniPos(parts2) + chFinPos(parts)) / 2) / 15) + 1;
        percent += calif;

        if (percent > 100) {
            percent = 100;
        }
        new DonutBar((int) percent).execute();
        return true;
    }

    /**
     * Promedio de la posición inicial en XYZ
     *
     * @param parts
     * @return
     */
    private Float chIniPos(String[] parts) {
        Float calif = 0f;
        if ((INI_LIM_INF_ANG_X <= Float.parseFloat(parts[0]) && Float.parseFloat(parts[0]) <= INI_LIM_SUP_ANG_X)) {
            calif += (100f / 3f);
        }
        if ((INI_LIM_INF_ANG_Y <= Float.parseFloat(parts[1]) && Float.parseFloat(parts[1]) <= INI_LIM_SUP_ANG_Y)) {
            calif += (100f / 3f);
        }
        if ((INI_LIM_INF_ANG_Y <= Float.parseFloat(parts[2]) && Float.parseFloat(parts[2]) <= INI_LIM_SUP_ANG_Y)) {
            calif += (100f / 3f);
        }
        return calif;
    }

    /**
     * Promedio de la posición final en XYZ
     *
     * @param parts
     * @return
     */
    private Float chFinPos(String[] parts) {
        Float calif = 0f;
        if ((FIN_LIM_INF_ANG_X <= Float.parseFloat(parts[0]) && Float.parseFloat(parts[0]) <= FIN_LIM_SUP_ANG_X)) {
            calif += (100f / 3f);
        }
        if ((FIN_LIM_INF_ANG_Y <= Float.parseFloat(parts[0]) && Float.parseFloat(parts[0]) <= FIN_LIM_SUP_ANG_Y)) {
            calif += (100f / 3f);
        }
        if ((FIN_LIM_INF_ANG_Y <= Float.parseFloat(parts[0]) && Float.parseFloat(parts[0]) <= FIN_LIM_SUP_ANG_Y)) {
            calif += (100f / 3f);
        }
        return calif;
    }

    public class DonutBar extends AsyncTask<Void, Integer, Integer> {

        private int cont;

        public DonutBar(int cont) {
            this.cont = cont;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dpProgress.setMax(100);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            dpProgress.setProgress(values[0]);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            publishProgress(cont);
            return null;
        }
    }
}
