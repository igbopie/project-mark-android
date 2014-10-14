package as.mark.android;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.LevelListDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import as.mark.android.api.Api;
import as.mark.android.model.Mark;

public class MainMapFragment extends Fragment implements LocationListener, GoogleMap.OnCameraChangeListener{

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private MapFragment mapFragment;
    private LocationSubscriptionManager locationSubscriptionManager;
    private SendQuery sendQuery;
    private Location currentUserLocation;
    private Map<String,Marker> renderedMarks = new HashMap<String, Marker>();
    private Map<Marker,Mark> renderedMarkerMarks = new HashMap<Marker, Mark>();
    private Marker myPositionMarker;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main_maps, container, false);
        if(mapFragment == null) {
            mapFragment = MapFragment.newInstance();

        }
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.map, mapFragment);
        fragmentTransaction.commit();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof LocationSubscriptionManager){
            locationSubscriptionManager = (LocationSubscriptionManager) activity;
            locationSubscriptionManager.subscribeLocationUpdates(this);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if(locationSubscriptionManager != null){
            locationSubscriptionManager.removeSubscriptionLocationUpdates(this);
            locationSubscriptionManager = null;
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        mMap = mapFragment.getMap();
        if(mMap!= null) {
            setUpMap();
            mMap.setOnCameraChangeListener(this);
        }
    }

    public static MainMapFragment newInstance() {
        MainMapFragment fragment = new MainMapFragment();
        Bundle args = new Bundle();
        //args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }
    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */


    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    @Override
    public void onLocationChanged(Location location) {
        currentUserLocation = location;
        LatLng mypos = new LatLng(location.getLatitude(), location.getLongitude());
        if(myPositionMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(mypos);
            markerOptions.title("Me");
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.headicon01s));

            myPositionMarker = mMap.addMarker(markerOptions);

        } else {
            myPositionMarker.setPosition(mypos);
        }

    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

        Log.i(getClass().getCanonicalName(),"onCameraChange");


        VisibleRegion vr = mMap.getProjection().getVisibleRegion();
        double left = vr.latLngBounds.southwest.longitude;
        double top = vr.latLngBounds.northeast.latitude;
        double right = vr.latLngBounds.northeast.longitude;
        double bottom = vr.latLngBounds.southwest.latitude;


        Location cornerLocation = new Location("corner");//(center's latitude,vr.latLngBounds.southwest.longitude)
        cornerLocation.setLatitude(bottom);
        cornerLocation.setLongitude(right);


        Location center = new Location("center");
        center.setLatitude( vr.latLngBounds.getCenter().latitude);
        center.setLongitude( vr.latLngBounds.getCenter().longitude);
        float radius = center.distanceTo(cornerLocation);//calculate distane between middleLeftcorner and center

        if(sendQuery != null){
            sendQuery.cancel(true);
        }

        sendQuery = new SendQuery(
                center.getLongitude(),
                center.getLatitude(),
                currentUserLocation.getLongitude(),
                currentUserLocation.getLatitude(),
                radius);
        sendQuery.execute();

    }

    public class SendQuery extends AsyncTask<Void,Void,Void>{

        double longitude;
        double latitude;
        double userLongitude;
        double userLatitude;
        double radius;

        List<Mark> results;

        public SendQuery(double longitude, double latitude, double userLongitude, double userLatitude, float radius) {
            this.longitude = longitude;
            this.latitude = latitude;
            this.userLongitude = userLongitude;
            this.userLatitude = userLatitude;
            this.radius = radius;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                results = Api.searchMarks(longitude,latitude,userLongitude,userLatitude, radius);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if(results != null){
                for(Mark mark:results){
                    if(!renderedMarks.containsKey(mark.getId())) {
                        new AddMarker(mark).execute();
                        Log.i("MapFragment","Added mark "+ mark.getId());

                    }else{

                        Log.i("MapFragment","Already Added mark "+ mark.getId());
                    }
                }
            }


        }
    }

    class AddMarker extends AsyncTask<Void,Void,Void>{
        private MarkerOptions markerOptions;
        private Mark mark;

        AddMarker(Mark mark) {
            this.mark = mark;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            markerOptions = new MarkerOptions();
            markerOptions.position( new LatLng(
                    mark.getLatitude(),
                    mark.getLongitude()
            ));


            markerOptions.title(mark.getName());

            try {
                Bitmap bi = Picasso.with(getActivity())
                        .load("http://mark-test-2.herokuapp.com/api/media/get/large/" + mark.getMapIconMediaId())
                        .get();
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bi));
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("MapFragment",e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Marker marker = mMap.addMarker(markerOptions);
            renderedMarks.put(mark.getId(),marker);
            renderedMarkerMarks.put(marker,mark);
        }
    }
}
