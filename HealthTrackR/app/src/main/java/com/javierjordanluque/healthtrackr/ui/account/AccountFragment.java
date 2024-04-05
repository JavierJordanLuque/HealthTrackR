package com.javierjordanluque.healthtrackr.ui.account;

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
import java.util.List;

public class AccountFragment extends Fragment {
    private OnToolbarChangeListener listener;
    private User user;

    public AccountFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_account, container, false);

        Bundle bundle = getArguments();
        if (bundle != null)
            user = (User) bundle.getSerializable("user");

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
                textViewBirthDate.setText(birthDate.toString());
            }

            Gender gender = user.getGender();
            if (gender != null) {
                TextView textViewGender = fragmentView.findViewById(R.id.textViewGender);
                textViewGender.setText(gender.name());
            }

            BloodType bloodType = user.getBloodType();
            if (bloodType != null) {
                TextView textViewBloodType = fragmentView.findViewById(R.id.textViewBloodType);
                textViewBloodType.setText(bloodType.name());
            }

            try {
                List<Allergy> allergies = user.getAllergies(requireActivity());

                if (allergies != null && !allergies.isEmpty()) {
                    StringBuilder allergiesStringBuilder = new StringBuilder();

                    for (Allergy allergy : allergies)
                        allergiesStringBuilder.append(allergy.getName()).append(", ");

                    allergiesStringBuilder.deleteCharAt(allergiesStringBuilder.length() - 2);

                    TextView textViewAllergies = fragmentView.findViewById(R.id.textViewAllergies);
                    textViewAllergies.setText(allergiesStringBuilder.toString());
                }
            } catch (DBFindException exception) {
                ExceptionManager.advertiseUI(requireActivity(), exception.getMessage());
            }

            try {
                List<PreviousMedicalCondition> conditions = user.getConditions(requireActivity());

                if (conditions != null && !conditions.isEmpty()) {
                    StringBuilder conditionsStringBuilder = new StringBuilder();

                    for (PreviousMedicalCondition condition : conditions)
                        conditionsStringBuilder.append(condition.getName()).append(", ");

                    conditionsStringBuilder.deleteCharAt(conditionsStringBuilder.length() - 2);

                    TextView textViewPreviousMedicalConditions = fragmentView.findViewById(R.id.textViewPreviousMedicalConditions);
                    textViewPreviousMedicalConditions.setText(conditionsStringBuilder.toString());
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
            startActivity(intent);
        });

        Button buttonSignOut = fragmentView.findViewById(R.id.buttonSignOut);
        buttonSignOut.setOnClickListener(view -> {
            //AuthenticationService.logout(user);
            AuthenticationService.clearCredentials(requireActivity());

            Intent intent = new Intent(requireActivity(), AuthenticationActivity.class);
            startActivity(intent);
        });

        Button buttonDeleteAccount = fragmentView.findViewById(R.id.buttonDeleteAccount);
        buttonDeleteAccount.setOnClickListener(view -> {
            try {
                user.deleteUser(requireActivity());

                Intent intent = new Intent(requireActivity(), AuthenticationActivity.class);
                startActivity(intent);
            } catch (DBDeleteException exception) {
                ExceptionManager.advertiseUI(requireActivity(), exception.getMessage());
            }
        });

        return fragmentView;
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
