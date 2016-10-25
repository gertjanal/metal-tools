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

package io.parsingdata.metal.formats.vhdx;

import static io.parsingdata.metal.Shorthand.con;
import static io.parsingdata.metal.Shorthand.eq;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import io.parsingdata.metal.encoding.ByteOrder;
import io.parsingdata.metal.encoding.Encoding;
import io.parsingdata.metal.expression.Expression;
import io.parsingdata.metal.expression.value.Value;

/**
 * Constants for VHDX Metal tokens.
 * Based on VHDX Format Specification v1.00: https://www.microsoft.com/en-us/download/details.aspx?id=34750
 *
 * @author Netherlands Forensic Institute.
 */
public class Constants {
    public static final Encoding ENC = new Encoding(ByteOrder.LITTLE_ENDIAN);
    public static final Encoding UTF_16 = new Encoding(StandardCharsets.UTF_16LE);
    public static final int UINT8 = 1;
    public static final int UINT16 = 16 / 8;
    public static final int UINT32 = 32 / 8;
    public static final int UINT64 = 64 / 8;
    public static final int GUID = 128 / 8;

    /**
     * Use a String representation of a GUID as predicate.
     * @param guid GUID, for example "caa16737-fa36-4d43-b3b6-33f0aa44e76b"
     * @return expression to use as predicate
     */
    public static Expression guid(final String guid) {
        final String[] parts = guid.split("-");
        if (parts.length != 5) {
            throw new IllegalArgumentException("Invalid GUID string: " + guid);
        }

        // Note that GUID bytes differ from UUID bytes, as the first 3 parts are reversed
        // Use ByteBuffer instead of long to make sure no leading zeroes are omitted
        final ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putInt(0, Integer.reverseBytes((int) Long.parseLong(parts[0], 16)));
        buffer.putShort(4, Short.reverseBytes((short) Integer.parseInt(parts[1], 16)));
        buffer.putShort(6, Short.reverseBytes((short) Integer.parseInt(parts[2], 16)));
        buffer.putLong(8, Long.parseLong(parts[4], 16));
        buffer.putShort(8, (short) Integer.parseInt(parts[3], 16));

        return eq(con(new Value(buffer.array(), ENC)));
    }
}
