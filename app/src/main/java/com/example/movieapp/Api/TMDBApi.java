package com.example.movieapp.Api;

import com.example.movieapp.Dto.MovieResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface TMDBApi {
    @GET("movie/popular")
    Call<MovieResponse> getPopularMovies(@Header("Authorization") String authHeader);

    @GET("movie/upcoming")
    Call<MovieResponse> getUpcomingMovies(@Header("Authorization") String authHeader);

    @GET("movie/top_rated")
    Call<MovieResponse> getTopRatedMovies(@Header("Authorization") String authHeader);
} 