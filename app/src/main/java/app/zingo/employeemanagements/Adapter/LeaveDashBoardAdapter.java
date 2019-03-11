package app.zingo.employeemanagements.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import app.zingo.employeemanagements.Custom.MyRegulerText;
import app.zingo.employeemanagements.Model.Employee;
import app.zingo.employeemanagements.Model.Leaves;
import app.zingo.employeemanagements.R;
import app.zingo.employeemanagements.UI.NewAdminDesigns.UpdateLeaveScreen;
import app.zingo.employeemanagements.Utils.PreferenceHandler;
import app.zingo.employeemanagements.Utils.ThreadExecuter;
import app.zingo.employeemanagements.Utils.Util;
import app.zingo.employeemanagements.WebApi.EmployeeApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeaveDashBoardAdapter extends RecyclerView.Adapter<LeaveDashBoardAdapter.ViewHolder>{

    private Context context;
    private ArrayList<Leaves> list;

    public LeaveDashBoardAdapter(Context context, ArrayList<Leaves> list) {

        this.context = context;
        this.list = list;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_leave_dashboard, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Leaves dto = list.get(position);


        if(dto!=null){

            getEmployee(dto.getEmployeeId(),holder.mEmpName,"name");

            holder.mLeaveUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    Intent intent = new Intent(context, UpdateLeaveScreen.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("LeaveId",list.get(position).getLeaveId());
                    bundle.putSerializable("Leaves",list.get(position));
                    intent.putExtras(bundle);
                    context.startActivity(intent);



                }
            });

            if(PreferenceHandler.getInstance(context).getUserRoleUniqueID()==2||PreferenceHandler.getInstance(context).getUserRoleUniqueID()==9){
                holder.mEmpContact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        getEmployee(dto.getEmployeeId(),holder.mEmpName,"contact");



                    }
                });


            }else{
                holder.mEmpContact.setVisibility(View.GONE);
            }


            String froms = dto.getFromDate();
            String tos = dto.getToDate();

            if(froms.contains("T")){

                String dojs[] = froms.split("T");

                try {
                    Date afromDate = new SimpleDateFormat("yyyy-MM-dd").parse(dojs[0]);
                    froms = new SimpleDateFormat("MMM dd,yyyy").format(afromDate);


                } catch (ParseException e) {
                    e.printStackTrace();
                }


            }

            if(tos.contains("T")){

                String dojs[] = tos.split("T");

                try {
                    Date afromDate = new SimpleDateFormat("yyyy-MM-dd").parse(dojs[0]);
                    tos = new SimpleDateFormat("MMM dd,yyyy").format(afromDate);

                } catch (ParseException e) {
                    e.printStackTrace();
                }


            }


            holder.mLeaveDuration.setText(froms+" to "+tos);
            holder.mLeaveDesc.setText(""+dto.getLeaveComment());

            if(dto.getStatus()!=null&&!dto.getStatus().isEmpty()){

                if(dto.getStatus().equalsIgnoreCase("Approved")){

                    holder.mStatusView.setBackgroundColor(Color.parseColor("#00FF00"));
                    holder.mLeaveStatus.setBackground(context.getResources().getDrawable(R.drawable.oval_green));
                    holder.mLeaveStatus.setText("A");


                }else if(dto.getStatus().equalsIgnoreCase("Rejected")){

                    holder.mStatusView.setBackgroundColor(Color.parseColor("#FF0000"));
                    holder.mLeaveStatus.setBackground(context.getResources().getDrawable(R.drawable.oval_red));
                    holder.mLeaveStatus.setText("R");


                }else if(dto.getStatus().equalsIgnoreCase("Pending")){

                    holder.mStatusView.setBackgroundColor(Color.parseColor("#FFFF00"));
                    holder.mLeaveStatus.setBackground(context.getResources().getDrawable(R.drawable.oval_yellow));
                    holder.mLeaveStatus.setText("P");

                }
            }else{
                holder.mStatusView.setBackgroundColor(Color.parseColor("#FFFF00"));
                holder.mLeaveStatus.setBackground(context.getResources().getDrawable(R.drawable.oval_yellow));
                holder.mLeaveStatus.setText("P");
            }

            if(dto.getLeaveType()!=null&&!dto.getLeaveType().isEmpty()){

                if(dto.getLeaveType().equalsIgnoreCase("Paid")){

                    holder.mLeaveType.setText("Leave Type: "+dto.getLeaveType());

                }else if(dto.getLeaveType().equalsIgnoreCase("Un Paid")){

                    holder.mLeaveType.setText("Leave Type: "+dto.getLeaveType());

                }else{
                    holder.mLeaveType.setText("Leave Type: Pending");
                }
            }else{

                holder.mLeaveType.setText("Leave Type: Pending");
            }

        }


    }




    @Override
    public int getItemCount() {
        return list.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder{

        public MyRegulerText mEmpName,mLeaveDuration,mLeaveDesc,mLeaveType;
        public TextView mLeaveStatus;
        public LinearLayout mLeaveUpdate,mEmpContact;
        public View mStatusView;

        public ViewHolder(final View itemView) {
            super(itemView);
            itemView.setClickable(true);

            mEmpName = (MyRegulerText) itemView.findViewById(R.id.from_empl);
            mLeaveDuration = (MyRegulerText) itemView.findViewById(R.id.time_leave);
            mLeaveDesc = (MyRegulerText) itemView.findViewById(R.id.leave_desc);
            mLeaveType = (MyRegulerText) itemView.findViewById(R.id.leave_type);
            mLeaveStatus = (TextView) itemView.findViewById(R.id.status_text);
            mLeaveUpdate = (LinearLayout) itemView.findViewById(R.id.leave_update);
            mEmpContact = (LinearLayout) itemView.findViewById(R.id.contact_employee);
            mStatusView = (View) itemView.findViewById(R.id.status);

        }
    }

    private void getEmployee(final int id, final MyRegulerText textView,final String type){



        new ThreadExecuter().execute(new Runnable() {
            @Override
            public void run() {
                EmployeeApi apiService = Util.getClient().create(EmployeeApi.class);
                Call<ArrayList<Employee>> call = apiService.getProfileById(id);

                call.enqueue(new Callback<ArrayList<Employee>>() {
                    @Override
                    public void onResponse(Call<ArrayList<Employee>> call, Response<ArrayList<Employee>> response) {
                        int statusCode = response.code();
                        if (statusCode == 200 || statusCode == 201 || statusCode == 203 || statusCode == 204) {


                           /* if (progressDialog!=null)
                                progressDialog.dismiss();*/
                            ArrayList<Employee> list = response.body();


                            if (list !=null && list.size()!=0) {

                                final Employee employees = list.get(0);

                                if(employees!=null){

                                    try{

                                        if(type.equalsIgnoreCase("name")){
                                            textView.setText(""+employees.getEmployeeName());
                                        }else if(type.equalsIgnoreCase("contact")){

                                            try{


                                                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
                                                LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                                View views = inflater.inflate(R.layout.alert_contact_employee, null);

                                                builder.setView(views);



                                                final MyRegulerText mEmpName = (MyRegulerText) views.findViewById(R.id.employee_name);
                                                final MyRegulerText mPhone = (MyRegulerText) views.findViewById(R.id.call_employee);
                                                final MyRegulerText mEmail = (MyRegulerText) views.findViewById(R.id.email_employee);

                                                final android.support.v7.app.AlertDialog dialogs = builder.create();
                                                dialogs.show();
                                                dialogs.setCanceledOnTouchOutside(true);



                                                mEmpName.setText("Contact "+employees.getEmployeeName());
                                                mPhone.setText("Call "+employees.getPhoneNumber());
                                                mEmail.setText("Email "+employees.getPrimaryEmailAddress());


                                                mPhone.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {

                                                        Intent intent = new Intent(Intent.ACTION_DIAL);
                                                        intent.setData(Uri.parse("tel:"+employees.getPhoneNumber()));
                                                        context.startActivity(intent);
                                                    }
                                                });

                                                mEmail.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {

                                                        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

                                                        /* Fill it with Data */
                                                        emailIntent.setType("plain/text");
                                                        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{""+employees.getPrimaryEmailAddress()});
                                                        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Leave Acknowldgement/Details");
                                                        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");

                                                        /* Send it off to the Activity-Chooser */
                                                        context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));

                                                    }
                                                });









                                            }catch (Exception e){
                                                e.printStackTrace();
                                            }

                                        }


                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }


                            }else{

                            }

                        }else {



                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<Employee>> call, Throwable t) {


                        Log.e("TAG", t.toString());
                    }
                });
            }


        });
    }

}
