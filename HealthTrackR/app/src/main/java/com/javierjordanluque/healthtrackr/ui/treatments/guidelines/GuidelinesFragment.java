package com.javierjordanluque.healthtrackr.ui.treatments.guidelines;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.models.Guideline;
import com.javierjordanluque.healthtrackr.models.Multimedia;
import com.javierjordanluque.healthtrackr.models.Treatment;
import com.javierjordanluque.healthtrackr.ui.MainActivity;
import com.javierjordanluque.healthtrackr.ui.OnToolbarChangeListener;
import com.javierjordanluque.healthtrackr.util.exceptions.DBDeleteException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBUpdateException;
import com.javierjordanluque.healthtrackr.util.exceptions.ExceptionManager;

import java.util.List;

public class GuidelinesFragment extends Fragment {
    private OnToolbarChangeListener listener;
    private Treatment treatment;
    private ImageView imageViewStatus;
    private TextView textViewStatus;
    private NestedScrollView nestedScrollView;
    private ConstraintLayout constraintLayoutNoElements;
    private LinearLayout linearLayout;

    public GuidelinesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_guidelines, container, false);

        imageViewStatus = fragmentView.findViewById(R.id.imageViewStatus);
        textViewStatus = fragmentView.findViewById(R.id.textViewStatus);

        nestedScrollView = fragmentView.findViewById(R.id.nestedScrollView);
        constraintLayoutNoElements = fragmentView.findViewById(R.id.constraintLayoutNoElements);
        linearLayout = fragmentView.findViewById(R.id.linearLayout);

        FloatingActionButton buttonAddGuideline = fragmentView.findViewById(R.id.buttonAddGuideline);
        buttonAddGuideline.setOnClickListener(view -> {
            if (treatment.isFinished()) {
                ((MainActivity) requireActivity()).showTreatmentFinishedDialog();
            } else {
                Intent intent = new Intent(requireActivity(), AddGuidelineActivity.class);
                intent.putExtra(Treatment.class.getSimpleName(), treatment.getId());
                startActivity(intent);
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

        try {
            showGuidelines(treatment.getGuidelines(requireActivity()));
        } catch (DBFindException exception) {
            ExceptionManager.advertiseUI(requireActivity(), exception.getMessage());
        }
    }

    private void showGuidelines(List<Guideline> guidelines) {
        linearLayout.removeAllViews();
        if (guidelines == null || guidelines.isEmpty()) {
            nestedScrollView.setVisibility(View.GONE);
            constraintLayoutNoElements.setVisibility(View.VISIBLE);

            TextView textViewNoElements = constraintLayoutNoElements.findViewById(R.id.textViewNoElements);
            textViewNoElements.setText(R.string.guidelines_no_elements);
        } else {
            constraintLayoutNoElements.setVisibility(View.GONE);
            nestedScrollView.setVisibility(View.VISIBLE);

            boolean isFirst = true;
            for (Guideline guideline : guidelines) {
                MaterialCardView cardView = (MaterialCardView) LayoutInflater.from(getContext()).inflate(R.layout.card_guideline, linearLayout, false);
                if (!isFirst) {
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) cardView.getLayoutParams();
                    layoutParams.topMargin = getResources().getDimensionPixelSize(R.dimen.form_margin_top);
                    cardView.setLayoutParams(layoutParams);
                } else {
                    isFirst = false;
                }

                TextView textViewTitle = cardView.findViewById(R.id.textViewTitle);
                textViewTitle.setText(guideline.getTitle());

                String description = guideline.getDescription();
                TextView textViewDescription = cardView.findViewById(R.id.textViewDescription);
                if (description != null) {
                    textViewDescription.setText(description);
                    textViewDescription.setVisibility(View.VISIBLE);
                }

                try {
                    List<Multimedia> multimedias = guideline.getMultimedias(requireActivity());

                    if (description != null && !multimedias.isEmpty())
                        cardView.findViewById(R.id.viewSeparationLine).setVisibility(View.VISIBLE);
                    if (!multimedias.isEmpty())
                        showMultimedias(cardView, multimedias);
                } catch (DBFindException exception) {
                    ExceptionManager.advertiseUI(requireActivity(), exception.getMessage());
                }

                cardView.setOnLongClickListener(view -> {
                    PopupMenu popupMenu = new PopupMenu(getContext(), view);
                    popupMenu.getMenuInflater().inflate(R.menu.guideline_menu, popupMenu.getMenu());

                    popupMenu.setOnMenuItemClickListener(item -> {
                        if (item.getItemId() == R.id.menuModifyGuideline) {
                            if (treatment.isFinished()) {
                                ((MainActivity) requireActivity()).showTreatmentFinishedDialog();
                            } else {
                                Intent intent = new Intent(requireActivity(), ModifyGuidelineActivity.class);
                                intent.putExtra(Treatment.class.getSimpleName(), treatment.getId());
                                intent.putExtra(Guideline.class.getSimpleName(), guideline.getId());
                                startActivity(intent);
                            }
                            return true;
                        } else if (item.getItemId() == R.id.menuDeleteGuideline) {
                            new AlertDialog.Builder(getContext())
                                    .setMessage(getString(R.string.guidelines_dialog_message_delete))
                                    .setPositiveButton(getString(R.string.button_delete), (dialog, which) -> {
                                        try {
                                            treatment.removeGuideline(requireActivity(), guideline);

                                            Toast.makeText(requireActivity(), getString(R.string.guidelines_toast_confirmation_delete), Toast.LENGTH_SHORT).show();
                                            onResume();
                                        } catch (DBDeleteException | DBFindException | DBUpdateException exception) {
                                            ExceptionManager.advertiseUI(requireActivity(), exception.getMessage());
                                        }
                                    })
                                    .setNegativeButton(getString(R.string.dialog_negative_cancel), null)
                                    .show();
                            return true;
                        }
                        return false;
                    });

                    popupMenu.show();
                    return true;
                });

                linearLayout.addView(cardView);
            }
        }
    }

    private void showMultimedias(MaterialCardView cardView, List<Multimedia> multimedias) {
        ConstraintLayout constraintLayout = cardView.findViewById(R.id.constraintLayout);
        constraintLayout.setVisibility(View.VISIBLE);

        ViewPager2 viewPager = constraintLayout.findViewById(R.id.viewPager);
        viewPager.setAdapter(new MultimediaPagerAdapter(requireActivity(), multimedias));

        if (multimedias.size() > 1) {
            LinearLayout linearLayoutCardView = cardView.findViewById(R.id.linearLayoutCardView);
            linearLayoutCardView.setPadding(linearLayoutCardView.getPaddingLeft(),
                    linearLayoutCardView.getPaddingTop(),
                    linearLayoutCardView.getPaddingRight(),
                    -18);

            TabLayout tabLayout = cardView.findViewById(R.id.tabLayout);
            tabLayout.setVisibility(View.VISIBLE);

            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {}).attach();
        }
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
