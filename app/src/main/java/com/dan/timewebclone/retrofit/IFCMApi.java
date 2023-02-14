package com.dan.timewebclone.retrofit;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMApi {


    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAANMZWwys:APA91bFD92zXhCuW7YkpcgPr7_LPOBkGNnL9K0PwWvNY3gmIEscj55VHbRYvk22-_gClBOVtI8KkJXk3akzZLbvoN5SZH7cW40W1jrOIA9ee0QoM39ztUG_pHbevJVnmvnrQCeDcicqZ"
    })
    @POST("fcm/send")
    Call<FCMResponse> send(@Body FCMBody body);

}
