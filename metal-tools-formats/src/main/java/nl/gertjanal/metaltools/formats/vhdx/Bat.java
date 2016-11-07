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

package nl.gertjanal.metaltools.formats.vhdx;

import static io.parsingdata.metal.Shorthand.add;
import static io.parsingdata.metal.Shorthand.cho;
import static io.parsingdata.metal.Shorthand.con;
import static io.parsingdata.metal.Shorthand.def;
import static io.parsingdata.metal.Shorthand.div;
import static io.parsingdata.metal.Shorthand.gtNum;
import static io.parsingdata.metal.Shorthand.last;
import static io.parsingdata.metal.Shorthand.ltNum;
import static io.parsingdata.metal.Shorthand.mod;
import static io.parsingdata.metal.Shorthand.mul;
import static io.parsingdata.metal.Shorthand.nod;
import static io.parsingdata.metal.Shorthand.pre;
import static io.parsingdata.metal.Shorthand.ref;
import static io.parsingdata.metal.Shorthand.repn;
import static io.parsingdata.metal.Shorthand.seq;
import static io.parsingdata.metal.Shorthand.sub;
import static io.parsingdata.metal.expression.value.ConstantFactory.createFromNumeric;
import static nl.gertjanal.metaltools.formats.vhdx.Constants.UINT64;
import static nl.gertjanal.metaltools.formats.vhdx.Metadata.BLOCK_SIZE_NAME;
import static nl.gertjanal.metaltools.formats.vhdx.Metadata.LOGICAL_SECTOR_SIZE_NAME;
import static nl.gertjanal.metaltools.formats.vhdx.Metadata.VIRTUAL_DISK_SIZE_NAME;
import static nl.gertjanal.metaltools.formats.vhdx.Region.FILE_OFFSET_NAME;

import java.math.BigDecimal;
import java.math.BigInteger;

import io.parsingdata.metal.data.Environment;
import io.parsingdata.metal.data.OptionalValueList;
import io.parsingdata.metal.encoding.Encoding;
import io.parsingdata.metal.expression.Expression;
import io.parsingdata.metal.expression.comparison.ComparisonExpression;
import io.parsingdata.metal.expression.value.BinaryValueExpression;
import io.parsingdata.metal.expression.value.OptionalValue;
import io.parsingdata.metal.expression.value.Value;
import io.parsingdata.metal.expression.value.ValueExpression;
import io.parsingdata.metal.token.Token;

/**
 * Metal tokens for VHDX Block Allocation Table.
 * Based on VHDX Format Specification v1.00: https://www.microsoft.com/en-us/download/details.aspx?id=34750
 *
 * @author Gertjan Al.
 */
public class Bat {
	private static final Token PAYLOAD_BLOCK_FULLY_PRESENT = def("payload_block_fully_present", UINT64, state(6));

	public static Token batEntry(final boolean resolveData) {
		return cho(
			// State:3
			// Reserved:17
			// FileOffsetMB:44
			// The State field specifies how the associated data block or sector bitmap block should be treated.
			// The FileOffsetMB field specifies the offset within the file in units of 1 MB.
			// The payload or sector bitmap block must reside after the header section and must not overlap any other structure.
			// The FileOffsetMB field value must be unique across all the BAT entries when it is other than zero.
			def("payload_block_not_present", UINT64, state(0)),
			def("payload_block_undefined", UINT64, state(1)),
			def("payload_block_zero", UINT64, state(2)),
			def("payload_block_unmapped", UINT64, state(3)),
			payloadFullyPresent(resolveData),
			def("payload_block_partially_present", UINT64, state(7)));
	}

	private static Token payloadFullyPresent(final boolean resolveData) {
		if (!resolveData) {
			return PAYLOAD_BLOCK_FULLY_PRESENT;
		}
		return seq(
			PAYLOAD_BLOCK_FULLY_PRESENT,
			sub(
				seq(
					def("payload_block_start", 1),
					nod(sub(last(ref(BLOCK_SIZE_NAME)), con(2))),
					def("payload_block_end", 1)),
				mul(
					dataOffset(last(ref("payload_block_fully_present"))),
					con(0x100000))));
	}

	private static final ValueExpression CHUNCK_RATIO = div(
		mul(
			con(1 << 23), // 2^23
			last(ref(LOGICAL_SECTOR_SIZE_NAME))),
		last(ref(BLOCK_SIZE_NAME)));

	private static final ValueExpression DATA_BLOCKS_COUNT = divCeil(
		last(ref(VIRTUAL_DISK_SIZE_NAME)),
		last(ref(BLOCK_SIZE_NAME)));

	private static final ValueExpression SECTOR_BITMAP_BLOCKSCOUNT = divCeil(
		DATA_BLOCKS_COUNT,
		CHUNCK_RATIO);

	public static final ValueExpression TOTAL_BAT_ENTRIES_DYNAMIC = add(
		DATA_BLOCKS_COUNT,
		divFloor(
			sub(DATA_BLOCKS_COUNT, con(1)),
			CHUNCK_RATIO));

