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
package org.datacleaner.database;

import java.io.File;
import java.io.Serializable;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;

import org.apache.commons.vfs2.FileObject;
import org.apache.metamodel.util.CollectionUtils;
import org.datacleaner.extensions.ClassLoaderUtils;
import org.datacleaner.util.ReflectionUtils;
import org.datacleaner.util.VFSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a database driver that the user has installed. Such database
 * drivers are based on (JAR) files on the filesystem and are loaded dynamically
 * (as opposed to statically loaded drivers, which are loaded at startup time).
 * 表示用户已安装的数据库驱动程序。此类数据库驱动程序基于文件系统上的（JAR）文件，
 * 并且是动态加载的（与在启动时加载的静态加载的驱动程序相反）。
 */
public final class UserDatabaseDriver implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(UserDatabaseDriver.class);
    private final File[] _files;
    private final String _driverClassName;
    private transient Driver _driverInstance;
    private transient Driver _registeredDriver;
    private transient boolean _loaded = false;

    public UserDatabaseDriver(final FileObject[] files, final String driverClassName) {
        this(convert(files), driverClassName);
    }

    public UserDatabaseDriver(final File[] files, final String driverClassName) {
        if (files == null) {
            throw new IllegalStateException("Driver file(s) cannot be null");
        }
        if (driverClassName == null) {
            throw new IllegalStateException("Driver class name cannot be null");
        }
        _files = files;
        _driverClassName = driverClassName;
    }

    private static File[] convert(final FileObject[] files) {
        return CollectionUtils.map(files, VFSUtils::toFile).toArray(new File[0]);

    }

    public String getDriverClassName() {
        return _driverClassName;
    }

    public File[] getFiles() {
        return Arrays.copyOf(_files, _files.length);
    }

    public UserDatabaseDriver loadDriver() throws IllegalStateException {
        if (!_loaded) {
            final ClassLoader driverClassLoader = ClassLoaderUtils.createClassLoader(_files);

            final Class<?> loadedClass;
            try {
                loadedClass = Class.forName(_driverClassName, true, driverClassLoader);
            } catch (final Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new IllegalStateException("Could not load driver class", e);
            }
            logger.info("Loaded class: {}", loadedClass.getName());

            if (ReflectionUtils.is(loadedClass, Driver.class)) {
                _driverInstance = (Driver) ReflectionUtils.newInstance(loadedClass);
                _registeredDriver = new DriverWrapper(_driverInstance);
                try {
                    DriverManager.registerDriver(_registeredDriver);
                } catch (final SQLException e) {
                    throw new IllegalStateException("Could not register driver", e);
                }
            } else {
                throw new IllegalStateException("Class is not a Driver class: " + _driverClassName);
            }
            _loaded = true;
        }
        return this;
    }

    public void unloadDriver() {
        try {
            DriverManager.deregisterDriver(_registeredDriver);
            _registeredDriver = null;
            _driverInstance = null;
            _loaded = false;
        } catch (final SQLException e) {
            logger.error("Exception occurred while unloading driver: " + _driverClassName, e);
        }
    }

    public boolean isLoaded() {
        return _loaded;
    }

    public DatabaseDriverState getState() {
        if (_loaded) {
            return DatabaseDriverState.INSTALLED_WORKING;
        }
        return DatabaseDriverState.INSTALLED_NOT_WORKING;
    }
}
