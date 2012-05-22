package gov.loc.repository.bagit.driver;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagHelper;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.Manifest;
import gov.loc.repository.bagit.PreBag;
import gov.loc.repository.bagit.ProgressListenable;
import gov.loc.repository.bagit.Bag.Format;
import gov.loc.repository.bagit.BagFactory.LoadOption;
import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.Manifest.Algorithm;
import gov.loc.repository.bagit.progresslistener.CompositeProgressListener;
import gov.loc.repository.bagit.progresslistener.ConsoleProgressListener;
import gov.loc.repository.bagit.progresslistener.LoggingProgressListener;
import gov.loc.repository.bagit.transformer.Completer;
import gov.loc.repository.bagit.transformer.Splitter;
import gov.loc.repository.bagit.transformer.impl.DefaultCompleter;
import gov.loc.repository.bagit.transformer.impl.HolePuncherImpl;
import gov.loc.repository.bagit.transformer.impl.SplitByFileType;
import gov.loc.repository.bagit.transformer.impl.SplitBySize;
import gov.loc.repository.bagit.transformer.impl.TagManifestCompleter;
import gov.loc.repository.bagit.transformer.impl.UpdateCompleter;
import gov.loc.repository.bagit.transformer.impl.UpdatePayloadOxumCompleter;
import gov.loc.repository.bagit.transfer.BagFetcher;
import gov.loc.repository.bagit.transfer.FetchFailStrategy;
import gov.loc.repository.bagit.transfer.StandardFailStrategies;
import gov.loc.repository.bagit.transfer.ThresholdFailStrategy;
import gov.loc.repository.bagit.transfer.dest.FileSystemFileDestination;
import gov.loc.repository.bagit.transfer.fetch.ExternalRsyncFetchProtocol;
import gov.loc.repository.bagit.transfer.fetch.FtpFetchProtocol;
import gov.loc.repository.bagit.transfer.fetch.HttpFetchProtocol;
import gov.loc.repository.bagit.utilities.OperatingSystemHelper;
import gov.loc.repository.bagit.utilities.SimpleResult;
import gov.loc.repository.bagit.utilities.SizeHelper;
import gov.loc.repository.bagit.verify.FailModeSupporting.FailMode;
import gov.loc.repository.bagit.verify.impl.CompleteVerifierImpl;
import gov.loc.repository.bagit.verify.impl.ParallelManifestChecksumVerifier;
import gov.loc.repository.bagit.verify.impl.ValidVerifierImpl;
import gov.loc.repository.bagit.writer.Writer;
import gov.loc.repository.bagit.writer.impl.FileSystemHelper;
import gov.loc.repository.bagit.writer.impl.FileSystemWriter;
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
	public static final String OPERATION_UPDATE_PAYLOAD_OXUM = "updatepayloadoxum";
	public static final String OPERATION_CHECK_PAYLOAD_OXUM = "checkpayloadoxum";
	public static final String OPERATION_VERIFY_PAYLOADMANIFESTS = "verifypayloadmanifests";
	public static final String OPERATION_VERIFY_TAGMANIFESTS = "verifytagmanifests";
	public static final String OPERATION_RETRIEVE = "retrieve";
	public static final String OPERATION_FILL_HOLEY = "fillholey";	
	public static final String OPERATION_BAG_IN_PLACE = "baginplace";
	public static final String OPERATION_SPLIT_BAG_BY_SIZE = "splitbagbysize";
	public static final String OPERATION_SPLIT_BAG_BY_FILE_TYPE = "splitbagbyfiletype";
	public static final String OPERATION_SPLIT_BAG_BY_SIZE_AND_FILE_TYPE = "splitbagbysizeandfiletype";
	
	public static final String PARAM_SOURCE = "source";
	public static final String PARAM_DESTINATION = "dest";
	//public static final String PARAM_BAG_DIR = "bagdir";
	public static final String PARAM_MISSING_BAGIT_TOLERANT = "missingbagittolerant";
	public static final String PARAM_ADDITIONAL_DIRECTORY_TOLERANT = "additionaldirectorytolerant";
	public static final String PARAM_MANIFEST_SEPARATOR = "manifestseparator";
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
	public static final String PARAM_RESUME = "resume";
	public static final String PARAM_MAX_BAG_SIZE = "maxbagsize";
	public static final String PARAM_KEEP_LOWEST_LEVEL_DIR = "keeplowestleveldir";
	public static final String PARAM_FILE_EXTENSIONS = "fileextensions";
	public static final String PARAM_EXCLUDE_DIRS = "excludedirs";
	public static final String PARAM_KEEP_SOURCE_BAG = "keepsourcebag";
	public static final String PARAM_KEEP_EMPTY_DIRS = "keepemptydirs";
	public static final String PARAM_VERBOSE = "verbose";
	public static final String PARAM_LOG_VERBOSE = "log-verbose";
	public static final String PARAM_EXCLUDE_SYMLINKS = "excludesymlinks";
	public static final String PARAM_FAIL_MODE = "failmode";
	public static final String PARAM_COMPRESSION_LEVEL = "compressionlevel";
	
	public static final String VALUE_WRITER_FILESYSTEM = Format.FILESYSTEM.name().toLowerCase();
	public static final String VALUE_WRITER_ZIP = Format.ZIP.name().toLowerCase();
	
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
		Parameter sourceParam = new UnflaggedOption(PARAM_SOURCE, FileStringParser.getParser().setMustExist(true), null, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The location of the source bag.");
		Parameter destParam = new UnflaggedOption(PARAM_DESTINATION, JSAP.STRING_PARSER, null, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The location of the destination bag.");
		Parameter optionalDestParam = new FlaggedOption(PARAM_DESTINATION, JSAP.STRING_PARSER, null, JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_DESTINATION, "The location of the destination bag (if different than the source bag).");
		Parameter optionalSplitDestParam = new FlaggedOption(PARAM_DESTINATION, JSAP.STRING_PARSER, null, JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_DESTINATION, "The location of the split bags. The default <parent dir of source bag>/<source bag name>_split.");
		Parameter missingBagItTolerantParam = new Switch(PARAM_MISSING_BAGIT_TOLERANT, JSAP.NO_SHORTFLAG, PARAM_MISSING_BAGIT_TOLERANT, "Tolerant of a missing bagit.txt.");
		Parameter additionalDirectoryTolerantParam = new Switch(PARAM_ADDITIONAL_DIRECTORY_TOLERANT, JSAP.NO_SHORTFLAG, PARAM_ADDITIONAL_DIRECTORY_TOLERANT, "Tolerant of additional directories in the bag_dir.");
		Parameter manifestSeparatorParam = new FlaggedOption(PARAM_MANIFEST_SEPARATOR, JSAP.STRING_PARSER, null, JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_MANIFEST_SEPARATOR, "Delimiter used in Payload and Tag Manifest files.  Place within quotes for whitespace.");
		Parameter writerParam = new FlaggedOption(PARAM_WRITER, EnumeratedStringParser.getParser(VALUE_WRITER_FILESYSTEM + ";" + VALUE_WRITER_ZIP), VALUE_WRITER_FILESYSTEM, JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_WRITER, MessageFormat.format("The writer to use to write the bag. Valid values are {0} and {1}.", VALUE_WRITER_FILESYSTEM, VALUE_WRITER_ZIP));
		Parameter payloadParam = new UnflaggedOption(PARAM_PAYLOAD, JSAP.STRING_PARSER, null, JSAP.REQUIRED, JSAP.GREEDY, "List of files/directories to include in payload. To add the children of a directory, but not the directory itself append with " + File.separator + "*.");
		Parameter excludePayloadDirParam = new Switch(PARAM_EXCLUDE_PAYLOAD_DIR, JSAP.NO_SHORTFLAG, PARAM_EXCLUDE_PAYLOAD_DIR, "Exclude the payload directory when constructing the url.");
		Parameter baseUrlParam = new UnflaggedOption(PARAM_BASE_URL, JSAP.STRING_PARSER, null, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The base url to be prepended in creating the fetch.txt.");
		Parameter retrieveUrlParam = new UnflaggedOption(PARAM_URL, JSAP.STRING_PARSER, null, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The url to retrieve the bag from.");
		Parameter threadsParam = new FlaggedOption(PARAM_THREADS, JSAP.INTEGER_PARSER, null, JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_THREADS, "The number of threads to use.  Default is equal to the number of processors.");
		Parameter fetchRetryParam = new FlaggedOption(PARAM_FETCH_RETRY, EnumeratedStringParser.getParser("none;next;retry;threshold"), "threshold", JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_FETCH_RETRY, "How to handle fetch failures.  Must be one of none, next, retry, or threshold.");
		Parameter fetchFailThreshold = new FlaggedOption(PARAM_FETCH_FAILURE_THRESHOLD, JSAP.INTEGER_PARSER, "200", JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_FETCH_FAILURE_THRESHOLD, "The number of total fetch failures to tolerate before giving up.");
		Parameter fetchFileFailThreshold = new FlaggedOption(PARAM_FETCH_FILE_FAILURE_THRESHOLD, JSAP.INTEGER_PARSER, "3", JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_FETCH_FILE_FAILURE_THRESHOLD, "The number times to retry a file before giving up on that file.");
		Parameter excludeBagInfoParam = new Switch(PARAM_EXCLUDE_BAG_INFO, JSAP.NO_SHORTFLAG, PARAM_EXCLUDE_BAG_INFO, "Excludes creating bag-info.txt, if necessary, when completing a bag.");
		Parameter noUpdatePayloadOxumParam = new Switch(PARAM_NO_UPDATE_PAYLOAD_OXUM, JSAP.NO_SHORTFLAG, PARAM_NO_UPDATE_PAYLOAD_OXUM, "Does not update Payload-Oxum in bag-info.txt when completing a bag.");
		Parameter noUpdateBaggingDateParam = new Switch(PARAM_NO_UPDATE_BAGGING_DATE, JSAP.NO_SHORTFLAG, PARAM_NO_UPDATE_BAGGING_DATE, "Does not update Bagging-Date in bag-info.txt when completing a bag.");
		Parameter noUpdateBagSizeParam = new Switch(PARAM_NO_UPDATE_BAG_SIZE, JSAP.NO_SHORTFLAG, PARAM_NO_UPDATE_BAG_SIZE, "Does not update Bag-Size in bag-info.txt when completing a bag.");
		Parameter excludeTagManifestParam = new Switch(PARAM_EXCLUDE_TAG_MANIFEST, JSAP.NO_SHORTFLAG, PARAM_EXCLUDE_TAG_MANIFEST, "Excludes creating a tag manifest when completing a bag.");
		Parameter tagManifestAlgorithmParam = new FlaggedOption(PARAM_TAG_MANIFEST_ALGORITHM, EnumeratedStringParser.getParser(getAlgorithmList()), Algorithm.MD5.bagItAlgorithm, JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_TAG_MANIFEST_ALGORITHM, MessageFormat.format("The algorithm used to generate the tag manifest. Valid values are {0}. Default is {1}.", getAlgorithmListString(), Algorithm.MD5.bagItAlgorithm ));
		Parameter payloadManifestAlgorithmParam = new FlaggedOption(PARAM_PAYLOAD_MANIFEST_ALGORITHM, EnumeratedStringParser.getParser(getAlgorithmList()), Algorithm.MD5.bagItAlgorithm, JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_PAYLOAD_MANIFEST_ALGORITHM, MessageFormat.format("The algorithm used to generate the payload manifest. Valid values are {0}. Default is {1}.", getAlgorithmListString(), Algorithm.MD5.bagItAlgorithm ));
		Parameter versionParam = new FlaggedOption(PARAM_VERSION, EnumeratedStringParser.getParser(getVersionList(), false, false), null, JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_VERSION, MessageFormat.format("The version used to check the bag. Valid values are {0}. Default is to discover from the bagit.txt or latest version.", getVersionListString()));
		Parameter relaxSSLParam = new Switch(PARAM_RELAX_SSL, JSAP.NO_SHORTFLAG, PARAM_RELAX_SSL, "Tolerant of self-signed SSL certificates.");
		Parameter usernameParam = new FlaggedOption(PARAM_USERNAME, JSAP.STRING_PARSER, null, JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_USERNAME, "The username for basic authentication.");
		Parameter passwordParam = new FlaggedOption(PARAM_PASSWORD, JSAP.STRING_PARSER, null, JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_PASSWORD, "The password for basic authentication.");
		Parameter retainBaseDirParam = new Switch(PARAM_RETAIN_BASE_DIR, JSAP.NO_SHORTFLAG, PARAM_RETAIN_BASE_DIR, "Indicates that the base directory (not just the contents of the base directory) should be placed in the data directory of the bag.");
		Parameter bagInfoTxtParam = new FlaggedOption(PARAM_BAGINFOTXT, FileStringParser.getParser().setMustExist(true), null, JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_BAGINFOTXT, "An external bag-info.txt file to include in the bag.");
		Parameter noResultFileParam = new Switch(PARAM_NO_RESULTFILE, JSAP.NO_SHORTFLAG, PARAM_NO_RESULTFILE, "Suppress creating a result file.");
		Parameter resumeParam = new Switch(PARAM_RESUME, JSAP.NO_SHORTFLAG, PARAM_RESUME, "Resume from where the fetch left off.");
		Parameter maxBagSizeParam = new FlaggedOption(PARAM_MAX_BAG_SIZE, JSAP.DOUBLE_PARSER, null, JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_MAX_BAG_SIZE, "The max size of a split bag in GB. Default is 300GB.");
		Parameter keepLowestLevelDirParam = new Switch(PARAM_KEEP_LOWEST_LEVEL_DIR, JSAP.NO_SHORTFLAG, PARAM_KEEP_LOWEST_LEVEL_DIR, "Does not split the lowest level directory.");
		Parameter fileExtensionsParam = new UnflaggedOption(PARAM_FILE_EXTENSIONS, JSAP.STRING_PARSER, null, JSAP.REQUIRED, JSAP.NOT_GREEDY, "File types delimited by a comma will be grouped into different bags; file types delimited by a colon will be grouped into one single bag.");
		Parameter excludeDirsParam = new FlaggedOption(PARAM_EXCLUDE_DIRS, JSAP.STRING_PARSER, null, JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_EXCLUDE_DIRS, "Directories in the bag to be ignored in the split operation; they will be kept in the source bag; they should be relative to the base path of the bag. ");
		Parameter keepEmptyDirsParam = new Switch(PARAM_KEEP_EMPTY_DIRS, JSAP.NO_SHORTFLAG, PARAM_KEEP_EMPTY_DIRS, "Retains empty directories by placing .keep files in them.");
		Parameter excludeSymlinksParam = new Switch(PARAM_EXCLUDE_SYMLINKS, JSAP.NO_SHORTFLAG, PARAM_EXCLUDE_SYMLINKS, "Ignore symbolic links (for bags on file systems only).");
		Parameter failModeParam = new FlaggedOption(PARAM_FAIL_MODE, EnumeratedStringParser.getParser(getFailModeList()), FailMode.FAIL_FAST.name(), JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_FAIL_MODE, MessageFormat.format("The fail mode for the verification.  " + 
				"Valid values are {0} (fail on first error), " +
				"{1} (fail over step of verification. A step is a set of like verification operations. For example, check that all payload files are in at least one manifest.), " +
				"{2} (fail after stage of verification. A stage is a set of logically grouped verification operations. For example, when validating a bag, all of the operations to verify that a bag is complete is a stage. This mode is how previous versions of BIL operated.), " +
				"{3} (complete verification then fail).", FailMode.FAIL_FAST.name(), FailMode.FAIL_STEP.name(), FailMode.FAIL_STAGE.name(), FailMode.FAIL_SLOW.name()));
		Parameter compressionParam = new FlaggedOption(PARAM_COMPRESSION_LEVEL, JSAP.INTEGER_PARSER, "0", JSAP.NOT_REQUIRED, JSAP.NO_SHORTFLAG, PARAM_COMPRESSION_LEVEL, "The compression level to apply to zip files. Valid values are 1 (least compression, fastest) to 9 (most compression, slowest).");
		
		this.addOperation(OPERATION_VERIFY_TAGMANIFESTS,
				"Verifies the checksums in all tag manifests.",
				new Parameter[] {sourceParam, versionParam, noResultFileParam, failModeParam},
				new String[] {MessageFormat.format("bag {0} {1}", OPERATION_VERIFY_TAGMANIFESTS, this.getBag("mybag"))});

		this.addOperation(OPERATION_VERIFY_PAYLOADMANIFESTS,
				"Verifies the checksums in all payload manifests.",
				new Parameter[] {sourceParam, versionParam, noResultFileParam, failModeParam},
				new String[] {MessageFormat.format("bag {0} {1}", OPERATION_VERIFY_PAYLOADMANIFESTS, this.getBag("mybag"))});

		this.addOperation(OPERATION_VERIFYVALID,
				"Verifies the validity of a bag.",
				new Parameter[] {sourceParam, versionParam, missingBagItTolerantParam, additionalDirectoryTolerantParam, noResultFileParam, excludeSymlinksParam, failModeParam},
				new String[] {MessageFormat.format("bag {0} {1}", OPERATION_VERIFYVALID, this.getBag("mybag"))});

		this.addOperation(OPERATION_VERIFYCOMPLETE,
				"Verifies the completeness of a bag.",
				new Parameter[] {sourceParam, versionParam, missingBagItTolerantParam, additionalDirectoryTolerantParam, noResultFileParam, excludeSymlinksParam, failModeParam},
				new String[] {MessageFormat.format("bag {0} {1}", OPERATION_VERIFYCOMPLETE, this.getBag("mybag"))});
		
		this.addOperation(OPERATION_SPLIT_BAG_BY_SIZE,
				"Splits a bag by size.",
				new Parameter[] {sourceParam, optionalSplitDestParam, maxBagSizeParam, keepLowestLevelDirParam, writerParam, compressionParam, excludeDirsParam},
				new String[] {MessageFormat.format("bag {0} {1}", OPERATION_SPLIT_BAG_BY_SIZE, this.getBag("mybag"))});		
		
		this.addOperation(OPERATION_SPLIT_BAG_BY_FILE_TYPE,
				"Splits a bag by file types.",
				new Parameter[] {sourceParam, fileExtensionsParam, optionalSplitDestParam, writerParam, compressionParam, excludeDirsParam},
				new String[] {MessageFormat.format("bag {0} {1} {2}", OPERATION_SPLIT_BAG_BY_FILE_TYPE, this.getBag("mybag"), "pdf,gif")});	
		
		this.addOperation(OPERATION_SPLIT_BAG_BY_SIZE_AND_FILE_TYPE,
				"Splits a bag by size and file types.",
				new Parameter[] {sourceParam, fileExtensionsParam, optionalSplitDestParam, maxBagSizeParam, keepLowestLevelDirParam, writerParam, compressionParam, excludeDirsParam},
				new String[] {MessageFormat.format("bag {0} {1} {2}", OPERATION_SPLIT_BAG_BY_SIZE_AND_FILE_TYPE, this.getBag("mybag"), "pdf,gif:xml,gif")});	
		
		List<Parameter> completeParams = new ArrayList<Parameter>();
		completeParams.add(excludeBagInfoParam);
		completeParams.add(noUpdateBaggingDateParam);
		completeParams.add(noUpdateBagSizeParam);
		completeParams.add(noUpdatePayloadOxumParam);
		completeParams.add(excludeTagManifestParam);
		completeParams.add(tagManifestAlgorithmParam);
		completeParams.add(payloadManifestAlgorithmParam);
		completeParams.add(versionParam);
		completeParams.add(manifestSeparatorParam);
		
		List<Parameter> makeCompleteParams = new ArrayList<Parameter>();
		makeCompleteParams.add(sourceParam);
		makeCompleteParams.add(destParam);
		makeCompleteParams.add(writerParam);
		makeCompleteParams.add(compressionParam);
		makeCompleteParams.addAll(completeParams);
		
		this.addOperation(OPERATION_MAKE_COMPLETE,
				"Completes a bag and then writes in a specified format.  Completing a bag fills in any missing parts.",
				makeCompleteParams,
				new String[] {MessageFormat.format("bag {0} {1} {2}", OPERATION_MAKE_COMPLETE, this.getBag("mybag"), this.getBag("myDestBag"))});

		this.addOperation(OPERATION_UPDATE,
				"Updates the manifests and (if it exists) the bag-info.txt for a bag.",
				new Parameter[] {sourceParam, optionalDestParam, writerParam, compressionParam, manifestSeparatorParam},
				new String[] {MessageFormat.format("bag {0} {1} ", OPERATION_UPDATE, this.getBag("mybag"))});
		
		this.addOperation(OPERATION_UPDATE_TAGMANIFESTS,
				"Updates the tag manifests for a bag.  The bag must be unserialized.",
				new Parameter[] {sourceParam, tagManifestAlgorithmParam, manifestSeparatorParam},
				new String[] {MessageFormat.format("bag {0} {1}", OPERATION_UPDATE_TAGMANIFESTS, this.getBag("mybag"))});

		this.addOperation(OPERATION_UPDATE_PAYLOAD_OXUM,
				"Generates and updates the Payload-Oxum in the bag-info.txt.  The bag must be unserialized.",
				new Parameter[] {sourceParam},
				new String[] {MessageFormat.format("bag {0} {1}", OPERATION_UPDATE_PAYLOAD_OXUM, this.getBag("mybag"))});

		List<Parameter> bagInPlaceParams = new ArrayList<Parameter>();
		bagInPlaceParams.add(sourceParam);
		bagInPlaceParams.add(retainBaseDirParam);
		bagInPlaceParams.addAll(completeParams);
		bagInPlaceParams.add(bagInfoTxtParam);
		bagInPlaceParams.add(keepEmptyDirsParam);
		this.addOperation(OPERATION_BAG_IN_PLACE,
				"Creates a bag-in-place.  The source must be a directory on a filesystem and may already have a data directory.",
				bagInPlaceParams,
				new String[] {MessageFormat.format("bag {0} {1}", OPERATION_BAG_IN_PLACE, this.getBag("mybag"))});
		
		List<Parameter> createParams = new ArrayList<Parameter>();
		createParams.add(destParam);
		createParams.add(payloadParam);
		createParams.add(writerParam);
		createParams.add(compressionParam);
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
		makeHoleyParam.add(compressionParam);
		makeHoleyParam.add(excludePayloadDirParam);		
		makeHoleyParam.add(resumeParam);		
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
				new Parameter[] {destParam, retrieveUrlParam, relaxSSLParam, threadsParam, fetchRetryParam, fetchFileFailThreshold, fetchFailThreshold, usernameParam, passwordParam, resumeParam},
				new String[] {MessageFormat.format("bag {0} {1} http://www.loc.gov/bags/mybag", OPERATION_RETRIEVE, this.getBag("myDestBag"))});
		
		this.addOperation(OPERATION_FILL_HOLEY, 
				"Retrieves any missing pieces of a local bag.", 
				new Parameter[] {sourceParam, relaxSSLParam, threadsParam, fetchRetryParam, fetchFileFailThreshold, fetchFailThreshold, usernameParam, passwordParam, resumeParam},
				new String[] {MessageFormat.format("bag {0} {1}", OPERATION_FILL_HOLEY, this.getBag("mybag"))});
		
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
		jsap.registerParameter(new Switch(PARAM_VERBOSE, JSAP.NO_SHORTFLAG, PARAM_VERBOSE, "Reports progress of the operation to the console."));
		jsap.registerParameter(new Switch(PARAM_LOG_VERBOSE, JSAP.NO_SHORTFLAG, PARAM_LOG_VERBOSE, "Reports progress of the operation to the log."));

		this.operationMap.put(name, new Operation(name, jsap, help, examples));
	}
	
	private void addOperation(String name, String help, List<Parameter> params, String[] examples) throws Exception {
		this.addOperation(name, help, params.toArray(new Parameter[] {}), examples);
	}

	
	public int execute(String[] args) throws Exception {		
		log.debug("Executing with arguments: " + argsToString(args));
		int ret = RETURN_SUCCESS;
		
		if (args.length == 0 || (args.length == 1 && args[0].equals("--" + PARAM_HELP))) {
			printUsage();
		} else if (args.length == 1 && args[0].equals("--" + PARAM_VERSION)) {
			printVersion();
			
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
					System.err.println("Error parse arguments.");
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

	private static String getFailModeList() {
		String list = "";
		for(int i=0; i < FailMode.values().length; i++) {
			list += FailMode.values()[i];
			if (i != FailMode.values().length -1) {
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
	
	private String getVersion() {
		return System.getProperty("version");
	}
	
	private void printVersion() {
		System.out.println("BagIt Library (BIL) Version " + this.getVersion());
	}
	
	private void printUsage() {
		printVersion();
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
		System.out.println("\t[--version]");
		System.out.println("\t\tPrints version of BIL and exits.");
		
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
			
			CompositeProgressListener listener = new CompositeProgressListener();
			if (config.getBoolean(PARAM_VERBOSE)) {
				listener.addProgressListener(new ConsoleProgressListener());
			}
			if (config.getBoolean(PARAM_LOG_VERBOSE)) {
				listener.addProgressListener(new LoggingProgressListener());
			}
			
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
				} else if (Format.ZIP.equals(format)) {
					writer = new ZipWriter(bagFactory);
					int compressionLevel = config.getInt(PARAM_COMPRESSION_LEVEL);
					if (compressionLevel >=1 && compressionLevel <= 9) ((ZipWriter)writer).setCompressionLevel(compressionLevel);
				}
			}
			if (writer instanceof ProgressListenable) ((ProgressListenable)writer).addProgressListener(listener);

			String manifestSeparator = null;
			int ret = RETURN_SUCCESS;

			if(config.contains(PARAM_MANIFEST_SEPARATOR)){
				manifestSeparator = config.getString(PARAM_MANIFEST_SEPARATOR).replace("\\t", "\t");
			}
									
			DefaultCompleter completer = new DefaultCompleter(bagFactory);
			completer.setGenerateBagInfoTxt(! config.getBoolean(PARAM_EXCLUDE_BAG_INFO, false));
			completer.setUpdateBaggingDate(! config.getBoolean(PARAM_NO_UPDATE_BAGGING_DATE, false));
			completer.setUpdateBagSize(! config.getBoolean(PARAM_NO_UPDATE_BAG_SIZE, false));
			completer.setUpdatePayloadOxum(! config.getBoolean(PARAM_NO_UPDATE_PAYLOAD_OXUM, false));
			completer.setGenerateTagManifest(! config.getBoolean(PARAM_EXCLUDE_TAG_MANIFEST, false));
			completer.setTagManifestAlgorithm(Algorithm.valueOfBagItAlgorithm(config.getString(PARAM_TAG_MANIFEST_ALGORITHM, Algorithm.MD5.bagItAlgorithm)));
			completer.setPayloadManifestAlgorithm(Algorithm.valueOfBagItAlgorithm(config.getString(PARAM_PAYLOAD_MANIFEST_ALGORITHM, Algorithm.MD5.bagItAlgorithm)));
			completer.setNonDefaultManifestSeparator(manifestSeparator);
			completer.addProgressListener(listener);
			
		    BagFetcher fetcher = new BagFetcher(bagFactory);
		    fetcher.addProgressListener(listener);

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
								
			}
			
			if (OPERATION_VERIFYVALID.equals(operation.name)) {				
				CompleteVerifierImpl completeVerifier = new CompleteVerifierImpl();
				completeVerifier.setMissingBagItTolerant(config.getBoolean(PARAM_MISSING_BAGIT_TOLERANT, false));
				completeVerifier.setAdditionalDirectoriesInBagDirTolerant(config.getBoolean(PARAM_ADDITIONAL_DIRECTORY_TOLERANT, false));
				completeVerifier.setIgnoreSymlinks(config.getBoolean(PARAM_EXCLUDE_SYMLINKS));
				completeVerifier.addProgressListener(listener);
				ParallelManifestChecksumVerifier checksumVerifier = new ParallelManifestChecksumVerifier();
				checksumVerifier.addProgressListener(listener);
				ValidVerifierImpl verifier = new ValidVerifierImpl(completeVerifier, checksumVerifier);
				verifier.addProgressListener(listener);
				verifier.setFailMode(FailMode.valueOf(config.getString(PARAM_FAIL_MODE).toUpperCase()));
				Bag bag = this.getBag(sourceFile, version, LoadOption.BY_MANIFESTS);				
				try {
					SimpleResult result = verifier.verify(bag);
					log.info(result.toString());
					System.out.println(result.toString(SimpleResult.DEFAULT_MAX_MESSAGES, "\n"));
					if (! result.isSuccess()) {
						if (writeResultFile) this.writeResultFile(operation.name, result, bag.getFile());
						ret = RETURN_FAILURE;
					}
				} finally {
					bag.close();
				}
			} else if (OPERATION_VERIFYCOMPLETE.equals(operation.name)) {				
				CompleteVerifierImpl completeVerifier = new CompleteVerifierImpl();
				completeVerifier.setMissingBagItTolerant(config.getBoolean(PARAM_MISSING_BAGIT_TOLERANT, false));				
				completeVerifier.setAdditionalDirectoriesInBagDirTolerant(config.getBoolean(PARAM_ADDITIONAL_DIRECTORY_TOLERANT, false));
				completeVerifier.setIgnoreSymlinks(config.getBoolean(PARAM_EXCLUDE_SYMLINKS));
				completeVerifier.addProgressListener(listener);				
				completeVerifier.setFailMode(FailMode.valueOf(config.getString(PARAM_FAIL_MODE).toUpperCase()));
				Bag bag = this.getBag(sourceFile, version, LoadOption.BY_MANIFESTS);
				try {
					SimpleResult result = completeVerifier.verify(bag);
					log.info(result.toString());
					System.out.println(result.toString(SimpleResult.DEFAULT_MAX_MESSAGES, "\n"));
					if (! result.isSuccess()) {
						if (writeResultFile) this.writeResultFile(operation.name, result, bag.getFile());
						ret = RETURN_FAILURE;
					}
				} finally {
					bag.close();
				}
			} else if (OPERATION_VERIFY_TAGMANIFESTS.equals(operation.name)) {				
				Bag bag = this.getBag(sourceFile, version, LoadOption.BY_MANIFESTS);
				try {
					ParallelManifestChecksumVerifier verifier = new ParallelManifestChecksumVerifier();
					verifier.addProgressListener(listener);
					verifier.setFailMode(FailMode.valueOf(config.getString(PARAM_FAIL_MODE).toUpperCase()));
					SimpleResult result = verifier.verify(bag.getTagManifests(), bag);
					log.info(result.toString());
					System.out.println(result.toString(SimpleResult.DEFAULT_MAX_MESSAGES, "\n"));
					if (! result.isSuccess()) {
						if (writeResultFile) this.writeResultFile(operation.name, result, bag.getFile());
						ret = RETURN_FAILURE;
					}
				} finally {
					bag.close();
				}
			} else if (OPERATION_VERIFY_PAYLOADMANIFESTS.equals(operation.name)) {				
				Bag bag = this.getBag(sourceFile, version, LoadOption.BY_MANIFESTS);
				try {
					ParallelManifestChecksumVerifier verifier = new ParallelManifestChecksumVerifier();
					verifier.addProgressListener(listener);
					verifier.setFailMode(FailMode.valueOf(config.getString(PARAM_FAIL_MODE).toUpperCase()));					
					SimpleResult result = verifier.verify(bag.getPayloadManifests(), bag);
					log.info(result.toString());
					System.out.println(result.toString(SimpleResult.DEFAULT_MAX_MESSAGES, "\n"));
					if (! result.isSuccess()) {
						if (writeResultFile) this.writeResultFile(operation.name, result, bag.getFile());
						ret = RETURN_FAILURE;
					}
				} finally {
					bag.close();
				}
			} else if (OPERATION_MAKE_COMPLETE.equals(operation.name)) {
				Bag bag = this.getBag(sourceFile, version, LoadOption.BY_FILES);
				try {
					Bag newBag = completer.complete(bag);
					try {
						newBag.write(writer, destFile);
					} finally {
						newBag.close();
					}
				} finally {
					bag.close();
				}
			}  else if (OPERATION_UPDATE.equals(operation.name)) {
				Bag bag = this.getBag(sourceFile, version, LoadOption.BY_FILES);
				try {
					UpdateCompleter updateCompleter = new UpdateCompleter(bagFactory);
					completer.setNonDefaultManifestSeparator(manifestSeparator);
					completer.addProgressListener(listener);
					Bag newBag = updateCompleter.complete(bag);
					try {
						newBag.write(writer, destFile != null?destFile:sourceFile);
					} finally {
						newBag.close();
					}
				} finally {
					bag.close();
				}
			}
			else if (OPERATION_UPDATE_TAGMANIFESTS.equals(operation.name)) {
				Bag bag = this.getBag(sourceFile, version, LoadOption.BY_FILES);
				try {
					TagManifestCompleter tagManifestCompleter = new TagManifestCompleter(bagFactory);
					tagManifestCompleter.setTagManifestAlgorithm(Algorithm.valueOfBagItAlgorithm(config.getString(PARAM_TAG_MANIFEST_ALGORITHM, Algorithm.MD5.bagItAlgorithm)));
					tagManifestCompleter.setNonDefaultManifestSeparator(manifestSeparator);
					Bag newBag = tagManifestCompleter.complete(bag);
					try {
						for(Manifest manifest : newBag.getTagManifests()) {
							FileSystemHelper.write(manifest, new File(sourceFile, manifest.getFilepath()));
						}
					} finally {
						bag.close();
					}
				} finally {
					bag.close();
				}
			} else if (OPERATION_UPDATE_PAYLOAD_OXUM.equals(operation.name)) {
					Bag bag = this.getBag(sourceFile, version, LoadOption.BY_FILES);
					try {
						UpdatePayloadOxumCompleter updatePayloadOxumCompleter = new UpdatePayloadOxumCompleter(bagFactory);
						Bag newBag = updatePayloadOxumCompleter.complete(bag);
						try {
							if (newBag.getBagInfoTxt() != null) {
								FileSystemHelper.write(newBag.getBagInfoTxt(), new File(sourceFile, newBag.getBagInfoTxt().getFilepath()));
							}
							for(Manifest manifest : newBag.getTagManifests()) {
								FileSystemHelper.write(manifest, new File(sourceFile, manifest.getFilepath()));
							}
						} finally {
							bag.close();
						}
					} finally {
						bag.close();
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
				preBag.makeBagInPlace(version != null ? version : BagFactory.LATEST, config.getBoolean(PARAM_RETAIN_BASE_DIR, false), config.getBoolean(PARAM_KEEP_EMPTY_DIRS, false), completer);
			} else if (OPERATION_CREATE.equals(operation.name)) {
				Bag bag = this.getBag(sourceFile, version, null);
				try {
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
					Bag newBag = completer.complete(bag);
					try {
						newBag.write(writer, destFile);
					} finally {
						newBag.close();
					}
				} finally {
					bag.close();
				}

			} else if (OPERATION_MAKE_HOLEY.equals(operation.name)) {
				HolePuncherImpl puncher = new HolePuncherImpl(bagFactory);
				Bag bag = this.getBag(sourceFile, version, LoadOption.BY_MANIFESTS);
				try {
					Bag newBag = puncher.makeHoley(bag, config.getString(PARAM_BASE_URL), ! config.getBoolean(PARAM_EXCLUDE_PAYLOAD_DIR, false), false, config.getBoolean(PARAM_RESUME));
					try {
						newBag.write(writer, destFile);
					} finally {
						bag.close();
					}
				} finally {
					bag.close();
				}
			} else if (OPERATION_GENERATE_PAYLOAD_OXUM.equals(operation.name)) {
				Bag bag = this.getBag(sourceFile, version, LoadOption.BY_MANIFESTS);
				try {
					String oxum = BagHelper.generatePayloadOxum(bag);				
					log.info("Payload-Oxum is " + oxum);
					System.out.println("Payload-Oxum is " + oxum);
				} finally {
					bag.close();
				}
			} else if (OPERATION_CHECK_PAYLOAD_OXUM.equals(operation.name)) {
				Bag bag = this.getBag(sourceFile, version, LoadOption.BY_MANIFESTS);
				try {
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
								String oxumMsg = "Payload-Oxum matches.";
								System.out.println(oxumMsg);
								log.info(oxumMsg);
							} else {
								String oxumMsg = MessageFormat.format("Payload-Oxum does not match. Expected {0} according to bag-info.txt but found {1}.", checkOxum, genOxum);
								System.out.println(oxumMsg);
								log.info(oxumMsg);
								ret = RETURN_FAILURE;
							}
						}
					}
				} finally {
					bag.close();
				}
			} else if (OPERATION_FILL_HOLEY.equals(operation.name)) {
			    FileSystemFileDestination dest = new FileSystemFileDestination(sourceFile);
				Bag bag = this.getBag(sourceFile, version, null);			    
			    try {
				    SimpleResult result = fetcher.fetch(bag, dest, config.getBoolean(PARAM_RESUME));
					log.info(result.toString());
					System.out.println(result.toString(SimpleResult.DEFAULT_MAX_MESSAGES, "\n"));
					if (! result.isSuccess()) {
						if (writeResultFile) this.writeResultFile(operation.name, result, bag.getFile());
						ret = RETURN_FAILURE;
					}
			    } finally {
			    	bag.close();
			    }
			} else if (OPERATION_RETRIEVE.equals(operation.name)) {
				SimpleResult result = fetcher.fetchRemoteBag(destFile, config.getString(PARAM_URL), config.getBoolean(PARAM_RESUME));
				log.info(result.toString());
				System.out.println(result.toString(SimpleResult.DEFAULT_MAX_MESSAGES, "\n"));
				if (! result.isSuccess()) {
					if (writeResultFile) this.writeResultFile(operation.name, result, destFile);
					ret = RETURN_FAILURE;
				}

			} else if(OPERATION_SPLIT_BAG_BY_SIZE.equals(operation.name)
					|| OPERATION_SPLIT_BAG_BY_FILE_TYPE.equals(operation.name)
					|| OPERATION_SPLIT_BAG_BY_SIZE_AND_FILE_TYPE.equals(operation.name)) {
				
				Bag srcBag = this.bagFactory.createBag(sourceFile, BagFactory.LoadOption.BY_FILES);
				try {
					Double sourceBagSize = null;
					if(srcBag.getBagInfoTxt() != null && srcBag.getBagInfoTxt().getPayloadOxum() != null){
						sourceBagSize = new Double(srcBag.getBagInfoTxt().getPayloadOxum());					
					}
	
					//The default dest of split bags is parentDirOfSourceBag/SourceBagName_split
			    	File destBagFile = destFile == null ? new File(srcBag.getFile() + "_split") : destFile;
	
			    	//The default max bag size is 300 GB. 
			    	Double maxBagSizeInGB = config.contains(PARAM_MAX_BAG_SIZE) ? config.getDouble(PARAM_MAX_BAG_SIZE) : 300;
				    Double maxBagSize =  maxBagSizeInGB != null ? maxBagSizeInGB * SizeHelper.GB : 300 * SizeHelper.GB;			
					
					String[] fileExtensions = config.contains(PARAM_FILE_EXTENSIONS) ? config.getString(PARAM_FILE_EXTENSIONS).split(",") : null;
					String[][] fileExtensionsIn = null;
					if(fileExtensions != null && fileExtensions.length > 0){
						fileExtensionsIn = new String[fileExtensions.length][];
						for(int i = 0 ; i < fileExtensions.length ; i++){
							fileExtensionsIn[i] = fileExtensions[i].split(":");
						}
					}
	
					String[] excludeDirs = config.contains(PARAM_EXCLUDE_DIRS) ? config.getString(PARAM_EXCLUDE_DIRS).split(",") : null;
	
					boolean keepLowestLevelDir = config.getBoolean(PARAM_KEEP_LOWEST_LEVEL_DIR, false);
					//boolean keepSourceBag = config.getBoolean(PARAM_KEEP_SOURCE_BAG, false);
					
					if(OPERATION_SPLIT_BAG_BY_SIZE.equals(operation.name)){
						if(sourceBagSize != null && sourceBagSize <= maxBagSizeInGB * SizeHelper.GB){
							System.out.println("Max bag size should not be greater than the source bag size.");
							return RETURN_FAILURE;
						}
	
						Splitter splitter = new SplitBySize(this.bagFactory, maxBagSize, keepLowestLevelDir, excludeDirs);
						List<Bag> splitBags = splitter.split(srcBag);
						try {
							this.completeAndWriteBagToDisk(splitBags, completer, writer, srcBag, destBagFile, true);
						} finally {
							for(Bag bag : splitBags) bag.close();
						}
	
					} else if(OPERATION_SPLIT_BAG_BY_FILE_TYPE.equals(operation.name)){
						if(fileExtensionsIn == null){
							System.out.println("File extensions should not be null or empty.");
							return RETURN_FAILURE;
				    	}
				    	
						Splitter splitter = new SplitByFileType(this.bagFactory, fileExtensionsIn, excludeDirs);
						List<Bag> splitBags = splitter.split(srcBag);
						try {
							this.completeAndWriteBagToDisk(splitBags, completer, writer, srcBag, destBagFile, false);
						} finally {
							for(Bag bag : splitBags) bag.close();
						}
						
					} else if(OPERATION_SPLIT_BAG_BY_SIZE_AND_FILE_TYPE.equals(operation.name)){
						if(fileExtensionsIn == null){
							System.out.println("File extensions should not be null or empty.");
							return RETURN_FAILURE;
				    	}
	
						Splitter splitter1 = new SplitByFileType(this.bagFactory, fileExtensionsIn, excludeDirs);
						List<Bag> bags = splitter1.split(srcBag);
						Splitter splitter2 = new SplitBySize(this.bagFactory, maxBagSize, keepLowestLevelDir, excludeDirs);
						try {
							for(Bag bag : bags) {							
								List<Bag> bagsUnderMaxSize = new ArrayList<Bag>();
								if(new Double(bag.getBagInfoTxt().getPayloadOxum()) <= maxBagSizeInGB * SizeHelper.GB){
									bagsUnderMaxSize.add(bag);
								}else{
									List<Bag> bags2 = splitter2.split(bag);
									try {
										this.completeAndWriteBagToDisk(bags2, completer, writer, srcBag, destBagFile, true);
									} finally {
										for(Bag bag2 : bags2) bag2.close();
									}
								}
								this.completeAndWriteBagToDisk(bagsUnderMaxSize, completer, writer, srcBag, destBagFile, true);							
							}
						} finally {
							for(Bag bag : bags) bag.close();
						}
					}
				} finally {
					srcBag.close();
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
	
	private void completeAndWriteBagToDisk(List<Bag> bags, Completer completer, Writer writer, Bag srcBag, File destBagFile, boolean appendNumber){
					
		int i = 0;
		for(Bag bag : bags) {
			Bag newBag = completer.complete(bag);
			
			StringBuffer bagName = new StringBuffer();
			bagName.append(srcBag.getFile().getName());
			if(newBag.getBagInfoTxt().get(Splitter.FILE_TYPE_KEY) != null){
				bagName.append("_").append(newBag.getBagInfoTxt().get(Splitter.FILE_TYPE_KEY).replaceAll(" ", "_"));
			}
			if(appendNumber && bags.size() > 1){
				bagName.append("_").append(i);						
			}			
			
			newBag.write(writer == null ? new FileSystemWriter(bagFactory) : writer, new File(destBagFile, bagName.toString()));
	    	i++;	    	
		}
		
	}
		
	private void writeResultFile(String operation, SimpleResult result, File bagFile) {
		if (result.isSuccess()) return;
		String filename = MessageFormat.format("{0}-{1}.txt", operation, System.getProperty("log.timestamp"));
		if (bagFile != null) {
			filename = MessageFormat.format("{0}-{1}", bagFile.getName(), filename);
		}
		File file = new File(filename);
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
