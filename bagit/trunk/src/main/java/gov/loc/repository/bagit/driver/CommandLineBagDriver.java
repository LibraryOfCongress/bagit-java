package gov.loc.repository.bagit.driver;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagHelper;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.BagWriter;
import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.Manifest.Algorithm;
import gov.loc.repository.bagit.bagwriter.BobUnserializedBagWriter;
import gov.loc.repository.bagit.bagwriter.FileSystemBagWriter;
import gov.loc.repository.bagit.bagwriter.SwordSerializedBagWriter;
import gov.loc.repository.bagit.bagwriter.TarBagWriter;
import gov.loc.repository.bagit.bagwriter.ZipBagWriter;
import gov.loc.repository.bagit.bagwriter.TarBagWriter.Compression;
import gov.loc.repository.bagit.completion.DefaultCompletionStrategy;
import gov.loc.repository.bagit.utilities.SimpleResult;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;
import com.martiansoftware.jsap.stringparsers.EnumeratedStringParser;
import com.martiansoftware.jsap.stringparsers.FileStringParser;

public class CommandLineBagDriver {
	
	public static final int RETURN_SUCCESS = 0;
	public static final int RETURN_FAILURE = 1;
	public static final int RETURN_ERROR = 2;	
	
	public static final String OPERATION_ISVALID = "isvalid";
	public static final String OPERATION_ISCOMPLETE = "iscomplete";
	public static final String OPERATION_WRITE = "write";
	public static final String OPERATION_MAKE_COMPLETE = "makecomplete";
	public static final String OPERATION_CREATE = "create";
	public static final String OPERATION_MAKE_HOLEY = "makeholey";
	public static final String OPERATION_GENERATE_PAYLOAD_OXUM = "generatepayloadoxum";
	public static final String OPERATION_CHECK_PAYLOAD_OXUM = "checkpayloadoxum";
	public static final String OPERATION_VERIFY_PAYLOADMANIFESTS = "verifypayloadmanifests";
	public static final String OPERATION_VERIFY_TAGMANIFESTS = "verifytagmanifests";
	
	public static final String PARAM_SOURCE = "source";
	public static final String PARAM_DESTINATION = "dest";
	public static final String PARAM_BAG_DIR = "bagdir";
	public static final String PARAM_MISSING_BAGIT_TOLERANT = "missingbagittolerant";
	public static final String PARAM_WRITER = "writer";
	public static final String PARAM_PAYLOAD = "payload";
	public static final String PARAM_EXCLUDE_PAYLOAD_DIR = "excludepayloaddir";
	public static final String PARAM_BASE_URL = "baseurl";
	public static final String PARAM_URL = "url";
	public static final String PARAM_EXCLUDE_BAG_INFO = "excludebaginfo";
	public static final String PARAM_NO_UPDATE_PAYLOAD_OXUM = "noupdatepayloadoxum";
	public static final String PARAM_NO_UPDATE_BAGGING_DATE = "noupdatebaggingdate";
	public static final String PARAM_NO_UPDATE_BAG_SIZE = "noupdatebagsize";
	public static final String PARAM_EXCLUDE_TAG_MANIFEST = "excludetagmanifest";
	public static final String PARAM_TAG_MANIFEST_ALGORITHM = "tagmanifestalgorithm";
	public static final String PARAM_PAYLOAD_MANIFEST_ALGORITHM = "payloadmanifestalgorithm";
	public static final String PARAM_VERSION = "version";
	public static final String PARAM_THREADS = "threads";
	public static final String PARAM_RELAX_SSL = "relaxssl";
	public static final String PARAM_USERNAME = "username";
	public static final String PARAM_PASSWORD = "password";
	
	public static final String VALUE_WRITER_FILESYSTEM = "filesystem";
	public static final String VALUE_WRITER_ZIP = "zip";
	public static final String VALUE_WRITER_TAR = "tar";
	public static final String VALUE_WRITER_TAR_GZ = "tar_gz";
	public static final String VALUE_WRITER_TAR_BZ2 = "tar_bz2";
	public static final String VALUE_WRITER_SWORD = "sword";
	public static final String VALUE_WRITER_BOB = "bob";
	
	private static final Log log = LogFactory.getLog(CommandLineBagDriver.class);

	private Map<String, Operation> operationMap = new HashMap<String, Operation>();
	
