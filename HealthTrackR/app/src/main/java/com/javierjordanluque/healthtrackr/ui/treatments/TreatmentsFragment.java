package com.javierjordanluque.healthtrackr.ui.treatments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.ui.MainActivity;
import com.javierjordanluque.healthtrackr.ui.OnToolbarChangeListener;

public class TreatmentsFragment extends Fragment {
    private OnToolbarChangeListener listener;

    public TreatmentsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_treatments, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listener != null)
            listener.onTitleChanged(getString(R.string.treatments_title));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((MainActivity) requireActivity()).showBackButton(false);
        if (context instanceof OnToolbarChangeListener)
            listener = (OnToolbarChangeListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
