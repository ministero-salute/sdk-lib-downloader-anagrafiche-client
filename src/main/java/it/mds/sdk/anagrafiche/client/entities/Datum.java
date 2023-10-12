package it.mds.sdk.anagrafiche.client.entities;

import java.util.Date;

/**
 * La più piccola unità dei dati dell'anagrafica.<br>
 * <br>
 * Espone i campi di validità del dato e il dato stesso.
 *
 * @author Accenture
 * Orritos, Abis, Mattei, Pittarelli
 */
public class Datum {

    private Date validFrom; //=> valid_from
    private Date validTo; //=> valid_to
    private String value; //=> value


    public Datum(Date validFrom, Date validTo, String value) {
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.value = value;
    }


    /**
     * Data di inizio validità del dato
     *
     * @return La data di inizio validità
     */
    public Date getValidFrom() {
        return this.validFrom;
    }

    /**
     * Data di fine validità del dato
     *
     * @return La data di fine validità
     */
    public Date getValidTo() {
        return this.validTo;
    }

    /**
     * Il dato vero e proprio
     *
     * @return Rappresentazione come stringa del dato
     */
    public String getValue() {
        return this.value;
    }

    void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    void setValidTo(Date validTo) {
        this.validTo = validTo;
    }

    void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Datum(validFrom=" + this.getValidFrom() + ", validTo=" + this.getValidTo() + ", value=" + this.getValue() + ")";
    }
}
