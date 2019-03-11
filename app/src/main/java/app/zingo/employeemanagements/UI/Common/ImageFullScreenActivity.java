package app.zingo.employeemanagements.UI.Common;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import app.zingo.employeemanagements.Custom.Zoom;
import app.zingo.employeemanagements.R;
import app.zingo.employeemanagements.UI.NewAdminDesigns.EmployeeDashBoardAdminView;

public class ImageFullScreenActivity extends AppCompatActivity {

    ImageView mSrc;

    String src="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try{

            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);

            setContentView(R.layout.activity_image_full_screen);
            //setContentView(new Zoom(this));
            getWindow().getDecorView().setBackground(new ColorDrawable(Color.TRANSPARENT));


            mSrc = (ImageView)findViewById(R.id.image_src);

            Bundle bundle = getIntent().getExtras();
            if(bundle!=null){

                src = bundle.getString("Image");
            }

            if(src != null && !src.isEmpty()){
                Picasso.with(ImageFullScreenActivity.this).load(src).placeholder(R.drawable.no_image).error(R.drawable.no_image).into(mSrc);


            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
