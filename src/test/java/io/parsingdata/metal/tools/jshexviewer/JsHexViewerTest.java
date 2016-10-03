/*
 * Copyright 2013-2016 Netherlands Forensic Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.parsingdata.metal.tools.jshexviewer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static io.parsingdata.metal.Shorthand.def;
import static io.parsingdata.metal.Shorthand.ref;
import static io.parsingdata.metal.Shorthand.seq;
import static io.parsingdata.metal.util.EncodingFactory.le;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import io.parsingdata.metal.data.Environment;
import io.parsingdata.metal.data.ParseResult;
import io.parsingdata.metal.format.PNG;
import io.parsingdata.metal.token.Token;
import io.parsingdata.metal.util.EnvironmentFactory;
import io.parsingdata.metal.util.InMemoryByteStream;

public class JsHexViewerTest {

    private static final Token STRING = seq(
        def("length", 1),
        def("text", ref("length")));

    @Test
    public void testGenerateData() throws Exception {
        final Environment env = EnvironmentFactory.stream(7, 'G', 'e', 'r', 't', 'j', 'a', 'n');
        final ParseResult result = STRING.parse(env, le());

        assertTrue(result.succeeded);

        // Write the data so it can be loaded manually in the viewer
        final File root = new File(getClass().getResource("/jsHexViewer").toURI());
        try (FileOutputStream out = new FileOutputStream(new File(root, "data"))) {
            final byte[] buffer = new byte[8];
            env.input.read(0, buffer);
            out.write(buffer);
        }

        JsHexViewer.generate(result.environment.order);

        // export("jsHexViewer_data.htm");
        final String generated = IOUtils.toString(getClass().getResourceAsStream("/jsHexViewer.htm"));
        final String expected = IOUtils.toString(getClass().getResourceAsStream("/jsHexViewer/jsHexViewer_data.htm"));
        assertEquals(expected, generated);
    }

    @Test
    public void testGeneratePng() throws Exception {
        byte[] data;
        try (final InputStream input = getClass().getResourceAsStream("/jsHexViewer/screenshot_data.png");
             final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            IOUtils.copy(input, output);
            data = output.toByteArray();
        }

        final Environment env = new Environment(new InMemoryByteStream(data));
        final ParseResult result = PNG.FORMAT.parse(env, le());
        assertTrue(result.succeeded);

        JsHexViewer.generate(result.environment.order);

        // export("jsHexViewer_screenshot.htm");
        final String generated = IOUtils.toString(getClass().getResourceAsStream("/jsHexViewer.htm"));
        final String expected = IOUtils.toString(getClass().getResourceAsStream("/jsHexViewer/jsHexViewer_screenshot.htm"));
        assertEquals(expected, generated);
    }

    private void export(final String fileName) throws Exception {
        final File export = new File(new File(getClass().getResource("/").toURI()).getParentFile().getParentFile(), "/src/test/resources/jsHexViewer/" + fileName);
        try (FileOutputStream fos = new FileOutputStream(export)) {
            IOUtils.copy(getClass().getResourceAsStream("/jsHexViewer.htm"), fos);
        }
    }
}
