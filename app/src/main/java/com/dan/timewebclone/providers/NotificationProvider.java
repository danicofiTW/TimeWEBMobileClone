package com.dan.timewebclone.providers;


import com.dan.timewebclone.models.FCMBody;
import com.dan.timewebclone.models.FCMResponse;
import com.dan.timewebclone.retrofit.IFCMApi;
import com.dan.timewebclone.retrofit.RetrofitClient;

import retrofit2.Call;

public class NotificationProvider
{
    public String url = "https://fcm.googleapis.com";

    public NotificationProvider(){
    }

    public Call<FCMResponse> sendNotification(FCMBody body){
        return RetrofitClient.getClientObject(url).create(IFCMApi.class).send(body);
    }
}
