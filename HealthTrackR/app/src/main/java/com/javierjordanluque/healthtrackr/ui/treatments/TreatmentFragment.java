package com.javierjordanluque.healthtrackr.ui.treatments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.models.Treatment;
import com.javierjordanluque.healthtrackr.models.enumerations.TreatmentCategory;
import com.javierjordanluque.healthtrackr.ui.MainActivity;
import com.javierjordanluque.healthtrackr.ui.OnToolbarChangeListener;
import com.javierjordanluque.healthtrackr.ui.treatments.medicines.MedicinesFragment;
import com.javierjordanluque.healthtrackr.util.exceptions.DBDeleteException;
import com.javierjordanluque.healthtrackr.util.exceptions.ExceptionManager;

import java.time.ZonedDateTime;

public class TreatmentFragment extends Fragment {
    private OnToolbarChangeListener listener;
    private Treatment treatment;
    private ImageView imageViewStatus;
    private TextView textViewStatus;
    private TextView textViewStartDate;
    private TextView textViewEndDate;
    private TextView textViewCategory;
    private TextView textViewDiagnosis;

    public TreatmentFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_treatment, container, false);

        imageViewStatus = fragmentView.findViewById(R.id.imageViewStatus);
        textViewStatus = fragmentView.findViewById(R.id.textViewStatus);

        textViewStartDate = fragmentView.findViewById(R.id.textViewStartDate);
        textViewEndDate = fragmentView.findViewById(R.id.textViewEndDate);
        textViewCategory = fragmentView.findViewById(R.id.textViewCategory);
        textViewDiagnosis = fragmentView.findViewById(R.id.textViewDiagnosis);

        FloatingActionButton buttonModifyTreatment = fragmentView.findViewById(R.id.buttonModifyTreatment);
        buttonModifyTreatment.setOnClickListener(view -> {
            Intent intent = new Intent(requireActivity(), ModifyTreatmentActivity.class);
            intent.putExtra(Treatment.class.getSimpleName(), treatment.getId());
            startActivity(intent);
        });

        RelativeLayout relativeLayoutGuidelines = fragmentView.findViewById(R.id.relativeLayoutGuidelines);
        relativeLayoutGuidelines.setOnClickListener(view -> {
            //openFragmentFromTreatment(new GuidelinesFragment());
        });

        RelativeLayout relativeLayoutMedicines = fragmentView.findViewById(R.id.relativeLayoutMedicines);
        relativeLayoutMedicines.setOnClickListener(view -> {
            openFragmentFromTreatment(new MedicinesFragment());
        });

        RelativeLayout relativeLayoutSymptoms = fragmentView.findViewById(R.id.relativeLayoutSymptoms);
        relativeLayoutSymptoms.setOnClickListener(view -> {
            //openFragmentFromTreatment(new SymptomsFragment());
        });

        RelativeLayout relativeLayoutTreatmentCalendar = fragmentView.findViewById(R.id.relativeLayoutTreatmentCalendar);
        relativeLayoutTreatmentCalendar.setOnClickListener(view -> {
            //openFragmentFromTreatment(new TreatmentCalendarFragment());
        });

        FloatingActionButton buttonQuestions = fragmentView.findViewById(R.id.buttonQuestions);
        buttonQuestions.setOnClickListener(view -> {
            //openFragmentFromTreatment(new QuestionsFragment());
        });

        FloatingActionButton buttonDeleteTreatment = fragmentView.findViewById(R.id.buttonDeleteTreatment);
        buttonDeleteTreatment.setOnClickListener(view -> showDeleteTreatmentConfirmationDialog());

        return fragmentView;
    }

    private void openFragmentFromTreatment(Fragment fragment) {
        Bundle bundle = new Bundle();
        bundle.putLong(Treatment.class.getSimpleName(), treatment.getId());
        fragment.setArguments(bundle);
        ((MainActivity) requireActivity()).replaceFragment(fragment);
    }

    private void showDeleteTreatmentConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setMessage(getString(R.string.treatments_dialog_message_delete))
                .setPositiveButton(getString(R.string.dialog_positive_delete), (dialog, id) -> {
                    try {
                        ((MainActivity) requireActivity()).sessionViewModel.getUserSession().removeTreatment(requireActivity(), treatment);

                        Toast.makeText(requireActivity(), getString(R.string.treatments_toast_confirmation_delete), Toast.LENGTH_SHORT).show();
                        requireActivity().onBackPressed();
                    } catch (DBDeleteException exception) {
                        ExceptionManager.advertiseUI(requireActivity(), exception.getMessage());
                    }
                })
                .setNegativeButton(getString(R.string.dialog_negative_cancel), (dialog, id) -> dialog.dismiss());
        builder.create().show();
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

        textViewStartDate.setText(((MainActivity) requireActivity()).showFormattedDate(treatment.getStartDate()));

        ZonedDateTime endDate = treatment.getEndDate();
        if (endDate != null) {
            textViewEndDate.setText(((MainActivity) requireActivity()).showFormattedDate(endDate));
        } else {
            textViewEndDate.setText(R.string.unspecified);
        }

        TreatmentCategory category = treatment.getCategory();
        String[] categoryOptions = getResources().getStringArray(R.array.treatments_array_category);
        String categoryString = categoryOptions[category.ordinal()];
        textViewCategory.setText(categoryString);

        String diagnosis = treatment.getDiagnosis();
        if (diagnosis != null) {
            textViewDiagnosis.setText(diagnosis);
        } else {
            textViewDiagnosis.setText(R.string.unspecified);
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
