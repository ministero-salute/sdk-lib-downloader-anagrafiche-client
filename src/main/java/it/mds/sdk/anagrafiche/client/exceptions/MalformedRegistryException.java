package it.mds.sdk.anagrafiche.client.exceptions;

/**
 * @author Accenture
 * Orritos, Abis, Mattei, Pittarelli
 */

public class MalformedRegistryException extends Exception {

    public static final String DEFAULT_EXCEPTION_MESSAGE = "Anagrafica malformata o inutilizzabile";

    public MalformedRegistryException() {
        super(DEFAULT_EXCEPTION_MESSAGE);
    }

    public MalformedRegistryException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public MalformedRegistryException(Throwable throwable) {
        super(DEFAULT_EXCEPTION_MESSAGE, throwable);
    }
}
