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
package org.datacleaner.sample.ui;

import javax.inject.Inject;

import org.datacleaner.api.Renderer;
import org.datacleaner.api.RendererBean;
import org.datacleaner.api.RendererPrecedence;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.guice.DCModule;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.panels.ComponentBuilderPresenterRenderingFormat;
import org.datacleaner.panels.TransformerComponentBuilderPresenter;
import org.datacleaner.sample.HelloWorldTransformer;
import org.datacleaner.widgets.properties.PropertyWidgetFactory;

/**
 * A sample renderer which provides a presenter object for the configuration
 * panel of a transformer.
 * 提供渲染器对象的示例渲染器，用于转换器的配置面板。
 *
 * Renderers like this are optional, but allows for absolute control over the
 * User Interface which may be useful for certain types of extensions.
 * 这样的渲染器是可选的，但可以对用户界面进行绝对控制，这对于某些类型的扩展可能很有用。
 */
@RendererBean(ComponentBuilderPresenterRenderingFormat.class)
public class HelloWorldTransformerBuilderRenderer
        implements Renderer<TransformerComponentBuilder<?>, TransformerComponentBuilderPresenter> {

    @Inject
    WindowContext windowContext;

    @Inject
    DCModule dcModule;

    @Inject
    DataCleanerConfiguration configuration;

    @Override
    public RendererPrecedence getPrecedence(final TransformerComponentBuilder<?> renderable) {
        if (renderable.getDescriptor().getComponentClass() == HelloWorldTransformer.class) {
            return RendererPrecedence.HIGHEST;
        }
        return RendererPrecedence.NOT_CAPABLE;
    }

    @Override
    public TransformerComponentBuilderPresenter render(final TransformerComponentBuilder<?> tjb) {
        final PropertyWidgetFactory propertyWidgetFactory =
                dcModule.createChildInjectorForComponent(tjb).getInstance(PropertyWidgetFactory.class);
        return new HelloWorldTransformerPresenter(tjb, windowContext, propertyWidgetFactory, configuration);
    }
}
