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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static io.parsingdata.metal.Shorthand.def;
import static io.parsingdata.metal.Shorthand.ref;
import static io.parsingdata.metal.Shorthand.seq;
import static io.parsingdata.metal.util.EncodingFactory.le;
import static io.parsingdata.metal.util.EnvironmentFactory.stream;

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
import io.parsingdata.metal.token.Token;
import io.parsingdata.metal.util.InMemoryByteStream;
import nl.gertjanal.metaltools.formats.MP4;
import nl.gertjanal.metaltools.formats.rar.RAR;
import nl.gertjanal.metaltools.formats.vhdx.VHDX;

public class JsHexViewerTest {

	private static final boolean RENEW = false;
	private static final Token STRING = seq(
		def("length", 1),
		def("text", ref("length")));

	@Test
	public void testGenerateData() throws Exception {
		final Environment env = stream(7, 'G', 'e', 'r', 't', 'j', 'a', 'n');
		final ParseResult result = STRING.parse(env, le());

		assertTrue(result.succeeded);

		// Write the data so it can be loaded manually in the viewer
		final File root = new File(getClass().getResource("/").toURI());
		try (FileOutputStream out = new FileOutputStream(new File(root, "data"))) {
			final byte[] buffer = new byte[8];
			env.input.read(0, buffer);
			out.write(buffer);
		}

		assertGenerate(result, "example_data");
	}

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
		final ParseResult result = RAR.FORMAT.parse(env, le());
		assertTrue(result.succeeded);

		assertGenerate(result, "example_rar");
	}

	@Test
	public void testGenerateMP4() throws Exception {
		final Environment env = environment("/mp4/big_buck_bunny_720p_1mb.mp4");
		final ParseResult result = MP4.FORMAT.parse(env, le());
		assertTrue(result.succeeded);

		assertGenerate(result, "example_mp4");
	}

	private Environment environment(final String name) throws IOException, URISyntaxException {
		final byte[] data = IOUtils.toByteArray(getClass().getResourceAsStream(name));
		return new Environment(new InMemoryByteStream(data));
	}

	private void assertGenerate(final ParseResult result, final String fileName) throws Exception {
		JsHexViewer.generate(result.environment.order, fileName);

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
			IOUtils.copy(getClass().getResourceAsStream("/" + fileName), fos);
		}
	}
}
