/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.anagrafiche.client;

public interface Authorizer<T> {

    void authorize(T entity);
}
