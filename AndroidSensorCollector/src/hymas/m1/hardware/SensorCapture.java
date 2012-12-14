package hymas.m1.hardware;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import hymas.m1.collecter.Collecter;
import hymas.m1.collecter.Label;
import hymas.m1.view.SensorObserver;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that monitor the data received from all the sensors. This can be used
 * for more purposes:
 * <ul>
 * <li>Saving to file</li>
 * <li>Displaying into SensorObserver</li>
 * <li>Training a NoiseReduction Algorithm</li>
 * </ul>
 * <br/><br/>
 * Noise Reduction algorithms can be used with the setFilter() method.
 *
 * @author Chirila Alexandru
 */
public class SensorCapture {

    private SensorManager sm;
    private LocationManager lm;
    private Activity ct;
    private Collecter coll; // collector used to save to file
    private List<Sensor> sensorList; // list of available sensors
    private SensorObserver so; // sensor observer used to monitor
    private NoiseReduction nr; // filter pass
    private SensorEventListener sl; //main sensor event listener
    private final SensorEventListener slTrain = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (nr != null) {
                nr.addEvent(event);
            }
            if (so != null) {
                so.notify(event);
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private final SensorEventListener slMonitor = new SensorEventListener() {
        public void onSensorChanged(SensorEvent se) {
            if (nr != null) {
                if (!nr.filter(se)) {
                    return;
                }
            }
            if (so != null) {
                so.notify(se);
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private final SensorEventListener slToFile = new SensorEventListener() {
        public void onSensorChanged(SensorEvent se) {
            float[] v = se.values;
            try {
                if (nr != null) {
                    if (!nr.filter(se)) {
                        return;
                    }
                }
                if (so != null) {
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
    private final LocationListener llToFile = new LocationListener() {
        public void onLocationChanged(Location loc) {
            try {
                coll.addGpsData(loc.getLatitude(), loc.getLongitude(),
                        loc.getAltitude(), loc.getBearing(),
                        loc.getAccuracy(), loc.getTime());
            } catch (NullPointerException ex) {
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };

    /**
     * Creates a new Sensor Capture from the given Activity.
     *
     * @param ct
     */
    public SensorCapture(Activity ct) {
        this.nr = null;
        this.so = null;
        this.coll = null;
        this.ct = ct;
        sm = (SensorManager) ct.getSystemService(Context.SENSOR_SERVICE);
        sensorList = sm.getSensorList(Sensor.TYPE_ALL);
        lm = (LocationManager) ct.getSystemService(Context.LOCATION_SERVICE);
    }

    /**
     * Creates a new Sensor Capture from the given Activity. And attaches an
     * observer to display sensor changes.
     *
     * @param ct
     * @param so an SensorObserver, if null it's ignored
     */
    public SensorCapture(Activity ct, SensorObserver so) {
        this(ct);
        this.so = so;
    }

    /**
     * Sets the filter pass for this object. Will be applied to all monitoring
     * session, except the training one.
     * @param nr 
     */
    public void setFilter(NoiseReduction nr) {
        this.nr = nr;
    }

    /**
     * Can be use to provide a custom made SensorEventListener for all Sensors.
     * @param sl 
     */
    public void setEventListener(SensorEventListener sl) {
        this.sl = sl;
    }
    
    
    /**
     * Starts capturing with the current set SensorEventListener. 
     * Before calling this method, you should call setEventListner
     * @param rate 
     */
    public void startCaptureAll(int rate) {
        for (Sensor sensor : sensorList) {
            sm.registerListener(sl, sensor, rate);
        }
    }

    /**
     * Starts monitoring and notifying the observer for any sensor changes
     * @param rate 
     */
    public void startMonitor(int rate) {
        sl = slMonitor;
        startCaptureAll(rate);
    }

    /**
     * Start training a NoiseReduction algorithm.
     * @param noiseReduction - the algorithm
     * @param time - the duration of the training
     * @param callBack - to be ran on the activity thread after the operation was completed
     * @param rate - the rate of sensor data collecting 
     */
    public void startTrain(NoiseReduction noiseReduction, final long time, final Runnable callBack, int rate) {
        this.nr = noiseReduction;
        sl = slTrain;
        startCaptureAll(rate);
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(time);
                    stopCapture();
                    Thread.sleep(100);
                    nr.calculateNoise();
                    ct.runOnUiThread(callBack);
                } catch (InterruptedException ex) {
                    Logger.getLogger(SensorCapture.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    /**
     * Starts capturing all data to file (including GPS)
     * If GPS is not present, it will try to use a NETWORK_PROVIDER or a 
     * PASSIVE_PROVIDER.
     * @param file - the file
     * @param action - the current action
     * @param gps - true if gps is present
     * @param rate 
     */
    public void startCaptureAllToFile(File file, Label action, boolean gps, int rate) {
        coll = new Collecter(file, action);
        coll.startCollecting();
        sl = slToFile;
        startCaptureAll(rate);
        if (gps == true && lm.getProvider(LocationManager.GPS_PROVIDER) != null) {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, llToFile);
        } else if (lm.getProvider(LocationManager.NETWORK_PROVIDER) != null) {
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, llToFile);
        } else {
            lm.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, llToFile);
        }
    }

    /**
     * Stops the current capture
     */
    public void stopCapture() {
        for (Sensor sensor : sensorList) {
            sm.unregisterListener(sl, sensor);
        }
        lm.removeUpdates(llToFile);
        if (coll != null) {
            coll.stopCollecting();
        }
    }

    /**
     * Gets all available sensor from the given context
     * @param ct
     * @return 
     */
    public static List<Sensor> getSensorList(Context ct) {
        return ((SensorManager) ct.getSystemService(Context.SENSOR_SERVICE))
                .getSensorList(Sensor.TYPE_ALL);
    }
}
