package com.javierjordanluque.healthtrackr;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.Context;

import com.javierjordanluque.healthtrackr.db.repositories.UserRepository;
import com.javierjordanluque.healthtrackr.models.User;
import com.javierjordanluque.healthtrackr.models.UserCredentials;
import com.javierjordanluque.healthtrackr.util.AuthenticationService;
import com.javierjordanluque.healthtrackr.util.exceptions.AuthenticationException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBFindException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBInsertException;
import com.javierjordanluque.healthtrackr.util.exceptions.DBUpdateException;
import com.javierjordanluque.healthtrackr.util.exceptions.ExceptionManager;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationServiceTest {
    @Mock
    private Context mockContext;
    private final String TAG = "AUTHENTICATION";
    private static final String ERROR = "E";
    private final String exceptionMessage = "Authentication exception";
    private MockedStatic<ExceptionManager> mockExceptionManager;
    private MockedConstruction<UserRepository> mockUserRepository;

    @Before
    public void setUp() {
        mockExceptionManager = mockStatic(ExceptionManager.class);
    }

    @After
    public void tearDown() {
        if (mockExceptionManager != null)
            mockExceptionManager.close();

        if (mockUserRepository != null)
            mockUserRepository.close();
    }

    @Test
    public void testRegister_WhenExistingEmail_ThenAuthenticationException() {
        String email = "existing@example.com";
        String password = "12345678Aa*";
        String firstName = "FirstName";
        String lastName = "LastName";

        mockUserRepository = Mockito.mockConstruction(UserRepository.class,
                (mock, context) -> when(mock.findUserCredentials(email)).thenReturn(new UserCredentials(0, null)));
        when(mockContext.getString(R.string.error_existing_email)).thenReturn(exceptionMessage);
        mockExceptionManager.when(() -> ExceptionManager.log(ERROR, TAG, AuthenticationException.class.getSimpleName(), exceptionMessage, null)).thenAnswer(invocation -> null);

        assertThrows(AuthenticationException.class, () -> {
            AuthenticationService.register(mockContext, email, password, firstName, lastName);

            verify(mockUserRepository.constructed().get(0), times(1)).findUserCredentials(email);
            mockExceptionManager.verify(() -> ExceptionManager.log(ERROR, TAG, AuthenticationException.class.getSimpleName(), exceptionMessage, null), times(1));
        });
    }

    @Test
    public void testRegister_WhenInvalidEmail_ThenAuthenticationException() {
        String email = "invalid@example";
        String password = "12345678Aa*";
        String firstName = "FirstName";
        String lastName = "LastName";

        mockUserRepository = Mockito.mockConstruction(UserRepository.class,
                (mock, context) -> when(mock.findUserCredentials(email)).thenReturn(null));
        when(mockContext.getString(R.string.error_invalid_email_requirements)).thenReturn(exceptionMessage);
        mockExceptionManager.when(() -> ExceptionManager.log(ERROR, TAG, AuthenticationException.class.getSimpleName(), exceptionMessage, null)).thenAnswer(invocation -> null);

        assertThrows(AuthenticationException.class, () -> {
            AuthenticationService.register(mockContext, email, password, firstName, lastName);

            verify(mockUserRepository.constructed().get(0), times(1)).findUserCredentials(email);
            mockExceptionManager.verify(() -> ExceptionManager.log(ERROR, TAG, AuthenticationException.class.getSimpleName(), exceptionMessage, null), times(1));
        });
    }

    @Test
    public void testRegister_WhenInvalidPassword_ThenAuthenticationException() {
        String email = "valid@example.com";
        String password = "12345678";
        String firstName = "FirstName";
        String lastName = "LastName";

        mockUserRepository = Mockito.mockConstruction(UserRepository.class,
                (mock, context) -> when(mock.findUserCredentials(email)).thenReturn(null));
        when(mockContext.getString(R.string.authentication_helper_password)).thenReturn(exceptionMessage);
        mockExceptionManager.when(() -> ExceptionManager.log(ERROR, TAG, AuthenticationException.class.getSimpleName(), exceptionMessage, null)).thenAnswer(invocation -> null);

        assertThrows(AuthenticationException.class, () -> {
            AuthenticationService.register(mockContext, email, password, firstName, lastName);

            verify(mockUserRepository.constructed().get(0), times(1)).findUserCredentials(email);
            mockExceptionManager.verify(() -> ExceptionManager.log(ERROR, TAG, AuthenticationException.class.getSimpleName(), exceptionMessage, null), times(1));
        });
    }

    @Test
    public void testRegister_WhenValidCredentials_ThenReturnUser() throws AuthenticationException, DBFindException, DBInsertException, DBUpdateException {
        String email = "valid@example.com";
        String password = "12345678Aa*";
        String firstName = "FirstName";
        String lastName = "LastName";

        long id = 1;
        User expectedResult = new User(email, firstName, lastName);
        expectedResult.setId(id);

        mockUserRepository = Mockito.mockConstruction(UserRepository.class,
                (mock, context) -> {
                    when(mock.findUserCredentials(email)).thenReturn(null);
                    when(mock.insert(any(User.class))).thenReturn(id);
                    doNothing().when(mock).updateUserCredentials(any(UserCredentials.class));
                });

        User obtainedResult = AuthenticationService.register(mockContext, email, password, firstName, lastName);

        verify(mockUserRepository.constructed().get(0), times(1)).findUserCredentials(email);
        verify(mockUserRepository.constructed().get(0), times(1)).insert(any(User.class));
        verify(mockUserRepository.constructed().get(0), times(1)).updateUserCredentials(any(UserCredentials.class));
        assertEquals(obtainedResult, expectedResult);
    }
}
