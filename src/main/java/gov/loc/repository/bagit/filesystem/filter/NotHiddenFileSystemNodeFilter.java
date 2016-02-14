package gov.loc.repository.bagit.filesystem.filter;

import gov.loc.repository.bagit.filesystem.FileSystemNode;
import gov.loc.repository.bagit.filesystem.FileSystemNodeFilter;
import gov.loc.repository.bagit.filesystem.impl.AbstractFileNode;

/**
 * Created by daniels on 2/12/16.
 */
public class NotHiddenFileSystemNodeFilter implements FileSystemNodeFilter {

    @Override
    public boolean accept(FileSystemNode fileSystemNode) {
        if (fileSystemNode instanceof AbstractFileNode) {
            // File-based node, check if resource is hidden
            //
            AbstractFileNode fileNode = (AbstractFileNode) fileSystemNode;
            return  !fileNode.getFile().isHidden();
        }

        // Not a file-based node, accept without checking
        //
        return true;
    }
}
