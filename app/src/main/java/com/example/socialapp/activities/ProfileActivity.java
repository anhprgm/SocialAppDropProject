package com.example.socialapp.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.socialapp.R;
import com.example.socialapp.databinding.ActivityProfileBinding;
import com.example.socialapp.utilities.Constants;
import com.example.socialapp.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    private PreferenceManager preferenceManager;
    private String encodedImage;
    private String NameNew;
    private Boolean Doi_Ten = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setContentView(binding.getRoot());
        loadUserDetails();
        binding.editImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
        saveChange();
        EditName();
        backIc();
    }
    private void loadUserDetails(){
        binding.textName.setText(preferenceManager.getString(Constants.KEY_NAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }
    private String encodeImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
    private void EditName(){
        binding.editName.setOnClickListener(v -> {
            binding.textName.setEnabled(true);
            binding.textName.setBackground(this.getResources().getDrawable(R.color.error));
            Doi_Ten = true;
            binding.SaveChange.setVisibility(View.VISIBLE);
        });
    }
    private void updateTextName(String NewName){
        preferenceManager.putString(Constants.KEY_NAME, NewName);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        documentReference.update(Constants.KEY_NAME, NewName)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(ProfileActivity.this, "Changed Name ", Toast.LENGTH_SHORT).show();
                    binding.SaveChange.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "ERROR", Toast.LENGTH_SHORT).show());
    }
    private void updateImage(String encodedImage){
        preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        documentReference.update(Constants.KEY_IMAGE, encodedImage)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(ProfileActivity.this, "Đổi Ảnh Thành Công", Toast.LENGTH_SHORT).show();
                    binding.SaveChange.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Lỗi", Toast.LENGTH_SHORT).show());
    }
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result ->{
                if (result.getResultCode() == RESULT_OK){
                    if(result.getData() != null){
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageProfile.setImageBitmap(bitmap);
                            encodedImage = encodeImage(bitmap);
                            binding.SaveChange.setVisibility(View.VISIBLE);
                            Doi_Ten = false;
                        }
                        catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
    );
    private void saveChange(){
        binding.SaveChange.setOnClickListener(v -> {
            if (Doi_Ten) {
                NameNew = binding.textName.getText().toString();
                updateTextName(NameNew);
                binding.textName.setEnabled(false);
                binding.textName.setBackground(null);
            } else {
                updateImage(encodedImage);
            }
        });
    }

    private void backIc() {
        binding.imageBack.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        });
    }
}