package com.javierjordanluque.healthcaretreatmenttracking.models;

import android.content.Context;

import com.javierjordanluque.healthcaretreatmenttracking.db.repositories.MedicineRepository;
import com.javierjordanluque.healthcaretreatmenttracking.db.repositories.TreatmentRepository;
import com.javierjordanluque.healthcaretreatmenttracking.models.enumerations.TreatmentCategory;

import java.time.LocalDate;
import java.util.List;

public class Treatment implements Identifiable {
    private long id;
    private User user;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private String diagnosis;
    private TreatmentCategory category;
    private List<Medicine> medicines;
    private List<Step> steps;
    private List<Symptom> symptoms;
    private List<Question> questions;
    private List<MedicalAppointment> appointments;

    public Treatment(Context context, User user, String title, LocalDate startDate, LocalDate endDate, String diagnosis, TreatmentCategory category) {
        this.user = user;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.diagnosis = diagnosis;
        this.category = category;

        // if (user != null)
        this.user.addTreatment(context, this);
    }

    public Treatment() {
    }

    public void modifyTreatment(Context context, String title, LocalDate startDate, LocalDate endDate, String diagnosis, TreatmentCategory category) {
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
        if (!this.endDate.equals(endDate)) {
            setEndDate(endDate);
            treatment.setEndDate(this.endDate);
        }
        if (!this.diagnosis.equals(diagnosis)) {
            setDiagnosis(diagnosis);
            treatment.setDiagnosis(this.diagnosis);
        }
        if (!this.category.equals(category)) {
            setCategory(category);
            treatment.setCategory(this.category);
        }

        TreatmentRepository treatmentRepository = new TreatmentRepository(context);
        treatmentRepository.update(treatment);
    }

    protected void addMedicine(Context context, Medicine medicine) {
        if (context != null) {
            MedicineRepository medicineRepository = new MedicineRepository(context);
            medicine.setId(medicineRepository.insert(medicine));
        }
        medicines.add(medicine);
    }

    public void removeMedicine(Context context, Medicine medicine) {
        MedicineRepository medicineRepository = new MedicineRepository(context);
        medicineRepository.delete(medicine);
        medicines.remove(medicine);
    }

    protected void addStep(Context context, Step step) {
        if (context != null) {
            StepRepository stepRepository = new StepRepository(context);
            step.setId(stepRepository.insert(step));
        }
        steps.add(step);
    }

    public void removeStep(Context context, Step step) {
        StepRepository stepRepository = new StepRepository(context);
        stepRepository.delete(step);
        steps.remove(step);
    }

    protected void addSymptom(Context context, Symptom symptom) {
        if (context != null) {
            SymptomRepository symptomRepository = new SymptomRepository(context);
            symptom.setId(symptomRepository.insert(symptom));
        }
        symptoms.add(symptom);
    }

    public void removeSymptom(Context context, Symptom symptom) {
        SymptomRepository symptomRepository = new SymptomRepository(context);
        symptomRepository.delete(symptom);
        symptoms.remove(symptom);
    }

    protected void addQuestion(Context context, Question question) {
        if (context != null) {
            QuestionRepository questionRepository = new QuestionRepository(context);
            question.setId(questionRepository.insert(question));
        }
        questions.add(question);
    }

    public void removeQuestion(Context context, Question question) {
        QuestionRepository questionRepository = new QuestionRepository(context);
        questionRepository.delete(question);
        questions.remove(question);
    }

    protected void addAppointment(Context context, MedicalAppointment appointment) {
        if (context != null) {
            MedicalAppointmentRepository medicalAppointmentRepository = new MedicalAppointmentRepository(context);
            appointment.setId(medicalAppointmentRepository.insert(appointment));
        }
        appointments.add(appointment);
    }

    public void removeAppointment(Context context, MedicalAppointment appointment) {
        MedicalAppointmentRepository medicalAppointmentRepository = new MedicalAppointmentRepository(context);
        medicalAppointmentRepository.delete(appointment);
        appointments.remove(appointment);
    }

    public List<MedicalAppointment> filterAppointments(boolean pastAppointments, boolean pendingAppointments) {
        // @TODO
        return null;
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

    public LocalDate getStartDate() {
        return startDate;
    }

    private void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    private void setEndDate(LocalDate endDate) {
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

    public List<Medicine> getMedicines() {
        if (medicines == null) {
            MedicineRepository medicineRepository = new MedicineRepository(context);
            setMedicines(medicineRepository.findTreatmentMedicines(this.id));
        }

        return medicines;
    }

    private void setMedicines(List<Medicine> medicines) {
        this.medicines = medicines;
    }

    public List<Step> getSteps() {
        if (steps == null) {
            StepRepository stepRepository = new StepRepository(context);
            setSteps(stepRepository.findTreatmentSteps(this.id));
        }

        return steps;
    }

    private void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public List<Symptom> getSymptoms() {
        if (symptoms == null) {
            SymptomRepository symptomRepository = new SymptomRepository(context);
            setSymptoms(symptomRepository.findTreatmentSymptoms(this.id));
        }

        return symptoms;
    }

    private void setSymptoms(List<Symptom> symptoms) {
        this.symptoms = symptoms;
    }

    public List<Question> getQuestions() {
        if (questions == null) {
            QuestionRepository questionRepository = new QuestionRepository(context);
            setQuestions(questionRepository.findTreatmentQuestions(this.id));
        }

        return questions;
    }

    private void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public List<MedicalAppointment> getAppointments() {
        if (appointments == null) {
            MedicalAppointmentRepository medicalAppointmentRepository = new MedicalAppointmentRepository(context);
            setAppointments(medicalAppointmentRepository.findTreatmentAppointments(this.id));
        }

        return appointments;
    }

    private void setAppointments(List<MedicalAppointment> appointments) {
        this.appointments = appointments;
    }
}
