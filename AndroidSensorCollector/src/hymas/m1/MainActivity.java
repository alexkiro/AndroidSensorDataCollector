package hymas.m1;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import hymas.m1.hardware.NoiseReduction;
import hymas.m1.hardware.SensorCapture;
import hymas.m1.view.GraphSensorObserver;
import hymas.m1.view.SensorObserver;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainActivity extends Activity {

    private SensorCapture sc;
    private SensorObserver obs;
    private GraphViewSeries series;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        LinearLayout layout = (LinearLayout) findViewById(R.id.displayLayout);
//        obs = createObserver(((SensorManager) getSystemService(Context.SENSOR_SERVICE))
//                .getSensorList(Sensor.TYPE_ALL), layout);
        
        

       //layout.addView(graphView);

    }

    public SensorObserver createObserver(List<Sensor> sensorList, LinearLayout layout) {
        SensorObserver so = new SensorObserver();
        for (Sensor sensor : sensorList) {
            TextView tw = new TextView(this);
            layout.addView(tw);
            so.add(sensor, tw);
        }
        return so;
    }

    public void onClickStartCollecting(View view) {
        LinearLayout layout = (LinearLayout) findViewById(R.id.displayLayout);
        final GraphSensorObserver gso = new GraphSensorObserver(this);
        GraphView graphView = gso.createGraphView();
        layout.addView(graphView);
        sc = new SensorCapture(this);
        sc.setEventListener(new SensorEventListener() {

            public void onSensorChanged(SensorEvent se) {
                gso.notify(se);
            }

            public void onAccuracyChanged(Sensor sensor, int i) {                
            }
        });
        sc.startCaptureAll(SensorManager.SENSOR_DELAY_NORMAL);        
    }

    public void onClickStartsCollecting(View view) {
        sc = new SensorCapture(this);
        final NoiseReduction nr = new NoiseReduction(((SensorManager) getSystemService(Context.SENSOR_SERVICE))
                .getSensorList(Sensor.TYPE_ALL));

        sc.setEventListener(new SensorEventListener() {
            public void onSensorChanged(SensorEvent se) {
                nr.addEvent(se);
                obs.notify(se);
            }

            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        });

        sc.startCaptureAll(SensorManager.SENSOR_DELAY_NORMAL);

        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(10000);
                    sc.stopCapture();
                    Thread.sleep(500);
                    nr.calculateNoise();
                    Thread.sleep(2000);
                    a(nr);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();



//        sc = new SensorCapture(this, obs);
//        File dir = getExternalFilesDir(null);
//        File newFile = new File(dir, "myfile2.xml");
//        sc.startCaptureAllToFile(newFile, Label.run, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void a(final NoiseReduction nr) {
        sc = new SensorCapture(this, obs);
        sc.setEventListener(new SensorEventListener() {
            public void onSensorChanged(SensorEvent se) {
                if (nr.filter(se)) {
                    obs.notify(se);
                }
            }

            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        });
        sc.startCaptureAll(SensorManager.SENSOR_DELAY_NORMAL);

    }

    public void onClickStopCollecting(View view) {
        sc.stopCapture();
    }
}
