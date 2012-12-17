package hymas.m1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Spinner;
import hymas.m1.collecter.Label;
import hymas.m1.hardware.NoiseReduction;
import hymas.m1.hardware.SensorCapture;
import hymas.m1.view.SensorObserver;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

/**
 * Handles the main activity of the application
 *
 * @author kiro
 */
public class MainActivity extends Activity {

    private final List<String> RATES = Arrays.asList("UI", "NORMAL", "GAME", "FASTEST");
    private SensorCapture sc;
    private SensorObserver obs;
    private NoiseReduction nr = null;
    private Spinner labelSpin;
    private Spinner rateSpin;
    private Button button;
    private ProgressBar progress;
    private CheckBox gps;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        button = (Button) findViewById(R.id.collectButton);
        progress = (ProgressBar) findViewById(R.id.marker_progress);
        progress.setVisibility(View.INVISIBLE);
        gps = (CheckBox) findViewById(R.id.checkGps);
        gps.setChecked(true);

        labelSpin = (Spinner) findViewById(R.id.labelSpinner);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, Label.getNames());
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        labelSpin.setAdapter(adapter1);

        rateSpin = (Spinner) findViewById(R.id.rateSpinner);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, RATES);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rateSpin.setAdapter(adapter2);
        rateSpin.setSelection(1);

        try {
            nr = NoiseReduction.deserialize(openFileInput("nr.obj"));
            System.err.println("nr = " + nr);
        } catch (FileNotFoundException ex) {
            nr = null;
        }
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * Starts the CalibrateActivity
     *
     * @param v
     */
    public void onClickCalibrate(View v) {
        Intent in = new Intent(this, CalibrateActivity.class);
        startActivity(in);
    }

    /**
     * Starts collecting data to file. Uses SharedPreferences file
     * "default.pref" to remember the last file number. The files are saved in
     * SDCard/ANDROID/data/hymas.m1/files/ The files are named data
     * &lt;number%gt;.xml The number is saved in the preference "currentFile"
     *
     * @param v
     */
    public void onClickStartCollecting(View v) {
        SharedPreferences sp = getSharedPreferences("default.pref", Context.MODE_PRIVATE);
        int currentFile = sp.getInt("currentFile", 0);
        if (sc == null) {
            Label label = Label.valueOf((String) labelSpin.getSelectedItem());

            File dir = getExternalFilesDir(null);
            File newFile = new File(dir, "data" + currentFile + ".xml");

            sc = new SensorCapture(this, obs);
            sc.setFilter(nr);
            sc.startCaptureAllToFile(newFile, label, gps.isChecked(), getSelectedRate());

            button.setText("Stop Collecting");
            progress.setVisibility(View.VISIBLE);

        } else {            
            sc.stopCapture();
            sc = null;
            sp.edit().putInt("currentFile", currentFile + 1).commit();
            button.setText("Start Collecting");
            progress.setVisibility(View.INVISIBLE);
        }
    }

    private int getSelectedRate() {
        int i = RATES.indexOf(rateSpin.getSelectedItem());
        switch (i) {
            case 0:
                return SensorManager.SENSOR_DELAY_UI;
            case 1:
                return SensorManager.SENSOR_DELAY_NORMAL;
            case 2:
                return SensorManager.SENSOR_DELAY_GAME;
            case 3:
                return SensorManager.SENSOR_DELAY_FASTEST;
            default:
                return SensorManager.SENSOR_DELAY_NORMAL;
        }
    }
}
