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
package org.jboss.arquillian.warp;

import org.jboss.arquillian.container.test.impl.enricher.resource.URLResourceProvider;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.container.test.spi.client.deployment.ProtocolArchiveProcessor;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.jboss.arquillian.warp.client.execution.WarpClientActionBuilder;
import org.jboss.arquillian.warp.impl.client.enrichment.EnrichmentObserver;
import org.jboss.arquillian.warp.impl.client.enrichment.RequestEnrichmentService;
import org.jboss.arquillian.warp.impl.client.enrichment.ResponseDeenrichmentService;
import org.jboss.arquillian.warp.impl.client.execution.AssertionSynchronizer;
import org.jboss.arquillian.warp.impl.client.execution.DefaultAssertionSynchronizer;
import org.jboss.arquillian.warp.impl.client.execution.DefaultRequestEnrichmentFilter;
import org.jboss.arquillian.warp.impl.client.execution.DefaultRequestEnrichmentService;
import org.jboss.arquillian.warp.impl.client.execution.DefaultRequestExecutor;
import org.jboss.arquillian.warp.impl.client.execution.DefaultResponseDeenrichmentFilter;
import org.jboss.arquillian.warp.impl.client.execution.DefaultResponseDeenrichmentService;
import org.jboss.arquillian.warp.impl.client.execution.DefaultWarpExecutor;
import org.jboss.arquillian.warp.impl.client.execution.RequestExecutionObserver;
import org.jboss.arquillian.warp.impl.client.execution.WarpContext;
import org.jboss.arquillian.warp.impl.client.execution.WarpContextImpl;
import org.jboss.arquillian.warp.impl.client.execution.WarpExecutionInitializer;
import org.jboss.arquillian.warp.impl.client.execution.WarpExecutor;
import org.jboss.arquillian.warp.impl.client.proxy.DefaultProxyService;
import org.jboss.arquillian.warp.impl.client.proxy.DefaultURLMapping;
import org.jboss.arquillian.warp.impl.client.proxy.ProxyObserver;
import org.jboss.arquillian.warp.impl.client.proxy.ProxyService;
import org.jboss.arquillian.warp.impl.client.proxy.ProxyURLProvider;
import org.jboss.arquillian.warp.impl.client.proxy.RequestEnrichmentFilter;
import org.jboss.arquillian.warp.impl.client.proxy.ResponseDeenrichmentFilter;
import org.jboss.arquillian.warp.impl.client.proxy.URLMapping;
import org.jboss.arquillian.warp.impl.client.scope.WarpExecutionContextImpl;

/**
 * The client side extension for enhancing test with JSFUnit's logic.
 *
 * @author Lukas Fryc
 */
public class WarpExtension implements LoadableExtension {

    @Override
    public void register(ExtensionBuilder builder) {

        // proxy
        builder.override(ResourceProvider.class, URLResourceProvider.class, ProxyURLProvider.class);

        // deployment enrichment
        builder.service(ApplicationArchiveProcessor.class, DeploymentEnricher.class);
        builder.service(AuxiliaryArchiveAppender.class, DeploymentEnricher.class);
        builder.service(ProtocolArchiveProcessor.class, DeploymentEnricher.class);

        // action executor
        builder.service(WarpClientActionBuilder.class, DefaultRequestExecutor.class);
        builder.observer(ServiceInjector.class);
        builder.observer(RequestExecutionObserver.class);
        builder.service(AssertionSynchronizer.class, DefaultAssertionSynchronizer.class);
        builder.context(WarpExecutionContextImpl.class);
        builder.service(WarpExecutor.class, DefaultWarpExecutor.class);
        builder.observer(WarpExecutionInitializer.class);
//        builder.observer(WarpExecutionContextHandler.class);
        builder.service(URLMapping.class, DefaultURLMapping.class);
        builder.service(ProxyService.class, DefaultProxyService.class);
        builder.service(RequestEnrichmentFilter.class, DefaultRequestEnrichmentFilter.class);
        builder.service(ResponseDeenrichmentFilter.class, DefaultResponseDeenrichmentFilter.class);
        builder.observer(ProxyObserver.class);
        builder.observer(EnrichmentObserver.class);
        builder.service(RequestEnrichmentService.class, DefaultRequestEnrichmentService.class);
        builder.service(ResponseDeenrichmentService.class, DefaultResponseDeenrichmentService.class);
        builder.service(WarpContext.class, WarpContextImpl.class);
    }
}
