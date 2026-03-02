package com.medoapps.www.onlinequran.util;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.medoapps.www.onlinequran.R;

public class AppBottomSheet {

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(int position);
    }

    private static BottomSheetDialog createStyledDialog(Context context) {
        if (context instanceof Activity && ((Activity) context).isFinishing()) {
            return null;
        }
        return new BottomSheetDialog(context, R.style.AppBottomSheetDialog);
    }

    /**
     * Confirmation dialog with positive/negative buttons.
     */
    public static BottomSheetDialog showConfirmation(Context context,
                                                      CharSequence title,
                                                      CharSequence message,
                                                      CharSequence positiveText,
                                                      CharSequence negativeText,
                                                      Runnable onPositive,
                                                      Runnable onNegative) {
        BottomSheetDialog dialog = createStyledDialog(context);
        if (dialog == null) return null;

        View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_confirm, null);
        TextView tvTitle = view.findViewById(R.id.bs_title);
        TextView tvMessage = view.findViewById(R.id.bs_message);
        MaterialButton btnPositive = view.findViewById(R.id.bs_btn_positive);
        MaterialButton btnNegative = view.findViewById(R.id.bs_btn_negative);

        tvTitle.setText(title);
        tvMessage.setText(message);
        btnPositive.setText(positiveText);
        btnNegative.setText(negativeText);

        btnPositive.setOnClickListener(v -> {
            dialog.dismiss();
            if (onPositive != null) onPositive.run();
        });
        btnNegative.setOnClickListener(v -> {
            dialog.dismiss();
            if (onNegative != null) onNegative.run();
        });

        dialog.setContentView(view);
        dialog.show();
        return dialog;
    }

    /**
     * Simple message dialog with OK button only.
     */
    public static BottomSheetDialog showMessage(Context context,
                                                 CharSequence title,
                                                 CharSequence message) {
        BottomSheetDialog dialog = createStyledDialog(context);
        if (dialog == null) return null;

        View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_confirm, null);
        TextView tvTitle = view.findViewById(R.id.bs_title);
        TextView tvMessage = view.findViewById(R.id.bs_message);
        MaterialButton btnPositive = view.findViewById(R.id.bs_btn_positive);
        MaterialButton btnNegative = view.findViewById(R.id.bs_btn_negative);

        tvTitle.setText(title);
        tvMessage.setText(message);
        btnPositive.setText(android.R.string.ok);
        btnNegative.setVisibility(View.GONE);

        btnPositive.setOnClickListener(v -> dialog.dismiss());

        dialog.setContentView(view);
        dialog.show();
        return dialog;
    }

    /**
     * List selection dialog.
     */
    public static BottomSheetDialog showList(Context context,
                                              CharSequence title,
                                              CharSequence[] items,
                                              OnItemClickListener onItemClick,
                                              OnItemLongClickListener onItemLongClick) {
        BottomSheetDialog dialog = createStyledDialog(context);
        if (dialog == null) return null;

        View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_list, null);
        TextView tvTitle = view.findViewById(R.id.bs_list_title);
        ListView listView = view.findViewById(R.id.bs_list_view);
        MaterialButton btnCancel = view.findViewById(R.id.bs_btn_cancel);

        tvTitle.setText(title);

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, v, position, id) -> {
            if (onItemClick != null) onItemClick.onItemClick(position);
        });

        if (onItemLongClick != null) {
            listView.setOnItemLongClickListener((parent, v, position, id) ->
                    onItemLongClick.onItemLongClick(position));
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.setContentView(view);
        dialog.show();
        return dialog;
    }

    /**
     * List selection dialog with a custom ArrayAdapter (e.g. for styled items).
     */
    public static BottomSheetDialog showListWithAdapter(Context context,
                                                         CharSequence title,
                                                         ArrayAdapter<?> adapter,
                                                         AdapterView.OnItemClickListener onItemClick) {
        BottomSheetDialog dialog = createStyledDialog(context);
        if (dialog == null) return null;

        View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_list, null);
        TextView tvTitle = view.findViewById(R.id.bs_list_title);
        ListView listView = view.findViewById(R.id.bs_list_view);
        MaterialButton btnCancel = view.findViewById(R.id.bs_btn_cancel);

        tvTitle.setText(title);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(onItemClick);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.setContentView(view);
        dialog.show();
        return dialog;
    }

    /**
     * Custom dialog with icon, title, message, and two buttons.
     */
    public static BottomSheetDialog showCustom(Context context,
                                                CharSequence title,
                                                CharSequence message,
                                                CharSequence positiveText,
                                                CharSequence negativeText,
                                                Runnable onPositive,
                                                Runnable onNegative,
                                                int iconResId) {
        BottomSheetDialog dialog = createStyledDialog(context);
        if (dialog == null) return null;

        View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_confirm, null);
        TextView tvTitle = view.findViewById(R.id.bs_title);
        TextView tvMessage = view.findViewById(R.id.bs_message);
        ImageView icon = view.findViewById(R.id.bs_icon);
        MaterialButton btnPositive = view.findViewById(R.id.bs_btn_positive);
        MaterialButton btnNegative = view.findViewById(R.id.bs_btn_negative);

        tvTitle.setText(title);
        tvMessage.setText(message);

        icon.setImageResource(iconResId);
        icon.setVisibility(View.VISIBLE);

        btnPositive.setText(positiveText);
        btnNegative.setText(negativeText);

        btnPositive.setOnClickListener(v -> {
            dialog.dismiss();
            if (onPositive != null) onPositive.run();
        });
        btnNegative.setOnClickListener(v -> {
            dialog.dismiss();
            if (onNegative != null) onNegative.run();
        });

        dialog.setContentView(view);
        dialog.show();
        return dialog;
    }
}
