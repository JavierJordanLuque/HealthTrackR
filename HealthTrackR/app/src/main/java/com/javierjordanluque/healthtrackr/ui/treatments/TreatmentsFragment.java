package com.javierjordanluque.healthtrackr.ui.treatments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.models.Treatment;
import com.javierjordanluque.healthtrackr.models.User;
import com.javierjordanluque.healthtrackr.models.enumerations.TreatmentCategory;
import com.javierjordanluque.healthtrackr.ui.BaseActivity;
import com.javierjordanluque.healthtrackr.ui.MainActivity;
import com.javierjordanluque.healthtrackr.ui.OnToolbarChangeListener;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.ExceptionManager;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TreatmentsFragment extends Fragment {
    private OnToolbarChangeListener listener;
    private User user;
    private NestedScrollView nestedScrollView;
    private ConstraintLayout constraintLayoutNoElements;
    private LinearLayout linearLayout;
    private String titleFilter;
    private ZonedDateTime startDateFilter;
    private ZonedDateTime endDateFilter;
    private TreatmentCategory categoryFilter;
    private Boolean statusPendingFilter;
    private Boolean statusInProgressFilter;
    private Boolean statusFinishedFilter;

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
        constraintLayoutNoElements = fragmentView.findViewById(R.id.constraintLayoutNoElements);
        linearLayout = fragmentView.findViewById(R.id.linearLayout);

        FloatingActionButton buttonAddTreatment = fragmentView.findViewById(R.id.buttonAddTreatment);
        buttonAddTreatment.setOnClickListener(view -> {
            Intent intent = new Intent(requireActivity(), AddTreatmentActivity.class);
            ((MainActivity) requireActivity()).fragmentLauncher.launch(intent);
        });

        ExtendedFloatingActionButton buttonFilterTreatments = fragmentView.findViewById(R.id.buttonFilterTreatments);
        buttonFilterTreatments.setOnClickListener(view -> {
            @SuppressLint("InflateParams") View popupView = getLayoutInflater().inflate(R.layout.filter_treatments, null);
            PopupWindow popupWindow = new PopupWindow(popupView, buttonFilterTreatments.getWidth(), LinearLayout.LayoutParams.WRAP_CONTENT, true);
            popupWindow.showAsDropDown(buttonFilterTreatments, 0, 0);

            EditText editTextTitle = popupView.findViewById(R.id.editTextTitle);
            if (titleFilter != null) {
                editTextTitle.setText(titleFilter);
            } else {
                editTextTitle.setText("");
            }

            EditText editTextStartDate = popupView.findViewById(R.id.editTextStartDate);
            if (startDateFilter != null) {
                editTextStartDate.setText(((MainActivity) requireActivity()).showFormattedDate(startDateFilter));
            } else {
                editTextStartDate.setText("");
            }
            editTextStartDate.setOnClickListener(startDateView -> ((MainActivity) requireActivity()).showDatePickerDialog(editTextStartDate, getString(R.string.treatments_dialog_message_start_date), false));
            editTextStartDate.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() > 0) {
                        editTextStartDate.setFocusableInTouchMode(true);
                    } else {
                        BaseActivity.hideKeyboard(requireActivity());
                        editTextStartDate.clearFocus();
                        editTextStartDate.setFocusableInTouchMode(false);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            EditText editTextEndDate = popupView.findViewById(R.id.editTextEndDate);
            if (endDateFilter != null) {
                editTextEndDate.setText(((MainActivity) requireActivity()).showFormattedDate(endDateFilter));
            } else {
                editTextEndDate.setText("");
            }
            editTextEndDate.setOnClickListener(endDateView -> ((MainActivity) requireActivity()).showDatePickerDialog(editTextEndDate, getString(R.string.treatments_dialog_message_end_date), false));
            editTextEndDate.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() > 0) {
                        editTextEndDate.setFocusableInTouchMode(true);
                    } else {
                        BaseActivity.hideKeyboard(requireActivity());
                        editTextEndDate.clearFocus();
                        editTextEndDate.setFocusableInTouchMode(false);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            Spinner spinnerCategory = configureCategorySpinner(popupView.findViewById(R.id.spinnerCategory));

            CheckBox checkBoxStatusPending = popupView.findViewById(R.id.checkBoxStatusPending);
            if (statusPendingFilter != null) {
                checkBoxStatusPending.setChecked(statusPendingFilter);
            } else {
                checkBoxStatusPending.setChecked(true);
            }

            CheckBox checkBoxStatusInProgress = popupView.findViewById(R.id.checkBoxStatusInProgress);
            if (statusInProgressFilter != null) {
                checkBoxStatusInProgress.setChecked(statusInProgressFilter);
            } else {
                checkBoxStatusInProgress.setChecked(true);
            }

            CheckBox checkBoxStatusFinished = popupView.findViewById(R.id.checkBoxStatusFinished);
            if (statusFinishedFilter != null) {
                checkBoxStatusFinished.setChecked(statusFinishedFilter);
            } else {
                checkBoxStatusFinished.setChecked(true);
            }

            Button buttonFilter = popupView.findViewById(R.id.buttonFilter);
            buttonFilter.setOnClickListener(v -> {
                titleFilter = editTextTitle.getText().toString().trim();
                if (titleFilter.isEmpty())
                    titleFilter = null;

                startDateFilter = null;
                if (!editTextStartDate.getText().toString().isEmpty())
                    startDateFilter = (ZonedDateTime) ((MainActivity) requireActivity()).getDateFromEditText(editTextStartDate, ZonedDateTime.class);

                endDateFilter = null;
                if (!editTextEndDate.getText().toString().isEmpty())
                    endDateFilter = (ZonedDateTime) ((MainActivity) requireActivity()).getDateFromEditText(editTextEndDate, ZonedDateTime.class);

                categoryFilter = getCategoryFromSpinner(spinnerCategory);

                statusPendingFilter = checkBoxStatusPending.isChecked();
                statusInProgressFilter = checkBoxStatusInProgress.isChecked();
                statusFinishedFilter = checkBoxStatusFinished.isChecked();

                List<Treatment> filteredTreatments = user.filterTreatments(titleFilter, startDateFilter, endDateFilter, categoryFilter, statusPendingFilter,
                        statusInProgressFilter, statusFinishedFilter);
                showTreatments(filteredTreatments, true);

                popupWindow.dismiss();
            });

            Button buttonShowAllFilter = popupView.findViewById(R.id.buttonShowAllFilter);
            buttonShowAllFilter.setOnClickListener(v -> {
                try {
                    resetFilters();

                    List<Treatment> unfilteredTreatments = user.getTreatments(requireActivity());
                    showTreatments(unfilteredTreatments, false);
                } catch (DBFindException exception) {
                    ExceptionManager.advertiseUI(requireActivity(), exception.getMessage());
                }

                popupWindow.dismiss();
            });
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

        try {
            resetFilters();
            showTreatments(user.getTreatments(requireActivity()), false);
        } catch (DBFindException exception) {
            ExceptionManager.advertiseUI(requireActivity(), exception.getMessage());
        }
    }

    private void showTreatments(List<Treatment> treatments, boolean filter) {
        linearLayout.removeAllViews();
        if (treatments == null || treatments.isEmpty()) {
            nestedScrollView.setVisibility(View.GONE);
            constraintLayoutNoElements.setVisibility(View.VISIBLE);

            TextView textViewNoElements = constraintLayoutNoElements.findViewById(R.id.textViewNoElements);
            if (!filter) {
                textViewNoElements.setText(R.string.treatments_no_elements);
            } else {
                textViewNoElements.setText(R.string.treatments_not_found_message);
            }
        } else {
            constraintLayoutNoElements.setVisibility(View.GONE);
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

                ((MainActivity) requireActivity()).setTreatmentLayoutStatus(treatment, cardView.findViewById(R.id.imageViewStatus), cardView.findViewById(R.id.textViewStatus));

                linearLayout.addView(cardView);
            }
        }
    }

    private void resetFilters() {
        titleFilter = null;
        startDateFilter = null;
        endDateFilter = null;
        categoryFilter = null;
        statusPendingFilter = true;
        statusInProgressFilter = true;
        statusFinishedFilter = true;
    }

    private TreatmentCategory getCategoryFromSpinner(Spinner spinnerCategory) {
        String[] categoryOptions = getResources().getStringArray(R.array.treatments_array_category);
        String selectedCategory = spinnerCategory.getSelectedItem().toString();

        if (selectedCategory.equals(categoryOptions[0])) {
            return TreatmentCategory.MEDICAL;
        } else if (selectedCategory.equals(categoryOptions[1])) {
            return TreatmentCategory.PHARMACOLOGICAL;
        } else if (selectedCategory.equals(categoryOptions[2])) {
            return TreatmentCategory.PHYSIOTHERAPY;
        } else if (selectedCategory.equals(categoryOptions[3])) {
            return TreatmentCategory.REHABILITATION;
        } else if (selectedCategory.equals(categoryOptions[4])) {
            return TreatmentCategory.PSYCHOLOGICAL;
        } else if (selectedCategory.equals(categoryOptions[5])) {
            return TreatmentCategory.PREVENTIVE;
        } else if (selectedCategory.equals(categoryOptions[6])) {
            return TreatmentCategory.CHRONIC;
        } else if (selectedCategory.equals(categoryOptions[7])) {
            return TreatmentCategory.ALTERNATIVE;
        } else {
            return null;
        }
    }

    private Spinner configureCategorySpinner(Spinner spinnerCategory) {
        List<String> categoryOptions = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.treatments_array_category)));
        categoryOptions.add(getString(R.string.unspecified));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item, categoryOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
        if (categoryFilter != null) {
            int index = Arrays.asList(TreatmentCategory.values()).indexOf(categoryFilter);
            spinnerCategory.setSelection(index);
        } else {
            spinnerCategory.setSelection(categoryOptions.size() -1);
        }

        return spinnerCategory;
    }

    @Override
    public void onAttach(@NonNull Context context) {
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
