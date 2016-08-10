package me.kaede.feya.home;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by Kaede on 16/8/10.
 */
public class DemoPagerAdapter extends FragmentStatePagerAdapter {

    public DemoPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return DemoListFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return DemoProvider.demos.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return DemoProvider.demos.keyAt(position);
    }
}
