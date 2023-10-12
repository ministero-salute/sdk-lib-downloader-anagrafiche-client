package it.mds.sdk.anagrafiche.client;

import it.mds.sdk.anagrafiche.client.entities.Datum;
import it.mds.sdk.anagrafiche.client.entities.Registry;
import it.mds.sdk.anagrafiche.client.protocol.JPLReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class DownloaderClientImplementationTest {

    // private static final String registryName = "anag_test";

    private static final String ANAG_BABY_FOOD_JSON = "{\n" +
            "    \"metadata\":{\"valid_from\": \"a\", \"valid_to\": \"b\", \"value\": \"c\"},\n" +
            "    \"types\":{\"a\": \"java.util.Date\", \"b\": \"java.util.Date\", \"c\": \"java.lang.String\"},\n" +
            "    \"data\":\n" +
            "    [\n" +
            "        {\"c\":\"A03RJ#F21.A07SE\",\"a\":\"2019-01-01\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03RC#F21.A07SE\",\"a\":\"2019-01-01\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03RL#F21.A07SE\",\"a\":\"2019-01-01\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A0EQL\",\"a\":\"2019-12-31\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A0EQL#F21.A07SE\",\"a\":\"2019-12-31\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A0EQM#F21.A07SE\",\"a\":\"2019-12-31\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03RB#F21.A07SE\",\"a\":\"2019-01-01\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A0EQM\",\"a\":\"2019-12-31\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03RD\",\"a\":\"2014-10-30\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03RE\",\"a\":\"2014-10-30\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03RF\",\"a\":\"2014-10-30\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03RG\",\"a\":\"2014-10-30\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03RH\",\"a\":\"2014-10-30\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03RJ\",\"a\":\"2014-10-30\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03RK\",\"a\":\"2014-10-30\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03PZ\",\"a\":\"2014-10-30\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03QA\",\"a\":\"2014-10-30\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03QB\",\"a\":\"2014-10-30\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03QC\",\"a\":\"2014-10-30\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03QD\",\"a\":\"2014-10-30\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03QE\",\"a\":\"2014-10-30\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03QF\",\"a\":\"2014-10-30\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03QG\",\"a\":\"2014-10-30\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03QH\",\"a\":\"2014-10-30\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03QJ\",\"a\":\"2014-10-30\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03QK\",\"a\":\"2014-10-30\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03QL\",\"a\":\"2014-10-30\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03QM\",\"a\":\"2014-10-30\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03QN\",\"a\":\"2014-10-30\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03QP\",\"a\":\"2014-10-30\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03QQ\",\"a\":\"2014-10-30\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03QR\",\"a\":\"2014-10-30\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03QS\",\"a\":\"2014-10-30\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03QT\",\"a\":\"2014-10-30\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03QV\",\"a\":\"2014-10-30\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03QY\",\"a\":\"2014-10-30\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03QZ\",\"a\":\"2014-10-30\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03RA\",\"a\":\"2014-10-30\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03RB\",\"a\":\"2014-10-30\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03RM\",\"a\":\"2014-10-30\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03RN\",\"a\":\"2014-10-30\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03RP\",\"a\":\"2014-10-30\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A0BZE\",\"a\":\"2014-10-30\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A0BZF\",\"a\":\"2014-10-30\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A16GS\",\"a\":\"2016-04-28\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"TEST\",\"a\":\"2020-04-01\",\"b\":\"2020-04-23\"},\n" +
            "        {\"c\":\"A03RC\",\"a\":\"2019-01-01\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03RL\",\"a\":\"2019-01-01\",\"b\":\"2999-12-31\"},\n" +
            "        {\"c\":\"A03QX\",\"a\":\"2019-12-31\",\"b\":\"2999-12-31\"}\n" +
            "    ]\n" +
            "}\n";

    private DownloaderClient client;

    @BeforeEach
    void setUp() throws Exception {
        this.client = DownloaderClientImplementation.instance();
    }

    @Test
    void testGetRegistries() {

        assertNotNull(client);

        // TODO

    }

    @Test
    void testRetrieveRegistry() {

        assertNotNull(client);

        // TODO
    }

    @Test
    void testRetrieveRegistry1() {

        assertNotNull(client);

        // TODO
    }

    @Test
    void testRetrieveRegistry2() {

        assertNotNull(client);

        // TODO
    }

    @Test
    void testRegistryFromInputStream() {

        assertNotNull(client);

        // TODO
    }

    @Test
    void testSetEndpointSoap() {

        client = DownloaderClientImplementation.instance();
        assertNotNull(client);

        String initVal = "http://localhost:8080/DownloaderAnagrafiche";

        client.setEndpointSoap(initVal);

        assertEquals(initVal, client.getEndpointSoap());

    }

    @Test
    void testSetWsseUsername() {

        client = DownloaderClientImplementation.instance();
        assertNotNull(client);

        String initVal = "userName";

        client.setWsseUsername(initVal);

        assertEquals(initVal, client.getWsseUsername());
    }

    @Test
    void testSetWssePassword() {

        client = DownloaderClientImplementation.instance();
        assertNotNull(client);

        String initVal = "password";

        client.setWssePassword(initVal);

        assertEquals(initVal, client.getWssePassword());
    }

    @Test
    void registryFromInputStream() {

        JPLReader reader = (JPLReader)client;

        Registry result = new Registry();

        try {
            reader.registryFromInputStream(result, new ByteArrayInputStream(ANAG_BABY_FOOD_JSON.getBytes(StandardCharsets.UTF_8)));

            assertNotNull(result);
            assertEquals(null, result.getName());

            Stream<Datum> data = result.getData();

            assertNotNull(data);

            List<Datum> list = data.collect(Collectors.toList());

            assertEquals(49, list.size());

        } catch (Exception e) {
            e.printStackTrace();
            fail("" + e);
        }
    }

    /* // N.B. Per questi test è necessario un server mock
    @Test
    void shouldRetrieveRegistry_withoutForce() {
        DownloaderClient client = DownloaderClientImplementation.instance();
        client.setEndpointSoap("localhost:8080");
        Map<String, String> securityFields = new HashMap<String, String>();
        securityFields.put("registryRole", "TEST");
        securityFields.put("registryApplicationId", "APP_ID_REGISTRYDOWNLOADER_CLIENT");

        try {
            Registry reg = client.retrieveRegistry(registryName, securityFields, false);
            assertNotNull(reg);
            assertNotNull(reg.getNextUpdate());
            assertNotNull(reg.getLastUpdate());

        } catch (MalformedRegistryException | RegistryNotFoundException e) {
            fail();
        }
    }

    @Test
    void shouldRetrieveRegistry_withForce() {
        DownloaderClient client = DownloaderClientImplementation.instance();
        client.setEndpointSoap("localhost:8080");
        Map<String, String> securityFields = new HashMap<String, String>();
        securityFields.put("registryRole", "TEST");
        securityFields.put("registryApplicationId", "APP_ID_REGISTRYDOWNLOADER_CLIENT");

        try {
            assertNotNull(client.retrieveRegistry(registryName, securityFields, true));
        } catch (MalformedRegistryException | RegistryNotFoundException e) {
            fail();
        }
    }

    @Test
    void shouldStreamFromRegistry() {

        DownloaderClient client = DownloaderClientImplementation.instance();
        client.setEndpointSoap("localhost:8080");
        Registry reg = null;
        Map<String, String> securityFields = new HashMap<String, String>();
        securityFields.put("registryRole", "TEST");
        securityFields.put("registryApplicationId", "APP_ID_REGISTRYDOWNLOADER_CLIENT");

        try {
            reg = client.retrieveRegistry(registryName, securityFields, true);

            assertNotNull(reg.getData());
            assertNotNull(reg.getMetadata());
            assertNotNull(reg.getTypes());
        } catch (MalformedRegistryException | RegistryNotFoundException e) {
            fail();
        }
    }

    @Test
    void shouldStreamFromRegistryFuture() throws MalformedRegistryException, RegistryNotFoundException {

        DownloaderClient client = DownloaderClientImplementation.instance();
        client.setEndpointSoap("localhost:8080");

        Map<String, String> securityFields = new HashMap<String, String>();
        securityFields.put("registryRole", "TEST");
        securityFields.put("registryApplicationId", "APP_ID_REGISTRYDOWNLOADER_CLIENT");

        //prima richiesta anagrafica può restituire dati
        Registry reg = client.retrieveRegistry(registryName, securityFields);

        assertNotNull(reg.getLastUpdate());
        assertNotNull(reg.getNextUpdate());
        assertNull(reg.getData());
        assertNull(reg.getMetadata());
        assertNull(reg.getTypes());
    }

    @Test
    void shouldListRegistry(){
        DownloaderClient client = DownloaderClientImplementation.instance();
        client.setEndpointSoap("localhost:8080");

        List<String> anagraficaLists = client.getRegistries();
        assertEquals(2,anagraficaLists.size());
    } */
}