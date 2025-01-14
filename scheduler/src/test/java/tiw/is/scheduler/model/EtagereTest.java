package tiw.is.scheduler.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EtagereTest {

    @Test
    void testConflit() {
        var e1 = new Etagere("e1");
        var e2 = new Etagere("e2");
        assertFalse(e1.hasConflitWith(e2));
        assertFalse(e2.hasConflitWith(e1));
        e1.addConflit(e2);
        assertTrue(e1.hasConflitWith(e2));
        e1.removeConflit(e2);
        assertFalse(e1.hasConflitWith(e2));
    }
}
