package app.zingo.employeemanagements.UI.Common;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import app.zingo.employeemanagements.Model.Employee;
import app.zingo.employeemanagements.Model.EmployeeImages;
import app.zingo.employeemanagements.Model.LiveTracking;
import app.zingo.employeemanagements.Model.LiveTrackingAdminData;
import app.zingo.employeemanagements.Model.ReportDataModel;
import app.zingo.employeemanagements.R;

import app.zingo.employeemanagements.UI.Admin.EmployeeLiveMappingScreen;
import app.zingo.employeemanagements.Utils.PreferenceHandler;
import app.zingo.employeemanagements.Utils.ThreadExecuter;
import app.zingo.employeemanagements.Utils.Util;
import app.zingo.employeemanagements.WebApi.EmployeeApi;
import app.zingo.employeemanagements.WebApi.LiveTrackingAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.text.TextUtils.isEmpty;

public class AllEmployeeLiveLocation extends AppCompatActivity {

    //maps related
    private GoogleMap mMap;
    MapView mapView;
    LinearLayout mlocation;
    LinearLayout scrollableLay,mMapShow;
    TextView mName,mUpdatingTime,mAddress,mLoadMap;
    ImageView mShowHide;

    ArrayList<LatLng> MarkerPoints;
    private LatLng lastKnownLatLng;
    private Polyline gpsTrack;

   
    ArrayList<Integer> colorValue;

    boolean arrayValue = false;
    int screenheight,screenwidth;

    //Global var
    ArrayList<LiveTrackingAdminData> listLive;

