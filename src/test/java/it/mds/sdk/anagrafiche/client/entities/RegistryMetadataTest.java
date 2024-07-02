/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.anagrafiche.client.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RegistryMetadataTest {

    private RegistryMetadata metadata;

    @BeforeEach
    void setUp() throws Exception {
        metadata = new RegistryMetadata();
    }

    @Test
    void constructor() {

        RegistryMetadata data = new RegistryMetadata();

        final String a = "a";
        final String b = "b";
        final String c = "c";

        assertEquals(a, data.getValidFrom());
        assertEquals(b, data.getValidTo());
        assertEquals(c, data.getValue());
    }

    @Test
    void setValidFrom() {
        final String vFrom = "vFrom";
        metadata.setValidFrom(vFrom);

        assertEquals(vFrom, metadata.getValidFrom());
    }

    @Test
    void setValidTo() {
        final String vTo = "vTo";
        metadata.setValidTo(vTo);

        assertEquals(vTo, metadata.getValidTo());
    }

    @Test
    void setValue() {
        final String newValue = "newValue";
        metadata.setValue(newValue);

        assertEquals(newValue, metadata.getValue());
    }
}