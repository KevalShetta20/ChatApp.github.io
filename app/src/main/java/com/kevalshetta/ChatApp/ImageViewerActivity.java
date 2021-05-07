package com.kevalshetta.ChatApp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.kevalshetta.buddiesgram.R;
import com.squareup.picasso.Picasso;

public class ImageViewerActivity extends AppCompatActivity {

    private ImageView image_viewer;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        imageUrl = getIntent().getStringExtra("url");
        Picasso.get().load(imageUrl).into(image_viewer);

        image_viewer = findViewById(R.id.image_viewer);
    }
}