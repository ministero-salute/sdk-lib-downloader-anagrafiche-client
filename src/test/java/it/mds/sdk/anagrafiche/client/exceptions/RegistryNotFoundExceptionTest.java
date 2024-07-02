/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.anagrafiche.client.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegistryNotFoundExceptionTest {

    @Test
    void getMessage() {
        try {
            throw new RegistryNotFoundException();

        } catch (RegistryNotFoundException exc) {

            assertEquals(RegistryNotFoundException.DEFAULT_EXCEPTION_MESSAGE, exc.getMessage());
        }


        String customMessage = "customMessage";
        String innerExceptionMessage = "innerExceptionMessage";
        Exception inner = new Exception(innerExceptionMessage);

        try {
            throw new RegistryNotFoundException(customMessage, inner);

        } catch (RegistryNotFoundException exc) {

            assertEquals(customMessage, exc.getMessage());
            assertEquals(innerExceptionMessage, exc.getCause().getMessage());
        }
    }

    @Test
    void getCause() {

        String innerExceptionMessage = "innerExceptionMessage";
        Exception inner = new Exception(innerExceptionMessage);

        try {
            throw new RegistryNotFoundException(inner);

        } catch (RegistryNotFoundException exc) {

            assertEquals(innerExceptionMessage, exc.getCause().getMessage());
        }
    }
}