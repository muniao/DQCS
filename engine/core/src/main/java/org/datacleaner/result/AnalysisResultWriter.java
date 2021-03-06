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
package org.datacleaner.result;

import java.io.OutputStream;
import java.io.Writer;
import java.util.function.Supplier;

import org.datacleaner.configuration.DataCleanerConfiguration;

/**
 * Defines the interface for components that write an {@link AnalysisResult}, typically to a file or {@link System#out}.
 * 为通常将{@link AnalysisResult}写入文件或{@link System＃out}的组件定义接口。
 */
public interface AnalysisResultWriter {

    void write(AnalysisResult result, DataCleanerConfiguration configuration, Supplier<Writer> writerRef,
            Supplier<OutputStream> outputStreamRef) throws Exception;
}
