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

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.impl.FileVolumeManager;
import com.github.junrar.rarfile.FileHeader;
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
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.zip.CRC32;
import nl.gertjanal.metaltools.formats.rar.RAR;
import nl.gertjanal.metaltools.formats.vhdx.VHDX;

public class JsHexViewerTest {

	private static final boolean RENEW = true;
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
            String filename = "/rar/multifolder.rar";
            //String filename = "/rar/example4.x.rar";
            File f = new File(getClass().getResource(filename).toURI());
		final Environment env = environment(filename);
		final ParseResult result = RAR.FORMAT.parse(env, le());
                unrarBytes(f);
              /*  ParseGraph graph = result.environment.order;
                ParseValueList list = ByName.getAllValues(graph, "FILE");
                ParseValue value = list.head;
                byte[] bytes = value.getValue();*/
                
		assertTrue(result.succeeded);

		assertGenerate(result, "example_rar");
	}
        
        private void unrarBytes(File f){
            Archive a = null;
            try {
                a = new Archive(new FileVolumeManager(f));
            } catch (RarException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (a != null) {
                a.getMainHeader().print();
                FileHeader fh = a.nextFileHeader();
                while (fh != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try {
                        File out = new File("/home/rogiel/fs/test/"
                                + fh.getFileNameString().trim());
                        System.out.println(out.getAbsolutePath());
                        a.extractFile(fh, baos);
                        baos.close();
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (RarException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    fh = a.nextFileHeader();
                    CRC32 crc = new CRC32();
                    byte[] bs = baos.toByteArray();
                    crc.update(bs);
                    String s = new String(bs);
                    System.out.println(s + " has crc:" + crc.getValue());
                }
            }
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
