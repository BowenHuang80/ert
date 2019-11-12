package com.completeinnovations.ert.authentication;

/**
 * Reference: Udini <a href="http://goo.gl/UkmVE">Write your own Android Authenticator</a>
 */
public interface ServerAuthenticate {
    public String userSignIn(final String user, final String pass, String authType) throws Exception;
}
