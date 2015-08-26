/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trustedanalytics.les.rest;

import org.trustedanalytics.cloud.cc.api.CcOperations;
import org.trustedanalytics.cloud.cc.api.CcOrg;
import org.trustedanalytics.les.storage.EventStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
public class EventsController {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final String LATEST_EVENTS_URL = "/rest/les/events";

    private final EventStore eventStore;

    private final CcOperations ccOperations;

    @Autowired
    public EventsController(EventStore eventStore, CcOperations ccOperations) {
        this.eventStore = eventStore;
        this.ccOperations = ccOperations;
    }

    @RequestMapping(LATEST_EVENTS_URL)
    public EventSummary getLatestEvents(
            @RequestParam(value = "org", required = false) UUID org,
            @RequestParam(value = "from", defaultValue = "0", required = false) int from,
            @RequestParam(value = "size", defaultValue = "50", required = false) int size) {
        List<String> orgs = new ArrayList<>();
        for (CcOrg ccOrg : ccOperations.getOrgs().toBlocking().toIterable()) {
            orgs.add(ccOrg.getGuid().toString());
        }

        if (org != null) {
            LOG.debug("Validating whether user belongs to organization: {}", org);
            if (!orgs.contains(org.toString())) {
                // Deny access to organizations of other users
                LOG.error("User does NOT belong to organization: {}", org);
                throw new AuthorizationException();
            }

            // Results only for specified organization
            orgs.clear();
            orgs.add(org.toString());
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Create EventSummary for orgs: {}", Arrays.toString(orgs.toArray()));
        }

        EventSummary eventSummary = new EventSummary(
                eventStore.getEventsCount(orgs),
                eventStore.getLatestEvents(orgs, from, size));

        LOG.debug("Created EventSummary: total={}, getEvents().size()={}", eventSummary.getTotal(), eventSummary.getEvents().size());

        return eventSummary;
    }
}