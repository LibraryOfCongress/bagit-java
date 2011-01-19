package gov.loc.repository.bagit.transformer.impl;

public class SplitBagHelper {
	public static boolean isExcluded(String[] excludeDirs, String filePath){
		if(excludeDirs == null) {
			return false;
		}
		
		boolean isExcluded = false;
		for(String excludeDir : excludeDirs){
			if(! excludeDir.endsWith("/")){
				excludeDir = excludeDir.trim().toLowerCase() + "/";
			}
			if(! filePath.endsWith("/")){
				filePath = filePath.trim().toLowerCase() + "/";
			}
			if(filePath.toLowerCase().indexOf(excludeDir.toLowerCase().trim()) >= 0){
				isExcluded = true;
				break;
			}
		}
		return isExcluded;
	}
}
