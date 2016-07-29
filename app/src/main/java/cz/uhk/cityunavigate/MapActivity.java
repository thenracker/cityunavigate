package cz.uhk.cityunavigate;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import cz.uhk.cityunavigate.model.Category;
import cz.uhk.cityunavigate.model.Group;
import cz.uhk.cityunavigate.util.ObservableList;
import cz.uhk.cityunavigate.util.Promise;

public class MapActivity extends AppCompatActivity {

    private MapView mapView;

    private GoogleMap map;

    private HashMap<Marker, cz.uhk.cityunavigate.model.Marker> markerIds;

    private List<Marker> markers;

    private List<Circle> circles;

    private List<Polygon> polygons;

    private int mapStyle = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        markers = new ArrayList<>();
        circles = new ArrayList<>();
        polygons = new ArrayList<>();
        markerIds = new HashMap<>();

        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                map.getUiSettings().setMyLocationButtonEnabled(false);
                /*
                if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                                              int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                map.setMyLocationEnabled(true);
                */
                map.setBuildingsEnabled(true);
                map.getUiSettings().setAllGesturesEnabled(true);
                map.getUiSettings().setCompassEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
                map.getUiSettings().setMapToolbarEnabled(true);
                map.getUiSettings().setZoomControlsEnabled(true);

                //CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(22.336292, 114.173910), 10);
                //TODO - ADD CENTER TO MY POSITION
                centreMapToLatLng(new LatLng(22.336292, 114.173910));

                //MAP LISTENERS
                map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        marker.hideInfoWindow();
                        Intent detailIntent = new Intent(MapActivity.this,DetailActivity.class);
                        detailIntent.putExtra("id",markerIds.get(marker).getId());
                        detailIntent.putExtra("groupid",markerIds.get(marker).getIdGroup());
                        startActivity(detailIntent);
                    }
                });
                /*
                //TODO? případně by na dlouhé podržení mohla vyskočit addMarkerActivity se zvolenou polohou
                map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        markers.add(map.addMarker(new MarkerOptions().position(latLng).title("Si podržel").snippet("dobrý ne?")));
                    }
                });
                */
                map.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
                    @Override
                    public void onInfoWindowLongClick(Marker marker) {
                        marker.hideInfoWindow();
                        Intent detailIntent = new Intent(MapActivity.this ,DetailActivity.class);
                        detailIntent.putExtra("id",markerIds.get(marker).getId());
                        detailIntent.putExtra("groupid",markerIds.get(marker).getIdGroup());
                        startActivity(detailIntent);
                    }
                });

                //ADDING ALL COMPONENTS
                putAllMarkersOnMap();

            }
        });
    }
    //FUNCTIONS
    private void centreMapToLatLngSmooth(LatLng latLng) {
        if (map != null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(22.336292, 114.173910), 16);
            map.animateCamera(cameraUpdate);
        }
    }

    private void centreMapToLatLng(LatLng latLng) {
        if (map != null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(22.336292, 114.173910), 16);
            map.moveCamera(cameraUpdate);
        }
    }

    private void clearMap() {
        map.clear();
        markers.clear(); //FULL CLEAN
    }

    private void putAllMarkersOnMap(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null){
            ObservableList<Group> groupss = Database.getUserGroups(user);
            groupss.addItemAddListener(new ObservableList.ItemAddListener<Group>() {
                @Override
                public void onItemAdded(@NotNull ObservableList<Group> list, @NotNull Collection<Group> addedItems) {
                    for(Group grp : addedItems){
                        ObservableList<cz.uhk.cityunavigate.model.Marker> myMarkers = Database.getGroupMarkers(grp.getId());
                        myMarkers.addItemAddListener(new ObservableList.ItemAddListener<cz.uhk.cityunavigate.model.Marker>() {
                            @Override
                            public void onItemAdded(@NotNull ObservableList<cz.uhk.cityunavigate.model.Marker> list, @NotNull Collection<cz.uhk.cityunavigate.model.Marker> addedItems) {
                                for(cz.uhk.cityunavigate.model.Marker m : addedItems){
                                    putMarker(m);
                                }
                            }
                        });
                    }
                }
            });

        }
        else{
            Toast.makeText(this, "YOU'RE NOT LOGGED IN", Toast.LENGTH_SHORT).show();
        }
    }

    private void putMarker(final cz.uhk.cityunavigate.model.Marker marker) { //TODO STILL IN DEPLOY

        Promise<Category> pc = Database.getCategoryById(marker.getIdCategory());
        pc.success(new Promise.SuccessListener<Category, Object>() {
            @Override
            public Object onSuccess(Category result) {
                //IF SOMETHING HAPPENS, i = 0 SO THE COLOR OF THE MARKER WILL BE DEFAULT RED
                Marker m = map.addMarker(new MarkerOptions() //saving in List<Marker> to be able to clear only one from all possible markers
                        .position(marker.getLocation())
                        .title(marker.getTitle())
                        .icon(BitmapDescriptorFactory.defaultMarker(result.getHue()))
                        .snippet(marker.getText()));
                markerIds.put(m, marker);
                markers.add(m);
                return null;
            }
        });

    }

    private void removeMarker(Marker m) {
        markerIds.remove(m);
        m.remove();
        markers.remove(m);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {//noinspection SimplifiableIfStatement

        int id = item.getItemId();

        if (id == R.id.action_map_change) {
            if(mapStyle == 0){
                map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            }else if (mapStyle == 1){
                map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            }else if (mapStyle == 2){
                mapStyle = -1;
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
            mapStyle++;
        }

        if (id == R.id.action_marker_add){
            startActivity(new Intent(this, AddMarkerActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    //FOLLOWING METHODS ARE FOR MAPVIEW CONTROLLING (map fragment must have)
    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }



}