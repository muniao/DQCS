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
package org.datacleaner.beans.filter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.metamodel.query.FilterItem;
import org.apache.metamodel.query.OperatorType;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.query.SelectItem;
import org.apache.metamodel.schema.Column;
import org.datacleaner.api.Alias;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Distributed;
import org.datacleaner.api.HasLabelAdvice;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.QueryOptimizedFilter;
import org.datacleaner.api.Validate;
import org.datacleaner.components.categories.FilterCategory;
import org.datacleaner.components.convert.ConvertToBooleanTransformer;
import org.datacleaner.components.convert.ConvertToDateTransformer;
import org.datacleaner.components.convert.ConvertToNumberTransformer;
import org.datacleaner.components.convert.ConvertToStringTransformer;
import org.datacleaner.util.ReflectionUtils;

import com.google.common.base.Joiner;

@Named("EqualsFilter.name")
@Description("EqualsFilter.Description")
@Categorized(FilterCategory.class)
@Distributed()
public class EqualsFilter implements QueryOptimizedFilter<EqualsFilter.Category>, HasLabelAdvice {

    public enum Category {
        @Alias("VALID") @Description("Outcome when the operands of the filter are equal.")EQUALS,

        @Alias("INVALID") @Description("Outcome when the operands of the filter are not equal.")NOT_EQUALS
    }

    @Inject
    @Configured(order = 1)
    @Alias("Column")
    @Description("The column to compare values of")
    InputColumn<?> inputColumn;

    @Inject
    @Configured(order = 21, required = false)
    @Alias("Values")
    @Description("Value(s) to compare with")
    String[] compareValues = new String[1];

    @Inject
    @Configured(order = 22, required = false)
    @Description("Column holding value to compare with")
    InputColumn<?> compareColumn;

    private Object[] _operands;
    private boolean _number = false;

    public EqualsFilter() {
    }

    public EqualsFilter(final String[] values, final InputColumn<?> column) {
        this();
        this.compareValues = values;
        this.inputColumn = column;
        init();
    }

    public EqualsFilter(final InputColumn<?> inputColumn, final InputColumn<?> valueColumn) {
        this();
        this.inputColumn = inputColumn;
        this.compareColumn = valueColumn;
        init();
    }

    public void setValues(final String[] values) {
        this.compareValues = values;
    }

    public void setValueColumn(final InputColumn<?> valueColumn) {
        this.compareColumn = valueColumn;
    }

    @Validate
    public void validate() {
        if (compareColumn == null) {
            if (compareValues == null || compareValues.length == 0) {
                throw new IllegalStateException("Either 'Values' or 'Value column' needs to be specified.");
            }
        }
    }

    @Initialize
    public void init() {
        final Class<?> dataType = inputColumn.getDataType();
        if (ReflectionUtils.isNumber(dataType)) {
            _number = true;
        }

        final List<Object> operandList = new ArrayList<>();
        if (compareColumn != null) {
            operandList.add(null);
        }
        if (compareValues != null) {
            for (final String value : compareValues) {
                final Object operand = toOperand(value);
                operandList.add(operand);
            }
        }

        _operands = operandList.toArray();
    }

    @Override
    public String getSuggestedLabel() {
        if (inputColumn == null) {
            return null;
        }
        if (compareColumn != null) {
            return inputColumn.getName() + " = " + compareColumn.getName();
        }
        if (compareValues == null || compareValues.length == 0) {
            return null;
        }

        if (compareValues.length == 1) {
            return inputColumn.getName() + " = " + compareValues[0];
        }

        return inputColumn.getName() + " IN (" + Joiner.on(',').join(compareValues) + ")";
    }

