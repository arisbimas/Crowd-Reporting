package com.aris.crowdreporting;

import com.aris.crowdreporting.Notifications.MyResponse;
import com.aris.crowdreporting.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAstsgwEw:APA91bGNtCGpdpDeQN6OgJ6uoAS-v9TNC18aFtNM35lEqppA9KbCcQylAgfCWONyWmeiE92HOT0WJHEI8-GAFxwhzp6rmMtOB-pKqAjTQBxYGo20RWt2-ixugU-EvhZGFegemPvf6UV6"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
