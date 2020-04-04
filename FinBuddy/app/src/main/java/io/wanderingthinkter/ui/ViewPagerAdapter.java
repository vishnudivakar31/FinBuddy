package io.wanderingthinkter.ui;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import io.wanderingthinkter.fragments.BillListFragment;
import io.wanderingthinkter.fragments.OverviewFragment;
import io.wanderingthinkter.fragments.SettingsFragment;


public class ViewPagerAdapter extends FragmentStateAdapter {


    public ViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new OverviewFragment();
            case 1: return new BillListFragment();
            case 2: return new SettingsFragment();
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
