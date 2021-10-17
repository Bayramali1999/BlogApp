package uz.soft.blogapp;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import uz.soft.blogapp.databinding.ActivityHomeBinding;
import uz.soft.blogapp.models.Post;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityHomeBinding binding;
    private Dialog popUpDialog;
    private ImageView userImg, bgImg, addImg;
    private ProgressBar pbLoading;
    private EditText etTitle, etDescr;
    private static final int ReqCode = 3;
    private Uri uri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        iniPopup();
        popupImageClick();
        setSupportActionBar(binding.appBarHome.toolbar);
        binding.appBarHome.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popUpDialog.show();
            }
        });
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if (item.getItemId() == R.id.nav_out) {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    drawer.closeDrawer(Gravity.LEFT);
                    return true;
                }

                return false;
            }
        });
        updateNavHeader();
    }

    private void popupImageClick() {
        bgImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkMyPermission();
            }
        });
    }

    private void checkMyPermission() {
        if (Build.VERSION_CODES.M < Build.VERSION.SDK_INT) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "Please chesck permission", Toast.LENGTH_SHORT).show();
                } else {
                    ActivityCompat.requestPermissions(HomeActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            ReqCode);
                }
            } else {
                openGallery();
            }
        } else {
            openGallery();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, ReqCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ReqCode && data != null && resultCode == RESULT_OK) {
            uri = data.getData();
            bgImg.setImageURI(uri);

        }
    }

    private void iniPopup() {
        popUpDialog = new Dialog(this);
        popUpDialog.setContentView(R.layout.poup_layout);
        popUpDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popUpDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
        popUpDialog.getWindow().getAttributes().gravity = Gravity.TOP;

        //ini

        userImg = popUpDialog.findViewById(R.id.poup_img_acc);
        addImg = popUpDialog.findViewById(R.id.popup_add);
        bgImg = popUpDialog.findViewById(R.id.popup_bg);

        etTitle = popUpDialog.findViewById(R.id.popup_title);
        etDescr = popUpDialog.findViewById(R.id.popup_descr);
        pbLoading = popUpDialog.findViewById(R.id.popup_pb);

        Glide.with(this).load(user.getPhotoUrl()).into(userImg);

        addImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addImg.setVisibility(View.INVISIBLE);
                pbLoading.setVisibility(View.VISIBLE);

                if (!etTitle.getText().toString().isEmpty() &&
                        !etDescr.getText().toString().isEmpty() &&
                        uri != null) {

                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("blog_image");
                    final StorageReference imageFilePath = storageReference.child(uri.getLastPathSegment());
                    imageFilePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    String imageDownloadLink = uri.toString();

                                    Post post = new Post(
                                            etTitle.getText().toString(),
                                            etDescr.getText().toString(),
                                            imageDownloadLink,
                                            user.getUid(),
                                            user.getPhotoUrl().toString()
                                    );

                                    addPost(post);
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            showMessage(e.getMessage());
                            addImg.setVisibility(View.VISIBLE);
                            pbLoading.setVisibility(View.INVISIBLE);
                        }
                    });

                } else {
                    addImg.setVisibility(View.VISIBLE);
                    pbLoading.setVisibility(View.INVISIBLE);
                    showMessage("Please check all verify");
                }
            }
        });

    }

    private void addPost(Post post) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Posts").push();

        String key = reference.getKey();
        post.setKey(key);

        reference.setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                showMessage("Post added");

                addImg.setVisibility(View.VISIBLE);
                pbLoading.setVisibility(View.INVISIBLE);
                popUpDialog.dismiss();
            }
        });
    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void updateNavHeader() {
        NavigationView navigationView = binding.navView;
        View view = navigationView.getHeaderView(0);

        TextView tvName = view.findViewById(R.id.nav_name);
        TextView tvMail = view.findViewById(R.id.nav_mail);
        ImageView nameImg = view.findViewById(R.id.nav_image);

        tvName.setText(user.getDisplayName());
        tvMail.setText(user.getEmail());

        Glide.with(this).load(user.getPhotoUrl()).into(nameImg);
    }
}