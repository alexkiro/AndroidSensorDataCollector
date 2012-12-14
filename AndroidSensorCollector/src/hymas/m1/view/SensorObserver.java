package hymas.m1.view;

import android.hardware.SensorEvent;

/**
 * Interface use to notify a GUI about Sensor Events in a SensorCapture object
 * @author Chirila Alexandru
 */
public interface SensorObserver {

    void notify(SensorEvent se);
}
