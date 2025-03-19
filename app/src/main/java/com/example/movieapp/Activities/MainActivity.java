package com.example.movieapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.example.movieapp.Adapters.FilmListAdapter;
import com.example.movieapp.Adapters.SlidersAdapter;
import com.example.movieapp.Domains.Cast;
import com.example.movieapp.Domains.Movie;
import com.example.movieapp.Dto.MovieResponse;
import com.example.movieapp.Api.TMDBApi;
import com.example.movieapp.Domains.Film;
import com.example.movieapp.Domains.SliderItems;
import com.example.movieapp.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private static final String BASE_URL = "https://api.themoviedb.org/3/";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI3YTkwOTBkNzc5YzcyODFiZTg0MTcxOWExNzlmOTAzZSIsIm5iZiI6MTc0MjM4NjA2OS4yMjU5OTk4LCJzdWIiOiI2N2RhYjM5NTdiYTdkYTcxNjNhMWU5OTUiLCJzY29wZXMiOlsiYXBpX3JlYWQiXSwidmVyc2lvbiI6MX0.gSwO3mJcY0WjsD7RCRrVQ4Zij2Rw4NdzCkTRHDikMls"; // Replace with your actual API key
    
    private Handler sliderHandler = new Handler();
    private Runnable sliderRunnable;
    private ViewPager2 viewPager2;
    private ProgressBar progressBarBanner, progressBarTop, progressBarUpcoming;
    private RecyclerView recyclerViewTopMovies, recyclerViewUpcoming;
    private ImageView profileImageView;
    private TMDBApi tmdbApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupRetrofit();
        initializeViews();
        setupProfileImage();
        setupWindowFlags();
        setupSliderRunnable();

        initBanner();
        initTopMoving();
        initUpcoming();
    }

    private void setupRetrofit() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(chain -> {
                    okhttp3.Request request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " + API_KEY)
                            .build();
                    return chain.proceed(request);
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        tmdbApi = retrofit.create(TMDBApi.class);
    }

    private void initializeViews() {
        viewPager2 = findViewById(R.id.viewPager2);
        progressBarBanner = findViewById(R.id.progressBarBanner);
        progressBarTop = findViewById(R.id.progressBarTop);
        progressBarUpcoming = findViewById(R.id.progressBarUpcoming);
        recyclerViewTopMovies = findViewById(R.id.recyclerViewTopMovies);
        recyclerViewUpcoming = findViewById(R.id.recyclerViewUpcoming);
        profileImageView = findViewById(R.id.imageView2);
    }

    private void setupProfileImage() {
        profileImageView.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void setupWindowFlags() {
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    private void setupSliderRunnable() {
        sliderRunnable = () -> viewPager2.setCurrentItem(viewPager2.getCurrentItem() + 1);
    }

    private void initUpcoming() {
        progressBarUpcoming.setVisibility(View.VISIBLE);
        tmdbApi.getUpcomingMovies("Bearer " + API_KEY).enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> movies = response.body().getResults();
                    if (!movies.isEmpty()) {
                        recyclerViewUpcoming.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
                        recyclerViewUpcoming.setAdapter(new FilmListAdapter(convertToFilmList(movies)));
                    } else {
                        System.out.println("Yaklaşan filmler listesi boş!");
                    }
                } else {
                    System.out.println("API yanıtı başarısız: " + response.code());
                    try {
                        System.out.println("Hata mesajı: " + response.errorBody().string());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                progressBarUpcoming.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                System.out.println("API çağrısı başarısız: " + t.getMessage());
                t.printStackTrace();
                progressBarUpcoming.setVisibility(View.GONE);
            }
        });
    }

    private void initTopMoving() {
        progressBarTop.setVisibility(View.VISIBLE);
        tmdbApi.getTopRatedMovies("Bearer " + API_KEY).enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> movies = response.body().getResults();
                    if (!movies.isEmpty()) {
                        recyclerViewTopMovies.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
                        recyclerViewTopMovies.setAdapter(new FilmListAdapter(convertToFilmList(movies)));
                    } else {
                        System.out.println("En iyi filmler listesi boş!");
                    }
                } else {
                    System.out.println("API yanıtı başarısız: " + response.code());
                    try {
                        System.out.println("Hata mesajı: " + response.errorBody().string());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                progressBarTop.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                System.out.println("API çağrısı başarısız: " + t.getMessage());
                t.printStackTrace();
                progressBarTop.setVisibility(View.GONE);
            }
        });
    }

    private void initBanner() {
        progressBarBanner.setVisibility(View.VISIBLE);
        tmdbApi.getPopularMovies("Bearer " + API_KEY).enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> movies = response.body().getResults();
                    if (!movies.isEmpty()) {
                        banners(convertToSliderItems(movies));
                    } else {
                        System.out.println("Popüler filmler listesi boş!");
                    }
                } else {
                    System.out.println("API yanıtı başarısız: " + response.code());
                    try {
                        System.out.println("Hata mesajı: " + response.errorBody().string());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                progressBarBanner.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                System.out.println("API çağrısı başarısız: " + t.getMessage());
                t.printStackTrace();
                progressBarBanner.setVisibility(View.GONE);
            }
        });
    }

    private ArrayList<Film> convertToFilmList(List<Movie> movies) {
        ArrayList<Film> films = new ArrayList<>();
        for (Movie movie : movies) {
            Film film = new Film();
            film.setTitle(movie.getTitle());
            film.setPicUrl(movie.getPosterPath());
            film.setDescription(movie.getOverview());
            film.setImdb((int) movie.getVoteAverage());
            film.setYear(2024); // Varsayılan yıl
            film.setTime("120 dk"); // Varsayılan süre
            film.setTrailer("https://www.youtube.com/watch?v=dQw4w9WgXcQ"); // Varsayılan trailer
            
            // Varsayılan genre listesi
            ArrayList<String> genres = new ArrayList<>();
            genres.add("Aksiyon");
            genres.add("Macera");
            film.setGenre(genres);
            
            // Varsayılan cast listesi
            ArrayList<Cast> casts = new ArrayList<>();
            Cast cast = new Cast();
            cast.setActor("Oyuncu Adı");
            cast.setPicUrl("https://image.tmdb.org/t/p/w500/actor.jpg");
            casts.add(cast);
            film.setCasts(casts);
            
            films.add(film);
        }
        return films;
    }

    private ArrayList<SliderItems> convertToSliderItems(List<Movie> movies) {
        ArrayList<SliderItems> items = new ArrayList<>();
        for (Movie movie : movies) {
            SliderItems item = new SliderItems();
            item.setImageUrl(movie.getBackdropPath());
            item.setTitle(movie.getTitle());
            item.setGenre("Film");
            item.setAge("13+");
            item.setYear("2024");
            item.setTime("120 dk");
            items.add(item);
        }
        return items;
    }

    private void banners(ArrayList<SliderItems> items) {
        viewPager2.setAdapter(new SlidersAdapter(items, viewPager2));
        viewPager2.setClipToPadding(false);
        viewPager2.setClipChildren(false);
        viewPager2.setOffscreenPageLimit(3);
        viewPager2.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        compositePageTransformer.addTransformer((page, position) -> {
            float r = 1 - Math.abs(position);
            page.setScaleY(0.85f + r * 0.15f);
        });

        viewPager2.setPageTransformer(compositePageTransformer);
        viewPager2.setCurrentItem(1);
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunnable);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunnable, 2000);
    }
}
