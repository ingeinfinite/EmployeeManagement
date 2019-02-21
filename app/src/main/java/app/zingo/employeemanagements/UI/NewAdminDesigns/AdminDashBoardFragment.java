package app.zingo.employeemanagements.UI.NewAdminDesigns;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.zingo.employeemanagements.Adapter.TaskAdminListAdapter;
import app.zingo.employeemanagements.Adapter.TaskEmployeeListAdapter;
import app.zingo.employeemanagements.Adapter.TaskListAdapter;
import app.zingo.employeemanagements.Custom.MyRegulerText;
import app.zingo.employeemanagements.Model.Departments;
import app.zingo.employeemanagements.Model.Employee;
import app.zingo.employeemanagements.Model.Leaves;
import app.zingo.employeemanagements.Model.LoginDetails;
import app.zingo.employeemanagements.Model.LoginDetailsNotificationManagers;
import app.zingo.employeemanagements.Model.TaskAdminData;
import app.zingo.employeemanagements.Model.Tasks;
import app.zingo.employeemanagements.R;
import app.zingo.employeemanagements.UI.Admin.CreatePaySlip;
import app.zingo.employeemanagements.UI.Company.OrganizationDetailScree;
import app.zingo.employeemanagements.UI.Employee.CreateEmployeeScreen;
import app.zingo.employeemanagements.UI.Employee.EmployeeListScreen;
import app.zingo.employeemanagements.UI.Landing.InternalServerErrorScreen;
import app.zingo.employeemanagements.Utils.PreferenceHandler;
import app.zingo.employeemanagements.Utils.ThreadExecuter;
import app.zingo.employeemanagements.Utils.Util;
import app.zingo.employeemanagements.WebApi.DepartmentApi;
import app.zingo.employeemanagements.WebApi.EmployeeApi;
import app.zingo.employeemanagements.WebApi.LeaveAPI;
import app.zingo.employeemanagements.WebApi.LoginDetailsAPI;
import app.zingo.employeemanagements.WebApi.TasksAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class AdminDashBoardFragment extends Fragment {

    final String TAG = "Employer Dash";
    View layout;
    RecyclerView mTaskList;
    LinearLayout mTaskLayout,mPendingLayout,mOnTaskLay,mDepartmentLay,mEmployeeLay,mPresenEmploLay,mAbEmLayy,mLeaveLay;
    private TaskAdminListAdapter mAdapter;
    MyRegulerText mDeptCount,mEmployeeCount,mOnTask,mPending,mEmployeePresent,mEmployeeAbsent,
                    mLeaveEmployee;//mUnmarkedEmployee

    static Context mContext;

    private static List<Employee> mEmployeeList = new ArrayList();
    private static List<Employee> searchList = new ArrayList();
    private Map<String, String> countMap = new HashMap();

    int deptCount=0,employee=0,onTasks=0,pendingTask=0,presentEmployee=0,
                absentEmployee=0,leaveEmployee=0,unMarkedEmployee=0;

    boolean checkValue = false;

    ArrayList<TaskAdminData> employeeTasks;
    ArrayList<TaskAdminData> pendingTasks ;
    ArrayList<TaskAdminData> completedTasks ;
    ArrayList<TaskAdminData> closedTasks ;
    ArrayList<TaskAdminData> onTask ;
    ArrayList<TaskAdminData> todayTasks;

    ArrayList<Employee> presentEmployees;
    ArrayList<Employee> absentEmployees;
    ArrayList<Employee> allEmployees;
    ArrayList<Employee> leaveEmployees;

    ArrayList<Integer> preEmpId;
    ArrayList<Integer> leaEmpId;
    ArrayList<Integer> absEmpId;
    ArrayList<Integer> all;
    Handler h;
    Runnable runnable;
    int delay = 5*1000;

    public AdminDashBoardFragment() {
        // Required empty public constructor
    }

    public static AdminDashBoardFragment getInstance() {
        return new AdminDashBoardFragment();
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
            this.layout = layoutInflater.inflate(R.layout.fragment_admin_dash_board, viewGroup, false);

            mContext = getContext();

            mTaskList = (RecyclerView) layout.findViewById(R.id.task_list_dash);
            mTaskLayout = (LinearLayout) layout.findViewById(R.id.today_task_list);
            mPendingLayout = (LinearLayout) layout.findViewById(R.id.pending_task_layout);
            mOnTaskLay = (LinearLayout) layout.findViewById(R.id.on_task_count_layout);
            mDepartmentLay = (LinearLayout) layout.findViewById(R.id.dept_count_layout);
            mEmployeeLay = (LinearLayout) layout.findViewById(R.id.employee_count_layout);
            mPresenEmploLay = (LinearLayout) layout.findViewById(R.id.present_count_layout);
            mAbEmLayy = (LinearLayout) layout.findViewById(R.id.absent_count_layout);
            mLeaveLay = (LinearLayout) layout.findViewById(R.id.leave_count_layout);
            mTaskLayout.setVisibility(View.GONE);
            mDeptCount = (MyRegulerText)layout.findViewById(R.id.dept_count_text);
            mEmployeeCount = (MyRegulerText)layout.findViewById(R.id.employee_count_text);
            mOnTask = (MyRegulerText)layout.findViewById(R.id.on_task_count_text);
            mPending = (MyRegulerText)layout.findViewById(R.id.pending_task_text);
            mEmployeePresent = (MyRegulerText)layout.findViewById(R.id.today_employee_present);
            mEmployeeAbsent = (MyRegulerText)layout.findViewById(R.id.absent_employee);
            mLeaveEmployee = (MyRegulerText)layout.findViewById(R.id.leave_employees);
           // mUnmarkedEmployee = (MyRegulerText)layout.findViewById(R.id.unmarked_employees);


            leaEmpId = new ArrayList<>();
            preEmpId = new ArrayList<>();
            absEmpId = new ArrayList<>();
            all = new ArrayList<>();


            if(PreferenceHandler.getInstance(getActivity()).getCompanyId()!=0){
                getDepartment();
                getEmployees();
            }else{
                Intent error = new Intent(getActivity(),InternalServerErrorScreen.class);
                startActivity(error);
            }


         //   showalertbox();

            if(checkValue){
                all.addAll(leaEmpId);
                all.addAll(preEmpId);
                all.addAll(absEmpId);

                all = removeDuplicates(all);

                allEmployees.removeAll(presentEmployees);
                allEmployees.removeAll(leaveEmployees);
                allEmployees = removeDuplicates(allEmployees);
                absentEmployees = allEmployees;

               mEmployeeAbsent.setText(""+(all.size()-1));
            }else{
                h = new Handler();
                //1 second=1000 milisecond, 15*1000=15seconds


                h.postDelayed( runnable = new Runnable() {
                    public void run() {
                        //do something
                        if(checkValue){
                            all.addAll(leaEmpId);
                            all.addAll(preEmpId);
                            all.addAll(absEmpId);

                            allEmployees.removeAll(presentEmployees);
                            allEmployees.removeAll(leaveEmployees);
                            allEmployees = removeDuplicates(allEmployees);
                            absentEmployees = allEmployees;

                            all = removeDuplicates(all);
                            mEmployeeAbsent.setText(""+(employee-all.size()));
                        }
                        h.postDelayed(runnable, delay);
                    }
                }, delay);
            }

            mPendingLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(pendingTasks!=null&&pendingTasks.size()!=0){
                        Intent pending = new Intent(getActivity(),PendingTasks.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("PendingTasks",pendingTasks);
                        bundle.putString("Title","Pending Tasks");
                        pending.putExtras(bundle);
                        startActivity(pending);
                    }

                }
            });

            mOnTaskLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(onTask!=null&&onTask.size()!=0){
                        Intent pending = new Intent(getActivity(),PendingTasks.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("PendingTasks",onTask);
                        bundle.putString("Title","On Tasks");
                        pending.putExtras(bundle);
                        startActivity(pending);
                    }

                }
            });

            mDepartmentLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent organization = new Intent(getActivity(), DepartmentLilstScreen.class);
                    getContext().startActivity(organization);

                }
            });

            mEmployeeLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent employee = new Intent(getContext(), EmployeeUpdateListScreen.class);
                    getContext().startActivity(employee);

                }
            });

            mPresenEmploLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(presentEmployees!=null&&presentEmployees.size()!=0){

                        Intent pending = new Intent(getActivity(),PresentEmployeeListScreen.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("Employees",presentEmployees);
                        pending.putExtras(bundle);
                        startActivity(pending);

                    }else {
                        Toast.makeText(getActivity(), "No Employees", Toast.LENGTH_SHORT).show();
                    }


                }
            });

            mAbEmLayy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {




                    if(absentEmployees!=null&&absentEmployees.size()!=0){

                        Intent pending = new Intent(getActivity(),PresentEmployeeListScreen.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("Employees",absentEmployees);
                        pending.putExtras(bundle);
                        startActivity(pending);

                    }else {
                        Toast.makeText(getActivity(), "No Employees", Toast.LENGTH_SHORT).show();
                    }


                }
            });

            mLeaveLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(leaveEmployees!=null&&leaveEmployees.size()!=0){

                        Intent pending = new Intent(getActivity(),PresentEmployeeListScreen.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("Employees",leaveEmployees);
                        pending.putExtras(bundle);
                        startActivity(pending);

                    }else {
                        Toast.makeText(getActivity(), "No Employees", Toast.LENGTH_SHORT).show();
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


        /*final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Loading Employees");
        progressDialog.setCancelable(false);
        progressDialog.show();*/

        new ThreadExecuter().execute(new Runnable() {
            @Override
            public void run() {
                EmployeeApi apiService = Util.getClient().create(EmployeeApi.class);
                Call<ArrayList<Employee>> call = apiService.getEmployeesByOrgId(PreferenceHandler.getInstance(getActivity()).getCompanyId());

                call.enqueue(new Callback<ArrayList<Employee>>() {
                    @Override
                    public void onResponse(Call<ArrayList<Employee>> call, Response<ArrayList<Employee>> response) {
                        int statusCode = response.code();
                        if (statusCode == 200 || statusCode == 201 || statusCode == 203 || statusCode == 204) {


                           /* if (progressDialog!=null)
                                progressDialog.dismiss();*/
                            ArrayList<Employee> list = response.body();

                            employeeTasks = new ArrayList<>();
                            todayTasks = new ArrayList<>();
                            pendingTasks = new ArrayList<>();
                            completedTasks = new ArrayList<>();
                            closedTasks = new ArrayList<>();
                            onTask = new ArrayList<>();
                            presentEmployees = new ArrayList<>();
                            absentEmployees = new ArrayList<>();
                            allEmployees = new ArrayList<>();
                            leaveEmployees = new ArrayList<>();

                            if (list !=null && list.size()!=0) {

                                ArrayList<Employee> employees = new ArrayList<>();
                                for(int i=0;i<list.size();i++){

                                    if(list.get(i).getEmployeeId()!=PreferenceHandler.getInstance(getActivity()).getUserId()){

                                        allEmployees.add(list.get(i));
                                        employees.add(list.get(i));

                                        final Calendar calendar = Calendar.getInstance();
                                        Date date2 = calendar.getTime();
                                        getTasks(list.get(i),new SimpleDateFormat("yyyy-MM-dd").format(date2));
                                        getApprovedLeaveDetails(list.get(i).getEmployeeId(),list.get(i));
                                        getRejectedLeaveDetails(list.get(i).getEmployeeId());

                                        LoginDetails loginDetails = new LoginDetails();
                                        loginDetails.setEmployeeId(list.get(i).getEmployeeId());
                                        loginDetails.setLoginDate(new SimpleDateFormat("MM/dd/yyyy").format(new Date()));
                                        getLoginDetails(loginDetails,list.get(i));

                                    }
                                }

                                if(employees!=null&&employees.size()!=0){

                                    employee = employees.size();

                                    mEmployeeCount.setText(""+employees.size());
                                    mOnTask.setText(""+onTasks);
                                    mPending.setText(""+pendingTask);


                                }else{


                                }


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
                      /*  if (progressDialog!=null)
                            progressDialog.dismiss();*/

                        Log.e("TAG", t.toString());
                    }
                });
            }


        });
    }
    private void getDepartment(){


        /*final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Loading Employees");
        progressDialog.setCancelable(false);
        progressDialog.show();*/

        new ThreadExecuter().execute(new Runnable() {
            @Override
            public void run() {
                DepartmentApi apiService = Util.getClient().create(DepartmentApi.class);
                Call<ArrayList<Departments>> call = apiService.getDepartmentByOrganization(PreferenceHandler.getInstance(getActivity()).getCompanyId());

                call.enqueue(new Callback<ArrayList<Departments>>() {
                    @Override
                    public void onResponse(Call<ArrayList<Departments>> call, Response<ArrayList<Departments>> response) {
                        int statusCode = response.code();
                        if (statusCode == 200 || statusCode == 201 || statusCode == 203 || statusCode == 204) {


                           /* if (progressDialog!=null)
                                progressDialog.dismiss();*/
                            ArrayList<Departments> list = response.body();
                            ArrayList<Departments> deptList = new ArrayList<>();


                            if (list !=null && list.size()!=0) {

                                //}
                                for (Departments dept:list) {

                                    if(!dept.getDepartmentName().equalsIgnoreCase("Founders")){
                                        deptList.add(dept);
                                    }

                                }

                                if(deptList.size()==0){
                                    departmentAlert();
                                }else{
                                        mDeptCount.setText(""+deptList.size());
                                }

                            }else{
                                departmentAlert();
                            }

                        }else {


                            Toast.makeText(getActivity(), "Failed due to : "+response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<Departments>> call, Throwable t) {
                        // Log error here since request failed
                      /*  if (progressDialog!=null)
                            progressDialog.dismiss();*/

                        Log.e("TAG", t.toString());
                    }
                });
            }


        });
    }

    private void departmentAlert(){

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View views = inflater.inflate(R.layout.alert_department_create, null);

        builder.setView(views);
         builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {

                        return true; // Consumed
                    }
                    else {
                        return false; // Not consumed
                    }
                }
            });

        final Button mSave = (Button) views.findViewById(R.id.save);
        final EditText desc = (EditText) views.findViewById(R.id.department_description);
        final TextInputEditText mName = (TextInputEditText) views.findViewById(R.id.department_name);


        final android.support.v7.app.AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);

        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String name = mName.getText().toString();
                String descrp = desc.getText().toString();

                if(name.isEmpty()){

                    Toast.makeText(getContext(), "Please enter Department Name", Toast.LENGTH_SHORT).show();

                }else if (descrp.isEmpty()){

                    Toast.makeText(getContext(), "Please enter Department Description", Toast.LENGTH_SHORT).show();
                }else{

                    Departments departments = new Departments();
                    departments.setDepartmentName(name);
                    departments.setDepartmentDescription(descrp);
                    departments.setOrganizationId(PreferenceHandler.getInstance(getContext()).getCompanyId());

                    try {
                        addDepartments(departments,dialog);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


            }
        });

    }


    public void addDepartments(final Departments departments,final AlertDialog dialogs) throws Exception{


        final ProgressDialog dialog = new ProgressDialog(getContext());
        dialog.setMessage("Saving Details..");
        dialog.setCancelable(false);
        dialog.show();

        DepartmentApi apiService = Util.getClient().create(DepartmentApi.class);

        Call<Departments> call = apiService.addDepartments(departments);

        call.enqueue(new Callback<Departments>() {
            @Override
            public void onResponse(Call<Departments> call, Response<Departments> response) {
//                List<RouteDTO.Routes> list = new ArrayList<RouteDTO.Routes>();
                try
                {
                    if(dialog != null && dialog.isShowing())
                    {
                        dialog.dismiss();
                    }

                    int statusCode = response.code();
                    if (statusCode == 200 || statusCode == 201) {

                        Departments s = response.body();

                        if(s!=null){

                            Toast.makeText(getContext(), "Department Creted Successfully ", Toast.LENGTH_SHORT).show();

                            dialogs.dismiss();
                            showalertbox();



                        }




                    }else {
                        Toast.makeText(getActivity(), "Failed Due to "+response.message(), Toast.LENGTH_SHORT).show();
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
            public void onFailure(Call<Departments> call, Throwable t) {

                if(dialog != null && dialog.isShowing())
                {
                    dialog.dismiss();
                }
                Toast.makeText(getActivity(), "Failed Due to "+t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("TAG", t.toString());
            }
        });



    }

    private void getTasks(final Employee employee,final String dateValue){

        final int employeeId = employee.getEmployeeId();


        new ThreadExecuter().execute(new Runnable() {
            @Override
            public void run() {
                TasksAPI apiService = Util.getClient().create(TasksAPI.class);
                Call<ArrayList<Tasks>> call = apiService.getTasksByEmployeeId(employeeId);

                call.enqueue(new Callback<ArrayList<Tasks>>() {
                    @Override
                    public void onResponse(Call<ArrayList<Tasks>> call, Response<ArrayList<Tasks>> response) {
                        int statusCode = response.code();
                        if (statusCode == 200 || statusCode == 201 || statusCode == 203 || statusCode == 204) {



                            ArrayList<Tasks> list = response.body();



                            Date date = new Date();
                            Date adate = new Date();
                            Date edate = new Date();

                            try {
                                date = new SimpleDateFormat("yyyy-MM-dd").parse(dateValue);


                            } catch (Exception e) {
                                e.printStackTrace();
                            }



                            if (list !=null && list.size()!=0) {



                                for (Tasks task:list) {


                                    TaskAdminData taskAdminData = new TaskAdminData();
                                    taskAdminData.setEmployee(employee);
                                    taskAdminData.setTasks(task);


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

                                                TaskAdminData taskAdminDatas = new TaskAdminData();
                                                taskAdminDatas.setEmployee(employee);
                                                taskAdminDatas.setTasks(task);

                                                todayTasks.add(taskAdminDatas);

                                            }
                                        }

                                    TaskAdminData taskAdminDatas = new TaskAdminData();
                                    taskAdminDatas.setEmployee(employee);
                                    taskAdminDatas.setTasks(task);
                                    employeeTasks.add(taskAdminDatas);
                                       // total = total+1;

                                        if(task.getStatus().equalsIgnoreCase("Completed")){
                                            completedTasks.add(taskAdminDatas);
                                            //complete = complete+1;
                                        }else if(task.getStatus().equalsIgnoreCase("Pending")){
                                            pendingTasks.add(taskAdminDatas);
                                            pendingTask = pendingTask+1;
                                        }else if(task.getStatus().equalsIgnoreCase("Closed")){
                                            closedTasks.add(taskAdminDatas);
                                           // closed = closed+1;
                                        }else if(task.getStatus().equalsIgnoreCase("On-Going")){
                                            onTask.add(taskAdminDatas);
                                            onTasks = onTasks+1;
                                        }



                                }

                                if(employeeTasks!=null&&employeeTasks.size()!=0){

                                    if(todayTasks!=null&&todayTasks.size()!=0){
                                        mTaskLayout.setVisibility(View.VISIBLE);

                                        mTaskList.removeAllViews();

                                        mAdapter = new TaskAdminListAdapter(getContext(),todayTasks);
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
                       /* if (progressDialog!=null)
                            progressDialog.dismiss();*/
                        Log.e("TAG", t.toString());
                    }
                });
            }


        });
    }

    private void getLoginDetails(final LoginDetails loginDetails,final Employee employee){




        new ThreadExecuter().execute(new Runnable() {
            @Override
            public void run() {
                LoginDetailsAPI apiService = Util.getClient().create(LoginDetailsAPI.class);
                Call<ArrayList<LoginDetails>> call = apiService.getLoginByEmployeeIdAndDate(loginDetails);

                call.enqueue(new Callback<ArrayList<LoginDetails>>() {
                    @Override
                    public void onResponse(Call<ArrayList<LoginDetails>> call, Response<ArrayList<LoginDetails>> response) {
                        int statusCode = response.code();
                        if (statusCode == 200 || statusCode == 201 || statusCode == 203 || statusCode == 204) {



                            ArrayList<LoginDetails> list = response.body();

                            if (list !=null && list.size()!=0) {




                                if(list.get(list.size()-1).getLogOutTime()==null||list.get(list.size()-1).getLogOutTime().isEmpty()){

                                    preEmpId.add(loginDetails.getEmployeeId());
                                    presentEmployee = presentEmployee+1;
                                    mEmployeePresent.setText(""+presentEmployee);
                                    presentEmployees.add(employee);
                                    checkValue = true;
                                }




                            }else{

                                // Toast.makeText(DailyTargetsForEmployeeActivity.this, "No Tasks given for this employee ", Toast.LENGTH_SHORT).show();

                            }

                        }else {



                            //Toast.makeText(DailyTargetsForEmployeeActivity.this, "Failed due to : "+response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<LoginDetails>> call, Throwable t) {
                        // Log error here since request failed
                       /* if (progressDialog!=null)
                            progressDialog.dismiss();*/
                        Log.e("TAG", t.toString());
                    }
                });
            }


        });
    }

    private void getApprovedLeaveDetails(final int employeeId,final Employee employee){

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
                                             /*   String parse = new SimpleDateFormat("MMM yyyy").format(afromDate);
                                                fromDate = new SimpleDateFormat("MMM yyyy").parse(parse);*/

                                            }

                                        }

                                        if(tos!=null&&!tos.isEmpty()){

                                            if(tos.contains("T")){

                                                String dojs[] = tos.split("T");

                                                atoDate = new SimpleDateFormat("yyyy-MM-dd").parse(dojs[0]);
                                              /*  String parse = new SimpleDateFormat("MMM yyyy").format(atoDate);
                                                toDate = new SimpleDateFormat("MMM yyyy").parse(parse);*/

                                            }

                                        }

                                        if(afromDate!=null&&atoDate!=null){



                                            if(date.getTime() >= afromDate.getTime() && date.getTime() <= atoDate.getTime()){

                                                approvedLeave.add(leaves);
                                                leaveEmployees.add(employee);
                                                leaEmpId.add(employeeId);
                                                checkValue = true;


                                            }
                                        }


                                        if(approvedLeave.size()!=0){
                                            leaveEmployee = leaveEmployee+1;
                                            mLeaveEmployee.setText(""+leaveEmployee);
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
    }
    private void getRejectedLeaveDetails(final int employeeId){




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
                                             /*   String parse = new SimpleDateFormat("MMM yyyy").format(afromDate);
                                                fromDate = new SimpleDateFormat("MMM yyyy").parse(parse);*/

                                            }

                                        }

                                        if(tos!=null&&!tos.isEmpty()){

                                            if(tos.contains("T")){

                                                String dojs[] = tos.split("T");

                                                atoDate = new SimpleDateFormat("yyyy-MM-dd").parse(dojs[0]);
                                              /*  String parse = new SimpleDateFormat("MMM yyyy").format(atoDate);
                                                toDate = new SimpleDateFormat("MMM yyyy").parse(parse);*/

                                            }

                                        }

                                        if(afromDate!=null&&atoDate!=null){



                                            if(date.getTime() >= afromDate.getTime() && date.getTime() <= atoDate.getTime()){

                                                rejectedLeave.add(leaves);
                                                absEmpId.add(employeeId);
                                                checkValue = true;

                                            }
                                        }


                                        if(rejectedLeave.size()!=0){
                                            absentEmployee = absentEmployee+1;
                                            //mEmployeeAbsent.setText(""+absentEmployee);
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
    }

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

    private void showalertbox() throws Exception{



        final android.app.AlertDialog.Builder dialogBuilder = new android.app.AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.depart_add_alert_layout,null);
        TextView dontCancelBtn = (TextView)view.findViewById(R.id.no_btn);
        TextView cancelBtn = (TextView)view.findViewById(R.id.exit_app_btn);
        TextView ask = (TextView)view.findViewById(R.id.ask);

        dialogBuilder.setView(view);
        final android.app.AlertDialog dialog = dialogBuilder.create();
        dialog.setCancelable(true);
        dialog.show();
        dontCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try
                {
                    if(dialog != null)
                    {
                        dialog.dismiss();
                    }

                    Intent employeeList = new Intent(getContext(), DepartmentLilstScreen.class);
                    getActivity().startActivity(employeeList);


                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try
                {
                    if(dialog != null)
                    {
                        dialog.dismiss();
                    }

                    Intent employee =new Intent(getActivity(),CreateEmployeeScreen.class);
                    startActivity(employee);


                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        });
    }

}
