package com.javierjordanluque.healthtrackr.ui.treatments.questions;

import android.app.AlertDialog;
import android.content.Context;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.models.Question;
import com.javierjordanluque.healthtrackr.models.Treatment;
import com.javierjordanluque.healthtrackr.ui.MainActivity;
import com.javierjordanluque.healthtrackr.ui.OnToolbarChangeListener;
import com.javierjordanluque.healthtrackr.util.exceptions.DBDeleteException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;
import com.javierjordanluque.healthtrackr.util.exceptions.ExceptionManager;

import java.util.List;

public class QuestionsFragment extends Fragment {
    private OnToolbarChangeListener listener;
    private Treatment treatment;
    private ImageView imageViewStatus;
    private TextView textViewStatus;
    private NestedScrollView nestedScrollView;
    private ConstraintLayout constraintLayoutNoElements;
    private LinearLayout linearLayout;

    public QuestionsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_questions, container, false);

        imageViewStatus = fragmentView.findViewById(R.id.imageViewStatus);
        textViewStatus = fragmentView.findViewById(R.id.textViewStatus);

        nestedScrollView = fragmentView.findViewById(R.id.nestedScrollView);
        constraintLayoutNoElements = fragmentView.findViewById(R.id.constraintLayoutNoElements);
        linearLayout = fragmentView.findViewById(R.id.linearLayout);

        FloatingActionButton buttonAddQuestion = fragmentView.findViewById(R.id.buttonAddQuestion);
        buttonAddQuestion.setOnClickListener(view -> {
            if (treatment.isFinished()) {
                ((MainActivity) requireActivity()).showTreatmentFinishedDialog();
            } else {
                addQuestion();
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
            showQuestions(treatment.getQuestions(requireActivity()));
        } catch (DBFindException exception) {
            ExceptionManager.advertiseUI(requireActivity(), exception.getMessage());
        }
    }

    private void showQuestions(List<Question> questions) {
        linearLayout.removeAllViews();
        if (questions == null || questions.isEmpty()) {
            nestedScrollView.setVisibility(View.GONE);
            constraintLayoutNoElements.setVisibility(View.VISIBLE);

            TextView textViewNoElements = constraintLayoutNoElements.findViewById(R.id.textViewNoElements);
            textViewNoElements.setText(R.string.questions_no_elements);
        } else {
            constraintLayoutNoElements.setVisibility(View.GONE);
            nestedScrollView.setVisibility(View.VISIBLE);

            boolean isFirst = true;
            for (Question question : questions) {
                MaterialCardView cardView = (MaterialCardView) LayoutInflater.from(getContext()).inflate(R.layout.card_question, linearLayout, false);
                if (!isFirst) {
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) cardView.getLayoutParams();
                    layoutParams.topMargin = getResources().getDimensionPixelSize(R.dimen.form_margin_top);
                    cardView.setLayoutParams(layoutParams);
                } else {
                    isFirst = false;
                }

                TextView textViewDescription = cardView.findViewById(R.id.textViewDescription);
                textViewDescription.setText(question.getDescription());

                cardView.setOnLongClickListener(view -> {
                    PopupMenu popupMenu = new PopupMenu(getContext(), view);
                    popupMenu.getMenuInflater().inflate(R.menu.question_menu, popupMenu.getMenu());

                    popupMenu.setOnMenuItemClickListener(item -> {
                        if (item.getItemId() == R.id.menuDeleteQuestion) {
                            new AlertDialog.Builder(getContext())
                                    .setMessage(getString(R.string.questions_dialog_message_delete))
                                    .setPositiveButton(getString(R.string.button_delete), (dialog, which) -> {
                                        try {
                                            treatment.removeQuestion(requireActivity(), question);

                                            Toast.makeText(requireActivity(), getString(R.string.questions_toast_confirmation_delete), Toast.LENGTH_SHORT).show();
                                            onResume();
                                        } catch (DBDeleteException exception) {
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

    private void addQuestion() {
        View dialogView = LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_add_question, null);

        TextView textViewDescriptionError = dialogView.findViewById(R.id.textViewDescriptionError);
        ImageView imageViewDescriptionError = dialogView.findViewById(R.id.imageViewDescriptionError);
        EditText editTextDescription = dialogView.findViewById(R.id.editTextDescription);
        editTextDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textViewDescriptionError.setVisibility(View.INVISIBLE);
                imageViewDescriptionError.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView);
        builder.setTitle(getString(R.string.questions_dialog_title_add));
        builder.setPositiveButton(getString(R.string.button_add),
                (dialog, which) -> {
                });
        builder.setNegativeButton(getString(R.string.dialog_negative_cancel), (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String description = editTextDescription.getText().toString().trim();
            if (!isValidDescription(description)) {
                textViewDescriptionError.setVisibility(View.VISIBLE);
                imageViewDescriptionError.setVisibility(View.VISIBLE);
            } else {
                try {
                    new Question(requireActivity(), treatment, description);

                    dialog.dismiss();
                    onResume();
                } catch (DBInsertException exception) {
                    ExceptionManager.advertiseUI(requireActivity(), exception.getMessage());
                }
            }
        });
    }

    private boolean isValidDescription(String description) {
        return !description.isEmpty();
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
