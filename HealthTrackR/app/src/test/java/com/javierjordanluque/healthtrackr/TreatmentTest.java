package com.javierjordanluque.healthtrackr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;

import com.javierjordanluque.healthtrackr.db.repositories.MedicalAppointmentRepository;
import com.javierjordanluque.healthtrackr.db.repositories.MedicineRepository;
import com.javierjordanluque.healthtrackr.db.repositories.TreatmentRepository;
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
public class TreatmentTest {
    private AutoCloseable mocks;
    @Mock
    private Context mockContext;
    private MockedConstruction<TreatmentRepository> mockTreatmentRepository;
    private User user;

    @Before
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);

        user = new User("email@example.com", "FirstName", "LastName");
        user.setTreatments(new ArrayList<>());
    }

    @After
    public void tearDown() throws Exception {
        mocks.close();

        if (mockTreatmentRepository != null)
            mockTreatmentRepository.close();
    }

    public Object[] treatmentAdditionParameters() {
        return new Object[]{
                new Object[]{"Title", ZonedDateTime.now(), ZonedDateTime.now().plusDays(1), "", TreatmentCategory.MEDICAL},
                new Object[]{"Another Title", ZonedDateTime.now().minusDays(2), ZonedDateTime.now().plusDays(2), "Initial Diagnosis",
                        TreatmentCategory.PHYSIOTHERAPY}
        };
    }
    @Test
    @Parameters(method = "treatmentAdditionParameters")
    public void testAddTreatment_ThenReturnAndInsertTreatment(String title, ZonedDateTime startDate, ZonedDateTime endDate, String diagnosis,
                                                              TreatmentCategory treatmentCategory) throws DBInsertException, DBFindException {
        mockTreatmentRepository = Mockito.mockConstruction(TreatmentRepository.class,
                (mock, context) -> when(mock.insert(any(Treatment.class))).thenReturn(1L));

        Treatment expectedResult = new Treatment(mockContext, user, title, startDate, endDate, diagnosis, treatmentCategory);
        Treatment obtainedResult = user.getTreatments(mockContext).get(0);

        verify(mockTreatmentRepository.constructed().get(0), times(1)).insert(any(Treatment.class));
        assertEquals(obtainedResult, expectedResult);
    }

    public Object[] treatmentModificationParameters() {
        return new Object[]{
                new Object[]{"Title", ZonedDateTime.now(), ZonedDateTime.now().plusDays(1), "", TreatmentCategory.MEDICAL,
                        "Title Modified", ZonedDateTime.now().minusDays(1), null, "Diagnosis", TreatmentCategory.ALTERNATIVE},
                new Object[]{"Another Title", ZonedDateTime.now().minusDays(2), ZonedDateTime.now().plusDays(2), "Initial Diagnosis", TreatmentCategory.CHRONIC,
                        "Updated Title", ZonedDateTime.now().minusDays(3), ZonedDateTime.now().plusDays(3), "Updated Diagnosis", TreatmentCategory.REHABILITATION}
        };
    }
    @Test
    @Parameters(method = "treatmentModificationParameters")
    public void testModifyTreatment_ThenModifyAndUpdateTreatment(String title, ZonedDateTime startDate, ZonedDateTime endDate, String diagnosis,
                                                                 TreatmentCategory treatmentCategory, String expectedTitle, ZonedDateTime expectedStartDate,
                                                                 ZonedDateTime expectedEndDate, String expectedDiagnosis,
                                                                 TreatmentCategory expectedTreatmentCategory)
            throws DBInsertException, DBFindException, DBDeleteException, DBUpdateException {
        mockTreatmentRepository = Mockito.mockConstruction(TreatmentRepository.class,
                (mock, context) -> doNothing().when(mock).update(any(Treatment.class)));

        Treatment treatment = new Treatment(null, user, title, startDate, endDate, diagnosis, treatmentCategory);
        user.setTreatments(new ArrayList<>(Collections.singletonList(treatment)));

        treatment.modifyTreatment(mockContext, expectedTitle, expectedStartDate, expectedEndDate, expectedDiagnosis, expectedTreatmentCategory);
        Treatment obtainedResult = user.getTreatments(mockContext).get(0);

        verify(mockTreatmentRepository.constructed().get(0), times(1)).update(any(Treatment.class));
        assertEquals(expectedTitle, obtainedResult.getTitle());
        assertEquals(expectedStartDate, obtainedResult.getStartDate());
        assertEquals(expectedEndDate, obtainedResult.getEndDate());
        assertEquals(expectedDiagnosis, obtainedResult.getDiagnosis());
        assertEquals(expectedTreatmentCategory, obtainedResult.getCategory());
    }

    public Object[] treatmentEliminationParameters() {
        return new Object[]{
                new Object[]{"Title", ZonedDateTime.now(), ZonedDateTime.now().plusDays(1), "", TreatmentCategory.MEDICAL},
                new Object[]{"Another Title", ZonedDateTime.now().minusDays(2), ZonedDateTime.now().plusDays(2), "Initial Diagnosis",
                        TreatmentCategory.PHYSIOTHERAPY}
        };
    }
    @Test
    @Parameters(method = "treatmentEliminationParameters")
    public void testDeleteTreatment_ThenRemoveTreatmentFromUser(String title, ZonedDateTime startDate, ZonedDateTime endDate, String diagnosis,
                                                                 TreatmentCategory treatmentCategory)
            throws DBInsertException, DBFindException, DBDeleteException {
        mockTreatmentRepository = Mockito.mockConstruction(TreatmentRepository.class,
                (mock, context) -> doNothing().when(mock).delete(any(Treatment.class)));

        MockedConstruction<MedicineRepository> mockMedicineRepository = Mockito.mockConstruction(MedicineRepository.class,
                (mock, context) -> when(mock.findTreatmentMedicines(1L)).thenReturn(new ArrayList<>()));

        MockedConstruction<MedicalAppointmentRepository> mockMedicalAppointmentRepository = Mockito.mockConstruction(MedicalAppointmentRepository.class,
                (mock, context) -> when(mock.findTreatmentAppointments(1L)).thenReturn(new ArrayList<>()));

        Treatment treatment = new Treatment(null, user, title, startDate, endDate, diagnosis, treatmentCategory);
        user.setTreatments(new ArrayList<>(Collections.singletonList(treatment)));

        user.removeTreatment(mockContext, treatment);

        verify(mockTreatmentRepository.constructed().get(0), times(1)).delete(treatment);
        assertTrue(user.getTreatments(mockContext).isEmpty());

        mockMedicineRepository.close();
        mockMedicalAppointmentRepository.close();
    }
}
