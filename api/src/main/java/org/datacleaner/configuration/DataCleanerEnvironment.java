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
package org.datacleaner.configuration;

import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.job.concurrent.TaskRunner;
import org.datacleaner.storage.StorageProvider;

/**
 * Represents the environment of one or more DataCleaner instances.
 * 表示一个或多个DataCleaner实例的环境。
 *
 * @see DataCleanerConfiguration
 */
public interface DataCleanerEnvironment {

    /**
     * Gets the {@link TaskRunner} defined in this environment
     *
     * @return the task runner defined in this environment
     */
    TaskRunner getTaskRunner();

    /**
     * @see DescriptorProvider
     * @return the descriptor provider defined in this configuration
     */
    DescriptorProvider getDescriptorProvider();

    /**
     * @see StorageProvider
     * @return the storage provider defined in this configuration
     */
    StorageProvider getStorageProvider();

    /**
     * @see InjectionManagerFactory
     * @return the injection manager factory defined in this configuration
     */
    InjectionManagerFactory getInjectionManagerFactory();

}
