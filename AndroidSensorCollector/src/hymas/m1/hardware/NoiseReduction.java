/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hymas.m1.hardware;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Chirila Alexandru
 */
public class NoiseReduction {

    private Map<Integer, SensorNoiseReductor> map;

    public NoiseReduction(List<Sensor> sensors) {
        this.map = new HashMap<Integer, SensorNoiseReductor>();
        for (Sensor sensor : sensors) {
            map.put(sensor.getType(), new SensorNoiseReductor());
        }
    }

    public void addEvent(SensorEvent se) {
        map.get(se.sensor.getType()).add(se);
    }
    
    public void calculateNoise(){
        for (Map.Entry<Integer, SensorNoiseReductor> entry : map.entrySet()) {
            entry.getValue().computeMean();
            entry.getValue().computeMaximumDeviation();
        }
    }
    
    public boolean filter(SensorEvent se){
        return map.get(se.sensor.getType()).check(se);
    }

    private class SensorNoiseReductor {

        private List<SensorEvent> data = new LinkedList<SensorEvent>();
        private int n;
        private double[] mean;
        private double[] deviation;
        private SensorEvent last = null;
        
        public boolean check(SensorEvent se){
            if (last == null){
                last = se;
                return true;
            } else {
                for (int i = 0; i < n; i++) {
                    if (Math.abs(last.values[i] - se.values[i]) > deviation[i]){
                        last = se;
                        return true;
                    }
                }
                //last = se; //not sure 
                return false;
            }
        }
        
        public void add(SensorEvent se) {
            if (data.isEmpty()) {
                n = se.values.length;
                initialize();
            }
            data.add(se);
        }
        
        public void initialize(){
            mean = new double[n];
            deviation = new double[n];
        }

        public void computeMean() {
            for (SensorEvent sensorEvent : data) {
                for (int i = 0; i < n; i++) {
                    mean[i] += sensorEvent.values[i];
                }
            }
            for (int i = 0; i < n; i++) {
                mean[i] = mean[i] / data.size();
            }
        }

        public void computeAbsoluteAverageDeviation() {
            for (SensorEvent sensorEvent : data) {
                for (int i = 0; i < n; i++) {
                    deviation[i] += Math.abs(mean[i] - sensorEvent.values[i]);
                }
            }
            System.err.println(data.get(0).sensor.getName());
            for (int i = 0; i < n; i++) {
                deviation[i] = mean[i] / data.size();
                System.err.println(deviation[i]);
            }
        }
        
        public void computeMaximumDeviation(){
            if (data.isEmpty()){
                n=3;
                initialize();
                return;
            }
            for (SensorEvent sensorEvent : data) {
                for (int i = 0; i < n; i++) {
                    double dev = Math.abs(mean[i] - sensorEvent.values[i]);
                    if (dev > deviation[i]){
                        deviation[i] = dev;
                    }
                }
            }
        }
        
        
    }
}
