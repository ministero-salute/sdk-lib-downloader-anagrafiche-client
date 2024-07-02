/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.anagrafiche.client.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MalformedRegistryExceptionTest {

    @Test
    void getMessage() {

        try {
            throw new MalformedRegistryException();

        } catch (MalformedRegistryException exc) {

            assertEquals(MalformedRegistryException.DEFAULT_EXCEPTION_MESSAGE, exc.getMessage());
        }


        String customMessage = "customMessage";
        String innerExceptionMessage = "innerExceptionMessage";
        Exception inner = new Exception(innerExceptionMessage);

        try {
            throw new MalformedRegistryException(customMessage, inner);

        } catch (MalformedRegistryException exc) {

            assertEquals(customMessage, exc.getMessage());
            assertEquals(innerExceptionMessage, exc.getCause().getMessage());
        }
    }

    @Test
    void getCause() {

        String innerExceptionMessage = "innerExceptionMessage";
        Exception inner = new Exception(innerExceptionMessage);

        try {
            throw new MalformedRegistryException(inner);

        } catch (MalformedRegistryException exc) {

            assertEquals(inner, exc.getCause());
            assertEquals(innerExceptionMessage, exc.getCause().getMessage());
        }
    }
}