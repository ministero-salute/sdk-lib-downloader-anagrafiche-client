/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.anagrafiche.client;

import org.apache.commons.lang3.StringUtils;

import java.util.Properties;

/**
 * Interfaccia d'ingresso al client:
 * espone gli unici metodi che potranno essere richiamati per scaricare le anagrafiche.<br>
 * <br>
 * Esempio di utilizzo:<br>
 * <code>
 * DownloaderClient client = DownloaderClientImplementation.instance();<br>
 * <br>
 * Registry reg = client.retrieveRegistry("some-registry");<br>
 * <br>
 * assertNotNull(reg);
 * </code>
 *
 * @author Accenture
 * Orritos, Abis, Mattei, Pittarelli
 */
public abstract class DownloaderClientFactory {

    private DownloaderClientFactory(){
        // intentionally empty
    }

    private static final String CLIENT_TYPE_PROPERTIES = "client.type";

    public static DownloaderClient createDownloaderClient(Properties conf) {
        String type = conf.getProperty(CLIENT_TYPE_PROPERTIES);
        if (StringUtils.isBlank(type)){
            type = ClientType.SOAP.name();
        }
        switch (ClientType.valueOf(type)) {
            case REST:
                return new DownloaderClientRestImpl(conf);
            case SOAP:
                return new DownloaderClientImplementation(conf);
            default:
                throw new RuntimeException("Unrecognized client type");
        }
    }
}
