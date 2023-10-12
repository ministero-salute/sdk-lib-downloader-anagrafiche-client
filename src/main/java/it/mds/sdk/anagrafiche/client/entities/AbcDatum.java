package it.mds.sdk.anagrafiche.client.entities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * La più piccola unità dei dati dell'anagrafica, ma con i nomi dei campi "compressi"
 *
 * @author Accenture
 * Orritos, Abis, Mattei, Pittarelli
 */
public class AbcDatum {

    private static final Log logger = LogFactory.getLog(AbcDatum.class);

    private String a;
    private String b;
    private String c;

    public AbcDatum() {}

    public AbcDatum(String a, String b, String c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }


    public String getA() {
        return this.a;
    }

    public String getB() {
        return this.b;
    }

    public String getC() {
        return this.c;
    }

    public void setA(String a) {
        this.a = a;
    }

    public void setB(String b) {
        this.b = b;
    }

    public void setC(String c) {
        this.c = c;
    }

    public Datum toDatum() {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        Date aDate = null;
        Date bDate = null;

        String initVal = "1900-01-01";
        String endVal = "9999-12-31";

        try {
            aDate = (a != null && !a.trim().isEmpty()) ? format.parse(a) : format.parse(initVal);
            bDate = (b != null && !b.trim().isEmpty()) ? format.parse(b) : format.parse(endVal);

        } catch (ParseException exc) {
            format = new SimpleDateFormat("yyyy-MMM-dd");

            try {
                aDate = a != null ? format.parse(a) : null;
                bDate = b != null ? format.parse(b) : null;

            } catch (ParseException exc2) {
                // TODO
                logger.error("[toDatum] Error: " + exc2.getMessage());
            }
        }

        return new Datum(aDate, bDate, c);
    }

    @Override
    public String toString() {
        return "Datum(a=" + this.getA() + ", b=" + this.getB() + ", c=" + this.getC() + ")";
    }
}