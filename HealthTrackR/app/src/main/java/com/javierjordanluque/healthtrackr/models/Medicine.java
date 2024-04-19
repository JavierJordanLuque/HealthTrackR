package com.javierjordanluque.healthtrackr.models;

import android.content.Context;

import com.javierjordanluque.healthtrackr.db.repositories.MedicineRepository;
import com.javierjordanluque.healthtrackr.db.repositories.NotificationRepository;
import com.javierjordanluque.healthtrackr.models.enumerations.AdministrationRoute;
import com.javierjordanluque.healthtrackr.util.PermissionManager;
import com.javierjordanluque.healthtrackr.util.exceptions.DBDeleteException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBUpdateException;
import com.javierjordanluque.healthtrackr.util.exceptions.NotificationException;
import com.javierjordanluque.healthtrackr.util.notifications.MedicationNotification;
import com.javierjordanluque.healthtrackr.util.notifications.NotificationScheduler;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

public class Medicine implements Identifiable {
    private long id;
    private Treatment treatment;
    private String name;
    private String activeSubstance;
    private Integer dose;
    private AdministrationRoute administrationRoute;
    private ZonedDateTime initialDosingTime;
    private Integer dosageFrequencyHours;
    private Integer dosageFrequencyMinutes;
    private List<MedicationNotification> notifications;

    public Medicine(Context context, Treatment treatment, String name, String activeSubstance, Integer dose, AdministrationRoute administrationRoute, ZonedDateTime initialDosingTime,
                    int dosageFrequencyHours, int dosageFrequencyMinutes) throws DBInsertException, DBDeleteException {
        this.treatment = treatment;
        this.name = name;
        this.activeSubstance = activeSubstance;
        this.dose = dose;
        this.administrationRoute = administrationRoute;
        this.initialDosingTime = initialDosingTime;
        this.dosageFrequencyHours = dosageFrequencyHours;
        this.dosageFrequencyMinutes = dosageFrequencyMinutes;

        if (context != null) {
            this.treatment.addMedicine(context, this);
            schedulePreviousMedicationNotification(context, NotificationScheduler.PREVIOUS_DEFAULT_MINUTES);
            scheduleMedicationNotification(context);
        }
    }

    public void schedulePreviousMedicationNotification(Context context, int previousMinutes) throws DBInsertException, DBDeleteException {
        if (PermissionManager.hasNotificationPermission(context)) {
            long timestamp = initialDosingTime.minusMinutes(previousMinutes).toInstant().toEpochMilli();
            MedicationNotification previousNotification = new MedicationNotification(this, timestamp);

            NotificationRepository notificationRepository = new NotificationRepository(context);
            previousNotification.setId(notificationRepository.insert(previousNotification));

            try {
                NotificationScheduler.scheduleInexactRepeatingNotification(context, previousNotification);
                notifications.add(previousNotification);
            } catch (NotificationException exception) {
                notificationRepository.delete(previousNotification);
            }
        }
    }

    public void scheduleMedicationNotification(Context context) throws DBInsertException, DBDeleteException {
        if (PermissionManager.hasNotificationPermission(context)) {
            long timestamp = initialDosingTime.toInstant().toEpochMilli();
            MedicationNotification notification = new MedicationNotification(this, timestamp);

            NotificationRepository notificationRepository = new NotificationRepository(context);
            notification.setId(notificationRepository.insert(notification));

            try {
                NotificationScheduler.scheduleInexactRepeatingNotification(context, notification);
                notifications.add(notification);
            } catch (NotificationException exception) {
                notificationRepository.delete(notification);
            }
        }
    }

    public void removeNotification(Context context, MedicationNotification notification) throws DBDeleteException, DBFindException {
        NotificationRepository notificationRepository = new NotificationRepository(context);
        notificationRepository.delete(notification);
        this.getNotifications(context).remove(notification);
    }

    private Medicine() {
    }

    public void modifyMedicine(Context context, Integer dose, AdministrationRoute administrationRoute, ZonedDateTime initialDosingTime, int dosageFrequencyHours,
                               int dosageFrequencyMinutes) throws DBUpdateException, DBFindException, DBDeleteException, DBInsertException {
        Medicine medicine = new Medicine();
        medicine.setId(this.id);
        medicine.setTreatment(this.treatment);

        if ((this.dose == null && dose != null ) || (dose != null && !this.dose.equals(dose))) {
            setDose(dose);
            medicine.setDose(this.dose);
        }

        if ((this.administrationRoute == null && administrationRoute != null ) || (administrationRoute != null && !this.administrationRoute.equals(administrationRoute))) {
            setAdministrationRoute(administrationRoute);
            medicine.setAdministrationRoute(this.administrationRoute);
        }

        ZonedDateTime oldInitialDosingTime = this.initialDosingTime;
        if (!this.initialDosingTime.equals(initialDosingTime)) {
            setInitialDosingTime(initialDosingTime);
            medicine.setInitialDosingTime(this.initialDosingTime);
        }

        if (!this.dosageFrequencyHours.equals(dosageFrequencyHours) || !this.dosageFrequencyMinutes.equals(dosageFrequencyMinutes)) {
            if (!this.dosageFrequencyHours.equals(dosageFrequencyHours)) {
                setDosageFrequencyHours(dosageFrequencyHours);
                medicine.setDosageFrequencyHours(this.dosageFrequencyHours);
            }

            if (!this.dosageFrequencyMinutes.equals(dosageFrequencyMinutes)) {
                setDosageFrequencyMinutes(dosageFrequencyMinutes);
                medicine.setDosageFrequencyMinutes(this.dosageFrequencyMinutes);
            }

            for (MedicationNotification medicationNotification : getNotifications(context)) {
                if (medicationNotification.getTimestamp() != medicationNotification.getMedicine().getInitialDosingTime().toInstant().toEpochMilli()) {
                    int previousMinutes = (int) ChronoUnit.MINUTES.between(oldInitialDosingTime, Instant.ofEpochMilli(medicationNotification.getTimestamp())
                            .atZone(oldInitialDosingTime.getZone()));

                    NotificationScheduler.cancelNotification(context, medicationNotification);
                    schedulePreviousMedicationNotification(context, previousMinutes);
                } else {
                    NotificationScheduler.cancelNotification(context, medicationNotification);
                    scheduleMedicationNotification(context);
                }
            }
        }

        MedicineRepository medicineRepository = new MedicineRepository(context);
        medicineRepository.update(medicine);
    }

