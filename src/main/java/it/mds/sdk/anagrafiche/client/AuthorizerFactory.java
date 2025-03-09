/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.anagrafiche.client;

import org.apache.commons.lang3.StringUtils;

import java.util.Properties;


public abstract class AuthorizerFactory {

    private AuthorizerFactory(){
        // intentionally empty
    }

    private static final String AUTH_TYPE_PROPERTIES = "rest.authorizer.type";

    public static Authorizer createAuthorizer(Properties conf) {
        String type = conf.getProperty(AUTH_TYPE_PROPERTIES);
        if (StringUtils.isBlank(type)){
            return null;
        }
        switch (type) {
            case "JWT":
                return new BearerJwtAuthorizer(conf);
            default:
                return null;
        }
    }
}
