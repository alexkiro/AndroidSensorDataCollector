package hymas.m1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

/**
 * Handles the main activity of the application
 * @author kiro
 */
public class MainActivity extends Activity {

    private SensorCapture sc;
    private SensorObserver obs;
    private NoiseReduction nr = null;
    private Spinner spin;
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

        spin = (Spinner) findViewById(R.id.labelSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, Label.getNames());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);

        try {
            nr = NoiseReduction.deserialize(openFileInput("nr.obj"));
            System.err.println("nr = " + nr);
        } catch (FileNotFoundException ex) {
            nr = null;
        }
    }

    /**
     * Starts the CalibrateActivity
     * @param v 
     */
    public void onClickCalibrate(View v) {
        Intent in = new Intent(this, CalibrateActivity.class);
        startActivity(in);
    }

    /**
     * Starts collecting data to file.
     * Uses SharedPreferences file "default.pref" to remember the last file number.
     * The files are saved in SDCard/ANDROID/data/hymas.m1/files/
     * The files are named data &lt;number%gt;.xml
     * The number is saved in the preference "currentFile"
     * @param v 
     */
    public void onClickStartCollecting(View v) {
        if (sc == null) {
            SharedPreferences sp = getSharedPreferences("default.pref", Context.MODE_PRIVATE);
            int currentFile = sp.getInt("currentFile", 0);
            sp.edit().putInt("currentFile", currentFile + 1).commit();

            Label label = Label.valueOf((String) spin.getSelectedItem());

            File dir = getExternalFilesDir(null);
            File newFile = new File(dir, "data" + currentFile + ".xml");

            sc = new SensorCapture(this, obs);
            sc.setFilter(nr);
            sc.startCaptureAllToFile(newFile, label, gps.isChecked(), SensorManager.SENSOR_DELAY_NORMAL);

            button.setText("Stop Collecting");
            progress.setVisibility(View.VISIBLE);

        } else {
            sc.stopCapture();
            sc = null;
            button.setText("Start Collecting");
            progress.setVisibility(View.INVISIBLE);
        }

    }
}
