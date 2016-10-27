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
import static io.parsingdata.metal.Shorthand.eq;
import static io.parsingdata.metal.Shorthand.expTrue;
import static io.parsingdata.metal.Shorthand.gtNum;
import static io.parsingdata.metal.Shorthand.last;
import static io.parsingdata.metal.Shorthand.nod;
import static io.parsingdata.metal.Shorthand.offset;
import static io.parsingdata.metal.Shorthand.pre;
import static io.parsingdata.metal.Shorthand.ref;
import static io.parsingdata.metal.Shorthand.repn;
import static io.parsingdata.metal.Shorthand.seq;
import static io.parsingdata.metal.Shorthand.str;
import static io.parsingdata.metal.Shorthand.sub;
import static io.parsingdata.metal.formats.vhdx.Constants.GUID;
import static io.parsingdata.metal.formats.vhdx.Constants.UINT16;
import static io.parsingdata.metal.formats.vhdx.Constants.UINT32;
import static io.parsingdata.metal.formats.vhdx.Constants.UINT64;
import static io.parsingdata.metal.formats.vhdx.Constants.guid;

import io.parsingdata.metal.expression.Expression;
import io.parsingdata.metal.token.Token;

/**
 * Metal tokens for VHDX Metadata.
 * Based on VHDX Format Specification v1.00: https://www.microsoft.com/en-us/download/details.aspx?id=34750
 *
 * @author Netherlands Forensic Institute.
 */
public class Metadata {
    public static final String LOGICAL_SECTOR_SIZE_NAME = "region.metadata.metadataTable.logicalSectorSize.data.LogicalSectorSize";
    public static final String VIRTUAL_DISK_SIZE_NAME = "region.metadata.metadataTable.virtualDiskSize.data.VirtualDiskSize";
    public static final String BLOCK_SIZE_NAME = "region.metadata.metadataTable.fileParameters.data.BlockSize";

    private static Token metadataTableEntry(final Expression predicate, final Token data) {
        return seq(
            // The ItemId field specifies a 128-bit identifier for the metadata item. The ItemId and IsUser value pair for an entry must be unique within the table.
            def("ItemId", GUID, predicate),

            // The Offset and Length fields specify the byte offset and length of the metadata item in bytes.
            // Offset must be at least 64 KB and is relative to the beginning of the metadata region. Length must be less than or equal to 1 MB.
            // The item described by this pair must fall entirely within the metadata region without overlapping any other item.
            // Finally, if Length is zero, then Offset must also be zero, in which case the metadata item should be considered present but empty.
            def("Offset", UINT32),
            def("Length", UINT32),

            // The IsUser field specifies whether this metadata item is considered system or user metadata.
            // Only up to 1024 entries may have this bit set; if more entries have this set, the metadata table is invalid.
            // A parser should generally not allow users to query metadata items that have this bit set to False.
            // def("IsUser", UINT32),
            def("Is", UINT32),

            // The IsVirtualDisk field specifies whether the metadata is file metadata or virtual disk metadata.
            // This determines the behavior when forking a new differencing VHDX file from an existing VHDX file, or when merging a differencing VHDX file into its parent.
            // When forking, a parser must copy all metadata items with this field set in the existing VHDX file to the new file, while leaving items with this field clear.
            // When merging, a parser must destroy any metadata with this field set in the parent and copy all metadata with this field set in the child to the parent.
            //def("IsVirtualDisk", UINT32),

            // The IsRequired field specifies whether the parser must understand this metadata item to be able load the file.
            // If this field is set to True and the parser does not recognize this metadata item, the parser must fail to load the file.
            //def("IsRequired", UINT32),
            //nod("Reserved", con(UINT32)),
            nod("Reserved2", con(UINT32)),

            pre(
                sub("data",
                    data,
                    add(offset(last(ref("metadataTableAnchor"))), last(ref("Offset")))),
                gtNum(last(ref("Length")), con(0))));
    }

    private static final Token FILE_PARAMETERS = seq(
        // The BlockSize field specifies the size of each payload block in bytes. The value must be a power of 2 and at least 1 MB and at most 256 MB.
        def("BlockSize", UINT32),

        // The LeaveBlocksAllocated field specifies whether blocks may be unallocated from the file.
        // If this field is set to 1, the parser should not change the state of any blocks to a BLOCK_NOT_PRESENT state,
        // and the parser must not reduce the size of the file to a value lower than would be required to allocate every block.
        // This field is intended to be used to create a fixed VHDX file that is fully provisioned.

        // The HasParent field specifies whether this file has a parent VHDX file. If set, the file is a differencing file,
        // and one or more parent locators specify the location and identity of the parent.
        // UINT32  LeaveBlocksAllocated:1;
        // UINT32  HasParent:1;
        // UINT32  Reserved:30;
        def("LeaveBlocksAllocated_HasParent", UINT32));

