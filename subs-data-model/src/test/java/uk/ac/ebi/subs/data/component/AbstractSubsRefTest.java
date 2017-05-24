package uk.ac.ebi.subs.data.component;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by rolando on 22/05/2017.
 */
public class AbstractSubsRefTest {

    private class MockRefClass extends AbstractSubsRef { }

    @Test
    public void testIsAccessioned_HasAccession() {
        MockRefClass mockRefObject = new MockRefClass();
        mockRefObject.setAccession("test");

        assertTrue(mockRefObject.isAccessioned());
    }

    @Test
    public void testIsAccessioned_HasNoAccession() {
        MockRefClass mockRefObject = new MockRefClass();

        assertFalse(mockRefObject.isAccessioned());
    }
}
