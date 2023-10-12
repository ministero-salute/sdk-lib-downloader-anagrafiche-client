package it.mds.sdk.anagrafiche.client.exceptions;

/**
 * @author Accenture
 * Orritos, Abis, Mattei, Pittarelli
 */

public class RegistryNotFoundException extends Exception {

    public static final String DEFAULT_EXCEPTION_MESSAGE = "Anagrafica non trovata";

    public RegistryNotFoundException() {
        super(DEFAULT_EXCEPTION_MESSAGE);
    }

    public RegistryNotFoundException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public RegistryNotFoundException(Throwable throwable) {
        super(DEFAULT_EXCEPTION_MESSAGE, throwable);
    }
}
