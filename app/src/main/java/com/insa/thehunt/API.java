package com.insa.thehunt;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface API {
    @FormUrlEncoded
    @POST("updateLocation")
    Call<POST> update(@Query("token") String token, @Query("lat") String latitude, @Field("long") String longitude);

}