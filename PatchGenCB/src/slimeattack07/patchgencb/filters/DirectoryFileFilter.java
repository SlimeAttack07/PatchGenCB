package slimeattack07.patchgencb.filters;

import java.io.File;
import java.io.FileFilter;


public class DirectoryFileFilter implements FileFilter {
	
	@Override
	public boolean accept(File f) {
		return f.isDirectory() && !f.getName().contains("patchgen");
	}
}
