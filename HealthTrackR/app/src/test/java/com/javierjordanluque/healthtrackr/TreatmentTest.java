package com.javierjordanluque.healthtrackr;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;

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
import org.mockito.junit.MockitoJUnitRunner;

import java.time.ZonedDateTime;
import java.util.ArrayList;

@RunWith(MockitoJUnitRunner.class)
public class TreatmentTest {
    @Mock
    private Context mockContext;
    private User user;
    private MockedConstruction<TreatmentRepository> mockTreatmentRepository;

    @Before
    public void setUp() {
        user = new User("email@example.com", "FirstName", "LastName");
        user.setTreatments(new ArrayList<>());
    }

    @After
    public void tearDown() {
        if (mockTreatmentRepository != null)
            mockTreatmentRepository.close();
    }

    @Test
    public void testAddTreatment_ThenReturnAndInsertTreatment() throws DBInsertException, DBFindException {
        String title = "Title";
        ZonedDateTime startDate =  ZonedDateTime.now();
        ZonedDateTime endDate = ZonedDateTime.now().plusDays(1);
        String diagnosis = "";
        TreatmentCategory treatmentCategory = TreatmentCategory.MEDICAL;

        mockTreatmentRepository = Mockito.mockConstruction(TreatmentRepository.class,
                (mock, context) -> when(mock.insert(any(Treatment.class))).thenReturn(1L));

        Treatment expectedResult = new Treatment(mockContext, user, title, startDate, endDate, diagnosis, treatmentCategory);
        Treatment obtainedResult = user.getTreatments(mockContext).get(0);

        verify(mockTreatmentRepository.constructed().get(0), times(1)).insert(any(Treatment.class));
        assertEquals(obtainedResult, expectedResult);
    }


    @Test
    public void testModifyTreatment_ThenModifyAndUpdateTreatment() throws DBInsertException, DBFindException, DBDeleteException, DBUpdateException {
        String title = "Title";
        ZonedDateTime startDate =  ZonedDateTime.now();
        ZonedDateTime endDate = ZonedDateTime.now().plusDays(1);
        String diagnosis = "";
        TreatmentCategory treatmentCategory = TreatmentCategory.MEDICAL;

        String expectedTitle = "Title Modified";
        ZonedDateTime expectedStartDate =  ZonedDateTime.now().minusDays(1);
        ZonedDateTime expectedEndDate = null;
        String expectedDiagnosis = "Diagnosis";
        TreatmentCategory expectedTreatmentCategory = TreatmentCategory.ALTERNATIVE;

        mockTreatmentRepository = Mockito.mockConstruction(TreatmentRepository.class,
                (mock, context) -> {
                    when(mock.insert(any(Treatment.class))).thenReturn(1L);
                    doNothing().when(mock).update(any(Treatment.class));
                });

        Treatment treatment = new Treatment(mockContext, user, title, startDate, endDate, diagnosis, treatmentCategory);
        treatment.modifyTreatment(mockContext, expectedTitle, expectedStartDate, expectedEndDate, expectedDiagnosis, expectedTreatmentCategory);
        Treatment obtainedResult = user.getTreatments(mockContext).get(0);

        assertEquals(obtainedResult.getTitle(), expectedTitle);
        assertEquals(obtainedResult.getStartDate(), expectedStartDate);
        assertEquals(obtainedResult.getEndDate(), expectedEndDate);
        assertEquals(obtainedResult.getDiagnosis(), expectedDiagnosis);
        assertEquals(obtainedResult.getCategory(), expectedTreatmentCategory);
    }
}
