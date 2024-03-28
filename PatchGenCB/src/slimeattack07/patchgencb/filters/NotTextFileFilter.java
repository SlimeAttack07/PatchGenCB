package slimeattack07.patchgencb.filters;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;



public class NotTextFileFilter implements FileFilter {
	private final ArrayList<String> BANNED = new ArrayList<>(Arrays.asList("txt", "md", "html", "css", "json"));

	@Override
	public boolean accept(File f) {
		if(f.isDirectory())
			return false;
		String[] parts = f.getName().split("\\.");
		
		return !BANNED.contains(parts[parts.length - 1]);
	}
}
