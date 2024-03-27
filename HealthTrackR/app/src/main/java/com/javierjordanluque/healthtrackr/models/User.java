package com.javierjordanluque.healthtrackr.models;

import android.content.Context;

import com.javierjordanluque.healthtrackr.db.repositories.AllergyRepository;
import com.javierjordanluque.healthtrackr.db.repositories.PreviousMedicalConditionRepository;
import com.javierjordanluque.healthtrackr.db.repositories.TreatmentRepository;
import com.javierjordanluque.healthtrackr.db.repositories.UserRepository;
import com.javierjordanluque.healthtrackr.models.enumerations.BloodType;
import com.javierjordanluque.healthtrackr.models.enumerations.Gender;
import com.javierjordanluque.healthtrackr.models.enumerations.TreatmentCategory;
import com.javierjordanluque.healthtrackr.util.exceptions.DBDeleteException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBUpdateException;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class User implements Identifiable {
    private long id;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private Gender gender;
    private BloodType bloodType;
    private List<Allergy> allergies;
    private List<PreviousMedicalCondition> conditions;
    private List<Treatment> treatments;

    public User(String email, String firstName, String lastName) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    private User() {
    }

    public void modifyUser(Context context, String firstName, String lastName, LocalDate birthDate, Gender gender, BloodType bloodType, List<Allergy> allergies,
                           List<PreviousMedicalCondition> conditions) throws DBDeleteException, DBInsertException, DBUpdateException {
        User user = new User();
        user.setId(this.id);

        if (!this.firstName.equals(firstName)) {
            setFirstName(firstName);
            user.setFirstName(this.firstName);
        }

        if (!this.lastName.equals(lastName)) {
            setLastName(lastName);
            user.setLastName(this.lastName);
        }

        if ((this.birthDate == null && birthDate != null ) || (birthDate != null && !this.birthDate.equals(birthDate))) {
            setBirthDate(birthDate);
            user.setBirthDate(this.birthDate);
        }
        if ((this.gender == null && gender != null ) || (gender != null && !this.gender.equals(gender))) {
            setGender(gender);
            user.setGender(this.gender);
        }
        if ((this.bloodType == null && bloodType != null ) || (bloodType != null && !this.bloodType.equals(bloodType))) {
            setBloodType(bloodType);
            user.setBloodType(this.bloodType);
        }

        AllergyRepository allergyRepository = new AllergyRepository(context);
        for (Allergy allergy : this.allergies) {
            if (!allergies.contains(allergy)) {
                allergyRepository.delete(allergy);
                removeAllergy(allergy);
            }
        }
        for (Allergy allergy : allergies) {
            if (!this.allergies.contains(allergy)) {
                allergy.setId(allergyRepository.insert(allergy));
                addAllergy(allergy);
                user.setAllergies(new ArrayList<>());
                user.addAllergy(allergy);
            }
        }

        PreviousMedicalConditionRepository previousMedicalConditionRepository = new PreviousMedicalConditionRepository(context);
        for (PreviousMedicalCondition condition : this.conditions) {
            if (!conditions.contains(condition)) {
                previousMedicalConditionRepository.delete(condition);
                removeCondition(condition);
            }
        }
        for (PreviousMedicalCondition condition : conditions) {
            if (!this.conditions.contains(condition)) {
                condition.setId(previousMedicalConditionRepository.insert(condition));
                addCondition(condition);
                user.setConditions(new ArrayList<>());
                user.addCondition(condition);
            }
        }

        UserRepository userRepository = new UserRepository(context);
        userRepository.update(user);
    }

    public void changePassword(Context context, String currentPassword, String newPassword) {
        // @TODO
    }

    protected void addAllergy(Allergy allergy) {
        allergies.add(allergy);
    }

    private void removeAllergy(Allergy allergy) {
        allergies.remove(allergy);
    }

    protected void addCondition(PreviousMedicalCondition condition) {
        conditions.add(condition);
    }

    private void removeCondition(PreviousMedicalCondition condition) {
        conditions.remove(condition);
    }

    protected void addTreatment(Context context, Treatment treatment) throws DBInsertException {
        if (context != null) {
            TreatmentRepository treatmentRepository = new TreatmentRepository(context);
            treatment.setId(treatmentRepository.insert(treatment));
        }
        treatments.add(treatment);
    }

    public void removeTreatment(Context context, Treatment treatment) throws DBDeleteException {
        TreatmentRepository treatmentRepository = new TreatmentRepository(context);
        treatmentRepository.delete(treatment);
        treatments.remove(treatment);
    }

    public List<Treatment> filterTreatments(String title, ZonedDateTime startDate, ZonedDateTime endDate, TreatmentCategory category) {
        List<Treatment> filteredTreatments = new ArrayList<>();

        for (Treatment treatment : treatments) {
            boolean titleMatches = title == null || treatment.getTitle().contains(title);
            boolean startDateMatches = startDate == null || treatment.getStartDate().isAfter(startDate) || treatment.getStartDate().isEqual(startDate);
            boolean endDateMatches = endDate == null || treatment.getEndDate().isBefore(endDate) || treatment.getEndDate().isEqual(endDate);
            boolean categoryMatches = category == null || treatment.getCategory().equals(category);

            if (titleMatches && startDateMatches && endDateMatches && categoryMatches)
                filteredTreatments.add(treatment);
        }

        return filteredTreatments;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public BloodType getBloodType() {
        return bloodType;
    }

    public void setBloodType(BloodType bloodType) {
        this.bloodType = bloodType;
    }

    public List<Allergy> getAllergies(Context context) throws DBFindException {
        if (allergies == null) {
            AllergyRepository allergyRepository = new AllergyRepository(context);
            setAllergies(allergyRepository.findUserAllergies(this.id));
        }

        return allergies;
    }

    public void setAllergies(List<Allergy> allergies) {
        this.allergies = allergies;
    }

    public List<PreviousMedicalCondition> getConditions(Context context) throws DBFindException {
        if (allergies == null) {
            PreviousMedicalConditionRepository previousMedicalConditionRepository = new PreviousMedicalConditionRepository(context);
            setConditions(previousMedicalConditionRepository.findUserConditions(this.id));
        }

        return conditions;
    }

    public void setConditions(List<PreviousMedicalCondition> conditions) {
        this.conditions = conditions;
    }

    public List<Treatment> getTreatments(Context context) throws DBFindException {
        if (treatments == null) {
            TreatmentRepository treatmentRepository = new TreatmentRepository(context);
            setTreatments(treatmentRepository.findUserTreatments(this.id));
        }

        return treatments;
    }

    public void setTreatments(List<Treatment> treatments) {
        this.treatments = treatments;
    }
}