    private static final Token VIRTUAL_DISK_SIZE =
        // The VirtualDiskSize specifies the virtual disk size, in bytes.
        // This field must be a multiple of the LogicalSectorSize (See section 3.5.2.4) metadata item, and must be at most 64 TB.
        def("VirtualDiskSize", UINT64);

    private static final Token PAGE83_DATA =
        // This Page83Data Item is a GUID field and should be set to a value that unique across all SCSI devices that properly support page 0x83.
        def("Page83Data", GUID);

    private static final Token DISK_LOGICAL_SECTOR_SIZE =
        // The LogicalSectorSize field specifies the virtual disk’s sector size, in bytes. This value must be set to 512 or 4096.
        // A parser must expose the virtual disk as having the specified sector size, but it may fail to load files with sector sizes that it does not support.
        // If the file has a parent, the logical sector size for the parent and child must be the same.
        // Note that the LogicalSectorSize value also determines the chunk size.
        def("LogicalSectorSize", UINT32);

    private static final Token DISK_PHYSICAL_SECTOR_SIZE =
        // The PhysicalSectorSize field specifies the virtual disk’s physical sector size, in bytes. This value must be set to 512 or 4096.
        // A parser must expose the virtual disk as having the specified physical sector size, but it may fail to load files with sector sizes that it does not support.
        def("PhysicalSectorSize", UINT32);

    private static final Token PARENT_LOCATOR_ENTRY = seq(
        // The KeyOffset and KeyLength fields specify the offset within the metadata item and length in bytes of the entry’s key.
        // Both values must be must be greater than zero.
        def("KeyOffset", UINT32),

        // The ValueOffset and ValueLength fields specify the offset within the metadata item and length in bytes of entry’s value. Both values may be zero.
        // The key and value strings are to be UNICODE strings with UTF-16 little-endian encoding. There must be no internal NUL characters, and the length field must not include a trailing NUL character. The key string is case sensitive, and lower-case keys are recommended. All keys must be unique, and there is no ordering to the entries.
        def("ValueOffset", UINT32),

        def("KeyLength", UINT16),
        def("ValueLength", UINT16));

    private static final Token PARENT_LOCATOR_HEADER = seq(
        // The LocatorType field specifies the type of the parent virtual disk.
        // This value will be different for each type (for example, VHDX, VHD or ISCSI), so a parser must validate that it understands that type.
        def("LocatorType", GUID),
        nod("Reserved", con(UINT16)),

        // The KeyValueCount field specifies the number of key-value pairs defined for this parent locator.
        def("KeyValueCount", UINT16),

        repn("entry",
            PARENT_LOCATOR_ENTRY,
            last(ref("KeyValueCount"))));

    private static final Token METADATA_TABLE_ENTRY = cho(
        str("fileParameters", metadataTableEntry(guid("caa16737-fa36-4d43-b3b6-33f0aa44e76b"), FILE_PARAMETERS)),
        str("virtualDiskSize", metadataTableEntry(guid("2fa54224-cd1b-4876-b211-5dbed83bf4b8"), VIRTUAL_DISK_SIZE)),
        str("page38Data", metadataTableEntry(guid("beca12ab-b2e6-4523-93ef-c309e000c746"), PAGE83_DATA)),
        str("logicalSectorSize", metadataTableEntry(guid("8141bf1d-a96f-4709-ba47-f233a8faab5f"), DISK_LOGICAL_SECTOR_SIZE)),
        str("physicalSectorSize", metadataTableEntry(guid("cda348c7-445d-4471-9cc9-e9885251c556"), DISK_PHYSICAL_SECTOR_SIZE)),
        str("parentLocator", metadataTableEntry(guid("a8d35f2d-b30b-454d-abf7-d3d84834ab0c"), PARENT_LOCATOR_HEADER)),

        metadataTableEntry(expTrue(), nod(con(0))));

    public static final Token METADATA_TABLE_HEADER = seq("metadataTable",
        def("metadataTableAnchor", 0),

        // The Signature field must be 0x617461646174656D (“metadata” as ASCII).
        def("Signature", UINT64, eq(con("metadata"))),
        nod("Reserved", con(UINT16)),

        // The EntryCount field specifies the number of entries in the table. This value must be less than or equal to 2047.
        def("EntryCount", UINT16),
        nod("Reserved2", con(UINT32 * 5)),

        repn(
            METADATA_TABLE_ENTRY,
            last(ref("EntryCount"))));
}
