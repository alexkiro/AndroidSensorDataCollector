/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hymas.m1.hardware;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import collecter.Collecter;
import collecter.Label;
import hymas.m1.view.SensorObserver;
import java.io.File;
import java.util.List;

/**
 *
 * @author Chirila Alexandru
 */
public class SensorCapture {

    private SensorManager sm;
    private Collecter coll = null;
    private List<Sensor> sensorList;
    private SensorObserver so = null;
    private SensorEventListener sl = new SensorEventListener() {
        public void onSensorChanged(SensorEvent se) {
            float[] v = se.values;
            try {
                if (so != null){
                    so.notify(se);
                }
                switch (se.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                        coll.addAccelerometerData(v[0], v[1], v[2], se.timestamp);
                        break;
                    case Sensor.TYPE_AMBIENT_TEMPERATURE:
                        coll.addAmbientTemperatureData(v[0], se.timestamp);
                        break;
                    case Sensor.TYPE_GRAVITY:
                        coll.addGravityData(v[0], v[1], v[2], se.timestamp);
                        break;
                    case Sensor.TYPE_GYROSCOPE:
                        coll.addGyroscopeData(v[0], v[1], v[2], se.timestamp);
                        break;
                    case Sensor.TYPE_LIGHT:
                        coll.addLightData(v[0], se.timestamp);
                        break;
                    case Sensor.TYPE_LINEAR_ACCELERATION:
                        coll.addLinearAccelerationData(v[0], v[1], v[2], se.timestamp);
                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        coll.addMagneticFieldData(v[0], v[1], v[2], se.timestamp);
                        break;
                    case Sensor.TYPE_PRESSURE:
                        coll.addPressureData(v[0], se.timestamp);
                        break;
                    case Sensor.TYPE_PROXIMITY:
                        coll.addProximityData(v[0], se.timestamp);
                        break;
                    case Sensor.TYPE_RELATIVE_HUMIDITY:
                        coll.addRelativeHumidityData(v[0], se.timestamp);
                        break;
                    case Sensor.TYPE_ROTATION_VECTOR:
                        coll.addVectorRotationData(v[0], v[1], v[2], se.timestamp);
                        break;
                    case Sensor.TYPE_TEMPERATURE:
                        coll.addAmbientTemperatureData(v[0], se.timestamp);
                        break;
                }
            } catch (NullPointerException ex) {
            }
        }

        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };

    public SensorCapture(Context ct) {
        sm = (SensorManager) ct.getSystemService(Context.SENSOR_SERVICE);
        sensorList = sm.getSensorList(Sensor.TYPE_ALL);
    }
    
    public SensorCapture(Context ct, SensorObserver so){
        this(ct);
        this.so = so;
    }
    
    public void setEventListener(SensorEventListener sl){
        this.sl = sl;
    }
    
    public void startCaptureAll(int rate){
        for (Sensor sensor : sensorList) {
            sm.registerListener(sl, sensor, rate);
        }
    }

    public void startCaptureAllToFile(File file, Label action, int rate) {
        coll = new Collecter(file, action);
        coll.startCollecting();
        startCaptureAll(rate);
    }

    public void stopCapture() {
        for (Sensor sensor : sensorList) {
            sm.unregisterListener(sl, sensor);
        }
        //coll.stopCollecting();
    }
}
