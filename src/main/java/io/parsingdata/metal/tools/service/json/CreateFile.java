package io.parsingdata.metal.tools.service.json;

public class CreateFile {
    private String _name;
    private long _size;

    public String getName() {
        return _name;
    }

    public void setName(final String name) {
        _name = name;
    }

    public long getSize() {
        return _size;
    }

    public void setSize(final long size) {
        _size = size;
    }
}