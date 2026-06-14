package com.medoapps.www.onlinequran.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.medoapps.www.onlinequran.R;

public class OnboardingTourFragment extends Fragment {

    private static final String ARG_ART = "art";
    private static final String ARG_TITLE = "title";
    private static final String ARG_DESC = "desc";
    private static final String ARG_OFFLINE = "offline";

    public static OnboardingTourFragment newInstance(int artRes, int titleRes, int descRes, boolean offline) {
        OnboardingTourFragment f = new OnboardingTourFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_ART, artRes);
        b.putInt(ARG_TITLE, titleRes);
        b.putInt(ARG_DESC, descRes);
        b.putBoolean(ARG_OFFLINE, offline);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_onboarding_tour, container, false);
        Bundle args = requireArguments();

        ((android.widget.ImageView) v.findViewById(R.id.onb_tour_art))
                .setImageResource(args.getInt(ARG_ART));
        ((android.widget.TextView) v.findViewById(R.id.onb_tour_title))
                .setText(args.getInt(ARG_TITLE));
        ((android.widget.TextView) v.findViewById(R.id.onb_tour_desc))
                .setText(args.getInt(ARG_DESC));
        v.findViewById(R.id.onb_tour_offline)
                .setVisibility(args.getBoolean(ARG_OFFLINE) ? View.VISIBLE : View.GONE);
        return v;
    }
}
