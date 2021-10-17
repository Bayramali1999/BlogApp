package uz.soft.blogapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class RegisterActivity extends AppCompatActivity {
    private static final int ReqCode = 1001;

    private ImageView regImage;
    private Uri uri;
    private EditText userName, userMail, userPassword, userPassword2;
    private Button btnReg;
    private ProgressBar progressBar;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        bindView();

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                btnReg.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);

                final String name = userName.getText().toString();
                final String mail = userMail.getText().toString();
                final String pass = userPassword.getText().toString();
                final String pass2 = userPassword2.getText().toString();

                if (name.isEmpty() || mail.isEmpty() || pass.isEmpty() || !pass.equals(pass2)) {
                    showMessage("Something is wrong");
                } else {
                    createUserAccount(name, mail, pass);
                }
            }
        });

        regImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    checkPermission();
                } else {
                    openGallery();
                }
            }
        });

    }

    private void createUserAccount(String name, String mail, String pass) {
        auth.createUserWithEmailAndPassword(mail, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            showMessage("Account Created");
                            uploadUserImg(name, uri, auth.getCurrentUser());
                        } else {
                            showMessage("Something is wrong");
                            btnReg.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
    }

    private void uploadUserImg(String name, Uri uri, FirebaseUser currentUser) {

        StorageReference mReference = FirebaseStorage.getInstance().getReference().child("user_photos");
        StorageReference imageFilePath = mReference.child(uri.getLastPathSegment());
        imageFilePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        UserProfileChangeRequest profileReq = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(uri)
                                .setDisplayName(name)
                                .build();

                        currentUser.updateProfile(profileReq)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            showMessage("Register Complated");

                                            updateUI();
                                        }
                                    }
                                });
                    }
                });
            }
        });
    }

    private void updateUI() {

        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();

    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        btnReg.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void bindView() {
        auth = FirebaseAuth.getInstance();
        regImage = findViewById(R.id.nav_user_photo);
        regImage.setImageResource(R.drawable.userphoto);
        userName = findViewById(R.id.reg_name);
        userMail = findViewById(R.id.reg_mail);
        userPassword = findViewById(R.id.reg_pass);
        userPassword2 = findViewById(R.id.reg_pass2);
        btnReg = findViewById(R.id.reg_btn);
        progressBar = findViewById(R.id.reg_pb);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void openGallery() {
        Intent imgIntent = new Intent(Intent.ACTION_GET_CONTENT);
        imgIntent.setType("image/*");
        startActivityForResult(imgIntent, ReqCode);
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "Please chesck permission", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(RegisterActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        ReqCode);
            }
        } else {
            openGallery();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && resultCode == RESULT_OK && requestCode == ReqCode) {
            uri = data.getData();
            regImage.setImageURI(uri);
        }
    }
}