package io.wanderingthinkter.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import io.wanderingthinkter.R;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button createAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        createAccountButton = findViewById(R.id.login_create_account_button);

        createAccountButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_create_account_button:
                Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);

                if(Build.VERSION.SDK_INT>20){
                    ActivityOptions options =
                            ActivityOptions.makeSceneTransitionAnimation(this);
                    startActivity(intent,options.toBundle());
                }else {
                    startActivity(intent);
                }
        }
    }
}
