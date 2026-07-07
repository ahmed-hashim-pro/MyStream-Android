package com.medoapps.www.onlinequran.onboarding;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.medoapps.www.onlinequran.R;

import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

/** The «choose your mushaf» step: a snap carousel of the seven print page sets. */
public class OnboardingMushafFragment extends Fragment {

    /** One selectable print. Sizes are the 1260-width bucket most phones download. */
    private static class Print {
        final String key;
        final int titleRes;
        final int descRes;
        final int sizeMb;
        final int chipRes; // 0 = no chip

        Print(String key, int titleRes, int descRes, int sizeMb, int chipRes) {
            this.key = key;
            this.titleRes = titleRes;
            this.descRes = descRes;
            this.sizeMb = sizeMb;
            this.chipRes = chipRes;
        }
    }

    private static final Print[] PRINTS = {
            new Print("madani", R.string.madani_title, R.string.madani_description,
                    77, R.string.onb_print_chip_default),
            new Print("new_madani", R.string.new_madani_title, R.string.new_madani_description,
                    96, 0),
            new Print("tajweed", R.string.tajweed_title, R.string.tajweed_description,
                    137, R.string.onb_print_chip_colored),
            new Print("naskh", R.string.naskh_title, R.string.naskh_description,
                    124, 0),
            new Print("shemerly", R.string.shemerly_title, R.string.shemerly_description,
                    114, 0),
            new Print("qaloon", R.string.qaloon_title, R.string.qaloon_description,
                    101, R.string.onb_print_chip_riwaya),
            new Print("warsh", R.string.warsh_title, R.string.warsh_description,
                    225, R.string.onb_print_chip_riwaya),
            new Print("jalala", R.string.jalala_title, R.string.jalala_description,
                    290, R.string.onb_print_chip_colored),
    };

    private OnboardingHost host;
    private RecyclerView carousel;
    private LinearLayout dotsLayout;
    private TextView nameView, metaView, chipView;
    private PrintAdapter adapter;

    // the shelf loops like a carousel: the adapter repeats the prints many
    // times and starts in the middle, so both directions wrap seamlessly
    private static final int LOOP_COUNT = PRINTS.length * 1000;
    private static final int LOOP_START = (LOOP_COUNT / 2) - ((LOOP_COUNT / 2) % PRINTS.length);

    /** Absolute adapter position of the selected card; print = position % PRINTS.length. */
    private int selectedIndex = LOOP_START;

    private static int printIndex(int position) {
        return position % PRINTS.length;
    }

    private Print selectedPrint() {
        return PRINTS[printIndex(selectedIndex)];
    }

