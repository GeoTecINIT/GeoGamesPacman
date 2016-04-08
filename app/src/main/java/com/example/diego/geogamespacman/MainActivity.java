package com.example.diego.geogamespacman;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleMarkerSymbol;

import java.util.List;

public class MainActivity extends AppCompatActivity implements GameService.GameServiceListener {

    private MapView mMapView;
    private GraphicsLayer gLayer;
    private boolean mapInitialized;
    protected GameService gameService;
    protected boolean gameServiceBound;
    private int graphicIDpos;
    private List<Node> nodes;
    private int getGraphicIDs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Retrieve the map and initial extent from XML layout
        mMapView = (MapView) findViewById(R.id.map);
        // Enable map to wrap around date line.
        gLayer = new GraphicsLayer();
        mMapView.addLayer(gLayer);

        // Bind to service
        Intent intent = new Intent(this, GameService.class);
        bindService(intent, gameServiceCon, Context.BIND_AUTO_CREATE);

         mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {
             @Override
             public void onStatusChanged(Object source, STATUS status) {

                 if (OnStatusChangedListener.STATUS.INITIALIZED == status && source == mMapView && !mapInitialized) {
                     mapInitialized = true;

                     String query = "( node [\"tourism\"] (39.928694653732364,-0.10814666748046874,40.01591464708541,0.03673553466796875); way [\"tourism\"] (39.928694653732364,-0.10814666748046874,40.01591464708541,0.03673553466796875); relation [\"tourism\"] (39.928694653732364,-0.10814666748046874,40.01591464708541,0.03673553466796875); ); out meta; out meta qt; ";
                     nodes = OSMWrapperAPI.getNodes(OSMWrapperAPI.getNodesViaOverpass(query));
                     Log.d("List:",Integer.toString(nodes.size()));

                     for(int i = 0; i < nodes.size(); i++) {
                         Node node = nodes.get(i);
                         Point pointGeometry = new Point(node.getLon(), node.getLat());
                         pointGeometry = (Point) GeometryEngine.project(pointGeometry,
                                 SpatialReference.create(SpatialReference.WKID_WGS84),
                                 mMapView.getSpatialReference());
                         SimpleMarkerSymbol simpleMarker = new SimpleMarkerSymbol(Color.YELLOW, 8,
                                 SimpleMarkerSymbol.STYLE.CIRCLE);
                         Graphic pointGraphic = new Graphic(pointGeometry, simpleMarker);
                         nodes.get(i).setGraphicID(gLayer.addGraphic(pointGraphic));
                     }

                     Point pointGeometry = new Point(-0.073885, 39.993808);
                     pointGeometry = (Point) GeometryEngine.project(pointGeometry, SpatialReference.create(SpatialReference.WKID_WGS84), mMapView.getSpatialReference());
                     SimpleMarkerSymbol simpleMarker = new SimpleMarkerSymbol(Color.RED, 10, SimpleMarkerSymbol.STYLE.CIRCLE);
                     Graphic pointGraphic = new Graphic(pointGeometry, simpleMarker);
                     graphicIDpos = gLayer.addGraphic(pointGraphic);


                 }
             }
         });

    }

    private ServiceConnection gameServiceCon = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            GameService.LocalBinder binder = (GameService.LocalBinder) service;
            gameService = binder.getService();
            gameServiceBound = true;
            gameService.registerListener(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            gameServiceBound = false;
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.unpause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updatePlayerPosition(Location location) {
        Log.d("Updated player position", location.toString());

        gLayer.removeGraphic(graphicIDpos);
        Point pointGeometry = new Point(location.getLongitude(), location.getLatitude());
        pointGeometry = (Point) GeometryEngine.project(pointGeometry, SpatialReference.create(SpatialReference.WKID_WGS84), mMapView.getSpatialReference());
        SimpleMarkerSymbol simpleMarker = new SimpleMarkerSymbol(Color.BLUE, 10, SimpleMarkerSymbol.STYLE.DIAMOND);
        Graphic pointGraphic = new Graphic(pointGeometry, simpleMarker);
        graphicIDpos = gLayer.addGraphic(pointGraphic);

        // Eat a node
        if (nodes.size()>0) {  // ask if there is some node to eat
            int closest_id = 0;
            Double closest_distance = Math.sqrt(Math.pow(location.getLatitude() - nodes.get(0).getLat(), 2) + Math.pow(location.getLongitude() - nodes.get(0).getLon(), 2));
            if (nodes.size()>1) {  // ask if there is more than one node to see if one of them is closer
                for (int i = 1; i < nodes.size(); i++) {
                    Double distance = Math.sqrt(Math.pow(location.getLatitude() - nodes.get(i).getLat(), 2) + Math.pow(location.getLongitude() - nodes.get(i).getLon(), 2));
                    if (distance < closest_distance) {  // ask if this node is closest to the previous
                        closest_id = i;
                        closest_distance = distance;
                    }
                }
            }
            Log.d("Closest Distance", Double.toString(closest_distance));
            if (closest_distance < 0.00002 ){
                Node node = nodes.get(closest_id);
                Log.d("Tags to Delete", node.getTagsText());
                gLayer.removeGraphic(node.getGraphicID());
                nodes.remove(closest_id);
                Log.d("nodes Size", Integer.toString(nodes.size()));
            }
        }

    }
}
