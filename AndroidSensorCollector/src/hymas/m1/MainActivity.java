package hymas.m1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import com.jjoe64.graphview.GraphViewSeries;
import hymas.m1.hardware.SensorCapture;
import hymas.m1.view.SensorObserver;

public class MainActivity extends Activity {

    private SensorCapture sc;
    private SensorObserver obs;
    private GraphViewSeries series;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        LinearLayout layout = (LinearLayout) findViewById(R.id.displayLayout);
       // obs = createObserver(, layout);
        
    }
    
    public void onClickCalibrate(View v){
        Intent in = new Intent(this, CalibrateActivity.class);
        startActivity(in);        
    }
}
