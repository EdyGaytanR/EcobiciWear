package gaytan.eduardo.wearmap;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.os.Bundle;
import android.support.wear.widget.SwipeDismissFrameLayout;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.Toast;

import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdate;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MapsActivity extends WearableActivity implements GoogleMap.OnInfoWindowClickListener,OnMapReadyCallback {

    /**
     * Map is initialized when it's fully loaded and ready to be used.
     *
     * @see #onMapReady(com.google.android.gms.maps.GoogleMap)
     */
    private GoogleMap mMap;

    private String TAG = MapsActivity.class.getSimpleName();
    private String ClientId = "767_5vgyvbs0z04c8sko44w4wg8044gg88swc8c4wkwcgowkgogogk";
    private String ClientSecret = "24o30414hzq8g0sgsswsc0gkcg448ss88o0o004oskw84oooso";

    ArrayList<HashMap<String, String>> statusList;
    ArrayList<HashMap<String, String>> locationList;

    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        statusList = new ArrayList<>();
        locationList = new ArrayList<>();
        new GetBikes().execute();

        // Enables always on.
        setAmbientEnabled();

        setContentView(R.layout.activity_maps);

        final SwipeDismissFrameLayout swipeDismissRootFrameLayout =
                (SwipeDismissFrameLayout) findViewById(R.id.swipe_dismiss_root_container);
        final FrameLayout mapFrameLayout = (FrameLayout) findViewById(R.id.map_container);

        // Enables the Swipe-To-Dismiss Gesture via the root layout (SwipeDismissFrameLayout).
        // Swipe-To-Dismiss is a standard pattern in Wear for closing an app and needs to be
        // manually enabled for any Google Maps Activity. For more information, review our docs:
        // https://developer.android.com/training/wearables/ui/exit.html
        swipeDismissRootFrameLayout.addCallback(new SwipeDismissFrameLayout.Callback() {
            @Override
            public void onDismissed(SwipeDismissFrameLayout layout) {
                // Hides view before exit to avoid stutter.
                layout.setVisibility(View.GONE);
                finish();
            }
        });

        // Adjusts margins to account for the system window insets when they become available.
        swipeDismissRootFrameLayout.setOnApplyWindowInsetsListener(
                new View.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsets onApplyWindowInsets(View view, WindowInsets insets) {
                        insets = swipeDismissRootFrameLayout.onApplyWindowInsets(insets);

                        FrameLayout.LayoutParams params =
                                (FrameLayout.LayoutParams) mapFrameLayout.getLayoutParams();

                        // Sets Wearable insets to FrameLayout container holding map as margins
                        params.setMargins(
                                insets.getSystemWindowInsetLeft(),
                                insets.getSystemWindowInsetTop(),
                                insets.getSystemWindowInsetRight(),
                                insets.getSystemWindowInsetBottom());
                        mapFrameLayout.setLayoutParams(params);

                        return insets;
                    }
                });

        // Obtain the MapFragment and set the async listener to be notified when the map is ready.
        MapFragment mapFragment =
                (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    private class GetBikes extends AsyncTask<Void, Void, Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MapsActivity.this,"Cargando Estaciones",Toast.LENGTH_LONG).show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            String url_token = "https://pubsbapi.smartbike.com/oauth/v2/token?client_id="+ClientId+ "&client_secret="+ClientSecret+"&grant_type=client_credentials";
            String JSonToken = sh.makeServiceCall(url_token);

            Log.e(TAG, "Response from url: " + JSonToken);
            if (JSonToken != null) {
                try {
                    JSONObject OJToken = new JSONObject(JSonToken);
                    String eco_token = OJToken.getString("access_token");

                    String url_list_stations_status = "https://pubsbapi.smartbike.com/api/v1/stations/status.json?access_token="+eco_token;
                    String JSonList = sh.makeServiceCall(url_list_stations_status);

                    String url_list_stations_location = "https://pubsbapi.smartbike.com/api/v1/stations.json?access_token="+eco_token;
                    String JSonList2 = sh.makeServiceCall(url_list_stations_location);


                    JSONObject list_stations_status = new JSONObject(JSonList);
                    JSONArray stations = list_stations_status.getJSONArray("stationsStatus");

                    JSONObject list_stations_location = new JSONObject(JSonList2);
                    JSONArray locationJS = list_stations_location.getJSONArray("stations");

                    for (int i = 0; i < stations.length(); i++) {
                        JSONObject c = stations.getJSONObject(i);
                        String id_statu = c.getString("id");
                        String status = c.getString("status");

                        // Phone node is JSON Object
                        JSONObject availability = c.getJSONObject("availability");
                        String bikes = availability.getString("bikes");
                        String slots = availability.getString("slots");


                        JSONObject l = locationJS.getJSONObject(i);
                        String id_loc = l.getString("id");
                        String name = l.getString("name");

                        // Phone node is JSON Object
                        JSONObject location = l.getJSONObject("location");
                        String lat = location.getString("lat");
                        String lon = location.getString("lon");

                        // tmp hash map for single contact
                        HashMap<String, String> contact = new HashMap<>();

                        // tmp hash map for single contact
                        HashMap<String, String> loc = new HashMap<>();

                        // adding each child node to HashMap key => value
                        contact.put("id_stat", id_statu);
                        contact.put("name", name);
                        contact.put("status", status);
                        contact.put("bikes", bikes);
                        contact.put("slots", slots);

                        // adding each child node to HashMap key => value
                        loc.put("id_loc", id_loc);
                        loc.put("lat", lat);
                        loc.put("lon", lon);

                        // adding contact to contact list
                        statusList.add(contact);
                        locationList.add(loc);
                    }


                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                }

            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            int size = locationList.size();
            for(int i = 0; i < size; i++){
                Log.e(TAG, "ID: 2");
                String station_name = statusList.get(i).get("name");
                String map_loc_id = locationList.get(i).get("id_loc");
                Log.e(TAG, "ID: 2");
                int map_loc_int = Integer.parseInt(map_loc_id);
                map_loc_int = map_loc_int - 1;
                /*Log.e(TAG, "ID: "+locationList.get(i).get("id_loc"));
                Log.e(TAG, "Lat: "+locationList.get(i).get("lat"));
                Log.e(TAG, "Lon: "+locationList.get(i).get("lon"));
                Log.e(TAG, "Bicis: "+statusList.get(map_loc_int).get("bikes"));
                Log.e(TAG, "Slots: "+statusList.get(map_loc_int).get("slots"));*/
                String new_lat = locationList.get(i).get("lat");
                float new_lat_int = Float.parseFloat(new_lat);
                String new_lon = locationList.get(i).get("lon");
                float new_lon_int = Float.parseFloat(new_lon);

                String new_bikes = statusList.get(map_loc_int).get("bikes");
                int new_bikes_int = Integer.parseInt(new_bikes);
                String new_slots = statusList.get(map_loc_int).get("slots");
                int new_slots_int = Integer.parseInt(new_slots);

                if(new_bikes_int >= 6 ){
                    LatLng marker_pos = new LatLng(new_lat_int, new_lon_int);
                    mMap.addMarker(new MarkerOptions().position(marker_pos).title(station_name).snippet("Bicis: "+new_bikes_int+" Slots: "+new_slots_int).icon(BitmapDescriptorFactory.fromResource(R.drawable.full)));
                }
                if(new_bikes_int <= 5 ){
                    LatLng marker_pos = new LatLng(new_lat_int, new_lon_int);
                    mMap.addMarker(new MarkerOptions().position(marker_pos).title(station_name).snippet("Bicis: "+new_bikes_int+" Slots: "+new_slots_int).icon(BitmapDescriptorFactory.fromResource(R.drawable.half)));
                }
                if(new_bikes_int == 0 ){
                    LatLng marker_pos = new LatLng(new_lat_int, new_lon_int);
                    mMap.addMarker(new MarkerOptions().position(marker_pos).title(station_name).snippet("Bicis: "+new_bikes_int+" Slots: "+new_slots_int).icon(BitmapDescriptorFactory.fromResource(R.drawable.none)));
                }
            }

        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Map is ready to be used.
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(19.411556, -99.17406);
        CameraUpdate zoom=CameraUpdateFactory.zoomTo(15);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.animateCamera(zoom);

        mMap.setOnInfoWindowClickListener((GoogleMap.OnInfoWindowClickListener) this);

    }

    @Override
    public void onInfoWindowClick(Marker marker) {

            marker.hideInfoWindow();

    }
}
