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
import it.mds.sdk.anagrafiche.client.protocol.JPLReader;
import it.mds.sdk.anagrafiche.client.utility.Utility;
import org.springframework.http.HttpStatus;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.support.MarshallingUtils;
import org.springframework.xml.transform.StringSource;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author Accenture
 * Orritos, Abis, Mattei, Pittarelli
 */

/*
 * Questa implementazione deve inviare il seguente messaggio SOAP (body d'esempio per la getRegistries):
 * 
 * <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:reg="http://mds.it/anagrafiche/sdk/registries">
	<SOAP-ENV:Header>
		<registryRole>TEST</registryRole>
		<registryApplicationId>APP_ID_REGISTRYDOWNLOADER_CLIENT</registryApplicationId>
		<wsse:Security xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd" xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd">
			<wsse:UsernameToken wsu:Id="UsernameToken-4">
				<wsse:Username>sdk_lombardia</wsse:Username>
				<wsse:Password Type="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText">Aa12345.**</wsse:Password>
				<wsse:Nonce EncodingType="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary">ucYNK1tD4mFDvV6KLBURZg==</wsse:Nonce>
				<wsu:Created>2022-05-30T13:28:01.516Z</wsu:Created>
			</wsse:UsernameToken>
		</wsse:Security>
	</SOAP-ENV:Header>
	<SOAP-ENV:Body>
		<reg:getRegistriesRequest>
			<reg:name>ANAG_INTERNAL_TEST_1</reg:name>
			<reg:downloadDate>19700108_000000</reg:downloadDate>
		</reg:getRegistriesRequest>
	</SOAP-ENV:Body>
 * </SOAP-ENV:Envelope>
 */
public class DownloaderClientImplementation extends WebServiceGatewaySupport implements DownloaderClient, JPLReader {

    private static final String APP_ID_REGISTRYDOWNLOADER_CLIENT = "APP_ID_REGISTRYDOWNLOADER_CLIENT";

	private static final String SECURITY_HEADER =
            "<wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">\n" +
            "    <wsse:UsernameToken wsu:Id=\"UsernameToken-4\">\n" +
            "        <wsse:Username>WSSE_USERNAME_PLACEHOLDER</wsse:Username>\n" +
            "        <wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\">WSSE_PASSWORD_PLACEHOLDER</wsse:Password>\n" +
            "        <wsse:Nonce EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\">ucYNK1tD4mFDvV6KLBURZg==</wsse:Nonce>\n" +
            "        <wsu:Created>WSU_TIME_PLACEHOLDER</wsu:Created>\n" +
            "    </wsse:UsernameToken>\n" +
            "</wsse:Security>\n";

    /**
     * Nome dell'header di autenticazione: definisce il ruolo per accedere al registry
     */
    public static final String SECURITY_REGISTRY_ROLE = "registryRole";

    /**
     * Nome dell'header di autenticazione: definisce l'ID dell'applicazione per accedere al registry
     */
    public static final String SECURITY_REGISTRY_APPLICATION_ID = "registryApplicationId";


    private static final String NAMESPACE_URI = "http://mds.it/anagrafiche/sdk/registries";
    private static final String CONTEXT_PATH = "it.mds.sdk.anagrafiche.client.gen";

    private final Hashtable<String, Date> callToServerDates; //NOSONAR

    private String endpointSoap;
    private String wsseUsername;
    private String wssePassword;

    /**
     * Parte dell'implementazione del pattern Singleton
     */
    private static final DownloaderClient instance = new DownloaderClientImplementation();

    /**
     * Costruttore private per l'implementazione del pattern Singleton
     */
    private DownloaderClientImplementation() {
        this.endpointSoap = "https://nsis.sanita.it/WSHUBPA/interop_new/services/DownloaderAnagrafiche"; // Esercizio URL esterna
        this.wsseUsername = "";
        this.wssePassword = "";

        this.callToServerDates = new Hashtable<>();
    }

    /**
     * Restituisce l'istanza del DownloaderClient.
     * @return Un'istanza del DownloaderClient
     */
    public static DownloaderClient instance() {
        return instance;
    }

    /**
     * Metodo che ritorna la lista delle anagrafiche
     * @return Lista dei nomi delle anagrafiche
     */
    @Override
    public List<String> getRegistries() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        Jaxb2Marshaller unmarshaller = new Jaxb2Marshaller();

        // Default security map:
        Map<String, String> securityFields = new HashMap<>();
        securityFields.put(SECURITY_REGISTRY_APPLICATION_ID, APP_ID_REGISTRYDOWNLOADER_CLIENT);

