package com.javierjordanluque.healthtrackr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;

import com.javierjordanluque.healthtrackr.db.repositories.MedicineRepository;
import com.javierjordanluque.healthtrackr.db.repositories.NotificationRepository;
import com.javierjordanluque.healthtrackr.models.Medicine;
import com.javierjordanluque.healthtrackr.models.Treatment;
import com.javierjordanluque.healthtrackr.models.User;
import com.javierjordanluque.healthtrackr.models.enumerations.AdministrationRoute;
import com.javierjordanluque.healthtrackr.models.enumerations.TreatmentCategory;
import com.javierjordanluque.healthtrackr.util.PermissionManager;
import com.javierjordanluque.healthtrackr.util.exceptions.DBDeleteException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;
import com.javierjordanluque.healthtrackr.util.notifications.MedicationNotification;
import com.javierjordanluque.healthtrackr.util.notifications.NotificationScheduler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class MedicineTest {
    private AutoCloseable mocks;
    @Mock
    private Context mockContext;
    private MockedConstruction<MedicineRepository> mockMedicineRepository;
    private Treatment treatment;

    @Before
    public void setUp() throws DBInsertException {
        mocks = MockitoAnnotations.openMocks(this);

        User user = new User("email@example.com", "FirstName", "LastName");
        treatment = new Treatment(null, user, "Title", ZonedDateTime.now().minusDays(1), null, null, TreatmentCategory.MEDICAL);
        user.setTreatments(new ArrayList<>(Collections.singletonList(treatment)));
        treatment.setMedicines(new ArrayList<>());
    }

    @After
    public void tearDown() throws Exception {
        mocks.close();

        if (mockMedicineRepository != null)
            mockMedicineRepository.close();
    }

    public Object[] medicineAdditionParameters() {
        return new Object[]{
                new Object[]{"Name", "Active substance", 500, AdministrationRoute.NASAL, ZonedDateTime.now(), 1, 5},
                new Object[]{"Another Name", null, null, AdministrationRoute.UNSPECIFIED, ZonedDateTime.now().minusDays(1), 0, 0},
        };
    }
    @Test
    @Parameters(method = "medicineAdditionParameters")
    public void testAddMedicine_ThenReturnAndInsertMedicine(String name, String activeSubstance, Integer dose, AdministrationRoute administrationRoute,
                                                              ZonedDateTime initialDosingTime, int dosageFrequencyHours, int dosageFrequencyMinutes)
            throws DBInsertException, DBFindException {
        mockMedicineRepository = Mockito.mockConstruction(MedicineRepository.class,
                (mock, context) -> when(mock.findTreatmentMedicines(1L)).thenReturn(new ArrayList<>()));

        Medicine expectedResult = new Medicine(mockContext, treatment, name, activeSubstance, dose, administrationRoute, initialDosingTime,
                dosageFrequencyHours, dosageFrequencyMinutes);
        Medicine obtainedResult = treatment.getMedicines(mockContext).get(0);

        verify(mockMedicineRepository.constructed().get(0), times(1)).insert(any(Medicine.class));
        assertEquals(obtainedResult, expectedResult);
    }

    public Object[] medicineDosageFrequencyNotZeroOrLessOrEqualThanPreviousMinutesParameters() {
        return new Object[]{
                new Object[]{"Name", "Active substance", 500, AdministrationRoute.NASAL, ZonedDateTime.now(), 0, 50, 60},
                new Object[]{"Another Name", null, null, AdministrationRoute.UNSPECIFIED, ZonedDateTime.now().minusDays(1), 0, 30, 30},
        };
    }
    @Test
    @Parameters(method = "medicineDosageFrequencyNotZeroOrLessOrEqualThanPreviousMinutesParameters")
    public void testSchedulePreviousMedicationNotification_WhenDosageFrequencyIsNotZeroLessOrEqualThanPreviousMinutes_ThenNoNotificationSettled(
            String name, String activeSubstance, Integer dose, AdministrationRoute administrationRoute, ZonedDateTime initialDosingTime,
            int dosageFrequencyHours, int dosageFrequencyMinutes, int previousMinutes) throws DBInsertException, DBFindException, DBDeleteException {
        MockedStatic<PermissionManager> mockPermissionManager = mockStatic(PermissionManager.class);
        mockPermissionManager.when(() -> PermissionManager.hasNotificationPermission(mockContext)).thenReturn(true);

        Medicine medicine = new Medicine(null, treatment, name, activeSubstance, dose, administrationRoute, initialDosingTime,
                dosageFrequencyHours, dosageFrequencyMinutes);
        treatment.setMedicines(new ArrayList<>(Collections.singletonList(medicine)));
        medicine.setNotifications(new ArrayList<>());

        medicine.schedulePreviousMedicationNotification(mockContext, previousMinutes);

        mockPermissionManager.close();

        assertTrue(medicine.getNotifications(mockContext).isEmpty());
    }

    public Object[] medicineDosageFrequencyZeroOrGreaterThanPreviousMinutesParameters() {
        return new Object[]{
                new Object[]{"Name", "Active substance", 500, AdministrationRoute.NASAL, ZonedDateTime.now(), 1, 1, 60},
                new Object[]{"Another Name", null, null, AdministrationRoute.UNSPECIFIED, ZonedDateTime.now().plusDays(1), 0, 0, 30},
        };
    }
    @Test
    @Parameters(method = "medicineDosageFrequencyZeroOrGreaterThanPreviousMinutesParameters")
    public void testSchedulePreviousMedicationNotification_WhenDosageFrequencyIsGreaterThanPreviousMinutes_ThenNotificationSettled(
            String name, String activeSubstance, Integer dose, AdministrationRoute administrationRoute, ZonedDateTime initialDosingTime,
            int dosageFrequencyHours, int dosageFrequencyMinutes, int previousMinutes) throws DBInsertException, DBFindException, DBDeleteException {
        MockedStatic<PermissionManager> mockPermissionManager = mockStatic(PermissionManager.class);
        mockPermissionManager.when(() -> PermissionManager.hasNotificationPermission(mockContext)).thenReturn(true);

        MockedConstruction<NotificationRepository> mockNotificationRepository = Mockito.mockConstruction(NotificationRepository.class,
                (mock, context) -> when(mock.insert(any(MedicationNotification.class))).thenReturn(1L));

        MockedStatic<NotificationScheduler> mockNotificationScheduler = mockStatic(NotificationScheduler.class);
        mockNotificationScheduler.when(() -> {
            NotificationScheduler.scheduleInexactRepeatingNotification(any(Context.class), any(MedicationNotification.class));
            NotificationScheduler.scheduleInexactNotification(any(Context.class), any(MedicationNotification.class));
        }).thenAnswer(invocation -> null);

        Medicine medicine = new Medicine(null, treatment, name, activeSubstance, dose, administrationRoute, initialDosingTime,
                dosageFrequencyHours, dosageFrequencyMinutes);
        treatment.setMedicines(new ArrayList<>(Collections.singletonList(medicine)));
        medicine.setNotifications(new ArrayList<>());

        medicine.schedulePreviousMedicationNotification(mockContext, previousMinutes);

        mockPermissionManager.close();
        mockNotificationRepository.close();
        mockNotificationScheduler.close();

        assertFalse(medicine.getNotifications(mockContext).isEmpty());
    }
}
