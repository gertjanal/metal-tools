/**
 * Copyright 2016 Gertjan Al
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.gertjanal.metaltools.service.json;

import java.math.BigInteger;
import java.util.UUID;

public class Slice {
    private BigInteger _offset;
    private int _size;
    private UUID _id;

    public Slice() {
    }

    public Slice(final Slice slice, final UUID id) {
        _offset = slice._offset;
        _size = slice._size;
        _id = id;
    }

    public Slice(final BigInteger offset, final int size, final UUID id) {
        _offset = offset;
        _size = size;
        _id = id;
    }

    public BigInteger getOffset() {
        return _offset;
    }

    public void setOffset(final BigInteger offset) {
        _offset = offset;
    }

    public int getSize() {
        return _size;
    }

    public void setSize(final int size) {
        _size = size;
    }

    public UUID getId() {
        return _id;
    }

    public void setId(final UUID id) {
        _id = id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_id == null) ? 0 : _id.hashCode());
        result = prime * result + ((_offset == null) ? 0 : _offset.hashCode());
        result = prime * result + _size;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Slice other = (Slice) obj;
        if (_id == null) {
            if (other._id != null) {
                return false;
            }
        }
        else if (!_id.equals(other._id)) {
            return false;
        }
        if (_offset == null) {
            if (other._offset != null) {
                return false;
            }
        }
        else if (!_offset.equals(other._offset)) {
            return false;
        }
        if (_size != other._size) {
            return false;
        }
        return true;
    }
}
