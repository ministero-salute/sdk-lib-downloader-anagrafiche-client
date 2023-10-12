package it.mds.sdk.anagrafiche.client.entities;


import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class AbcDatumTest {

    private final String initVal = "1900-01-01";
    private final String endVal  = "9999-12-31";


    @Test
    void testDetailedConstructor() {
        final String value = "c";

        AbcDatum abcDatum = new AbcDatum(initVal, endVal, value);

        assertEquals(initVal, abcDatum.getA());
        assertEquals(endVal,  abcDatum.getB());
        assertEquals(value,   abcDatum.getC());
    }

    @Test
    void testToDatum_shouldHandleNullDates() {

        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        try {
            final Date aDate = format.parse(initVal);
            final Date bDate = format.parse(endVal);

            final AbcDatum abcDatum = new AbcDatum();
            abcDatum.setA(null);
            abcDatum.setB(null);
            abcDatum.setC(null);

            assertNull(abcDatum.getA());
            assertNull(abcDatum.getB());
            assertNull(abcDatum.getC());

            final Datum result = abcDatum.toDatum();

            assertEquals(aDate, result.getValidFrom());
            assertEquals(bDate, result.getValidTo());
            assertNull(result.getValue());

        } catch (ParseException e) {
            e.printStackTrace();

            fail("" + e);
        }

    }

    @Test
    void testToDatum_shouldHandleEmptyStringDates() {

        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        try {
            final Date aDate = format.parse(initVal);
            final Date bDate = format.parse(endVal);

            final AbcDatum abcDatum = new AbcDatum();
            abcDatum.setA("");
            abcDatum.setB("");
            abcDatum.setC("");

            assertEquals("", abcDatum.getA());
            assertEquals("", abcDatum.getB());
            assertEquals("", abcDatum.getC());

            final Datum result = abcDatum.toDatum();

            assertEquals(aDate, result.getValidFrom());
            assertEquals(bDate, result.getValidTo());
            assertEquals("",    result.getValue());

        } catch (ParseException e) {
            e.printStackTrace();

            fail("" + e);
        }
    }

    @Test
    void testDoesntThrowExceptions() {

        final AbcDatum abcDatum = new AbcDatum("wrong", "wrong", "c");

        try {

            abcDatum.toDatum();

        } catch (Exception exc) {

            exc.printStackTrace();

            fail("" + exc);
        }
    }

    @Test
    void testOverriddenToString() {

        final String value = "c";

        final AbcDatum abcDatum = new AbcDatum(initVal, endVal, value);

        assertNotNull(abcDatum.toString());
        assertNotEquals("", abcDatum.toString());
    }
}