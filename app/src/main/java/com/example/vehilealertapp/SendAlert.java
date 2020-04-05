package com.example.vehilealertapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;

public class SendAlert extends AppCompatActivity implements View.OnClickListener {
    Button location;
    TextView latitude;
    EditText name;
    EditText cntno;
    EditText reg_no;
    TextView longitude;
    TextView details;
    Button getimage;
    ImageView carimg;
    LocationRequest locationrequest;
    public static final int ACTION_CAMERA=3;
    public static final int REQUEST_APP_LOCATION=1;
    public static final int LOCATION_SERVICE=2;
    FusedLocationProviderClient providerClient;
    LocationCallback lc;
    private Uri imageuri;
    private Button send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_alert);
        wireUpListeners();
        if(savedInstanceState!=null) {
            latitude.setText(savedInstanceState.getString("latitude"));
            longitude.setText(savedInstanceState.getString("longitude"));
            if(savedInstanceState.getString("carimg")!=null) {
                imageuri=Uri.parse(savedInstanceState.getString("carimg"));
                Picasso.get().load(imageuri).fit().centerCrop().into(carimg);
            }
        }
    }

    private void wireUpListeners() {
        location=(Button)findViewById(R.id.loc);
        latitude=(TextView) findViewById(R.id.latitude);
        longitude=(TextView)findViewById(R.id.longitude);
        location.setOnClickListener(this);
        getLocationService();
        providerClient=LocationServices.getFusedLocationProviderClient(this);
        lc=new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

            }
        };
        getimage=(Button)findViewById(R.id.get_image);
        getimage.setOnClickListener(this);
        carimg=(ImageView)findViewById(R.id.img);
        send=(Button)findViewById(R.id.send);
        send.setOnClickListener(this);
        name=(EditText)findViewById(R.id.name);
        cntno=(EditText)findViewById(R.id.contact_no);
        reg_no=(EditText)findViewById(R.id.reg_no);
        details=(EditText)findViewById(R.id.car_details);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        providerClient.requestLocationUpdates(locationrequest,lc, Looper.getMainLooper());
    }

    @Override
    protected void onStop() {
        super.onStop();
        providerClient.removeLocationUpdates(lc);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.loc:getLocation();
                            break;
            case R.id.get_image:getImage();
                            break;
            case R.id.send:
                if(validateInput()) {
                    FragmentManager fm = getSupportFragmentManager();
                    Fragment fg = fm.findFragmentByTag("send_fragment");
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    if (fg != null) {
                        ft.remove(fg);
                    }
                    ft.addToBackStack(null);
                    send_fragment sf = new send_fragment();
                    sf.setVic(new Victim(reg_no.getText().toString(),name.getText().toString(),details.getText().toString(),cntno.getText().toString()),imageuri);
                    sf.setLocation(new location(Double.parseDouble(latitude.getText().toString()),Double.parseDouble(longitude.getText().toString())));
                    sf.show(ft, "send_fragment");
                }


        }
    }

    private boolean validateInput() {

        if(name.getText().toString().length()==0|| name.getText().toString().trim().length()==0) {
            name.setError("please enter a valid name");
            name.setFocusable(true);
            return false;
        }
        if(cntno.getText().toString().length()==0||cntno.getText().toString().trim().length()==0) {
            cntno.setError("please enter a valid contact no");
            name.setFocusable(true);
            return false;
        }
        if(reg_no.getText().toString().length()==0|| reg_no.getText().toString().trim().length()==0) {
            reg_no.setError("please enter a valid registration numeber");
            name.setFocusable(true);
            return false;
        }
        if(latitude.getText().toString().length()==0 && longitude.getText().toString().length()==0)
        {
            latitude.setError("please get current location");
            longitude.setError("please get current location");
            latitude.setFocusable(true);
            longitude.setFocusable(true);
            return false;
        }

        return true;
    }

    private void getLocation() {
        if(getRequiredPermissions() && checkConnectivity()) {
            getLocationUpdates();
        }

    }
    private boolean getRequiredPermissions() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_APP_LOCATION);
        return true;
    }
    private boolean checkConnectivity() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode)
        {
            case REQUEST_APP_LOCATION:if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                        getLocation();
                    else
                        showSnackBarForAppLocation();
                    break;
        }
    }

    private void showSnackBarForAppLocation() {
        View view=findViewById(R.id.layout);
        Snackbar.make(view,"you must give permission",Snackbar.LENGTH_INDEFINITE).setAction("permit", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intnt= new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri= Uri.fromParts("package",getPackageName(),null);
                intnt.setData(uri);
                startActivity(intnt);
            }
        }).setActionTextColor(Color.GREEN).show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(latitude!=null)
        outState.putString("latitude",latitude.getText().toString());
        if(longitude!=null)
        outState.putString("longitude",longitude.getText().toString());
        if(imageuri!=null)
            outState.putString("carimg",imageuri.toString());
    }

    private void getLocationService() {
        locationrequest= LocationRequest.create();
        locationrequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationrequest.setFastestInterval(10000);
        locationrequest.setInterval(50000);
        locationrequest.setNumUpdates(5);
        LocationSettingsRequest.Builder lb=new LocationSettingsRequest.Builder().addLocationRequest(locationrequest);
        SettingsClient client= LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task=client.checkLocationSettings(lb.build());
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(e instanceof ResolvableApiException)
                {
                    try {
                        ResolvableApiException resolve = (ResolvableApiException) e;
                        ((ResolvableApiException) e).startResolutionForResult(SendAlert.this, LOCATION_SERVICE);
                    }
                    catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }
    private void getLocationUpdates() {
        providerClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                DecimalFormat df=new DecimalFormat("00.0000000");
                latitude.setText(df.format(location.getLatitude())+"");
                longitude.setText(df.format(location.getLongitude())+"");
            }
        });
    }

    private void getImage() {
            Intent intnt=new Intent(Intent.ACTION_PICK);
            intnt.setType("image/*");
            intnt.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"image/jpeg","image/png"});
            startActivityForResult(intnt,ACTION_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK)
        {
            switch(requestCode)
            {
                case ACTION_CAMERA:
                                   if(data!=null) {
                                       imageuri = data.getData();
                                       Picasso.get().load(imageuri).fit().centerCrop().into(carimg);
                                   }
                                       break;

            }
        }
    }
}
