/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.anagrafiche.client;


import it.mds.sdk.anagrafiche.client.entities.Datum;
import it.mds.sdk.anagrafiche.client.entities.Registry;
import it.mds.sdk.anagrafiche.client.entities.RegistryMetadata;
import it.mds.sdk.anagrafiche.client.entities.Types;
import it.mds.sdk.anagrafiche.client.gen.GetRegistriesNameListRequest;
import it.mds.sdk.anagrafiche.client.gen.GetRegistriesNameListResponse;
import it.mds.sdk.anagrafiche.client.gen.GetRegistriesRequest;
import it.mds.sdk.anagrafiche.client.gen.GetRegistriesResponse;
import it.mds.sdk.anagrafiche.client.utility.Utility;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Accenture
 * Orritos, Abis, Mattei, Pittarelli
 */

public class DownloaderClientRestImpl implements DownloaderClient {

    protected final Log log = LogFactory.getLog(this.getClass());

    /**
     * Nome dell'header che identifica la data di ultimo aggiornamento del file di anagrafica
     */
    public static final String LAST_UPDATE_HEADER = "X-ANAGRAFICHE-LASTUPDATE";
    /**
     * Nome dell'header che identifica la data di prossimo aggiornamento del file di anagrafica
     */
    public static final String NEXT_UPDATE_HEADER = "X-ANAGRAFICHE-NEXTUPDATE";

    public static final String X_APPLICATION_ID = "X-APPLICATION-ID";

    public static final String DATE_FORMAT = "yyyyMMdd_HHmmss";

    private static final String CLIENT_BASE_URL = "client.host";

    private static final String X_APPLICATION_ID_PROP_NAME = "client.rest.headers.x-pplication-id";

    private final ConcurrentHashMap<String, Date> callToServerDates = new ConcurrentHashMap<>(); //NOSONAR

    private final RestTemplate restTemplate;
    private String baseUrl;
    private Authorizer authorizer;
    private String xApplicationId;

    DownloaderClientRestImpl(Properties conf) {
        this.restTemplate = restTemplate(conf);
        if (conf != null && !conf.isEmpty()) {
            this.baseUrl = conf.getProperty(CLIENT_BASE_URL);
            this.xApplicationId = conf.getProperty(X_APPLICATION_ID_PROP_NAME);
        }
        this.authorizer=AuthorizerFactory.createAuthorizer(conf);
    }


    private RestTemplate restTemplate(Properties conf){
        return new RestTemplate(ignoreSslVerificationRequestFactory());
    }



    private ClientHttpRequestFactory ignoreSslVerificationRequestFactory() {
        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

        SSLContext sslContext = null;
        try {
            sslContext = org.apache.http.ssl.SSLContexts.custom()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }

        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(csf)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory();

        requestFactory.setHttpClient(httpClient);
        requestFactory.setReadTimeout(5000);
        requestFactory.setConnectTimeout(10000);

        return requestFactory;
    }

