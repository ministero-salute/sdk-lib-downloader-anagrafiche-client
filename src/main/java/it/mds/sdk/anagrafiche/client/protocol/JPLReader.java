/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.anagrafiche.client.protocol;

import it.mds.sdk.anagrafiche.client.entities.Registry;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

/**
 * Interface for classes that implement
 * the "Json Protocol based on Lines".
 *
 * @author Accenture
 * Orritos, Abis, Mattei, Pittarelli
 */
public interface JPLReader {

    void registryFromInputStream(Registry registry, InputStream stream) throws IOException, ParseException;
}
