package gov.loc.repository.bagit.driver;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagHelper;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.PreBag;
import gov.loc.repository.bagit.Bag.Format;
import gov.loc.repository.bagit.BagFactory.LoadOption;
import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.Manifest.Algorithm;
import gov.loc.repository.bagit.transformer.HolePuncher;
import gov.loc.repository.bagit.transformer.impl.DefaultCompleter;
import gov.loc.repository.bagit.transformer.impl.HolePuncherImpl;
import gov.loc.repository.bagit.transformer.impl.TagManifestCompleter;
import gov.loc.repository.bagit.transformer.impl.UpdateCompleter;
import gov.loc.repository.bagit.transfer.BagFetcher;
import gov.loc.repository.bagit.transfer.BobSender;
import gov.loc.repository.bagit.transfer.FetchFailStrategy;
import gov.loc.repository.bagit.transfer.StandardFailStrategies;
import gov.loc.repository.bagit.transfer.SwordSender;
import gov.loc.repository.bagit.transfer.ThresholdFailStrategy;
import gov.loc.repository.bagit.transfer.dest.FileSystemFileDestination;
import gov.loc.repository.bagit.transfer.fetch.ExternalRsyncFetchProtocol;
import gov.loc.repository.bagit.transfer.fetch.FtpFetchProtocol;
import gov.loc.repository.bagit.transfer.fetch.HttpFetchProtocol;
import gov.loc.repository.bagit.utilities.OperatingSystemHelper;
import gov.loc.repository.bagit.utilities.SimpleResult;
import gov.loc.repository.bagit.verify.CompleteVerifier;
import gov.loc.repository.bagit.verify.ValidVerifier;
import gov.loc.repository.bagit.verify.impl.CompleteVerifierImpl;
import gov.loc.repository.bagit.verify.impl.ParallelManifestChecksumVerifier;
import gov.loc.repository.bagit.verify.impl.ValidVerifierImpl;
import gov.loc.repository.bagit.writer.Writer;
import gov.loc.repository.bagit.writer.impl.FileSystemHelper;
import gov.loc.repository.bagit.writer.impl.FileSystemWriter;
import gov.loc.repository.bagit.writer.impl.TarBz2Writer;
import gov.loc.repository.bagit.writer.impl.TarGzWriter;
import gov.loc.repository.bagit.writer.impl.TarWriter;
import gov.loc.repository.bagit.writer.impl.ZipWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Authenticator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;
import com.martiansoftware.jsap.stringparsers.EnumeratedStringParser;
import com.martiansoftware.jsap.stringparsers.FileStringParser;

public class CommandLineBagDriver {
	
	public static final int RETURN_SUCCESS = 0;
	public static final int RETURN_FAILURE = 1;
	public static final int RETURN_ERROR = 2;	
	
	public static final String OPERATION_VERIFYVALID = "verifyvalid";
	public static final String OPERATION_VERIFYCOMPLETE = "verifycomplete";
	public static final String OPERATION_MAKE_COMPLETE = "makecomplete";
	public static final String OPERATION_UPDATE = "update";
	public static final String OPERATION_UPDATE_TAGMANIFESTS = "updatetagmanifests";
	public static final String OPERATION_CREATE = "create";
	public static final String OPERATION_MAKE_HOLEY = "makeholey";
	public static final String OPERATION_GENERATE_PAYLOAD_OXUM = "generatepayloadoxum";
	public static final String OPERATION_CHECK_PAYLOAD_OXUM = "checkpayloadoxum";
	public static final String OPERATION_VERIFY_PAYLOADMANIFESTS = "verifypayloadmanifests";
	public static final String OPERATION_VERIFY_TAGMANIFESTS = "verifytagmanifests";
	public static final String OPERATION_RETRIEVE = "retrieve";
	public static final String OPERATION_FILL_HOLEY = "fillholey";	
	public static final String OPERATION_SEND_BOB = "bob";
	public static final String OPERATION_SEND_SWORD = "sword";
	public static final String OPERATION_BAG_IN_PLACE = "baginplace";
	
	public static final String PARAM_PROGRESS = "show-progress";
	public static final String PARAM_SOURCE = "source";
	public static final String PARAM_DESTINATION = "dest";
	//public static final String PARAM_BAG_DIR = "bagdir";
	public static final String PARAM_MISSING_BAGIT_TOLERANT = "missingbagittolerant";
	public static final String PARAM_ADDITIONAL_DIRECTORY_TOLERANT = "additionaldirectorytolerant";
	public static final String PARAM_MANIFEST_DELIMITER = "manifestdelimiter";
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
	public static final String PARAM_FETCH_RETRY = "on-failure";
	public static final String PARAM_FETCH_FAILURE_THRESHOLD = "max-failures";
	public static final String PARAM_FETCH_FILE_FAILURE_THRESHOLD = "max-file-failures";
	public static final String PARAM_RELAX_SSL = "relaxssl";
	public static final String PARAM_USERNAME = "username";
	public static final String PARAM_PASSWORD = "password";
	public static final String PARAM_THROTTLE = "throttle";
	public static final String PARAM_HELP = "help";
	public static final String PARAM_RETAIN_BASE_DIR = "retainbasedir";
	public static final String PARAM_BAGINFOTXT = "baginfotxt";
	public static final String PARAM_NO_RESULTFILE = "noresultfile";
	
	public static final String VALUE_WRITER_FILESYSTEM = Format.FILESYSTEM.name().toLowerCase();
	public static final String VALUE_WRITER_ZIP = Format.ZIP.name().toLowerCase();
	public static final String VALUE_WRITER_TAR = Format.TAR.name().toLowerCase();
	public static final String VALUE_WRITER_TAR_GZ = Format.TAR_GZ.name().toLowerCase();
	public static final String VALUE_WRITER_TAR_BZ2 = Format.TAR_BZ2.name().toLowerCase();
	
