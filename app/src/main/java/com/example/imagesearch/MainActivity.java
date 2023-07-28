package com.example.imagesearch;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private EditText searchEditText;
    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private Button searchButton;
    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private Map<String, Uri> imageMap;
    private ImageView resultImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        searchEditText = findViewById(R.id.searchEditText);
        resultImageView = findViewById(R.id.resultImageView);
        searchButton = findViewById(R.id.searchButton);
        recyclerView = findViewById(R.id.recyclerView);
        imageMap = new HashMap<>();
        imageAdapter = new ImageAdapter();

        recyclerView.setLayoutManager(new GridLayoutManager(this, 4, GridLayoutManager.VERTICAL, false));

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String imageName = searchEditText.getText().toString().trim();
                searchImage(imageName);
            }
        });

        recyclerView.setAdapter(imageAdapter); // Set the adapter

        loadAllImages();
    }

    private void loadAllImages() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
            return;
        }

        String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int idColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                int nameColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                int dataColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                long imageId = cursor.getLong(idColumnIndex);
                String imageName = cursor.getString(nameColumnIndex);
                String imageData = cursor.getString(dataColumnIndex);
                Uri imageUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(imageId));
                imageMap.put(imageName, imageUri);
                // Add the image data to the adapter
                imageAdapter.addImage(imageData, imageName);
            }
            cursor.close();
        }
    }

    private void searchImage(String imageName) {
        if (imageMap.containsKey(imageName)) {
            Uri imageUri = imageMap.get(imageName);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                // Display the searched image in the resultImageView
                resultImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                Toast.makeText(this, "Failed to display image", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Image not found", Toast.LENGTH_SHORT).show();
        }
    }


    private class ImageData {
        private String imageData;
        private String imageName;

        public ImageData(String imageData, String imageName) {
            this.imageData = imageData;
            this.imageName = imageName;
        }

        public String getImageData() {
            return imageData;
        }

        public String getImageName() {
            return imageName;
        }
    }

    // ImageAdapter class
    private class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
        private List<ImageData> imageList;

        public ImageAdapter() {
            imageList = new ArrayList<>();
        }

        public void addImage(String imageData, String imageName) {
            ImageData image = new ImageData(imageData, imageName);
            imageList.add(image);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
            return new ImageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            ImageData image = imageList.get(position);
            String imageData = image.getImageData();
            String imageName = image.getImageName();

            // Load and display the image using the imageData
            Glide.with(MainActivity.this)
                    .load(imageData)
                    .into(holder.imageView);

            // Set the image name below the image
            holder.imageNameTextView.setText(imageName);
        }

        @Override
        public int getItemCount() {
            return imageList.size();
        }

        // ImageViewHolder class
        private class ImageViewHolder extends RecyclerView.ViewHolder {
            private ImageView imageView;
            private TextView imageNameTextView;

            public ImageViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageView);
                imageNameTextView = itemView.findViewById(R.id.imageNameTextView);
            }
        }
    }
}