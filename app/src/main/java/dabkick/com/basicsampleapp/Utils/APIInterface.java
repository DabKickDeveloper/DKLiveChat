package dabkick.com.basicsampleapp.Utils;

import com.dabkick.engine.DKServer.DeveloperData;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface APIInterface {

    @GET("chatSample/getSampleCredentials.php?")
    Call<DeveloperData> getDevIdandKey(@Query("sampleAppUserId") String appUserId);
}
