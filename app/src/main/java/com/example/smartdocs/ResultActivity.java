package com.example.smartdocs;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;
import android.widget.ImageButton;

public class ResultActivity extends AppCompatActivity {
    private List<String> imageNames;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // SUPPRIME : plus de Toolbar dans le layout harmonisé
        // Toolbar toolbar = findViewById(R.id.toolbarResult);
        // setSupportActionBar(toolbar);
        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // getSupportActionBar().setTitle("Résultats");

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        imageNames = getIntent().getStringArrayListExtra("images");
        if (imageNames == null) imageNames = new ArrayList<>();

        RecyclerView rvResults = findViewById(R.id.rvResults);
        rvResults.setLayoutManager(new GridLayoutManager(this, 2));
        ImageAdapter imageAdapter = new ImageAdapter(imageNames);
        rvResults.setAdapter(imageAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
        private List<String> imageNames;
        public ImageAdapter(List<String> imageNames) { this.imageNames = imageNames; }
        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_image, parent, false);
            return new ImageViewHolder(view);
        }
        @Override
        public void onBindViewHolder(ImageViewHolder holder, int position) {
            String name = imageNames.get(position);
            String url = "https://ged.smart4apps.com/Images/Temp/thumbs/" + name;
            Glide.with(holder.imageView.getContext()).load(url).placeholder(R.drawable.ic_launcher_background).into(holder.imageView);
            // SUPPRIME : plus de nom à afficher
            // holder.tvImageName.setText(name);
            holder.imageView.setOnClickListener(v -> {
                Intent intent = new Intent(ResultActivity.this, FullscreenImageActivity.class);
                intent.putExtra("imageUrl", url);
                startActivity(intent);
            });
        }
        @Override
        public int getItemCount() { return imageNames.size(); }
        public class ImageViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            // SUPPRIME : plus de TextView tvImageName;
            public ImageViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageView);
                // SUPPRIME : plus de tvImageName
            }
        }
    }
} 