        try {

            return getWebServiceTemplate().sendAndReceive(getEndpointSoap(),
                message -> {
                    //Settiamo la request

                    //Settiamo la request nella SOAP
                    QName headerApplicationName = new QName(NAMESPACE_URI, SECURITY_REGISTRY_APPLICATION_ID);
                    SoapHeader soapHeader1 = ((SoapMessage) message).getSoapHeader();
                    soapHeader1.addHeaderElement(headerApplicationName).setText(securityFields.get(SECURITY_REGISTRY_APPLICATION_ID));
                    //---------------------------------
                    final String updatedSecurityHeader = updateWSSE(SECURITY_HEADER);

                    StringSource headerSource = new StringSource(updatedSecurityHeader);
                    Transformer transformer = TransformerFactory.newInstance().newTransformer();
                    transformer.transform(headerSource, soapHeader1.getResult());
                    //---------------------------------
                    GetRegistriesNameListRequest request = new GetRegistriesNameListRequest();
                    marshaller.setContextPath(CONTEXT_PATH);
                    MarshallingUtils.marshal(marshaller, request, message);
                },
                message -> {
                    unmarshaller.setContextPath(CONTEXT_PATH);
                    GetRegistriesNameListResponse response = (GetRegistriesNameListResponse) MarshallingUtils.unmarshal(unmarshaller, message);
                    return response.getRegistriesList();
                });

        } catch (Exception exc) {
            logger.error("[getRegistries] Calling " + getEndpointSoap() + ": " + exc.getMessage());
            return new ArrayList<>();
        }
    }

    private String updateWSSE(String base) {

        final TimeZone tz = TimeZone.getTimeZone("UTC");
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.mmm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz);

        final String nowAsISO = df.format(new Date());

        logger.debug("[updateWSSE] Using updated time for WSSE security: " + nowAsISO);

        String result = base.replace("WSU_TIME_PLACEHOLDER", nowAsISO);
        result = result.replace("WSSE_USERNAME_PLACEHOLDER", this.getWsseUsername());
        result = result.replace("WSSE_PASSWORD_PLACEHOLDER", this.getWssePassword());

        return result;
    }

    /**
     * Permette il download di un'intera anagrafica,
     * utilizzando i seguenti valori di default per i parametri di sicurezza:
     * - "registryRole" = "TEST"
     * - "registryApplicationId" = "APP_ID_REGISTRYDOWNLOADER_CLIENT"
     *
     * Il download di questa anagrafica non verrà forzato.
     *
     * @param registryName Il nome dell'anagrafica che si vuole ricevere.
     *
     * @return Una nuova versione dell'anagrafica,
     * oppure un'anagrafica senza dati perchè è ancora valida la precedente versione scaricata dal client.
     */
    @Override
    public Registry retrieveRegistry(String registryName) {

        // Default security map:
        Map<String, String> securityFields = new HashMap<>();
        securityFields.put(SECURITY_REGISTRY_ROLE, "TEST");
        securityFields.put(SECURITY_REGISTRY_APPLICATION_ID, APP_ID_REGISTRYDOWNLOADER_CLIENT);

        //recupera data ultima richiesta
        Date lastUpDate = retrieveLastUpdateDate(registryName);

        return retrieveRegistry(registryName, lastUpDate, securityFields);
    }

    /**
     * Permette il download di un'intera anagrafica,
     * utilizzando i seguenti valori di default per i parametri di sicurezza:
     * - "registryRole" = "TEST"
     * - "registryApplicationId" = "APP_ID_REGISTRYDOWNLOADER_CLIENT"
     *
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
    @Override
    public Registry retrieveRegistry(String registryName, boolean force) {

        // Default security map:
        Map<String, String> securityFields = new HashMap<>();
        securityFields.put(SECURITY_REGISTRY_ROLE, "TEST");
        securityFields.put(SECURITY_REGISTRY_APPLICATION_ID, APP_ID_REGISTRYDOWNLOADER_CLIENT);

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
     * @param lastUpdate Data e ora di ultimo aggiornamento dell'anagrafica
     *
     * @return Un registry popolato con gli opportuni dati dell'anagrafica
     */
    private Registry retrieveRegistry(String registryName, Date lastUpdate, Map<String, String> securityFields) {

        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        Jaxb2Marshaller unmarshaller = new Jaxb2Marshaller();
        Registry response;

        response = getWebServiceTemplate().sendAndReceive(getEndpointSoap(),
                message -> {
                    //Settiamo la request nella SOAP
                    QName headerApplicationName = new QName(NAMESPACE_URI, SECURITY_REGISTRY_APPLICATION_ID);
                    SoapHeader soapHeader1 = ((SoapMessage)message).getSoapHeader();
                    soapHeader1.addHeaderElement(headerApplicationName).setText(securityFields.get(SECURITY_REGISTRY_APPLICATION_ID));
                    //---------------------------------
                    final String updatedSecurityHeader = updateWSSE(SECURITY_HEADER);

                    StringSource headerSource = new StringSource(updatedSecurityHeader);
                    Transformer transformer = TransformerFactory.newInstance().newTransformer();
                    transformer.transform(headerSource, soapHeader1.getResult());
                    //---------------------------------
                    GetRegistriesRequest request = getRegistryRequest(registryName, lastUpdate);
                    marshaller.setContextPath(CONTEXT_PATH);
                    MarshallingUtils.marshal(marshaller, request, message);
                },
                message -> {
                    //settiamo e lavoriamo la response
                    unmarshaller.setContextPath(CONTEXT_PATH);
                    unmarshaller.setMtomEnabled(true);

                    GetRegistriesResponse getRegistriesResponse = (GetRegistriesResponse) MarshallingUtils.unmarshal(unmarshaller, message);

                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    message.writeTo(out);

                    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

                    out.flush();
                    out.close();

                    DataHandler stream = mimeAttachmentExtractor(in);

                    getRegistriesResponse.setStream(stream);

                    return registryFromResponse(getRegistriesResponse, registryName);
                });

        return response;
    }

    public DataHandler mimeAttachmentExtractor(InputStream isMtm) {

        DataHandler result = null;

        try {
            MimeMultipart mp = new MimeMultipart(new ByteArrayDataSource(isMtm, "application/zip"));

            int count = mp.getCount();

            for (int i = 0; i < count; i++) {
                BodyPart bodyPart = mp.getBodyPart(i);
                if (   "application/octet-stream".equalsIgnoreCase(bodyPart.getContentType())
                    || "application/zip".equalsIgnoreCase(bodyPart.getContentType()) ) {

                    result = bodyPart.getDataHandler();
                    break;
                }
            }

        } catch (Exception exc) {

            // TODO

            logger.error("[mimeAttachmentExtractor] Errore: " + exc.getMessage());
        }

        return result;
    }

    @Override
    @SuppressWarnings("java:S2677")  // Sopprime i warnings sulle readLine() commentate sotto
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
     * Ritorna la request
     *
     * @param registryName nome dell'anagrafica
     * @param lastUpload data del download richiesto
     * @return GetRegistriesRequest
     */
    private GetRegistriesRequest getRegistryRequest(String registryName, Date lastUpload) {

        //Converto Date in String
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String lastDownloadDate = sdf.format(lastUpload);

        GetRegistriesRequest request = new GetRegistriesRequest();
        request.setName(registryName);
        request.setDownloadDate(lastDownloadDate == null ? "19920414_090100" : lastDownloadDate);
        return request;
    }

    /**
     * Instanzia nuova anagrafica e valorizza i dati ricevuti dalla risposta
     *
     * cod 200:
     *      isNew = true: file ricevuto e aggiornato rispetto all'ultimo in possesso dal client.
     *      valorizza tutti gli attributi della classe Registry
     *
     * cod 304:
     *      isNew = false: Il file in possesso del client è ancora valido.
     *      valorizza solo la data di ultimo e prossimo aggiornamento del file da parte del server
     *
     * Il body della risposta rappresenta sempre un file zip
     *
     * @param response Response contentenete l'InputStream del file zip contenente anagrafica e i suoi metadata
     * @param registryName nome dell'anagrafica
     * @return un istanza della classe Registry
     * @throws IOException Exception
     */
    @SuppressWarnings("java:S5042")  // Sopprime il warning sul getNextEntry(), attaccabile solo con un attacco Man-In-The-Middle
    private Registry registryFromResponse(GetRegistriesResponse response, String registryName) throws IOException {

        final Registry result = new Registry();

        // Ritorna l'HTTP Status basandosi sulla response
        HttpStatus statusCode = getStatusCodeFromResponse(response);

        if (statusCode == HttpStatus.OK || statusCode == HttpStatus.NOT_MODIFIED) {
            result.setName(registryName);
            this.setFileDatesFromResponse(result, response);

            //registra data richiesta in hashtable
            this.callToServerDates.put(registryName, new Date());
        }

        if (statusCode == HttpStatus.OK) {

            // 200 -> anagrafica nuova (isNews -> true)
            result.setNew(true);

            InputStream stream = response.getStream().getInputStream();
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

        } else if(statusCode == HttpStatus.NOT_MODIFIED) {

            // 304 -> anagrafica ancora valida (isNew -> false)
            result.setNew(false);
        }

        return result;
    }

    /**
     * Sets dates from the response on the passed registry
     *
     * @param result Registry passed, to be filled with dates extracted from SOAP response
     * @param response The SOAP response from the remote service
     */
    private void setFileDatesFromResponse(Registry result, GetRegistriesResponse response) {
        result.setLastUpdate(response.getLastUpdate().toGregorianCalendar().getTime());
        result.setNextUpdate(response.getNextUpdate().toGregorianCalendar().getTime());
    }

    /**
     * Ritorna:
     *  - HttpStatus.OK             -> fileName è valorizzato e non è stringa vuota
     *  - HttpStatus.NOT_MODIFIED   -> fileName è null ma lastUpdate e nextUpdate sono valorizzate
     * @param response response
     * @return HttpStatus
     */
    private HttpStatus getStatusCodeFromResponse(GetRegistriesResponse response) {
        HttpStatus statusCode;

        if(response.getFilename() != null && !"".equals(response.getFilename().trim())) {
            statusCode = HttpStatus.OK;
        }
        else if((response.getFilename() == null || "".equals(response.getFilename().trim())) && (
                response.getLastUpdate() != null && response.getNextUpdate() != null)) {
            statusCode = HttpStatus.NOT_MODIFIED;
        }else{
            statusCode = HttpStatus.BAD_REQUEST;
        }
        return statusCode;
    }

    /**
     * Restituisce l'endpoint al quale viene indirizzata ogni richiesta per le anagrafiche
     *
     * @return URL completo dell'endpoint SOAP
     */
    @Override
    public String getEndpointSoap() {

        return this.endpointSoap;

        // return "https://nsis.sanita.it/WSHUBPA/interop_new/services/DownloaderAnagrafiche"; // Esercizio URL esterna
        // return "https://cooperazionecoll.salute.gov.it/WSHUBPA/interop_new/services/DownloaderAnagrafiche"; // Collaudo URL pubblica
        // return "http://10.175.6.34:8130/interop_new/services/DownloaderAnagrafiche"; // Collaudo URL interna
    }

    /**
     * Imposta l'host remoto al quale verranno richieste le anagrafiche.<br>
     * Se questo metodo non viene richiamato l'host è impostato a un valore di default.
     *
     * @param endpoint Stringa contenente l'intero URL dell'endpoint SOAP<br>
     *                 E.g.:<br>
     *                 <code>client.setEndpointSoap("https://nsis.sanita.it/WSHUBPA/interop_new/services/DownloaderAnagrafiche"); // Esercizio URL esterna</code><br>
     *                 <code>client.setEndpointSoap("https://cooperazionecoll.salute.gov.it/WSHUBPA/interop_new/services/DownloaderAnagrafiche"; // Collaudo URL pubblica</code><br>
     *                 <code>client.setEndpointSoap("http://10.175.6.34:8130/interop_new/services/DownloaderAnagrafiche"; // Collaudo URL interna</code><br>
     */
    @Override
    public void setEndpointSoap(String endpoint) {
        this.endpointSoap = endpoint;
    }

    /**
     * Restituisce la username WSSE (Web Services Security)
     *
     * @return Stringa con la username
     */
    @Override
    public String getWsseUsername() {
        return wsseUsername;
    }

    /**
     * Imposta la username WSSE (Web Services Security)
     *
     * @param wsseUsername Stringa con la username
     */
    @Override
    public void setWsseUsername(String wsseUsername) {
        this.wsseUsername = wsseUsername;
    }

    /**
     * Restituisce la password WSSE (Web Services Security)
     *
     * @return Stringa con la password, in chiaro
     */
    @Override
    public String getWssePassword() {
        return wssePassword;
    }

    /**
     * Imposta la password WSSE (Web Services Security)
     *
     * @param wssePassword Stringa con la password, in chiaro
     */
    @Override
    public void setWssePassword(String wssePassword) {
        this.wssePassword = wssePassword;
    }

    /**
     * Recupera dalla hashtable la data di ultima richiesta dell'anagrafica
     *
     * @param registryName
     * @return
     */
    private Date retrieveLastUpdateDate(String registryName) {

        Date lastUpdate = this.callToServerDates.get(registryName);
        if( lastUpdate == null ) {
            lastUpdate = Date.from(Instant.EPOCH);
        }

        return lastUpdate;
    }

    /**
     * Elabora l'inputStream contenente la lista dei nomi delle anagrafiche
     *
     * @param stream elenco nomi anagrafiche
     * @return Lista con i nomi delle anagrafiche disponibili
     */
    @SuppressWarnings("unused")
	private List<String> setRegistriesList(InputStream stream) throws IOException {

        final InputStreamReader reader = new InputStreamReader(stream);
        final BufferedReader bufferedReader = new BufferedReader(reader);

        return Collections.singletonList(bufferedReader.readLine()
                .replace("[", "")
                .replace("]", ""));
    }

}