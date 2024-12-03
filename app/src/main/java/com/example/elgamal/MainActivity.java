package com.example.elgamal;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

//public class MainActivity extends AppCompatActivity {
//
//    private ImageView imageViewOriginal, imageViewDecrypted;
//    private EditText etEncodedData;
//    private Bitmap originalBitmap;
//    private SecretKey aesKey;
//    private KeyPair rsaKeyPair;
//    private byte[] iv;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        imageViewOriginal = findViewById(R.id.imageViewOriginal);
//        imageViewDecrypted = findViewById(R.id.imageViewDecrypted);
//        etEncodedData = findViewById(R.id.etEncodedData);
//
//        Button btnSelectImage = findViewById(R.id.btnSelectImage);
//        Button btnEncrypt = findViewById(R.id.btnEncrypt);
//        Button btnDecrypt = findViewById(R.id.btnDecrypt);
//
//        try {
//            // Tạo cặp khóa RSA và khóa AES
//            rsaKeyPair = generateRSAKeyPair();
//            aesKey = generateAESKey();
//        } catch (Exception e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Lỗi khởi tạo khóa!", Toast.LENGTH_SHORT).show();
//        }
//
//        btnSelectImage.setOnClickListener(v -> selectImage());
//        btnEncrypt.setOnClickListener(v -> encryptImage());
//        btnDecrypt.setOnClickListener(v -> decryptImage());
//    }
//
//    private void selectImage() {
//        Intent intent = new Intent(Intent.ACTION_PICK);
//        intent.setType("image/*");
//        startActivityForResult(intent, 100);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data != null) {
//            Uri imageUri = data.getData();
//            try {
//                originalBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
//                imageViewOriginal.setImageBitmap(originalBitmap);
//            } catch (Exception e) {
//                e.printStackTrace();
//                Toast.makeText(this, "Không thể chọn hình ảnh!", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    private void encryptImage() {
//        try {
//            if (originalBitmap == null) {
//                Toast.makeText(this, "Hãy chọn hình ảnh trước!", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            // Chuyển đổi hình ảnh thành byte array
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            originalBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
//            byte[] imageData = baos.toByteArray();
//
//            // Tạo IV ngẫu nhiên
//            iv = new byte[16];
//            new SecureRandom().nextBytes(iv);
//            IvParameterSpec ivSpec = new IvParameterSpec(iv);
//
//            // Mã hóa dữ liệu hình ảnh bằng AES
//            Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//            aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);
//            byte[] encryptedImageData = aesCipher.doFinal(imageData);
//
//            // Mã hóa khóa AES bằng RSA
//            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//            rsaCipher.init(Cipher.ENCRYPT_MODE, rsaKeyPair.getPublic());
//            byte[] encryptedAESKey = rsaCipher.doFinal(aesKey.getEncoded());
//
//            // Kết hợp dữ liệu thành chuỗi Base64
//            String encodedData = Base64.encodeToString(encryptedAESKey, Base64.NO_WRAP) + ":" +
//                    Base64.encodeToString(iv, Base64.NO_WRAP) + ":" +
//                    Base64.encodeToString(encryptedImageData, Base64.NO_WRAP);
//
//            etEncodedData.setText(encodedData);
//            Toast.makeText(this, "Mã hóa thành công!", Toast.LENGTH_SHORT).show();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Lỗi mã hóa hình ảnh!", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void decryptImage() {
//        try {
//            String encodedData = etEncodedData.getText().toString();
//            if (encodedData.isEmpty()) {
//                Toast.makeText(this, "Hãy nhập dữ liệu để giải mã!", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            // Phân tách dữ liệu từ chuỗi Base64
//            String[] parts = encodedData.split(":");
//            if (parts.length != 3) {
//                Toast.makeText(this, "Dữ liệu không hợp lệ!", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            byte[] encryptedAESKey = Base64.decode(parts[0], Base64.NO_WRAP);
//            byte[] iv = Base64.decode(parts[1], Base64.NO_WRAP);
//            byte[] encryptedImageData = Base64.decode(parts[2], Base64.NO_WRAP);
//
//            // Giải mã khóa AES bằng RSA
//            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//            rsaCipher.init(Cipher.DECRYPT_MODE, rsaKeyPair.getPrivate());
//            byte[] decryptedAESKey = rsaCipher.doFinal(encryptedAESKey);
//
//            // Tạo lại khóa AES và giải mã dữ liệu hình ảnh
//            SecretKey originalAESKey = new SecretKeySpec(decryptedAESKey, "AES");
//            IvParameterSpec ivSpec = new IvParameterSpec(iv);
//
//            Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//            aesCipher.init(Cipher.DECRYPT_MODE, originalAESKey, ivSpec);
//            byte[] decryptedImageData = aesCipher.doFinal(encryptedImageData);
//
//            // Chuyển đổi byte array thành Bitmap
//            Bitmap decryptedBitmap = BitmapFactory.decodeByteArray(decryptedImageData, 0, decryptedImageData.length);
//            imageViewDecrypted.setImageBitmap(decryptedBitmap);
//
//            Toast.makeText(this, "Giải mã thành công!", Toast.LENGTH_SHORT).show();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Lỗi giải mã hình ảnh!", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private KeyPair generateRSAKeyPair() throws Exception {
//        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
//        keyPairGenerator.initialize(2048);
//        return keyPairGenerator.generateKeyPair();
//    }
//
//    private SecretKey generateAESKey() throws Exception {
//        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
//        keyGenerator.init(256);
//        return keyGenerator.generateKey();
//    }
//}