    public void modifyMedicineNotifications(Context context, int previousNotificationTimeHours, int previousNotificationTimeMinutes, boolean previousNotificationStatus,
                                           boolean dosingNotificationStatus) throws DBFindException, DBDeleteException, DBInsertException {
        notifications = getNotifications(context);

        MedicationNotification previousNotification = null;
        MedicationNotification exactNotification = null;

        for (MedicationNotification notification : notifications) {
            if (notification.getTimestamp() != getInitialDosingTime().toInstant().toEpochMilli()) {
                previousNotification = notification;
            } else {
                exactNotification = notification;
            }
        }

        int previousMinutes = previousNotificationTimeHours * 60 + previousNotificationTimeMinutes;
        long previousTimestamp = initialDosingTime.minusMinutes(previousMinutes).toInstant().toEpochMilli();

        if (previousNotification != null && (!previousNotificationStatus || previousNotification.getTimestamp() != previousTimestamp))
            NotificationScheduler.cancelNotification(context, previousNotification);
        if (previousNotificationStatus && (previousNotification == null || previousNotification.getTimestamp() != previousTimestamp))
            schedulePreviousMedicationNotification(context, previousMinutes);

        if (exactNotification != null && !dosingNotificationStatus)
            NotificationScheduler.cancelNotification(context, exactNotification);
        if (dosingNotificationStatus && exactNotification == null)
            scheduleMedicationNotification(context);
    }

    public ZonedDateTime calculateNextDose() {
        Duration frequency = Duration.ofHours(dosageFrequencyHours).plusMinutes(dosageFrequencyMinutes);
        long dosesElapsed = Duration.between(initialDosingTime, ZonedDateTime.now()).toMinutes() / frequency.toMinutes();

        return initialDosingTime.plus(frequency.multipliedBy(dosesElapsed + 1));
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Treatment getTreatment() {
        return treatment;
    }

    private void setTreatment(Treatment treatment) {
        this.treatment = treatment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getActiveSubstance() {
        return activeSubstance;
    }

    public void setActiveSubstance(String activeSubstance) {
        this.activeSubstance = activeSubstance;
    }

    public Integer getDose() {
        return dose;
    }

    private void setDose(Integer dose) {
        this.dose = dose;
    }

    public AdministrationRoute getAdministrationRoute() {
        return administrationRoute;
    }

    private void setAdministrationRoute(AdministrationRoute administrationRoute) {
        this.administrationRoute = administrationRoute;
    }

    public ZonedDateTime getInitialDosingTime() {
        return initialDosingTime;
    }

    private void setInitialDosingTime(ZonedDateTime initialDosingTime) {
        this.initialDosingTime = initialDosingTime;
    }

    public Integer getDosageFrequencyHours() {
        return dosageFrequencyHours;
    }

    private void setDosageFrequencyHours(int dosageFrequencyHours) {
        this.dosageFrequencyHours = dosageFrequencyHours;
    }

    public Integer getDosageFrequencyMinutes() {
        return dosageFrequencyMinutes;
    }

    private void setDosageFrequencyMinutes(int dosageFrequencyMinutes) {
        this.dosageFrequencyMinutes = dosageFrequencyMinutes;
    }

    public List<MedicationNotification> getNotifications(Context context) throws DBFindException {
        if (notifications == null) {
            NotificationRepository notificationRepository = new NotificationRepository(context);
            setNotifications(notificationRepository.findMedicineNotifications(this.treatment.getId(), this.id));
        }

        return notifications;
    }

    private void setNotifications(List<MedicationNotification> notifications) {
        this.notifications = notifications;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Medicine medicine = (Medicine) obj;
        return id == medicine.id &&
                Objects.equals(treatment, medicine.treatment) &&
                Objects.equals(name, medicine.name) &&
                Objects.equals(activeSubstance, medicine.activeSubstance) &&
                Objects.equals(dose, medicine.dose) &&
                administrationRoute == medicine.administrationRoute &&
                Objects.equals(initialDosingTime, medicine.initialDosingTime) &&
                Objects.equals(dosageFrequencyHours, medicine.dosageFrequencyHours) &&
                Objects.equals(dosageFrequencyMinutes, medicine.dosageFrequencyMinutes);
    }
}
