package com.dan.timewebclone.retrofit;


import com.dan.timewebclone.models.FCMBody;
import com.dan.timewebclone.models.FCMResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMApi {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAA9tJcuCI:APA91bG5wVFHcgTEZOF2oQbbUExd-JdVWIVKbobrBbc8_IG2sWv3AVUeuJWygzvQY0q0DHqJjND1MRWzA-cPJXwFTzvBvLspU0g0wxMoGJLQZRZHzJEc3EaqpzqrazUFfGz9HSPK3KUH"
    })
    @POST("fcm/send")
    Call<FCMResponse> send(@Body FCMBody body);

}