    /**
     * Metodo che ritorna la lista delle anagrafiche
     *
     * @return Lista dei nomi delle anagrafiche
     */
    @Override
    public List<String> getRegistries() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add(X_APPLICATION_ID, xApplicationId);
            Optional.ofNullable(authorizer).ifPresent(a -> a.authorize(headers));
            HttpEntity<Void> requestEntity = new HttpEntity<>(null, headers);
            ResponseEntity<List> response = send(this.baseUrl + "/registryList", HttpMethod.GET, requestEntity, List.class, Collections.emptyMap());
            return response.getBody();

        } catch (Exception exc) {
            log.error("[getRegistries] Calling " + this.baseUrl + ": " + exc.getMessage());
            return new ArrayList<>();
        }
    }


    /**
     * Permette il download di un'intera anagrafica,
     * utilizzando i seguenti valori di default per i parametri di sicurezza:
     * - "registryRole" = "TEST"
     * - "registryApplicationId" = "APP_ID_REGISTRYDOWNLOADER_CLIENT"
     * <p>
     * Il download di questa anagrafica non verrà forzato.
     *
     * @param registryName Il nome dell'anagrafica che si vuole ricevere.
     * @return Una nuova versione dell'anagrafica,
     * oppure un'anagrafica senza dati perchè è ancora valida la precedente versione scaricata dal client.
     */
    @Override
    public Registry retrieveRegistry(String registryName) {

        //recupera data ultima richiesta
        Date lastUpDate = retrieveLastUpdateDate(registryName);

        return retrieveRegistry(registryName, lastUpDate, null);
    }

    /**
     * Permette il download di un'intera anagrafica,
     * utilizzando i seguenti valori di default per i parametri di sicurezza:
     * - "registryRole" = "TEST"
     * - "registryApplicationId" = "APP_ID_REGISTRYDOWNLOADER_CLIENT"
     * <p>
     * Il download di questa anagrafica non verrà forzato.
     *
     * @param registryName Il nome dell'anagrafica che si vuole ricevere.
     * @param force        Parametro che può pilotare il download forzato dell'anagrafica.
     *                     Se 'true' l'anagrafica verrà forzata,
     *                     se 'fals', no.
     * @return Una nuova versione dell'anagrafica,
     * oppure un'anagrafica senza dati perchè è ancora valida la precedente versione scaricata dal client.
     */
    @Override
    public Registry retrieveRegistry(String registryName, boolean force) {

        // Default security map:
        Map<String, String> securityFields = new HashMap<>();

        return retrieveRegistry(registryName, securityFields, force);
    }

    @Override
    public Registry retrieveRegistry(String registryName, Map<String, String> securityFields) {

        //recupera data ultima richiesta
        Date lastUpDate = retrieveLastUpdateDate(registryName);

        return retrieveRegistry(registryName, lastUpDate, securityFields);
    }

    @Override
    public Registry retrieveRegistry(String registryName, Map<String, String> securityFields, boolean force) {

        if (force) {
            return retrieveRegistry(registryName, Date.from(Instant.EPOCH), securityFields);
        } else {
            return retrieveRegistry(registryName, securityFields);
        }
    }

    /**
     * Implementazione reale del metodo di retrieve dell'anagrafica
     *
     * @param registryName Nome dell'anagrafica
     * @param lastUpdate   Data e ora di ultimo aggiornamento dell'anagrafica
     * @return Un registry popolato con gli opportuni dati dell'anagrafica
     */
    private Registry retrieveRegistry(String registryName, Date lastUpdate, Map<String, String> securityFields) {
        try{
            Map<String, String> pathParams = new HashMap<>();
            pathParams.put("name", registryName);
            pathParams.put("downloadDate", new SimpleDateFormat(DATE_FORMAT).format(lastUpdate));

            HttpHeaders headers = new HttpHeaders();
            headers.add(X_APPLICATION_ID, xApplicationId);
            Optional.ofNullable(authorizer).ifPresent(a -> a.authorize(headers));
            HttpEntity<Void> requestEntity = new HttpEntity<>(null, headers);
            ResponseEntity<byte[]> response = send(this.baseUrl + "/registries/{name}/{downloadDate}", HttpMethod.GET, requestEntity, byte[].class, pathParams);


            return registryFromResponse(response, registryName);
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }



    public void registryFromInputStream(Registry result, InputStream stream) throws IOException {

        final InputStreamReader reader = new InputStreamReader(stream);
        final BufferedReader bufferedReader = new BufferedReader(reader);

        /** Extract metadata by reading the first three lines:
         * {
         *     "metadata":{"valid_from": "a", "valid_to": "b", "value": "c"},
         *     "types":{"a": "Date", "b": "Date", "c": "String"},
         **/
        bufferedReader.readLine(); // Discard the "{"

        final String metadataStr = bufferedReader.readLine().replace("\"metadata\":", "");
        final String typesStr = bufferedReader.readLine().replace("\"types\":", "");

        //inizializzo i metadata del Registry
        result.setMetadata(Utility.converter(metadataStr, RegistryMetadata.class));

        //inizializzo i types del registry
        result.setTypes(Utility.converter(typesStr, Types.class));

        bufferedReader.readLine(); // Discard "data:"
        bufferedReader.readLine(); // Discard "["

        Stream<Datum> data = bufferedReader.lines()
                .map(Utility::converter)
                .filter(Objects::nonNull);
        result.setData(data);
    }



    /**
     * Instanzia nuova anagrafica e valorizza i dati ricevuti dalla risposta
     * <p>
     * cod 200:
     * isNew = true: file ricevuto e aggiornato rispetto all'ultimo in possesso dal client.
     * valorizza tutti gli attributi della classe Registry
     * <p>
     * cod 304:
     * isNew = false: Il file in possesso del client è ancora valido.
     * valorizza solo la data di ultimo e prossimo aggiornamento del file da parte del server
     * <p>
     * Il body della risposta rappresenta sempre un file zip
     *
     * @param response     Response contentenete l'InputStream del file zip contenente anagrafica e i suoi metadata
     * @param registryName nome dell'anagrafica
     * @return un istanza della classe Registry
     * @throws IOException Exception
     */
    @SuppressWarnings("java:S5042")
    // Sopprime il warning sul getNextEntry(), attaccabile solo con un attacco Man-In-The-Middle
    private Registry registryFromResponse(ResponseEntity<byte[]> response, String registryName) throws IOException, ParseException {

        final Registry result = new Registry();

        HttpStatus statusCode = response.getStatusCode();

        if (statusCode == HttpStatus.OK || statusCode == HttpStatus.NOT_MODIFIED) {
            result.setName(registryName);
            this.setFileDatesFromResponse(result, response);

            //registra data richiesta in hashtable
            this.callToServerDates.put(registryName, new Date());
        }

        if (statusCode == HttpStatus.OK) {

            // 200 -> anagrafica nuova (isNews -> true)
            result.setNew(true);

            InputStream stream = new ByteArrayInputStream(response.getBody());
            ZipInputStream zis = new ZipInputStream(stream);

            // Posiziono lo stream sull'unico file (json) presente nello zip
            ZipEntry entry = zis.getNextEntry();

            if (entry != null) {

                // Imposto dati, metadata e types anagrafica dello stream del file json
                registryFromInputStream(result, zis);

            } else {

                // Segnaliamo l'errore all'esterno
                result.setNew(false);
            }

        } else if (statusCode == HttpStatus.NOT_MODIFIED) {

            // 304 -> anagrafica ancora valida (isNew -> false)
            result.setNew(false);
        }

        return result;
    }

    /**
     * Sets dates from the response on the passed registry
     *
     * @param result   Registry passed, to be filled with dates extracted from SOAP response
     * @param response The SOAP response from the remote service
     */
    private void setFileDatesFromResponse(Registry result, ResponseEntity<byte[]> response) throws ParseException {
        Optional<HttpHeaders> opHeaders = Optional.of(response).map(HttpEntity::getHeaders);
        Date lastUpdate = opHeaders.map(h -> h.getFirst(LAST_UPDATE_HEADER)).map(this::parseDate).orElse(new Date());
        Date nextUpdate = opHeaders.map(h -> h.getFirst(NEXT_UPDATE_HEADER)).map(this::parseDate).orElse(new Date(OffsetDateTime.now().plusDays(1).toInstant().toEpochMilli()));
        result.setLastUpdate(lastUpdate);
        result.setNextUpdate(nextUpdate);
    }

    private Date parseDate(String date) {
        try{
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.parse(date);}catch (ParseException pe) {
            throw new RuntimeException(pe.getMessage(), pe);
        }
    }


    /**
     * Recupera dalla hashtable la data di ultima richiesta dell'anagrafica
     *
     * @param registryName
     * @return
     */
    private Date retrieveLastUpdateDate(String registryName) {

        Date lastUpdate = this.callToServerDates.get(registryName);
        if (lastUpdate == null) {
            lastUpdate = Date.from(Instant.EPOCH);
        }

        return lastUpdate;
    }


    <T> ResponseEntity<T> send(String url, HttpMethod method, @Nullable HttpEntity<?> requestEntity,
                               Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException {

        return restTemplate.exchange(url, method, requestEntity, responseType, uriVariables);
    }

}