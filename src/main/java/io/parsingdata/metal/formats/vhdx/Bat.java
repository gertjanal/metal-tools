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

import static io.parsingdata.metal.Shorthand.add;
import static io.parsingdata.metal.Shorthand.cho;
import static io.parsingdata.metal.Shorthand.con;
import static io.parsingdata.metal.Shorthand.def;
import static io.parsingdata.metal.Shorthand.div;
import static io.parsingdata.metal.Shorthand.last;
import static io.parsingdata.metal.Shorthand.mod;
import static io.parsingdata.metal.Shorthand.mul;
import static io.parsingdata.metal.Shorthand.ref;
import static io.parsingdata.metal.Shorthand.repn;
import static io.parsingdata.metal.Shorthand.sub;
import static io.parsingdata.metal.formats.vhdx.Constants.UINT64;

import io.parsingdata.metal.expression.Expression;
import io.parsingdata.metal.expression.comparison.ComparisonExpression;
import io.parsingdata.metal.expression.value.Value;
import io.parsingdata.metal.expression.value.ValueExpression;
import io.parsingdata.metal.token.Token;

/**
 * Metal tokens for VHDX Block Allocation Table.
 * Based on VHDX Format Specification v1.00: https://www.microsoft.com/en-us/download/details.aspx?id=34750
 *
 * @author Netherlands Forensic Institute.
 */
public class Bat {

    private static final int PAYLOAD_BLOCK_NOT_PRESENT = 0;
    private static final int PAYLOAD_BLOCK_UNDEFINED = 1;
    private static final int PAYLOAD_BLOCK_ZERO = 2;
    private static final int PAYLOAD_BLOCK_UNMAPPED = 3;
    private static final int PAYLOAD_BLOCK_FULLY_PRESENT = 6;
    private static final int PAYLOAD_BLOCK_PARTIALLY_PRESENT = 7;

    public static final Token BAT_ENTRY = cho(
        // State:3
        // Reserved:17
        // FileOffsetMB:44
        // The State field specifies how the associated data block or sector bitmap block should be treated.
        // The FileOffsetMB field specifies the offset within the file in units of 1 MB.
        // The payload or sector bitmap block must reside after the header section and must not overlap any other structure.
        // The FileOffsetMB field value must be unique across all the BAT entries when it is other than zero.
        def("payload_block_not_present", UINT64, state(PAYLOAD_BLOCK_NOT_PRESENT)),
        def("payload_block_undefined", UINT64, state(PAYLOAD_BLOCK_UNDEFINED)),
        def("payload_block_zero", UINT64, state(PAYLOAD_BLOCK_ZERO)),
        def("payload_block_unmapped", UINT64, state(PAYLOAD_BLOCK_UNMAPPED)),
        def("payload_block_fully_present", UINT64, state(PAYLOAD_BLOCK_FULLY_PRESENT)),
        def("payload_block_partially_present", UINT64, state(PAYLOAD_BLOCK_PARTIALLY_PRESENT)));

    private static final ValueExpression CHUNCK_RATIO = div(
        mul(
            con(1 << 23), // 2^23
            last(ref("LogicalSectorSize"))),
        last(ref("BlockSize")));

    // ceil(VirtualDiskSize / BlockSize) == (VirtualDiskSize + (BlockSize - (VirtualDiskSize % BlockSize)) / BlockSize)
    private static final ValueExpression DATA_BLOCKS_COUNT = div(
        add(
            sub(
                last(ref("BlockSize")),
                mod(last(ref("VirtualDiskSize")), last(ref("BlockSize")))),
            last(ref("VirtualDiskSize"))),
        last(ref("BlockSize")));

    // ceil (DataBlocksCount / Chunk Ratio) == (DataBlocksCount + (ChunckRatio - (DataBlocksCount % ChunckRatio)) / ChunkRatio
    private static final ValueExpression SECTOR_BITMAP_BLOCKSCOUNT = div(
        add(
            CHUNCK_RATIO,
            sub(CHUNCK_RATIO,
                mod(DATA_BLOCKS_COUNT, CHUNCK_RATIO))),
        CHUNCK_RATIO);

    public static final ValueExpression TOTAL_BAT_ENTRIES = mul(
        SECTOR_BITMAP_BLOCKSCOUNT,
        add(CHUNCK_RATIO, con(1)));

    public static final Token BAT = sub(
        repn(
            BAT_ENTRY,
            TOTAL_BAT_ENTRIES),
        last(ref("bat.FileOffset")));

    private static Expression state(final int state) {
        return new ComparisonExpression(null, con(state)) {
            @Override
            public boolean compare(final Value left, final Value right) {
                return right.asNumeric().longValue() == (left.asNumeric().longValue() & 0x07); // First 3 bits
            }
        };
    }
}
