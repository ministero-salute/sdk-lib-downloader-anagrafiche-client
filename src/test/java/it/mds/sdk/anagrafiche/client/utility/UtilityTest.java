/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.anagrafiche.client.utility;

import it.mds.sdk.anagrafiche.client.entities.Datum;
import it.mds.sdk.anagrafiche.client.entities.RegistryMetadata;
import it.mds.sdk.anagrafiche.client.entities.Types;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtilityTest {

    @Test
    void testConverter_shouldHandleJson() {
        Datum result = Utility.converter(",{\"c\":\"030303\",\"a\":\"2005-01-01\",\"b\":\"2016-02-29\"}");

        assertNotNull(result);
        assertNotNull(result.getValidFrom());
        assertNotNull(result.getValidTo());
        assertNotNull(result.getValue());


        result = Utility.converter(",{\"c\":\"030303\"}");

        assertNotNull(result);
        assertNotNull(result.getValidFrom());
        assertNotNull(result.getValidTo());
        assertNotNull(result.getValue());


        result = Utility.converter(",{\"c\":\"030303\",\"b\":\"2016-02-29\"}");

        assertNotNull(result);
        assertNotNull(result.getValidFrom());
        assertNotNull(result.getValidTo());
        assertNotNull(result.getValue());


        result = Utility.converter(",{\"c\":\"030303\",\"a\":\"2005-01-01\"}");

        assertNotNull(result);
        assertNotNull(result.getValidFrom());
        assertNotNull(result.getValidTo());
        assertNotNull(result.getValue());


        result = Utility.converter(",{\"c\":\"030303\",\"a\":\"\",\"b\":\"\"}");

        assertNotNull(result);
        assertNotNull(result.getValidFrom());
        assertNotNull(result.getValidTo());
        assertNotNull(result.getValue());


        result = Utility.converter(",{\"c\":\"030303\",\"a\":null,\"b\":null}");

        assertNotNull(result);
        assertNotNull(result.getValidFrom());
        assertNotNull(result.getValidTo());
        assertNotNull(result.getValue());
    }

    @Test
    void testConverter_shouldHandleOtherThanJson() {
        Datum result = Utility.converter("");

        assertNull(result);

        result = Utility.converter(" ");

        assertNull(result);

        result = Utility.converter("}");

        assertNull(result);

        result = Utility.converter("]}");

        assertNull(result);

        Types types = Utility.converter("{\"a\": \"java.util.Date\", \"b\": \"java.util.Date\", \"c\": \"java.lang.String\"}", Types.class);

        assertNotNull(types);
        assertEquals(java.util.Date.class, types.getA());
        assertEquals(java.util.Date.class, types.getB());
        assertEquals(java.lang.String.class, types.getC());

        RegistryMetadata metadata = Utility.converter("{\"valid_from\": \"a\", \"valid_to\": \"b\", \"value\": \"c\"}", RegistryMetadata.class);

        assertNotNull(metadata);
    }
}