    int listSize = 0,countValue=0;
    ArrayList<Employee> employees;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try{

            setContentView(R.layout.activity_all_employee_live_location);

            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            setTitle("Location History");

            mapView = (MapView) findViewById(R.id.employee_live_list_map);
            mlocation = (LinearLayout) findViewById(R.id.location_list);
            scrollableLay = (LinearLayout) findViewById(R.id.nestedScrollView);
            mMapShow = (LinearLayout) findViewById(R.id.map_lay_short);
            mShowHide = (ImageView) findViewById(R.id.show_lay_hide);
            mName = (TextView) findViewById(R.id.emp_name_loca);
            mUpdatingTime = (TextView) findViewById(R.id.updated_details);
            mAddress = (TextView) findViewById(R.id.address);
            mLoadMap = (TextView) findViewById(R.id.load_details);

            final ViewGroup.LayoutParams params = scrollableLay.getLayoutParams();
            final ViewGroup.LayoutParams paramsm = mMapShow.getLayoutParams();

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            screenheight = displayMetrics.heightPixels;
            screenwidth = displayMetrics.widthPixels;

            mapView.onCreate(savedInstanceState);
            mapView.onResume();

            MarkerPoints = new ArrayList<>();


            try {
                MapsInitializer.initialize(AllEmployeeLiveLocation.this);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;


                    if (ActivityCompat.checkSelfPermission(AllEmployeeLiveLocation.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(AllEmployeeLiveLocation.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        return;
                    }
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    mMap.getProjection().getVisibleRegion().latLngBounds.getCenter();
                    mMap.setMyLocationEnabled(true);


                    try{

                        mLoadMap.setText(""+new SimpleDateFormat("dd-MM-yyyy").format(new Date()));

                        getEmployees(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

                    }catch (Exception e){
                        e.printStackTrace();
                    }


                }
            });

            mShowHide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(mlocation.getVisibility()==View.VISIBLE){


                        // Changes the height and width to the specified *pixels*
                        params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                        params.width = screenwidth;
                        scrollableLay.setLayoutParams(params);

                        paramsm.height = LinearLayout.LayoutParams.MATCH_PARENT;
                        paramsm.width = screenwidth;
                        mMapShow.setLayoutParams(paramsm);

                        mlocation.setVisibility(View.GONE);
                        mShowHide.setImageResource(R.drawable.up_arrow_location);


                    }else{

                        // Changes the height and width to the specified *pixels*

                        params.width = screenwidth;
                        params.height = screenheight/2;
                        scrollableLay.setLayoutParams(params);

                        paramsm.width = screenwidth;
                        paramsm.height = screenheight/2;
                        mMapShow .setLayoutParams(paramsm);

                        mlocation.setVisibility(View.VISIBLE);
                        mShowHide.setImageResource(R.drawable.down_arrow_location);
                    }

                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void getEmployees(final String dateValue){


        final ProgressDialog progressDialog = new ProgressDialog(AllEmployeeLiveLocation.this);
        progressDialog.setTitle("Loading Details...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new ThreadExecuter().execute(new Runnable() {
            @Override
            public void run() {
                EmployeeApi apiService = Util.getClient().create(EmployeeApi.class);
                Call<ArrayList<Employee>> call = apiService.getEmployeesByOrgId(PreferenceHandler.getInstance(AllEmployeeLiveLocation.this).getCompanyId());

                call.enqueue(new Callback<ArrayList<Employee>>() {
                    @Override
                    public void onResponse(Call<ArrayList<Employee>> call, Response<ArrayList<Employee>> response) {
                        int statusCode = response.code();
                        if (statusCode == 200 || statusCode == 201 || statusCode == 203 || statusCode == 204) {


                            if (progressDialog!=null)
                                progressDialog.dismiss();
                            ArrayList<Employee> list = response.body();

                            listLive = new ArrayList<>();

                            if (list !=null && list.size()!=0) {

                                employees = new ArrayList<>();

                                Date date=null;
                                try {
                                    date = new SimpleDateFormat("yyyy-MM-dd").parse(dateValue);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                colorValue = new ArrayList<>();
                                for(int i=0;i<list.size();i++){

                                    if(list.get(i).getUserRoleId()!=2){

                                        employees.add(list.get(i));


                                      

                                    }
                                }


                                if(employees!=null&&employees.size()!=0){

                                    listSize = employees.size();

                                    for (Employee e:employees) {

                                        LiveTracking lv = new LiveTracking();
                                        lv.setEmployeeId(e.getEmployeeId());
                                        lv.setTrackingDate(new SimpleDateFormat("MM/dd/yyyy").format(date));
                                        getLiveLocation(lv,e);

                                    }


                                }else{
                                    Toast.makeText(AllEmployeeLiveLocation.this, "No Employes", Toast.LENGTH_SHORT).show();
                                }





                                //}

                            }else{

                            }

                        }else {

                            Toast.makeText(AllEmployeeLiveLocation.this, "Failed due to : "+response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<Employee>> call, Throwable t) {
                        // Log error here since request failed
                        if (progressDialog!=null)
                            progressDialog.dismiss();

                        Log.e("TAG", t.toString());
                    }
                });
            }


        });
    }

    private void getLiveLocation(final LiveTracking lv,final Employee employee){


        final ProgressDialog progressDialog = new ProgressDialog(AllEmployeeLiveLocation.this);
        progressDialog.setTitle("Loading Details..");
        progressDialog.setCancelable(false);
        progressDialog.show();

        countValue = countValue+1;

        new ThreadExecuter().execute(new Runnable() {
            @Override
            public void run() {
                LiveTrackingAPI apiService = Util.getClient().create(LiveTrackingAPI.class);
                Call<ArrayList<LiveTracking>> call = apiService.getLiveTrackingByEmployeeIdAndDate(lv);

                call.enqueue(new Callback<ArrayList<LiveTracking>>() {
                    @Override
                    public void onResponse(Call<ArrayList<LiveTracking>> call, Response<ArrayList<LiveTracking>> response) {
                        int statusCode = response.code();
                        if (statusCode == 200 || statusCode == 201 || statusCode == 203 || statusCode == 204) {


                            if (progressDialog!=null)
                                progressDialog.dismiss();
                            ArrayList<LiveTracking> list = response.body();
                            float distance = 0;


                            if (list !=null && list.size()!=0) {

                                LiveTrackingAdminData lts = new LiveTrackingAdminData();
                                lts.setEmpName(employee);
                                lts.setLiveTracking(list.get(list.size()-1));
                                listLive.add(lts);

                                lastKnownLatLng = new LatLng(Double.parseDouble(list.get(list.size()-1).getLatitude()),Double.parseDouble(list.get(list.size()-1).getLongitude()));
                                
                                createMarker(Double.parseDouble(list.get(list.size()-1).getLatitude()), Double.parseDouble(list.get(list.size()-1).getLongitude()),employee.getEmployeeName()+"\nLast Location ",""+""+getAddress(Double.parseDouble(list.get(list.size()-1).getLongitude()),Double.parseDouble(list.get(list.size()-1).getLatitude())));

                                if(listSize<=countValue){

                                    onAddField(listLive);

                                    try{

                                       //LatLng calymayor = new LatLng(Double.parseDouble(listLive.get(countValue).getLiveTracking().getLatitude()), Double.parseDouble(listLive.get(countValue).getLiveTracking().getLongitude()));
                                        mMap.moveCamera(CameraUpdateFactory.newLatLng(lastKnownLatLng));
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLatLng, 30));

                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }





                                }

                            }else{


                                LiveTrackingAdminData lts = new LiveTrackingAdminData();
                                lts.setEmpName(employee);
                                lts.setLiveTracking(null);
                                listLive.add(lts);



                                if(listSize<=countValue){


                                    onAddField(listLive);

                                    try{

                                        //LatLng calymayor = new LatLng(Double.parseDouble(listLive.get(countValue).getLiveTracking().getLatitude()), Double.parseDouble(listLive.get(countValue).getLiveTracking().getLongitude()));
                                        mMap.moveCamera(CameraUpdateFactory.newLatLng(lastKnownLatLng));
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLatLng, 30));

                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }

                                }



                            }

                        }else {



                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<LiveTracking>> call, Throwable t) {
                        // Log error here since request failed
                        if (progressDialog!=null)
                            progressDialog.dismiss();
                        Log.e("TAG", t.toString());
                    }
                });
            }


        });
    }

    protected Marker createMarker(double latitude, double longitude, String title, String snippet) {

        //BitmapDescriptorFactory.defaultMarker(new Random().nextInt(360));
        float color = new Random().nextInt(360);
        colorValue.add((int)color);

        return mMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .anchor(0.5f, 0.5f)
                .title(title)
                .snippet(snippet)
                .icon( BitmapDescriptorFactory.defaultMarker(color)));


    }

