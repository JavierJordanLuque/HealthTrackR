package com.javierjordanluque.healthtrackr.models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.javierjordanluque.healthtrackr.db.repositories.MedicalAppointmentRepository;
import com.javierjordanluque.healthtrackr.db.repositories.MedicineRepository;
import com.javierjordanluque.healthtrackr.db.repositories.QuestionRepository;
import com.javierjordanluque.healthtrackr.db.repositories.StepRepository;
import com.javierjordanluque.healthtrackr.db.repositories.SymptomRepository;
import com.javierjordanluque.healthtrackr.db.repositories.TreatmentRepository;
import com.javierjordanluque.healthtrackr.models.enumerations.TreatmentCategory;
import com.javierjordanluque.healthtrackr.util.exceptions.DBDeleteException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBUpdateException;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class Treatment implements Identifiable, Parcelable {
    private long id;
    private User user;
    private String title;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;
    private String diagnosis;
    private TreatmentCategory category;
    private List<Medicine> medicines;
    private List<Step> steps;
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

        this.user.addTreatment(context, this);
    }

    private Treatment() {
    }

    public void modifyTreatment(Context context, String title, ZonedDateTime startDate, ZonedDateTime endDate, String diagnosis, TreatmentCategory category) throws DBUpdateException {
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
            setEndDate(endDate);
            treatment.setEndDate(this.endDate);
        }
        if ((this.diagnosis == null && diagnosis != null ) || (diagnosis != null && !this.diagnosis.equals(diagnosis))) {
            setDiagnosis(diagnosis);
            treatment.setDiagnosis(this.diagnosis);
        }
        if ((this.category == null && category != null ) || (category != null && !this.category.equals(category))) {
            setCategory(category);
            treatment.setCategory(this.category);
        }

        TreatmentRepository treatmentRepository = new TreatmentRepository(context);
        treatmentRepository.update(treatment);
    }

    protected void addMedicine(Context context, Medicine medicine) throws DBInsertException {
        if (context != null) {
            MedicineRepository medicineRepository = new MedicineRepository(context);
            medicine.setId(medicineRepository.insert(medicine));
        }
        medicines.add(medicine);
    }

    public void removeMedicine(Context context, Medicine medicine) throws DBDeleteException {
        MedicineRepository medicineRepository = new MedicineRepository(context);
        medicineRepository.delete(medicine);
        medicines.remove(medicine);
    }

    protected void addStep(Context context, Step step) throws DBInsertException {
        if (context != null) {
            StepRepository stepRepository = new StepRepository(context);
            step.setId(stepRepository.insert(step));
        }
        steps.add(step);
    }

    public void removeStep(Context context, Step step) throws DBDeleteException {
        StepRepository stepRepository = new StepRepository(context);
        stepRepository.delete(step);
        steps.remove(step);
    }

    protected void addSymptom(Context context, Symptom symptom) throws DBInsertException {
        if (context != null) {
            SymptomRepository symptomRepository = new SymptomRepository(context);
            symptom.setId(symptomRepository.insert(symptom));
        }
        symptoms.add(symptom);
    }

    public void removeSymptom(Context context, Symptom symptom) throws DBDeleteException {
        SymptomRepository symptomRepository = new SymptomRepository(context);
        symptomRepository.delete(symptom);
        symptoms.remove(symptom);
    }

    protected void addQuestion(Context context, Question question) throws DBInsertException {
        if (context != null) {
            QuestionRepository questionRepository = new QuestionRepository(context);
            question.setId(questionRepository.insert(question));
        }
        questions.add(question);
    }

    public void removeQuestion(Context context, Question question) throws DBDeleteException {
        QuestionRepository questionRepository = new QuestionRepository(context);
        questionRepository.delete(question);
        questions.remove(question);
    }

    protected void addAppointment(Context context, MedicalAppointment appointment) throws DBInsertException {
        if (context != null) {
            MedicalAppointmentRepository medicalAppointmentRepository = new MedicalAppointmentRepository(context);
            appointment.setId(medicalAppointmentRepository.insert(appointment));
        }
        appointments.add(appointment);
    }

    public void removeAppointment(Context context, MedicalAppointment appointment) throws DBDeleteException {
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
        }

        return medicines;
    }

    private void setMedicines(List<Medicine> medicines) {
        this.medicines = medicines;
    }

    public List<Step> getSteps(Context context) throws DBFindException {
        if (steps == null) {
            StepRepository stepRepository = new StepRepository(context);
            setSteps(stepRepository.findTreatmentSteps(this.id));
        }

        return steps;
    }

    private void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public List<Symptom> getSymptoms(Context context) throws DBFindException {
        if (symptoms == null) {
            SymptomRepository symptomRepository = new SymptomRepository(context);
            setSymptoms(symptomRepository.findTreatmentSymptoms(this.id));
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
        }

        return appointments;
    }

    private void setAppointments(List<MedicalAppointment> appointments) {
        this.appointments = appointments;
    }

    protected Treatment(Parcel in) {
        id = in.readLong();
        user = in.readParcelable(User.class.getClassLoader());
        title = in.readString();
        startDate = (ZonedDateTime) in.readSerializable();
        endDate = (ZonedDateTime) in.readSerializable();
        diagnosis = in.readString();
        category = TreatmentCategory.valueOf(in.readString());
        medicines = in.createTypedArrayList(Medicine.CREATOR);
        steps = in.createTypedArrayList(Step.CREATOR);
        symptoms = in.createTypedArrayList(Symptom.CREATOR);
        questions = in.createTypedArrayList(Question.CREATOR);
        appointments = in.createTypedArrayList(MedicalAppointment.CREATOR);
    }

    public static final Creator<Treatment> CREATOR = new Creator<Treatment>() {
        @Override
        public Treatment createFromParcel(Parcel in) {
            return new Treatment(in);
        }

        @Override
        public Treatment[] newArray(int size) {
            return new Treatment[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeParcelable(user, flags);
        dest.writeString(title);
        dest.writeSerializable(startDate);
        dest.writeSerializable(endDate);
        dest.writeString(diagnosis);
        dest.writeString(category.name());
        dest.writeTypedList(medicines);
        dest.writeTypedList(steps);
        dest.writeTypedList(symptoms);
        dest.writeTypedList(questions);
        dest.writeTypedList(appointments);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}