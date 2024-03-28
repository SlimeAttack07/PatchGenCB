package slimeattack07.patchgencb;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/** Handler for the 'codegen' button in the view screen.
 * Handles generation of the files that developers can use to automate parts of the patch note generation process.
 * 
 */
public class GenCodeHandler {

	public void execute() {
		String dir = System.getProperty("user.dir");
		System.out.println(String.format("Running from directory '%s'", dir));
		createFiles();
		Utils.displayError("PatchGen: Code generator", "Generated code.");
	}

	/** Generate files.
	 * 
	 */
	private void createFiles() {
		File[] files = new File[4];
		files[0] = Utils.requestFile("annotations", "Watchable", "java");
		files[1] = Utils.requestFile("annotations", "CategoryInfo", "java");
		files[2] = Utils.requestFile("patchnotes", "basic", "css");
		files[3] = Utils.requestFile("patchnotes", "basic", "js");
		
		for(File file : files) {
			if(file != null) {
				try (BufferedWriter bw = new BufferedWriter(new FileWriter(file));) {
					// TODO: Add version check If I decide to ever update this plugin.
					if (file.exists()) 
						bw.append(Watchable.getCode());
					else if(file.createNewFile())
						bw.append(Watchable.getCode());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
