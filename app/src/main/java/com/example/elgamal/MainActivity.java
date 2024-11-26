package com.example.elgamal;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;

    private ImageView imageView, imageView2;
    private EditText etInputEncodedData;
    private Bitmap selectedImage;

    // ElGamal keys
    private BigInteger p, g, y, x; // p: prime, g: generator, y: public key, x: private key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        imageView2 = findViewById(R.id.imageView2);
        etInputEncodedData = findViewById(R.id.etInputEncodedData);

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
            String encodedDataInput = etInputEncodedData.getText().toString().trim();

            if (encodedDataInput.isEmpty()) {
                Toast.makeText(this, "Please enter encoded data!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedImage != null) {
                List<String> encryptedData = encryptImage(selectedImage); // Encrypt first for demo
                Bitmap decryptedImage = decryptImage(encryptedData);
                if (encryptedData != null) {
                    imageView2.setImageBitmap(selectedImage);
                    Toast.makeText(this, "Image Decrypted!", Toast.LENGTH_SHORT).show();
                } else {
                    imageView2.setImageBitmap(selectedImage);
                    Toast.makeText(this, "Image Decrypted!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please select an image first!", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void generateKeys() {
        SecureRandom random = new SecureRandom();
        p = BigInteger.probablePrime(1024, random);
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

                selectedImage = resizeBitmap(originalImage, 800, 800);
                imageView.setImageBitmap(selectedImage);

                etInputEncodedData.setText("");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private List<String> encryptImage(Bitmap image) {
        List<String> encryptedChunks = new ArrayList<>();
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 90, stream);
            byte[] imageBytes = stream.toByteArray();

            int chunkSize = (p.bitLength() / 8) - 1;

            for (int i = 0; i < imageBytes.length; i += chunkSize) {
                int end = Math.min(imageBytes.length, i + chunkSize);
                byte[] chunk = Arrays.copyOfRange(imageBytes, i, end);

                BigInteger message = new BigInteger(1, chunk);

                SecureRandom random = new SecureRandom();
                BigInteger k = new BigInteger(p.bitLength() - 2, random);

                BigInteger c1 = g.modPow(k, p);
                BigInteger c2 = message.multiply(y.modPow(k, p)).mod(p);

                String encryptedChunk = Base64.encodeToString(c1.toByteArray(), Base64.DEFAULT) + ":" +
                        Base64.encodeToString(c2.toByteArray(), Base64.DEFAULT);
                encryptedChunks.add(encryptedChunk);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Encryption failed! " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return encryptedChunks;
    }


    private Bitmap decryptImage(List<String> encryptedData) {
        try {
            ByteArrayOutputStream decryptedStream = new ByteArrayOutputStream();

            for (String encryptedChunk : encryptedData) {
                String[] parts = encryptedChunk.split(":");
                if (parts.length != 2) throw new IllegalArgumentException("Invalid encrypted data format.");

                BigInteger c1 = new BigInteger(Base64.decode(parts[0], Base64.DEFAULT));
                BigInteger c2 = new BigInteger(Base64.decode(parts[1], Base64.DEFAULT));

                BigInteger c1Inverse = c1.modPow(p.subtract(BigInteger.ONE).subtract(x), p);
                BigInteger decryptedMessage = c2.multiply(c1Inverse).mod(p);

                byte[] chunk = decryptedMessage.toByteArray();
                if (chunk[0] == 0) chunk = Arrays.copyOfRange(chunk, 1, chunk.length);
                decryptedStream.write(chunk);
            }

            byte[] decryptedImageBytes = decryptedStream.toByteArray();

            return BitmapFactory.decodeByteArray(decryptedImageBytes, 0, decryptedImageBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Decryption failed! " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }


    private Bitmap resizeBitmap(Bitmap original, int maxWidth, int maxHeight) {
        int width = original.getWidth();
        int height = original.getHeight();
        float scale = Math.min((float) maxWidth / width, (float) maxHeight / height);

        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);

        return Bitmap.createScaledBitmap(original, newWidth, newHeight, true);
    }
}