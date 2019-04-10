package tt.oo.mm;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;

public class LocationActivity extends Activity {
    MapView mMapView;
    AMap mAmap;
    int LocationRequestCode = 1000;
    int StorageRequestCode = 1001;
    Marker mMarker;
    double latitude, longitude;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_location);
        mMapView = findViewById(R.id.map);

        mMapView.onCreate(savedInstanceState);
        if (mMapView != null) {
            mAmap = mMapView.getMap();
        }

        if (checkCallingPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && checkCallingPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            initAMap();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, LocationRequestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @androidx.annotation.NonNull String[] permissions, @androidx.annotation.NonNull int[] grantResults) {
        if (requestCode == this.LocationRequestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                initAMap();
            } else {
                Toast.makeText(this, "请打开定位权限 和 写文件权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initAMap() {
        if (mAmap != null) {
            MyLocationStyle myLocationStyle = new MyLocationStyle();
            myLocationStyle.interval(2000);
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);
            myLocationStyle.showMyLocation(true);

            mAmap.setMyLocationStyle(myLocationStyle);
            mAmap.setMyLocationEnabled(true);

            mAmap.setOnMapClickListener(new AMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    if (mMarker == null) {
                        mMarker = mAmap.addMarker(new MarkerOptions().position(latLng));
                    } else {
                        mMarker.setMarkerOptions(new MarkerOptions().position(latLng));
                    }

                    setCurrentPosition(latLng.latitude, latLng.longitude);
                }
            });
        }
    }

    /**
     * 需要被hook的方法，获取经纬度
     * @param latitude
     * @param longitude
     */
    public void setCurrentPosition(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }
}
