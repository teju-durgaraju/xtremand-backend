package com.xtremand.email.verification.service.factory;

import org.springframework.stereotype.Component;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.TextParseException;

@Component
public class LookupFactory {

    /**
     * Creates a new DNS Lookup object.
     * This method wraps the constructor to allow for mocking in tests.
     * @param name the name to look up
     * @param type the type to look up
     * @return a new Lookup object
     * @throws TextParseException if the name is not a valid DNS name
     */
    public Lookup create(String name, int type) throws TextParseException {
        return new Lookup(name, type);
    }
}