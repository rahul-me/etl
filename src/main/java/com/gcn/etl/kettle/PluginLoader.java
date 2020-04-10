package com.gcn.etl.kettle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pentaho.di.core.plugins.Plugin;
import org.pentaho.di.core.plugins.PluginFolder;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginTypeInterface;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

public class PluginLoader {
	
	private static Logger logger = LogManager.getLogger(PluginLoader.class.getName());

	public static void loadPlugin(BaseStepMeta pluginClass, String pluginId, String pluginCategory, String pluginName,
			String pluginImageName) {

		try {
			
			StepPluginType.getInstance().getPluginFolders().add(new PluginFolder("/WEB-INF/lib/", false, true));
			StepPluginType step = StepPluginType.getInstance();
			step.getPluginFolders().add(new PluginFolder("/WEB-INF/lib/", false, true));
			
			Class<? extends PluginTypeInterface> pluginTypeClass = StepPluginType.class;
			
			PluginRegistry registry = PluginRegistry.getInstance();
			PluginInterface plugin = registry.getPlugin(pluginTypeClass, pluginClass);
			
			Map<Class<?>, String> classMap = new HashMap<Class<?>, String>();
			PluginInterface pluginInterface = plugin;
			if (plugin == null) {
				logger.info(pluginClass.getClass().getSimpleName() + " plugin not found. Registering...");
				
				// Register a single step plugin
				classMap.put(StepMetaInterface.class, pluginClass.getClass().getName());
				pluginInterface = new Plugin(new String[] { pluginId }, pluginTypeClass, StepMetaInterface.class,
						pluginCategory, pluginName, null, pluginImageName, false, true, classMap,
						new ArrayList<String>(), null, // No error help file
						null, // pluginFolder
						null, // documentation URL
						null, // cases URL
						null // forum URL
				);
				registry.registerPlugin(pluginTypeClass, pluginInterface);				
				
			} else {
				logger.info(pluginClass.getClass().getSimpleName() + " plugin already registered...");
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}
}
