package com.example.movieapp.Activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.example.movieapp.Domains.MovieDetail;
import com.example.movieapp.Dto.MovieResponse;
import com.example.movieapp.Api.TMDBApi;
import com.example.movieapp.Domains.Film;
import com.example.movieapp.Domains.SliderItems;
import com.example.movieapp.R;
import com.example.movieapp.Utils.ThemeHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import android.content.SharedPreferences;
import android.util.DisplayMetrics;

public class MainActivity extends AppCompatActivity {
    private static final String BASE_URL = "https://api.themoviedb.org/3/";
    private static final String API_KEY = "7a9090d779c7281be841719a179f903e";
    
    private Handler sliderHandler = new Handler();
    private Runnable sliderRunnable;
    private ViewPager2 viewPager2;
    private ProgressBar progressBarBanner, progressBarTop, progressBarUpcoming;
    private RecyclerView recyclerViewTopMovies, recyclerViewUpcoming;
    private ImageView profileImageView;
    private TextView userNameText, userEmailText;
    private TMDBApi tmdbApi;
    private EditText searchEditText;
    private RecyclerView searchResultsRecyclerView;
    private ProgressBar searchProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Dil ayarını uygula
        SharedPreferences sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE);
        boolean isTurkish = sharedPreferences.getBoolean("isTurkish", false);
        Locale locale = isTurkish ? new Locale("tr") : new Locale("en");
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.setLocale(locale);
        res.updateConfiguration(conf, dm);
        
        // Tema ayarını uygula
        ThemeHelper.applyTheme(ThemeHelper.isDarkMode(this));
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupRetrofit();
        initializeViews();
        setupProfileImage();
        setupWindowFlags();
        setupSliderRunnable();
        displayUserName();
        setupBottomNavigation();

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
        profileImageView = findViewById(R.id.profileImage);
        userNameText = findViewById(R.id.userNameText);
        userEmailText = findViewById(R.id.userEmailText);
        searchEditText = findViewById(R.id.editTextText);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        searchProgressBar = findViewById(R.id.searchProgressBar);

        setupSearchFunctionality();
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

    private void displayUserName() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            userNameText.setText(getString(R.string.hello));
            userEmailText.setText(email);
        }
    }

    private void setupSearchFunctionality() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            private Handler handler = new Handler();
            private Runnable searchRunnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }

                searchRunnable = () -> {
                    String query = s.toString().trim();
                    if (query.length() >= 2) { // En az 2 karakter girildiğinde aramayı başlat
                        performSearch(query);
                    } else {
                        // 2 karakterden az girildiğinde normal film listelerini göster
                        searchResultsRecyclerView.setVisibility(View.GONE);
                        viewPager2.setVisibility(View.VISIBLE);
                        recyclerViewTopMovies.setVisibility(View.VISIBLE);
                        recyclerViewUpcoming.setVisibility(View.VISIBLE);
                    }
                };

                // 500ms gecikme ile arama yap
                handler.postDelayed(searchRunnable, 500);
            }
        });
    }

    private void performSearch(String query) {
        searchProgressBar.setVisibility(View.VISIBLE);
        
        // Arama yapılırken diğer film listelerini gizle
        viewPager2.setVisibility(View.GONE);
        recyclerViewTopMovies.setVisibility(View.GONE);
        recyclerViewUpcoming.setVisibility(View.GONE);
        searchResultsRecyclerView.setVisibility(View.VISIBLE);

        tmdbApi.searchMovies(API_KEY, query, "").enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> movies = response.body().getResults();
                    if (!movies.isEmpty()) {
                        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                        searchResultsRecyclerView.setAdapter(new FilmListAdapter(convertToFilmList(movies, searchResultsRecyclerView)));
                    } else {
                        Toast.makeText(MainActivity.this, getString(R.string.no_movies_found), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.search_failed), Toast.LENGTH_SHORT).show();
                }
                searchProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                searchProgressBar.setVisibility(View.GONE);
            }
        });
    }

    private void initUpcoming() {
        progressBarUpcoming.setVisibility(View.VISIBLE);
        tmdbApi.getUpcomingMovies(API_KEY, "credits,release_dates").enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> movies = response.body().getResults();
                    if (!movies.isEmpty()) {
                        recyclerViewUpcoming.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
                        recyclerViewUpcoming.setAdapter(new FilmListAdapter(convertToFilmList(movies, recyclerViewUpcoming)));
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
            public void onFailure(Call<MovieResponse> call, Throwable t) { // Api çağrısı hiç ulaşmamışsa
                System.out.println("API çağrısı başarısız: " + t.getMessage());
                t.printStackTrace();
                progressBarUpcoming.setVisibility(View.GONE);
            }
        });
    }

    private void initTopMoving() {
        progressBarTop.setVisibility(View.VISIBLE);
        tmdbApi.getTopRatedMovies(API_KEY, "credits,release_dates").enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> movies = response.body().getResults();
                    if (!movies.isEmpty()) {
                        recyclerViewTopMovies.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
                        recyclerViewTopMovies.setAdapter(new FilmListAdapter(convertToFilmList(movies, recyclerViewTopMovies)));
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
        tmdbApi.getPopularMovies(API_KEY, "credits,release_dates").enqueue(new Callback<MovieResponse>() {
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

    private void loadMovieDetails(Movie movie, Film film, ArrayList<Film> films, RecyclerView recyclerView) {
        tmdbApi.getMovieDetails(movie.getId(), API_KEY, "credits,release_dates").enqueue(new Callback<MovieDetail>() {
            @Override
            public void onResponse(Call<MovieDetail> call, Response<MovieDetail> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieDetail detail = response.body();
                    film.setTime(detail.getRuntime() + " dk");
                    
                    // Genre listesini güncelle
                    ArrayList<String> genres = new ArrayList<>();
                    if (detail.getGenres() != null) {
                        for (MovieDetail.Genre genre : detail.getGenres()) {
                            genres.add(genre.getName());
                        }
                    }
                    film.setGenre(genres);
                    
                    // Cast listesini güncelle
                    ArrayList<Cast> casts = new ArrayList<>();
                    if (detail.getCredits() != null && detail.getCredits().getCast() != null) {
                        for (MovieDetail.Cast apiCast : detail.getCredits().getCast()) {
                            Cast cast = new Cast();
                            cast.setActor(apiCast.getName());
                            cast.setPicUrl(apiCast.getProfilePath());
                            casts.add(cast);
                        }
                    }
                    film.setCasts(casts);
                    
                    // Adapter'ı güncelle
                    recyclerView.getAdapter().notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<MovieDetail> call, Throwable t) {
                System.out.println("Film detayları alınamadı: " + t.getMessage());
            }
        });
    }

    private void loadMovieDetailsForSlider(Movie movie, SliderItems item, ArrayList<SliderItems> items, ViewPager2 viewPager) {
        tmdbApi.getMovieDetails(movie.getId(), API_KEY, "credits,release_dates").enqueue(new Callback<MovieDetail>() {
            @Override
            public void onResponse(Call<MovieDetail> call, Response<MovieDetail> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieDetail detail = response.body();
                    item.setTime(detail.getRuntime() + " dk");
                    
                    if (detail.getGenres() != null && !detail.getGenres().isEmpty()) {
                        item.setGenre(detail.getGenres().get(0).getName());
                    }
                    
                    // Adapter'ı güncelle
                    viewPager.getAdapter().notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<MovieDetail> call, Throwable t) {
                System.out.println("Film detayları alınamadı: " + t.getMessage());
            }
        });
    }

    private ArrayList<Film> convertToFilmList(List<Movie> movies, RecyclerView recyclerView) {
        ArrayList<Film> films = new ArrayList<>();
        for (Movie movie : movies) {
            Film film = new Film();
            film.setId(movie.getId());
            film.setTitle(movie.getTitle());
            film.setPicUrl(movie.getPosterPath());
            film.setDescription(movie.getOverview());
            film.setImdb((int) movie.getVoteAverage());
            
            // Yıl bilgisini release_date'den al (format: YYYY-MM-DD)
            String releaseDate = movie.getReleaseDate();
            if (releaseDate != null && releaseDate.length() >= 4) {
                film.setYear(Integer.parseInt(releaseDate.substring(0, 4)));
            }
            
            film.setTrailer("https://www.youtube.com/watch?v=pGta87S18ZQ");
            films.add(film);
            
            // Her film için detay bilgilerini yükle
            loadMovieDetails(movie, film, films, recyclerView);
        }
        return films;
    }

    private ArrayList<SliderItems> convertToSliderItems(List<Movie> movies) {
        ArrayList<SliderItems> items = new ArrayList<>();
        for (Movie movie : movies) {
            SliderItems item = new SliderItems();
            item.setImageUrl(movie.getBackdropPath());
            item.setTitle(movie.getTitle());
            
            // Yıl bilgisini release_date'den al
            String releaseDate = movie.getReleaseDate();
            if (releaseDate != null && releaseDate.length() >= 4) {
                item.setYear(releaseDate.substring(0, 4));
            }
            
            item.setGenre("Film"); // Geçici değer, detaylar gelince güncellenecek
            item.setAge("13+");
            items.add(item);
            
            // Her film için detay bilgilerini yükle
            loadMovieDetailsForSlider(movie, item, items, viewPager2);
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

    private void setupBottomNavigation() {
        ChipNavigationBar bottomNav = findViewById(R.id.bottom_menu);
        bottomNav.setItemSelected(R.id.explorer, true);

        bottomNav.setOnItemSelectedListener(id -> {
            if (id == R.id.explorer) {
                // Ana sayfadayız, bir şey yapmaya gerek yok
            } else if (id == R.id.favorites) {
                Toast.makeText(this, getString(R.string.favorites_coming_soon), Toast.LENGTH_SHORT).show();
            } else if (id == R.id.profile) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
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
