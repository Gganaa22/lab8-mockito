package mn.edu.must.sqat;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseRegistrationServiceTest {

    @Mock StudentRepository repository;
    @Mock EmailSender emailSender;

    CourseRegistrationService service;

    private static final String EMAIL = "s1@must.edu.mn";

    @BeforeEach
    void setUp() {
        service = new CourseRegistrationService(repository, emailSender);
    }

    private Student activeStudent(String... taken) {
        return new Student("S1", EMAIL, true, List.of(taken));
    }

    // A1
    @Test
    void register_activeStudentWithPrereqs_returnsOkSavesAndSendsEmail() {
        when(repository.findStudent("S1")).thenReturn(activeStudent("CS101"));
        when(repository.courseExists("CS201")).thenReturn(true);
        when(repository.prerequisites("CS201")).thenReturn(List.of("CS101"));

        String result = service.register("S1", "CS201");

        assertEquals("OK", result);
        verify(repository).saveRegistration("S1", "CS201");
        verify(emailSender).send(eq(EMAIL), anyString(), contains("CS201"));
    }

    // A2
    @Test
    void register_unknownStudent_returnsNoStudentAndDoesNotSaveOrEmail() {
        when(repository.findStudent("S9")).thenReturn(null);

        String result = service.register("S9", "CS201");

        assertEquals("ERROR_NO_STUDENT", result);
        verify(repository, never()).saveRegistration(any(), any());
        verify(emailSender, never()).send(any(), any(), any());
    }

    // A3
    @Test
    void register_inactiveStudent_returnsInactiveAndNeverTouchesEmail() {
        when(repository.findStudent("S1"))
                .thenReturn(new Student("S1", EMAIL, false, List.of()));

        String result = service.register("S1", "CS201");

        assertEquals("ERROR_INACTIVE_STUDENT", result);
        verifyNoInteractions(emailSender);
        verify(repository, never()).saveRegistration(any(), any());
    }

    // A4
    @Test
    void register_missingPrerequisite_returnsPrerequisitesAndDoesNotSave() {
        when(repository.findStudent("S1")).thenReturn(activeStudent("CS101"));
        when(repository.courseExists("CS301")).thenReturn(true);
        when(repository.prerequisites("CS301")).thenReturn(List.of("CS101", "CS102"));

        String result = service.register("S1", "CS301");

        assertEquals("ERROR_PREREQUISITES", result);
        verify(repository, never()).saveRegistration(any(), any());
    }

    // A5
    @Test
    void register_emailSenderThrows_stillReturnsOkAndSaves() {
        when(repository.findStudent("S1")).thenReturn(activeStudent("CS101"));
        when(repository.courseExists("CS201")).thenReturn(true);
        when(repository.prerequisites("CS201")).thenReturn(List.of("CS101"));
        doThrow(new RuntimeException("SMTP down"))
                .when(emailSender).send(anyString(), anyString(), anyString());

        String result = service.register("S1", "CS201");

        assertEquals("OK", result);
        verify(repository).saveRegistration("S1", "CS201");
    }

    // A6
    @Test
    void register_nullArguments_throwsIllegalArgumentAndNoInteractions() {
        assertThrows(IllegalArgumentException.class, () -> service.register(null, "CS201"));
        assertThrows(IllegalArgumentException.class, () -> service.register("S1", null));
        verifyNoInteractions(repository, emailSender);
    }

    // A7
    @Test
    void register_success_emailBodyContainsCourseCode() {
        when(repository.findStudent("S1")).thenReturn(activeStudent("CS101"));
        when(repository.courseExists("CS201")).thenReturn(true);
        when(repository.prerequisites("CS201")).thenReturn(List.of("CS101"));
        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);

        service.register("S1", "CS201");

        verify(emailSender).send(eq(EMAIL), anyString(), body.capture());
        assertTrue(body.getValue().contains("CS201"));
    }

    // A8
    @ParameterizedTest
    @CsvSource({
            "false, true, CS101, ERROR_NO_STUDENT",
            "true, false, CS101, ERROR_INACTIVE_STUDENT",
            "true, true, '', ERROR_PREREQUISITES"
    })
    void register_errorCases_returnExpectedCode(boolean exists, boolean active,
                                                String taken, String expected) {
        Student student = exists
                ? new Student("S1", EMAIL, active, taken.isEmpty() ? List.of() : List.of(taken))
                : null;
        lenient().when(repository.findStudent("S1")).thenReturn(student);
        lenient().when(repository.courseExists("CS201")).thenReturn(true);
        lenient().when(repository.prerequisites("CS201")).thenReturn(List.of("CS101"));

        assertEquals(expected, service.register("S1", "CS201"));
        verify(repository, never()).saveRegistration(any(), any());
    }
}
