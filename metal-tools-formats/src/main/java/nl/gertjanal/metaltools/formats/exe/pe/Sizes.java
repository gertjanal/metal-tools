package nl.gertjanal.metaltools.formats.exe.pe;

public class Sizes {

    static final int CHAR = 1;
    static final int SHORT = 2;
    static final int LONG = 4;
    static final int POINTER = 2;
	//
//    static Token map(final String defName, final StringProperty property, final int size, final long value, final String text) {
//        return seq(
//            Shorthand.def(defName, size, eqNum(con(value))),
//            let(property, con(text)));
//    }
//
//    static Token map(final StringProperty defName, final StringProperty property, final int size, final long value, final String text) {
//        return seq(
//            Glue.def(defName, size, eqNum(con(value))),
//            let(property, con(text)));
//    }
//
//    static Token map(final StringProperty property, final int size, final long value, final String text) {
//        return map("mapper", property, size, value, text);
//    }
}
