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
package org.jboss.arquillian.warp.ftest.eventbus;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.warp.Activity;
import org.jboss.arquillian.warp.Inspection;
import org.jboss.arquillian.warp.Warp;
import org.jboss.arquillian.warp.WarpTest;
import org.jboss.arquillian.warp.servlet.AfterServlet;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

/**
 *
 * @author atzoum
 *
 */
@RunWith(Arquillian.class)
@WarpTest
@RunAsClient
public class TestContainerToClientChannel {

    @Drone
    private WebDriver browser;

    @ArquillianResource
    private URL contextPath;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war").addClass(DummyCommand.class)
                .addClasses(DummyCommandRemoteExtension.class, DummyCommandSender.class, DummyCommandRemoteEvent.class)
                .addAsWebResource(new File("src/main/webapp/index.html")).addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsServiceProvider(RemoteLoadableExtension.class.getName(), DummyCommandRemoteExtension.class.getName());
    }

    /**
     * A remote command will be sent at <code>Activity</code> event. This remote command will be executed on the container,
     * causing a DummyCommand to be sent back to the client.
     */
    @Test
    @InSequence(1)
    public void testSuccessfulCommand() {
        DummyCommandReceiver.fail = false;

        // enable Remote Command Sender on client side.
        DummyRemoteCommandSender.ENABLED = true;

        DummyCommandInspection inspection = Warp.initiate(new Activity() {
            public void perform() {
                browser.navigate().to(contextPath + "index.html");
            }
        }).inspect(new DummyCommandInspection());
        DummyRemoteCommandSender.ENABLED = false;
        assertNotNull("response can't be null", inspection.response);
        assertNull("thowable must be null", inspection.failure);
    }

    @Test
    @InSequence(2)
    public void testFailedCommand() {
        DummyCommandReceiver.fail = true;
        DummyRemoteCommandSender.ENABLED = true;

        DummyCommandInspection errorInspection = Warp.initiate(new Activity() {
            public void perform() {
                browser.navigate().to(contextPath + "index.html");
            }
        }).inspect(new DummyCommandInspection());
        DummyRemoteCommandSender.ENABLED = false;
        assertNull("errorInspection must be null", errorInspection.response);
        assertNotNull("thowable must not be null", errorInspection.failure);
    }

    public static class DummyCommandInspection extends Inspection {

        private static final long serialVersionUID = 1L;
        private String response = null;
        private Throwable failure = null;

        @AfterServlet
        public void afterServlet() {
            response = DummyCommandSender.getResponse();
            failure = DummyCommandSender.getFailure();
        }
    }

}
