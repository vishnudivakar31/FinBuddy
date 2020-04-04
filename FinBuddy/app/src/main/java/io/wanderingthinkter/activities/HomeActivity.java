package io.wanderingthinkter.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.transition.Slide;
import android.view.animation.DecelerateInterpolator;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import io.wanderingthinkter.R;
import io.wanderingthinkter.ui.ViewPagerAdapter;

import static android.view.Gravity.RIGHT;

public class HomeActivity extends AppCompatActivity {

    private static final Integer[] tabTextList = new Integer[] {
            R.string.overview_txt,
            R.string.list_view,
            R.string.settings_txt
    };

    private static final Integer[] tabIconList = new Integer[] {
            R.drawable.ic_overview_white,
            R.drawable.ic_browse_white,
            R.drawable.ic_settings_white
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAnimation();
        setContentView(R.layout.activity_home);

        ViewPager2 viewPager = findViewById(R.id.homepage_viewpager);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(),
                getLifecycle());
        viewPager.setAdapter(viewPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.homepage_tab_layout);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    tab.setText(tabTextList[position]);
                    tab.setIcon(tabIconList[position]);
                })
                .attach();
    }

    public void setAnimation() {
        Slide slide = new Slide();
        slide.setSlideEdge(RIGHT);
        slide.setDuration(400);
        slide.setInterpolator(new DecelerateInterpolator());
        getWindow().setExitTransition(slide);
        getWindow().setEnterTransition(slide);
    }
}
