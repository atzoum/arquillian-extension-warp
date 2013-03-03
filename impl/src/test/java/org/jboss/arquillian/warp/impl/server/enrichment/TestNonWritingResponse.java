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
package org.jboss.arquillian.warp.impl.server.enrichment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.jboss.arquillian.warp.impl.server.execution.NonWritingResponse;
import org.junit.Before;
import org.junit.Test;

public class TestNonWritingResponse {

    private ByteArrayServletOutputStream output;
    private HttpServletResponse response;
    private NonWritingResponse nonWritingResponse;

    @Before
    public void setUp() throws IOException {
        // given
        output = new ByteArrayServletOutputStream();
        response = mock(HttpServletResponse.class);
        when(response.getOutputStream()).thenReturn(output);
        nonWritingResponse = new NonWritingResponse(response);
    }

    @Test
    public void test_writing_to_printWriter() throws IOException {
        // when
        nonWritingResponse.getWriter().write("test");
        nonWritingResponse.finallyWrite(output);

        // then
        assertEquals("test", output.toString());
    }

    @Test
    public void test_not_closing() throws IOException {
        // when
        nonWritingResponse.finallyWrite(output);
        nonWritingResponse.finallyClose(output);

        // then
        assertFalse(output.isClosed());
    }

    @Test
    public void test_closing_printWriter() throws IOException {
        // when
        nonWritingResponse.getWriter().close();
        nonWritingResponse.finallyWrite(output);
        nonWritingResponse.finallyClose(output);

        // then
        assertTrue(output.isClosed());
    }

    @Test
    public void test_writing_to_outputStream() throws IOException {
        // when
        nonWritingResponse.getOutputStream().write(bytes("test"));
        nonWritingResponse.finallyWrite(output);

        // then
        assertEquals("test", output.toString());
    }

    @Test
    public void test_closing_outputStream() throws IOException {
        // when
        nonWritingResponse.getOutputStream().close();
        nonWritingResponse.finallyWrite(output);
        nonWritingResponse.finallyClose(output);

        // then
        assertTrue(output.isClosed());
    }

    @Test
    public void test_setting_content_length() throws IOException {
        // when
        nonWritingResponse.setContentLength(1);
        nonWritingResponse.setContentLength(2);
        nonWritingResponse.finallyWrite(output);

        // then
        verify(response).setContentLength(2);
    }

    @Test
    public void test_not_setting_content_length() throws IOException {
        // when
        nonWritingResponse.finallyWrite(output);
        nonWritingResponse.finallyClose(output);

        // then
        verifyNoMoreInteractions(response);
    }

    @Test
    public void test_adding_to_output_written_by_printWriter() throws IOException {
        // when
        nonWritingResponse.getWriter().write("a");
        response.getOutputStream().write(bytes("b"));
        nonWritingResponse.finallyWrite(output);

        // then
        assertEquals("ba", output.toString());
    }

    private byte[] bytes(String string) {
        return string.getBytes();
    }
}
