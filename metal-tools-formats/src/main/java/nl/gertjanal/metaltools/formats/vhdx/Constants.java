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

package nl.gertjanal.metaltools.formats.vhdx;

import java.nio.charset.StandardCharsets;

import io.parsingdata.metal.encoding.ByteOrder;
import io.parsingdata.metal.encoding.Encoding;

/**
 * Constants for VHDX Metal tokens. Based on VHDX Format Specification v1.00:
 * https://www.microsoft.com/en-us/download/details.aspx?id=34750
 *
 * @author Gertjan Al.
 */
public class Constants {
	public static final Encoding ENC = new Encoding(ByteOrder.LITTLE_ENDIAN);
	public static final Encoding UTF_16 = new Encoding(StandardCharsets.UTF_16LE);
	public static final int UINT8 = 1;
	public static final int UINT16 = 16 / 8;
	public static final int UINT32 = 32 / 8;
	public static final int UINT64 = 64 / 8;
	public static final int GUID = 128 / 8;
}
