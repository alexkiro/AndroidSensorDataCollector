/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hymas.m1;

import android.app.Activity;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import hymas.m1.hardware.NoiseReduction;
import hymas.m1.hardware.SensorCapture;
import hymas.m1.view.SensorObserver;

/**
 *
 * @author kiro
 */
public class CalibrateActivity extends Activity {

    private SensorObserver obs;
    private NoiseReduction nr;
    private SensorCapture tsc = null;
    private SensorCapture trainSC = null;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.calibrate);
        LinearLayout layout = (LinearLayout) findViewById(R.id.calibrateLayout);
        obs = SensorObserver.createObserver(this, SensorCapture.getSensorList(this), layout);
    }

    
    @Override
    protected void onStop() {
        super.onStop(); 
        if (trainSC != null){
            trainSC.stopCapture();
        }
        if (tsc != null){
            tsc.stopCapture();
        }
    }
    
    

    public void onClickStartCalibrating(View view) {
        Toast.makeText(this, "Calibration initialized. Do NOT move the device!", Toast.LENGTH_LONG).show();
        final Button b1 = (Button) findViewById(R.id.startCalibrating);
        final Button b2 = (Button) findViewById(R.id.startTesting);
        final Button b3 = (Button) findViewById(R.id.saveCalibration);
        b1.setEnabled(false);
        b2.setEnabled(false);
        b3.setEnabled(false);
        trainSC = new SensorCapture(this, obs);
        nr = new NoiseReduction(SensorCapture.getSensorList(this));
        trainSC.startTrain(nr, 20000, new Runnable() {
            public void run() {
                b1.setEnabled(true);
                b2.setEnabled(true);
                b3.setEnabled(true);
            }
        }, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onClickStartTesting(View view) {        
        Button b = (Button) view;
        if (tsc == null) {
            Toast.makeText(this, "Starting Testing", Toast.LENGTH_SHORT).show();
            tsc = new SensorCapture(this, obs);
            if (nr != null) {
                tsc.setFilter(nr);
            }
            tsc.startMonitor(SensorManager.SENSOR_DELAY_NORMAL);
            b.setText("Stop Testing");
        } else {
            tsc.stopCapture();
            tsc = null;
            b.setText("Start Testing");
            Toast.makeText(this, "Stoping Testing", Toast.LENGTH_SHORT).show();
        }
    }
    
    public void onClickSaveCalibration(View view) {
        if (nr != null){
            //@TODO: serialize to file
            finish();
        } else {
            Toast.makeText(this, "No current calibration", Toast.LENGTH_SHORT).show();
        }
    }
}
