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
package org.datacleaner.monitor.wizard;

import java.util.Locale;

import org.datacleaner.monitor.configuration.TenantContext;
import java.util.function.Function;

/**
 * Context object which is shared throughout the wizard session
 * 在整个向导会话中共享的上下文对象
 */
public interface WizardContext {

    /**
     * Gets the locale of the client that is accessing this wizard.
     * 
     * @return
     */
    public Locale getLocale();

    /**
     * Gets the tenant context of the current wizard session
     * 
     * @return
     */
    public TenantContext getTenantContext();

    /**
     * Gets a read-only view of the HTTP session of the user.
     * 
     * @return
     */
    public Function<String, Object> getHttpSession();
}
