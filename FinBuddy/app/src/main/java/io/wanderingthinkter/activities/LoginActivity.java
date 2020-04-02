package io.wanderingthinkter.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import io.wanderingthinkter.R;
import io.wanderingthinkter.models.CurrentUser;
import io.wanderingthinkter.models.UserModel;

import static io.wanderingthinkter.util.Constants.KEY_USER_ID;
import static io.wanderingthinkter.util.Constants.USER_COLLECTION;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText emailET, passwordET;
    private ProgressBar progressBar;

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection(USER_COLLECTION);
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button createAccountButton = findViewById(R.id.login_create_account_button);
        Button signInButton = findViewById(R.id.login_sign_in_button);
        emailET = findViewById(R.id.login_email);
        passwordET = findViewById(R.id.login_password);
        progressBar = findViewById(R.id.login_progressbar);
        Button forgotPasswordButton = findViewById(R.id.login_forgot_button);

        createAccountButton.setOnClickListener(this);
        signInButton.setOnClickListener(this);
        forgotPasswordButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_create_account_button:
                Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);

                ActivityOptions options =
                        ActivityOptions.makeSceneTransitionAnimation(this);
                startActivity(intent,options.toBundle());
                break;
            case R.id.login_sign_in_button:
                signInWithEmailAndPassword();
                break;
            case R.id.login_forgot_button:
                forgotPassword();
        }
    }

    private void forgotPassword() {
        auth.signOut();
        String email = emailET.getText().toString().trim();
        if(!TextUtils.isEmpty(email)) {
            progressBar.setVisibility(View.VISIBLE);
            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                showSnackBar(R.string.email_sent_txt);
                            } else {
                                showSnackBar(R.string.error_message);
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });
        } else {
            showSnackBar(R.string.email_mandatory);
        }
    }

    private void signInWithEmailAndPassword() {
        String email = emailET.getText().toString().trim();
        String password = passwordET.getText().toString().trim();
        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            progressBar.setVisibility(View.VISIBLE);
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                firebaseUser = auth.getCurrentUser();
                                assert firebaseUser != null;
                                collectionReference
                                        .whereEqualTo(KEY_USER_ID, firebaseUser.getUid())
                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                            @Override
                                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                                                @Nullable FirebaseFirestoreException e) {
                                                assert queryDocumentSnapshots != null;
                                                for(QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                                    UserModel userModel = snapshot.toObject(UserModel.class);
                                                    CurrentUser currentUser = CurrentUser.getInstance();
                                                    currentUser.setUsername(userModel.getName());
                                                    currentUser.setUserId(userModel.getUserId());
                                                    Intent intent = new Intent(LoginActivity.this,
                                                            CreateBillActivity.class);
                                                    ActivityOptions options =
                                                            ActivityOptions.makeSceneTransitionAnimation(LoginActivity.this);
                                                    startActivity(intent,options.toBundle());
                                                    progressBar.setVisibility(View.GONE);
                                                    finish();
                                                }
                                            }
                                        });
                            } else {
                                progressBar.setVisibility(View.GONE);
                                showSnackBar(R.string.error_message);
                            }
                        }
                    });
        } else {
            showSnackBar(R.string.all_fields_are_mandatory);
        }
    }

    private void showSnackBar(int textID) {
        Snackbar.make(findViewById(R.id.login_activity), textID,
                Snackbar.LENGTH_SHORT).show();
    }
}
