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
package org.trustedanalytics.les;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.trustedanalytics.les.storage.EventInfo;
import org.trustedanalytics.les.storage.MongoEventStore;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class MongoEventStoreTests {

    private MongoEventStore sut;

    @Mock
    private MongoOperations mongoOperations;

    @Test
    public void save_insertsRecordToRepository() {
        sut = new MongoEventStore(mongoOperations);

        EventInfo e = new EventInfo();
        sut.save(e);

        verify(mongoOperations, times(1)).insert(e);
    }

    @Test
    public void getEventsCount_callsCountOnRepository() {
        when(mongoOperations.count(any(Query.class), eq(EventInfo.class)))
                .thenReturn(123L);

        sut = new MongoEventStore(mongoOperations);
        long count = sut.getEventsCount(new ArrayList<>());

        verify(mongoOperations, times(1)).count(any(Query.class), eq(EventInfo.class));
        assertEquals(123L, count);
    }

    @Test
    public void getLatestEvents_callsFindOnMongoOperations() {
        List<EventInfo> events = new ArrayList<>();
        events.add(new EventInfo());
        when(mongoOperations.find(any(Query.class), eq(EventInfo.class)))
                .thenReturn(events);

        sut = new MongoEventStore(mongoOperations);
        List<EventInfo> actualEvents = sut.getLatestEvents(new ArrayList<>(), 1, 5);

        verify(mongoOperations, times(1)).find(any(Query.class), eq(EventInfo.class));
        assertEquals(events.size(), actualEvents.size());
        assertEquals(events.get(0), actualEvents.get(0));
    }
}