    public String getAddress(final double longitude,final double latitude )
    {

        String addressValue = "";
        try
        {
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(AllEmployeeLiveLocation.this, Locale.ENGLISH);


            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();



            System.out.println("address = "+address);

            String currentLocation;

            if(!isEmpty(address))
            {
                currentLocation=address;
                addressValue = address;

            }
            else
                System.out.println("Wrong");



        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            addressValue = "";
        }

        return addressValue;
    }

    public void onAddField(final  ArrayList<LiveTrackingAdminData> liveTrackingArrayList) {

        /*ViewGroup.LayoutParams params = scrollableLay.getLayoutParams();
// Changes the height and width to the specified *pixels*
        params.height = screenheight/2;
        params.width = screenwidth;
        scrollableLay.setLayoutParams(params);*/

       /* ViewGroup.LayoutParams paramsm = mMapShow.getLayoutParams();
        paramsm.width = screenwidth;
        paramsm.height = screenheight/2;
        mMapShow.setLayoutParams(paramsm);*/

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.bottom_location_places, null);



        TextView mNo = (TextView) rowView.findViewById(R.id.no_result);
        mNo.setVisibility(View.GONE);
        RecyclerView list = (RecyclerView)rowView.findViewById(R.id.location_lists);

        list.removeAllViews();

        if(liveTrackingArrayList!=null&&liveTrackingArrayList.size()!=0){
            mShowHide.setVisibility(View.VISIBLE);

            if(colorValue!=null&&colorValue.size()!=0){
                colorValue.remove(0);
            }

            LocationLiveAdapter adapter = new LocationLiveAdapter(AllEmployeeLiveLocation.this,liveTrackingArrayList,colorValue);
            list.setAdapter(adapter);



        }

