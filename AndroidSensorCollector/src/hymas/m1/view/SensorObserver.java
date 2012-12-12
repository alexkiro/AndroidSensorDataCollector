/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hymas.m1.view;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.widget.TextView;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author kiro
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
    
}
