package com.example.movieapp.Model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MovieDetail {
    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("overview")
    private String overview;

    @SerializedName("poster_path")
    private String posterPath;

    @SerializedName("backdrop_path")
    private String backdropPath;

    @SerializedName("release_date")
    private String releaseDate;

    @SerializedName("runtime")
    private int runtime;

    @SerializedName("vote_average")
    private double voteAverage;

    @SerializedName("genres")
    private List<Genre> genres;

    @SerializedName("credits")
    private Credits credits;

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getOverview() {
        return overview;
    }

    public String getPosterPath() {
        return "https://image.tmdb.org/t/p/w500" + posterPath;
    }

    public String getBackdropPath() {
        return "https://image.tmdb.org/t/p/original" + backdropPath;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public int getRuntime() {
        return runtime;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public Credits getCredits() {
        return credits;
    }

    public static class Genre {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    public static class Credits {
        @SerializedName("cast")
        private List<Cast> cast;

        public List<Cast> getCast() {
            return cast;
        }
    }

    public static class Cast {
        @SerializedName("name")
        private String name;

        @SerializedName("profile_path")
        private String profilePath;

        public String getName() {
            return name;
        }

        public String getProfilePath() {
            return profilePath != null ? "https://image.tmdb.org/t/p/w500" + profilePath : null;
        }
    }
} 