package com.example.caregiver;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BeaconFragment extends Fragment {

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

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        ViewPager viewPager = view.findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(0);
        prepareViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        return view;
    }

    private void prepareViewPager(ViewPager viewPager) {
        BeaconAdapter adapter = new BeaconAdapter(getFragmentManager());
        viewPager.setAdapter(adapter);
    }

    private class BeaconAdapter extends FragmentStatePagerAdapter {
        public BeaconAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            BeaconRegionList regionListFragment = new BeaconRegionList();
            BeaconAddRegion addRegionFragment = new BeaconAddRegion();
            switch (position) {
                case 1:
                    return addRegionFragment;
                default:
                    return regionListFragment;
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
                case 1:
                    return "Add Region";
                default:
                    return "Regions";
            }
        }
    }

}
