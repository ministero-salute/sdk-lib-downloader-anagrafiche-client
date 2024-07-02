/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.anagrafiche.client.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Classe dei metadata.<br>
 * <br>
 * Assumiamo e ci aspettiamo i dati, sempre nella forma della seguente tripletta:<br>
 *  - valid_from = nome del campo che contiene la data di inizio validità del dato<br>
 *  - valid_to = nome del campo che contiene la data di fine validità del dato<br>
 *  - value = nome del campo che contiene il dato vero e proprio<br>
 *
 * @author Accenture
 * Orritos, Abis, Mattei, Pittarelli
 */
@Data
public class RegistryMetadata {

    @JsonProperty("valid_from")
    private String validFrom = "a";

    @JsonProperty("valid_to")
    private String validTo   = "b";

    private String value     = "c";
}
