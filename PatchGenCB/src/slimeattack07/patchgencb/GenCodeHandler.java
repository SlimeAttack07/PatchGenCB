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

	public static void execute() {
		System.out.println(String.format("Running from directory '%s'", PatchGenCB.working_dir));
		createFiles();
		Utils.displayError("PatchGen: Code generator", "Generated code.");
	}

	/** Generate files.
	 * 
	 */
	private static void createFiles() {
		File[] files = new File[4];
		files[0] = Utils.requestFile("annotations", "Watchable", "txt");
		files[1] = Utils.requestFile("annotations", "CategoryInfo", "txt");
		files[2] = Utils.requestFile("patchnotes", "basic", "css");
		files[3] = Utils.requestFile("patchnotes", "basic", "js");
		
		for(int i = 0; i < files.length; i++) {
			File file = files[i];
			
			if(file != null) {
				try (BufferedWriter bw = new BufferedWriter(new FileWriter(file));) {
					// TODO: Add version check If I decide to ever update this plugin.
					
					String code = "";
					
					switch(i) {
					case 0: code = Watchable.getCode(); break;
					case 1: code = CategoryInfo.getCode(); break;
					case 2: code = BasicStyle.getStyle(); break;
					default: code = BasicStyleJS.getScript(); break;
					}
					
					if (file.exists()) 
						bw.append(code);
					else if(file.createNewFile())
						bw.append(code);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
