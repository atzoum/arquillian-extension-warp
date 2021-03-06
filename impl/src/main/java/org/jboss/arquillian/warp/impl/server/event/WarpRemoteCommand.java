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
package org.jboss.arquillian.warp.impl.server.event;

import java.io.Serializable;

import org.jboss.arquillian.container.test.spi.command.Command;

/**
 *
 * A command that executes on the remote container.
 *
 * @author Aris Tzoumas
 *
 */
public class WarpRemoteCommand implements Command<String>, Serializable {

    private static final long serialVersionUID = 1L;

    protected String result;
    protected Throwable throwable;
    protected WarpRemoteEvent payload;

    public WarpRemoteCommand(WarpRemoteEvent payload) {
        this.payload = payload;
    }

    @Override
    public String getResult() {
        return result;
    }

    @Override
    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public Throwable getThrowable() {
       return throwable;
    }

    public WarpRemoteEvent getPayload() {
        return payload;
    }

    public void setPayload(WarpRemoteEvent payload) {
        this.payload = payload;
    }


}