	public static void main(String[] args) throws Exception {
		CommandLineBagDriver driver = new CommandLineBagDriver();		
		int ret = driver.execute(args);
		System.exit(ret);		
	}
	
	public CommandLineBagDriver() throws Exception {
		//Initialize
		Parameter sourceParam = new UnflaggedOption(PARAM_SOURCE, FileStringParser.getParser().setMustExist(true), null, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The location of the source bag.");
		Parameter destParam = new FlaggedOption(PARAM_DESTINATION, JSAP.STRING_PARSER, null, JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_DESTINATION, "The location of the destination bag when writing with the filesystem, tar, or zip bag writer.");
		Parameter missingBagItTolerantParam = new Switch(PARAM_MISSING_BAGIT_TOLERANT, JSAP.NO_SHORTFLAG, PARAM_MISSING_BAGIT_TOLERANT, "Tolerant of a missing bag-it.txt.");
		Parameter writerParam = new FlaggedOption(PARAM_WRITER, EnumeratedStringParser.getParser(VALUE_WRITER_FILESYSTEM + ";" + VALUE_WRITER_ZIP + ";" + VALUE_WRITER_TAR + ";" + VALUE_WRITER_SWORD + ";" + VALUE_WRITER_BOB + ";" + VALUE_WRITER_TAR_GZ + ";" + VALUE_WRITER_TAR_BZ2), VALUE_WRITER_FILESYSTEM, JSAP.REQUIRED, JSAP.NO_SHORTFLAG, PARAM_WRITER, MessageFormat.format("The writer to use to write the bag. Valid values are {0}, {1}, {2}, {3}, {4}, {5} and {6}.", VALUE_WRITER_FILESYSTEM, VALUE_WRITER_TAR, VALUE_WRITER_TAR_GZ, VALUE_WRITER_TAR_BZ2, VALUE_WRITER_ZIP, VALUE_WRITER_SWORD, VALUE_WRITER_BOB));
		Parameter payloadParam = new UnflaggedOption(PARAM_PAYLOAD, FileStringParser.getParser().setMustExist(true), null, JSAP.REQUIRED, JSAP.GREEDY, "List of files/directories to include in payload.");
		Parameter excludePayloadDirParam = new Switch(PARAM_EXCLUDE_PAYLOAD_DIR, JSAP.NO_SHORTFLAG, PARAM_EXCLUDE_PAYLOAD_DIR, "Exclude the payload directory when constructing the url.");
		Parameter baseUrlParam = new UnflaggedOption(PARAM_BASE_URL, JSAP.STRING_PARSER, null, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The base url to be prepended in creating the fetch.txt.");
		Parameter urlParam = new FlaggedOption(PARAM_URL, JSAP.STRING_PARSER, null, JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_URL, "The url to be used in creating a resource using SWORD/BOB.");
		Parameter threadsParam = new FlaggedOption(PARAM_THREADS, JSAP.INTEGER_PARSER, null, JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_THREADS, "The number of threads to use when posting resources with BOB.  Default is " + BobUnserializedBagWriter.DEFAULT_THREADS);
		Parameter bagDirParam = new FlaggedOption(PARAM_BAG_DIR, JSAP.STRING_PARSER, "bag", JSAP.REQUIRED, JSAP.NO_SHORTFLAG, PARAM_BAG_DIR, "The name of the directory within the serialized bag when creating a resource using SWORD.");
		Parameter excludeBagInfoParam = new Switch(PARAM_EXCLUDE_BAG_INFO, JSAP.NO_SHORTFLAG, PARAM_EXCLUDE_BAG_INFO, "Excludes creating bag-info.txt, if necessary, when completing a bag.");
		Parameter noUpdatePayloadOxumParam = new Switch(PARAM_NO_UPDATE_PAYLOAD_OXUM, JSAP.NO_SHORTFLAG, PARAM_NO_UPDATE_PAYLOAD_OXUM, "Does not update Payload-Oxum in bag-info.txt when completing a bag.");
		Parameter noUpdateBaggingDateParam = new Switch(PARAM_NO_UPDATE_BAGGING_DATE, JSAP.NO_SHORTFLAG, PARAM_NO_UPDATE_BAGGING_DATE, "Does not update Bagging-Date in bag-info.txt when completing a bag.");
		Parameter noUpdateBagSizeParam = new Switch(PARAM_NO_UPDATE_BAG_SIZE, JSAP.NO_SHORTFLAG, PARAM_NO_UPDATE_BAG_SIZE, "Does not update Bag-Size in bag-info.txt when completing a bag.");
		Parameter excludeTagManifestParam = new Switch(PARAM_EXCLUDE_TAG_MANIFEST, JSAP.NO_SHORTFLAG, PARAM_EXCLUDE_TAG_MANIFEST, "Excludes creating a tag manifest when completing a bag.");
		Parameter tagManifestAlgorithmParam = new FlaggedOption(PARAM_TAG_MANIFEST_ALGORITHM, EnumeratedStringParser.getParser(getAlgorithmList()), Algorithm.MD5.bagItAlgorithm, JSAP.REQUIRED, JSAP.NO_SHORTFLAG, PARAM_TAG_MANIFEST_ALGORITHM, MessageFormat.format("The algorithm used to generate the tag manifest. Valid values are {0}. Default is {1}.", getAlgorithmListString(), Algorithm.MD5.bagItAlgorithm ));
		Parameter payloadManifestAlgorithmParam = new FlaggedOption(PARAM_PAYLOAD_MANIFEST_ALGORITHM, EnumeratedStringParser.getParser(getAlgorithmList()), Algorithm.MD5.bagItAlgorithm, JSAP.REQUIRED, JSAP.NO_SHORTFLAG, PARAM_PAYLOAD_MANIFEST_ALGORITHM, MessageFormat.format("The algorithm used to generate the payload manifest. Valid values are {0}. Default is {1}.", getAlgorithmListString(), Algorithm.MD5.bagItAlgorithm ));
		Parameter versionParam = new FlaggedOption(PARAM_VERSION, EnumeratedStringParser.getParser(getVersionList(), false, false), null, JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_VERSION, MessageFormat.format("The version used to check the bag. Valid values are {0}. Default is to discover from the bag-it.txt or latest version.", getVersionListString()));
		Parameter relaxSSLParam = new Switch(PARAM_RELAX_SSL, JSAP.NO_SHORTFLAG, "relaxssl", "Tolerant of self-signed SSL certificates.");
		Parameter usernameParam = new FlaggedOption(PARAM_USERNAME, JSAP.STRING_PARSER, null, JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_USERNAME, "The username for basic authentication.");
		Parameter passwordParam = new FlaggedOption(PARAM_PASSWORD, JSAP.STRING_PARSER, null, JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_PASSWORD, "The password for basic authentication.");
		
		List<Parameter> params = new ArrayList<Parameter>();		
		params.add(sourceParam);
		params.add(versionParam);
		this.addOperation(OPERATION_VERIFY_TAGMANIFESTS, "Verifies the checksums in all tag manifests.", params.toArray(new Parameter[] {}));
		this.addOperation(OPERATION_VERIFY_PAYLOADMANIFESTS, "Verifies the checksums in all payload manifests.", params.toArray(new Parameter[] {}));
		params.add(missingBagItTolerantParam);
		this.addOperation(OPERATION_ISVALID, "Checks validity of a bag.", params.toArray(new Parameter[] {}));
		this.addOperation(OPERATION_ISCOMPLETE, "Checks completeness of a bag.", params.toArray(new Parameter[] {}));
		
		List<Parameter> writerParams = new ArrayList<Parameter>();
		writerParams.add(writerParam);
		writerParams.add(urlParam);
		writerParams.add(bagDirParam);
		writerParams.add(threadsParam);
		writerParams.add(relaxSSLParam);
		writerParams.add(usernameParam);
		writerParams.add(passwordParam);
		
		params.clear();
		params.add(sourceParam);
		params.add(destParam);
		params.addAll(writerParams);
		this.addOperation(OPERATION_WRITE, "Writes a bag in a specified format.", params.toArray(new Parameter[] {}));

		List<Parameter> completeParams = new ArrayList<Parameter>();
		completeParams.add(excludeBagInfoParam);
		completeParams.add(noUpdateBaggingDateParam);
		completeParams.add(noUpdateBagSizeParam);
		completeParams.add(noUpdatePayloadOxumParam);
		completeParams.add(excludeTagManifestParam);
		completeParams.add(tagManifestAlgorithmParam);
		completeParams.add(payloadManifestAlgorithmParam);
		completeParams.add(versionParam);
		
		params.clear();
		params.add(sourceParam);
		params.add(destParam);
		params.addAll(writerParams);
		params.addAll(completeParams);
		this.addOperation(OPERATION_MAKE_COMPLETE, "Completes a bag and then writes in a specified format.", params.toArray(new Parameter[] {}));

		params.clear();
		params.add(destParam);
		params.addAll(writerParams);
		params.addAll(completeParams);
		params.add(payloadParam);
		
		this.addOperation(OPERATION_CREATE, "Creates a bag from supplied files/directories, completes the bag, and then writes in a specified format.", params.toArray(new Parameter[] {}));

		params.clear();
		params.add(sourceParam);
		params.add(baseUrlParam);
		params.add(destParam);
		params.addAll(writerParams);
		params.add(excludePayloadDirParam);
		this.addOperation(OPERATION_MAKE_HOLEY, "Add holes to a bag and then writes in a specified format.", params.toArray(new Parameter[] {}));

		params.clear();
		params.add(sourceParam);		
		this.addOperation(OPERATION_GENERATE_PAYLOAD_OXUM, "Generates Payload-Oxum for the bag.", params.toArray(new Parameter[] {}));
		this.addOperation(OPERATION_CHECK_PAYLOAD_OXUM, "Generates Payload-Oxum and checks against Payload-Oxum in bag-info.txt.", params.toArray(new Parameter[] {}));
		
	}

	private void addOperation(String name, String help, Parameter[] params) throws Exception {
		this.operationMap.put(name, new Operation(name, new SimpleJSAP(name, help, params), help));
	}

	
	public int execute(String[] args) throws Exception {		
		log.debug("Executing with arguments: " + argsToString(args));
		
		int ret = RETURN_SUCCESS;
		
		if (args.length == 0) {
			System.err.println("Error: An operation is required.");
			printUsage();
			ret = RETURN_ERROR;
		} else	{	
			String operationArg = args[0];		
			if (! this.operationMap.containsKey(operationArg)) {
				System.err.println("Error: Unknown operation.");
				printUsage();
				ret = RETURN_ERROR;
			} else {
				Operation operation = this.operationMap.get(operationArg);
		
				String[] newArgs = new String[args.length -1];
				if (newArgs.length > 0) {
					System.arraycopy(args, 1, newArgs, 0, args.length-1);
				} else {
					newArgs = new String[] {"--help"};
				}
				
				JSAPResult config = operation.jsap.parse(newArgs);
				if (operation.jsap.messagePrinted()) {
					ret = RETURN_ERROR;
				} else {
					ret = this.performOperation(operation, config);
				}
			}
		}
		log.info("Returning " + ret);
		return ret;
	}
	
	private static String getAlgorithmList() {
		String list = "";
		for(int i=0; i < Algorithm.values().length; i++) {
			list += Algorithm.values()[i].bagItAlgorithm;
			if (i != Algorithm.values().length -1) {
				list += ";";
			}
		}
		return list;
	}
	
	private static String getAlgorithmListString() {
		String list = "";
		for(int i=0; i < Algorithm.values().length; i++) {
			list += Algorithm.values()[i].bagItAlgorithm;

			if (i != Algorithm.values().length -1) {
				list += " and ";
			} else if (i != Algorithm.values().length -1) {
				list += ", ";
			}
		}
		return list;
		
	}

	private static String getVersionList() {
		String list = "";
		for(int i=0; i < Version.values().length; i++) {
			list += Version.values()[i].versionString;
			if (i != Version.values().length -1) {
				list += ";";
			}
		}
		return list;
	}
	
	private static String getVersionListString() {
		String list = "";
		for(int i=0; i < Version.values().length; i++) {
			list += Version.values()[i].versionString;

			if (i != Version.values().length -1) {
				list += " and ";
			} else if (i != Version.values().length -1) {
				list += ", ";
			}
		}
		return list;
		
	}
		
	private void printUsage() {
		System.out.println("Usage: bag <operation> [--help]");
		System.out.println("\t<operation>");
		System.out.print("\t\tValid operations are: ");
		Operation[] operationArray = this.operationMap.values().toArray(new Operation[] {});
		for(int i=0; i<operationArray.length; i++) {
			System.out.print(operationArray[i].name);
			if (i == operationArray.length-2) {
				System.out.print(" and ");
			} else if (i == operationArray.length-1) {
				System.out.println(".");
			} else {
				System.out.print(", ");
			}			
		}
		System.out.println("\t\tOperation explanations: ");
		for(int i=0; i<operationArray.length; i++) {
			System.out.println(MessageFormat.format("\t\t\t{0}: {1}", operationArray[i].name, operationArray[i].help));
		}
		
		System.out.println("\t[--help]");
		System.out.println("\t\tPrints usage message for the operation.");
	}
	
	private static String argsToString(String[] args) {
		String string = "";
		for(int i=0; i < args.length; i++) {
			if (i > 0) {
				string += " ";
			}
			string += args[i];
		}
		return string;
	}

	private int performOperation(Operation operation, JSAPResult config) {
		log.info("Performing operation: " + operation.name);

		try {
			File sourceFile = null;
			Bag bag = null;
			if (config.contains(PARAM_VERSION)) {
				Version version = Version.valueOfString(config.getString(PARAM_VERSION));
				if (config.contains(PARAM_SOURCE)) {
					sourceFile = config.getFile(PARAM_SOURCE);
					bag = BagFactory.createBag(sourceFile, version);
				} else {
					bag = BagFactory.createBag(version);
				}
			} else {
				if (config.contains(PARAM_SOURCE)) {
					sourceFile = config.getFile(PARAM_SOURCE);
					bag = BagFactory.createBag(sourceFile);
				} else {
					bag = BagFactory.createBag();
				}
			}
			File destFile = null;
			if (config.contains(PARAM_DESTINATION)) {				
				destFile = new File(config.getString(PARAM_DESTINATION));
			}
			String collectionURL = config.getString(PARAM_URL);
			String username = config.getString(PARAM_USERNAME);
			String password = config.getString(PARAM_PASSWORD);
			boolean relaxSSL = config.getBoolean(PARAM_RELAX_SSL, false);
						
			BagWriter writer = null;
			if (VALUE_WRITER_FILESYSTEM.equals(config.getString(PARAM_WRITER))) {
				if (destFile == null) {
					log.error("Error: If writing to a filesystem bag writer, a destination must be provided.");
					return RETURN_ERROR;
				}
				writer = new FileSystemBagWriter(destFile, true);
			} else if (VALUE_WRITER_ZIP.equals(config.getString(PARAM_WRITER))) {
				if (destFile == null) {
					log.error("Error: If writing to a zip bag writer, a destination must be provided.");
					return RETURN_ERROR;
				}
				writer = new ZipBagWriter(destFile);
			} else if (VALUE_WRITER_TAR.equals(config.getString(PARAM_WRITER))) {
				if (destFile == null) {
					log.error("Error: If writing to a tar bag writer, a destination must be provided.");
					return RETURN_ERROR;
				}
				writer = new TarBagWriter(destFile);
			} else if (VALUE_WRITER_TAR_GZ.equals(config.getString(PARAM_WRITER))) {
				if (destFile == null) {
					log.error("Error: If writing to a tar_gz bag writer, a destination must be provided.");
					return RETURN_ERROR;
				}
				writer = new TarBagWriter(destFile, Compression.GZ);
			} else if (VALUE_WRITER_TAR_BZ2.equals(config.getString(PARAM_WRITER))) {
				if (destFile == null) {
					log.error("Error: If writing to a tar_bz2 bag writer, a destination must be provided.");
					return RETURN_ERROR;
				}
				writer = new TarBagWriter(destFile, Compression.BZ2);
			} else if (VALUE_WRITER_SWORD.equals(config.getString(PARAM_WRITER))) {				
				if (collectionURL == null) {
					log.error("Error: If writing to a SWORD serialized bag writer, a collection url must be provided.");
					return RETURN_ERROR;					
				}
				writer = new SwordSerializedBagWriter(config.getString(PARAM_BAG_DIR), collectionURL, relaxSSL, username, password);
			} else if (VALUE_WRITER_BOB.equals(config.getString(PARAM_WRITER))) {				
				if (collectionURL == null) {
					log.error("Error: If writing to a BOB unserialized bag writer, a collection url must be provided.");
					return RETURN_ERROR;					
				}
				writer = new BobUnserializedBagWriter(collectionURL, relaxSSL, username, password);
				((BobUnserializedBagWriter)writer).setThreads(config.getInt(PARAM_THREADS, BobUnserializedBagWriter.DEFAULT_THREADS));
			}
			DefaultCompletionStrategy strategy = new DefaultCompletionStrategy();
			strategy.setGenerateBagInfoTxt(! config.getBoolean(PARAM_EXCLUDE_BAG_INFO, false));
			strategy.setUpdateBaggingDate(! config.getBoolean(PARAM_NO_UPDATE_BAGGING_DATE, false));
			strategy.setUpdateBagSize(! config.getBoolean(PARAM_NO_UPDATE_BAG_SIZE, false));
			strategy.setUpdatePayloadOxum(! config.getBoolean(PARAM_NO_UPDATE_PAYLOAD_OXUM, false));
			strategy.setGenerateTagManifest(! config.getBoolean(PARAM_EXCLUDE_TAG_MANIFEST, false));
			strategy.setTagManifestAlgorithm(Algorithm.valueOfBagItAlgorithm(config.getString(PARAM_TAG_MANIFEST_ALGORITHM, Algorithm.MD5.bagItAlgorithm)));
			strategy.setPayloadManifestAlgorithm(Algorithm.valueOfBagItAlgorithm(config.getString(PARAM_PAYLOAD_MANIFEST_ALGORITHM, Algorithm.MD5.bagItAlgorithm)));
			
			int ret = RETURN_SUCCESS;
			
			if (OPERATION_ISVALID.equals(operation.name)) {				
				SimpleResult result = bag.isValid(config.getBoolean(PARAM_MISSING_BAGIT_TOLERANT, false));
				log.info(result.toString());
				if (! result.isSuccess()) {
					ret = RETURN_FAILURE;
				}
				return RETURN_SUCCESS;
			} else if (OPERATION_ISCOMPLETE.equals(operation.name)) {				
				SimpleResult result = bag.isComplete(config.getBoolean(PARAM_MISSING_BAGIT_TOLERANT, false));
				log.info(result.toString());
				if (! result.isSuccess()) {
					ret = RETURN_FAILURE;
				}
			} else if (OPERATION_VERIFY_TAGMANIFESTS.equals(operation.name)) {				
				SimpleResult result = bag.verifyTagManifests();
				log.info(result.toString());
				if (! result.isSuccess()) {
					ret = RETURN_FAILURE;
				}
			} else if (OPERATION_VERIFY_PAYLOADMANIFESTS.equals(operation.name)) {				
				SimpleResult result = bag.verifyTagManifests();
				log.info(result.toString());
				if (! result.isSuccess()) {
					ret = RETURN_FAILURE;
				}
			} else if (OPERATION_WRITE.equals(operation.name)) {								
				bag.write(writer);
			} else if (OPERATION_MAKE_COMPLETE.equals(operation.name)) {
				bag.makeComplete(strategy);
				bag.write(writer);
			} else if (OPERATION_CREATE.equals(operation.name)) {
				for(File file : config.getFileArray(PARAM_PAYLOAD)) {
					bag.addPayload(file);
				}
				bag.makeComplete(strategy);
				bag.write(writer);				

			} else if (OPERATION_MAKE_HOLEY.equals(operation.name)) {
				bag.makeHoley(config.getString(PARAM_BASE_URL), config.getBoolean(PARAM_EXCLUDE_PAYLOAD_DIR, false));
				bag.write(writer);
			} else if (OPERATION_GENERATE_PAYLOAD_OXUM.equals(operation.name)) {
				String oxum = BagHelper.generatePayloadOxum(bag);				
				log.info("Payload-Oxum: " + oxum);
			} else if (OPERATION_CHECK_PAYLOAD_OXUM.equals(operation.name)) {
				String genOxum = BagHelper.generatePayloadOxum(bag);
				BagInfoTxt bagInfo = bag.getBagInfoTxt();
				if (bagInfo == null) {
					log.error("Bag does not contain bag-info.txt.");
					ret = RETURN_ERROR;
				} else {
					String checkOxum = bagInfo.getPayloadOxum();
					if (checkOxum == null) {
						log.error("bag-info.txt does not contain Payload-Oxum.");
						ret = RETURN_ERROR;
					} else {
						if (checkOxum.equals(genOxum)) {
							log.info("Payload-Oxum matches.");
						} else {
							log.info("Payload-Oxum does not match.");
							ret = RETURN_FAILURE;
						}
					}
				}
			}
			log.info("Operation completed.");
			return ret;
		}
		catch(Exception ex) {
			log.error("Error: " + ex.getMessage(), ex);
			return RETURN_ERROR;
		}
	}
	
	private class Operation {
		public String help;
		public SimpleJSAP jsap;
		public String name;
		
		public Operation(String name, SimpleJSAP jsap, String help) {
			this.name = name;
			this.help = help;
			this.jsap = jsap;
		}
	}

}
