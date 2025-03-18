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

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.movieapp.Adapters.CastListAdapter;
import com.example.movieapp.Adapters.CategoryEachFilmAdapter;
import com.example.movieapp.Domains.Film;
import com.example.movieapp.R;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class DetailActivity extends AppCompatActivity {
    private ImageView filmPic;
    private TextView titleTxt, imdbTxt, movieTimesTxt, movieSummery;
    private Button watchTrailerBtn;
    private ImageView backImg;
    private BlurView blurView;
    private RecyclerView genreView, CastView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

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

        setVariable();

        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

    }

    private void setVariable() {
        Film item = (Film) getIntent().getSerializableExtra("object");
        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transform(new CenterCrop(), new GranularRoundedCorners(0, 0, 50, 50));

        Glide.with(this)
                .load(item.getPoster())
                .apply(requestOptions)
                .into(filmPic);

        titleTxt.setText(item.getTitle());
        imdbTxt.setText("IMDB " + item.getImdb());
        movieTimesTxt.setText(item.getYear() + " - " + item.getTime());
        movieSummery.setText(item.getDescription());

        watchTrailerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = item.getTrailer().replace("https://www.youtube.com/watch?v=", "");
                Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getTrailer()));

                try {
                    startActivity(appIntent);
                } catch (ActivityNotFoundException ex) {
                    startActivity(webIntent);
                }
            }
        });
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

        if (item.getGenre() != null) {
            genreView.setAdapter(new CategoryEachFilmAdapter(item.getGenre()));
            genreView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        }

        if (item.getCasts() != null) {
            CastView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            CastView.setAdapter(new CastListAdapter(item.getCasts()));
        }
    }
}