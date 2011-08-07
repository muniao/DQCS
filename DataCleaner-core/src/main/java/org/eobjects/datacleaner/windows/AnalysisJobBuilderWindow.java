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
package org.eobjects.datacleaner.windows;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eobjects.analyzer.beans.api.Renderer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerChangeListener;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.ExploringAnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.FilterChangeListener;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.analyzer.job.builder.MergedOutcomeJobBuilder;
import org.eobjects.analyzer.job.builder.RowProcessingAnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.SourceColumnChangeListener;
import org.eobjects.analyzer.job.builder.TransformerChangeListener;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.analyzer.job.builder.UnconfiguredConfiguredPropertyException;
import org.eobjects.analyzer.result.renderer.RendererFactory;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.Main;
import org.eobjects.datacleaner.actions.AddAnalyzerActionListener;
import org.eobjects.datacleaner.actions.AddTransformerActionListener;
import org.eobjects.datacleaner.actions.HideTabTextActionListener;
import org.eobjects.datacleaner.actions.JobBuilderTabTextActionListener;
import org.eobjects.datacleaner.actions.RunAnalysisActionListener;
import org.eobjects.datacleaner.actions.SaveAnalysisJobActionListener;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.guice.DatastoreName;
import org.eobjects.datacleaner.guice.JobFilename;
import org.eobjects.datacleaner.guice.Nullable;
import org.eobjects.datacleaner.panels.AbstractJobBuilderPanel;
import org.eobjects.datacleaner.panels.ComponentJobBuilderPresenter;
import org.eobjects.datacleaner.panels.ComponentJobBuilderRenderingFormat;
import org.eobjects.datacleaner.panels.DCGlassPane;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.panels.DatastoreListPanel;
import org.eobjects.datacleaner.panels.FilterListPanel;
import org.eobjects.datacleaner.panels.MetadataPanel;
import org.eobjects.datacleaner.panels.RowProcessingAnalyzerJobBuilderPresenter;
import org.eobjects.datacleaner.panels.SchemaTreePanel;
import org.eobjects.datacleaner.panels.SourceColumnsPanel;
import org.eobjects.datacleaner.panels.TransformerJobBuilderPresenter;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.LabelUtils;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.CollapsibleTreePanel;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.widgets.DCPersistentSizedPanel;
import org.eobjects.datacleaner.widgets.DCPopupBubble;
import org.eobjects.datacleaner.widgets.DCWindowMenuBar;
import org.eobjects.datacleaner.widgets.LoginStatusLabel;
import org.eobjects.datacleaner.widgets.result.DCRendererInitializer;
import org.eobjects.datacleaner.widgets.tabs.CloseableTabbedPane;
import org.eobjects.datacleaner.widgets.tabs.TabCloseEvent;
import org.eobjects.datacleaner.widgets.tabs.TabCloseListener;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main window in the DataCleaner GUI. This window is called the
 * AnalysisJobBuilderWindow because it's main purpose is to present a job that
 * is being built. Behind the covers this job state is respresented in the
 * {@link AnalysisJobBuilder} class.
 * 
 * @author Kasper Sørensen
 */
