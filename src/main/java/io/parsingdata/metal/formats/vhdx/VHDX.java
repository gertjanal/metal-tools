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

import static io.parsingdata.metal.Shorthand.cho;
import static io.parsingdata.metal.Shorthand.con;
import static io.parsingdata.metal.Shorthand.expTrue;
import static io.parsingdata.metal.Shorthand.gtNum;
import static io.parsingdata.metal.Shorthand.last;
import static io.parsingdata.metal.Shorthand.ltNum;
import static io.parsingdata.metal.Shorthand.ref;
import static io.parsingdata.metal.Shorthand.seq;
import static io.parsingdata.metal.Shorthand.sub;
import static io.parsingdata.metal.formats.vhdx.FileIdentifier.FILE_IDENTIFIER;
import static io.parsingdata.metal.formats.vhdx.Header.header;
import static io.parsingdata.metal.formats.vhdx.Log.LOGS;
import static io.parsingdata.metal.formats.vhdx.Region.REGION;

import io.parsingdata.metal.token.Token;

/**
 * Virtual Hard Disk X implementation.
 * Based on VHDX Format Specification v1.00: https://www.microsoft.com/en-us/download/details.aspx?id=34750
 *
 * @author Netherlands Forensic Institute.
 */
public class VHDX {
    private static final Token HEADERS_REGIONS = cho(
        seq(
            sub("header", header(expTrue()), con(0x10000)), // 64KiB
            sub("oldHeader", header(ltNum(last(ref("header.SequenceNumber")))), con(0x20000)), // 128KiB
            sub("region", REGION, con(0x30000)), // 192 KiB
            sub("oldRegion", REGION, con(0x40000))), // 256 KiB
        seq(
            sub("oldHeader", header(expTrue()), con(0x10000)), // 64KiB
            sub("header", header(gtNum(last(ref("oldHeader.SequenceNumber")))), con(0x20000)), // 128KiB
            sub("oldRegion", REGION, con(0x30000)), // 192 KiB
            sub("region", REGION, con(0x40000)))); // 256 KiB

    public static final Token VHDX = seq(
        FILE_IDENTIFIER,
        HEADERS_REGIONS,
        LOGS); // BAT is not needed as it is calculated on-read in the VHDXstream code
}
