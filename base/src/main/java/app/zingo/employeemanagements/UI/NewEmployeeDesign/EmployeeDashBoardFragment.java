package app.zingo.employeemanagements.UI.NewEmployeeDesign;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import app.zingo.employeemanagements.Adapter.TaskListAdapter;
import app.zingo.employeemanagements.Custom.MyRegulerText;
import app.zingo.employeemanagements.Model.Employee;
import app.zingo.employeemanagements.Model.Leaves;
import app.zingo.employeemanagements.Model.LoginDetails;
import app.zingo.employeemanagements.Model.Tasks;
import app.zingo.employeemanagements.UI.NewAdminDesigns.PendingTasks;
import app.zingo.employeemanagements.Utils.PreferenceHandler;
import app.zingo.employeemanagements.Utils.ThreadExecuter;
import app.zingo.employeemanagements.Utils.Util;
import app.zingo.employeemanagements.WebApi.EmployeeApi;
import app.zingo.employeemanagements.WebApi.LeaveAPI;
import app.zingo.employeemanagements.WebApi.LoginDetailsAPI;
import app.zingo.employeemanagements.WebApi.TasksAPI;
import app.zingo.employeemanagements.base.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class EmployeeDashBoardFragment extends Fragment {

    final String TAG = "Employer Dash";
    View layout;
    RecyclerView mTaskList;
    TextView mLoggedTime,mMeeting;
    LinearLayout mTodayTaskLayout,mPendingLayout;
    private TaskListAdapter mAdapter;
    MyRegulerText mWorkedDays,mLeaveDays,mOnTask,mPending;

    Context mContext;



    int onTasks=0,pendingTask=0;



    ArrayList<Tasks> employeeTasks;
    ArrayList<Tasks> pendingTasks ;
    ArrayList<Tasks> completedTasks ;
    ArrayList<Tasks> closedTasks ;
    ArrayList<Tasks> onTask ;

    int monthDate = -1;



    public EmployeeDashBoardFragment() {
        // Required empty public constructor
    }

    public static EmployeeDashBoardFragment getInstance() {
        return new EmployeeDashBoardFragment();
    }

    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        // GAUtil.trackScreen(getActivity(), "Employer Dashboard");
    }


    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        super.onCreateView(layoutInflater, viewGroup, savedInstanceState);
        try{
            this.layout = layoutInflater.inflate(R.layout.fragment_employee_dash_board, viewGroup, false);

            mContext = getContext();

            mTaskList = layout.findViewById(R.id.task_list_dash);
            mLoggedTime = layout.findViewById(R.id.log_out_time);
            mMeeting = layout.findViewById(R.id.meeting_info);
            mTodayTaskLayout = layout.findViewById(R.id.today_task_list);
            mTodayTaskLayout.setVisibility(View.GONE);
            mWorkedDays = layout.findViewById(R.id.worked_days_count);
            mLeaveDays = layout.findViewById(R.id.leave_days_count);
            mOnTask = layout.findViewById(R.id.on_task_count_text);
            mPendingLayout = layout.findViewById(R.id.pending_task_layout);
            mPending = layout.findViewById(R.id.pending_task_text);


            String meetingStatus = PreferenceHandler.getInstance(getActivity()).getMeetingLoginStatus();

            if (meetingStatus != null && meetingStatus.equalsIgnoreCase("Login")) {
                mMeeting.setText("You are in some meeting .So Please checkout");
            }


            getEmployees();


            mPendingLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(pendingTasks!=null&&pendingTasks.size()!=0){
                        Intent pending = new Intent(getActivity(), PendingTasks.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("PendingTasksNormal",pendingTasks);
                        pending.putExtras(bundle);
                        startActivity(pending);
                    }

                }
            });


            return this.layout;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public void onResume() {
        super.onResume();
    }

    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
    }

    private void getEmployees(){




        new ThreadExecuter().execute(new Runnable() {
            @Override
            public void run() {
                EmployeeApi apiService = Util.getClient().create(EmployeeApi.class);
                Call<ArrayList<Employee>> call = apiService.getProfileById(PreferenceHandler.getInstance(getActivity()).getUserId());

                call.enqueue(new Callback<ArrayList<Employee>>() {
                    @Override
                    public void onResponse(Call<ArrayList<Employee>> call, Response<ArrayList<Employee>> response) {
                        int statusCode = response.code();
                        if (statusCode == 200 || statusCode == 201 || statusCode == 203 || statusCode == 204) {


                           /* if (progressDialog != null&&progressDialog.isShowing())
                                progressDialog.dismiss();*/
                            ArrayList<Employee> list = response.body();


                            if (list !=null && list.size()!=0) {

                               Employee employees = list.get(0);


                                        getTasks(employees.getEmployeeId());
                                        getApprovedLeaveDetails(employees.getEmployeeId());
                                        //getRejectedLeaveDetails(employees.getEmployeeId());



                                        LoginDetails loginDetails = new LoginDetails();
                                        loginDetails.setEmployeeId(employees.getEmployeeId());
                                        loginDetails.setLoginDate(new SimpleDateFormat("MM/dd/yyyy").format(new Date()));
                                        getLoginDetails(loginDetails);


                                //}

                            }else{

                            }

                        }else {


                            Toast.makeText(getActivity(), "Failed due to : "+response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<Employee>> call, Throwable t) {
                        // Log error here since request failed
                      /*  if (progressDialog != null&&progressDialog.isShowing())
                            progressDialog.dismiss();*/

                        Log.e("TAG", t.toString());
                    }
                });
            }


        });
    }







    private void getTasks(final int employeeId){




        new ThreadExecuter().execute(new Runnable() {
            @Override
            public void run() {
                TasksAPI apiService = Util.getClient().create(TasksAPI.class);
                Call<ArrayList<Tasks>> call = apiService.getTasks();

                call.enqueue(new Callback<ArrayList<Tasks>>() {
                    @Override
                    public void onResponse(Call<ArrayList<Tasks>> call, Response<ArrayList<Tasks>> response) {
                        int statusCode = response.code();
                        if (statusCode == 200 || statusCode == 201 || statusCode == 203 || statusCode == 204) {



                            ArrayList<Tasks> list = response.body();
                            ArrayList<Tasks> todayTasks = new ArrayList<>();
                            employeeTasks = new ArrayList<>();
                            pendingTasks = new ArrayList<>();
                            completedTasks = new ArrayList<>();
                            closedTasks = new ArrayList<>();
                            onTask = new ArrayList<>();

                            Date date = new Date();
                            Date adate = new Date();
                            Date edate = new Date();



                            try {
                                date = new SimpleDateFormat("yyyy-MM-dd").parse(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));


                            } catch (Exception e) {
                                e.printStackTrace();
                            }



                            if (list !=null && list.size()!=0) {



                                for (Tasks task:list) {

                                    if(task.getEmployeeId()==employeeId){

                                        String froms = task.getStartDate();
                                        String tos = task.getEndDate();

                                        Date afromDate = null;
                                        Date atoDate = null;

                                        if(froms!=null&&!froms.isEmpty()){

                                            if(froms.contains("T")){

                                                String dojs[] = froms.split("T");

                                                try {
                                                    afromDate = new SimpleDateFormat("yyyy-MM-dd").parse(dojs[0]);
                                                } catch (ParseException e) {
                                                    e.printStackTrace();
                                                }
                                             /*   String parse = new SimpleDateFormat("MMM yyyy").format(afromDate);
                                                fromDate = new SimpleDateFormat("MMM yyyy").parse(parse);*/

                                            }

                                        }

                                        if(tos!=null&&!tos.isEmpty()){

                                            if(tos.contains("T")){

                                                String dojs[] = tos.split("T");

                                                try {
                                                    atoDate = new SimpleDateFormat("yyyy-MM-dd").parse(dojs[0]);
                                                } catch (ParseException e) {
                                                    e.printStackTrace();
                                                }
                                              /*  String parse = new SimpleDateFormat("MMM yyyy").format(atoDate);
                                                toDate = new SimpleDateFormat("MMM yyyy").parse(parse);*/

                                            }

                                        }

                                        if(afromDate!=null&&atoDate!=null){



                                            if(date.getTime() >= afromDate.getTime() && date.getTime() <= atoDate.getTime()){

                                                todayTasks.add(task);

                                                if(task.getStatus().equalsIgnoreCase("Completed")){
                                                    completedTasks.add(task);
                                                    //complete = complete+1;
                                                }else if(task.getStatus().equalsIgnoreCase("Closed")){
                                                    closedTasks.add(task);
                                                    // closed = closed+1;
                                                }else if(task.getStatus().equalsIgnoreCase("On-Going")){
                                                    onTask.add(task);
                                                    onTasks = onTasks+1;
                                                }

                                            }else if(task.getStatus().equalsIgnoreCase("Pending")){
                                                todayTasks.add(task);

                                                pendingTasks.add(task);
                                                pendingTask = pendingTask+1;
                                            }
                                        }

                                        employeeTasks.add(task);
                                        // total = total+1;



                                    }

                                }

                                if(employeeTasks!=null&&employeeTasks.size()!=0){

                                    if(todayTasks!=null&&todayTasks.size()!=0) {
                                        mTodayTaskLayout.setVisibility(View.VISIBLE);
                                        mAdapter = new TaskListAdapter(getContext(), todayTasks);
                                        mTaskList.setAdapter(mAdapter);
                                    }

                                    mOnTask.setText(""+onTasks);
                                    mPending.setText(""+pendingTask);
                                }else{

                                }



                            }else{

                                // Toast.makeText(DailyTargetsForEmployeeActivity.this, "No Tasks given for this employee ", Toast.LENGTH_SHORT).show();

                            }

                        }else {



                            //Toast.makeText(DailyTargetsForEmployeeActivity.this, "Failed due to : "+response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<Tasks>> call, Throwable t) {
                        // Log error here since request failed
                       /* if (progressDialog != null&&progressDialog.isShowing())
                            progressDialog.dismiss();*/
                        Log.e("TAG", t.toString());
                    }
                });
            }


        });
    }

    private void getLoginDetails(final LoginDetails loginDetails){




        new ThreadExecuter().execute(new Runnable() {
            @Override
            public void run() {
                LoginDetailsAPI apiService = Util.getClient().create(LoginDetailsAPI.class);
                Call<ArrayList<LoginDetails>> call = apiService.getLoginByEmployeeId(loginDetails.getEmployeeId());

                call.enqueue(new Callback<ArrayList<LoginDetails>>() {
                    @Override
                    public void onResponse(Call<ArrayList<LoginDetails>> call, Response<ArrayList<LoginDetails>> response) {
                        int statusCode = response.code();
                        if (statusCode == 200 || statusCode == 201 || statusCode == 203 || statusCode == 204) {



                            ArrayList<LoginDetails> list = response.body();

                            if (list !=null && list.size()!=0) {

                                LoginDetails loginDetails = list.get(list.size()-1);

                                monthDate = Calendar.getInstance().get(Calendar.MONTH)+1;
                                int year = Calendar.getInstance().get(Calendar.YEAR);


                                Date adate = null;
                                Date edate = null;

                                String dateValues = new SimpleDateFormat("yyyy-MM-dd").format(new Date());


                                try {

                                    adate = new SimpleDateFormat("yyyy-MM-dd").parse(year+"-"+monthDate+"-01");
                                    edate = new SimpleDateFormat("yyyy-MM-dd").parse(dateValues);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                                if(loginDetails!=null){

                                    String logout = loginDetails.getLogOutTime();
                                    String login = loginDetails.getLoginTime();
                                    String dates = loginDetails.getLoginDate();
                                    String date= null;

                                    if(dates!=null&&!dates.isEmpty()){

                                        if(dates.contains("T")){

                                            String logins[] = dates.split("T");
                                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                            SimpleDateFormat sdfs = new SimpleDateFormat("MMM dd,yyyy");

                                            Date dt = null;
                                            try {
                                                dt = sdf.parse(logins[0]);
                                                date = sdfs.format(dt);
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }

                                        }
                                    }

                                    if(logout!=null&&!logout.isEmpty()&&(login!=null&&!login.isEmpty())){

                                        if(date!=null&&!date.isEmpty()){

                                            mLoggedTime.setText("Last Logout : "+logout);

                                        }else{

                                            mLoggedTime.setText("Last Logout : "+logout);
                                        }


                                        PreferenceHandler.getInstance(getActivity()).setLoginStatus("Logout");

                                    }else if(login!=null&&!login.isEmpty()&&(logout==null||logout.isEmpty())){

                                        if(date!=null&&!date.isEmpty()){

                                            mLoggedTime.setText("Last Logged in : "+login);

                                        }else{

                                            mLoggedTime.setText("Last Logged in : "+login);
                                        }

                                        PreferenceHandler.getInstance(getActivity()).setLoginStatus("Login");
                                        PreferenceHandler.getInstance(getActivity()).setLoginId(loginDetails.getLoginDetailsId());
                                    }

                                }

                                Collections.sort(list, LoginDetails.compareLogin);


                                ArrayList<String> dateList = new ArrayList<>();

                                for(int i=0;i<list.size();i++){

                                    if(list.get(i).getLoginDate().contains("T")){

                                        String date[] = list.get(i).getLoginDate().split("T");
                                        Date dates = null;
                                        try {
                                            dates = new SimpleDateFormat("yyyy-MM-dd").parse(date[0]);
                                            String dateValue = new SimpleDateFormat("MMM dd,yyyy").format(dates);

                                            if(adate!=null&&edate!=null){

                                                if(dates.getTime()>=adate.getTime()&&dates.getTime()<=edate.getTime()){

                                                    dateList.add(date[0]);
                                                }

                                            }




                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }

                                    }

                                }

                                if(dateList!=null&&dateList.size()!=0){

                                    Set<String> s = new LinkedHashSet<String>(dateList);

                                    mWorkedDays.setText(""+s.size());
                                }



                            }else{

                                mLoggedTime.setText("You have not Check-In today.Please put your check-in in Attendance Screen");

                               /* final android.app.AlertDialog.Builder dialogBuilder = new android.app.AlertDialog.Builder(getActivity());
                                LayoutInflater inflater = getLayoutInflater();
                                View views = inflater.inflate(R.layout.check_in_pop,null);
                                Button agree = (Button) views.findViewById(R.id.dialog_ok);


                                dialogBuilder.setView(views);
                                final android.app.AlertDialog dialog = dialogBuilder.create();
                                dialog.setCancelable(true);
                                dialog.show();

                                agree.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                        Intent intent = new Intent(getActivity(), EmployeeNewMainScreen.class);
                                        intent.putExtra("viewpager_position", 2);
                                        intent.putExtra("Condition", false);
                                        startActivity(intent);
                                    }
                                });*/

                            }

                        }else {



                            //Toast.makeText(DailyTargetsForEmployeeActivity.this, "Failed due to : "+response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<LoginDetails>> call, Throwable t) {
                        // Log error here since request failed
                       /* if (progressDialog != null&&progressDialog.isShowing())
                            progressDialog.dismiss();*/
                        Log.e("TAG", t.toString());
                    }
                });
            }


        });
    }

    private void getApprovedLeaveDetails(final int employeeId){

        new ThreadExecuter().execute(new Runnable() {
            @Override
            public void run() {
                LeaveAPI apiService = Util.getClient().create(LeaveAPI.class);
                Call<ArrayList<Leaves>> call = apiService.getLeavesByStatusAndEmployeeId("Approved",employeeId);

                call.enqueue(new Callback<ArrayList<Leaves>>() {
                    @Override
                    public void onResponse(Call<ArrayList<Leaves>> call, Response<ArrayList<Leaves>> response) {
                        int statusCode = response.code();
                        if (statusCode == 200 || statusCode == 201 || statusCode == 203 || statusCode == 204) {


                            int daysInMonth = 0;



                            try{

                                ArrayList<Leaves> list = response.body();
                                ArrayList<Leaves> approvedLeave = new ArrayList<>();


                                Date date = new Date();
                                Date adate = new Date();
                                Date edate = new Date();



                                try {
                                    date = new SimpleDateFormat("yyyy-MM-dd").parse(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

                                    System.out.println("Day countr "+daysInMonth);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }






                                if (list !=null && list.size()!=0) {


                                    /*for (Leaves leaves:list) {

                                        String froms = leaves.getFromDate();
                                        String tos = leaves.getToDate();
                                        Date fromDate = null;
                                        Date toDate = null;
                                        Date afromDate = null;
                                        Date atoDate = null;

                                        if(froms!=null&&!froms.isEmpty()){

                                            if(froms.contains("T")){

                                                String dojs[] = froms.split("T");

                                                afromDate = new SimpleDateFormat("yyyy-MM-dd").parse(dojs[0]);
                                             *//*   String parse = new SimpleDateFormat("MMM yyyy").format(afromDate);
                                                fromDate = new SimpleDateFormat("MMM yyyy").parse(parse);*//*

                                            }

                                        }

                                        if(tos!=null&&!tos.isEmpty()){

                                            if(tos.contains("T")){

                                                String dojs[] = tos.split("T");

                                                atoDate = new SimpleDateFormat("yyyy-MM-dd").parse(dojs[0]);
                                              *//*  String parse = new SimpleDateFormat("MMM yyyy").format(atoDate);
                                                toDate = new SimpleDateFormat("MMM yyyy").parse(parse);*//*

                                            }

                                        }

                                        if(afromDate!=null&&atoDate!=null){



                                            if(date.getTime() >= afromDate.getTime() && date.getTime() <= atoDate.getTime()){

                                                approvedLeave.add(leaves);

                                                checkValue = true;


                                            }
                                        }


                                        if(approvedLeave.size()!=0){
                                            mLeaveDays.setText(""+approvedLeave.size());
                                        }



                                    }
*/
                                    mLeaveDays.setText(""+list.size());

                                }else{



                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }


                        }else {


                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<Leaves>> call, Throwable t) {
                        // Log error here since request failed

                        Log.e("TAG", t.toString());
                    }
                });
            }


        });
    }
   /* private void getRejectedLeaveDetails(final int employeeId){




        new ThreadExecuter().execute(new Runnable() {
            @Override
            public void run() {
                LeaveAPI apiService = Util.getClient().create(LeaveAPI.class);
                Call<ArrayList<Leaves>> call = apiService.getLeavesByStatusAndEmployeeId("Rejected",employeeId);

                call.enqueue(new Callback<ArrayList<Leaves>>() {
                    @Override
                    public void onResponse(Call<ArrayList<Leaves>> call, Response<ArrayList<Leaves>> response) {
                        int statusCode = response.code();
                        if (statusCode == 200 || statusCode == 201 || statusCode == 203 || statusCode == 204) {


                            int daysInMonth = 0;



                            try{

                                ArrayList<Leaves> list = response.body();
                                ArrayList<Leaves> rejectedLeave = new ArrayList<>();


                                Date date = new Date();
                                Date adate = new Date();
                                Date edate = new Date();



                                try {
                                    date = new SimpleDateFormat("yyyy-MM-dd").parse(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

                                    System.out.println("Day countr "+daysInMonth);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }






                                if (list !=null && list.size()!=0) {


                                    for (Leaves leaves:list) {

                                        String froms = leaves.getFromDate();
                                        String tos = leaves.getToDate();
                                        Date fromDate = null;
                                        Date toDate = null;
                                        Date afromDate = null;
                                        Date atoDate = null;

                                        if(froms!=null&&!froms.isEmpty()){

                                            if(froms.contains("T")){

                                                String dojs[] = froms.split("T");

                                                afromDate = new SimpleDateFormat("yyyy-MM-dd").parse(dojs[0]);
                                             *//*   String parse = new SimpleDateFormat("MMM yyyy").format(afromDate);
                                                fromDate = new SimpleDateFormat("MMM yyyy").parse(parse);*//*

                                            }

                                        }

                                        if(tos!=null&&!tos.isEmpty()){

                                            if(tos.contains("T")){

                                                String dojs[] = tos.split("T");

                                                atoDate = new SimpleDateFormat("yyyy-MM-dd").parse(dojs[0]);
                                              *//*  String parse = new SimpleDateFormat("MMM yyyy").format(atoDate);
                                                toDate = new SimpleDateFormat("MMM yyyy").parse(parse);*//*

                                            }

                                        }

                                        if(afromDate!=null&&atoDate!=null){



                                            if(date.getTime() >= afromDate.getTime() && date.getTime() <= atoDate.getTime()){

                                                rejectedLeave.add(leaves);

                                                checkValue = true;

                                            }
                                        }


                                        if(rejectedLeave.size()!=0){
                                            absentEmployee = absentEmployee+1;
                                            mEmployeeAbsent.setText(""+absentEmployee);
                                        }



                                    }


                                }else{



                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }


                        }else {


                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<Leaves>> call, Throwable t) {
                        // Log error here since request failed

                        Log.e("TAG", t.toString());
                    }
                });
            }


        });
    }*/

    public static <T> ArrayList<T> removeDuplicates(ArrayList<T> list)
    {

        // Create a new ArrayList
        ArrayList<T> newList = new ArrayList<T>();

        // Traverse through the first list
        for (T element : list) {

            // If this element is not present in newList
            // then add it
            if (!newList.contains(element)) {

                newList.add(element);
            }
        }

        // return the new list
        return newList;
    }

}

