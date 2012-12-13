package hymas.m1.view;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Chirila Alexandru
 */
public class SensorObserver {
    private Map<Integer, TextView> map = new HashMap<Integer, TextView>();
    public SensorObserver(){
        
    }
    public void add(Sensor s, TextView t){        
        map.put(s.getType(), t);
    }
    public void notify(SensorEvent se){
        String s = se.sensor.getName() + ": ";
        for (float f : se.values) {
            s += f + " ";
        }        
        map.get(se.sensor.getType()).setText(s);
    }
    
    public static SensorObserver createObserver(Context ct, List<Sensor> sensorList, LinearLayout layout) {
        SensorObserver so = new SensorObserver();
        for (Sensor sensor : sensorList) {
            TextView tw = new TextView(ct);
            layout.addView(tw);
            so.add(sensor, tw);
        }
        return so;
    }
    
}
