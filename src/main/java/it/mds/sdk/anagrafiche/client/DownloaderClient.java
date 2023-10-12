package it.mds.sdk.anagrafiche.client;

import it.mds.sdk.anagrafiche.client.entities.Registry;
import it.mds.sdk.anagrafiche.client.exceptions.MalformedRegistryException;
import it.mds.sdk.anagrafiche.client.exceptions.RegistryNotFoundException;

import java.util.List;
import java.util.Map;

/**
 * Interfaccia d'ingresso al client:
 * espone gli unici metodi che potranno essere richiamati per scaricare le anagrafiche.<br>
 * <br>
 * Esempio di utilizzo:<br>
 * <code>
 *     DownloaderClient client = DownloaderClientImplementation.instance();<br>
 * <br>
 *     Registry reg = client.retrieveRegistry("some-registry");<br>
 * <br>
 *     assertNotNull(reg);
 * </code>
 *
 * @author Accenture
 * Orritos, Abis, Mattei, Pittarelli
 */
public interface DownloaderClient {

    /**
     * Restituisce l'endpoint al quale viene indirizzata ogni richiesta per le anagrafiche
     *
     * @return URL completo dell'endpoint SOAP
     */
    String getEndpointSoap();

    /**
     * Imposta l'host remoto al quale verranno richieste le anagrafiche.<br>
     * Se questo metodo non viene richiamato l'host è impostato a un valore di default.
     *
     * @param endpoint Stringa contenente l'intero URL dell'endpoint SOAP<br>
     *                 E.g.:<br>
     *                 <code>client.setRemoteHost("https://nsis.sanita.it/WSHUBPA/interop_new/services/DownloaderAnagrafiche"); // Esercizio URL esterna</code><br>
     *                 <code>client.setRemoteHost("https://cooperazionecoll.salute.gov.it/WSHUBPA/interop_new/services/DownloaderAnagrafiche"; // Collaudo URL pubblica</code><br>
     *                 <code>client.setRemoteHost("http://10.175.6.34:8130/interop_new/services/DownloaderAnagrafiche"; // Collaudo URL interna</code><br>
     */
    void setEndpointSoap(String endpoint);

    /**
     * Restituisce la username WSSE (Web Services Security)
     *
     * @return Stringa con la username
     */
    String getWsseUsername();

    /**
     * Imposta la username WSSE (Web Services Security)
     *
     * @param wsseUsername Stringa con la username
     */
    void setWsseUsername(String wsseUsername);

    /**
     * Restituisce la password WSSE (Web Services Security)
     *
     * @return Stringa con la password, in chiaro
     */
    String getWssePassword();

    /**
     * Imposta la password WSSE (Web Services Security)
     *
     * @param wssePassword Stringa con la password, in chiaro
     */
    void setWssePassword(String wssePassword);



    /**
     * Restituisce la lista dei nomi di tutte le anagrafiche.
     *
     * @return Una lista di nomi di anagrafiche.
     */
    List<String> getRegistries();

    /**
     * Permette il download di un'intera anagrafica,
     * utilizzando i seguenti valori di default per i parametri di sicurezza:<br>
     * - "registryRole" = "TEST"<br>
     * - "registryApplicationId" = "APP_ID_REGISTRYDOWNLOADER_CLIENT"<br>
     * <br>
     * Il download di questa anagrafica non verrà forzato.
     *
     * @param registryName Il nome dell'anagrafica che si vuole ricevere.
     *
     * @return Una nuova versione dell'anagrafica,
     * oppure un'anagrafica senza dati perchè è ancora valida la precedente versione scaricata dal client.
     */
    Registry retrieveRegistry(String registryName);

    /**
     * Permette il download di un'intera anagrafica,
     * utilizzando i seguenti valori di default per i parametri di sicurezza:<br>
     * - "registryRole" = "TEST"<br>
     * - "registryApplicationId" = "APP_ID_REGISTRYDOWNLOADER_CLIENT"<br>
     * <br>
     * Il download di questa anagrafica non verrà forzato.
     *
     * @param registryName Il nome dell'anagrafica che si vuole ricevere.
     * @param force Parametro che può pilotare il download forzato dell'anagrafica.
     *              Se 'true' l'anagrafica verrà forzata,
     *              se 'fals', no.
     *
     * @return Una nuova versione dell'anagrafica,
     * oppure un'anagrafica senza dati perchè è ancora valida la precedente versione scaricata dal client.
     */
    Registry retrieveRegistry(String registryName, boolean force);

    /**
     * Permette il download di un'intera anagrafica.
     *
     * @param registryName Il nome dell'anagrafica che si vuole ricevere.
     *
     * @return Una nuova versione dell'anagrafica,
     * oppure un'anagrafica senza dati perchè è ancora valida la precedente versione scaricata dal client.
     */
    Registry retrieveRegistry(String registryName, Map<String, String> securityFields) throws MalformedRegistryException, RegistryNotFoundException;

    /**
     * Permette il download di un'intera anagrafica,
     * anche se non ne esiste una nuova versione sul downloader
     *
     * @param registryName Il nome dell'anagrafica che si vuole ricevere.
     * @param force Parametro che istruisce il client a forzare il download dell'anagrafica in qualsiasi caso.<br>
     *              Chiamare questo metodo con 'force = false' equivale a chiamare direttamente 'retrieveRegistry(String registryName)'.
     *
     * @return Una versione dell'anagrafica con i dati sempre popolati
     */
    Registry retrieveRegistry(String registryName, Map<String, String> securityFields, boolean force) throws MalformedRegistryException, RegistryNotFoundException;

}
