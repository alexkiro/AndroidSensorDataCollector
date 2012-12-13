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
 *
 * @author Chirila Alexandru
 */
public class SensorCapture {

    private SensorManager sm;
    private LocationManager lm;
    private Activity ct;
    private Collecter coll = null;
    private List<Sensor> sensorList;
    private SensorObserver so = null;
    private NoiseReduction nr = null;
    private SensorEventListener sl;
    private SensorEventListener slTrain = new SensorEventListener() {
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
    private SensorEventListener slMonitor = new SensorEventListener() {
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
    private SensorEventListener slToFile = new SensorEventListener() {
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
    private LocationListener llToFile = new LocationListener() {
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

    public SensorCapture(Activity ct) {
        this.ct = ct;
        sm = (SensorManager) ct.getSystemService(Context.SENSOR_SERVICE);
        sensorList = sm.getSensorList(Sensor.TYPE_ALL);
        lm = (LocationManager) ct.getSystemService(Context.LOCATION_SERVICE);
    }

    public SensorCapture(Activity ct, SensorObserver so) {
        this(ct);
        this.so = so;
    }

    public void setFilter(NoiseReduction nr) {
        this.nr = nr;
    }

    public void setEventListener(SensorEventListener sl) {
        this.sl = sl;
    }

    private void startCaptureAll(int rate) {
        for (Sensor sensor : sensorList) {
            sm.registerListener(sl, sensor, rate);
        }
    }

    public void startMonitor(int rate) {
        sl = slMonitor;
        startCaptureAll(rate);
    }

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

    public void startCaptureAllToFile(File file, Label action, boolean gps, int rate) {
        coll = new Collecter(file, action);
        coll.startCollecting();
        sl = slToFile;
        startCaptureAll(rate);        
        if (gps == true && lm.getProvider(LocationManager.GPS_PROVIDER) != null){
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, llToFile);
        } else if (lm.getProvider(LocationManager.NETWORK_PROVIDER) != null){
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, llToFile);
        } else {
            lm.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, llToFile);
        }
    }

    public void stopCapture() {
        for (Sensor sensor : sensorList) {
            sm.unregisterListener(sl, sensor);
        }
        lm.removeUpdates(llToFile);
        if (coll != null) {
            coll.stopCollecting();
        }
    }

    public static List<Sensor> getSensorList(Context ct) {
        return ((SensorManager) ct.getSystemService(Context.SENSOR_SERVICE))
                .getSensorList(Sensor.TYPE_ALL);
    }
}
