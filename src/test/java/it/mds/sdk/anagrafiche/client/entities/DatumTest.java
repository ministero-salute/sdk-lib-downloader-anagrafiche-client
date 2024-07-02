/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.anagrafiche.client.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatumTest {

    private final Date from    = new Date(123456789L);
    private final Date to      = new Date(987654321L);
    private final String value = "value";
    private Datum datum;

    @BeforeEach
    void setUp() throws Exception {
        datum = new Datum(from, to, value);
    }

    @Test
    void constructor() {
        assertEquals(datum.getValidFrom(), from);
        assertEquals(datum.getValidTo(),   to);
        assertEquals(datum.getValue(),     value);
    }

    @Test
    void setValidFrom() {
        final Date now = new Date();
        datum.setValidFrom(now);

        assertEquals(now, datum.getValidFrom());
    }

    @Test
    void setValidTo() {
        final Date now = new Date();
        datum.setValidTo(now);

        assertEquals(now, datum.getValidTo());
    }

    @Test
    void setValue() {
        final String newValue = "newValue";
        datum.setValue(newValue);

        assertEquals(newValue, datum.getValue());
    }
}