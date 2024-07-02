/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.anagrafiche.client.entities;

import lombok.Data;

import java.util.Date;

/**
 * Classe dei tipi-dato, per ogni campo indicato nei metadata.<br>
 * <br>
 * Assumiamo e ci aspettiamo i dati, sempre nella forma della seguente tripletta:<br>
 * - a = tipo del campo che contiene la data di inizio validità del dato =&gt; sempre java.util.Date<br>
 * - b = tipo del campo che contiene la data di fine validità del dato =&gt; sempre java.util.Date<br>
 * - c = tipo del campo che contiene il dato vero e proprio =&gt; sempre String<br>
 *
 * @author Accenture
 * Orritos, Abis, Mattei, Pittarelli
 */
@Data
public class Types {

    private Class<Date>   a = java.util.Date.class;

    private Class<Date>   b = java.util.Date.class;

    private Class<String> c = String.class;
}
