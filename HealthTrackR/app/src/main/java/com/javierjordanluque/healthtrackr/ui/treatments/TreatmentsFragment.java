package com.javierjordanluque.healthtrackr.ui.treatments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.models.Treatment;
import com.javierjordanluque.healthtrackr.models.User;
import com.javierjordanluque.healthtrackr.models.enumerations.TreatmentCategory;
import com.javierjordanluque.healthtrackr.ui.MainActivity;
import com.javierjordanluque.healthtrackr.ui.OnToolbarChangeListener;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.ExceptionManager;

import java.time.ZonedDateTime;
import java.util.List;

public class TreatmentsFragment extends Fragment {
    private OnToolbarChangeListener listener;
    private User user;
    private NestedScrollView nestedScrollView;
    private ConstraintLayout constraintLayoutEmpty;
    private LinearLayout linearLayout;

    public TreatmentsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_treatments, container, false);

        nestedScrollView = fragmentView.findViewById(R.id.nestedScrollView);
        constraintLayoutEmpty = fragmentView.findViewById(R.id.constraintLayoutEmpty);
        linearLayout = fragmentView.findViewById(R.id.linearLayout);

        FloatingActionButton buttonAddTreatment = fragmentView.findViewById(R.id.buttonAddTreatment);
        buttonAddTreatment.setOnClickListener(view -> {
            Intent intent = new Intent(requireActivity(), AddTreatmentActivity.class);
            startActivity(intent);
        });

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();

        user = ((MainActivity) requireActivity()).sessionViewModel.getUserSession();

        ((MainActivity) requireActivity()).currentFragment = this;
        ((MainActivity) requireActivity()).showBackButton(false);
        if (listener != null)
            listener.onTitleChanged(getString(R.string.treatments_app_bar_title));

        List<Treatment> treatments = null;
        try {
            treatments = user.getTreatments(requireActivity());
        } catch (DBFindException exception) {
            ExceptionManager.advertiseUI(requireActivity(), exception.getMessage());
        }

        linearLayout.removeAllViews();
        if (treatments == null || treatments.isEmpty()) {
            nestedScrollView.setVisibility(View.GONE);
            constraintLayoutEmpty.setVisibility(View.VISIBLE);
        } else {
            constraintLayoutEmpty.setVisibility(View.GONE);
            nestedScrollView.setVisibility(View.VISIBLE);

            boolean isFirst = true;
            for (Treatment treatment : treatments) {
                MaterialCardView cardView = (MaterialCardView) LayoutInflater.from(getContext()).inflate(R.layout.card_treatment, linearLayout, false);

                if (!isFirst) {
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) cardView.getLayoutParams();
                    layoutParams.topMargin = getResources().getDimensionPixelSize(R.dimen.form_margin_top);
                    cardView.setLayoutParams(layoutParams);
                } else {
                    isFirst = false;
                }

                TextView textViewTitle = cardView.findViewById(R.id.textViewTitle);
                textViewTitle.setText(treatment.getTitle());

                ZonedDateTime startDate = treatment.getStartDate();
                TextView textViewStartDate = cardView.findViewById(R.id.textViewStartDate);
                textViewStartDate.setText(((MainActivity) requireActivity()).showFormattedDate(startDate));

                ZonedDateTime endDate = treatment.getEndDate();
                TextView textViewEndDate = cardView.findViewById(R.id.textViewEndDate);
                if (endDate != null) {
                    textViewEndDate.setText(((MainActivity) requireActivity()).showFormattedDate(endDate));
                } else {
                    textViewEndDate.setText(R.string.unspecified);
                }

                TreatmentCategory treatmentCategory = treatment.getCategory();
                String[] categoryOptions = getResources().getStringArray(R.array.treatments_array_category);
                String categoryString = categoryOptions[treatmentCategory.ordinal()];
                TextView textViewCategory = cardView.findViewById(R.id.textViewCategory);
                textViewCategory.setText(categoryString);

                cardView.setOnClickListener(view -> {
                    Fragment fragment = new TreatmentFragment();
                    Bundle bundle = new Bundle();
                    bundle.putLong(Treatment.class.getSimpleName(), treatment.getId());
                    fragment.setArguments(bundle);
                    ((MainActivity) requireActivity()).replaceFragment(fragment);
                });

                linearLayout.addView(cardView);
            }
        }
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
