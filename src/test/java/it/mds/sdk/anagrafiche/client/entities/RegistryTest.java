package it.mds.sdk.anagrafiche.client.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class RegistryTest {

    private Registry reg;

    @BeforeEach
    void setUp() throws Exception {
        reg = new Registry();
    }

    @Test
    void setNew() {

        assertNotNull(reg);

        reg.setNew(true);

        assertTrue(reg.isNew());
    }

    @Test
    void setLastUpdate() {

        assertNotNull(reg);

        final Date now = new Date();

        reg.setLastUpdate(now);
        assertEquals(now, reg.getLastUpdate());
    }

    @Test
    void setNextUpdate() {

        assertNotNull(reg);

        final Date now = new Date();

        reg.setNextUpdate(now);
        assertEquals(now, reg.getNextUpdate());
    }

    @Test
    void setName() {

        assertNotNull(reg);

        final String name = "a name";

        reg.setName(name);
        assertEquals(name, reg.getName());
    }

    @Test
    void setTypes() {

        assertNotNull(reg);

        // TODO
    }

    @Test
    void setMetadata() {

        assertNotNull(reg);

        // TODO
    }

    @Test
    void setData() {

        assertNotNull(reg);

        // TODO
    }
}