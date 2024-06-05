package com.javierjordanluque.healthtrackr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;

import com.javierjordanluque.healthtrackr.db.repositories.MedicalAppointmentRepository;
import com.javierjordanluque.healthtrackr.db.repositories.NotificationRepository;
import com.javierjordanluque.healthtrackr.models.Location;
import com.javierjordanluque.healthtrackr.models.MedicalAppointment;
import com.javierjordanluque.healthtrackr.models.Treatment;
import com.javierjordanluque.healthtrackr.models.User;
import com.javierjordanluque.healthtrackr.models.enumerations.TreatmentCategory;
import com.javierjordanluque.healthtrackr.util.exceptions.DBDeleteException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBUpdateException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class MedicalAppointmentTest {
    private AutoCloseable mocks;
    @Mock
    private Context mockContext;
    private MockedConstruction<MedicalAppointmentRepository> mockMedicalAppointmentRepository;
    private Treatment treatment;

    @Before
    public void setUp() throws DBInsertException {
        mocks = MockitoAnnotations.openMocks(this);

        User user = new User("email@example.com", "FirstName", "LastName");
        treatment = new Treatment(null, user, "Title", ZonedDateTime.now().minusDays(1), null, null, TreatmentCategory.MEDICAL);
        user.setTreatments(new ArrayList<>(Collections.singletonList(treatment)));
        treatment.setAppointments(new ArrayList<>());
    }

    @After
    public void tearDown() throws Exception {
        mocks.close();

        if (mockMedicalAppointmentRepository != null)
            mockMedicalAppointmentRepository.close();
    }

    public Object[] medicalAppointmentAdditionParameters() {
        return new Object[]{
                new Object[]{ZonedDateTime.now(), "Subject", new Location(null, 60.0, -60.0)},
                new Object[]{ZonedDateTime.now().plusDays(50), null, new Location("Street 1", null, null)}
        };
    }
    @Test
    @Parameters(method = "medicalAppointmentAdditionParameters")
    public void testAddMedicalAppointment_ThenReturnAndInsertMedicalAppointment(ZonedDateTime dateTime, String subject, Location location)
            throws DBInsertException, DBFindException {
        mockMedicalAppointmentRepository = Mockito.mockConstruction(MedicalAppointmentRepository.class,
                (mock, context) -> when(mock.findTreatmentAppointments(1L)).thenReturn(new ArrayList<>()));

        MedicalAppointment expectedResult = new MedicalAppointment(mockContext, treatment, dateTime, subject, location);
        MedicalAppointment obtainedResult = treatment.getAppointments(mockContext).get(0);

        verify(mockMedicalAppointmentRepository.constructed().get(0), times(1)).insert(any(MedicalAppointment.class));
        assertEquals(obtainedResult, expectedResult);
    }

    public Object[] medicalAppointmentModificationParameters() {
        return new Object[]{
                new Object[]{ZonedDateTime.now(), "Subject", new Location(null, 60.0, -60.0),
                        ZonedDateTime.now().minusDays(360), null, new Location("Street 1", null, null)},
                new Object[]{ZonedDateTime.now().plusDays(50), null, new Location("Street 1",null, null),
                        ZonedDateTime.now(), null, new Location("Street 2", null, null)}
        };
    }
    @Test
    @Parameters(method = "medicalAppointmentModificationParameters")
    public void testModifyMedicalAppointment_ThenModifyAndUpdateMedicalAppointment(ZonedDateTime dateTime, String subject, Location location,
                                                                                   ZonedDateTime expectedDateTime, String expectedSubject,
                                                                                   Location expectedLocation)
            throws DBInsertException, DBFindException, DBDeleteException, DBUpdateException {
        mockMedicalAppointmentRepository = Mockito.mockConstruction(MedicalAppointmentRepository.class,
                (mock, context) -> doNothing().when(mock).update(any(MedicalAppointment.class)));

        MockedConstruction<NotificationRepository> mockNotificationRepository = Mockito.mockConstruction(NotificationRepository.class,
                (mock, context) -> when(mock.findAppointmentNotification(anyLong())).thenReturn(null));

        MedicalAppointment medicalAppointment = new MedicalAppointment(null, treatment, dateTime, subject, location);
        treatment.setAppointments(new ArrayList<>(Collections.singletonList(medicalAppointment)));

        medicalAppointment.modifyMedicalAppointment(mockContext, expectedDateTime, expectedSubject, expectedLocation);
        MedicalAppointment obtainedResult = treatment.getAppointments(mockContext).get(0);

        verify(mockMedicalAppointmentRepository.constructed().get(0), times(1)).update(any(MedicalAppointment.class));
        assertEquals(expectedDateTime, obtainedResult.getDateTime());
        assertEquals(expectedSubject, obtainedResult.getSubject());
        assertEquals(expectedLocation, obtainedResult.getLocation());

        mockNotificationRepository.close();
    }

    public Object[] medicalAppointmentEliminationParameters() {
        return new Object[]{
                new Object[]{ZonedDateTime.now(), "Subject", new Location(null, 60.0, -60.0)},
                new Object[]{ZonedDateTime.now().plusDays(50), null, new Location("Street 1", null, null)}
        };
    }
    @Test
    @Parameters(method = "medicalAppointmentEliminationParameters")
    public void testDeleteMedicalAppointment_ThenRemoveMedicalAppointmentFromTreatment(ZonedDateTime dateTime, String subject, Location location)
            throws DBInsertException, DBFindException, DBDeleteException {
        mockMedicalAppointmentRepository = Mockito.mockConstruction(MedicalAppointmentRepository.class,
                (mock, context) -> doNothing().when(mock).delete(any(MedicalAppointment.class)));

        MockedConstruction<NotificationRepository> mockNotificationRepository = Mockito.mockConstruction(NotificationRepository.class,
                (mock, context) -> when(mock.findMedicineNotifications(anyLong(), anyLong())).thenReturn(new ArrayList<>()));

        MedicalAppointment medicalAppointment = new MedicalAppointment(null, treatment, dateTime, subject, location);
        treatment.setAppointments(new ArrayList<>(Collections.singletonList(medicalAppointment)));

        treatment.removeAppointment(mockContext, medicalAppointment);

        verify(mockMedicalAppointmentRepository.constructed().get(0), times(1)).delete(medicalAppointment);
        assertTrue(treatment.getAppointments(mockContext).isEmpty());

        mockNotificationRepository.close();
    }
}
