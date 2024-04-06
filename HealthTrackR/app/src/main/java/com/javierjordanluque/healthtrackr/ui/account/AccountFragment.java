package com.javierjordanluque.healthtrackr.ui.account;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.javierjordanluque.healthtrackr.R;
import com.javierjordanluque.healthtrackr.models.Allergy;
import com.javierjordanluque.healthtrackr.models.PreviousMedicalCondition;
import com.javierjordanluque.healthtrackr.models.User;
import com.javierjordanluque.healthtrackr.models.enumerations.BloodType;
import com.javierjordanluque.healthtrackr.models.enumerations.Gender;
import com.javierjordanluque.healthtrackr.ui.AuthenticationActivity;
import com.javierjordanluque.healthtrackr.ui.MainActivity;
import com.javierjordanluque.healthtrackr.ui.OnToolbarChangeListener;
import com.javierjordanluque.healthtrackr.util.AuthenticationService;
import com.javierjordanluque.healthtrackr.util.exceptions.DBDeleteException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.ExceptionManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AccountFragment extends Fragment {
    private OnToolbarChangeListener listener;
    private User user;

    public AccountFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
            user = getArguments().getParcelable(User.class.getSimpleName());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_account, container, false);

        if (user != null) {
            TextView textViewEmail = fragmentView.findViewById(R.id.textViewEmail);
            textViewEmail.setText(user.getEmail());

            TextView textViewFirstName = fragmentView.findViewById(R.id.textViewFirstName);
            textViewFirstName.setText(user.getFirstName());

            TextView textViewLastName = fragmentView.findViewById(R.id.textViewLastName);
            textViewLastName.setText(user.getLastName());

            LocalDate birthDate = user.getBirthDate();
            if (birthDate != null) {
                TextView textViewBirthDate = fragmentView.findViewById(R.id.textViewBirthDate);
                textViewBirthDate.setText(((MainActivity) requireActivity()).showFormattedDate(birthDate));
            }

            Gender gender = user.getGender();
            if (gender != null) {
                String[] genderOptions = getResources().getStringArray(R.array.gender_options);
                String genderString = genderOptions[gender.ordinal()];
                TextView textViewGender = fragmentView.findViewById(R.id.textViewGender);
                textViewGender.setText(genderString);
            }

            BloodType bloodType = user.getBloodType();
            if (bloodType != null) {
                String[] bloodTypeOptions = getResources().getStringArray(R.array.blood_type_options);
                String bloodTypeString = bloodTypeOptions[bloodType.ordinal()];
                TextView textViewBloodType = fragmentView.findViewById(R.id.textViewBloodType);
                textViewBloodType.setText(bloodTypeString);
            }

            try {
                List<Allergy> allergies = user.getAllergies(requireActivity());

                if (allergies != null && !allergies.isEmpty()) {
                    List<String> allergiesNames = new ArrayList<>();
                    for (Allergy allergy : allergies)
                        allergiesNames.add(allergy.getName());

                    TextView textViewAllergies = fragmentView.findViewById(R.id.textViewAllergies);
                    textViewAllergies.setText(((MainActivity) requireActivity()).showFormattedList(allergiesNames));
                }
            } catch (DBFindException exception) {
                ExceptionManager.advertiseUI(requireActivity(), exception.getMessage());
            }

            try {
                List<PreviousMedicalCondition> conditions = user.getConditions(requireActivity());

                if (conditions != null && !conditions.isEmpty()) {
                    List<String> conditionsNames = new ArrayList<>();
                    for (PreviousMedicalCondition condition : conditions)
                        conditionsNames.add(condition.getName());

                    TextView textViewPreviousMedicalConditions = fragmentView.findViewById(R.id.textViewPreviousMedicalConditions);
                    textViewPreviousMedicalConditions.setText(((MainActivity) requireActivity()).showFormattedList(conditionsNames));
                }
            } catch (DBFindException exception) {
                ExceptionManager.advertiseUI(requireActivity(), exception.getMessage());
            }
        }

        FloatingActionButton buttonModifyAccount = fragmentView.findViewById(R.id.buttonModifyAccount);
        buttonModifyAccount.setOnClickListener(view -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.addToBackStack(this.getClass().getSimpleName());
            fragmentTransaction.commit();

            Intent intent = new Intent(requireActivity(), ModifyAccountActivity.class);
            intent.putExtra(User.class.getSimpleName(), user);
            startActivity(intent);
        });

        Button buttonSignOut = fragmentView.findViewById(R.id.buttonSignOut);
        buttonSignOut.setOnClickListener(view -> {
            AuthenticationService.logout(user);
            AuthenticationService.clearCredentials(requireActivity());

            Intent intent = new Intent(requireActivity(), AuthenticationActivity.class);
            startActivity(intent);
        });

        Button buttonDeleteAccount = fragmentView.findViewById(R.id.buttonDeleteAccount);
        buttonDeleteAccount.setOnClickListener(view -> {
            showConfirmationDialog();
        });

        return fragmentView;
    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setMessage(getString(R.string.account_delete_dialog))
                .setPositiveButton(getString(R.string.dialog_yes), (dialog, id) -> {
                    try {
                        user.deleteUser(requireActivity());
                        AuthenticationService.logout(user);
                        AuthenticationService.clearCredentials(requireActivity());

                        Intent intent = new Intent(requireActivity(), AuthenticationActivity.class);
                        startActivity(intent);
                    } catch (DBDeleteException exception) {
                        ExceptionManager.advertiseUI(requireActivity(), exception.getMessage());
                    }
                })
                .setNegativeButton(getString(R.string.dialog_no), (dialog, id) -> {
                    dialog.dismiss();
                });
        builder.create().show();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).showBackButton(false);
        if (listener != null)
            listener.onTitleChanged(getString(R.string.account_title));
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
