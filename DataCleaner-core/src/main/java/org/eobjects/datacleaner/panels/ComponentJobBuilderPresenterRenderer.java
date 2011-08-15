/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.panels;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Renderer;
import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.beans.api.RendererPrecedence;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.analyzer.job.builder.RowProcessingAnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.guice.InjectorBuilder;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;

/**
 * Renders/creates the default panels that present component job builders.
 * 
 * @author Kasper Sørensen
 */
@RendererBean(ComponentJobBuilderRenderingFormat.class)
public class ComponentJobBuilderPresenterRenderer implements
		Renderer<AbstractBeanJobBuilder<?, ?, ?>, ComponentJobBuilderPresenter> {

	@Inject
	WindowContext windowContext;

	@Inject
	AnalyzerBeansConfiguration configuration;

	@Inject
	InjectorBuilder injectorBuilder;

	@Override
	public RendererPrecedence getPrecedence(AbstractBeanJobBuilder<?, ?, ?> renderable) {
		return RendererPrecedence.LOW;
	}

	@Override
	public ComponentJobBuilderPresenter render(AbstractBeanJobBuilder<?, ?, ?> renderable) {
		final PropertyWidgetFactory propertyWidgetFactory = injectorBuilder.with(
				PropertyWidgetFactory.TYPELITERAL_BEAN_JOB_BUILDER, renderable).getInstance(PropertyWidgetFactory.class);

		if (renderable instanceof FilterJobBuilder) {
			FilterJobBuilder<?, ?> fjb = (FilterJobBuilder<?, ?>) renderable;
			return new FilterJobBuilderPanel(fjb, propertyWidgetFactory);
		} else if (renderable instanceof TransformerJobBuilder) {
			TransformerJobBuilder<?> tjb = (TransformerJobBuilder<?>) renderable;
			return new TransformerJobBuilderPanel(tjb, windowContext, propertyWidgetFactory, configuration);
		} else if (renderable instanceof RowProcessingAnalyzerJobBuilder) {
			RowProcessingAnalyzerJobBuilder<?> ajb = (RowProcessingAnalyzerJobBuilder<?>) renderable;
			return new RowProcessingAnalyzerJobBuilderPanel(ajb, propertyWidgetFactory);
		}
		throw new UnsupportedOperationException();
	}

}
