package com.dan.timewebclone.retrofit;

import com.dan.timewebclone.channel.NotificationMessage;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface HMSApi {

    @Headers("Content-Type:application/json; charset=UTF-8")
    @POST("v1/APP_ID/messages:send")
    Call<Void> createNotification(
            @Header("Authorization") String authorization,
            @Body NotificationMessage notificationBody

    );

}