    private final Map<String, Bitmap> previewCache = new HashMap<>();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        host = (OnboardingHost) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_onboarding_mushaf, container, false);

        nameView = v.findViewById(R.id.onb_print_name);
        metaView = v.findViewById(R.id.onb_print_meta);
        chipView = v.findViewById(R.id.onb_print_chip);
        dotsLayout = v.findViewById(R.id.onb_print_dots);
        carousel = v.findViewById(R.id.onb_print_carousel);

        selectedIndex = LOOP_START + indexOfKey(host.getOnboardingState().pageType);
        // commit the initially shown card so the displayed selection is always
        // the one that gets applied, even if the user never touches the shelf
        host.getOnboardingState().pageType = selectedPrint().key;

        adapter = new PrintAdapter();
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false);
        carousel.setLayoutManager(layoutManager);
        carousel.setAdapter(adapter);
        // selection just swaps the ring + badge; a change cross-fade would pull
        // pooled holders in with stale scale/alpha from their side-card days
        carousel.setItemAnimator(null);

        // LinearSnapHelper gives the carousel feel: a fling glides over several
        // cards and settles on the nearest, instead of one sticky page per swipe
        LinearSnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(carousel);

        // resolved here: the fragment is guaranteed attached in onCreateView,
        // while the posted lambda may run after detach (222dp = item card width)
        final int cardWidth = Math.round(222 * getResources().getDisplayMetrics().density);

        // center the snapped card: symmetric padding of (width - card) / 2.
        // The scroll listener is only registered AFTER the padding + initial
        // centering land - the first offscreen layout dispatches onScrolled(0,0)
        // against the unpadded center, and on wide screens its snap view would
        // clobber the seeded selection before this lambda ever runs.
        carousel.post(() -> {
            if (carousel.getWidth() == 0) {
                return;
            }
            int pad = Math.max(0, (carousel.getWidth() - cardWidth) / 2);
            carousel.setPadding(pad, 0, pad, 0);
            layoutManager.scrollToPositionWithOffset(selectedIndex, 0);
            carousel.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                    applyCarouselTransforms();
                    View snapped = snapHelper.findSnapView(layoutManager);
                    if (snapped != null) {
                        int position = layoutManager.getPosition(snapped);
                        if (position != RecyclerView.NO_POSITION && position != selectedIndex) {
                            setSelected(position);
                        }
                    }
                }
            });
            carousel.post(this::applyCarouselTransforms);
        });

        buildDots();
        bindDetails();

        v.findViewById(R.id.onb_print_continue).setOnClickListener(view -> host.goToNextPage());
        return v;
    }

    @Override
    public void onDestroyView() {
        // no recycle(): a page transition may still be drawing these; GC reclaims them
        previewCache.clear();
        super.onDestroyView();
    }

    private int indexOfKey(String key) {
        for (int i = 0; i < PRINTS.length; i++) {
            if (PRINTS[i].key.equals(key)) {
                return i;
            }
        }
        return 0;
    }

    private void setSelected(int position) {
        final int previous = selectedIndex;
        selectedIndex = position;
        host.getOnboardingState().pageType = PRINTS[printIndex(position)].key;
        // adapter mutation is unsupported inside a scroll callback - rebind on
        // the next frame; the details/dots don't touch the adapter and can stay live
        carousel.post(() -> {
            adapter.notifyItemChanged(previous);
            adapter.notifyItemChanged(position);
        });
        bindDetails();
        highlightDot(printIndex(position));
    }

    private void bindDetails() {
        Print print = selectedPrint();
        nameView.setText(print.titleRes);
        String size = getString(R.string.onb_print_size,
                NumberFormat.getInstance().format(print.sizeMb));
        metaView.setText(getString(print.descRes) + " · " + size);
        if (print.chipRes != 0) {
            chipView.setVisibility(View.VISIBLE);
            chipView.setText(print.chipRes);
        } else {
            chipView.setVisibility(View.INVISIBLE); // keep the row height stable
        }
    }

    /** The selected card clearly dominates: side cards drop to ~65% and fade hard. */
    private void applyCarouselTransforms() {
        float center = carousel.getWidth() / 2f;
        for (int i = 0; i < carousel.getChildCount(); i++) {
            View child = carousel.getChildAt(i);
            float childCenter = (child.getLeft() + child.getRight()) / 2f;
            float offset = childCenter - center;
            float distance = Math.min(1f, Math.abs(offset) / center);
            float scale = 1f - 0.35f * distance;
            child.setScaleX(scale);
            child.setScaleY(scale);
            child.setAlpha(1f - 0.55f * distance);
            // scaling shrinks the card inside its layout slot, leaving a hole
            // next to the centered card - slide it inward by the width it lost
            float inward = child.getWidth() * (1f - scale) / 2f;
            child.setTranslationX(offset > 0 ? -inward : (offset < 0 ? inward : 0f));
        }
    }

    private void buildDots() {
        dotsLayout.removeAllViews();
        int pad = Math.round(5 * getResources().getDisplayMetrics().density);
        for (int i = 0; i < PRINTS.length; i++) {
            TextView dot = new TextView(requireContext());
            dot.setText(Html.fromHtml("&#8226;"));
            dot.setTextSize(22);
            dot.setPadding(pad, 0, pad, 0);
            dotsLayout.addView(dot);
        }
        highlightDot(printIndex(selectedIndex));
    }

    private void highlightDot(int position) {
        for (int i = 0; i < dotsLayout.getChildCount(); i++) {
            ((TextView) dotsLayout.getChildAt(i)).setTextColor(getResources().getColor(
                    i == position ? R.color.onb_accent_end : R.color.onb_dot_inactive));
        }
    }

    @Nullable
    private Bitmap previewFor(String key) {
        Bitmap cached = previewCache.get(key);
        if (cached != null) {
            return cached;
        }
        try (InputStream in = requireContext().getAssets().open("onboarding/prints/" + key + ".webp")) {
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            if (bitmap != null) {
                previewCache.put(key, bitmap);
            }
            return bitmap;
        } catch (IOException e) {
            return null;
        }
    }

    // ---- carousel adapter ----

    private class PrintAdapter extends RecyclerView.Adapter<PrintAdapter.Holder> {

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View item = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_onboarding_print, parent, false);
            return new Holder(item);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            // pooled holders keep the scale/alpha/shift from wherever they last
            // sat on the shelf; reset and let the scroll transform re-apply
            holder.itemView.setScaleX(1f);
            holder.itemView.setScaleY(1f);
            holder.itemView.setAlpha(1f);
            holder.itemView.setTranslationX(0f);
            Print print = PRINTS[printIndex(position)];
            holder.name.setText(print.titleRes);
            holder.image.setImageBitmap(previewFor(print.key));
            boolean selected = position == selectedIndex;
            holder.card.setBackgroundResource(selected
                    ? R.drawable.bg_onboarding_card_selected : R.drawable.bg_onboarding_card);
            holder.badge.setVisibility(selected ? View.VISIBLE : View.GONE);
            holder.itemView.setOnClickListener(v -> centerOn(holder.getBindingAdapterPosition()));
        }

        @Override
        public int getItemCount() {
            return LOOP_COUNT;
        }

        private void centerOn(int position) {
            if (position == RecyclerView.NO_POSITION) {
                return;
            }
            RecyclerView.ViewHolder holder = carousel.findViewHolderForAdapterPosition(position);
            if (holder != null) {
                View item = holder.itemView;
                float itemCenter = (item.getLeft() + item.getRight()) / 2f;
                carousel.smoothScrollBy(Math.round(itemCenter - carousel.getWidth() / 2f), 0);
            } else {
                carousel.smoothScrollToPosition(position);
            }
        }

        class Holder extends RecyclerView.ViewHolder {
            final View card;
            final ImageView image;
            final TextView name;
            final View badge;

            Holder(View itemView) {
                super(itemView);
                card = itemView.findViewById(R.id.onb_print_card);
                image = itemView.findViewById(R.id.onb_print_image);
                name = itemView.findViewById(R.id.onb_print_card_name);
                badge = itemView.findViewById(R.id.onb_print_badge);
            }
        }
    }
}
