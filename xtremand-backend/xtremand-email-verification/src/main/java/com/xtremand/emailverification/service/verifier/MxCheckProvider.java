package com.xtremand.emailverification.service.verifier;

import com.xtremand.emailverification.service.factory.LookupFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.xbill.DNS.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class MxCheckProvider {

    private final LookupFactory lookupFactory;

    /**
     * Checks if a domain has valid MX records.
     *
     * @param domain The domain to check.
     * @return true if valid MX records are found, false otherwise.
     */
    public boolean hasMxRecords(String domain) {
        try {
            Lookup lookup = lookupFactory.create(domain, Type.MX);
            Record[] records = lookup.run();
            if (lookup.getResult() != Lookup.SUCCESSFUL) {
                log.warn("MX record lookup failed for domain {}: {}", domain, lookup.getErrorString());
                return false;
            }
            return records != null && records.length > 0;
        } catch (TextParseException e) {
            log.error("Error parsing domain for MX lookup: {}", domain, e);
            return false;
        }
    }

    /**
     * Retrieves the MX hosts for a given domain, sorted by priority.
     *
     * @param domain The domain to query.
     * @return A sorted list of MX hostnames.
     */
    public List<String> getMxHosts(String domain) {
        try {
            Lookup lookup = lookupFactory.create(domain, Type.MX);
            Record[] records = lookup.run();

            if (lookup.getResult() != Lookup.SUCCESSFUL || records == null) {
                return List.of();
            }

            return Arrays.stream(records)
                    .map(record -> (MXRecord) record)
                    .sorted(java.util.Comparator.comparingInt(MXRecord::getPriority))
                    .map(mxRecord -> mxRecord.getTarget().toString(true))
                    .collect(Collectors.toList());
        } catch (TextParseException e) {
            log.error("Error parsing domain for MX lookup: {}", domain, e);
            return List.of();
        }
    }
}