	private static final Log log = LogFactory.getLog(CommandLineBagDriver.class);

	private Map<String, Operation> operationMap = new HashMap<String, Operation>();
	private BagFactory bagFactory = new BagFactory();
	
	public static void main(String[] args) throws Exception {
		CommandLineBagDriver driver = new CommandLineBagDriver();		
		int ret = driver.execute(args);
		if (ret == RETURN_ERROR) {
			System.err.println(MessageFormat.format("An error occurred. Check the {0}/logs/bag-{1}.log for more details.", System.getProperty("app.home"), System.getProperty("log.timestamp")));
		}
		System.exit(ret);
	}
	
	public CommandLineBagDriver() throws Exception {
		//Initialize
		Parameter showProgressParam = new Switch(PARAM_PROGRESS, JSAP.NO_SHORTFLAG, PARAM_PROGRESS, "Reports progress of the operation to the console.");
		Parameter sourceParam = new UnflaggedOption(PARAM_SOURCE, FileStringParser.getParser().setMustExist(true), null, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The location of the source bag.");
		Parameter destParam = new UnflaggedOption(PARAM_DESTINATION, JSAP.STRING_PARSER, null, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The location of the destination bag.");
		Parameter optionalDestParam = new FlaggedOption(PARAM_DESTINATION, JSAP.STRING_PARSER, null, JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_DESTINATION, "The location of the destination bag (if different than the source bag).");
		Parameter missingBagItTolerantParam = new Switch(PARAM_MISSING_BAGIT_TOLERANT, JSAP.NO_SHORTFLAG, PARAM_MISSING_BAGIT_TOLERANT, "Tolerant of a missing bag-it.txt.");
		Parameter additionalDirectoryTolerantParam = new Switch(PARAM_ADDITIONAL_DIRECTORY_TOLERANT, JSAP.NO_SHORTFLAG, PARAM_ADDITIONAL_DIRECTORY_TOLERANT, "Tolerant of additional directories in the bag_dir.");
		Parameter manifestDelimiterParam = new FlaggedOption(PARAM_MANIFEST_DELIMITER, JSAP.STRING_PARSER, null, JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_MANIFEST_DELIMITER, "Delimiter used in Payload and Tag Manifest files.");
		Parameter writerParam = new FlaggedOption(PARAM_WRITER, EnumeratedStringParser.getParser(VALUE_WRITER_FILESYSTEM + ";" + VALUE_WRITER_ZIP + ";" + VALUE_WRITER_TAR + ";" + VALUE_WRITER_TAR_GZ + ";" + VALUE_WRITER_TAR_BZ2), VALUE_WRITER_FILESYSTEM, JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_WRITER, MessageFormat.format("The writer to use to write the bag. Valid values are {0}, {1}, {2}, {3}, and {4}.", VALUE_WRITER_FILESYSTEM, VALUE_WRITER_TAR, VALUE_WRITER_TAR_GZ, VALUE_WRITER_TAR_BZ2, VALUE_WRITER_ZIP));
		Parameter payloadParam = new UnflaggedOption(PARAM_PAYLOAD, JSAP.STRING_PARSER, null, JSAP.REQUIRED, JSAP.GREEDY, "List of files/directories to include in payload. To add the children of a directory, but not the directory itself append with " + File.separator + "*.");
		Parameter excludePayloadDirParam = new Switch(PARAM_EXCLUDE_PAYLOAD_DIR, JSAP.NO_SHORTFLAG, PARAM_EXCLUDE_PAYLOAD_DIR, "Exclude the payload directory when constructing the url.");
		Parameter baseUrlParam = new UnflaggedOption(PARAM_BASE_URL, JSAP.STRING_PARSER, null, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The base url to be prepended in creating the fetch.txt.");
		Parameter bobSwordUrlParam = new UnflaggedOption(PARAM_URL, JSAP.STRING_PARSER, null, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The url to be used in creating a resource using SWORD/BOB.");
		Parameter retrieveUrlParam = new UnflaggedOption(PARAM_URL, JSAP.STRING_PARSER, null, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The url to retrieve the bag from.");
		Parameter threadsParam = new FlaggedOption(PARAM_THREADS, JSAP.INTEGER_PARSER, null, JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_THREADS, "The number of threads to use.  Default is equal to the number of processors.");
		Parameter fetchRetryParam = new FlaggedOption(PARAM_FETCH_RETRY, EnumeratedStringParser.getParser("none;next;retry;threshold"), "threshold", JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_FETCH_RETRY, "How to handle fetch failures.  Must be one of none, next, retry, or threshold.");
		Parameter fetchFailThreshold = new FlaggedOption(PARAM_FETCH_FAILURE_THRESHOLD, JSAP.INTEGER_PARSER, "200", JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_FETCH_FAILURE_THRESHOLD, "The number of total fetch failures to tolerate before giving up.");
		Parameter fetchFileFailThreshold = new FlaggedOption(PARAM_FETCH_FILE_FAILURE_THRESHOLD, JSAP.INTEGER_PARSER, "3", JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_FETCH_FILE_FAILURE_THRESHOLD, "The number times to retry a file before giving up on that file.");
		//Parameter bagDirParam = new FlaggedOption(PARAM_BAG_DIR, JSAP.STRING_PARSER, "bag", JSAP.REQUIRED, JSAP.NO_SHORTFLAG, PARAM_BAG_DIR, "The name of the directory within the serialized bag when creating a resource using SWORD.");
		Parameter excludeBagInfoParam = new Switch(PARAM_EXCLUDE_BAG_INFO, JSAP.NO_SHORTFLAG, PARAM_EXCLUDE_BAG_INFO, "Excludes creating bag-info.txt, if necessary, when completing a bag.");
		Parameter noUpdatePayloadOxumParam = new Switch(PARAM_NO_UPDATE_PAYLOAD_OXUM, JSAP.NO_SHORTFLAG, PARAM_NO_UPDATE_PAYLOAD_OXUM, "Does not update Payload-Oxum in bag-info.txt when completing a bag.");
		Parameter noUpdateBaggingDateParam = new Switch(PARAM_NO_UPDATE_BAGGING_DATE, JSAP.NO_SHORTFLAG, PARAM_NO_UPDATE_BAGGING_DATE, "Does not update Bagging-Date in bag-info.txt when completing a bag.");
		Parameter noUpdateBagSizeParam = new Switch(PARAM_NO_UPDATE_BAG_SIZE, JSAP.NO_SHORTFLAG, PARAM_NO_UPDATE_BAG_SIZE, "Does not update Bag-Size in bag-info.txt when completing a bag.");
		Parameter excludeTagManifestParam = new Switch(PARAM_EXCLUDE_TAG_MANIFEST, JSAP.NO_SHORTFLAG, PARAM_EXCLUDE_TAG_MANIFEST, "Excludes creating a tag manifest when completing a bag.");
		Parameter tagManifestAlgorithmParam = new FlaggedOption(PARAM_TAG_MANIFEST_ALGORITHM, EnumeratedStringParser.getParser(getAlgorithmList()), Algorithm.MD5.bagItAlgorithm, JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_TAG_MANIFEST_ALGORITHM, MessageFormat.format("The algorithm used to generate the tag manifest. Valid values are {0}. Default is {1}.", getAlgorithmListString(), Algorithm.MD5.bagItAlgorithm ));
		Parameter payloadManifestAlgorithmParam = new FlaggedOption(PARAM_PAYLOAD_MANIFEST_ALGORITHM, EnumeratedStringParser.getParser(getAlgorithmList()), Algorithm.MD5.bagItAlgorithm, JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_PAYLOAD_MANIFEST_ALGORITHM, MessageFormat.format("The algorithm used to generate the payload manifest. Valid values are {0}. Default is {1}.", getAlgorithmListString(), Algorithm.MD5.bagItAlgorithm ));
		Parameter versionParam = new FlaggedOption(PARAM_VERSION, EnumeratedStringParser.getParser(getVersionList(), false, false), null, JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_VERSION, MessageFormat.format("The version used to check the bag. Valid values are {0}. Default is to discover from the bag-it.txt or latest version.", getVersionListString()));
		Parameter relaxSSLParam = new Switch(PARAM_RELAX_SSL, JSAP.NO_SHORTFLAG, PARAM_RELAX_SSL, "Tolerant of self-signed SSL certificates.");
		Parameter usernameParam = new FlaggedOption(PARAM_USERNAME, JSAP.STRING_PARSER, null, JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_USERNAME, "The username for basic authentication.");
		Parameter passwordParam = new FlaggedOption(PARAM_PASSWORD, JSAP.STRING_PARSER, null, JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_PASSWORD, "The password for basic authentication.");
		Parameter throttleParam = new FlaggedOption(PARAM_THROTTLE, JSAP.INTEGER_PARSER, null, JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_THROTTLE, "A pause between HTTP posts in milliseconds for BOB.");
		Parameter retainBaseDirParam = new Switch(PARAM_RETAIN_BASE_DIR, JSAP.NO_SHORTFLAG, PARAM_RETAIN_BASE_DIR, "Indicates that the base directory (not just the contents of the base directory) should be placed in the data directory of the bag.");
		Parameter bagInfoTxtParam = new FlaggedOption(PARAM_BAGINFOTXT, FileStringParser.getParser().setMustExist(true), null, JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_BAGINFOTXT, "An external bag-info.txt file to include in the bag.");
		Parameter noResultFileParam = new Switch(PARAM_NO_RESULTFILE, JSAP.NO_SHORTFLAG, PARAM_NO_RESULTFILE, "Suppress creating a result file.");
		
		this.addOperation(OPERATION_VERIFY_TAGMANIFESTS,
				"Verifies the checksums in all tag manifests.",
				new Parameter[] {sourceParam, versionParam, noResultFileParam},
				new String[] {MessageFormat.format("bag {0} {1}", OPERATION_VERIFY_TAGMANIFESTS, this.getBag("mybag"))});

		this.addOperation(OPERATION_VERIFY_PAYLOADMANIFESTS,
				"Verifies the checksums in all payload manifests.",
				new Parameter[] {sourceParam, versionParam, noResultFileParam},
				new String[] {MessageFormat.format("bag {0} {1}", OPERATION_VERIFY_PAYLOADMANIFESTS, this.getBag("mybag"))});

		this.addOperation(OPERATION_VERIFYVALID,
				"Verifies the validity of a bag.",
				new Parameter[] {sourceParam, versionParam, missingBagItTolerantParam, additionalDirectoryTolerantParam, noResultFileParam},
				new String[] {MessageFormat.format("bag {0} {1}", OPERATION_VERIFYVALID, this.getBag("mybag"))});

		this.addOperation(OPERATION_VERIFYCOMPLETE,
				"Verifies the completeness of a bag.",
				new Parameter[] {sourceParam, versionParam, missingBagItTolerantParam, additionalDirectoryTolerantParam, noResultFileParam},
				new String[] {MessageFormat.format("bag {0} {1}", OPERATION_VERIFYCOMPLETE, this.getBag("mybag"))});
		
		List<Parameter> completeParams = new ArrayList<Parameter>();
		completeParams.add(excludeBagInfoParam);
		completeParams.add(noUpdateBaggingDateParam);
		completeParams.add(noUpdateBagSizeParam);
		completeParams.add(noUpdatePayloadOxumParam);
		completeParams.add(excludeTagManifestParam);
		completeParams.add(tagManifestAlgorithmParam);
		completeParams.add(payloadManifestAlgorithmParam);
		completeParams.add(versionParam);
		completeParams.add(manifestDelimiterParam);
		
		List<Parameter> makeCompleteParams = new ArrayList<Parameter>();
		makeCompleteParams.add(sourceParam);
		makeCompleteParams.add(destParam);
		makeCompleteParams.add(writerParam);
		makeCompleteParams.addAll(completeParams);
		
		this.addOperation(OPERATION_MAKE_COMPLETE,
				"Completes a bag and then writes in a specified format.  Completing a bag fills in any missing parts.",
				makeCompleteParams,
				new String[] {MessageFormat.format("bag {0} {1} {2}", OPERATION_MAKE_COMPLETE, this.getBag("mybag"), this.getBag("myDestBag"))});

		this.addOperation(OPERATION_UPDATE,
				"Updates the manifests and (if it exists) the bag-info.txt for a bag.",
				new Parameter[] {sourceParam, optionalDestParam, writerParam, manifestDelimiterParam},
				new String[] {MessageFormat.format("bag {0} {1} ", OPERATION_UPDATE, this.getBag("mybag"))});

		
		this.addOperation(OPERATION_UPDATE_TAGMANIFESTS,
				"Updates the tag manifests for a bag.  The bag must be unserialized.",
				new Parameter[] {sourceParam, tagManifestAlgorithmParam, manifestDelimiterParam},
				new String[] {MessageFormat.format("bag {0} {1}", OPERATION_UPDATE_TAGMANIFESTS, this.getBag("mybag"))});
				
		List<Parameter> bagInPlaceParams = new ArrayList<Parameter>();
		bagInPlaceParams.add(sourceParam);
		bagInPlaceParams.add(retainBaseDirParam);
		bagInPlaceParams.addAll(completeParams);
		bagInPlaceParams.add(bagInfoTxtParam);
		this.addOperation(OPERATION_BAG_IN_PLACE,
				"Creates a bag-in-place.  The source must be a directory on a filesystem and may already have a data directory.",
				bagInPlaceParams,
				new String[] {MessageFormat.format("bag {0} {1}", OPERATION_BAG_IN_PLACE, this.getBag("mybag"))});
		
		List<Parameter> createParams = new ArrayList<Parameter>();
		createParams.add(destParam);
		createParams.add(payloadParam);
		createParams.add(writerParam);
		createParams.addAll(completeParams);
		createParams.add(bagInfoTxtParam);

		this.addOperation(OPERATION_CREATE,
				"Creates a bag from supplied files/directories, completes the bag, and then writes in a specified format.",
				createParams, 
				new String[] {MessageFormat.format("bag {0} {1} {2} {3}", OPERATION_CREATE, this.getBag("mybag"), this.getData("somedata"), this.getData("otherdata/afile.txt"))});
		
		List<Parameter> makeHoleyParam = new ArrayList<Parameter>();
		makeHoleyParam.add(sourceParam);
		makeHoleyParam.add(destParam);
		makeHoleyParam.add(baseUrlParam);
		makeHoleyParam.add(writerParam);
		makeHoleyParam.add(excludePayloadDirParam);		
		//TODO
//		makeHoleyParam.add(manifestDelimiterParam);
		
		this.addOperation(OPERATION_MAKE_HOLEY, 
				"Generates a fetch.txt and then writes bag in a specified format.", 
				makeHoleyParam, 
				new String[] {MessageFormat.format("bag {0} {1} {2} http://www.loc.gov/bags", OPERATION_MAKE_HOLEY, this.getBag("mybag"), this.getBag("myDestBag"))});

		this.addOperation(OPERATION_GENERATE_PAYLOAD_OXUM,
				"Generates and returns the Payload-Oxum for the bag.",
				new Parameter[] {sourceParam},
				new String[] {MessageFormat.format("bag {0} {1}", OPERATION_GENERATE_PAYLOAD_OXUM, this.getBag("mybag"))});
		
		this.addOperation(OPERATION_CHECK_PAYLOAD_OXUM, 
				"Generates Payload-Oxum and checks against Payload-Oxum in bag-info.txt.", 
				new Parameter[] {sourceParam},
				new String[] {MessageFormat.format("bag {0} {1}", OPERATION_CHECK_PAYLOAD_OXUM, this.getBag("mybag"))});

		this.addOperation(OPERATION_RETRIEVE, 
				"Retrieves a bag exposed by a web server. A local holey bag is not required.", 
				new Parameter[] {destParam, retrieveUrlParam, showProgressParam, threadsParam, fetchRetryParam, fetchFileFailThreshold, fetchFailThreshold, usernameParam, passwordParam},
				new String[] {MessageFormat.format("bag {0} {1} http://www.loc.gov/bags/mybag", OPERATION_RETRIEVE, this.getBag("myDestBag"))});
		
		this.addOperation(OPERATION_FILL_HOLEY, 
				"Retrieves any missing pieces of a local bag specified in the fetch.txt.", 
				new Parameter[] {sourceParam, showProgressParam, relaxSSLParam, threadsParam, fetchRetryParam, fetchFileFailThreshold, fetchFailThreshold, usernameParam, passwordParam},
				new String[] {MessageFormat.format("bag {0} {1}", OPERATION_RETRIEVE, this.getBag("mybag"))});
		
		List<Parameter> senderParams = new ArrayList<Parameter>();
		senderParams.add(sourceParam);
		senderParams.add(bobSwordUrlParam);
		senderParams.add(relaxSSLParam);
		senderParams.add(usernameParam);
		senderParams.add(passwordParam);
		this.addOperation(OPERATION_SEND_SWORD, 
				"Sends a bag using SWORD.",
				senderParams,
				new String[] {MessageFormat.format("bag {0} {1} http://locdrop.loc.gov/sword", OPERATION_SEND_SWORD, this.getBag("mybag"))});

		List<Parameter> bobParams = new ArrayList<Parameter>();
		bobParams.addAll(senderParams);
		bobParams.add(throttleParam);
		bobParams.add(threadsParam);
		this.addOperation(OPERATION_SEND_BOB, 
				"Sends a bag using BOB.", 
				bobParams, 
				new String[] {MessageFormat.format("bag {0} {1} http://locdrop.loc.gov/bob", OPERATION_SEND_BOB, this.getBag("mybag"))});
	}

	private String getBag(String bagName) {
		if (OperatingSystemHelper.isWindows()) {
			return "c:\\bags\\" + bagName;
		}
		return "/bags/" + bagName;
	}

	private String getData(String filepath) {
		if (OperatingSystemHelper.isWindows()) {
			return "c:\\data\\" + filepath;
		}
		return "/data/" + filepath;
		
	}
	
	private void addOperation(String name, String help, Parameter[] params, String[] examples) throws Exception {
		JSAP jsap = new JSAP();
		//jsap.setHelp(help);
		for(Parameter param : params) {
			jsap.registerParameter(param);
		}
		jsap.registerParameter(new Switch( PARAM_HELP, JSAP.NO_SHORTFLAG, PARAM_HELP, "Prints help." ));
		this.operationMap.put(name, new Operation(name, jsap, help, examples));
	}
	
	private void addOperation(String name, String help, List<Parameter> params, String[] examples) throws Exception {
		this.addOperation(name, help, params.toArray(new Parameter[] {}), examples);
	}

	
	public int execute(String[] args) throws Exception {		
		log.debug("Executing with arguments: " + argsToString(args));
		log.info("Executing with arguments: " + argsToString(args));
		
		int ret = RETURN_SUCCESS;
		
		if (args.length == 0 || (args.length == 1 && args[0].equals("--" + PARAM_HELP))) {
			printUsage();
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
					newArgs = new String[] {"--" + PARAM_HELP};
				}
				
				JSAPResult config = operation.jsap.parse(newArgs);
				if (config.getBoolean(PARAM_HELP, false)) {
					printOperationUsage(config, operation);
				} else if (! config.success()) {
					printOperationUsage(config, operation);
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
		
	@SuppressWarnings("unchecked")
	private void printOperationUsage(JSAPResult config, Operation operation) {
		if (! config.getBoolean(PARAM_HELP, false)) {
			Iterator<String> errIter = config.getErrorMessageIterator();
			while(errIter.hasNext()) {
				System.err.println("Error: " + errIter.next());
			}
		}
		System.out.println(MessageFormat.format("Usage: bag {0} {1}", operation.name, operation.jsap.getUsage()));
        System.out.println("Operation explanation:");
        System.out.println("\t" + operation.help);
        System.out.println("Operation parameters:");
		System.out.println(operation.jsap.getHelp());
		if (operation.examples.length > 0) {
			System.out.println("Examples:");
			for(String example : operation.examples) {
				System.out.println("\t" + example);
			}
		}
		

	}
	
	private void printUsage() {
		System.out.println("Usage: bag <operation> [operation arguments] [--help]");
        System.out.println("Parameters:");
		System.out.println("\t<operation>");
		System.out.print("\t\tValid operations are: ");
		List<String> names = new ArrayList<String>(this.operationMap.keySet());
		Collections.sort(names);
		for(int i=0; i<names.size(); i++) {
			System.out.print(names.get(i));
			if (i == names.size()-2) {
				System.out.print(" and ");
			} else if (i == names.size()-1) {
				System.out.println(".");
			} else {
				System.out.print(", ");
			}			
		}
		System.out.println("\t\tOperation explanations: ");
		for(String name : names) {
			System.out.println(MessageFormat.format("\t\t\t{0}: {1}", name, this.operationMap.get(name).help));
		}
		
		System.out.println("\t[--help]");
		System.out.println("\t\tPrints usage message for the operation.");
		System.out.println("Examples:");
		System.out.println("\tbag verifyvalid --help");
		System.out.println("\t\tPrints help for the verifyvalid operation.");
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

	private Bag getBag(File sourceFile, Version version, LoadOption loadOption) {
		if (version != null) {
			if (sourceFile != null) {
				return bagFactory.createBag(sourceFile, version, loadOption);
			} else {
				return bagFactory.createBag(version);
			}
		} else {
			if (sourceFile != null) {
				return bagFactory.createBag(sourceFile, loadOption);
			} else {
				return bagFactory.createBag();
			}
		}
	}
	
	private int performOperation(Operation operation, JSAPResult config) {
		String msg = "Performing operation: " + operation.name;
		log.info(msg);
		
		try {
			File sourceFile = null;
			if (config.contains(PARAM_SOURCE)) {
				sourceFile = config.getFile(PARAM_SOURCE);					
			}
			Version version = null;			
			if (config.contains(PARAM_VERSION)) {
				version = Version.valueOfString(config.getString(PARAM_VERSION));
			}

			File destFile = null;
			if (config.contains(PARAM_DESTINATION)) {				
				destFile = new File(config.getString(PARAM_DESTINATION));
			}

			Writer writer = null;
			if (config.contains(PARAM_WRITER)) {
				Format format = Format.valueOf(config.getString(PARAM_WRITER).toUpperCase());
				if (Format.FILESYSTEM.equals(format)) {
					writer = new FileSystemWriter(bagFactory);
				} else if (Format.TAR.equals(format)) {
					writer = new TarWriter(bagFactory);
				} else if (Format.TAR_BZ2.equals(format)) {
					writer = new TarBz2Writer(bagFactory);
				} else if (Format.TAR_GZ.equals(format)) {
					writer = new TarGzWriter(bagFactory);
				} else if (Format.ZIP.equals(format)) {
					writer = new ZipWriter(bagFactory);
				}
			}

			String  delimiter = null;
			String delimiter2 = null;
			int ret = RETURN_SUCCESS;

			if(config.contains(PARAM_MANIFEST_DELIMITER)){
				delimiter = config.getString(PARAM_MANIFEST_DELIMITER);
				delimiter2 = delimiter.replace("\\t", "\t");
				if (delimiter2.trim().length() > 0){
					System.err.println("Delimiter parameter contains invalid characters.");
					log.error("Delimiter parameter contains invalid characters.");
					ret = RETURN_ERROR;
				}
			}
			
						
			DefaultCompleter completer = new DefaultCompleter(bagFactory);
			completer.setGenerateBagInfoTxt(! config.getBoolean(PARAM_EXCLUDE_BAG_INFO, false));
			completer.setUpdateBaggingDate(! config.getBoolean(PARAM_NO_UPDATE_BAGGING_DATE, false));
			completer.setUpdateBagSize(! config.getBoolean(PARAM_NO_UPDATE_BAG_SIZE, false));
			completer.setUpdatePayloadOxum(! config.getBoolean(PARAM_NO_UPDATE_PAYLOAD_OXUM, false));
			completer.setGenerateTagManifest(! config.getBoolean(PARAM_EXCLUDE_TAG_MANIFEST, false));
			completer.setTagManifestAlgorithm(Algorithm.valueOfBagItAlgorithm(config.getString(PARAM_TAG_MANIFEST_ALGORITHM, Algorithm.MD5.bagItAlgorithm)));
			completer.setPayloadManifestAlgorithm(Algorithm.valueOfBagItAlgorithm(config.getString(PARAM_PAYLOAD_MANIFEST_ALGORITHM, Algorithm.MD5.bagItAlgorithm)));

		    BagFetcher fetcher = new BagFetcher(bagFactory);

		    boolean writeResultFile = ! config.getBoolean(PARAM_NO_RESULTFILE, false);
		    
		    //These settings applies only for operations FillHoley bag and Retrieve a remote bag
			if(operation.name.equals(OPERATION_FILL_HOLEY) || operation.name.equals(OPERATION_RETRIEVE) ){

				// The authenticator configuration must be first, since the
				// HttpFetchProtocol constructor calls for authentication.
			    String username = config.getString(PARAM_USERNAME);
			    String password = config.getString(PARAM_PASSWORD);

			    if (username != null && password != null)
			    {
			    	Authenticator.setDefault(new ConstantCredentialsAuthenticator(username, password));
			    }
			    else if (username != null && password == null)
			    {
				    Authenticator.setDefault(new ConsoleAuthenticator(username));
			    }
			    else
			    {
			    	Authenticator.setDefault(new NoCredentialsAuthenticator());
			    }
					    
			    // TODO Make this dynamically register somehow.
				HttpFetchProtocol http = new HttpFetchProtocol();
				http.setRelaxedSsl(config.getBoolean(PARAM_RELAX_SSL, false));				
			    fetcher.registerProtocol("http", http);
			    fetcher.registerProtocol("https", http);
			    fetcher.registerProtocol("ftp", new FtpFetchProtocol());
			    fetcher.registerProtocol("rsync", new ExternalRsyncFetchProtocol());
					    
				int threads = config.getInt(PARAM_THREADS, 0);
				if (threads != 0) {
					fetcher.setNumberOfThreads(threads);
				}
				
				// Should always have a default value, as the parser is told
				// to give it one.
				String fetchRetryString = config.getString(PARAM_FETCH_RETRY);
				FetchFailStrategy failStrategy;
								
				if (fetchRetryString.equalsIgnoreCase("none"))
					failStrategy = StandardFailStrategies.FAIL_FAST;
				else if (fetchRetryString.equalsIgnoreCase("next"))
					failStrategy = StandardFailStrategies.ALWAYS_CONTINUE;
				else if (fetchRetryString.equalsIgnoreCase("retry"))
					failStrategy = StandardFailStrategies.ALWAYS_RETRY;
				else // threshold
				{
					int fileFailThreshold = config.getInt(PARAM_FETCH_FILE_FAILURE_THRESHOLD);
					int failThreshold = config.getInt(PARAM_FETCH_FAILURE_THRESHOLD);
						
					failStrategy = new ThresholdFailStrategy(fileFailThreshold, failThreshold);
				}
							
				fetcher.setFetchFailStrategy(failStrategy);
								
				if (config.getBoolean(PARAM_PROGRESS, false))
				{
					fetcher.addProgressListener(new ConsoleProgressListener());
				}
			}
			
//			int ret = RETURN_SUCCESS;
			
			if (OPERATION_VERIFYVALID.equals(operation.name)) {				
				CompleteVerifier completeVerifier = new CompleteVerifierImpl();
				completeVerifier.setMissingBagItTolerant(config.getBoolean(PARAM_MISSING_BAGIT_TOLERANT, false));
				completeVerifier.setAdditionalDirectoriesInBagDirTolerant(config.getBoolean(PARAM_ADDITIONAL_DIRECTORY_TOLERANT, false));
				ValidVerifier verifier = new ValidVerifierImpl(completeVerifier, new ParallelManifestChecksumVerifier());
				Bag bag = this.getBag(sourceFile, version, LoadOption.BY_PAYLOAD_MANIFESTS);
				SimpleResult result = verifier.verify(bag);
				log.info(result.toString());
				System.out.println(result.toString(SimpleResult.DEFAULT_MAX_MESSAGES, "\n"));
				if (! result.isSuccess()) {
					if (writeResultFile) this.writeResultFile(operation.name, result);
					ret = RETURN_FAILURE;
				}
			} else if (OPERATION_VERIFYCOMPLETE.equals(operation.name)) {				
				CompleteVerifier completeVerifier = new CompleteVerifierImpl();
				completeVerifier.setMissingBagItTolerant(config.getBoolean(PARAM_MISSING_BAGIT_TOLERANT, false));				
				completeVerifier.setAdditionalDirectoriesInBagDirTolerant(config.getBoolean(PARAM_ADDITIONAL_DIRECTORY_TOLERANT, false));
				Bag bag = this.getBag(sourceFile, version, LoadOption.BY_PAYLOAD_MANIFESTS);
				SimpleResult result = completeVerifier.verify(bag);
				log.info(result.toString());
				System.out.println(result.toString(SimpleResult.DEFAULT_MAX_MESSAGES, "\n"));
				if (! result.isSuccess()) {
					if (writeResultFile) this.writeResultFile(operation.name, result);
					ret = RETURN_FAILURE;
				}
			} else if (OPERATION_VERIFY_TAGMANIFESTS.equals(operation.name)) {				
				Bag bag = this.getBag(sourceFile, version, LoadOption.BY_PAYLOAD_MANIFESTS);
				SimpleResult result = bag.verifyTagManifests();
				log.info(result.toString());
				System.out.println(result.toString(SimpleResult.DEFAULT_MAX_MESSAGES, "\n"));
				if (! result.isSuccess()) {
					if (writeResultFile) this.writeResultFile(operation.name, result);
					ret = RETURN_FAILURE;
				}
			} else if (OPERATION_VERIFY_PAYLOADMANIFESTS.equals(operation.name)) {				
				Bag bag = this.getBag(sourceFile, version, LoadOption.BY_PAYLOAD_MANIFESTS);
				SimpleResult result = bag.verifyPayloadManifests();
				log.info(result.toString());
				System.out.println(result.toString(SimpleResult.DEFAULT_MAX_MESSAGES, "\n"));
				if (! result.isSuccess()) {
					if (writeResultFile) this.writeResultFile(operation.name, result);
					ret = RETURN_FAILURE;
				}
			} else if (OPERATION_MAKE_COMPLETE.equals(operation.name)) {
				Bag bag = this.getBag(sourceFile, version, LoadOption.BY_PAYLOAD_FILES);
				bag.getBagPartFactory().setManifestSeparator(delimiter2);
				Bag newBag = completer.complete(bag);
				newBag.write(writer, destFile);				
			}  else if (OPERATION_UPDATE.equals(operation.name)) {
				Bag bag = this.getBag(sourceFile, version, LoadOption.BY_PAYLOAD_FILES);
				UpdateCompleter updateCompleter = new UpdateCompleter(bagFactory);
				bag.getBagPartFactory().setManifestSeparator(delimiter2);
				Bag newBag = updateCompleter.complete(bag);
				newBag.write(writer, destFile != null?destFile:sourceFile);
			}
			else if (OPERATION_UPDATE_TAGMANIFESTS.equals(operation.name)) {
				Bag bag = this.getBag(sourceFile, version, LoadOption.BY_PAYLOAD_FILES);
				TagManifestCompleter tagManifestCompleter = new TagManifestCompleter(bagFactory);
				tagManifestCompleter.setTagManifestAlgorithm(Algorithm.valueOfBagItAlgorithm(config.getString(PARAM_TAG_MANIFEST_ALGORITHM, Algorithm.MD5.bagItAlgorithm)));
				bag.getBagPartFactory().setManifestSeparator(delimiter2);
				Bag newBag = tagManifestCompleter.complete(bag);
				for(Manifest manifest : newBag.getTagManifests()) {
					FileSystemHelper.write(manifest, new File(sourceFile, manifest.getFilepath()));
				}
			} else if (OPERATION_BAG_IN_PLACE.equals(operation.name)) {
				PreBag preBag = this.bagFactory.createPreBag(sourceFile);
				if (config.contains(PARAM_BAGINFOTXT)) {
					File bagInfoTxtFile = config.getFile(PARAM_BAGINFOTXT);
					if (! bagInfoTxtFile.getName().equals(bagFactory.getBagConstants().getBagInfoTxt())) {
						msg = MessageFormat.format("External bag-info.txt must be named {0}.", bagFactory.getBagConstants().getBagInfoTxt());
						System.err.println(msg);
						log.error(msg);
						ret = RETURN_ERROR;
					}
					List<File> tagFiles = new ArrayList<File>();
					tagFiles.add(bagInfoTxtFile);
					preBag.setTagFiles(tagFiles);
				}				
				preBag.makeBagInPlace(version != null ? version : BagFactory.LATEST, config.getBoolean(PARAM_RETAIN_BASE_DIR, false), completer);
			} else if (OPERATION_CREATE.equals(operation.name)) {
				Bag bag = this.getBag(sourceFile, version, null);
				for(String filepath : config.getStringArray(PARAM_PAYLOAD)) {
					if (filepath.endsWith(File.separator + "*")) {
						File parentDir = new File(filepath.substring(0, filepath.length()-2));
						if (! parentDir.exists()) {
							throw new RuntimeException(MessageFormat.format("{0} does not exist.", parentDir));
						}
						if (! parentDir.isDirectory()) {
							throw new RuntimeException(MessageFormat.format("{0} is not a directory.", parentDir));
						}
						for(File childFile : parentDir.listFiles()) {
							bag.addFileToPayload(childFile);
						}
					} else {						
						bag.addFileToPayload(new File(filepath));
					}
				}
				if (config.contains(PARAM_BAGINFOTXT)) {
					File bagInfoTxtFile = config.getFile(PARAM_BAGINFOTXT);
					if (! bagInfoTxtFile.getName().equals(bag.getBagConstants().getBagInfoTxt())) {
						msg = MessageFormat.format("External bag-info.txt must be named {0}.", bag.getBagConstants().getBagInfoTxt());
						System.err.println(msg);
						log.error(msg);
						ret = RETURN_ERROR;
					}
					bag.addFileAsTag(bagInfoTxtFile);
				}
				bag.getBagPartFactory().setManifestSeparator(delimiter2);
				Bag newBag = completer.complete(bag);
				newBag.write(writer, destFile);

			} else if (OPERATION_MAKE_HOLEY.equals(operation.name)) {
				HolePuncher puncher = new HolePuncherImpl(bagFactory);
				Bag bag = this.getBag(sourceFile, version, LoadOption.BY_PAYLOAD_MANIFESTS);
				bag.getBagPartFactory().setManifestSeparator(delimiter2);
				Bag newBag = puncher.makeHoley(bag, config.getString(PARAM_BASE_URL), ! config.getBoolean(PARAM_EXCLUDE_PAYLOAD_DIR, false), false);
				newBag.write(writer, destFile);
			} else if (OPERATION_GENERATE_PAYLOAD_OXUM.equals(operation.name)) {
				Bag bag = this.getBag(sourceFile, version, LoadOption.BY_PAYLOAD_MANIFESTS);
				String oxum = BagHelper.generatePayloadOxum(bag);				
				log.info("Payload-Oxum is " + oxum);
				System.out.println("Payload-Oxum is " + oxum);
			} else if (OPERATION_CHECK_PAYLOAD_OXUM.equals(operation.name)) {
				Bag bag = this.getBag(sourceFile, version, LoadOption.BY_PAYLOAD_MANIFESTS);
				String genOxum = BagHelper.generatePayloadOxum(bag);
				BagInfoTxt bagInfo = bag.getBagInfoTxt();
				if (bagInfo == null) {
					System.err.println("Bag does not contain bag-info.txt.");
					log.error("Bag does not contain bag-info.txt.");
					ret = RETURN_ERROR;
				} else {
					String checkOxum = bagInfo.getPayloadOxum();
					if (checkOxum == null) {
						System.err.println("bag-info.txt does not contain Payload-Oxum.");
						log.error("bag-info.txt does not contain Payload-Oxum.");
						ret = RETURN_ERROR;
					} else {
						if (checkOxum.equals(genOxum)) {
							System.out.println("Payload-Oxum matches.");
							log.info("Payload-Oxum matches.");
						} else {
							System.out.println("Payload-Oxum does not match.");
							log.info("Payload-Oxum does not match.");
							ret = RETURN_FAILURE;
						}
					}
				}
			} else if (OPERATION_FILL_HOLEY.equals(operation.name)) {
			    FileSystemFileDestination dest = new FileSystemFileDestination(sourceFile);
				Bag bag = this.getBag(sourceFile, version, null);			    
			    SimpleResult result = fetcher.fetch(bag, dest);
			    ret = result.isSuccess()?RETURN_SUCCESS:RETURN_FAILURE;
			} else if (OPERATION_RETRIEVE.equals(operation.name)) {
			    SimpleResult result = fetcher.fetchRemoteBag(destFile, config.getString(PARAM_URL));
			    ret = result.isSuccess()?RETURN_SUCCESS:RETURN_FAILURE;

			} else if (OPERATION_SEND_BOB.equals(operation.name)) {
				BobSender sender = new BobSender();
				String username = config.getString(PARAM_USERNAME);
				if (username != null) {
					sender.setUsername(username);
				}
				String password = config.getString(PARAM_PASSWORD);
				if (password != null) {
					sender.setPassword(password);
				}
				sender.setRelaxedSSL(config.getBoolean(PARAM_RELAX_SSL, false));
				int throttle = config.getInt(PARAM_THROTTLE, 0);
				if (throttle != 0) {
					sender.setThrottle(throttle);
				}
				int threads = config.getInt(PARAM_THREADS, 0);
				if (threads != 0) {
					sender.setThreads(threads);
				}
				Bag bag = this.getBag(sourceFile, version, LoadOption.BY_PAYLOAD_MANIFESTS);
				sender.send(bag, config.getString(PARAM_URL));
			} else if (OPERATION_SEND_SWORD.equals(operation.name)) {
				SwordSender sender = new SwordSender(new ZipWriter(bagFactory));
				String username = config.getString(PARAM_USERNAME);
				if (username != null) {
					sender.setUsername(username);
				}
				String password = config.getString(PARAM_PASSWORD);
				if (password != null) {
					sender.setPassword(password);
				}
				sender.setRelaxedSSL(config.getBoolean(PARAM_RELAX_SSL, false));
				Bag bag = this.getBag(sourceFile, version, LoadOption.BY_PAYLOAD_MANIFESTS);
				sender.send(bag, config.getString(PARAM_URL));				
			}
			log.info("Operation completed.");
			return ret;
		}
		catch(Exception ex) {
			log.error("Error: " + ex.getMessage(), ex);
			return RETURN_ERROR;
		}
	}
	
	private void writeResultFile(String operation, SimpleResult result) {
		if (result.isSuccess()) return;
		File file = new File(MessageFormat.format("{0}-{1}.txt", operation, System.getProperty("log.timestamp")));
		FileWriter writer = null;
		try {
			writer = new FileWriter(file);
			for(String msg : result.getMessages()) {
				writer.write(msg + "\n");
			}
			System.out.println("Complete results written to " + file.getCanonicalPath());
		} catch (IOException e) {
			log.error("Unable to write results", e);
		} finally {
			IOUtils.closeQuietly(writer);
		}
		
	}
	
	private class Operation {
		public String help;
		public JSAP jsap;
		public String name;
		public String[] examples;
		
		public Operation(String name, JSAP jsap, String help, String[] examples) {
			this.name = name;
			this.help = help;
			this.jsap = jsap;
			this.examples = examples;
		}
	}

}
