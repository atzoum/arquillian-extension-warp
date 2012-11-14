/**
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.warp.impl.client.execution;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.warp.exception.ClientWarpExecutionException;
import org.jboss.arquillian.warp.impl.client.enrichment.RequestEnrichmentService;
import org.jboss.arquillian.warp.impl.client.event.FilterHttpRequest;
import org.jboss.arquillian.warp.impl.client.proxy.RequestEnrichmentFilter;
import org.jboss.netty.handler.codec.http.HttpRequest;

public class DefaultRequestEnrichmentFilter implements RequestEnrichmentFilter {

    @Inject
    private Event<FilterHttpRequest> tryEnrichRequest;

    private RequestEnrichmentService enrichmentService;

    @Override
    public void filter(HttpRequest request) {
        final WarpContext context = WarpContextStore.getCurrentInstance();
        try {
            if (context != null) {
                final SynchronizationPoint synchronization = context.getSynchronization();

                if (synchronization.isWaitingForRequests()) {
                    try {

                        tryEnrichRequest.fire(new FilterHttpRequest(request, enrichmentService));

                    } catch (Exception originalException) {
                        ClientWarpExecutionException explainingException = new ClientWarpExecutionException(
                                "enriching request failed: " + originalException.getMessage(), originalException);
                        context.pushException(explainingException);
                    }
                }
            }
        } catch (Exception e) {
            if (context != null) {
                context.pushException(e);
            }
        }
    }

    @Override
    public void initialize(RequestEnrichmentService enrichmentService) {
        this.enrichmentService = enrichmentService;
    }
}
