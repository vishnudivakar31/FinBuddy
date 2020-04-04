package io.wanderingthinkter.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.transition.Slide;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;

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
    private static final int CREATE_BILL_CODE = 1;
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAnimation();
        setContentView(R.layout.activity_home);

        ViewPager2 viewPager = findViewById(R.id.homepage_viewpager);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(),
                getLifecycle());
        viewPager.setAdapter(viewPagerAdapter);

        tabLayout = findViewById(R.id.homepage_tab_layout);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    tab.setText(tabTextList[position]);
                    tab.setIcon(tabIconList[position]);
                })
                .attach();

        Button addItem = findViewById(R.id.homepage_add_item_button);
        addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this,
                        CreateBillActivity.class);
                ActivityOptions options =
                        ActivityOptions.makeSceneTransitionAnimation(HomeActivity.this);
                startActivityForResult(intent, CREATE_BILL_CODE, options.toBundle());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == CREATE_BILL_CODE) {
            TabLayout.Tab tab = tabLayout.getTabAt(tabLayout.getSelectedTabPosition());
            tabLayout.selectTab(tab);
        }
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
