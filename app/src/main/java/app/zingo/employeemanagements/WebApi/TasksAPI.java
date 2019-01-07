package app.zingo.employeemanagements.WebApi;

import java.util.ArrayList;

import app.zingo.employeemanagements.Model.Leaves;
import app.zingo.employeemanagements.Model.Tasks;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by ZingoHotels Tech on 05-01-2019.
 */

public interface TasksAPI {

    @POST("Tasks")
    Call<Tasks> addTasks(@Body Tasks details);

    @GET("Tasks")
    Call<ArrayList<Tasks>> getTasks();



}