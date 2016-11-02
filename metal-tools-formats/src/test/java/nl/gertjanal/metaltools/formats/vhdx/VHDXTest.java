/**
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

package nl.gertjanal.metaltools.formats.vhdx;

import static org.junit.Assert.assertTrue;

import static io.parsingdata.metal.util.EncodingFactory.le;

import org.junit.Test;

import io.parsingdata.metal.data.Environment;
import io.parsingdata.metal.data.ParseResult;
import nl.gertjanal.metaltools.formats.ResourceEnvironment;

public class VHDXTest {

	@Test
	public void testNTFSdynamic() throws Exception {
		final Environment environment = ResourceEnvironment.environment("/vhdx/NTFSdynamic.vhdx");
		final ParseResult result = VHDX.format(true).parse(environment, le());
		assertTrue(result.succeeded);
	}

	@Test
	public void testNTFSfixed() throws Exception {
		final Environment environment = ResourceEnvironment.environment("/vhdx/NTFSfixed.vhdx");
		final ParseResult result = VHDX.format(true).parse(environment, le());
		assertTrue(result.succeeded);
	}

	@Test
	public void testWindows10() throws Exception {
		final Environment environment = ResourceEnvironment.environment("/vhdx/MSEdge-Win10_preview_truncated.vhdx");
		final ParseResult result = VHDX.format(false).parse(environment, le());
		assertTrue(result.succeeded);
	}
}
