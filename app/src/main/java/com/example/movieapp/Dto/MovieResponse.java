package com.example.movieapp.Dto;

import com.example.movieapp.Domains.Movie;

import java.util.List;

public class MovieResponse {
    private List<Movie> results;

    public List<Movie> getResults() {
        return results;
    }

    public void setResults(List<Movie> results) {
        this.results = results;
    }
} 