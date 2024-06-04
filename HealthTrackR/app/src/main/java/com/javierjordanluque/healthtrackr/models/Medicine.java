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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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
                    int dosageFrequencyHours, int dosageFrequencyMinutes) throws DBInsertException {
        this.treatment = treatment;
        this.name = name;
        this.activeSubstance = activeSubstance;
        this.dose = dose;
        this.administrationRoute = administrationRoute;
        this.initialDosingTime = initialDosingTime;
        this.dosageFrequencyHours = dosageFrequencyHours;
        this.dosageFrequencyMinutes = dosageFrequencyMinutes;

        if (context != null)
            this.treatment.addMedicine(context, this);
    }

    public void schedulePreviousMedicationNotification(Context context, int previousMinutes) throws DBInsertException, DBDeleteException {
        if (previousMinutes > 0 && PermissionManager.hasNotificationPermission(context)) {
            long timestamp = initialDosingTime.minusMinutes(previousMinutes).toInstant().toEpochMilli();
            long totalDosageFrequencyMinutes = TimeUnit.HOURS.toMinutes(dosageFrequencyHours) + dosageFrequencyMinutes;

            if ((totalDosageFrequencyMinutes == 0 && timestamp > System.currentTimeMillis()) ||
                    totalDosageFrequencyMinutes > previousMinutes) {
                MedicationNotification previousNotification = new MedicationNotification(this, timestamp);
                NotificationRepository notificationRepository = new NotificationRepository(context);
                previousNotification.setId(notificationRepository.insert(previousNotification));

                if (totalDosageFrequencyMinutes != 0) {
                    try {
                        NotificationScheduler.scheduleInexactRepeatingNotification(context, previousNotification);
                    } catch (NotificationException exception) {
                        notificationRepository.delete(previousNotification);
                    }
                } else {
                    NotificationScheduler.scheduleInexactNotification(context, previousNotification);
                }

                if (notifications == null)
                    this.setNotifications(new ArrayList<>());
                notifications.add(previousNotification);
            }
        }
    }

    public void scheduleMedicationNotification(Context context) throws DBInsertException, DBDeleteException {
        if (PermissionManager.hasNotificationPermission(context)) {
            long timestamp = initialDosingTime.toInstant().toEpochMilli();

            if (dosageFrequencyHours != 0 || dosageFrequencyMinutes != 0 || timestamp > System.currentTimeMillis()) {
                MedicationNotification notification = new MedicationNotification(this, timestamp);
                NotificationRepository notificationRepository = new NotificationRepository(context);
                notification.setId(notificationRepository.insert(notification));

                if (dosageFrequencyHours != 0 || dosageFrequencyMinutes != 0) {
                    try {
                        NotificationScheduler.scheduleInexactRepeatingNotification(context, notification);
                    } catch (NotificationException exception) {
                        notificationRepository.delete(notification);
                    }
                } else {
                    NotificationScheduler.scheduleInexactNotification(context, notification);
                }

                if (notifications == null)
                    this.setNotifications(new ArrayList<>());
                notifications.add(notification);
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
        } else if (this.dose != null && dose == null) {
            setDose(null);
            medicine.setDose(Integer.MIN_VALUE);
        }

        if ((this.administrationRoute == null && administrationRoute != null ) || (administrationRoute != null && !this.administrationRoute.equals(administrationRoute))) {
            setAdministrationRoute(administrationRoute);
            medicine.setAdministrationRoute(this.administrationRoute);
        }

        ZonedDateTime oldInitialDosingTime = this.initialDosingTime;
        boolean modifiedInitialDosingTime = false;
        if (!this.initialDosingTime.equals(initialDosingTime)) {
            setInitialDosingTime(initialDosingTime);
            medicine.setInitialDosingTime(this.initialDosingTime);

            modifiedInitialDosingTime = true;
        }

        boolean modifiedDosingFrequency = false;
        if (!this.dosageFrequencyHours.equals(dosageFrequencyHours) || !this.dosageFrequencyMinutes.equals(dosageFrequencyMinutes)) {
            if (!this.dosageFrequencyHours.equals(dosageFrequencyHours)) {
                setDosageFrequencyHours(dosageFrequencyHours);
                medicine.setDosageFrequencyHours(this.dosageFrequencyHours);
            }

            if (!this.dosageFrequencyMinutes.equals(dosageFrequencyMinutes)) {
                setDosageFrequencyMinutes(dosageFrequencyMinutes);
                medicine.setDosageFrequencyMinutes(this.dosageFrequencyMinutes);
            }

            modifiedDosingFrequency = true;
        }

        if (modifiedInitialDosingTime || modifiedDosingFrequency) {
            for (MedicationNotification medicationNotification :  new ArrayList<>(getNotifications(context))) {
                if (medicationNotification.getTimestamp() != oldInitialDosingTime.toInstant().toEpochMilli()) {
                    int previousMinutes = (int) ChronoUnit.MINUTES.between(Instant.ofEpochMilli(medicationNotification.getTimestamp())
                            .atZone(oldInitialDosingTime.getZone()), oldInitialDosingTime);

                    NotificationScheduler.cancelNotification(context, medicationNotification);
                    schedulePreviousMedicationNotification(context, previousMinutes);
                } else {
                    NotificationScheduler.cancelNotification(context, medicationNotification);
                    scheduleMedicationNotification(context);
                }
            }
        }

        if (!(medicine.getDose() == null && medicine.getAdministrationRoute() == null && medicine.getInitialDosingTime() == null && medicine.getDosageFrequencyMinutes() == null &&
                medicine.getDosageFrequencyHours() == null)) {
            MedicineRepository medicineRepository = new MedicineRepository(context);
            medicineRepository.update(medicine);
        }
    }

    public void modifyMedicineNotifications(Context context, int previousNotificationTimeHours, int previousNotificationTimeMinutes, boolean previousNotificationStatus,
                                           boolean dosingNotificationStatus) throws DBFindException, DBDeleteException, DBInsertException {
        MedicationNotification previousNotification = null;
        MedicationNotification exactNotification = null;

        for (MedicationNotification notification : notifications) {
            if (notification.getTimestamp() != getInitialDosingTime().toInstant().toEpochMilli()) {
                previousNotification = notification;
            } else {
                exactNotification = notification;
            }
        }

        int previousMinutes = (int) (TimeUnit.HOURS.toMinutes(previousNotificationTimeHours) + previousNotificationTimeMinutes);
        long timestamp = initialDosingTime.minusMinutes(previousMinutes).toInstant().toEpochMilli();

        if (previousNotification != null && (!previousNotificationStatus || previousNotification.getTimestamp() != timestamp))
            NotificationScheduler.cancelNotification(context, previousNotification);
        if (previousNotificationStatus && (previousNotification == null || previousNotification.getTimestamp() != timestamp))
            schedulePreviousMedicationNotification(context, previousMinutes);

        if (exactNotification != null && !dosingNotificationStatus)
            NotificationScheduler.cancelNotification(context, exactNotification);
        if (dosingNotificationStatus && exactNotification == null)
            scheduleMedicationNotification(context);
    }

    public ZonedDateTime calculateNextDose() {
        ZonedDateTime nextDose = null;

        if (!treatment.isFinished()) {
            Duration frequency = Duration.ofHours(dosageFrequencyHours).plusMinutes(dosageFrequencyMinutes);

            if (initialDosingTime.isAfter(ZonedDateTime.now())) {
                nextDose = initialDosingTime;
            } else if (!frequency.isZero()) {
                long dosesElapsed = Duration.between(initialDosingTime, ZonedDateTime.now()).toMinutes() / frequency.toMinutes();
                nextDose = initialDosingTime.plus(frequency.multipliedBy(dosesElapsed + 1));
            }
        }

        return nextDose;
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

    public void setTreatment(Treatment treatment) {
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

            for (MedicationNotification medicationNotification : notifications)
                medicationNotification.setMedicine(this);
        }

        return notifications;
    }

    public void setNotifications(List<MedicationNotification> notifications) {
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
