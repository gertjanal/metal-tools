package nl.gertjanal.metaltools.formats.rar;

import static io.parsingdata.metal.Shorthand.con;
import static io.parsingdata.metal.Shorthand.nod;

import io.parsingdata.metal.token.Token;

/**
 * The RAR archive format.
 * RAR 5.0: {@link http://www.rarlab.com/technote.htm}
 * RAR 4.x: {@link http://www.forensicswiki.org/wiki/RAR}
 *
 * @author Netherlands Forensic Institute.
 */
public class RAR {
	public static final int BYTE = 1;
	public static final int UINT16 = 16 / 8;
	public static final int UINT32 = 32 / 8;
	public static final int UINT64 = 64 / 8;

	public static final Token FORMAT = nod(con(0));
}
