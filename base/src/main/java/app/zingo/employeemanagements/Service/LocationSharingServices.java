package app.zingo.employeemanagements.Service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

import app.zingo.employeemanagements.Model.LiveTracking;
import app.zingo.employeemanagements.Utils.PreferenceHandler;
import app.zingo.employeemanagements.Utils.TrackGPS;
import app.zingo.employeemanagements.Utils.Util;
import app.zingo.employeemanagements.WebApi.LiveTrackingAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by ZingoHotels Tech on 02-01-2019.
 */

public class LocationSharingServices extends Service {

    TrackGPS gps;
    LiveTracking liveTracking;
    int SECONDS = 60; // The delay in minutes

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        gps = new TrackGPS(LocationSharingServices.this);


        try {

            if(locationCheck()&& PreferenceHandler.getInstance(getApplicationContext()).getUserId()!=0&& PreferenceHandler.getInstance(getApplicationContext()).getUserRoleUniqueID()!=2&& PreferenceHandler.getInstance(getApplicationContext()).getLoginStatus().equalsIgnoreCase("Login")){

                if(gps.canGetLocation())
                {
                    System.out.println("Long and lat Rev "+gps.getLatitude()+" = "+gps.getLongitude());
                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();

                    if(latitude!=0&&longitude!=0){
                        LiveTracking liveTracking = new LiveTracking();
                        liveTracking.setEmployeeId(PreferenceHandler.getInstance(LocationSharingServices.this).getUserId());
                        liveTracking.setLatitude(""+latitude);
                        liveTracking.setLongitude(""+longitude);
                        liveTracking.setTrackingDate(new SimpleDateFormat("MM/dd/yyyy").format(new Date()));
                        liveTracking.setTrackingTime(new SimpleDateFormat("HH:mm:ss").format(new Date()));
                        //liveTracking.setTrackingDate("01/02/2019");
                        addLiveTracking(liveTracking);
                    }



                }
                else
                {

                }
            }


        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        Intent restartService = new Intent(getApplicationContext(),
                this.getClass());
        restartService.setPackage(getPackageName());
        PendingIntent restartServicePI = PendingIntent.getService(
                getApplicationContext(), 1, restartService,
                PendingIntent.FLAG_ONE_SHOT);

        //Restart the service once it has been killed android


        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 30000, restartServicePI);

        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // TODO Auto-generated method stub
        Intent restartService = new Intent(getApplicationContext(),
                this.getClass());
        restartService.setPackage(getPackageName());
        PendingIntent restartServicePI = PendingIntent.getService(
                getApplicationContext(), 1, restartService,
                PendingIntent.FLAG_ONE_SHOT);

        //Restart the service once it has been killed android


        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 30000, restartServicePI);

    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        System.out.println("Location 1");

        //start a separate thread and start listening to your network object
    }




    public boolean locationCheck(){

        final boolean status = false;
        LocationManager lm = (LocationManager) LocationSharingServices.this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        return gps_enabled || network_enabled;
    }

    public void addLiveTracking(final LiveTracking liveTracking) {





        LiveTrackingAPI apiService = Util.getClient().create(LiveTrackingAPI.class);

        Call<LiveTracking> call = apiService.addLiveTracking(liveTracking);

        call.enqueue(new Callback<LiveTracking>() {
            @Override
            public void onResponse(Call<LiveTracking> call, Response<LiveTracking> response) {
//                List<RouteDTO.Routes> list = new ArrayList<RouteDTO.Routes>();
                try
                {


                    int statusCode = response.code();
                    if (statusCode == 200 || statusCode == 201) {

                        LiveTracking s = response.body();

                        if(s!=null){

                            Log.e("TAG", "Success");
                        }




                    }else {

                    }
                }
                catch (Exception ex)
                {


                    ex.printStackTrace();
                }
//                callGetStartEnd();
            }

            @Override
            public void onFailure(Call<LiveTracking> call, Throwable t) {


                Log.e("TAG", t.toString());
            }
        });



    }

    @Override
    public void onDestroy() {

        if(PreferenceHandler.getInstance(getApplicationContext()).getUserId()!=0&& PreferenceHandler.getInstance(getApplicationContext()).getUserRoleUniqueID()!=2&& PreferenceHandler.getInstance(getApplicationContext()).getLoginStatus().equalsIgnoreCase("Login")){


            Intent restartService = new Intent(getApplicationContext(),
                    LocationSharingServices.class);
            restartService.setPackage(getPackageName());
            PendingIntent restartServicePI = PendingIntent.getService(
                    getApplicationContext(), 1, restartService,
                    PendingIntent.FLAG_ONE_SHOT);

            //Restart the service once it has been killed android


            AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 30000, restartServicePI);
        }else{

            super.onDestroy();
        }


    }
}