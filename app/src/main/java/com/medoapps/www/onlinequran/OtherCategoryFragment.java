package com.medoapps.www.onlinequran;

import static com.medoapps.www.onlinequran.R.id.adView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdView;
import com.medoapps.www.onlinequran.more.MoreAdapter;
import com.medoapps.www.onlinequran.more.MoreItemDecoration;
import com.medoapps.www.onlinequran.more.MoreUiState;
import com.medoapps.www.onlinequran.more.MoreViewModel;

/**
 * The More page. See design/specs/more-page.md and design/mockups/more-page.html.
 *
 * View layer only: it renders {@link MoreUiState} and forwards events to
 * {@link MoreViewModel}. It reads no settings and computes no state of its own.
 */
public class OtherCategoryFragment extends Fragment implements MoreAdapter.Listener {

    public static final String PAGE_TITLE = "Tab2";
    static OtherCategoryFragment instance4;

    private AdView mAdView;
    private MoreViewModel viewModel;
    private MoreAdapter adapter;
    private HeroController hero;

    private RecyclerView list;
    private View empty;
    private TextView emptyText;

    public static OtherCategoryFragment newInstance() {
        return new OtherCategoryFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_other_category_fragment, container, false);

        // preserved side effects from the previous implementation
        new SettingSaved(getContext()).LoadData();
        instance4 = this;
        SettingSaved.IsOpen = 1;
        SettingSaved.SounlLoad = 1;

        // Kept per spec Q8: the banner stays wired but is never asked to load.
        mAdView = view.findViewById(adView);
        if (mAdView != null) {
            mAdView.setAdListener(new AdListener() {
                @Override public void onAdLoaded() {
                    mAdView.setVisibility(View.VISIBLE);
                }
            });
        }

        viewModel = new ViewModelProvider(this).get(MoreViewModel.class);

        setupHero(view);
        setupList(view);

        viewModel.state().observe(getViewLifecycleOwner(), this::render);
        return view;
    }

    private void setupHero(View root) {
        // apply() is the terminal call — title()/avatar() only stage values.
        // No back arrow: this is a bottom-nav root, not a detail screen.
        hero = HeroController.attach(root, getActivity())
                .avatar(R.mipmap.ic_launcher_new_transparent9)
                .title(R.string.more_tab_header)
                .action(R.drawable.outline_emoji_events_24, () -> {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).showRewardedAd();
                    }
                })
                .action(R.drawable.ic_settings_gear,
                        () -> startActivity(new Intent(getContext(), Settings.class)))
                .search(query -> viewModel.onQueryChanged(query));
        hero.apply();
    }

    private void setupList(View root) {
        list = root.findViewById(R.id.listView);
        empty = root.findViewById(R.id.moreEmpty);
        emptyText = root.findViewById(R.id.moreEmptyText);

        adapter = new MoreAdapter(this);
        GridLayoutManager lm = new GridLayoutManager(requireContext(), MoreAdapter.SPAN_COUNT);
        lm.setSpanSizeLookup(adapter.spanSizeLookup(requireContext()));
        list.setLayoutManager(lm);
        list.addItemDecoration(new MoreItemDecoration(requireContext(), adapter));
        list.setAdapter(adapter);

        root.findViewById(R.id.moreEmptyClear).setOnClickListener(v -> {
            // HeroController owns the SearchView; clear it directly so the field and
            // the state holder cannot disagree about whether a query is active.
            android.widget.SearchView search = root.findViewById(R.id.heroSearch);
            if (search != null) {
                search.setQuery("", false);
                search.setIconified(true);
            }
            viewModel.onSearchDismissed();
        });
    }

    private void render(MoreUiState state) {
        adapter.submit(state);
        boolean searchEmpty = state.isSearchEmpty();
        empty.setVisibility(searchEmpty ? View.VISIBLE : View.GONE);
        list.setVisibility(searchEmpty ? View.GONE : View.VISIBLE);
        if (searchEmpty) {
            emptyText.setText(getString(R.string.more_search_empty, state.query));
        }
    }

    // ------------------------------------------------------ adapter callbacks

    @Override
    public void onEntryClicked(MoreUiState.Entry entry) {
        if (entry.opensLiveList) {
            // Live Streaming swaps a fragment into this screen's own container —
            // @id/EntireLayoutCategory must survive any redesign of the layout.
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.EntireLayoutCategory, new LiveList(), "liveListFragment")
                    .addToBackStack("liveListFragmentBAck")
                    .commit();
        } else if (entry.destination != null) {
            startActivity(new Intent(getContext(), entry.destination));
        }
    }

    @Override
    public void onContextCardClicked() {
        startActivity(new Intent(getContext(), PrayerTimesActivity.class));
    }

    // ------------------------------------------------------------- lifecycle

    @Override
    public void onResume() {
        super.onResume();
        viewModel.onScreenResumed();
    }

    @Override
    public void onPause() {
        viewModel.onScreenPaused();
        super.onPause();
    }
}
