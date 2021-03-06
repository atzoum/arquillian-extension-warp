/**
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.warp.impl.client.eventbus;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.jboss.arquillian.warp.impl.server.event.AfterSuiteRemoteEvent;
import org.jboss.arquillian.warp.impl.server.event.BeforeSuiteRemoteEvent;
import org.jboss.arquillian.warp.impl.server.event.WarpRemoteEvent;

/**
 * <p>
 * Controls the Warp's suite lifecycle on container by propagating.
 * </p>
 *
 * <p>
 * The client-side {@link Before} event will be mapped to {@link BeforeSuite} on server-side.
 * </p>
 *
 * <p>
 * The client-side {@link After} event will be mapped to {@link AfterSuite} on server-side.
 * </p>
 *
 * @author Aris Tzoumas
 */
public class RemoteSuiteLifecyclePropagation {

    @Inject
    private Event<WarpRemoteEvent> remoteEvent;

    void sendBefore(@Observes Before event) throws Exception {
        remoteEvent.fire(new BeforeSuiteRemoteEvent());
    }

    void sendAfter(@Observes After event) throws Exception {
        remoteEvent.fire(new AfterSuiteRemoteEvent());
    }
}
