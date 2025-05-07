package com.example.movieapp.Activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.movieapp.Adapters.CastListAdapter;
import com.example.movieapp.Adapters.CategoryEachFilmAdapter;
import com.example.movieapp.Api.TMDBApi;
import com.example.movieapp.Model.Film;
import com.example.movieapp.Model.MovieDetail;
import com.example.movieapp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DetailActivity extends AppCompatActivity {
    private ImageView filmPic;
    private TextView titleTxt, imdbTxt, movieTimesTxt, movieSummery;
    private Button watchTrailerBtn;
    private ImageView backImg;
    private BlurView blurView;
    private RecyclerView genreView, CastView;
    private TMDBApi tmdbApi;
    private static final String BASE_URL = "https://api.themoviedb.org/3/";
    private static final String API_KEY = "7a9090d779c7281be841719a179f903e";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        setupRetrofit();
        initializeViews();
        setVariable();

        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    private void setupRetrofit() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        tmdbApi = retrofit.create(TMDBApi.class);
    }

    private void initializeViews() {
        filmPic = findViewById(R.id.filmPic);
        titleTxt = findViewById(R.id.titleTxt);
        imdbTxt = findViewById(R.id.imdbTxt);
        movieTimesTxt = findViewById(R.id.movieTimesTxt);
        movieSummery = findViewById(R.id.movieSummery);
        watchTrailerBtn = findViewById(R.id.watchTrailerBtn);
        backImg = findViewById(R.id.backImg);
        blurView = findViewById(R.id.blurView);
        genreView = findViewById(R.id.genreView);
        CastView = findViewById(R.id.CastView);
    }

    private void setVariable() {
        Film item = (Film) getIntent().getSerializableExtra("object");
        loadMovieDetails(item);

        backImg.setOnClickListener(v -> finish());

        float radius = 10f;
        View decorView = getWindow().getDecorView();
        ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
        Drawable windowsBackground = decorView.getBackground();

        blurView.setupWith(rootView, new RenderScriptBlur(this))
                .setFrameClearDrawable(windowsBackground)
                .setBlurRadius(radius);
        blurView.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        blurView.setClipToOutline(true);
    }

    private void loadMovieDetails(Film item) {
        System.out.println("Film ID: " + item.getId()); // ID'yi kontrol et
        tmdbApi.getMovieDetails(item.getId(), API_KEY, "credits").enqueue(new Callback<MovieDetail>() {
            @Override
            public void onResponse(Call<MovieDetail> call, Response<MovieDetail> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieDetail movieDetail = response.body();
                    System.out.println("Film detayları başarıyla alındı: " + movieDetail.getTitle());
                    updateUI(movieDetail, item);
                } else {
                    System.out.println("Film detayları alınamadı. Hata kodu: " + response.code());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Hata detayı yok";
                        System.out.println("Hata detayı: " + errorBody);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(DetailActivity.this, "Film detayları alınamadı. Hata kodu: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MovieDetail> call, Throwable t) {
                System.out.println("Film detayları alınamadı. Hata: " + t.getMessage());
                t.printStackTrace();
                Toast.makeText(DetailActivity.this, "Film detayları alınamadı: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(MovieDetail movieDetail, Film item) {
        RequestOptions requestOptions = new RequestOptions()
                .transform(new CenterCrop(), new GranularRoundedCorners(0, 0, 50, 50));

        Glide.with(this)
                .load(movieDetail.getPosterPath())
                .apply(requestOptions)
                .into(filmPic);

        titleTxt.setText(movieDetail.getTitle());
        imdbTxt.setText(String.format("IMDB %.1f", movieDetail.getVoteAverage()));
        
        // Yıl bilgisini release_date'den al (format: YYYY-MM-DD)
        String year = movieDetail.getReleaseDate().substring(0, 4);
        String duration = movieDetail.getRuntime() + " dk";
        movieTimesTxt.setText(year + " - " + duration);
        
        movieSummery.setText(movieDetail.getOverview());

        // Genre listesini güncelle
        List<String> genres = movieDetail.getGenres().stream()
                .map(MovieDetail.Genre::getName)
                .collect(Collectors.toList());
        genreView.setAdapter(new CategoryEachFilmAdapter(genres));
        genreView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Cast listesini güncelle
        if (movieDetail.getCredits() != null && movieDetail.getCredits().getCast() != null) {
            ArrayList<com.example.movieapp.Model.Cast> castList = new ArrayList<>();
            for (MovieDetail.Cast apiCast : movieDetail.getCredits().getCast()) {
                com.example.movieapp.Model.Cast cast = new com.example.movieapp.Model.Cast();
                cast.setActor(apiCast.getName());
                cast.setPicUrl(apiCast.getProfilePath());
                castList.add(cast);
            }
            CastView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            CastView.setAdapter(new CastListAdapter(castList));
        }

        watchTrailerBtn.setOnClickListener(v -> {
            String videoId = item.getTrailer().replace("https://www.youtube.com/watch?v=", "");
            Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getTrailer()));

            try {
                startActivity(appIntent);
            } catch (ActivityNotFoundException ex) {
                startActivity(webIntent);
            }
        });
    }
}