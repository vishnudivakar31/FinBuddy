package io.wanderingthinkter.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.transition.Slide;
import android.util.Log;
import android.view.animation.DecelerateInterpolator;

import io.wanderingthinkter.R;
import io.wanderingthinkter.models.CurrentUser;

import static android.view.Gravity.RIGHT;

public class CreateBillActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAnimation();
        setContentView(R.layout.activity_create_bill);

        CurrentUser user = CurrentUser.getInstance();
        Log.d("CurrUser", user.getUsername());
    }

    @SuppressLint("RtlHardcoded")
    public void setAnimation() {
        Slide slide = new Slide();
        slide.setSlideEdge(RIGHT);
        slide.setDuration(400);
        slide.setInterpolator(new DecelerateInterpolator());
        getWindow().setExitTransition(slide);
        getWindow().setEnterTransition(slide);
    }
}
