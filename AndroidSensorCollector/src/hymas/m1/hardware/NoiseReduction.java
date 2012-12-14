package hymas.m1.hardware;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hold information about sensors and computes a filter to remove the noise and 
 * random fluctuation.
 * @author Chirila Alexandru
 */
public class NoiseReduction implements Serializable {

    private Map<Integer, SensorNoiseReductor> map;  //maps sensor to noise reduction information

    public NoiseReduction(List<Sensor> sensors) {
        this.map = new HashMap<Integer, SensorNoiseReductor>();
        for (Sensor sensor : sensors) {
            map.put(sensor.getType(), new SensorNoiseReductor());
        }
    }

    /**
     * Add an event to the training set
     * @param se 
     */
    public void addEvent(SensorEvent se) {
        map.get(se.sensor.getType()).add(se);
    }

    /**
     * Computes the filters for all the sensors
     */
    public void calculateNoise() {
        for (Map.Entry<Integer, SensorNoiseReductor> entry : map.entrySet()) {
            entry.getValue().computeMean();
            entry.getValue().computeMaximumDeviation();
        }
    }

    /**
     * Filters a SensorEvent by comparing it to the last SensorEvent inputed and 
     * applying a filter.
     * @param se 
     * @return False if the new event is consider to be noise, True otherwise
     */
    public boolean filter(SensorEvent se) {
        return map.get(se.sensor.getType()).check(se);
    }

    public static void serialize(File file, NoiseReduction obj) {
        try {
            FileOutputStream fileOut = new FileOutputStream(file);
            serialize(fileOut, obj);
            fileOut.close();
        } catch (IOException ex) {
            Logger.getLogger(NoiseReduction.class.getName()).log(Level.SEVERE, null, ex);        
        }
    }

    public static void serialize(FileOutputStream fileOut, NoiseReduction obj) {
        try {            
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(obj);
            out.close();
            fileOut.close();
        } catch (IOException ex) {
            Logger.getLogger(NoiseReduction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    public static NoiseReduction deserialize(File file) {
        FileInputStream fileIn = null;
        try {
            fileIn = new FileInputStream(file);
            return deserialize(fileIn);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(NoiseReduction.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fileIn.close();
            } catch (IOException ex) {
                Logger.getLogger(NoiseReduction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
    
    
    public static NoiseReduction deserialize(FileInputStream fileIn){
        try {
            NoiseReduction result;            
            ObjectInputStream in = new ObjectInputStream(fileIn);
            result = (NoiseReduction) in.readObject();
            in.close();
            fileIn.close();
            return result;
        } catch (OptionalDataException ex) {
            Logger.getLogger(NoiseReduction.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(NoiseReduction.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(NoiseReduction.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private class SensorNoiseReductor implements Serializable {

        private transient List<SensorEvent> data; //training data
        private int n;
        private double[] mean;  // the Arithmetic mean of the data
        private double[] deviation; // the filter
        private transient SensorEvent last; //the last inputed event

        private SensorNoiseReductor() {
            this.last = null;
            this.data = new LinkedList<SensorEvent>();
        }

        public boolean check(SensorEvent se) {
            if (last == null) {
                last = se;
                return true;
            } else {
                for (int i = 0; i < n; i++) {
                    if (Math.abs(last.values[i] - se.values[i]) > deviation[i]) {
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

        public void initialize() {
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

        public void computeMaximumDeviation() {
            if (data.isEmpty()) {
                n = 3;
                initialize();
                return;
            }
            for (SensorEvent sensorEvent : data) {
                for (int i = 0; i < n; i++) {
                    double dev = Math.abs(mean[i] - sensorEvent.values[i]);
                    if (dev > deviation[i]) {
                        deviation[i] = dev;
                    }
                }
            }
        }
    }
}
