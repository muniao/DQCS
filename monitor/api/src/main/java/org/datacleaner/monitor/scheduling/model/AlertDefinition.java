/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
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
package org.datacleaner.monitor.scheduling.model;

import java.io.Serializable;

import org.datacleaner.monitor.shared.model.MetricIdentifier;

/**
 * Defines the rules of an alert that the user has configured.
 * 定义用户已配置的警报规则。
 */
public class AlertDefinition implements Serializable, Comparable<AlertDefinition> {

    private static final long serialVersionUID = 1L;

    private MetricIdentifier _metricIdentifier;
    private String _description;
    private Number _minimumValue;
    private Number _maximumValue;
    private AlertSeverity _severity;

    // no-args constructor
    public AlertDefinition() {
        this(null, null, null, null, null);
    }

    public AlertDefinition(String description, MetricIdentifier metricIdentifier, Number minimumValue,
            Number maximumValue, AlertSeverity severity) {
        _description = description;
        _metricIdentifier = metricIdentifier;
        _minimumValue = minimumValue;
        _maximumValue = maximumValue;
        _severity = severity;
    }

    public Number getMaximumValue() {
        return _maximumValue;
    }

    public Number getMinimumValue() {
        return _minimumValue;
    }

    public MetricIdentifier getMetricIdentifier() {
        if (_metricIdentifier == null) {
            return new MetricIdentifier();
        }
        return _metricIdentifier;
    }

    public void setMetricIdentifier(MetricIdentifier metricIdentifier) {
        _metricIdentifier = metricIdentifier;
    }

    public void setMaximumValue(Number maximumValue) {
        _maximumValue = maximumValue;
    }

    public void setMinimumValue(Number minimumValue) {
        _minimumValue = minimumValue;
    }

    public String getDescription() {
        if (_description == null || _description.trim().isEmpty()) {
            if (_metricIdentifier == null) {
                return "";
            } else if (_minimumValue == null && _maximumValue == null) {
                return _metricIdentifier.getDisplayName();
            } else {
                StringBuilder sb = new StringBuilder();
                if (_severity == AlertSeverity.WARNING || _severity == AlertSeverity.FATAL) {
                    sb.append("Req. ");
                } else {
                    sb.append("Expect ");
                }
                sb.append(_metricIdentifier.getMetricDescriptorName());
                if (_minimumValue != null && _maximumValue != null) {
                    sb.append("from " + _minimumValue + " to " + _maximumValue);
                } else if (_minimumValue != null) {
                    sb.append("greater than " + _minimumValue);
                } else {
                    sb.append("less than " + _minimumValue);
                }
                return sb.toString();
            }
        }
        return _description;
    }

    public void setDescription(String description) {
        _description = description;
    }

    public AlertSeverity getSeverity() {
        return _severity;
    }

    public void setSeverity(AlertSeverity severity) {
        _severity = severity;
    }

    @Override
    public String toString() {
        return getDescription();
    }

    @Override
    public int compareTo(AlertDefinition other) {
        if (other == this) {
            return 0;
        }
        int diff = getMetricIdentifier().compareTo(other.getMetricIdentifier());
        if (diff == 0) {
            diff = getDescription().compareTo(other.getDescription());
        }
        return diff;
    }
}
