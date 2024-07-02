/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.anagrafiche.client.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.mds.sdk.anagrafiche.client.entities.AbcDatum;
import it.mds.sdk.anagrafiche.client.entities.Datum;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Utility class with mostly data-converting methods
 *
 * @author Accenture
 * Orritos, Abis, Mattei, Pittarelli
 */
public class Utility {

    static final Log logger = LogFactory.getLog(Utility.class);

    /**
     * Locked constructor
     */
    private Utility() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Mapper used by all the utility methods
     */
    private static final ObjectMapper mapper;

    /**
     * Date-format used by the mapper
     */
    private static final DateFormat df;



    /*
     * Initializes the mapper
     */
    static {
        mapper = new ObjectMapper();
        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        mapper.setDateFormat(df);
    }


    /**
     * Converts a JSON string to a Datum object
     *
     * @param jsonPart A String containing a JSON
     *
     * @return A Datum object if parseable as that, null otherwise
     */
    public static Datum converter(String jsonPart) {
        
        if (jsonPart == null) {
            return null;
        }

        final String trimmedJsonPart = jsonPart.trim();
        
        if (!"".equals(trimmedJsonPart) && !"]".equals(trimmedJsonPart) && !"}".equals(trimmedJsonPart)) {

            jsonPart = jsonPart.replace(",{", "{");
            jsonPart = jsonPart.replace("},","}");

            try{
                AbcDatum abcDatum = mapper.readValue(jsonPart, AbcDatum.class);
                return (abcDatum != null) ? abcDatum.toDatum() : null;
                
            }  catch (JsonProcessingException e) {

                logger.warn("[converter] Impossibile convertire il dato " + jsonPart + " ", e);

                return null;
            }
            
        } else {
            return null;
        }
    }

    /**
     * Converts a JSON string to a generic T object
     *
     * @param jsonPart A String containing a JSON
     *
     * @return A T object if parseable as that, null otherwise
     */
    public static <T> T converter(String jsonPart, Class<T> classToMap) {

        if (jsonPart == null) {
            return null;
        }

        final String trimmedJsonPart = jsonPart.trim();

        if (!"".equals(trimmedJsonPart) && !"]".equals(trimmedJsonPart) && !"}".equals(trimmedJsonPart)) {

            jsonPart = jsonPart.replace(",{", "{");
            jsonPart = jsonPart.replace("},", "}");

            try {
                return mapper.readValue(jsonPart, classToMap);

            } catch (JsonProcessingException e) {

                logger.warn("[converter] Impossibile convertire il dato " + jsonPart + " ", e);

                return null;
            }

        } else {
            return null;
        }
    }
}