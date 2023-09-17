package com.gbsoft.ellosseum;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.gbsoft.ellosseum.databinding.ActivityMapBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private final String TAG = "MapActivityLog";

    private ActivityMapBinding mBinding;

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    private int mGetUserGps = -1;
    private LatLng mLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        mBinding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        this.initialSet();
    }

    private void initialSet() {
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
    }


    @Override
    protected void onPause() {
        super.onPause();
        Common.stopLocationService(MapActivity.this, Common.isLocationServiceRunning(MapActivity.this));
        finish();
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        Log.v(TAG, "onMapReady");
//        if (Common.sAuthority == Common.EMP) {  // 근로자
//            Intent intent = getIntent();
//            double lat = Double.parseDouble(intent.getStringExtra("lat"));
//            double lon = Double.parseDouble(intent.getStringExtra("lon"));
//            mLatLng = new LatLng(lat, lon);
//        } else {    // 관리자
//            SharedPreferences sharedPreferences = getSharedPreferences("Info", MODE_PRIVATE);
//            double lat = Double.parseDouble(sharedPreferences.getString("lat", ""));
//            double lon = Double.parseDouble(sharedPreferences.getString("lon", ""));
//            mLatLng = new LatLng(lat, lon);
//        }

        Intent intent = getIntent();
        mGetUserGps = intent.getIntExtra("getUserGps", -1);
        if (mGetUserGps == 1) { // sos 알람으로 map 들어온 경우 (관리자)
            double lat = Double.parseDouble(intent.getStringExtra("lat"));
            double lon = Double.parseDouble(intent.getStringExtra("lon"));
            mLatLng = new LatLng(lat, lon);
        } else {
            // 현장지도 클릭하고 들어온 경우 (관리자)
            SharedPreferences sharedPreferences = getSharedPreferences("Info", MODE_PRIVATE);
            try {
                double lat = Double.parseDouble(sharedPreferences.getString("lat", ""));
                double lon = Double.parseDouble(sharedPreferences.getString("lon", ""));
                mLatLng = new LatLng(lat, lon);
            } catch (Exception e) {
                mLatLng = new LatLng(0, 0);
            }
        }

        mMap = googleMap;

        // 마커 표시
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(mLatLng);
        markerOptions.title("내 위치");
        markerOptions.snippet(mLatLng.latitude + "/" + mLatLng.longitude);
        mMap.addMarker(markerOptions);

        // 영역 다각형 표시 (범어역 주변 영역)
        PolygonOptions polygonOptions = new PolygonOptions().add(
                new LatLng(35.859071, 128.624046),
                new LatLng(35.858592, 128.624040),
                new LatLng(35.858491, 128.623928),
                new LatLng(35.857266, 128.624018),
                new LatLng(35.857395, 128.625611),
                new LatLng(35.857773, 128.625668),
                new LatLng(35.858591, 128.625251),
                new LatLng(35.857773, 128.625668),
                new LatLng(35.858662, 128.625197),
                new LatLng(35.859071, 128.624488)
        );

        Polygon polygon = mMap.addPolygon(polygonOptions);
        polygon.setFillColor(0x55ff0000);
        polygon.setStrokeWidth(1);
        polygon.setStrokeColor(0xffff0000);

        // 기존에 사용하던 다음 2줄은 문제가 있습니다.

        // CameraUpdateFactory.zoomTo가 오동작하네요.
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(SEOUL));
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 17));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // TODO : 현장코드 - 구역코드 입력 후 해당하는 이슈 사진 데이터 가져오기
        mMapFragment.getMapAsync(this);
//        mBinding.btnMove.setOnClickListener(moveClick);
    }


    View.OnClickListener moveClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.v(TAG, "moveClick");
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.855750, 128.6235187), 15));
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }
}
