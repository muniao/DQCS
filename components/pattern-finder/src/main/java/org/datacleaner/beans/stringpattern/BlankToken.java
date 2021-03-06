/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.datacleaner.beans.stringpattern;

import org.datacleaner.util.LabelUtils;

/**
 * Token which represents a null
 * 表示空值的令牌
 */
public class BlankToken implements Token {

    public static final Token INSTANCE = new BlankToken();

    private BlankToken() {
    }

    @Override
    public TokenType getType() {
        return TokenType.PREDEFINED;
    }

    @Override
    public String getString() {
        return LabelUtils.BLANK_LABEL;
    }

    @Override
    public char charAt(final int index) {
        throw new UnsupportedOperationException("Blank string does not have any chars");
    }

    @Override
    public int length() {
        return 0;
    }

}
