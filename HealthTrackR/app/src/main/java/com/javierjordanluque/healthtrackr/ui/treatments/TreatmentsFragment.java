package com.javierjordanluque.healthtrackr.ui.treatments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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

    public TreatmentsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
            user = getArguments().getParcelable(User.class.getSimpleName());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_treatments, container, false);

        NestedScrollView nestedScrollView = fragmentView.findViewById(R.id.nestedScrollView);
        ConstraintLayout constraintLayoutEmpty = fragmentView.findViewById(R.id.constraintLayoutEmpty);

        List<Treatment> treatments = null;
        try {
            treatments = user.getTreatments(requireActivity());
        } catch (DBFindException exception) {
            ExceptionManager.advertiseUI(requireActivity(), exception.getMessage());
        }

        if (treatments == null || treatments.isEmpty()) {
            nestedScrollView.setVisibility(View.GONE);
            constraintLayoutEmpty.setVisibility(View.VISIBLE);
        } else {
            constraintLayoutEmpty.setVisibility(View.GONE);
            nestedScrollView.setVisibility(View.VISIBLE);

            LinearLayout linearLayout = fragmentView.findViewById(R.id.linearLayout);

            for (Treatment treatment : treatments) {
                MaterialCardView cardView = (MaterialCardView) LayoutInflater.from(getContext()).inflate(R.layout.card_treatment, linearLayout, false);

                TextView textViewTitle = cardView.findViewById(R.id.textViewTitle);
                textViewTitle.setText(treatment.getTitle());

                ZonedDateTime startDate = treatment.getStartDate();
                TextView textViewStartDate = cardView.findViewById(R.id.textViewStartDate);
                textViewStartDate.setText(((MainActivity) requireActivity()).showFormattedDate(startDate));

                ZonedDateTime endDate = treatment.getEndDate();
                if (endDate != null) {
                    TextView textViewEndDate = cardView.findViewById(R.id.textViewEndDate);
                    textViewEndDate.setText(((MainActivity) requireActivity()).showFormattedDate(endDate));
                }

                TreatmentCategory treatmentCategory = treatment.getCategory();
                String[] categoryOptions = getResources().getStringArray(R.array.treatments_category_options);
                String categoryString = categoryOptions[treatmentCategory.ordinal()];
                TextView textViewCategory = cardView.findViewById(R.id.textViewCategory);
                textViewCategory.setText(categoryString);

                setTreatmentStatus(cardView, startDate, endDate);

                Button buttonTreatment = cardView.findViewById(R.id.buttonTreatment);
                buttonTreatment.setOnClickListener(view -> openTreatment(treatment));
                cardView.setOnClickListener(view -> openTreatment(treatment));

                linearLayout.addView(cardView);
            }
        }

        FloatingActionButton buttonAddTreatment = fragmentView.findViewById(R.id.buttonAddTreatment);
        buttonAddTreatment.setOnClickListener(view -> {
            /*
            ((MainActivity) requireActivity()).addFragmentToBackStack(this.getClass().getSimpleName());

            Intent intent = new Intent(requireActivity(), AddTreatmentActivity.class);
            intent.putExtra(User.class.getSimpleName(), user);
            startActivity(intent);
             */
        });

        return fragmentView;
    }

    private void openTreatment(Treatment treatment) {
        /*
        ((MainActivity) requireActivity()).addFragmentToBackStack(this.getClass().getSimpleName());
        ((MainActivity) requireActivity()).replaceFragment(TreatmentFragment.class, Treatment.class.getSimpleName(), treatment);
         */
    }

    private void setTreatmentStatus(MaterialCardView cardView, ZonedDateTime startDate, ZonedDateTime endDate) {
        ImageView imageViewStatus = cardView.findViewById(R.id.imageViewStatus);
        TextView textViewStatus = cardView.findViewById(R.id.textViewStatus);
        ZonedDateTime currentDate = ZonedDateTime.now();

        if (startDate.isBefore(currentDate)) {
            imageViewStatus.setImageDrawable(AppCompatResources.getDrawable(requireActivity(), R.drawable.ic_treatment_pending));
            imageViewStatus.setContentDescription(getString(R.string.content_description_pending_treatment_status));
            textViewStatus.setText(getString(R.string.treatments_pending));
        } else if ((startDate.isAfter(currentDate) || startDate.isEqual(currentDate)) && (endDate == null || endDate.isBefore(currentDate))) {
            imageViewStatus.setImageDrawable(AppCompatResources.getDrawable(requireActivity(), R.drawable.ic_treatment_in_progress));
            imageViewStatus.setContentDescription(getString(R.string.content_description_in_progress_treatment_status));
            textViewStatus.setText(getString(R.string.treatments_in_progress));
        } else if (endDate != null && endDate.isBefore(currentDate)) {
            imageViewStatus.setImageDrawable(AppCompatResources.getDrawable(requireActivity(), R.drawable.ic_treatment_finished));
            imageViewStatus.setContentDescription(getString(R.string.content_description_finished_treatment_status));
            textViewStatus.setText(getString(R.string.treatments_finished));
        }
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
