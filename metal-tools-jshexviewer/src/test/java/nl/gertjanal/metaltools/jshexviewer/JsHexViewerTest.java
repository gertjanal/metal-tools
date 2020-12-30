/**
 * Copyright 2016 Gertjan Al
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.gertjanal.metaltools.jshexviewer;

import static java.nio.charset.StandardCharsets.UTF_8;

import static org.apache.commons.io.IOUtils.copy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static io.parsingdata.metal.Shorthand.def;
import static io.parsingdata.metal.Shorthand.last;
import static io.parsingdata.metal.Shorthand.ref;
import static io.parsingdata.metal.Shorthand.seq;
import static io.parsingdata.metal.util.EncodingFactory.le;
import static io.parsingdata.metal.util.EnvironmentFactory.env;
import static io.parsingdata.metal.util.ParseStateFactory.stream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import io.parsingdata.metal.data.Environment;
import io.parsingdata.metal.data.ParseState;
import io.parsingdata.metal.format.PNG;
import io.parsingdata.metal.format.ZIP;
import io.parsingdata.metal.token.Token;
import nl.gertjanal.metaltools.formats.fat16.FAT16;
import nl.gertjanal.metaltools.formats.mp4.MP4;
import nl.gertjanal.metaltools.formats.rar.RAR;

public class JsHexViewerTest {

    private static final boolean RENEW = true;
    private static final Token STRING = seq(def("length", 1), def("text", last(ref("length"))));

    @Test
    public void testGenerateData() throws Exception {
        final ParseState parseState = stream(7, 'G', 'e', 'r', 't', 'j', 'a', 'n');
        final Optional<ParseState> result = STRING.parse(env(parseState, le()));

        assertTrue(result.isPresent());

        // Write the data so it can be loaded manually in the viewer
        final File root = new File(getClass().getResource("/").toURI());
        try (FileOutputStream out = new FileOutputStream(new File(root, "data"))) {
            out.write(7);
            out.write("Gertjan".getBytes(UTF_8));
        }

        assertGenerate(result.get(), "example_data");
    }

    @Test
    public void testGeneratePng() throws Exception {
        final ParseState result = parse("/screenshot_data.png", PNG.FORMAT);
        assertGenerate(result, "example_png");
    }

    @Test
    public void testGenerateZip() throws Exception {
        final ParseState result = parse("/data.zip", ZIP.FORMAT);
        assertGenerate(result, "example_zip");
    }

    @Test
    public void testGenerateRAR() throws Exception {
        final ParseState result = parse("/rar/example4.x.rar", RAR.FORMAT);
        assertGenerate(result, "example_rar");
    }

    @Test
    public void testGenerateMP4() throws Exception {
        final ParseState result = parse("/mp4/big_buck_bunny_720p_1mb.mp4", MP4.FORMAT);
        assertGenerate(result, "example_mp4");
    }

    @Test
    public void testGenerateFAT16() throws Exception {
        final ParseState result = parse("/fat16/fat16.img", FAT16.FORMAT);
        assertGenerate(result, "example_fat16");
    }

    private ParseState parse(final String name, final Token format) throws IOException, URISyntaxException {
        return format.parse(environment(name)).get();
    }

    private Environment environment(final String name) throws IOException, URISyntaxException {
        return env(stream(getClass().getResource(name).toURI()));
    }

    private void assertGenerate(final ParseState result, final String fileName) throws Exception {
        JsHexViewer.generate(result.order, fileName);

        if (RENEW) {
            export(fileName);
        }
        final String generated = IOUtils.toString(getClass().getResourceAsStream("/" + fileName + ".js"), StandardCharsets.UTF_8);
        final String expected = IOUtils.toString(getClass().getResourceAsStream("/jsHexViewer/" + fileName + ".js"), StandardCharsets.UTF_8);
        assertEquals(expected, generated);
    }

    private void export(final String fileName) throws Exception {
        exportFile(fileName + ".htm");
        exportFile(fileName + ".js");
        fail("Export should not be used in production");
    }

    private void exportFile(final String fileName) throws Exception {
        final File export = new File(new File(getClass().getResource("/").toURI()).getParentFile().getParentFile(), "/src/main/resources/jsHexViewer/" + fileName);
        try (FileOutputStream fos = new FileOutputStream(export)) {
            copy(getClass().getResourceAsStream("/" + fileName), fos);
        }
    }
}
