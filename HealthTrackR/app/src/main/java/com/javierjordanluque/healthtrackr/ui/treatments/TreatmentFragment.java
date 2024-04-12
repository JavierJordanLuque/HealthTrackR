package com.javierjordanluque.healthtrackr.ui.treatments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.models.Treatment;
import com.javierjordanluque.healthtrackr.models.User;
import com.javierjordanluque.healthtrackr.ui.MainActivity;
import com.javierjordanluque.healthtrackr.ui.OnToolbarChangeListener;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.ExceptionManager;

public class TreatmentFragment extends Fragment {
    private OnToolbarChangeListener listener;
    private Treatment treatment;
    private TextView textView;

    public TreatmentFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_treatment, container, false);

        textView = fragmentView.findViewById(R.id.textView);

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();

        ((MainActivity) requireActivity()).currentFragment = this;
        User user = ((MainActivity) requireActivity()).sessionViewModel.getUserSession();
        if (user != null) {
            Bundle bundle = getArguments();
            if (bundle != null) {
                long treatmentId = bundle.getLong(Treatment.class.getSimpleName());
                try {
                    for (Treatment treatment : user.getTreatments(requireActivity())) {
                        if (treatment.getId() == treatmentId) {
                            this.treatment = treatment;
                            break;
                        }
                    }
                } catch (DBFindException exception) {
                    ExceptionManager.advertiseUI(requireActivity(), exception.getMessage());
                }
            }
        }
        ((MainActivity) requireActivity()).showBackButton(true);

        if (listener != null)
            listener.onTitleChanged(getString(R.string.treatments_title));

        textView.setText(treatment.getTitle());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnToolbarChangeListener)
            listener = (OnToolbarChangeListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}