package com.smile.colorballs.retrofit_package;

import com.smile.colorballs.model.PlayerList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiInterface {
    //  from ASP.NET Core API
    // [HttpGet("/{pageSize}/{pageNo}/{orderBy}")]
    @GET("/Playerscore/{pageSize}/{pageNo}/{orderBy}")
    Call<PlayerList> getTop10Players(@Path("pageSize") int pageSize, @Path("pageNo") int pageNo, @Path("orderBy") String orderBy);
}
