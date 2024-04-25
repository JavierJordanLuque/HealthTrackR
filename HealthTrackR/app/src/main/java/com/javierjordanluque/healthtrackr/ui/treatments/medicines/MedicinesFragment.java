package com.javierjordanluque.healthtrackr.ui.treatments.medicines;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.models.Medicine;
import com.javierjordanluque.healthtrackr.models.Treatment;
import com.javierjordanluque.healthtrackr.models.enumerations.AdministrationRoute;
import com.javierjordanluque.healthtrackr.ui.MainActivity;
import com.javierjordanluque.healthtrackr.ui.OnToolbarChangeListener;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.ExceptionManager;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

public class MedicinesFragment extends Fragment {
    private OnToolbarChangeListener listener;
    private Treatment treatment;
    private ImageView imageViewStatus;
    private TextView textViewStatus;
    private NestedScrollView nestedScrollView;
    private ConstraintLayout constraintLayoutNoElements;
    private LinearLayout linearLayout;

    public MedicinesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_medicines, container, false);

        imageViewStatus = fragmentView.findViewById(R.id.imageViewStatus);
        textViewStatus = fragmentView.findViewById(R.id.textViewStatus);

        nestedScrollView = fragmentView.findViewById(R.id.nestedScrollView);
        constraintLayoutNoElements = fragmentView.findViewById(R.id.constraintLayoutNoElements);
        linearLayout = fragmentView.findViewById(R.id.linearLayout);

        FloatingActionButton buttonAddTreatment = fragmentView.findViewById(R.id.buttonAddMedicine);
        buttonAddTreatment.setOnClickListener(view -> {
            if (treatment.isFinished()) {
                ((MainActivity) requireActivity()).showTreatmentFinishedDialog();
            } else {
                Intent intent = new Intent(requireActivity(), AddMedicineActivity.class);
                intent.putExtra(Treatment.class.getSimpleName(), treatment.getId());

                ((MainActivity) requireActivity()).fragmentLauncher.launch(intent);
            }
        });

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();

        treatment = ((MainActivity) requireActivity()).getTreatmentFromBundle(getArguments());
        ((MainActivity) requireActivity()).setTreatmentLayoutStatus(treatment, imageViewStatus, textViewStatus);

        ((MainActivity) requireActivity()).currentFragment = this;
        ((MainActivity) requireActivity()).showBackButton(true);
        if (listener != null)
            listener.onTitleChanged(treatment.getTitle());

        List<Medicine> medicines = null;
        try {
            medicines = treatment.getMedicines(requireActivity());
        } catch (DBFindException exception) {
            ExceptionManager.advertiseUI(requireActivity(), exception.getMessage());
        }

        showMedicines(medicines);
    }

    private void showMedicines(List<Medicine> medicines) {
        linearLayout.removeAllViews();
        if (medicines == null || medicines.isEmpty()) {
            nestedScrollView.setVisibility(View.GONE);
            constraintLayoutNoElements.setVisibility(View.VISIBLE);

            TextView textViewNoElements = constraintLayoutNoElements.findViewById(R.id.textViewNoElements);
            textViewNoElements.setText(R.string.medicines_no_elements);
        } else {
            constraintLayoutNoElements.setVisibility(View.GONE);
            nestedScrollView.setVisibility(View.VISIBLE);

            boolean isFirst = true;
            for (Medicine medicine : medicines) {
                MaterialCardView cardView = (MaterialCardView) LayoutInflater.from(getContext()).inflate(R.layout.card_medicine, linearLayout, false);
                if (!isFirst) {
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) cardView.getLayoutParams();
                    layoutParams.topMargin = getResources().getDimensionPixelSize(R.dimen.form_margin_top);
                    cardView.setLayoutParams(layoutParams);
                } else {
                    isFirst = false;
                }

                TextView textViewName = cardView.findViewById(R.id.textViewName);
                textViewName.setText(medicine.getName());

                AdministrationRoute administrationRoute = medicine.getAdministrationRoute();
                TextView textViewAdministrationRoute = cardView.findViewById(R.id.textViewAdministrationRoute);
                if (administrationRoute != null) {
                    String[] administrationRouteOptions = getResources().getStringArray(R.array.medicines_array_administration_route);
                    String administrationRouteString = administrationRouteOptions[administrationRoute.ordinal()];
                    textViewAdministrationRoute.setText(administrationRouteString);
                } else {
                    textViewAdministrationRoute.setText(R.string.unspecified);
                }

                Integer dose = medicine.getDose();
                TextView textViewDose = cardView.findViewById(R.id.textViewDose);
                if (dose != null) {
                    String doseString = dose + getString(R.string.medicines_mg);
                    textViewDose.setText(doseString);
                } else {
                    textViewDose.setText(R.string.unspecified);
                }

                ZonedDateTime nextDose = medicine.calculateNextDose();
                TextView textViewNextDose = cardView.findViewById(R.id.textViewNextDose);
                if (nextDose != null) {
                    textViewNextDose.setText(((MainActivity) requireActivity()).formatTimeDifference(Duration.between(ZonedDateTime.now(), nextDose).toMillis()));
                } else {
                    textViewNextDose.setText(R.string.medicines_none);
                }

                cardView.setOnClickListener(view -> {
                    Fragment fragment = new MedicineFragment();
                    Bundle bundle = new Bundle();
                    bundle.putLong(Treatment.class.getSimpleName(), treatment.getId());
                    bundle.putLong(Medicine.class.getSimpleName(), medicine.getId());
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