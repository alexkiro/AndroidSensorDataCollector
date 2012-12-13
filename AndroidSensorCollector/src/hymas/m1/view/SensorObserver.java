package hymas.m1.view;

import android.hardware.SensorEvent;

/**
 *
 * @author kiro
 */
public interface SensorObserver {

    void notify(SensorEvent se);
}
