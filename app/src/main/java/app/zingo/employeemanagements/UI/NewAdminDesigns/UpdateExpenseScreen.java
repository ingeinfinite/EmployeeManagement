package app.zingo.employeemanagements.UI.NewAdminDesigns;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import app.zingo.employeemanagements.Model.Expenses;
import app.zingo.employeemanagements.Model.Tasks;
import app.zingo.employeemanagements.R;
import app.zingo.employeemanagements.Utils.Util;
import app.zingo.employeemanagements.WebApi.ExpensesApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateExpenseScreen extends AppCompatActivity {

    TextInputEditText mExpenseType,mAmount,mCAmount,mTo;
    EditText mExpenseComment,mStatusRem;
    LinearLayout mExpenseImages;
    Spinner mStatus;
    AppCompatButton mApply;

    Expenses expenses;
    static int  ADAPTER_POSITION = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try{

            setContentView(R.layout.activity_update_expense_screen);

            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            setTitle("Update Expenses");

            mExpenseType = (TextInputEditText)findViewById(R.id.expense_title);
            mAmount = (TextInputEditText)findViewById(R.id.amount_expense);
            mCAmount = (TextInputEditText)findViewById(R.id.camount_expense);
            mTo = (TextInputEditText)findViewById(R.id.to_date);
            mExpenseComment = (EditText)findViewById(R.id.expense_description);
            mStatusRem = (EditText)findViewById(R.id.expense_remarks);
            mStatus = (Spinner) findViewById(R.id.expense_status_spinner);
            mApply = (AppCompatButton) findViewById(R.id.apply_expense);

            mExpenseImages = (LinearLayout) findViewById(R.id.expense_image);

            Bundle bundle = getIntent().getExtras();

            if (bundle != null) {

                expenses = (Expenses) bundle.getSerializable("Expenses");
                ADAPTER_POSITION = bundle.getInt("Position");

            }

            if(expenses!=null){

                mExpenseType.setText(""+expenses.getExpenseTitle());
                mAmount.setText(""+expenses.getAmount());
                mCAmount.setText(""+expenses.getClaimedAmount());


                String froms = expenses.getDate();


                Date afromDate = null;

                if(froms!=null&&!froms.isEmpty()){

                    if(froms.contains("T")){

                        String dojs[] = froms.split("T");

                        try {
                            afromDate = new SimpleDateFormat("yyyy-MM-dd").parse(dojs[0]);
                            froms = new SimpleDateFormat("MMM dd,yyyy").format(afromDate);

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }




                    }

                    mTo.setText(froms+"");

                }

                if(expenses.getDescription()!=null&&!expenses.getDescription().isEmpty()){
                    mExpenseComment.setText(""+expenses.getDescription());
                }

                if(expenses.getStatusRemarks()!=null&&!expenses.getStatusRemarks().isEmpty()){
                    mStatusRem.setText(""+expenses.getStatusRemarks());
                }


                String status = expenses.getStatus();

                if(status.equalsIgnoreCase("Pending")){

                    mStatus.setSelection(0);

                }else if(status.equalsIgnoreCase("Approved")){
                    mStatus.setSelection(1);

                }else if(status.equalsIgnoreCase("Rejected")){
                    mStatus.setSelection(2);

                }else if(status.equalsIgnoreCase("Cancelled")){
                    mStatus.setSelection(3);

                }else{

                    mStatus.setSelection(0);
                }



                if(expenses.getImageUrl()!=null&&!expenses.getImageUrl().isEmpty()){

                    final LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    try{
                        View v = vi.inflate(R.layout.gallery_layout, null);
                        ImageView blogs =(ImageView) v.findViewById(R.id.blog_images);

                        Picasso.with(UpdateExpenseScreen.this).load(expenses.getImageUrl()).placeholder(R.drawable.no_image).
                                error(R.drawable.no_image).into(blogs);


                        mExpenseImages.addView(v);

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

            }

            mApply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(expenses!=null){

                        validate();
                    }


                }
            });



        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void validate(){

        String des = mExpenseComment.getText().toString();
        String re = mStatusRem.getText().toString();

        Expenses updating = expenses;

        updating.setStatus(mStatus.getSelectedItem().toString());

        if(des!=null&&!des.isEmpty()){
            updating.setDescription(des);
        }else{
            updating.setDescription("");
        }

        if(re!=null&&!re.isEmpty()){
            updating.setStatusRemarks(re);
        }else{
            updating.setStatusRemarks("");
        }

        try {
            updateExpenses(updating);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void updateExpenses(final Expenses tasks) throws Exception{

        final ProgressDialog dialog = new ProgressDialog(UpdateExpenseScreen.this);
        dialog.setMessage("Saving Details..");
        dialog.setCancelable(false);
        dialog.show();

        ExpensesApi apiService = Util.getClient().create(ExpensesApi.class);

        Call<Expenses> call = apiService.updateExpenses(tasks.getExpenseId(),tasks);

        call.enqueue(new Callback<Expenses>() {
            @Override
            public void onResponse(Call<Expenses> call, Response<Expenses> response) {
//                List<RouteDTO.Routes> list = new ArrayList<RouteDTO.Routes>();
                try
                {
                    if(dialog != null && dialog.isShowing())
                    {
                        dialog.dismiss();
                    }

                    int statusCode = response.code();
                    if (statusCode == 200 || statusCode == 201|| statusCode == 204) {


                        Toast.makeText(UpdateExpenseScreen.this, "Update Expenses succesfully", Toast.LENGTH_SHORT).show();

                        UpdateExpenseScreen.this.finish();

                    }else {
                        Toast.makeText(UpdateExpenseScreen.this, "Failed Due to "+response.message(), Toast.LENGTH_SHORT).show();
                    }
                }
                catch (Exception ex)
                {

                    if(dialog != null && dialog.isShowing())
                    {
                        dialog.dismiss();
                    }
                    ex.printStackTrace();
                }
//                callGetStartEnd();
            }

            @Override
            public void onFailure(Call<Expenses> call, Throwable t) {

                if(dialog != null && dialog.isShowing())
                {
                    dialog.dismiss();
                }
                Toast.makeText(UpdateExpenseScreen.this, "Failed Due to "+t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("TAG", t.toString());
            }
        });



    }
}
