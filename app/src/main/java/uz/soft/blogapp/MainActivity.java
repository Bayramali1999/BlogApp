package uz.soft.blogapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {


    private EditText loginMail, loginPass;
    private Button btnLogin;
    private ProgressBar loginPb;
    private FirebaseAuth firebaseAuth;
    private ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseAuth = FirebaseAuth.getInstance();
        loginMail = findViewById(R.id.login_mail);
        loginPass = findViewById(R.id.login_pass);
        btnLogin = findViewById(R.id.login_btn);
        loginPb = findViewById(R.id.login_pb);
        iv = findViewById(R.id.login_img);


        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        loginPb.setVisibility(View.INVISIBLE);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnLogin.setVisibility(View.INVISIBLE);
                loginPb.setVisibility(View.VISIBLE);
                final String mail = loginMail.getText().toString();
                final String pass = loginPass.getText().toString();

                if (mail.isEmpty() || pass.isEmpty()) {
                    showMessage("Please Verify All filed");
                    btnLogin.setVisibility(View.VISIBLE);
                    loginPb.setVisibility(View.INVISIBLE);
                } else {
                    singInMail(mail, pass);
                }
            }
        });
    }

    private void singInMail(String mail, String pass) {
        firebaseAuth.signInWithEmailAndPassword(mail, pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            updateUI();
                        } else {
                            showMessage(task.getException().getMessage());
                            loginPb.setVisibility(View.INVISIBLE);
                            btnLogin.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void updateUI() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            updateUI();
        }
    }
}