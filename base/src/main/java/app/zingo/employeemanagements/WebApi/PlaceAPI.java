package app.zingo.employeemanagements.WebApi;

import com.google.android.gms.location.places.Place;

import app.zingo.employeemanagements.Model.Places;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

/**
 * Created by ZingoHotels Tech on 26-09-2018.
 */

public interface PlaceAPI {

    @GET("https://maps.googleapis.com/maps/api/geocode/json")//?lat={lati}&lon={longi}&units=metric"
    Call<Places> getPlaces(@Query("latlng") String lati, @Query("key") String key);
}
