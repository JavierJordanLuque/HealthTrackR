package com.javierjordanluque.healthtrackr.models;

import android.content.Context;

import com.javierjordanluque.healthtrackr.R;
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
import com.javierjordanluque.healthtrackr.util.exceptions.HashException;
import com.javierjordanluque.healthtrackr.util.exceptions.SerializationException;
import com.javierjordanluque.healthtrackr.util.security.SecurityService;
import com.javierjordanluque.healthtrackr.util.security.SerializationUtils;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        } else if (this.birthDate != null && birthDate == null) {
            setBirthDate(null);
            user.setBirthDate(LocalDate.MIN);
        }

        if ((this.gender == null && gender != null ) || (gender != null && !this.gender.equals(gender))) {
            setGender(gender);
            user.setGender(this.gender);
        }

        if ((this.bloodType == null && bloodType != null ) || (bloodType != null && !this.bloodType.equals(bloodType))) {
            setBloodType(bloodType);
            user.setBloodType(this.bloodType);
        }

        if (!(user.getFirstName() == null && user.getLastName() == null && user.getBirthDate() == null && user.getGender() == null && user.getBloodType() == null)) {
            UserRepository userRepository = new UserRepository(context);
            userRepository.update(user);
        }

        AllergyRepository allergyRepository = new AllergyRepository(context);
        List<Allergy> allergyList = new ArrayList<>(this.allergies);
        for (Allergy allergy : allergyList) {
            if (!allergies.contains(allergy)) {
                allergyRepository.delete(allergy);
                removeAllergy(allergy);
            }
        }
        for (Allergy allergy : allergies) {
            if (!this.allergies.contains(allergy)) {
                allergy.setId(allergyRepository.insert(allergy));
                addAllergy(allergy);
            }
        }

        PreviousMedicalConditionRepository previousMedicalConditionRepository = new PreviousMedicalConditionRepository(context);
        List<PreviousMedicalCondition> conditionList = new ArrayList<>(this.conditions);
        for (PreviousMedicalCondition condition : conditionList) {
            if (!conditions.contains(condition)) {
                previousMedicalConditionRepository.delete(condition);
                removeCondition(condition);
            }
        }
        for (PreviousMedicalCondition condition : conditions) {
            if (!this.conditions.contains(condition)) {
                condition.setId(previousMedicalConditionRepository.insert(condition));
                addCondition(condition);
            }
        }
    }

    public void changePassword(Context context, String currentPassword, String newPassword) throws Exception {
        try {
            UserRepository userRepository = new UserRepository(context);
            UserCredentials userCredentials = userRepository.findUserCredentials(email);

            if (userCredentials != null) {
                if (!SecurityService.meetsPasswordRequirements(newPassword))
                    throw new Exception(context.getString(R.string.authentication_helper_password));
                if (!userCredentials.equalsPassword(currentPassword))
                    throw new Exception(context.getString(R.string.error_incorrect_password));

                userRepository.updateUserCredentials(new UserCredentials(getId(), SecurityService.hashWithSalt(SerializationUtils.serialize(newPassword))));
            }
        } catch (DBFindException | SerializationException | HashException | DBUpdateException exception) {
            throw new Exception("Failed to change password with the following credentials: Email (" + email + "), Password (" + currentPassword + ")", exception);
        }
    }

    public void deleteUser(Context context) throws DBDeleteException {
        UserRepository userRepository = new UserRepository(context);
        userRepository.delete(this);
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
        TreatmentRepository treatmentRepository = new TreatmentRepository(context);
        treatment.setId(treatmentRepository.insert(treatment));
        treatments.add(treatment);
    }

    public void removeTreatment(Context context, Treatment treatment) throws DBDeleteException {
        TreatmentRepository treatmentRepository = new TreatmentRepository(context);
        treatmentRepository.delete(treatment);
        treatments.remove(treatment);
    }

    public List<Treatment> filterTreatments(String title, ZonedDateTime startDate, ZonedDateTime endDate, TreatmentCategory category, boolean statusPending,
                                            boolean statusInProgress, boolean statusFinished) {
        List<Treatment> filteredTreatments = new ArrayList<>();

        for (Treatment treatment : treatments) {
            boolean titleMatches = title == null || treatment.getTitle().toLowerCase().contains(title.toLowerCase());
            boolean startDateMatches = startDate == null || treatment.getStartDate().isAfter(startDate) || treatment.getStartDate().isEqual(startDate);
            boolean endDateMatches = endDate == null || (treatment.getEndDate() != null && treatment.getEndDate().isBefore(endDate)) || (treatment.getEndDate() != null && treatment.getEndDate().isEqual(endDate));
            boolean categoryMatches = category == null || treatment.getCategory().equals(category);
            boolean statusMatches = (statusPending && treatment.isPending()) || (statusInProgress && treatment.isInProgress()) || (statusFinished && treatment.isFinished());

            if (titleMatches && startDateMatches && endDateMatches && categoryMatches && statusMatches)
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
        if (conditions == null) {
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

        treatments.sort((treatment1, treatment2) -> {
            // Sort in progress treatments
            if (treatment1.isInProgress() && treatment2.isPending()) {
                return -1;
            } else if (treatment1.isPending() && treatment2.isInProgress()) {
                return 1;
            } else if (treatment1.isInProgress() && treatment2.isInProgress()) {
                if (treatment1.getEndDate() != null && treatment2.getEndDate() != null) {
                    int endDateComparison = treatment2.getEndDate().compareTo(treatment1.getEndDate());

                    if (endDateComparison != 0) {
                        return endDateComparison;
                    } else {
                        int startDateComparison = treatment2.getStartDate().compareTo(treatment1.getStartDate());

                        if (startDateComparison != 0) {
                            return startDateComparison;
                        } else {
                            return treatment1.getTitle().compareTo(treatment2.getTitle());
                        }
                    }
                } else if (treatment1.getEndDate() != null) {
                    return -1;
                } else if (treatment2.getEndDate() != null) {
                    return 1;
                } else {
                    return treatment1.getTitle().compareTo(treatment2.getTitle());
                }
            }

            // Sort pending treatments
            if (treatment1.isPending() && treatment2.isPending()) {
                if (treatment1.getStartDate() != null && treatment2.getStartDate() != null) {
                    int startDateComparison = treatment2.getStartDate().compareTo(treatment1.getStartDate());

                    if (startDateComparison != 0) {
                        return startDateComparison;
                    } else {
                        if (treatment1.getEndDate() != null && treatment2.getEndDate() != null) {
                            int endDateComparison = treatment2.getEndDate().compareTo(treatment1.getEndDate());

                            if (endDateComparison != 0) {
                                return endDateComparison;
                            } else {
                                return treatment1.getTitle().compareTo(treatment2.getTitle());
                            }
                        } else if (treatment1.getEndDate() != null) {
                            return -1;
                        } else if (treatment2.getEndDate() != null) {
                            return 1;
                        } else {
                            return treatment1.getTitle().compareTo(treatment2.getTitle());
                        }
                    }
                } else if (treatment1.getStartDate() != null) {
                    return -1;
                } else if (treatment2.getStartDate() != null) {
                    return 1;
                } else {
                    return treatment1.getTitle().compareTo(treatment2.getTitle());
                }
            }

            // Sort finished treatments
            if (treatment1.isFinished() && treatment2.isFinished()) {
                if (treatment1.getEndDate() != null && treatment2.getEndDate() != null) {
                    int endDateComparison = treatment2.getEndDate().compareTo(treatment1.getEndDate());

                    if (endDateComparison != 0) {
                        return endDateComparison;
                    } else {
                        int startDateComparison = treatment2.getStartDate().compareTo(treatment1.getStartDate());
                        if (startDateComparison != 0) {
                            return startDateComparison;
                        } else {
                            return treatment1.getTitle().compareTo(treatment2.getTitle());
                        }
                    }
                } else if (treatment1.getEndDate() != null) {
                    return -1;
                } else if (treatment2.getEndDate() != null) {
                    return 1;
                } else {
                    return treatment1.getTitle().compareTo(treatment2.getTitle());
                }
            } else if (treatment1.isFinished()) {
                return 1;
            } else {
                return -1;
            }
        });

        return treatments;
    }

    public void setTreatments(List<Treatment> treatments) {
        this.treatments = treatments;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        User user = (User) obj;
        return id == user.id &&
                email.equals(user.email) &&
                firstName.equals(user.firstName) &&
                lastName.equals(user.lastName) &&
                Objects.equals(birthDate, user.birthDate) &&
                gender == user.gender &&
                bloodType == user.bloodType;
    }
}
