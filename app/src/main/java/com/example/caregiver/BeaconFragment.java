package com.example.caregiver;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BeaconFragment extends Fragment {


    ViewPager viewPager;
    TabLayout tabLayout;


    public BeaconFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ProfileFragment.
     */
    public static BeaconFragment newInstance() {
        BeaconFragment fragment = new BeaconFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_beacon, container, false);

        tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        viewPager = (ViewPager) view.findViewById(R.id.view_pager);
        prepareViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        return view;
    }

    private void prepareViewPager(ViewPager viewPager) {
        BeaconAdapter adapter = new BeaconAdapter(getFragmentManager());
        viewPager.setAdapter(adapter);
    }

    private class BeaconAdapter extends FragmentPagerAdapter {
        public BeaconAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    BeaconRegionList regionListFragment = new BeaconRegionList();
                    return regionListFragment;
                case 1:
                    BeaconAddRegion addRegionFragment = new BeaconAddRegion();
                    return addRegionFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Regions";
                case 1:
                    return "Add Region";
                default:
                    return null;
            }
        }
    }

}
