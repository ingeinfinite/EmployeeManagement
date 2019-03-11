package app.zingo.employeemanagements.UI.NewEmployeeDesign;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import app.zingo.employeemanagements.Adapter.MeetingDetailAdapter;
import app.zingo.employeemanagements.BuildConfig;
import app.zingo.employeemanagements.Custom.MyRegulerText;
import app.zingo.employeemanagements.Custom.RoundImageView;
import app.zingo.employeemanagements.FireBase.SharedPrefManager;
import app.zingo.employeemanagements.Model.Employee;
import app.zingo.employeemanagements.Model.EmployeeDeviceMapping;
import app.zingo.employeemanagements.Model.EmployeeImages;
import app.zingo.employeemanagements.Model.Meetings;
import app.zingo.employeemanagements.Model.Organization;
import app.zingo.employeemanagements.R;
import app.zingo.employeemanagements.Service.CheckDataAndLocation;
import app.zingo.employeemanagements.Service.DistanceCheck;
import app.zingo.employeemanagements.Service.LocationSharingServices;
import app.zingo.employeemanagements.UI.Admin.CreatePaySlip;
import app.zingo.employeemanagements.UI.Admin.DashBoardAdmin;
import app.zingo.employeemanagements.UI.Common.PlanExpireScreen;
import app.zingo.employeemanagements.UI.Employee.DashBoardEmployee;
import app.zingo.employeemanagements.UI.Landing.InternalServerErrorScreen;
import app.zingo.employeemanagements.UI.LandingScreen;
import app.zingo.employeemanagements.UI.NewAdminDesigns.AdminDashBoardFragment;
import app.zingo.employeemanagements.UI.NewAdminDesigns.AdminHomeFragment;
import app.zingo.employeemanagements.UI.NewAdminDesigns.AdminNewMainScreen;
import app.zingo.employeemanagements.UI.NewAdminDesigns.EmployerNotificationFragment;
import app.zingo.employeemanagements.UI.NewAdminDesigns.TaskListFragment;
import app.zingo.employeemanagements.UI.SignUpOptioins;
import app.zingo.employeemanagements.Utils.Constants;
import app.zingo.employeemanagements.Utils.PreferenceHandler;
import app.zingo.employeemanagements.Utils.ThreadExecuter;
import app.zingo.employeemanagements.Utils.Util;
import app.zingo.employeemanagements.WebApi.EmployeeApi;
import app.zingo.employeemanagements.WebApi.EmployeeDeviceApi;
import app.zingo.employeemanagements.WebApi.EmployeeImageAPI;
import app.zingo.employeemanagements.WebApi.MeetingsAPI;
import app.zingo.employeemanagements.WebApi.OrganizationApi;
import app.zingo.employeemanagements.WebApi.UploadApi;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeNewMainScreen extends AppCompatActivity {

    static final String TAG = "FounderMainScreen";
    RoundImageView mProfileImage;
    TextView mTrialMsgInfo;
    LinearLayout mTrialInfoLay,mShareLayout;

    boolean doubleBackToExitPressedOnce = false;
    public long[] mTimer = new long[1];
    /* renamed from: t */
    private Timer t;

    Employee profile;
    EmployeeImages employeeImages;
    int userId=0,imageId=0;
    String userName="",userEmail="";
    String appType="",planType="",licensesStartDate="",licenseEndDate="";
    int planId=0;

    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    String status,selectedImage;

    String currentVersion, latestVersion;
    Dialog dialog;

    private static final int REQUEST_PERMISSIONS = 100;
    boolean boolean_permission;
    SharedPreferences mPref;
    SharedPreferences.Editor medit;

    LinearLayout mWhatsapp;

    int pos;
    boolean con = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try{

            setContentView(R.layout.activity_employee_new_main_screen);
            setupData();
            Bundle extras = getIntent().getExtras();
            if(extras != null) {
                pos = extras.getInt("viewpager_position");
                con = extras.getBoolean("Condition");
            }

            PreferenceHandler.getInstance(EmployeeNewMainScreen.this).setFirstCheck(con);
            setupViewPager((ViewPager) findViewById(R.id.viewPager));
            mWhatsapp = (LinearLayout)findViewById(R.id.whatsapp_open);

            mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            medit = mPref.edit();



            getCurrentVersion();
            Intent serviceIntent = new Intent(EmployeeNewMainScreen.this,CheckDataAndLocation.class);
            startService(serviceIntent);

            mWhatsapp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String message = "Hi I'm "+PreferenceHandler.getInstance(EmployeeNewMainScreen.this).getUserFullName()+",\n My Organization Name is "+PreferenceHandler.getInstance(EmployeeNewMainScreen.this).getCompanyName()+".I am writing about the feedback of Zingy app Ver: "+BuildConfig.VERSION_NAME+".";

                    PackageManager packageManager = getPackageManager();
                    Intent i = new Intent(Intent.ACTION_VIEW);

                    try {
                        String url = "https://api.whatsapp.com/send?phone=+918987250539" +"&text=" + URLEncoder.encode(message, "UTF-8");
                        i.setPackage("com.whatsapp");
                        i.setData(Uri.parse(url));
                        if (i.resolveActivity(packageManager) != null) {
                            startActivity(i);
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(EmployeeNewMainScreen.this, "WhatsApp not installed.", Toast.LENGTH_SHORT).show();
                    }

                    /*try {

                     *//*Uri uri = Uri.parse("whatsapp://send?phone=+918987250539" );
                        Intent i = new Intent(Intent.ACTION_VIEW, uri);
                        i.putExtra("sms_body", "Hi");
                        startActivity(i);*//*

                        Uri uri = Uri.parse("smsto:+918987250539" );
                        Intent i = new Intent(Intent.ACTION_SENDTO, uri);
                        i.putExtra("sms_body", "Hi");
                        i.setPackage("com.whatsapp");
                        startActivity(i);
                    }
                    catch (ActivityNotFoundException e){
                        e.printStackTrace();
                        Toast.makeText(AdminNewMainScreen.this, "WhatsApp not installed.", Toast.LENGTH_SHORT).show();
                    }*/

                }
            });

            fn_permission();
            if (boolean_permission) {

                if (mPref.getString("service", "").matches("")&&PreferenceHandler.getInstance(EmployeeNewMainScreen.this).getLoginStatus().equalsIgnoreCase("Login")&&!PreferenceHandler.getInstance(EmployeeNewMainScreen.this).isLocationOn()) {
                    medit.putString("service", "service").commit();

                    Intent intent = new Intent(getApplicationContext(), DistanceCheck.class);
                    startService(intent);

                } else {
                   // Toast.makeText(getApplicationContext(), "Service is already running", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Please enable the gps", Toast.LENGTH_SHORT).show();
            }


        }catch(Exception e){
            e.printStackTrace();
        }

    }

    private void fn_permission() {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {

            if ((ActivityCompat.shouldShowRequestPermissionRationale(EmployeeNewMainScreen.this, android.Manifest.permission.ACCESS_FINE_LOCATION))) {


            } else {
                ActivityCompat.requestPermissions(EmployeeNewMainScreen.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION

                        },
                        REQUEST_PERMISSIONS);

            }
        } else {
            boolean_permission = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    boolean_permission = true;

                } else {
                    Toast.makeText(getApplicationContext(), "Please allow the permission", Toast.LENGTH_LONG).show();

                }
            }
        }
    }




    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList();
        private final List<String> mFragmentTitleList = new ArrayList();

        public ViewPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        public Fragment getItem(int i) {
            return (Fragment) this.mFragmentList.get(i);
        }

        public int getCount() {
            return this.mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String str) {
            this.mFragmentList.add(fragment);
            this.mFragmentTitleList.add(str);
        }

        public CharSequence getPageTitle(int i) {
            return (CharSequence) this.mFragmentTitleList.get(i);
        }
    }

    /*public void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(PreferenceHandler.getInstance(AdminNewMainScreen.this).getUserFullName());
        toolbar.setLogo(R.mipmap.ic_launcher);
    }*/

    private void setupTabIcons(TabLayout tabLayout) {
        tabLayout.getTabAt(4).setIcon(R.drawable.white_navigation);
    }

    private void setupViewPager(ViewPager viewPager) {
        viewPager.setOffscreenPageLimit(5);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        viewPagerAdapter.addFragment(EmployeeDashBoardFragment.getInstance(), "Dash Board");
        viewPagerAdapter.addFragment(EmployeeNotificationScrenFragment.getInstance(), "Notification");
        viewPagerAdapter.addFragment(EmployeeLoginFragment.getInstance(), "Attendance");
        viewPagerAdapter.addFragment(EmployeeTaskFragment.getInstance(), "Tasks");
        viewPagerAdapter.addFragment(EmployeeHomeFragment.getInstance(), "");
        viewPager.setAdapter(viewPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons(tabLayout);

        if(pos!=0){
            viewPager.setCurrentItem(pos);
        }
    }

    public void onBackPressed() {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (this.doubleBackToExitPressedOnce) {
            super.onBackPressed();
            finish();
        } else {
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_LONG).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    EmployeeNewMainScreen.this.doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

   /* public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_employer, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() != R.id.action_setting) {
            return super.onOptionsItemSelected(menuItem);
        }
        startActivity(new Intent(this, SettingsActivity.class));
        return true;
    }*/

    protected void onStart() {
        super.onStart();
    }

   /* public void setupNotification() {
        String token = FirebaseInstanceId.getInstance().getToken();
        Map hashMap = new HashMap();
        hashMap.put(VariableConstants.FCM_ID_TOKEN, token);
        FirebaseDatabase instance = FirebaseDatabase.getInstance();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("AppUser/");
        stringBuilder.append(FirebaseAuth.getInstance().getCurrentUser().getUid());
        instance.getReference(stringBuilder.toString()).updateChildren(hashMap);
    }*/

   /* public void managePermissions() {
        ((Builder) ((Builder) ((Builder) ((Builder) ((Builder) ((Builder) ((Builder) TedPermission.with(this).setPermissionListener(new C19413())).setRationaleTitle((int) C1404R.string.rationale_title)).setRationaleMessage((int) C1404R.string.rationale_message)).setDeniedTitle((CharSequence) "Permission denied")).setDeniedMessage((CharSequence) "If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")).setGotoSettingButtonText((CharSequence) "Go To Settings")).setPermissions("android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION", "android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE", "android.permission.READ_PHONE_STATE")).check();
    }*/

    public void setupData() {
        // View findViewById = findViewById(R.id.subscriptionIcon);
        //View findViewById2 = findViewById(R.id.settingIcon);
        TextView organizationName = (TextView) findViewById(R.id.organizationName);
        View profileView = findViewById(R.id.profile);
        TextView userName = (TextView) findViewById(R.id.userName);
        mProfileImage = (RoundImageView) findViewById(R.id.profilePicture);
        mTrialInfoLay = (LinearLayout) findViewById(R.id.trial_version_info_layout);
        mTrialMsgInfo = (TextView) findViewById(R.id.trial_version_info_msg);
        mTrialMsgInfo.setVisibility(View.GONE);
        mShareLayout = (LinearLayout) findViewById(R.id.share_layout);


        organizationName.setText(PreferenceHandler.getInstance(EmployeeNewMainScreen.this).getCompanyName());
        userName.setText(PreferenceHandler.getInstance(EmployeeNewMainScreen.this).getUserFullName());
        /*if (this.mAppUser != null) {
            PictureUtil.setAppUserPicture(imageView);
            if (StringUtils.isNotEmpty(this.mAppUser.getOrganizationName())) {
                textView.setText(this.mAppUser.getOrganizationName());
            } else {
                textView.setText("Company Name not Set");
            }
            if (StringUtils.isNotEmpty(this.mAppUser.getName())) {
                textView2.setText(this.mAppUser.getName());
            }
        }*/

        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){

            profile = (Employee) bundle.getSerializable("Profile");

        }
        userId = PreferenceHandler.getInstance(EmployeeNewMainScreen.this).getUserId();
        int mapId = PreferenceHandler.getInstance(EmployeeNewMainScreen.this).getMappingId();

        EmployeeDeviceMapping hm = new EmployeeDeviceMapping();
        String token = SharedPrefManager.getInstance(EmployeeNewMainScreen.this).getDeviceToken();

        if(userId!=0&&token!=null&&mapId==0){
            hm.setEmployeeId(userId);
            hm.setDeviceId(token);
            addDeviceId(hm);
        }

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                selectImage();
            }
        });

        if(profile==null){
            if(userId!=0){
                System.out.println("Going it");
                getProfile(userId,mProfileImage);
            }

        }else{

            profile.setAppOpen(true);
            updateProfile(profile);

            ArrayList<EmployeeImages> images = profile.getEmployeeImages();

            if(images!=null&&images.size()!=0){
                employeeImages = images.get(0);

                if(employeeImages!=null){

                    imageId = employeeImages.getEmployeeImageId();
                    String base=employeeImages.getImage();
                    if(base != null && !base.isEmpty()){
                        Picasso.with(EmployeeNewMainScreen.this).load(base).placeholder(R.drawable.profile_image).
                                error(R.drawable.profile_image).into(mProfileImage);


                    }
                }

            }

        }

        profileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        try {

            if(PreferenceHandler.getInstance(EmployeeNewMainScreen.this).getCompanyId()!=0){
                getCompany(PreferenceHandler.getInstance(EmployeeNewMainScreen.this).getCompanyId());
            }else{
               /* Intent i = new Intent(EmployeeNewMainScreen.this, InternalServerErrorScreen.class);

                startActivity(i);*/
            }


        } catch (Exception e) {
            e.printStackTrace();
            Intent i = new Intent(EmployeeNewMainScreen.this, InternalServerErrorScreen.class);

            startActivity(i);
        }
        //Subscribtion Icon visibility based on Employer
       /* if (StringUtils.equalsIgnoreCase(this.mAppUser.getUserType(), "Employer")) {
            findViewById.setOnClickListener(new C13816());
        } else {
            findViewById.setVisibility(8);
        }*/

        //Setting icon click function
        //profileView.setOnClickListener(new C13827());

        mShareLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String upToNCharacters = PreferenceHandler.getInstance(EmployeeNewMainScreen.this).getCompanyName().substring(0, Math.min(PreferenceHandler.getInstance(EmployeeNewMainScreen.this).getCompanyName().length(), 4));


                String body = "<html><head>" +
                        "</head>" +
                        "<body>" +
                        "<h2>Hello,</h2>" +
                        "<p><br>You are invited to join the Zingy Employee App Platform. </p></br></br>"+
                        "<br><p>Here is a Procedure to Join the Platform using the Below Procedures. Make sure you store them safely. </p>"+
                        "</br><p><br>Our Organization Code- "+upToNCharacters+PreferenceHandler.getInstance(EmployeeNewMainScreen.this).getCompanyId()+
                        "</br></p><br><b>Step 1:  </b>"+"Download the app by clicking here <a href=\"https://play.google.com/store/apps/details?id=app.zingo.employeemanagements\">https://play.google.com/store/apps/details?id=app.zingo.employeemanagements</a>"+
                        "</br><br><b>Step 2: </b>"+"Click on Get Started and \"Join us as an Employee\""+
                        "</br><br><b>Step 3: </b>"+"Verify your Mobile number and then Enter the Organization Code - "+upToNCharacters+PreferenceHandler.getInstance(EmployeeNewMainScreen.this).getCompanyId()+
                        "</br><br><b>Step 4:</b>"+"Enter your basic details and the complete the Sign up process"+
                        "</br><p>From now on, Please login to your account using your organization email id and your password on a daily basis for attendance system,leave management,Expense management, sales visit etc., via mobile app. </p>"+
                        "</br><p>If you have any questions then contact the Admin/HR of the company.</p>"+
                        "</br><p><b>Cheers,</b><br><br>"+PreferenceHandler.getInstance(EmployeeNewMainScreen.this).getUserFullName()+"</p></body></html>";

                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("text/plain");


                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Employee Management App Invitation");


                emailIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(new StringBuilder()
                        .append(body)
                        .toString()));
                //emailIntent.putExtra(Intent.EXTRA_HTML_TEXT, body);
                startActivity(Intent.createChooser(emailIntent, "Send email.."));
            }
        });
    }

    private void setTimer() {
        if (this.t == null) {
            this.t = new Timer();
            this.t.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    long[] jArr = mTimer;
                    jArr[0] = jArr[0] + 1000;
                }
            }, 0, 1000);
        }
    }

    private void stopTimer() {
        if (this.t != null) {
            this.t.cancel();
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }


    public void getProfile(final int id,final ImageView mProfileImage ){

        new ThreadExecuter().execute(new Runnable() {
            @Override
            public void run() {

                final EmployeeApi subCategoryAPI = Util.getClient().create(EmployeeApi.class);
                Call<ArrayList<Employee>> getProf = subCategoryAPI.getProfileById(id);
                //Call<ArrayList<Blogs>> getBlog = blogApi.getBlogs();

                getProf.enqueue(new Callback<ArrayList<Employee>>() {

                    @Override
                    public void onResponse(Call<ArrayList<Employee>> call, Response<ArrayList<Employee>> response) {

                        if (response.code() == 200)
                        {
                            System.out.println("Inside api");

                            profile = response.body().get(0);
                            profile.setAppOpen(true);
                            updateProfile(profile);

                            ArrayList<EmployeeImages> images = profile.getEmployeeImages();

                            if(images!=null&&images.size()!=0){
                                employeeImages = images.get(0);

                                if(employeeImages!=null){
                                    imageId = employeeImages.getEmployeeImageId();
                                    String base=employeeImages.getImage();
                                    if(base != null && !base.isEmpty()){
                                        Picasso.with(EmployeeNewMainScreen.this).load(base).placeholder(R.drawable.profile_image).
                                                error(R.drawable.profile_image).into(mProfileImage);


                                    }
                                }

                            }

                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<Employee>> call, Throwable t) {

                    }
                });

            }

        });
    }

    private void selectImage() {
        final CharSequence[] items = {"Choose from Library",
                "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(EmployeeNewMainScreen.this);
        builder.setTitle("Add Image!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Choose from Library")) {

                    galleryIntent();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);

        }
    }

    private void onSelectFromGalleryResult(Intent data) {

        try{


            Uri selectedImageUri = data.getData( );
            String picturePath = getPath( EmployeeNewMainScreen.this, selectedImageUri );
            Log.d("Picture Path", picturePath);
            String[] all_path = {picturePath};
            selectedImage = all_path[0];
            System.out.println("allpath === "+data.getPackage());
            for (String s:all_path)
            {
                //System.out.println(s);
                File imgFile = new  File(s);
                if(imgFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    //addView(null,Util.getResizedBitmap(myBitmap,400));
                    addImage(null,Util.getResizedBitmap(myBitmap,700));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    public static String getPath(Context context, Uri uri ) {
        String result = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver( ).query( uri, proj, null, null, null );
        if(cursor != null){
            if ( cursor.moveToFirst( ) ) {
                int column_index = cursor.getColumnIndexOrThrow( proj[0] );
                result = cursor.getString( column_index );
            }
            cursor.close( );
        }
        if(result == null) {
            result = "Not found";
        }
        return result;
    }

    public void addImage(String uri,Bitmap bitmap)
    {
        try{


            if(uri != null)
            {

            }
            else if(bitmap != null)
            {
                mProfileImage.setImageBitmap(bitmap);

                if(selectedImage != null && !selectedImage.isEmpty())
                {
                    File file = new File(selectedImage);

                    if(file.length() <= 1*1024*1024)
                    {
                        FileOutputStream out = null;
                        String[] filearray = selectedImage.split("/");
                        final String filename = getFilename(filearray[filearray.length-1]);

                        out = new FileOutputStream(filename);
                        Bitmap myBitmap = BitmapFactory.decodeFile(selectedImage);

//          write the compressed bitmap at the field_icon specified by filename.
                        myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

                        uploadImage(filename,profile);



                    }
                    else
                    {
                        compressImage(selectedImage,profile);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void uploadImage(final String filePath,final Employee employee)
    {
        //String filePath = getRealPathFromURIPath(uri, ImageUploadActivity.this);

        final File file = new File(filePath);
        int size = 1*1024*1024;

        if(file != null)
        {
            if(file.length() > size)
            {
                System.out.println(file.length());
                compressImage(filePath,employee);
            }
            else
            {
                final ProgressDialog dialog = new ProgressDialog(EmployeeNewMainScreen.this);
                dialog.setCancelable(false);
                dialog.setTitle("Uploading Image..");
                dialog.show();
                Log.d("Image Upload", "Filename " + file.getName());

                RequestBody mFile = RequestBody.create(MediaType.parse("image"), file);
                MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("file", file.getName(), mFile);
                RequestBody filename = RequestBody.create(MediaType.parse("text/plain"), file.getName());
                UploadApi uploadImage = Util.getClient().create(UploadApi.class);

                Call<String> fileUpload = uploadImage.uploadProfileImages(fileToUpload, filename);
                fileUpload.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if(dialog != null && dialog.isShowing())
                        {
                            dialog.dismiss();
                        }


                        if(employeeImages==null){
                            EmployeeImages employeeImages = new EmployeeImages();

                            if(Util.IMAGE_URL==null){
                                employeeImages.setImage(Constants.IMAGE_URL+response.body().toString());
                            }else{
                                employeeImages.setImage(Util.IMAGE_URL+response.body().toString());
                            }


                            employeeImages.setEmployeeId(employee.getEmployeeId());


                            addProfileImage(employeeImages);
                        }else{

                            EmployeeImages employeeImagess = employeeImages;
                            if(Util.IMAGE_URL==null){
                                employeeImages.setImage(Constants.IMAGE_URL+response.body().toString());
                            }else{
                                employeeImages.setImage(Util.IMAGE_URL+response.body().toString());
                            }
                            employeeImagess.setEmployeeImageId(employeeImages.getEmployeeImageId());
                            employeeImages.setEmployeeId(employee.getEmployeeId());

                            updateProfileImage(employeeImages);
                        }




                        if(filePath.contains("MyFolder/Images"))
                        {
                            file.delete();
                        }
                    }
                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Log.d("UpdateCate", "Error " + t.getMessage());
                    }
                });
            }
        }
    }

    public String compressImage(String filePath,final  Employee Employee) {

        //String filePath = getRealPathFromURI(imageUri);
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = actualHeight/2;//2033.0f;
        float maxWidth = actualWidth/2;//1011.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String[] filearray = filePath.split("/");
        final String filename = getFilename(filearray[filearray.length-1]);
        try {
            out = new FileOutputStream(filename);


//          write the compressed bitmap at the field_icon specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

            uploadImage(filename,Employee);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return filename;

    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    public String getFilename(String filePath) {
        File file = new File(Environment.getExternalStorageDirectory().getPath(), "MyFolder/Images");
        if (!file.exists()) {
            file.mkdirs();
        }
        System.out.println("getFilePath = "+filePath);
        String uriSting;
        if(filePath.contains(".jpg"))
        {
            uriSting = (file.getAbsolutePath() + "/" + filePath);
        }
        else
        {
            uriSting = (file.getAbsolutePath() + "/" + filePath+".jpg" );
        }
        return uriSting;

    }

    private void updateProfileImage(final EmployeeImages employeeImages) {


        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setTitle("Updating Image..");
        dialog.show();

        new ThreadExecuter().execute(new Runnable() {
            @Override
            public void run() {
                EmployeeImageAPI auditApi = Util.getClient().create(EmployeeImageAPI.class);
                Call<EmployeeImages> response = auditApi.updateEmployeeImage(employeeImages.getEmployeeImageId(),employeeImages);
                response.enqueue(new Callback<EmployeeImages>() {
                    @Override
                    public void onResponse(Call<EmployeeImages> call, Response<EmployeeImages> response) {
                        if(dialog != null)
                        {
                            dialog.dismiss();
                        }
                        System.out.println(response.code());

                        if(response.code() == 201||response.code() == 200||response.code() == 204)
                        {
                            Toast.makeText(EmployeeNewMainScreen.this,"Profile Image Updated",Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(EmployeeNewMainScreen.this,response.message(),Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<EmployeeImages> call, Throwable t) {
                        if(dialog != null)
                        {
                            dialog.dismiss();
                        }
                        Toast.makeText(EmployeeNewMainScreen.this,t.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });
    }

    private void addProfileImage(final EmployeeImages employeeImages) {


        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setTitle("Updating Image..");
        dialog.show();

        new ThreadExecuter().execute(new Runnable() {
            @Override
            public void run() {
                EmployeeImageAPI auditApi = Util.getClient().create(EmployeeImageAPI.class);
                Call<EmployeeImages> response = auditApi.addEmployeeImage(employeeImages);
                response.enqueue(new Callback<EmployeeImages>() {
                    @Override
                    public void onResponse(Call<EmployeeImages> call, Response<EmployeeImages> response) {
                        if(dialog != null)
                        {
                            dialog.dismiss();
                        }
                        System.out.println(response.code());

                        if(response.code() == 201||response.code() == 200||response.code() == 204)
                        {
                            Toast.makeText(EmployeeNewMainScreen.this,"Profile Image Updated",Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(EmployeeNewMainScreen.this,response.message(),Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<EmployeeImages> call, Throwable t) {
                        if(dialog != null)
                        {
                            dialog.dismiss();
                        }
                        Toast.makeText(EmployeeNewMainScreen.this,t.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });
    }

    public void addDeviceId(final EmployeeDeviceMapping pf)
    {

        new ThreadExecuter().execute(new Runnable() {
            @Override
            public void run() {


                EmployeeDeviceApi hotelOperation = Util.getClient().create(EmployeeDeviceApi.class);
                Call<EmployeeDeviceMapping> response = hotelOperation.addProfileDevice(pf);

                response.enqueue(new Callback<EmployeeDeviceMapping>() {
                    @Override
                    public void onResponse(Call<EmployeeDeviceMapping> call, Response<EmployeeDeviceMapping> response) {
                        System.out.println("GetHotelByProfileId = "+response.code());


                        if(response.code() == 200||response.code() == 201||response.code() == 202||response.code() == 204)
                        {
                            try{
                                System.out.println("registered");
                                EmployeeDeviceMapping pr = response.body();

                                System.out.println();

                                if(pr != null)
                                {

                                    PreferenceHandler.getInstance(EmployeeNewMainScreen.this).setMappingId(pr.getEmployeeDeviceMappingId());



                                }

                            }catch (Exception e){
                                e.printStackTrace();
                            }




                        }else if(response.code() == 404){
                            System.out.println("already registered");



                        }
                        else
                        {


                        }


                    }

                    @Override
                    public void onFailure(Call<EmployeeDeviceMapping> call, Throwable t) {


                    }
                });
            }
        });
    }

    public void getCompany(final int id) throws Exception{

        new ThreadExecuter().execute(new Runnable() {
            @Override
            public void run() {

                final OrganizationApi subCategoryAPI = Util.getClient().create(OrganizationApi.class);
                Call<ArrayList<Organization>> getProf = subCategoryAPI.getOrganizationById(id);
                //Call<ArrayList<Blogs>> getBlog = blogApi.getBlogs();

                getProf.enqueue(new Callback<ArrayList<Organization>>() {

                    @Override
                    public void onResponse(Call<ArrayList<Organization>> call, Response<ArrayList<Organization>> response) {

                        if (response.code() == 200||response.code() == 201||response.code() == 204&&response.body().size()!=0)
                        {

                            Organization organization = response.body().get(0);
                            System.out.println("Inside api");
                            PreferenceHandler.getInstance(EmployeeNewMainScreen.this).setCompanyId(organization.getOrganizationId());
                            PreferenceHandler.getInstance(EmployeeNewMainScreen.this).setCompanyName(organization.getOrganizationName());
                            PreferenceHandler.getInstance(EmployeeNewMainScreen.this).setAppType(organization.getAppType());

                            PreferenceHandler.getInstance(EmployeeNewMainScreen.this).setAppType(organization.getAppType());
                            PreferenceHandler.getInstance(EmployeeNewMainScreen.this).setLicenseStartDate(organization.getLicenseStartDate());
                            PreferenceHandler.getInstance(EmployeeNewMainScreen.this).setLicenseEndDate(organization.getLicenseEndDate());
                            PreferenceHandler.getInstance(EmployeeNewMainScreen.this).setSignupDate(organization.getSignupDate());
                            PreferenceHandler.getInstance(EmployeeNewMainScreen.this).setOrganizationLongi(organization.getLongitude());
                            PreferenceHandler.getInstance(EmployeeNewMainScreen.this).setOrganizationLati(organization.getLatitude());
                            PreferenceHandler.getInstance(EmployeeNewMainScreen.this).setPlanType(organization.getPlanType());
                            PreferenceHandler.getInstance(EmployeeNewMainScreen.this).setEmployeeLimit(organization.getEmployeeLimit());
                            PreferenceHandler.getInstance(EmployeeNewMainScreen.this).setPlanId(organization.getPlanId());

                            appType = PreferenceHandler.getInstance(EmployeeNewMainScreen.this).getAppType();
                            planType = PreferenceHandler.getInstance(EmployeeNewMainScreen.this).getPlanType();
                            licensesStartDate = PreferenceHandler.getInstance(EmployeeNewMainScreen.this).getLicenseStartDate();
                            licenseEndDate = PreferenceHandler.getInstance(EmployeeNewMainScreen.this).getLicenseEndDate();
                            planId = PreferenceHandler.getInstance(EmployeeNewMainScreen.this).getPlanId();

                            try{

                                if(appType!=null){

                                    if(appType.equalsIgnoreCase("Trial")){

                                        SimpleDateFormat smdf = new SimpleDateFormat("MM/dd/yyyy");

                                        long days = dateCal(licenseEndDate);


                                        mTrialInfoLay.setVisibility(View.VISIBLE);
                                        if((smdf.parse(licenseEndDate).getTime()<smdf.parse(smdf.format(new Date())).getTime())){

                                            Toast.makeText(EmployeeNewMainScreen.this, "Your Trial Period is Expired", Toast.LENGTH_SHORT).show();
                                            PreferenceHandler.getInstance(EmployeeNewMainScreen.this).clear();

                                            Intent log = new Intent(EmployeeNewMainScreen.this, PlanExpireScreen.class);
                                            log.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            log.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                            //Toast.makeText(EmployeeNewMainScreen.this,"Logout",Toast.LENGTH_SHORT).show();
                                            startActivity(log);
                                            finish();

                                        }else{
                                            mTrialMsgInfo.setText("Your Trial version is going to expiry in "+days+" days");
                                            if(days>=1&&days<=5){
                                                 //popupUpgrade("Hope your enjoying to use our Trial version.Get more features You need to Upgrade App","Your trial period is going to expire in "+days+" days");



                                            }else if(days==0){
                                               // popupUpgrade("Hope your enjoying to use our Trial version.Get more features You need to Upgrade App","Today is last day for your free trial");
                                                mTrialMsgInfo.setText("Your Trial version is going to expiry in today");

                                            }else if(days<0){
                                                Toast.makeText(EmployeeNewMainScreen.this, "Your Trial Period is Expired", Toast.LENGTH_SHORT).show();
                                                PreferenceHandler.getInstance(EmployeeNewMainScreen.this).clear();

                                                Intent log = new Intent(EmployeeNewMainScreen.this, PlanExpireScreen.class);
                                                log.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                log.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                                //Toast.makeText(EmployeeNewMainScreen.this,"Logout",Toast.LENGTH_SHORT).show();
                                                startActivity(log);
                                                finish();
                                            }

                                        }

                                    }else if(appType.equalsIgnoreCase("Paid")){
                                        mTrialInfoLay.setVisibility(View.GONE);
                                    }
                                }

                            }catch (Exception e){
                                e.printStackTrace();
                            }






                        }else{


                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<Organization>> call, Throwable t) {

                    }
                });

            }

        });
    }

    public long dateCal(String date){

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");



        Date fd=null,td=null;

        try {
            fd = sdf.parse(""+date);
            td = sdf.parse(""+sdf.format(new Date()));

            long diff = fd.getTime() - td.getTime();
            long days = diff / (24 * 60 * 60 * 1000);



            return  days;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }


    }


    private void getCurrentVersion() {
        System.out.println("Google inside");
        PackageManager pm = this.getPackageManager();
        PackageInfo pInfo = null;

        try {
            pInfo = pm.getPackageInfo(this.getPackageName(), 0);

        } catch (PackageManager.NameNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        currentVersion = pInfo.versionName;
        String app_version = PreferenceHandler.getInstance(EmployeeNewMainScreen.this).getAppVersion();

        if(app_version!=null&&!app_version.isEmpty()){


            if(currentVersion.equalsIgnoreCase(app_version)){

            }else{
                final AlertDialog.Builder builder = new AlertDialog.Builder(EmployeeNewMainScreen.this);
                builder.setTitle("A New Update is Available");
                builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse
                                ("https://play.google.com/store/apps/details?id=app.zingo.employeemanagements")));
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //background.start();
                        // Toast.makeText(AdminNewMainScreen.this, "Check", Toast.LENGTH_SHORT).show();
                    }
                });

                builder.setCancelable(false);
                dialog = builder.show();
            }
        }

        // new GetVersionCode().execute();

    }

    class GetVersionCode extends AsyncTask<Void, String, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String newVersion = null;
            try {
                Connection connection = Jsoup.connect("https://play.google.com/store/apps/details?id=app.zingo.employeemanagements" + "&hl=en");
                Document document = connection.timeout(30000)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .get();

                Elements versions = document.getElementsByClass("htlgb");

                for (int i = 0; i < versions.size(); i++) {
                    newVersion = versions.get(i).text();
                    if (Pattern.matches("^[0-9]{1}.[0-9]{1}.[0-9]{1}$", newVersion)) {
                        break;
                    }
                }

            } catch (Exception e) {
                return newVersion;
            }
            return newVersion;
        }

        @Override
        protected void onPostExecute(String onlineVersion) {
            super.onPostExecute(onlineVersion);
            if (onlineVersion != null && !onlineVersion.isEmpty()) {
                System.out.println("Online Version==" + onlineVersion);
                System.out.println("Current Version==" + currentVersion);
                if (!onlineVersion.equalsIgnoreCase(currentVersion)) {
                    showUpdateDialog();
                } else {
                    // Toast.makeText(MainActivity.this, "Check", Toast.LENGTH_SHORT).show();
                    System.out.println("Check");
                }
            } else {
                System.out.println("Check out");
            }
        }


        private void showUpdateDialog() {
            final AlertDialog.Builder builder = new AlertDialog.Builder(EmployeeNewMainScreen.this);
            builder.setTitle("A New Update is Available");
            builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse
                            ("https://play.google.com/store/apps/details?id=app.zingo.employeemanagements")));
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    //background.start();
                    Toast.makeText(EmployeeNewMainScreen.this, "Check", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setCancelable(false);
            dialog = builder.show();
        }


    }

    public void updateProfile(final Employee employee){



        new ThreadExecuter().execute(new Runnable() {
            @Override
            public void run() {

                final EmployeeApi subCategoryAPI = Util.getClient().create(EmployeeApi.class);
                Call<Employee> getProf = subCategoryAPI.updateEmployee(employee.getEmployeeId(),employee);
                //Call<ArrayList<Blogs>> getBlog = blogApi.getBlogs();

                getProf.enqueue(new Callback<Employee>() {

                    @Override
                    public void onResponse(Call<Employee> call, Response<Employee> response) {


                        if (response.code() == 200||response.code()==201||response.code()==204)
                        {


                        }else{
                            // Toast.makeText(ChangePasswordScreen.this, "Failed due to status code"+response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Employee> call, Throwable t) {


                        //  Toast.makeText(ChangePasswordScreen.this, "Something went wrong due to "+t.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

            }

        });
    }

    private void getMeetingsDetails(final Meetings loginDetails){

        new ThreadExecuter().execute(new Runnable() {
            @Override
            public void run() {
                MeetingsAPI apiService = Util.getClient().create(MeetingsAPI.class);
                Call<ArrayList<Meetings>> call = apiService.getMeetingsByEmployeeIdAndDate(loginDetails);

                call.enqueue(new Callback<ArrayList<Meetings>>() {
                    @Override
                    public void onResponse(Call<ArrayList<Meetings>> call, Response<ArrayList<Meetings>> response) {
                        int statusCode = response.code();
                        if (statusCode == 200 || statusCode == 201 || statusCode == 203 || statusCode == 204) {



                            ArrayList<Meetings> list = response.body();

                            if (list !=null && list.size()!=0) {

                            }else{
                                // Toast.makeText(DailyTargetsForEmployeeActivity.this, "No Tasks given for this employee ", Toast.LENGTH_SHORT).show();

                            }

                        }else {



                            //Toast.makeText(DailyTargetsForEmployeeActivity.this, "Failed due to : "+response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<Meetings>> call, Throwable t) {
                        // Log error here since request failed
                       /* if (progressDialog!=null)
                            progressDialog.dismiss();*/
                        Log.e("TAG", t.toString());
                    }
                });
            }


        });
    }

}
