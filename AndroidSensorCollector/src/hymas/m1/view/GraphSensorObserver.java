/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hymas.m1.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author kiro
 */
public class GraphSensorObserver {

    private Map<Integer, GraphViewSeries> map = new HashMap<Integer, GraphViewSeries>();
    private Map<Integer, Counter> map2 = new HashMap<Integer, Counter>();
    private Activity activity;
    private long time;

    private class Counter {

        public int count = 0;

        public void inc() {
            count++;
        }
    }

    public int random() {
        return Color.rgb(new Random().nextInt(256),
                new Random().nextInt(256),
                new Random().nextInt(256));
    }

    public GraphSensorObserver(Activity activity) {
        this.activity = activity;
        time = System.currentTimeMillis();
        List<Sensor> sensorList = ((SensorManager) activity.getSystemService(Context.SENSOR_SERVICE)).getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : sensorList) {
            map.put(sensor.getType(),
                    new GraphViewSeries(sensor.getName(), 
                    new GraphViewSeries.GraphViewStyle(random(), 1),
                    new GraphView.GraphViewData[]{new GraphView.GraphViewData(0, 0)}));
            map2.put(sensor.getType(), new Counter());
        }
    }

    public GraphView createGraphView() {
        GraphView g = new LineGraphView(activity, "");
        for (Map.Entry<Integer, GraphViewSeries> entry : map.entrySet()) {
            Integer integer = entry.getKey();
            GraphViewSeries graphViewSeries = entry.getValue();
            g.addSeries(graphViewSeries);
        }
        g.setScrollable(true);
        g.setScalable(true);
        g.setViewPort(0, 100);
        return g;
    }

    public void notify(SensorEvent se) {
        double d = 0;
        for (float f : se.values) {
            d += f;
        }

        map.get(se.sensor.getType()).appendData(
                new GraphView.GraphViewData(map2.get(se.sensor.getType()).count, 1), true);

        map2.get(se.sensor.getType()).inc();

    }
}