	public static final ValueExpression TOTAL_BAT_ENTRIES_DIFFERENCING = mul(
		SECTOR_BITMAP_BLOCKSCOUNT,
		add(CHUNCK_RATIO, con(1)));

	public static final Token SECTOR_BITMAP = cho(
		// This state indicates that this sector bitmap block’s contents are undefined and that the block is not allocated in the file.
		// For a fixed or dynamic VHDX file, all sector bitmap block entries must be in this state.
		// For a differencing VHDX file, a sector bitmap block entry must not be in this state if
		// any of the associated payload block entries are in the PAYLOAD_BLOCK_PARTIALLY_PRESENT state.
		def("sector_bitmap_block_not_present", UINT64, state(0)),

		// This state indicates that the sector bitmap block contents are defined in the file at a location pointed to by the FileOffsetMB field.
		// For a fixed or dynamic VHDX file, a sector bitmap block entry must not be in this state.
		// For differencing VHDX file, a sector bitmap block entry must be set to the SB_BLOCK_ PRESENT state if any associated payload blocks
		// are the PAYLOAD_BLOCK_ PARTIALLY_PRESENT state. The sector bitmap block contents are defined in the file at the location specified
		// by the FileOffsetMB field.
		def("sector_bitmap_block_present", UINT64, state(6)));

	public static Token bat(final boolean resolveData) {
		// The block allocation table consists of bat entries.
		// After n entries (n == chunk ratio), a block is added describing if the previous blocks should be used (in case of a differencing disk).
		// Differencing disks are a overlay / snapshot on top of another parent vhdx,
		// for example a vhdx with an application on top of a vhdx operating system.
		return sub(
			seq(
				pre(
					// The bat consists of n entries > chunk ratio
					seq(
						repn(
							seq(
								repn(
									batEntry(resolveData),
									CHUNCK_RATIO),
								SECTOR_BITMAP),
							divFloor(TOTAL_BAT_ENTRIES_DYNAMIC, CHUNCK_RATIO)),
						repn(
							batEntry(resolveData),
							mod(TOTAL_BAT_ENTRIES_DYNAMIC, CHUNCK_RATIO))),
					gtNum(TOTAL_BAT_ENTRIES_DYNAMIC, CHUNCK_RATIO)),
				pre(
					// The bat consists of n entries < chunk ratio (no sector bitmaps)
					repn(
						batEntry(resolveData),
						TOTAL_BAT_ENTRIES_DYNAMIC),
					ltNum(TOTAL_BAT_ENTRIES_DYNAMIC, CHUNCK_RATIO))),
			last(ref(FILE_OFFSET_NAME)));
	}

	private static Expression state(final int state) {
		return new ComparisonExpression(null, con(state)) {
			@Override
			public boolean compare(final Value left, final Value right) {
				return right.asNumeric().longValue() == (left.asNumeric().longValue() & 0x07); // First 3 bits
			}
		};
	}

	private static ValueExpression dataOffset(final ValueExpression value) {
		return new ValueExpression() {

			@Override
			public OptionalValueList eval(final Environment env, final Encoding enc) {
				final OptionalValueList values = value.eval(env, enc);
				if (values.isEmpty()) {
					return OptionalValueList.EMPTY;
				}
				final long fileOffset = values.head.get().asNumeric().longValue() >> 20;
				return OptionalValueList.create(OptionalValue.of(createFromNumeric(fileOffset, enc)));
			}
		};
	}

	private static BinaryValueExpression divFloor(final ValueExpression left, final ValueExpression right) {
		return new BinaryValueExpression(left, right) {

			@Override
			public OptionalValue eval(final Value left, final Value right, final Environment env, final Encoding enc) {
				if (right.asNumeric().equals(BigInteger.ZERO)) {
					return OptionalValue.empty();
				}
				final BigDecimal leftDecimal = new BigDecimal(left.asNumeric());
				final BigDecimal rightDecimal = new BigDecimal(right.asNumeric());
				return OptionalValue.of(createFromNumeric(leftDecimal.divide(rightDecimal, BigDecimal.ROUND_FLOOR).toBigInteger(), enc));
			}
		};
	}

	private static BinaryValueExpression divCeil(final ValueExpression left, final ValueExpression right) {
		return new BinaryValueExpression(left, right) {

			@Override
			public OptionalValue eval(final Value left, final Value right, final Environment env, final Encoding enc) {
				if (right.asNumeric().equals(BigInteger.ZERO)) {
					return OptionalValue.empty();
				}
				final BigDecimal leftDecimal = new BigDecimal(left.asNumeric());
				final BigDecimal rightDecimal = new BigDecimal(right.asNumeric());
				return OptionalValue.of(createFromNumeric(leftDecimal.divide(rightDecimal, BigDecimal.ROUND_CEILING).toBigInteger(), enc));
			}
		};
	}
}