    private Object toOperand(final Object value) {
        final Class<?> dataType = inputColumn.getDataType();
        if (ReflectionUtils.isBoolean(dataType)) {
            return ConvertToBooleanTransformer.transformValue(value, ConvertToBooleanTransformer.DEFAULT_TRUE_TOKENS,
                    ConvertToBooleanTransformer.DEFAULT_FALSE_TOKENS);
        } else if (ReflectionUtils.isDate(dataType)) {
            return ConvertToDateTransformer.getInternalInstance().transformValue(value);
        } else if (ReflectionUtils.isNumber(dataType)) {
            return ConvertToNumberTransformer.transformValue(value);
        } else if (ReflectionUtils.isString(dataType)) {
            return ConvertToStringTransformer.transformValue(value);
        } else {
            return value;
        }
    }

    @Override
    public EqualsFilter.Category categorize(final InputRow inputRow) {
        final Object inputValue = inputRow.getValue(inputColumn);

        final Object compareValue;
        if (compareColumn == null) {
            compareValue = null;
        } else {
            compareValue = inputRow.getValue(compareColumn);
        }

        return filter(inputValue, compareValue);
    }

    public EqualsFilter.Category filter(final Object value) {
        return filter(value, null);
    }

    public EqualsFilter.Category filter(final Object value, final Object compareValue) {
        final Object[] operands;

        if (compareColumn != null) {
            operands = new Object[] { toOperand(compareValue) };
        } else {
            operands = _operands;
        }

        if (value == null) {
            for (final Object obj : operands) {
                if (obj == null) {
                    return Category.EQUALS;
                }
            }
            return Category.NOT_EQUALS;
        } else {
            for (final Object operand : operands) {
                if (operand != null) {

                    if (_number) {
                        final Number n1 = (Number) operand;
                        final Number n2 = (Number) value;
                        if (equals(n1, n2)) {
                            return Category.EQUALS;
                        }
                    } else {
                        if (operand.equals(value)) {
                            return Category.EQUALS;
                        }
                        if (operand instanceof String) {
                            // convert to string to check
                            final String str1 = operand.toString();
                            final String str2 = value.toString();
                            if (str1.equals(str2)) {
                                return Category.EQUALS;
                            }
                        }
                    }
                }
            }
        }

        return Category.NOT_EQUALS;
    }

    private boolean equals(final Number n1, final Number n2) {
        if (n1 instanceof Short || n1 instanceof Integer || n1 instanceof Long || n2 instanceof Short
                || n2 instanceof Integer || n2 instanceof Long) {
            // use long comparision
            return n1.longValue() == n2.longValue();
        }
        return n1.doubleValue() == n2.doubleValue();
    }

    @Override
    public boolean isOptimizable(final Category category) {
        if (compareColumn != null && compareValues != null && compareValues.length > 0) {
            boolean hasCompareValues = false;
            for (final String compareValue : compareValues) {
                if (compareValue != null) {
                    hasCompareValues = true;
                }
            }
            return !hasCompareValues;
        }
        return true;
    }

    @Override
    public Query optimizeQuery(final Query q, final Category category) {
        final Column inputPhysicalColumn = inputColumn.getPhysicalColumn();
        if (category == Category.EQUALS) {
            if (compareColumn == null) {
                final SelectItem selectItem = new SelectItem(inputPhysicalColumn);
                final List<FilterItem> filterItems = new ArrayList<>();
                for (final Object operand : _operands) {
                    filterItems.add(new FilterItem(selectItem, OperatorType.EQUALS_TO, operand));
                }

                if (filterItems.size() == 1) {
                    q.where(filterItems.get(0));
                } else {
                    q.where(new FilterItem(filterItems.toArray(new FilterItem[filterItems.size()])));
                }
            } else {
                final Column valuePhysicalColumn = compareColumn.getPhysicalColumn();
                q.where(inputPhysicalColumn, OperatorType.EQUALS_TO, valuePhysicalColumn);
            }
        } else {
            if (compareColumn == null) {
                for (final Object operand : _operands) {
                    q.where(inputColumn.getPhysicalColumn(), OperatorType.DIFFERENT_FROM, operand);
                }
            } else {
                final Column valuePhysicalColumn = compareColumn.getPhysicalColumn();
                q.where(inputPhysicalColumn, OperatorType.DIFFERENT_FROM, valuePhysicalColumn);
            }
        }
        return q;
    }
}
