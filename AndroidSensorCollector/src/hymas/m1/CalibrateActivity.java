package hymas.m1;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import hymas.m1.hardware.NoiseReduction;
import hymas.m1.hardware.SensorCapture;
import hymas.m1.view.SensorObserver;
import hymas.m1.view.TextViewSensorObserver;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This Activity is responsible for the calibrating the noise reduction module.
 * In order to eliminate sensor fluctuations while the device is stable.
 * @author Chirila Alexandru
 */
public class CalibrateActivity extends Activity {

    private final long TIME = 20000; //time to run the calibrating algorithm
    private final int RATE = SensorManager.SENSOR_DELAY_NORMAL;
    private SensorObserver obs;
    private NoiseReduction nr = null;
    private SensorCapture tsc = null; // test sensor capture, used durring sensor test
    private SensorCapture trainSC = null; // train sensor capture, used to train a calibrating algorithm

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.calibrate);
        LinearLayout layout = (LinearLayout) findViewById(R.id.calibrateLayout);
        obs = TextViewSensorObserver.createObserver(
                this, SensorCapture.getSensorList(this), layout);
        try {
            nr = NoiseReduction.deserialize(openFileInput("nr.obj"));
            System.err.println("nr = " + nr);
        } catch (FileNotFoundException ex) {
            nr = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (trainSC != null) {
            trainSC.stopCapture();
        }
        if (tsc != null) {
            tsc.stopCapture();
        }
    }

    /**
     * Start the calibrating algorithm. While running the buttons are disabled.
     * @param view 
     */
    public void onClickStartCalibrating(View view) {
        Toast.makeText(this, "Calibration initialized. Do NOT move the device!", Toast.LENGTH_LONG).show();
        final Button b1 = (Button) findViewById(R.id.startCalibrating);
        final Button b2 = (Button) findViewById(R.id.startTesting);
        final Button b3 = (Button) findViewById(R.id.saveCalibration);
        final Button b4 = (Button) findViewById(R.id.deleteCalibration);
        b1.setEnabled(false);
        b2.setEnabled(false);
        b3.setEnabled(false);
        b4.setEnabled(false);
        trainSC = new SensorCapture(this, obs);
        nr = new NoiseReduction(SensorCapture.getSensorList(this));
        trainSC.startTrain(nr, TIME, new Runnable() {
            public void run() {
                b1.setEnabled(true);
                b2.setEnabled(true);
                b3.setEnabled(true);
                b4.setEnabled(true);
            }
        }, RATE);
    }

    /**
     * Start monitoring the devices with Noise Reduction if available
     * @param view 
     */
    public void onClickStartTesting(View view) {
        Button b = (Button) view;
        if (tsc == null) {
            Toast.makeText(this, "Starting Testing", Toast.LENGTH_SHORT).show();
            tsc = new SensorCapture(this, obs);
            if (nr != null) {
                tsc.setFilter(nr);
            }
            tsc.startMonitor(RATE);
            b.setText("Stop Testing");
        } else {
            tsc.stopCapture();
            tsc = null;
            b.setText("Start Testing");
            Toast.makeText(this, "Stoping Testing", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Saves the calibration to file "nr.obj" internally, and closes the activity.
     * @param view 
     */
    public void onClickSaveCalibration(View view) {
        if (nr != null) {
            try {
                FileOutputStream fileOut = openFileOutput("nr.obj", Context.MODE_PRIVATE);
                NoiseReduction.serialize(fileOut, nr);
                finish();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(CalibrateActivity.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            Toast.makeText(this, "No current calibration", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Deletes the calibration file and object
     * @param view 
     */
    public void onClickDeleteCalibration(View view) {

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        deleteFile("nr.obj");
                        nr = null;
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();

    }
}
