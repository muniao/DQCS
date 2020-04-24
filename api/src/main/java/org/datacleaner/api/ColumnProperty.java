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
package org.datacleaner.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.metamodel.schema.Column;

/**
 * Annotation to provide the extra metadata about a property that it is a
 * reference to a {@link Column}. The reference may be a hard reference (an
 * actual {@link Column}) or just a simple String reference (usually to make it
 * easier to serialize and deserialize state and handle schema changes).
 * 提供关于属性的额外元数据的注释，该属性是对{@link Column}的引用。
 * 该引用可以是硬引用（实际的{@link Column}），
 * 也可以是简单的String引用（通常使更容易序列化和反序列化状态以及处理模式更改）。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@Documented
@Inherited
public @interface ColumnProperty {

    /**
     * Determines if a (single) column property may be used semantically as an
     * column-array where each array-item represents a new analyzer job with an
     * otherwise duplicated configuration.
     */
    boolean escalateToMultipleJobs() default false;
}
