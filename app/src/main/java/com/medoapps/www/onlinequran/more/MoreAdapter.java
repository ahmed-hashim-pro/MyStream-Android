package com.medoapps.www.onlinequran.more;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.medoapps.www.onlinequran.R;

import java.util.ArrayList;
import java.util.List;

/**
 * One RecyclerView, four view types. See design/specs/more-page.md sec.1.
 *
 * Rows and headers take the full span, tiles take a quarter — that mixed-span grid
 * is the whole point of the design, and the reason the old single-view-type
 * CategoryAdapter could not be extended.
 */
public class MoreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int SPAN_COUNT = 4;

    static final int TYPE_CONTEXT = 0;
    static final int TYPE_HEADER = 1;
    static final int TYPE_ROW = 2;
    static final int TYPE_TILE = 3;

    /** Emitted upward; the adapter never navigates or reads settings itself. */
    public interface Listener {
        void onEntryClicked(MoreUiState.Entry entry);
        void onContextCardClicked();
    }

    /** Flattened render list: the adapter draws this, never the grouped state. */
    private static final class Item {
        final int type;
        @Nullable final MoreUiState.Entry entry;
        final int headerRes;
        @Nullable final MoreUiState.ContextCard card;

        Item(int type, @Nullable MoreUiState.Entry entry, int headerRes,
             @Nullable MoreUiState.ContextCard card) {
            this.type = type;
            this.entry = entry;
            this.headerRes = headerRes;
            this.card = card;
        }
    }

    private final List<Item> items = new ArrayList<>();
    private final Listener listener;

    public MoreAdapter(Listener listener) {
        this.listener = listener;
        setHasStableIds(false);
    }

    public void submit(MoreUiState state) {
        items.clear();
        if (state.contextCard != null) {
            items.add(new Item(TYPE_CONTEXT, null, 0, state.contextCard));
        }
        for (MoreUiState.Group g : state.groups) {
            items.add(new Item(TYPE_HEADER, null, g.titleRes, null));
            for (MoreUiState.Entry e : g.entries) {
                items.add(new Item(
                        e.shape == MoreUiState.Shape.ROW ? TYPE_ROW : TYPE_TILE, e, 0, null));
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Full span for everything except tiles. Tiles drop to 3-up at large font scales
     * so a two-line label does not clip (spec sec.5, large font).
     */
    public GridLayoutManager.SpanSizeLookup spanSizeLookup(final Context context) {
        return new GridLayoutManager.SpanSizeLookup() {
            @Override public int getSpanSize(int position) {
                if (position < 0 || position >= items.size()) return SPAN_COUNT;
                if (items.get(position).type != TYPE_TILE) return SPAN_COUNT;
                return context.getResources().getConfiguration().fontScale >= 1.3f
                        ? SPAN_COUNT / 3 + (SPAN_COUNT % 3 == 0 ? 0 : 1)
                        : 1;
            }
        };
    }

    boolean isRow(int position) {
        return position >= 0 && position < items.size() && items.get(position).type == TYPE_ROW;
    }

    boolean isTile(int position) {
        return position >= 0 && position < items.size() && items.get(position).type == TYPE_TILE;
    }

    @Override public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @Override public int getItemCount() {
        return items.size();
    }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater i = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_CONTEXT: return new ContextVH(i.inflate(R.layout.item_more_context, parent, false));
            case TYPE_HEADER:  return new HeaderVH(i.inflate(R.layout.item_more_header, parent, false));
            case TYPE_ROW:     return new RowVH(i.inflate(R.layout.item_more_row, parent, false));
            default:           return new TileVH(i.inflate(R.layout.item_more_tile, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Item item = items.get(position);
        if (holder instanceof ContextVH) {
            ((ContextVH) holder).bind(item.card, listener);
        } else if (holder instanceof HeaderVH) {
            ((HeaderVH) holder).bind(item.headerRes);
        } else if (holder instanceof RowVH) {
            ((RowVH) holder).bind(item.entry, listener);
        } else if (holder instanceof TileVH) {
            ((TileVH) holder).bind(item.entry, listener);
        }
    }

    // ------------------------------------------------------------- holders

    static final class ContextVH extends RecyclerView.ViewHolder {
        final TextView title, subtitle, time;
        final ImageView chevron;

        ContextVH(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.moreContextTitle);
            subtitle = v.findViewById(R.id.moreContextSubtitle);
            time = v.findViewById(R.id.moreContextTime);
            chevron = v.findViewById(R.id.moreContextChevron);
        }

        void bind(@Nullable MoreUiState.ContextCard card, Listener l) {
            if (card == null) return;
            title.setText(card.prayerName);
            subtitle.setText(card.cityText);
            subtitle.setVisibility(TextUtils.isEmpty(card.cityText) ? View.GONE : View.VISIBLE);
            // no time to show in the needs-location variant; a chevron invites the fix instead
            time.setText(card.timeText);
            time.setVisibility(TextUtils.isEmpty(card.timeText) ? View.GONE : View.VISIBLE);
            chevron.setVisibility(card.needsLocation ? View.VISIBLE : View.GONE);
            itemView.setContentDescription(card.prayerName + " " + card.cityText + " " + card.timeText);
            itemView.setOnClickListener(v -> l.onContextCardClicked());
        }
    }

    static final class HeaderVH extends RecyclerView.ViewHolder {
        final TextView label;

        HeaderVH(@NonNull View v) {
            super(v);
            label = v.findViewById(R.id.moreHeaderLabel);
        }

        void bind(int titleRes) {
            label.setText(titleRes);
        }
    }

    static final class RowVH extends RecyclerView.ViewHolder {
        final ImageView icon, chevron;
        final TextView title, subtitle, state;

        RowVH(@NonNull View v) {
            super(v);
            icon = v.findViewById(R.id.moreRowIcon);
            title = v.findViewById(R.id.moreRowTitle);
            subtitle = v.findViewById(R.id.moreRowSubtitle);
            state = v.findViewById(R.id.moreRowState);
            chevron = v.findViewById(R.id.moreRowChevron);
        }

        void bind(@Nullable MoreUiState.Entry e, Listener l) {
            if (e == null) return;
            icon.setImageResource(e.iconRes);
            title.setText(e.titleRes);

            boolean hasSub = !TextUtils.isEmpty(e.subtitle);
            subtitle.setText(hasSub ? e.subtitle : "");
            subtitle.setVisibility(hasSub ? View.VISIBLE : View.GONE);

            boolean hasState = !TextUtils.isEmpty(e.state);
            state.setText(hasState ? e.state : "");
            state.setVisibility(hasState ? View.VISIBLE : View.GONE);
            // the chevron is the fallback affordance: show it only when no state
            // occupies that slot, so the two never collide
            chevron.setVisibility(hasState ? View.GONE : View.VISIBLE);

            itemView.setContentDescription(itemView.getContext().getString(e.titleRes)
                    + (hasState ? ", " + e.state : "") + (hasSub ? ", " + e.subtitle : ""));
            itemView.setOnClickListener(v -> l.onEntryClicked(e));
        }
    }

    static final class TileVH extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView label, state;

        TileVH(@NonNull View v) {
            super(v);
            icon = v.findViewById(R.id.moreTileIcon);
            label = v.findViewById(R.id.moreTileLabel);
            state = v.findViewById(R.id.moreTileState);
        }

        void bind(@Nullable MoreUiState.Entry e, Listener l) {
            if (e == null) return;
            icon.setImageResource(e.iconRes);
            label.setText(e.titleRes);

            boolean hasState = !TextUtils.isEmpty(e.state);
            state.setText(hasState ? e.state : "");
            state.setVisibility(hasState ? View.VISIBLE : View.GONE);

            itemView.setContentDescription(itemView.getContext().getString(e.titleRes)
                    + (hasState ? ", " + e.state : ""));
            itemView.setOnClickListener(v -> l.onEntryClicked(e));
        }
    }
}
