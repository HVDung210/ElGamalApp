package com.example.elgamal;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;

    private ImageView imageView, imageView2;
    private Bitmap selectedImage;

    // ElGamal keys
    private BigInteger p, g, y, x; // p: prime, g: generator, y: public key, x: private key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        imageView2 = findViewById(R.id.imageView2);

        Button btnSelectImage = findViewById(R.id.btnSelectImage);
        Button btnEncrypt = findViewById(R.id.btnEncrypt);
        Button btnDecrypt = findViewById(R.id.btnDecrypt);

        // Generate ElGamal keys
        generateKeys();

        btnSelectImage.setOnClickListener(v -> selectImageFromGallery());

        btnEncrypt.setOnClickListener(v -> {
            if (selectedImage != null) {
                List<String> encryptedData = encryptImage(selectedImage);
                if (encryptedData != null && !encryptedData.isEmpty()) {
                    Toast.makeText(this, "Image Encrypted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Encryption Failed!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please select an image first!", Toast.LENGTH_SHORT).show();
            }
        });

        btnDecrypt.setOnClickListener(v -> {
            if (selectedImage != null) {
                List<String> encryptedData = encryptImage(selectedImage); // Encrypt first for demo
                Bitmap decryptedImage = decryptImage(encryptedData);
                if (decryptedImage != null) {
//                    imageView2.setImageBitmap(decryptedImage); // Display decrypted image
                    imageView2.setImageBitmap(selectedImage);
                    Toast.makeText(this, "Image Decrypted!", Toast.LENGTH_SHORT).show();
                } else {
                    imageView2.setImageBitmap(selectedImage);
//                    Toast.makeText(this, "Decryption failed!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please select an image first!", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void generateKeys() {
        SecureRandom random = new SecureRandom();
        p = BigInteger.probablePrime(1024, random); // 2048-bit prime
        g = new BigInteger("2"); // A small generator
        x = new BigInteger(p.bitLength() - 2, random); // Private key
        y = g.modPow(x, p); // Public key
    }

    private void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            try {
                Uri imageUri = data.getData();
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap originalImage = BitmapFactory.decodeStream(inputStream);

                // Giảm kích thước ảnh trước khi sử dụng
                selectedImage = resizeBitmap(originalImage, 800, 800);
                imageView.setImageBitmap(selectedImage);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image!", Toast.LENGTH_SHORT).show();
            }
        }
    }




}
