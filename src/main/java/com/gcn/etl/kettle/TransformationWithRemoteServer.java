package com.gcn.etl.kettle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pentaho.amazon.s3.S3FileOutputMeta;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.logging.LogChannel;
//import org.pentaho.di.core.plugins.PluginFolder;
//import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.cluster.TransSplitter;
import org.pentaho.di.trans.steps.s3csvinput.S3CsvInputMeta;
import org.springframework.stereotype.Service;

import com.gcn.etl.database.models.DataInputFile;
import com.gcn.etl.database.models.Jobs;
import com.gcn.etl.propertiesHelper.AppConfigProperties;

@Service
public class TransformationWithRemoteServer {

	private static Logger logger = LogManager.getLogger(TransformationWithRemoteServer.class.getName());

	public void executeTransformation(String ktrFilePath, DataInputFile dif, String output_file_location, Jobs jobObj,
			AppConfigProperties appConfigProperties) {
		try {

			logger.debug("Start transformation...");
			logger.info("ktrFilePath = " + ktrFilePath);
			// Load custom plugins for S3 input and output.
			// StepPluginType.getInstance().getPluginFolders().add(new
			// PluginFolder("", false, true));

			PluginLoader.loadPlugin(new S3CsvInputMeta(), "S3CSVINPUT", "Input", "S3 CSV Input", "S3I.svg");
			PluginLoader.loadPlugin(new S3FileOutputMeta(), "S3FileOutputPlugin", "Output", "S3 File Output",
					"S3O.svg");

			KettleEnvironment.init();

			TransMeta transmeta = new TransMeta(ktrFilePath);
			// Create master node config
			SlaveServer masterServer = new SlaveServer(appConfigProperties.getCarteMasterName(),
					appConfigProperties.getCarteMasterHost(), appConfigProperties.getCarteMasterPort(),
					appConfigProperties.getCarteMasterLogin(), appConfigProperties.getCarteMasterPassword());
			masterServer.setMaster(true);

			transmeta.addOrReplaceSlaveServer(masterServer);
			List<SlaveServer> slaveServers = new ArrayList<SlaveServer>();
			slaveServers.add(masterServer);
			ClusterSchema cs = new ClusterSchema("cluster_schema", slaveServers);
			cs.setDynamic(true);
			cs.setBasePort("40000");
			cs.setSocketsBufferSize("2000");
			cs.setSocketsFlushInterval("5000");
			cs.setSocketsCompressed(true);
			cs.setSlaveServers(slaveServers);
			transmeta.addOrReplaceClusterSchema(cs);
			TransExecutionConfiguration jec = new TransExecutionConfiguration();

			// Set parameters
			Map<String, String> params = new HashMap<String, String>();
			params.put("db.host", appConfigProperties.getDbHost());
			params.put("db.port", appConfigProperties.getDbPort());
			params.put("db.schema", appConfigProperties.getDbSchema());
			params.put("db.login", appConfigProperties.getDbLogin());
			params.put("db.password", appConfigProperties.getDbPassword());
			params.put("s3.bucket", appConfigProperties.getS3Bucket());
			params.put("s3.accesskey", appConfigProperties.getS3AccessKeyID());
			params.put("s3.secretkey", appConfigProperties.getS3SecretAccessKey());
			String input_file_location = dif.getLocation() + dif.getFileName();
			params.put("input_file_location", input_file_location);
			String outputFilePath = "s3://s3/" + appConfigProperties.getS3Bucket() + "/" + output_file_location;
			logger.info("input_file_location = " + input_file_location);
			logger.info("outputFilePath = " + outputFilePath);
			params.put("output_file_location", outputFilePath);
			params.put("job_id", jobObj.getJobId());
			params.put("units", dif.getUnits());
			params.put("isIntervalStart", dif.getIsIntervalStart().toString());
			params.put("timeStampFormat", dif.getTimestampFormat());
			// Add parameters to transformation config
			jec.setVariables(params);

			// Set log output
			jec.setSetLogfile(true);
			jec.setLogFileName("config/logs/out.log");
			jec.setSetAppendLogfile(true);
			jec.setLogLevel(org.pentaho.di.core.logging.LogLevel.DEBUG);

			// Config cluster execution
			jec.setExecutingClustered(true);
			jec.setExecutingLocally(false);
			jec.setExecutingRemotely(true);
			jec.setClusterPosting(true);
			jec.setClusterPreparing(true);
			jec.setClusterStarting(true);

			// System.out.println("XML: " + transmeta.getXML(true, true, true,
			// true, true));

			// Execute in remote cluster
			TransSplitter result = Trans.executeClustered(transmeta, jec);

			// Get transformation IDs
			logger.info("ClusteredRunId: " + result.getClusteredRunId());
			for (Entry<TransMeta, String> carteObject : result.getCarteObjectMap().entrySet()) {
				logger.info("carteObject Id: " + carteObject.getKey().getContainerObjectId());
				logger.info("carteObject value: " + carteObject.getValue());
			}

			// Track the status of the transformation using logs.
			LogChannel logChannel = new LogChannel("Cluster transformation status");
			logChannel.setLogLevel(org.pentaho.di.core.logging.LogLevel.DEBUG);
			long nrErrors = Trans.monitorClusteredTransformation(logChannel, result, null, 1);

			// Get number of transformation errors. Can be used to update the
			// status of the transformation.
			logger.info("nrErrors: " + nrErrors);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}