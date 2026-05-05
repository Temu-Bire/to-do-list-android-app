package com.example.to_dolist.ui;

import android.view.View;
import android.widget.AdapterView;

public class SimpleItemSelectedListener implements AdapterView.OnItemSelectedListener {

    public interface OnSelectionChanged {
        void onChanged();
    }

    private final OnSelectionChanged callback;

    public SimpleItemSelectedListener(OnSelectionChanged callback) {
        this.callback = callback;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        callback.onChanged();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // No-op
    }
}
