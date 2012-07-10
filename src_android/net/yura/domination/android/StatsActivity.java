package net.yura.domination.android;

import java.util.List;
import net.yura.android.AndroidMeApp;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.core.Player;
import net.yura.domination.mobile.flashgui.DominationMain;
import net.yura.domination.mobile.flashgui.GameActivity;
import net.yura.mobile.util.Properties;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import android.app.Activity;
import android.os.Bundle;

public class StatsActivity extends Activity {

    Properties resb = GameActivity.resb;
    
    Risk getRisk() {
        DominationMain dmain = (DominationMain)AndroidMeApp.getMIDlet();
        return dmain.risk;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setTitle( resb.getString("swing.tab.statistics") );

        showGraph(1);
    }
    
    public void showGraph(int a) {
        GraphicalView gview = ChartFactory.getLineChartView(this, getDataset(a), getRenderer() );
        setContentView(gview);
    }
    
    private XYMultipleSeriesRenderer getRenderer() {
        
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        
        List<Player> players = getRisk().getGame().getPlayersStats();

        for (int c = 0; c < players.size(); c++) {
        
            Player p = players.get(c);
            
            SimpleSeriesRenderer r = new XYSeriesRenderer();
            r.setColor( p.getColor() );
            renderer.addSeriesRenderer(r);

        }
        return renderer;
    }

    public XYMultipleSeriesDataset getDataset(int a) {

        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        
        List<Player> players = getRisk().getGame().getPlayersStats();

        //draw each player graph.
        for (int c = 0; c < players.size(); c++) {
            
            Player p = players.get(c);
            
            CategorySeries series = new CategorySeries( p.getName() );
            
            int[] PointToDraw = p.getStatistics(a);

            int newPoint=0;
            
            series.add( newPoint ); // everything starts from 0
            
            for (int i = 0; i < PointToDraw.length; i++) {

                if ( a==0 || a==1 || a==2 || a==6 || a==7) {
                    newPoint = PointToDraw[i] ;
                }
                else {
                    newPoint += PointToDraw[i] ;
                }

                series.add( newPoint );

            }

            dataset.addSeries(series.toXYSeries());

        }
        
        return dataset;

    }

}
