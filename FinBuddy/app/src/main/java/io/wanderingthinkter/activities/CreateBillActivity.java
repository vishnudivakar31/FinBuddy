package io.wanderingthinkter.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import io.wanderingthinkter.R;
import io.wanderingthinkter.models.CurrentUser;

public class CreateBillActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_bill);

        CurrentUser user = CurrentUser.getInstance();
        Log.d("CurrUser", user.getUsername());
    }
}
