package gov.loc.repository.bagit.driver;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagHelper;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.BagWriter;
import gov.loc.repository.bagit.BagFactory.Version;
import gov.loc.repository.bagit.Manifest.Algorithm;
import gov.loc.repository.bagit.bagwriter.FileSystemBagWriter;
import gov.loc.repository.bagit.bagwriter.TarBagWriter;
import gov.loc.repository.bagit.bagwriter.ZipBagWriter;
import gov.loc.repository.bagit.completion.DefaultCompletionStrategy;
import gov.loc.repository.bagit.utilities.SimpleResult;

import java.io.File;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
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
	public static final String OPERATION_COMPLETE = "complete";
	public static final String OPERATION_CREATE = "create";
	public static final String OPERATION_MAKE_HOLEY = "makeholey";
	public static final String OPERATION_GENERATE_PAYLOAD_OXUM = "generatepayloadoxum";
	public static final String OPERATION_CHECK_PAYLOAD_OXUM = "checkpayloadoxum";
	
	public static final String OPTION_SOURCE = "source";
	public static final String OPTION_DESTINATION = "dest";
	public static final String OPTION_MISSING_BAGIT_TOLERANT = "missingBagItTolerant";
	public static final String OPTION_WRITER = "writer";
	public static final String OPTION_PAYLOAD = "payload";
	public static final String OPTION_EXCLUDE_PAYLOAD_DIR = "excludePayloadDir";
	public static final String OPTION_BASE_URL = "baseUrl";
	public static final String OPTION_EXCLUDE_BAG_INFO = "excludeBagInfo";
	public static final String OPTION_NO_UPDATE_PAYLOAD_OXUM = "noUpdatePayloadOxum";
	public static final String OPTION_NO_UPDATE_BAGGING_DATE = "noUpdateBaggingDate";
	public static final String OPTION_NO_UPDATE_BAG_SIZE = "noUpdateBagSize";
	public static final String OPTION_EXCLUDE_TAG_MANIFEST = "excludeTagManifest";
	public static final String OPTION_TAG_MANIFEST_ALGORITHM = "tagManifestAlgorithm";
	public static final String OPTION_PAYLOAD_MANIFEST_ALGORITHM = "payloadManifestAlgorithm";
	public static final String OPTION_VERSION = "version";
	
	public static final String VALUE_WRITER_FILESYSTEM = "filesystem";
	public static final String VALUE_WRITER_ZIP = "zip";
	public static final String VALUE_WRITER_TAR = "tar";
	
	private static final Log log = LogFactory.getLog(CommandLineBagDriver.class);
	
	public static void main(String[] args) throws Exception {
		int ret = main2(args);
		log.info("Returning " + ret);
		System.exit(ret);
		
	}
	
	public static int main2(String[] args) throws Exception {
		Parameter sourceParam = new UnflaggedOption(OPTION_SOURCE, JSAP.STRING_PARSER, null, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The location of the source bag.");
		Parameter destParam = new UnflaggedOption(OPTION_DESTINATION, JSAP.STRING_PARSER, null, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The location of the destination bag.");
		Parameter missingBagItTolerantParam = new Switch(OPTION_MISSING_BAGIT_TOLERANT, JSAP.NO_SHORTFLAG, "missingbagittolerant", "Tolerant of a missing bag-it.txt.");
		Parameter writerParam = new FlaggedOption(OPTION_WRITER, EnumeratedStringParser.getParser(VALUE_WRITER_FILESYSTEM + ";" + VALUE_WRITER_ZIP + ";" + VALUE_WRITER_TAR), VALUE_WRITER_FILESYSTEM, JSAP.REQUIRED, 'w', "writer", MessageFormat.format("The writer to use to write the bag. Valid values are {0}, {1} and {2}.", VALUE_WRITER_FILESYSTEM, VALUE_WRITER_TAR, VALUE_WRITER_ZIP));
		Parameter payloadParam = new UnflaggedOption(OPTION_PAYLOAD, FileStringParser.getParser().setMustExist(true), null, JSAP.REQUIRED, JSAP.GREEDY, "List of files/directories to include in payload.");
		Parameter excludePayloadDirParam = new Switch(OPTION_EXCLUDE_PAYLOAD_DIR, JSAP.NO_SHORTFLAG, "excludepayloaddir", "Exclude the payload directory when constructing the url.");
		Parameter baseUrlParam = new UnflaggedOption(OPTION_BASE_URL, JSAP.STRING_PARSER, null, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The base url to be prepended in creating the fetch.txt.");
		Parameter excludeBagInfoParam = new Switch(OPTION_EXCLUDE_BAG_INFO, JSAP.NO_SHORTFLAG, "excludebaginfo", "Excludes creating bag-info.txt, if necessary, when completing a bag.");
		Parameter noUpdatePayloadOxumParam = new Switch(OPTION_NO_UPDATE_PAYLOAD_OXUM, JSAP.NO_SHORTFLAG, "noupdatepayloadoxum", "Does not update Payload-Oxum in bag-info.txt when completing a bag.");
		Parameter noUpdateBaggingDateParam = new Switch(OPTION_NO_UPDATE_BAGGING_DATE, JSAP.NO_SHORTFLAG, "noupdatebaggingdate", "Does not update Bagging-Date in bag-info.txt when completing a bag.");
		Parameter noUpdateBagSizeParam = new Switch(OPTION_NO_UPDATE_BAG_SIZE, JSAP.NO_SHORTFLAG, "noupdatebagsize", "Does not update Bag-Size in bag-info.txt when completing a bag.");
		Parameter excludeTagManifestParam = new Switch(OPTION_EXCLUDE_TAG_MANIFEST, JSAP.NO_SHORTFLAG, "excludetagmanifest", "Excludes creating a tag manifest when completing a bag.");
		Parameter tagManifestAlgorithmParam = new FlaggedOption(OPTION_TAG_MANIFEST_ALGORITHM, EnumeratedStringParser.getParser(getAlgorithmList()), Algorithm.MD5.bagItAlgorithm, JSAP.REQUIRED, 't', "tagManifestAlgorithm", MessageFormat.format("The algorithm used to generate the tag manifest. Valid values are {0}. Default is {1}.", getAlgorithmListString(), Algorithm.MD5.bagItAlgorithm ));
		Parameter payloadManifestAlgorithmParam = new FlaggedOption(OPTION_PAYLOAD_MANIFEST_ALGORITHM, EnumeratedStringParser.getParser(getAlgorithmList()), Algorithm.MD5.bagItAlgorithm, JSAP.REQUIRED, 'p', "payloadManifestAlgorithm", MessageFormat.format("The algorithm used to generate the payload manifest. Valid values are {0}. Default is {1}.", getAlgorithmListString(), Algorithm.MD5.bagItAlgorithm ));
		Parameter versionParam = new FlaggedOption(OPTION_VERSION, EnumeratedStringParser.getParser(getVersionList(), false, false), null, JSAP.NOT_REQUIRED, 'v', "version", MessageFormat.format("The version used to check the bag. Valid values are {0}. Default is to discover from the bag-it.txt or latest version.", getVersionListString()));
		
		Map<String, SimpleJSAP> jsapMap = new HashMap<String, SimpleJSAP>();
		jsapMap.put(OPERATION_ISVALID, new SimpleJSAP("bag isvalid", "Checks validity of a bag.", new Parameter[] { sourceParam, missingBagItTolerantParam, versionParam }));
		jsapMap.put(OPERATION_ISCOMPLETE, new SimpleJSAP("bag iscomplete", "Checks completeness of a bag.", new Parameter[] { sourceParam, missingBagItTolerantParam, versionParam }));
		jsapMap.put(OPERATION_WRITE, new SimpleJSAP("bag write", "Writes a bag in a specified format.", new Parameter[] { sourceParam, destParam, writerParam }));
		jsapMap.put(OPERATION_COMPLETE, new SimpleJSAP("bag complete", "Completes a bag and then writes in a specified format.", new Parameter[] { sourceParam, destParam, writerParam, excludeBagInfoParam, noUpdateBaggingDateParam, noUpdateBagSizeParam, noUpdatePayloadOxumParam, excludeTagManifestParam, tagManifestAlgorithmParam, payloadManifestAlgorithmParam, versionParam }));
		jsapMap.put(OPERATION_CREATE, new SimpleJSAP("bag create", "Creates a bag from supplied files/directories, completes the bag, and then writes in a specified format.", new Parameter[] { destParam, payloadParam, writerParam, excludeBagInfoParam, noUpdateBaggingDateParam, noUpdateBagSizeParam, noUpdatePayloadOxumParam, excludeTagManifestParam, tagManifestAlgorithmParam, payloadManifestAlgorithmParam, versionParam }));
		jsapMap.put(OPERATION_MAKE_HOLEY, new SimpleJSAP("bag makeholey", "Add holes to a bag and then writes in a specified format.", new Parameter[] { sourceParam, destParam, baseUrlParam, writerParam, excludePayloadDirParam}));
		jsapMap.put(OPERATION_GENERATE_PAYLOAD_OXUM, new SimpleJSAP("bag generatepayloadoxum", "Generates Payload-Oxum for the bag.", new Parameter[] { sourceParam }));
		jsapMap.put(OPERATION_CHECK_PAYLOAD_OXUM, new SimpleJSAP("bag checkpayloadoxum", "Generates Payload-Oxum and checks against Payload-Oxum in bag-info.txt.", new Parameter[] { sourceParam }));
		
		if (args.length == 0) {
			System.out.println("Error: An operation is required.");
			printUsage(jsapMap.keySet());
			return RETURN_ERROR;
		}		
		String operation = args[0];		
		if (! jsapMap.containsKey(operation)) {
			System.out.println("Error: Unknown operation.");
			printUsage(jsapMap.keySet());
			return RETURN_ERROR;
		}
		SimpleJSAP jsap = jsapMap.get(operation);

		String[] newArgs = new String[args.length -1];
		if (newArgs.length > 0) {
			System.arraycopy(args, 1, newArgs, 0, args.length-1);
		} else {
			newArgs = new String[] {"--help"};
		}
		
		JSAPResult config = jsap.parse(newArgs);
		if (jsap.messagePrinted()) {
			return RETURN_ERROR;
		}
		
		log.info("Performing operation: " + operation);
		System.out.println("Performing operation: " + operation);
		
		int ret = RETURN_SUCCESS;
		
		try {
			File sourceFile = null;
			Bag bag = null;
			if (config.contains(OPTION_VERSION)) {
				Version version = Version.valueOfString(config.getString(OPTION_VERSION));
				if (config.contains(OPTION_SOURCE)) {
					sourceFile = new File(config.getString(OPTION_SOURCE));
					bag = BagFactory.createBag(sourceFile, version);
				} else {
					bag = BagFactory.createBag(version);
				}
			} else {
				if (config.contains(OPTION_SOURCE)) {
					sourceFile = new File(config.getString(OPTION_SOURCE));
					bag = BagFactory.createBag(sourceFile);
				} else {
					bag = BagFactory.createBag();
				}
			}
			File destFile = null;
			if (config.contains(OPTION_DESTINATION)) {				
				destFile = new File(config.getString(OPTION_DESTINATION));
			}

			BagWriter writer = null;
			if (VALUE_WRITER_FILESYSTEM.equals(config.getString(OPTION_WRITER))) {
				writer = new FileSystemBagWriter(destFile, true);
			} else if (VALUE_WRITER_ZIP.equals(config.getString(OPTION_WRITER))) {
				writer = new ZipBagWriter(destFile);
			} else if (VALUE_WRITER_TAR.equals(config.getString(OPTION_WRITER))) {
				writer = new TarBagWriter(destFile);
			}
			
			DefaultCompletionStrategy strategy = new DefaultCompletionStrategy();
			strategy.setGenerateBagInfoTxt(! config.getBoolean(OPTION_EXCLUDE_BAG_INFO, false));
			strategy.setUpdateBaggingDate(! config.getBoolean(OPTION_NO_UPDATE_BAGGING_DATE, false));
			strategy.setUpdateBagSize(! config.getBoolean(OPTION_NO_UPDATE_BAG_SIZE, false));
			strategy.setUpdatePayloadOxum(! config.getBoolean(OPTION_NO_UPDATE_PAYLOAD_OXUM, false));
			strategy.setGenerateTagManifest(! config.getBoolean(OPTION_EXCLUDE_TAG_MANIFEST, false));
			strategy.setTagManifestAlgorithm(Algorithm.valueOfBagItAlgorithm(config.getString(OPTION_TAG_MANIFEST_ALGORITHM, Algorithm.MD5.bagItAlgorithm)));
			strategy.setPayloadManifestAlgorithm(Algorithm.valueOfBagItAlgorithm(config.getString(OPTION_PAYLOAD_MANIFEST_ALGORITHM, Algorithm.MD5.bagItAlgorithm)));
			
			if (OPERATION_ISVALID.equals(operation)) {				
				SimpleResult result = bag.isValid(config.getBoolean(OPTION_MISSING_BAGIT_TOLERANT, false));
				System.out.println(result.toString());
				if (! result.isSuccess()) {
					ret = RETURN_FAILURE;
				}
			} else if (OPERATION_ISCOMPLETE.equals(operation)) {				
				SimpleResult result = bag.isComplete(config.getBoolean(OPTION_MISSING_BAGIT_TOLERANT, false));
				System.out.println(result.toString());
				if (! result.isSuccess()) {
					ret = RETURN_FAILURE;
				}
			} else if (OPERATION_WRITE.equals(operation)) {								
				bag.write(writer);
			} else if (OPERATION_COMPLETE.equals(operation)) {
				bag.complete(strategy);
				bag.write(writer);
			} else if (OPERATION_CREATE.equals(operation)) {
				for(File file : config.getFileArray(OPTION_PAYLOAD)) {
					bag.addPayload(file);
				}
				bag.complete(strategy);
				bag.write(writer);				

			} else if (OPERATION_MAKE_HOLEY.equals(operation)) {
				bag.makeHoley(config.getString(OPTION_BASE_URL), config.getBoolean(OPTION_EXCLUDE_PAYLOAD_DIR, false));
				bag.write(writer);
			} else if (OPERATION_GENERATE_PAYLOAD_OXUM.equals(operation)) {
				String oxum = BagHelper.generatePayloadOxum(bag);
				System.out.println("Payload-Oxum: " + oxum);
			} else if (OPERATION_CHECK_PAYLOAD_OXUM.equals(operation)) {
				String genOxum = BagHelper.generatePayloadOxum(bag);
				BagInfoTxt bagInfo = bag.getBagInfoTxt();
				if (bagInfo == null) {
					System.out.println("Bag does not contain bag-info.txt.");
					ret = RETURN_ERROR;
				} else {
					String checkOxum = bagInfo.getPayloadOxum();
					if (checkOxum == null) {
						System.out.println("bag-info.txt does not contain Payload-Oxum.");
						ret = RETURN_ERROR;
					} else {
						if (checkOxum.equals(genOxum)) {
							System.out.println("Payload-Oxum matches.");
						} else {
							System.out.println("Payload-Oxum does not match.");
							ret = RETURN_FAILURE;
						}
					}
				}
				
			}
		}
		catch(Exception ex) {
			log.error("Error: " + ex.getMessage(), ex);
			System.out.println("Error: " + ex.getMessage());
			return RETURN_ERROR;
		}
		log.info("Operation completed.");
		System.out.println("Operation completed.");
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
	
	
	private static void printUsage(Collection<String> operations) {
		System.out.println("Usage: bag <operation> [--help]");
		System.out.println("\t<operation>");
		System.out.print("\t\tValid operations are: ");
		String[] operationArray = operations.toArray(new String[] {});
		for(int i=0; i<operationArray.length; i++) {
			System.out.print(operationArray[i]);
			if (i == operationArray.length-2) {
				System.out.print(" and ");
			} else if (i == operationArray.length-1) {
				System.out.println(".");
			} else {
				System.out.print(", ");
			}
			
		}
		System.out.println("\t[--help]");
		System.out.println("\t\tPrints help message for the operation.");
	}
}
