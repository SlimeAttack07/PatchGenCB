package slimeattack07.patchgencb;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ResetTextHandler {

	public static void execute() {
		if(Utils.displayYesNo("PatchGen: Reset text file", "Would you like to clear the text.json file to start fresh for the next patch?")) {
			File file = Utils.requestFile("data", "text", "json");
			
			if(file.exists())
				try (FileWriter fw = new FileWriter(file);){
					// Nothing to do here, opening file will reset it for us.
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

}
