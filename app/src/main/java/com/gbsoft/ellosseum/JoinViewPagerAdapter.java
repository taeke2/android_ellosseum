package com.gbsoft.ellosseum;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

public class JoinViewPagerAdapter extends FragmentStateAdapter {

    private ArrayList<Fragment> mList;

    public JoinViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, ArrayList<Fragment> list) {
        super(fragmentActivity);
        this.mList = list;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return mList.get(position);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}
