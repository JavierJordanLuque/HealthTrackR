package com.javierjordanluque.healthtrackr.models;

import android.content.Context;

import com.javierjordanluque.healthtrackr.db.repositories.MedicalAppointmentRepository;
import com.javierjordanluque.healthtrackr.db.repositories.MedicineRepository;
import com.javierjordanluque.healthtrackr.db.repositories.QuestionRepository;
import com.javierjordanluque.healthtrackr.db.repositories.GuidelineRepository;
import com.javierjordanluque.healthtrackr.db.repositories.SymptomRepository;
import com.javierjordanluque.healthtrackr.db.repositories.TreatmentRepository;
import com.javierjordanluque.healthtrackr.models.enumerations.TreatmentCategory;
import com.javierjordanluque.healthtrackr.util.exceptions.DBDeleteException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBUpdateException;
import com.javierjordanluque.healthtrackr.util.notifications.MedicalAppointmentNotification;
import com.javierjordanluque.healthtrackr.util.notifications.MedicationNotification;
import com.javierjordanluque.healthtrackr.util.notifications.NotificationScheduler;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class Treatment implements Identifiable {
    private long id;
    private User user;
    private String title;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;
    private String diagnosis;
    private TreatmentCategory category;
    private List<Medicine> medicines;
    private List<Guideline> guidelines;
    private List<Symptom> symptoms;
    private List<Question> questions;
    private List<MedicalAppointment> appointments;

    public Treatment(Context context, User user, String title, ZonedDateTime startDate, ZonedDateTime endDate, String diagnosis, TreatmentCategory category) throws DBInsertException {
        this.user = user;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.diagnosis = diagnosis;
        this.category = category;

        if (context != null)
            this.user.addTreatment(context, this);
    }

    private Treatment() {
    }

    public void modifyTreatment(Context context, String title, ZonedDateTime startDate, ZonedDateTime endDate, String diagnosis, TreatmentCategory category)
            throws DBUpdateException, DBFindException, DBInsertException, DBDeleteException {
        Treatment treatment = new Treatment();
        treatment.setId(this.id);

        if (!this.title.equals(title)) {
            setTitle(title);
            treatment.setTitle(this.title);
        }

        if (!this.startDate.equals(startDate)) {
            setStartDate(startDate);
            treatment.setStartDate(this.startDate);
        }

        if ((this.endDate == null && endDate != null ) || (endDate != null && !this.endDate.equals(endDate))) {
            if (isFinished() && !endDate.isBefore(ZonedDateTime.now())) {
                for (Medicine medicine : getMedicines(context)) {
                    medicine.schedulePreviousMedicationNotification(context, NotificationScheduler.PREVIOUS_DEFAULT_MINUTES);
                    medicine.scheduleMedicationNotification(context);
                }
            } else if (!isFinished() && endDate.isBefore(ZonedDateTime.now())) {
                for (Medicine medicine : getMedicines(context)) {
                    for (MedicationNotification medicationNotification :  new ArrayList<>(medicine.getNotifications(context)))
                        NotificationScheduler.cancelNotification(context, medicationNotification);
                }
            }
            setEndDate(endDate);
            treatment.setEndDate(this.endDate);
        } else if (this.endDate != null && endDate == null) {
            if (isFinished()) {
                for (Medicine medicine : getMedicines(context)) {
                    medicine.schedulePreviousMedicationNotification(context, NotificationScheduler.PREVIOUS_DEFAULT_MINUTES);
                    medicine.scheduleMedicationNotification(context);
                }
            }
            setEndDate(null);
            treatment.setEndDate(ZonedDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC));
        }

        if ((this.diagnosis == null && diagnosis != null ) || (diagnosis != null && !this.diagnosis.equals(diagnosis))) {
            setDiagnosis(diagnosis);
            treatment.setDiagnosis(this.diagnosis);
        } else if (this.diagnosis != null && diagnosis == null) {
            setDiagnosis(null);
            treatment.setDiagnosis("");
        }

        if ((this.category == null && category != null ) || (category != null && !this.category.equals(category))) {
            setCategory(category);
            treatment.setCategory(this.category);
        }

        if (!(treatment.getTitle() == null && treatment.getStartDate() == null && treatment.getEndDate() == null && treatment.getDiagnosis() == null &&
                treatment.getCategory() == null)) {
            TreatmentRepository treatmentRepository = new TreatmentRepository(context);
            treatmentRepository.update(treatment);
        }
    }

    public boolean isStarted() {
        return !ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).isBefore(startDate);
    }
    public boolean isPending() {
        return !isStarted();
    }

    public boolean isInProgress() {
        return isStarted() && !isFinished();
    }

    public boolean isFinished() {
        return endDate != null && ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).isAfter(endDate);
    }

    protected void addMedicine(Context context, Medicine medicine) throws DBInsertException {
        MedicineRepository medicineRepository = new MedicineRepository(context);
        medicine.setId(medicineRepository.insert(medicine));
        medicines.add(medicine);
    }

    public void removeMedicine(Context context, Medicine medicine) throws DBDeleteException, DBFindException {
        for (MedicationNotification medicationNotification : new ArrayList<>(medicine.getNotifications(context)))
            NotificationScheduler.cancelNotification(context, medicationNotification);

        MedicineRepository medicineRepository = new MedicineRepository(context);
        medicineRepository.delete(medicine);
        medicines.remove(medicine);
    }

    protected void addGuideline(Context context, Guideline guideline) throws DBInsertException, DBFindException, DBUpdateException {
        if (guideline.getNumOrder() <= this.guidelines.size())
            guideline.adjustGuidelinesNumOrder(context, true);

        GuidelineRepository guidelineRepository = new GuidelineRepository(context);
        guideline.setId(guidelineRepository.insert(guideline));
        guidelines.add(guideline);
    }

    public void removeGuideline(Context context, Guideline guideline) throws DBDeleteException, DBFindException, DBUpdateException {
        if (guideline.getNumOrder() < this.guidelines.size())
            guideline.adjustGuidelinesNumOrder(context, false);

        GuidelineRepository guidelineRepository = new GuidelineRepository(context);
        guidelineRepository.delete(guideline);
        guidelines.remove(guideline);
    }

    protected void addSymptom(Context context, Symptom symptom) throws DBInsertException {
        SymptomRepository symptomRepository = new SymptomRepository(context);
        symptom.setId(symptomRepository.insert(symptom));
        symptoms.add(symptom);
    }

    public void removeSymptom(Context context, Symptom symptom) throws DBDeleteException {
        SymptomRepository symptomRepository = new SymptomRepository(context);
        symptomRepository.delete(symptom);
        symptoms.remove(symptom);
    }

    protected void addQuestion(Context context, Question question) throws DBInsertException {
        QuestionRepository questionRepository = new QuestionRepository(context);
        question.setId(questionRepository.insert(question));
        questions.add(question);
    }

    public void removeQuestion(Context context, Question question) throws DBDeleteException {
        QuestionRepository questionRepository = new QuestionRepository(context);
        questionRepository.delete(question);
        questions.remove(question);
    }

    protected void addAppointment(Context context, MedicalAppointment appointment) throws DBInsertException {
        MedicalAppointmentRepository medicalAppointmentRepository = new MedicalAppointmentRepository(context);
        appointment.setId(medicalAppointmentRepository.insert(appointment));
        appointments.add(appointment);
    }

    public void removeAppointment(Context context, MedicalAppointment appointment) throws DBDeleteException, DBFindException {
        MedicalAppointmentNotification appointmentNotification = appointment.getNotification(context);
        if (appointmentNotification != null)
            NotificationScheduler.cancelNotification(context,appointmentNotification);

        MedicalAppointmentRepository medicalAppointmentRepository = new MedicalAppointmentRepository(context);
        medicalAppointmentRepository.delete(appointment);
        appointments.remove(appointment);
    }

    public List<MedicalAppointment> filterAppointments(boolean pastAppointments, boolean pendingAppointments) {
        List<MedicalAppointment> filteredAppointments = new ArrayList<>();

        for (MedicalAppointment appointment : appointments) {
            if (pendingAppointments && appointment.isPending())
                filteredAppointments.add(appointment);
            if (pastAppointments && !appointment.isPending())
                filteredAppointments.add(appointment);
        }

        return filteredAppointments;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public String getTitle() {
        return title;
    }

    private void setTitle(String title) {
        this.title = title;
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    private void setStartDate(ZonedDateTime startDate) {
        this.startDate = startDate;
    }

    public ZonedDateTime getEndDate() {
        return endDate;
    }

    private void setEndDate(ZonedDateTime endDate) {
        this.endDate = endDate;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    private void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public TreatmentCategory getCategory() {
        return category;
    }

    private void setCategory(TreatmentCategory category) {
        this.category = category;
    }

    public List<Medicine> getMedicines(Context context) throws DBFindException {
        if (medicines == null) {
            MedicineRepository medicineRepository = new MedicineRepository(context);
            setMedicines(medicineRepository.findTreatmentMedicines(this.id));

            for (Medicine medicine : medicines)
                medicine.setTreatment(this);
        }

        medicines.sort((medicine1, medicine2) -> {
            ZonedDateTime nextDose1 = medicine1.calculateNextDose();
            ZonedDateTime nextDose2 = medicine2.calculateNextDose();

            if (nextDose1 == null && nextDose2 == null) {
                return medicine1.getInitialDosingTime().compareTo(medicine2.getInitialDosingTime());
            } else if (nextDose1 == null) {
                return 1;
            } else if (nextDose2 == null) {
                return -1;
            } else {
                int compareNextDose = nextDose1.compareTo(nextDose2);

                return compareNextDose != 0 ? compareNextDose : medicine1.getInitialDosingTime().compareTo(medicine2.getInitialDosingTime());
            }
        });

        return medicines;
    }

    public void setMedicines(List<Medicine> medicines) {
        this.medicines = medicines;
    }

    public List<Guideline> getGuidelines(Context context) throws DBFindException {
        if (guidelines == null) {
            GuidelineRepository guidelineRepository = new GuidelineRepository(context);
            setGuidelines(guidelineRepository.findTreatmentGuidelines(this.id));

            for (Guideline guideline : guidelines)
                guideline.setTreatment(this);
        }
        guidelines.sort(Comparator.comparingInt(Guideline::getNumOrder));

        return guidelines;
    }

    private void setGuidelines(List<Guideline> guidelines) {
        this.guidelines = guidelines;
    }

    public List<Symptom> getSymptoms(Context context) throws DBFindException {
        if (symptoms == null) {
            SymptomRepository symptomRepository = new SymptomRepository(context);
            setSymptoms(symptomRepository.findTreatmentSymptoms(this.id));

            for (Symptom symptom : symptoms)
                symptom.setTreatment(this);
        }

        return symptoms;
    }

    private void setSymptoms(List<Symptom> symptoms) {
        this.symptoms = symptoms;
    }

    public List<Question> getQuestions(Context context) throws DBFindException {
        if (questions == null) {
            QuestionRepository questionRepository = new QuestionRepository(context);
            setQuestions(questionRepository.findTreatmentQuestions(this.id));

            for (Question question : questions)
                question.setTreatment(this);
        }

        return questions;
    }

    private void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public List<MedicalAppointment> getAppointments(Context context) throws DBFindException {
        if (appointments == null) {
            MedicalAppointmentRepository medicalAppointmentRepository = new MedicalAppointmentRepository(context);
            setAppointments(medicalAppointmentRepository.findTreatmentAppointments(this.id));

            for (MedicalAppointment appointment : appointments)
                appointment.setTreatment(this);
        }
        appointments.sort(Comparator.comparing(MedicalAppointment::getDateTime));

        return appointments;
    }

    public void setAppointments(List<MedicalAppointment> appointments) {
        this.appointments = appointments;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Treatment treatment = (Treatment) obj;
        return id == treatment.id &&
                Objects.equals(user, treatment.user) &&
                Objects.equals(title, treatment.title) &&
                Objects.equals(startDate, treatment.startDate) &&
                Objects.equals(endDate, treatment.endDate) &&
                Objects.equals(diagnosis, treatment.diagnosis) &&
                category == treatment.category;
    }
}