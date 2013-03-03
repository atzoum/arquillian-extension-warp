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
package org.jboss.arquillian.warp.impl.server.execution;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.core.spi.ManagerBuilder;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.jboss.arquillian.warp.impl.server.delegation.RequestDelegationService;
import org.jboss.arquillian.warp.impl.server.delegation.RequestDelegator;
import org.jboss.arquillian.warp.impl.server.event.ProcessHttpRequest;
import org.jboss.arquillian.warp.spi.WarpCommons;
import org.jboss.arquillian.warp.spi.context.RequestScoped;
import org.jboss.arquillian.warp.spi.event.AfterRequest;
import org.jboss.arquillian.warp.spi.event.BeforeRequest;

/**
 * <p>
 * Filter that detects whenever the incoming request is enriched and thus should be processed by {@link WarpRequestProcessor}.
 * </p>
 *
 * @author Lukas Fryc
 */
@WebFilter(urlPatterns = "/*", asyncSupported = true)
public class WarpFilter implements Filter {
    private static final String DEFAULT_EXTENSION_CLASS = "org.jboss.arquillian.core.impl.loadable.LoadableExtensionLoader";

    private RequestDelegator delegator;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        delegator = new RequestDelegator();
    }

    @Override
    public void destroy() {
        delegator = null;
    }

    /**
     * Detects whenever the request is HTTP request and if yes, delegates to
     * {@link #doFilterHttp(HttpServletRequest, HttpServletResponse, FilterChain)}.
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, final FilterChain chain) throws IOException,
            ServletException {

        if (req instanceof HttpServletRequest && resp instanceof HttpServletResponse) {
            doFilterHttp((HttpServletRequest) req, (HttpServletResponse) resp, chain);
        } else {
            chain.doFilter(req, resp);
        }
    }

    /**
     * <p>
     * Checks whether the request processing can be delegated to one of registered {@link RequestDelegationService}s.
     * </p>
     *
     * <p>
     * If not, delegates processing to {@link #doFilterWarp(HttpServletRequest, HttpServletResponse, FilterChain)}.
     * </p>
     */
    private void doFilterHttp(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        boolean isDelegated = delegator.tryDelegateRequest(request, response, filterChain);

        if (!isDelegated) {
            doFilterWarp(request, response, filterChain);
        }
    }

    /**
     * <p>
     * Starts the Arquillian Manager, starts contexts and registers contextual instances.
     * </p>
     *
     * <p>
     * Throws {@link ProcessHttpRequest} event which is used for further request processing.
     * </p>
     */
    private void doFilterWarp(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        NonWritingResponse responseWrapper = new NonWritingResponse(response);
        try {
            ManagerBuilder builder = ManagerBuilder.from().extension(Class.forName(DEFAULT_EXTENSION_CLASS));
            Manager manager = builder.create();

            manager.start();

            manager.bind(ApplicationScoped.class, Manager.class, manager);

            manager.fire(new BeforeSuite());
            manager.fire(new BeforeRequest(request, responseWrapper));

            manager.bind(RequestScoped.class, ServletRequest.class, request);
            manager.bind(RequestScoped.class, ServletResponse.class, responseWrapper);
            manager.bind(RequestScoped.class, HttpServletRequest.class, request);
            manager.bind(RequestScoped.class, HttpServletResponse.class, responseWrapper);
            manager.bind(RequestScoped.class, FilterChain.class, filterChain);

            try {
                manager.fire(new ProcessHttpRequest());
            } finally {
                manager.fire(new AfterRequest(request, responseWrapper));
                manager.fire(new AfterSuite());

                manager.shutdown();
                // Write response headers
                for (String header : responseWrapper.getHeaderNames()) {
                    for (String value : responseWrapper.getHeaders().get(header)) {
                        response.addHeader(header, value);
                    }
                }
                response.setStatus(responseWrapper.getStatus());
                responseWrapper.finallyWrite(response.getOutputStream());
                responseWrapper.finallyClose(response.getOutputStream());
            }

        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Default service loader can't be found: " + DEFAULT_EXTENSION_CLASS, e);
        }
    }
}
