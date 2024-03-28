package slimeattack07.patchgencb.generators;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/** Abstract patch note generator.
 * 
 */
public abstract class AbstractPatchNoteGenerator {
	
	/** Add content to file.
	 * 
	 * @param file File to add content to.
	 * @param content Content to add.
	 */
	protected void addToFile(File file, String content) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));) {
			if(file.exists() && file.isFile()) {
				bw.append(content);
			}
			else
				if(file.createNewFile())
					bw.append(content);
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
	}
}
