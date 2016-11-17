/**
 * Copyright 2016 Gertjan Al
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.gertjanal.metaltools.jstree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static io.parsingdata.metal.util.EncodingFactory.le;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import io.parsingdata.metal.data.Environment;
import io.parsingdata.metal.data.ParseResult;
import io.parsingdata.metal.format.PNG;
import io.parsingdata.metal.format.ZIP;
import io.parsingdata.metal.util.InMemoryByteStream;
import nl.gertjanal.metaltools.formats.vhdx.VHDX;

public class JsTreeTest {

    private static final boolean RENEW = false;

    @Test
    public void testGeneratePng() throws Exception {
        final Environment env = environment("/screenshot_data.png");
        final ParseResult result = PNG.FORMAT.parse(env, le());
        assertTrue(result.succeeded);

        assertGenerate(result, "example_png");
    }

    @Test
    public void testGenerateZip() throws Exception {
        final Environment env = environment("/data.zip");
        final ParseResult result = ZIP.FORMAT.parse(env, le());
        assertTrue(result.succeeded);

        assertGenerate(result, "example_zip");
    }

    @Test
    public void testGenerateVHDX() throws Exception {
        final Environment env = environment("/vhdx/NTFSdynamic.vhdx");
        final ParseResult result = VHDX.format(true).parse(env, le());
        assertTrue(result.succeeded);

        assertGenerate(result, "example_vhdx");
    }

    @Test
    public void testGenerateRAR() throws Exception {
        final Environment env = environment("/rar/example4.x.rar");
        final ParseResult result = VHDX.format(true).parse(env, le());
        assertTrue(result.succeeded);

        assertGenerate(result, "example_rar");
    }

    private Environment environment(final String name) throws IOException, URISyntaxException {
        final byte[] data = IOUtils.toByteArray(getClass().getResourceAsStream(name));
        return new Environment(new InMemoryByteStream(data));
    }

    private void assertGenerate(final ParseResult result, final String fileName) throws Exception {
        JsTree.generate(result.environment.order, fileName);

        if (RENEW) {
            export(fileName);
        }
        final String generated = IOUtils.toString(getClass().getResourceAsStream("/" + fileName + ".htm"), StandardCharsets.UTF_8);
        final String expected = IOUtils.toString(getClass().getResourceAsStream("/jsTree/" + fileName + ".htm"), StandardCharsets.UTF_8);
        assertEquals(expected, generated);
    }

    private void export(final String fileName) throws Exception {
        exportFile(fileName + ".htm");
        fail("Export should not be used in production");
    }

    private void exportFile(final String fileName) throws Exception {
        final File export = new File(new File(getClass().getResource("/").toURI()).getParentFile().getParentFile(), "/src/main/resources/jsTree/" + fileName);
        try (FileOutputStream fos = new FileOutputStream(export)) {
            IOUtils.copy(getClass().getResourceAsStream("/" + fileName), fos);
        }
    }
}
