/**
 * Copyright 2016 Gertjan Al
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

package nl.gertjanal.metaltools.jshexviewer;

import static org.junit.Assert.assertTrue;

import static io.parsingdata.metal.util.EncodingFactory.le;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import io.parsingdata.metal.data.Environment;
import io.parsingdata.metal.data.ParseResult;
import io.parsingdata.metal.util.InMemoryByteStream;
import nl.gertjanal.metaltools.formats.rar.RAR;

/**
 * Generate JsHexViewer for example.rar resource file.
 *
 * @author Gertjan Al.
 */
public class JsHexViewerRARGenerator {

	@Test
	public void generate() throws Exception {
		final Environment env = environment("/rar/example.rar");
		final ParseResult result = RAR.FORMAT.parse(env, le());
		assertTrue(result.succeeded);
		JsHexViewer.generate(result.environment.order, "rar");

		// openBrowser("rar.htm"); // Enable this line in the first run to open the browser
	}

	private void openBrowser(final String file) throws Exception {
		final File root = new File(getClass().getResource("/").toURI());
		Desktop.getDesktop().browse(new File(root, file).toURI());
	}

	private Environment environment(final String name) throws IOException, URISyntaxException {
		System.out.println("After the hexviewer is generated, select source:");
		System.out.println(getClass().getResource(name).getPath());
		final byte[] data = IOUtils.toByteArray(getClass().getResourceAsStream(name));
		return new Environment(new InMemoryByteStream(data));
	}
}