        mlocation.addView(rowView);


    }

    public class LocationLiveAdapter extends RecyclerView.Adapter<LocationLiveAdapter.ViewHolder>{

        private Context context;
        private ArrayList<LiveTrackingAdminData> list;
        private ArrayList<Integer> colorDesign;
        private static final int VIEW_TYPE_TOP = 0;
        private static final int VIEW_TYPE_MIDDLE = 1;
        private static final int VIEW_TYPE_BOTTOM = 2;


        public LocationLiveAdapter(Context context, ArrayList<LiveTrackingAdminData> list,ArrayList<Integer> colorDesign) {

            this.context = context;
            this.list = list;
            this.colorDesign = colorDesign;


        }

        @Override
        public LocationLiveAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_all_emp_live, parent, false);
            LocationLiveAdapter.ViewHolder viewHolder = new LocationLiveAdapter.ViewHolder(v);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final LocationLiveAdapter.ViewHolder holder, final int position) {


            LiveTrackingAdminData item = list.get(position);

            holder.mItemTitle.setText(""+item.getEmpName().getEmployeeName());


            if(item.getLiveTracking()!=null){

                String froms = item.getLiveTracking().getTrackingDate();
                String times = item.getLiveTracking().getTrackingTime();

                Date fromDate = null;



                if(times!=null&&!times.isEmpty()) {



                    String dojs[] = froms.split("T");

                    try {


                        if(times==null||times.isEmpty()){
                            times = "00:00:00";
                        }
                        fromDate = new SimpleDateFormat("HH:mm:ss").parse(times);


                        String parses = new SimpleDateFormat("HH:mm").format(fromDate);
                        holder.mItemTime.setText("Last Updated at "+parses);

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }



                }
                double lng = Double.parseDouble(item.getLiveTracking().getLongitude());
                double lat = Double.parseDouble(item.getLiveTracking().getLatitude());
                getAddress(lng,lat,holder.mItemSubtitle);


                holder.mItemSubtitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {



                            Intent intent = new Intent(context, EmployeeLiveMappingScreen.class);
                            Bundle bundle = new Bundle();
                            bundle.putInt("EmployeeId",list.get(position).getEmpName().getEmployeeId());
                            bundle.putSerializable("Employee",list.get(position).getEmpName());
                            intent.putExtras(bundle);
                            context.startActivity(intent);




                    }
                });



            }


        }

        @Override
        public int getItemViewType(int position) {
            if(position == 0) {
                return VIEW_TYPE_TOP;
            }else if(position == list.size() - 1) {
                return VIEW_TYPE_BOTTOM;
            }else {
                return VIEW_TYPE_MIDDLE;
            }

        }



        @Override
        public int getItemCount() {
            return list.size();
        }


        class ViewHolder extends RecyclerView.ViewHolder /*implements View.OnClickListener*/ {

            TextView mItemTitle;
            TextView mItemSubtitle;
            TextView mItemTime;
            TextView mItemKm;
            FrameLayout mItemLine;
            ImageView mLocationColor;

            public ViewHolder(View itemView) {
                super(itemView);
                itemView.setClickable(true);

                mItemTitle = (TextView) itemView.findViewById(R.id.item_title);
                mItemSubtitle = (TextView) itemView.findViewById(R.id.item_subtitle);
                mItemTime = (TextView) itemView.findViewById(R.id.item_time);
                mItemKm = (TextView) itemView.findViewById(R.id.item_km);
                mItemLine = (FrameLayout) itemView.findViewById(R.id.item_line);
                mLocationColor = (ImageView) itemView.findViewById(R.id.location_color);


            }
        }

        public void getAddress(final double longitude,final double latitude,final TextView textView )
        {

            try
            {
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(context, Locale.ENGLISH);


                addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName();
                String local = addresses.get(0).getSubLocality();



                System.out.println("address = "+address);

                String currentLocation;

                if(!isEmpty(address))
                {
                    currentLocation=address;

                    textView.setText(currentLocation);


                }
                else{

                    textView.setText("Unknown");

                }



            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }

    public void getAddressValue(final double longitude,final double latitude,final TextView textView  )
    {

        try
        {
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(AllEmployeeLiveLocation.this, Locale.ENGLISH);


            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();
            String local = addresses.get(0).getSubLocality();



            System.out.println("address = "+address);

            String currentLocation;

            if(!isEmpty(address))
            {
                currentLocation=address;



                textView.setText(currentLocation);

            }
            else{

                textView.setText("Unknown");

            }



        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.refresh_calendar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id)
        {
            case android.R.id.home:

                AllEmployeeLiveLocation.this.finish();
                break;

            case R.id.action_calendar:

                openDatePicker();
                break;



            case R.id.action_refresh:

                restartActivity(AllEmployeeLiveLocation.this);
                //recreate();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void openDatePicker() {
        // Get Current Date

        final Calendar c = Calendar.getInstance();
        int mYear  = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay   = c.get(Calendar.DAY_OF_MONTH);
        //launch datepicker modal
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        try {
                            Log.d("Date", "DATE SELECTED "+dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

                            Calendar newDate = Calendar.getInstance();
                            newDate.set(year,monthOfYear,dayOfMonth);


                            String date1 = (monthOfYear + 1)  + "/" + (dayOfMonth) + "/" + year;

                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");



                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
                            try {
                                Date fdate = simpleDateFormat.parse(date1);

                                mLoadMap.setText(""+sdf.format(fdate));

                                if(employees!=null&&employees.size()!=0){

                                    listSize = employees.size();

                                    for (Employee e:employees) {

                                        LiveTracking lv = new LiveTracking();
                                        lv.setEmployeeId(e.getEmployeeId());
                                        lv.setTrackingDate(new SimpleDateFormat("MM/dd/yyyy").format(fdate));
                                        getLiveLocation(lv,e);

                                    }


                                }else{
                                   getEmployees(new SimpleDateFormat("yyyy-MM-dd").format(fdate));
                                }





                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                        }
                        catch (Exception ex)
                        {
                            ex.printStackTrace();
                        }


                    }
                }, mYear, mMonth, mDay);

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();

    }

    public static void restartActivity(Activity activity){
        if (Build.VERSION.SDK_INT >= 11) {
            activity.recreate();
        } else {
            activity.finish();
            activity.startActivity(activity.getIntent());
        }
    }
}
