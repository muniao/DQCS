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
package org.datacleaner.job.runner;

import org.datacleaner.api.InputRow;
import org.datacleaner.job.FilterOutcomes;

/**
 * Defines a callback for {@link RowProcessingConsumer}s to request a
 * {@link InputRow} to have it's next steps processed.
 * 为{@link RowProcessingConsumer}定义一个回调，以请求{@link InputRow}对其进行后续处理。
 */
public interface RowProcessingChain {

    void processNext(InputRow row, int distinctCount, FilterOutcomes outcomes);
}