public class MainActivity extends AppCompatActivity {

    private ImageView imageViewOriginal, imageViewDecrypted;
    private EditText etEncodedData;
    private Bitmap originalBitmap;
    private SecretKey aesKey;
    private KeyPair rsaKeyPair;
    private byte[] iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageViewOriginal = findViewById(R.id.imageViewOriginal);
        imageViewDecrypted = findViewById(R.id.imageViewDecrypted);
        etEncodedData = findViewById(R.id.etEncodedData);

        Button btnSelectImage = findViewById(R.id.btnSelectImage);
        Button btnEncrypt = findViewById(R.id.btnEncrypt);
        Button btnDecrypt = findViewById(R.id.btnDecrypt);

        try {
            rsaKeyPair = generateRSAKeyPair();
            aesKey = generateAESKey();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error initializing keys!", Toast.LENGTH_SHORT).show();
        }

        btnSelectImage.setOnClickListener(v -> selectImage());
        btnEncrypt.setOnClickListener(v -> encryptImage());
        btnDecrypt.setOnClickListener(v -> decryptImage());
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                originalBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                imageViewOriginal.setImageBitmap(originalBitmap);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Unable to select image!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void encryptImage() {
        if (originalBitmap == null) {
            Toast.makeText(this, "Please select an image first!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File imageFile = saveBitmapToFile(originalBitmap, "original_image.png");
            FileInputStream fis = new FileInputStream(imageFile);

            // Generate random IV
            iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // Encrypt image data
            Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);

            File encryptedFile = new File(getFilesDir(), "encrypted_image.dat");
            FileOutputStream fos = new FileOutputStream(encryptedFile);
            CipherOutputStream cos = new CipherOutputStream(fos, aesCipher);

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                cos.write(buffer, 0, bytesRead);
            }

            cos.close();
            fis.close();

            // Encrypt AES key using RSA
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsaCipher.init(Cipher.ENCRYPT_MODE, rsaKeyPair.getPublic());
            byte[] encryptedAESKey = rsaCipher.doFinal(aesKey.getEncoded());

            // Combine encrypted key, IV, and image path
            String encodedData = Base64.encodeToString(encryptedAESKey, Base64.NO_WRAP) + ":" +
                    Base64.encodeToString(iv, Base64.NO_WRAP) + ":" +
                    encryptedFile.getAbsolutePath();

            etEncodedData.setText(encodedData);
            Toast.makeText(this, "Image encrypted successfully!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Image encryption failed!", Toast.LENGTH_SHORT).show();
        }
    }

    private void decryptImage() {
        try {
            String encodedData = etEncodedData.getText().toString();
            if (encodedData.isEmpty()) {
                Toast.makeText(this, "Please enter encoded data!", Toast.LENGTH_SHORT).show();
                return;
            }

            String[] parts = encodedData.split(":");
            if (parts.length != 3) {
                Toast.makeText(this, "Invalid data format!", Toast.LENGTH_SHORT).show();
                return;
            }

            byte[] encryptedAESKey = Base64.decode(parts[0], Base64.NO_WRAP);
            byte[] iv = Base64.decode(parts[1], Base64.NO_WRAP);
            String encryptedFilePath = parts[2];

            File encryptedFile = new File(encryptedFilePath);
            if (!encryptedFile.exists()) {
                Toast.makeText(this, "Encrypted file not found!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Decrypt AES key
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsaCipher.init(Cipher.DECRYPT_MODE, rsaKeyPair.getPrivate());
            byte[] decryptedAESKey = rsaCipher.doFinal(encryptedAESKey);

            SecretKey originalAESKey = new SecretKeySpec(decryptedAESKey, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // Decrypt image data
            Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            aesCipher.init(Cipher.DECRYPT_MODE, originalAESKey, ivSpec);

            FileInputStream fis = new FileInputStream(encryptedFile);
            CipherInputStream cis = new CipherInputStream(fis, aesCipher);

            File decryptedFile = new File(getFilesDir(), "decrypted_image.png");
            FileOutputStream fos = new FileOutputStream(decryptedFile);

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = cis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }

            cis.close();
            fos.close();

            // Display the decrypted image
            Bitmap decryptedBitmap = BitmapFactory.decodeFile(decryptedFile.getAbsolutePath());
            imageViewDecrypted.setImageBitmap(decryptedBitmap);

            Toast.makeText(this, "Image decrypted successfully!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Image decryption failed!", Toast.LENGTH_SHORT).show();
        }
    }

    private File saveBitmapToFile(Bitmap bitmap, String fileName) throws IOException {
        File file = new File(getFilesDir(), fileName);
        FileOutputStream fos = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fos.close();
        return file;
    }

    private KeyPair generateRSAKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }

    private SecretKey generateAESKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        return keyGenerator.generateKey();
    }
}