@Singleton
public final class AnalysisJobBuilderWindow extends AbstractWindow implements AnalyzerChangeListener,
		TransformerChangeListener, FilterChangeListener, SourceColumnChangeListener, TabCloseListener {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(AnalysisJobBuilderWindow.class);
	private static final ImageManager imageManager = ImageManager.getInstance();

	private static final int DEFAULT_WINDOW_WIDTH = 900;
	private static final int DEFAULT_WINDOW_HEIGHT = 630;

	private static final int SOURCE_TAB = 0;
	private static final int METADATA_TAB = 1;
	private static final int FILTERS_TAB = 2;

	private final Map<RowProcessingAnalyzerJobBuilder<?>, RowProcessingAnalyzerJobBuilderPresenter> _rowProcessingTabPresenters = new LinkedHashMap<RowProcessingAnalyzerJobBuilder<?>, RowProcessingAnalyzerJobBuilderPresenter>();
	private final Map<TransformerJobBuilder<?>, TransformerJobBuilderPresenter> _transformerPresenters = new LinkedHashMap<TransformerJobBuilder<?>, TransformerJobBuilderPresenter>();
	private final Map<ComponentJobBuilderPresenter, JComponent> _jobBuilderTabs = new HashMap<ComponentJobBuilderPresenter, JComponent>();
	private final AnalysisJobBuilder _analysisJobBuilder;
	private final AnalyzerBeansConfiguration _configuration;
	private final RendererFactory _componentJobBuilderPresenterRendererFactory;
	private final CloseableTabbedPane _tabbedPane;
	private final FilterListPanel _filterListPanel;
	private final DCLabel _statusLabel = DCLabel.bright("");
	private final CollapsibleTreePanel _leftPanel;
	private final SourceColumnsPanel _sourceColumnsPanel;
	private volatile AbstractJobBuilderPanel _latestPanel = null;
	private final SchemaTreePanel _schemaTreePanel;
	private final JButton _saveButton;
	private final JButton _visualizeButton;
	private final JButton _addTransformerButton;
	private final JButton _addAnalyzerButton;
	private final JButton _runButton;
	private final Provider<DCWindowMenuBar> _windowMenuBarProvider;
	private final Provider<RunAnalysisActionListener> _runAnalysisActionProvider;
	private final DCGlassPane _glassPane;
	private final DatastoreListPanel _datastoreListPanel;
	private String _jobFilename;
	private Datastore _datastore;
	private DataContextProvider _dataContextProvider;
	private boolean _datastoreSelectionEnabled;

	@Inject
	protected AnalysisJobBuilderWindow(AnalyzerBeansConfiguration configuration, WindowContext windowContext,
			Provider<DCRendererInitializer> rendererInitializerProvider, Provider<RunAnalysisActionListener> runAnalysisActionProvider, AnalysisJobBuilder analysisJobBuilder,
			@Nullable Datastore datastore, @Nullable @JobFilename String jobFilename,
			@Nullable @DatastoreName String datastoreName, Provider<DCWindowMenuBar> windowMenuBarProvider) {
		super(windowContext);
		_jobFilename = jobFilename;
		_configuration = configuration;
		_windowMenuBarProvider = windowMenuBarProvider;
		_runAnalysisActionProvider = runAnalysisActionProvider;

		if (analysisJobBuilder == null) {
			_analysisJobBuilder = new AnalysisJobBuilder(_configuration);
		} else {
			_analysisJobBuilder = analysisJobBuilder;
		}

		if (datastore == null) {
			if (datastoreName != null) {
				_datastore = configuration.getDatastoreCatalog().getDatastore(datastoreName);
			} else {
				DataContextProvider dcp = _analysisJobBuilder.getDataContextProvider();
				if (dcp != null) {
					_datastore = dcp.getDatastore();
				}
			}
		} else {
			_datastore = datastore;
		}
		_datastoreSelectionEnabled = true;
		_componentJobBuilderPresenterRendererFactory = new RendererFactory(_configuration.getDescriptorProvider(),
				rendererInitializerProvider.get());
		_glassPane = new DCGlassPane(this);

		_analysisJobBuilder.getAnalyzerChangeListeners().add(this);
		_analysisJobBuilder.getTransformerChangeListeners().add(this);
		_analysisJobBuilder.getFilterChangeListeners().add(this);
		_analysisJobBuilder.getSourceColumnListeners().add(this);

		_saveButton = new JButton("Save analysis job", imageManager.getImageIcon("images/actions/save.png"));
		_visualizeButton = createToolbarButton("Visualize", "images/actions/visualize.png",
				"<html><b>Visualize job</b><br/>Visualize the components of this job in a flow-chart.</html>");
		_addTransformerButton = createToolbarButton("Add transformer", IconUtils.TRANSFORMER_IMAGEPATH,
				"<html><b>Transformers</b><br/>Preprocess your data in order to extract, combine or generate separate values.</html>");
		_addAnalyzerButton = createToolbarButton("Add analyzer", IconUtils.ANALYZER_IMAGEPATH,
				"<html><b>Analyzers</b><br/>Analyzers provide Data Quality analysis and profiling operations.</html>");
		_runButton = new JButton("Run analysis", imageManager.getImageIcon("images/actions/execute.png"));

		_datastoreListPanel = new DatastoreListPanel(_configuration, this, _glassPane);
		_datastoreListPanel.setBorder(new EmptyBorder(4, 4, 0, 150));

		_sourceColumnsPanel = new SourceColumnsPanel(_analysisJobBuilder, getWindowContext());
		_filterListPanel = new FilterListPanel(_analysisJobBuilder, _componentJobBuilderPresenterRendererFactory);
		_filterListPanel.addPreconfiguredPresenter(_sourceColumnsPanel.getMaxRowsFilterShortcutPanel());

		_tabbedPane = new CloseableTabbedPane();
		_tabbedPane.addTabCloseListener(this);
		_tabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public synchronized void stateChanged(ChangeEvent e) {
				if (_latestPanel != null) {
					_latestPanel.applyPropertyValues(false);
				}
				Component selectedComponent = _tabbedPane.getSelectedComponent();
				if (selectedComponent instanceof AbstractJobBuilderPanel) {
					_latestPanel = (AbstractJobBuilderPanel) selectedComponent;
				} else {
					_latestPanel = null;
				}
				updateStatusLabel();
			}
		});
		final MetadataPanel metadataPanel = new MetadataPanel(_analysisJobBuilder);

		final DCPanel sourceTabOuterPanel = new DCPanel(imageManager.getImage("images/window/source-tab-background.png"),
				95, 95, WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
		sourceTabOuterPanel.setLayout(new VerticalLayout(0));
		sourceTabOuterPanel.add(_datastoreListPanel);
		sourceTabOuterPanel.add(_sourceColumnsPanel);

		_tabbedPane.addTab("Source", imageManager.getImageIcon("images/model/source.png"),
				WidgetUtils.scrolleable(sourceTabOuterPanel));
		_tabbedPane.setRightClickActionListener(0, new HideTabTextActionListener(_tabbedPane, 0));
		_tabbedPane.addTab("Metadata", imageManager.getImageIcon("images/model/metadata.png"), metadataPanel);
		_tabbedPane.setRightClickActionListener(1, new HideTabTextActionListener(_tabbedPane, 1));
		_tabbedPane.addTab("Filters", imageManager.getImageIcon(IconUtils.FILTER_IMAGEPATH),
				WidgetUtils.scrolleable(_filterListPanel));
		_tabbedPane.setRightClickActionListener(2, new HideTabTextActionListener(_tabbedPane, 2));

		_tabbedPane.setUnclosableTab(SOURCE_TAB);
		_tabbedPane.setUnclosableTab(METADATA_TAB);
		_tabbedPane.setUnclosableTab(FILTERS_TAB);

		_tabbedPane.addSeparator();

		_schemaTreePanel = new SchemaTreePanel(_analysisJobBuilder, getWindowContext());
		_leftPanel = new CollapsibleTreePanel(_schemaTreePanel);
		_leftPanel.setVisible(false);
		_leftPanel.setCollapsed(true);
		_schemaTreePanel.setUpdatePanel(_leftPanel);

		setDatastore(_datastore);
	}

	private JButton createToolbarButton(String text, String iconPath, String popupDescription) {
		JButton button = new JButton(text, imageManager.getImageIcon(iconPath));
		button.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
		button.setFocusPainted(false);
		if (popupDescription != null) {
			DCPopupBubble popupBubble = new DCPopupBubble(_glassPane, popupDescription, 0, 0, iconPath);
			popupBubble.attachTo(button);
		}
		return button;
	}

	/**
	 * Gets whether or not the datastore has been set in this window (ie. if the
	 * tree is showing a datastore).
	 * 
	 * @return true if a datastore is set.
	 */
	public boolean isDatastoreSet() {
		return _datastore != null;
	}

	/**
	 * Initializes the window to use a particular datastore in the schema tree.
	 * 
	 * @param datastore
	 */
	public void setDatastore(final Datastore datastore) {
		setDatastore(datastore, false);
	}

	/**
	 * Initializes the window to use a particular datastore in the schema tree.
	 * 
	 * @param datastore
	 * @param expandTree
	 *            true if the datastore tree should be initially expanded.
	 */
	public void setDatastore(final Datastore datastore, boolean expandTree) {
		final DataContextProvider dcp;
		if (datastore == null) {
			dcp = null;
		} else {
			dcp = datastore.getDataContextProvider();
		}

		_datastore = datastore;
		if (_dataContextProvider != null) {
			_dataContextProvider.close();
		}
		_dataContextProvider = dcp;
		_analysisJobBuilder.setDatastore(datastore);
		_schemaTreePanel.setDatastore(datastore, expandTree);

		if (datastore == null) {
			_analysisJobBuilder.reset();
			displayDatastoreSelection();
		} else {
			displaySourceColumnsList();
		}

		updateStatusLabel();
	}

	private void displaySourceColumnsList() {
		_leftPanel.setVisible(true);
		if (_leftPanel.isCollapsed()) {
			_leftPanel.setCollapsed(false);
		}

		_sourceColumnsPanel.setVisible(true);
		_datastoreListPanel.setVisible(false);
	}

	private void displayDatastoreSelection() {
		if (isShowing()) {
			if (_datastore == null) {
				if (!_leftPanel.isCollapsed()) {
					_leftPanel.setCollapsed(true);
				}
				Timer timer = new Timer(500, new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						_leftPanel.setVisible(false);
					}
				});
				timer.setRepeats(false);
				timer.start();

				_sourceColumnsPanel.setVisible(false);
				_datastoreListPanel.setVisible(true);

				_datastoreListPanel.requestSearchFieldFocus();
			}
		}
	}

	@Override
	protected void onWindowVisible() {
		displayDatastoreSelection();
	}

	private void updateStatusLabel() {
		boolean success = false;

		if (_datastore == null) {
			_statusLabel.setText("Welcome to DataCleaner " + Main.VERSION);
			_statusLabel.setIcon(imageManager.getImageIcon("images/window/app-icon.png", IconUtils.ICON_SIZE_SMALL));
		} else {
			try {
				if (_analysisJobBuilder.isConfigured(true)) {
					success = true;
					_statusLabel.setText("Job is correctly configured");
					_statusLabel.setIcon(imageManager.getImageIcon("images/status/valid.png", IconUtils.ICON_SIZE_SMALL));
				} else {
					_statusLabel.setText("Job is not correctly configured");
					_statusLabel.setIcon(imageManager.getImageIcon("images/status/warning.png", IconUtils.ICON_SIZE_SMALL));
				}
			} catch (Exception ex) {
				logger.debug("Job not correctly configured", ex);
				final String errorMessage;
				if (ex instanceof UnconfiguredConfiguredPropertyException) {
					ConfiguredPropertyDescriptor configuredProperty = ((UnconfiguredConfiguredPropertyException) ex)
							.getConfiguredProperty();
					AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder = ((UnconfiguredConfiguredPropertyException) ex)
							.getBeanJobBuilder();
					errorMessage = "Property '" + configuredProperty.getName() + "' in "
							+ LabelUtils.getLabel(beanJobBuilder) + " is not set!";
				} else {
					errorMessage = ex.getMessage();
				}
				_statusLabel.setText("Job error status: " + errorMessage);
				_statusLabel.setIcon(imageManager.getImageIcon("images/status/error.png", IconUtils.ICON_SIZE_SMALL));
			}
		}

		_runButton.setEnabled(success);
	}

	public String getStatusLabelText() {
		return _statusLabel.getText();
	}

	@Override
	protected boolean onWindowClosing() {
		if (!super.onWindowClosing()) {
			return false;
		}

		final int count = getWindowContext().getWindowCount(AnalysisJobBuilderWindow.class);

		final boolean windowClosing;
		final boolean exit;

		if (count == 1) {
			// if this is the last workspace window
			if (isDatastoreSet() && isDatastoreSelectionEnabled()) {
				// if datastore is set and datastore selection is enabled,
				// return to datastore selection.
				resetJob();
				exit = false;
				windowClosing = false;
			} else {
				// if datastore is not set, show exit dialog
				exit = getWindowContext().showExitDialog();
				windowClosing = exit;
			}
		} else {
			// if there are more workspace windows, simply close the window
			exit = false;
			windowClosing = true;
		}

		if (windowClosing) {
			_analysisJobBuilder.getAnalyzerChangeListeners().remove(this);
			_analysisJobBuilder.getTransformerChangeListeners().remove(this);
			_analysisJobBuilder.getFilterChangeListeners().remove(this);
			_analysisJobBuilder.getSourceColumnListeners().remove(this);
			_analysisJobBuilder.close();
			if (_dataContextProvider != null) {
				_dataContextProvider.close();
			}
			getContentPane().removeAll();
		}

		if (exit) {
			// trigger removeAll() to make sure removeNotify() methods are
			// invoked.
			getWindowContext().exit();
		}
		return windowClosing;
	}

	private void resetJob() {
		setDatastore(null);
	}

	public void setJobFilename(String jobFilename) {
		_jobFilename = jobFilename;
		updateWindowTitle();
	}

	@Override
	public String getWindowTitle() {
		String title = "Analysis job";
		if (_datastore != null) {
			String datastoreName = _datastore.getName();
			if (!StringUtils.isNullOrEmpty(datastoreName)) {
				title = datastoreName + " | " + title;
			}
		}
		if (!StringUtils.isNullOrEmpty(_jobFilename)) {
			title = _jobFilename + " | " + title;
		}
		return title;
	}

	@Override
	public Image getWindowIcon() {
		return imageManager.getImage("images/filetypes/analysis_job.png");
	}

	@Override
	protected boolean isWindowResizable() {
		return true;
	}

	@Override
	protected JComponent getWindowContent() {
		setJMenuBar(_windowMenuBarProvider.get());

		_saveButton.addActionListener(new SaveAnalysisJobActionListener(this, _analysisJobBuilder));

		_visualizeButton.setToolTipText("Visualize execution flow");
		_visualizeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				VisualizeJobWindow window = new VisualizeJobWindow(_analysisJobBuilder, getWindowContext());
				window.setVisible(true);
			}
		});

		// Add transformer
		_addTransformerButton.addActionListener(new AddTransformerActionListener(_configuration, _analysisJobBuilder));

		// Add analyzer
		_addAnalyzerButton.addActionListener(new AddAnalyzerActionListener(_configuration, _analysisJobBuilder));

		// Run analysis
		final RunAnalysisActionListener runAnalysisActionListener = _runAnalysisActionProvider.get();
		_runButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				applyPropertyValues();

				// TODO: Also support exploring analyzers

				runAnalysisActionListener.actionPerformed(e);
			}
		});

		_saveButton.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
		_saveButton.setFocusPainted(false);
		_visualizeButton.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
		_visualizeButton.setFocusPainted(false);
		_addAnalyzerButton.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
		_addAnalyzerButton.setFocusPainted(false);
		_runButton.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
		_runButton.setFocusPainted(false);

		final JToolBar toolBar = WidgetFactory.createToolBar();
		toolBar.add(_saveButton);
		toolBar.add(_visualizeButton);
		toolBar.add(WidgetFactory.createToolBarSeparator());
		toolBar.add(_addTransformerButton);
		toolBar.add(_addAnalyzerButton);
		toolBar.add(WidgetFactory.createToolBarSeparator());
		toolBar.add(_runButton);

		final JXStatusBar statusBar = WidgetFactory.createStatusBar(_statusLabel);
		final LoginStatusLabel loggedInStatusLabel = new LoginStatusLabel(_glassPane, getWindowContext());
		statusBar.add(loggedInStatusLabel);

		final DCPanel toolBarPanel = new DCPanel(WidgetUtils.BG_COLOR_LESS_DARK, WidgetUtils.BG_COLOR_DARK);
		toolBarPanel.setLayout(new BorderLayout());
		toolBarPanel.add(toolBar, BorderLayout.CENTER);

		final DCPanel panel = new DCPersistentSizedPanel(getClass().getName(), DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);
		panel.setLayout(new BorderLayout());
		panel.add(toolBarPanel, BorderLayout.NORTH);
		panel.add(_leftPanel, BorderLayout.WEST);
		panel.add(_tabbedPane, BorderLayout.CENTER);
		panel.add(statusBar, BorderLayout.SOUTH);

		WidgetUtils.centerOnScreen(this);

		initializeExistingComponents();

		return panel;
	}

	/**
	 * Applies property values for all components visible in the window.
	 */
	public void applyPropertyValues() {
		_filterListPanel.applyPropertyValues();

		for (TransformerJobBuilderPresenter presenter : _transformerPresenters.values()) {
			presenter.applyPropertyValues();
		}

		for (RowProcessingAnalyzerJobBuilderPresenter presenter : _rowProcessingTabPresenters.values()) {
			presenter.applyPropertyValues();
		}
	}

	/**
	 * Method used to initialize any components that may be in the
	 * AnalysisJobBuilder before this window has been created. Typically this
	 * will only happen when opening a saved job.
	 */
	private void initializeExistingComponents() {
		_filterListPanel.initializeExistingComponents();

		List<TransformerJobBuilder<?>> transformerJobBuilders = _analysisJobBuilder.getTransformerJobBuilders();
		for (TransformerJobBuilder<?> tjb : transformerJobBuilders) {
			onAdd(tjb);
		}

		List<MergedOutcomeJobBuilder> mergedOutcomeJobBuilders = _analysisJobBuilder.getMergedOutcomeJobBuilders();
		for (MergedOutcomeJobBuilder mojb : mergedOutcomeJobBuilders) {
			// TODO: onAdd(mojb)
			logger.warn("Job contains unsupported MergedOutcomeJobBuilders: {}", mojb);
		}

		List<AnalyzerJobBuilder<?>> analyzerJobBuilders = _analysisJobBuilder.getAnalyzerJobBuilders();
		for (AnalyzerJobBuilder<?> ajb : analyzerJobBuilders) {
			if (ajb instanceof RowProcessingAnalyzerJobBuilder<?>) {
				onAdd((RowProcessingAnalyzerJobBuilder<?>) ajb);
			} else if (ajb instanceof ExploringAnalyzerJobBuilder<?>) {
				onAdd((ExploringAnalyzerJobBuilder<?>) ajb);
			} else {
				throw new IllegalStateException("Unknown analyzer type: " + ajb);
			}
		}

		onSourceColumnsChanged();
	}

	private void onSourceColumnsChanged() {
		boolean everythingEnabled = true;

		if (_analysisJobBuilder.getSourceColumns().isEmpty()) {
			_tabbedPane.setSelectedIndex(SOURCE_TAB);
			everythingEnabled = false;
		}

		int tabCount = _tabbedPane.getTabCount();
		for (int i = 1; i < tabCount; i++) {
			_tabbedPane.setEnabledAt(i, everythingEnabled);
		}
		_saveButton.setEnabled(everythingEnabled);
		_visualizeButton.setEnabled(everythingEnabled);
		_addTransformerButton.setEnabled(everythingEnabled);
		_addAnalyzerButton.setEnabled(everythingEnabled);
	}

	public void setDatastoreSelectionEnabled(boolean datastoreSelectionEnabled) {
		_datastoreSelectionEnabled = datastoreSelectionEnabled;
	}

	public boolean isDatastoreSelectionEnabled() {
		return _datastoreSelectionEnabled;
	}

	@Override
	protected boolean isCentered() {
		return true;
	}

	@Override
	public void tabClosed(TabCloseEvent ev) {
		Component panel = ev.getTabContents();

		if (panel != null) {
			// if panel was a row processing analyzer panel
			for (Iterator<RowProcessingAnalyzerJobBuilderPresenter> it = _rowProcessingTabPresenters.values().iterator(); it
					.hasNext();) {
				RowProcessingAnalyzerJobBuilderPresenter analyzerPresenter = it.next();
				if (_jobBuilderTabs.get(analyzerPresenter) == panel) {
					_analysisJobBuilder.removeAnalyzer(analyzerPresenter.getJobBuilder());
					return;
				}
			}

			// if panel was a transformer panel
			for (Iterator<TransformerJobBuilderPresenter> it = _transformerPresenters.values().iterator(); it.hasNext();) {
				TransformerJobBuilderPresenter transformerPresenter = it.next();
				if (_jobBuilderTabs.get(transformerPresenter) == panel) {
					_analysisJobBuilder.removeTransformer(transformerPresenter.getJobBuilder());
					return;
				}
			}
			// TODO also handle exploring analyzers
		}
		logger.warn("Could not handle removal of tab {}, containing {}", ev.getTabIndex(), panel);
	}

	@Override
	public void onAdd(ExploringAnalyzerJobBuilder<?> analyzerJobBuilder) {
		_tabbedPane.addTab(LabelUtils.getLabel(analyzerJobBuilder), new JLabel("TODO: Exploring analyzer"));
		_tabbedPane.setSelectedIndex(_tabbedPane.getTabCount() - 1);
		updateStatusLabel();
	}

	@Override
	public void onAdd(RowProcessingAnalyzerJobBuilder<?> analyzerJobBuilder) {
		@SuppressWarnings("unchecked")
		final Renderer<RowProcessingAnalyzerJobBuilder<?>, ? extends ComponentJobBuilderPresenter> renderer = (Renderer<RowProcessingAnalyzerJobBuilder<?>, ? extends ComponentJobBuilderPresenter>) _componentJobBuilderPresenterRendererFactory
				.getRenderer(analyzerJobBuilder, ComponentJobBuilderRenderingFormat.class);
		RowProcessingAnalyzerJobBuilderPresenter presenter = (RowProcessingAnalyzerJobBuilderPresenter) renderer
				.render(analyzerJobBuilder);

		_rowProcessingTabPresenters.put(analyzerJobBuilder, presenter);
		JComponent comp = presenter.createJComponent();
		_tabbedPane.addTab(LabelUtils.getLabel(analyzerJobBuilder),
				IconUtils.getDescriptorIcon(analyzerJobBuilder.getDescriptor(), IconUtils.ICON_SIZE_LARGE), comp);
		_jobBuilderTabs.put(presenter, comp);
		final int tabIndex = _tabbedPane.getTabCount() - 1;
		_tabbedPane.setRightClickActionListener(tabIndex, new JobBuilderTabTextActionListener(_analysisJobBuilder,
				analyzerJobBuilder, tabIndex, _tabbedPane));
		_tabbedPane.setSelectedIndex(tabIndex);
		updateStatusLabel();
	}

	@Override
	public void onRemove(ExploringAnalyzerJobBuilder<?> analyzerJobBuilder) {
		// TODO
		updateStatusLabel();
	}

	@Override
	public void onRemove(RowProcessingAnalyzerJobBuilder<?> analyzerJobBuilder) {
		RowProcessingAnalyzerJobBuilderPresenter presenter = _rowProcessingTabPresenters.remove(analyzerJobBuilder);
		JComponent comp = _jobBuilderTabs.remove(presenter);
		_tabbedPane.remove(comp);
		updateStatusLabel();
	}

	@Override
	public void onAdd(TransformerJobBuilder<?> transformerJobBuilder) {
		@SuppressWarnings("unchecked")
		final Renderer<TransformerJobBuilder<?>, ? extends ComponentJobBuilderPresenter> renderer = (Renderer<TransformerJobBuilder<?>, ? extends ComponentJobBuilderPresenter>) _componentJobBuilderPresenterRendererFactory
				.getRenderer(transformerJobBuilder, ComponentJobBuilderRenderingFormat.class);
		final TransformerJobBuilderPresenter presenter = (TransformerJobBuilderPresenter) renderer
				.render(transformerJobBuilder);

		_transformerPresenters.put(transformerJobBuilder, presenter);
		JComponent comp = presenter.createJComponent();
		_tabbedPane.addTab(LabelUtils.getLabel(transformerJobBuilder),
				IconUtils.getDescriptorIcon(transformerJobBuilder.getDescriptor(), IconUtils.ICON_SIZE_LARGE), comp);
		_jobBuilderTabs.put(presenter, comp);
		final int tabIndex = _tabbedPane.getTabCount() - 1;
		_tabbedPane.setSelectedIndex(tabIndex);
		_tabbedPane.setRightClickActionListener(tabIndex, new JobBuilderTabTextActionListener(_analysisJobBuilder,
				transformerJobBuilder, tabIndex, _tabbedPane));
		updateStatusLabel();
	}

	@Override
	public void onRemove(TransformerJobBuilder<?> transformerJobBuilder) {
		TransformerJobBuilderPresenter presenter = _transformerPresenters.remove(transformerJobBuilder);
		JComponent comp = _jobBuilderTabs.remove(presenter);
		_tabbedPane.remove(comp);
		updateStatusLabel();
	}

	@Override
	public void onOutputChanged(TransformerJobBuilder<?> transformerJobBuilder, List<MutableInputColumn<?>> outputColumns) {
		TransformerJobBuilderPresenter presenter = _transformerPresenters.get(transformerJobBuilder);
		if (presenter != null) {
			presenter.onOutputChanged(outputColumns);
		}
	}

	@Override
	public void onAdd(final FilterJobBuilder<?, ?> filterJobBuilder) {
		FilterJobBuilder<?, ?> maxRowsFilterJobBuilder = _sourceColumnsPanel.getMaxRowsFilterShortcutPanel().getJobBuilder();
		if (filterJobBuilder == maxRowsFilterJobBuilder) {
			// draw a "think bubble" near the filter tab stating that the filter
			// was added.
			final Rectangle filterTabBounds = _tabbedPane.getTabBounds(FILTERS_TAB);
			final Point tpLocation = _tabbedPane.getLocationOnScreen();

			final int x = filterTabBounds.x + tpLocation.x + 50;
			final int y = filterTabBounds.y + tpLocation.y + filterTabBounds.height;

			final DCPopupBubble popupBubble = new DCPopupBubble(_glassPane, "<html>'<b>"
					+ LabelUtils.getLabel(filterJobBuilder) + "</b>'<br/>added to <b>filters</b></html>", x, y,
					"images/menu/filter-tab.png");
			popupBubble.showTooltip(2000);
		} else {
			_tabbedPane.setSelectedIndex(FILTERS_TAB);
		}
		updateStatusLabel();
	}

	@Override
	public void onRemove(FilterJobBuilder<?, ?> filterJobBuilder) {
		updateStatusLabel();
	}

	@Override
	public void onConfigurationChanged(FilterJobBuilder<?, ?> filterJobBuilder) {
		updateStatusLabel();
	}

	@Override
	public void onRequirementChanged(FilterJobBuilder<?, ?> filterJobBuilder) {
	}

	@Override
	public void onConfigurationChanged(TransformerJobBuilder<?> transformerJobBuilder) {
		TransformerJobBuilderPresenter presenter = _transformerPresenters.get(transformerJobBuilder);
		if (presenter != null) {
			presenter.onConfigurationChanged();
		}
		updateStatusLabel();
	}

	@Override
	public void onRequirementChanged(TransformerJobBuilder<?> transformerJobBuilder) {
		TransformerJobBuilderPresenter presenter = _transformerPresenters.get(transformerJobBuilder);
		if (presenter != null) {
			presenter.onRequirementChanged();
		}
	}

	@Override
	public void onConfigurationChanged(ExploringAnalyzerJobBuilder<?> analyzerJobBuilder) {
		updateStatusLabel();
	}

	@Override
	public void onConfigurationChanged(RowProcessingAnalyzerJobBuilder<?> analyzerJobBuilder) {
		RowProcessingAnalyzerJobBuilderPresenter presenter = _rowProcessingTabPresenters.get(analyzerJobBuilder);
		if (presenter != null) {
			presenter.onConfigurationChanged();
		}
		updateStatusLabel();
	}

	@Override
	public void onRequirementChanged(RowProcessingAnalyzerJobBuilder<?> analyzerJobBuilder) {
		RowProcessingAnalyzerJobBuilderPresenter presenter = _rowProcessingTabPresenters.get(analyzerJobBuilder);
		if (presenter != null) {
			presenter.onRequirementChanged();
		}
	}

	@Override
	public void onAdd(InputColumn<?> sourceColumn) {
		onSourceColumnsChanged();
		updateStatusLabel();
	}

	@Override
	public void onRemove(InputColumn<?> sourceColumn) {
		onSourceColumnsChanged();
		updateStatusLabel();
	}
}
