package com.example.movieapp.Api;

import com.example.movieapp.Dto.MovieResponse;
import com.example.movieapp.Domains.MovieDetail;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TMDBApi {
    @GET("movie/popular")
    Call<MovieResponse> getPopularMovies(
        @Query("api_key") String apiKey,
        @Query("append_to_response") String appendToResponse
    );

    @GET("movie/upcoming")
    Call<MovieResponse> getUpcomingMovies(
        @Query("api_key") String apiKey,
        @Query("append_to_response") String appendToResponse
    );

    @GET("movie/top_rated")
    Call<MovieResponse> getTopRatedMovies(
        @Query("api_key") String apiKey,
        @Query("append_to_response") String appendToResponse
    );

    @GET("movie/{movie_id}")
    Call<MovieDetail> getMovieDetails(
        @Path("movie_id") int movieId,
        @Query("api_key") String apiKey,
        @Query("append_to_response") String appendToResponse
    );

    @GET("search/movie")
    Call<MovieResponse> searchMovies(
            @Query("api_key") String apiKey,
            @Query("query") String searchQuery,
            @Query("append_to_response") String appendToResponse
    );
} 