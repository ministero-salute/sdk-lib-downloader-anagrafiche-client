/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.anagrafiche.client.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TypesTest {

    @Test
    void testConstructor() {

        Types types = new Types();

        assertNotNull(types);
        assertEquals(java.util.Date.class, types.getA());
        assertEquals(java.util.Date.class, types.getB());
        assertEquals(java.lang.String.class, types.getC());
    }

    @Test
    void setA() {

        final Types types = new Types();

        types.setA(java.util.Date.class);

        assertEquals(java.util.Date.class, types.getA());
    }

    @Test
    void setB() {

        final Types types = new Types();

        types.setB(java.util.Date.class);

        assertEquals(java.util.Date.class, types.getB());
    }

    @Test
    void setC() {

        final Types types = new Types();

        types.setC(java.lang.String.class);

        assertEquals(java.lang.String.class, types.getC());
    }
}