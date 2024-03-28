package slimeattack07.patchgencb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Handler for the 'generate' button in the view screen. Handles generation of
 * patch notes as well as updating the monitored data.
 */
public class GenerateHandler {
	// TODO: Make log to expose what plugin is doing and if it has encountered
	// errors.
	public void execute() {
		String dir = System.getProperty("user.dir");
		System.out.println(String.format("Running from directory '%s'", dir));

		processProject();

		Utils.displayInfo("Generate patch notes", String.format("Generated patch notes for project '%s'", dir));
	}

	/**
	 * Processes a project, generating patch notes.
	 * 
	 */
	private void processProject() {
		JsonArray data = new JsonArray();

		// TODO: Make the code for reading stuff.
	}

	/** TODO: Rewrite stuff here.
	 * Process annotations on a field.
	 * 
	 * @param category The category id to overwrite the field's category with. Will not overwrite if the empty String is provided.
	 * @return A JsonObject holding the data related to the field.
	 */
	@Nullable
	private JsonObject processAnnotations(ArrayList<String> annotations, String category) {
		for (String ann : annotations) {
			if (ann.equals("Watchable")) { // TODO: Make these constants
				System.out.println(String.format("   Annotation info: %s", ann));
				JsonObject outer = new JsonObject();
				Object value = "todo";

				// Store value of field.
				if (value instanceof Number)
					outer.addProperty(PatchNoteData.VALUE, (Number) value);
				else if (value instanceof Boolean)
					outer.addProperty(PatchNoteData.VALUE, (Boolean) value);
				else
					outer.addProperty(PatchNoteData.VALUE, value.toString());
				/*
				// Store info like category and name (if provided).
				for (IMemberValuePair pair : ann.getMemberValuePairs()) {
					System.out.println(String.format("      Pair %s %s", pair.getMemberName(), pair.getValue()));

					switch (pair.getMemberName()) {
					case PatchNoteData.ID:
						outer.addProperty(PatchNoteData.ID, pair.getValue().toString());
						break;
					case PatchNoteData.CATEGORY:
						if(category.isBlank())
							outer.addProperty(PatchNoteData.CATEGORY, pair.getValue().toString());
						
						break;
						
					case PatchNoteData.NAME:
						outer.addProperty(PatchNoteData.NAME, pair.getValue().toString());
						break;
					case PatchNoteData.BULLETED:
						outer.addProperty(PatchNoteData.BULLETED, (boolean) pair.getValue());
						break;
					default:
						System.out.println(String.format("Unknown memberpair: %s = %s", pair.getMemberName(),
								pair.getValue()));
						break;
					}
				}
				*/
				if(!category.isBlank())
					outer.addProperty(PatchNoteData.CATEGORY, category);
					
				System.out.println("Generated following JSON:");
				System.out.println(outer);
				return outer;
			}
		}

		return null;
	}

	/**
	 * Generate JSON database.
	 * 
	 * @param project   Project to generate files for.
	 * @param result    The resulted text to put in the file.
	 * @param overwrite Whether an existing file should be overwritten.
	 */
	private void createFiles(JsonObject result) {
		// TODO: Make folder/file gen run on plugin load?
		// Check if patchgen folder exists, create if it doesn't exist.
		Utils.requestDir("", "patchgen");
		Utils.requestDir("patchgen", "data");

		// Check if data.json exists, create if it doesn't exist.
		
		boolean accepted = false;
		String version = "";
		
		while(!accepted) {
			System.out.println("DataGen: Specify version:");
			version = Utils.displayNotBlankInput("PatchGen: Version input", "Specify version name.", "categories");
			
			if(version.equals("NOTHING")) {
				Utils.displayWarning("PatchGen: Version input", "User canceled generation of patch notes.");
				return;
			}

			File file = Utils.requestUniqueFile("patchgen/data", version, "json");

			if(file != null) {
				try(BufferedWriter bw = new BufferedWriter(new FileWriter(file));){
					accepted = true;
					bw.append(result.toString());
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}

		compareToVersion(version);
	}

	/**
	 * Compare two versions. New version should be provided, old version will be
	 * requested from user by this method.
	 * 
	 * @param new_version The new version.
	 */
	private void compareToVersion(String new_version) {
		// TODO: Add version check. Temporarily using System.in for testing.
		boolean accepted = false;
		File file_new = null;
		File file_old = null;
		String old_version = "";
		
		while(!accepted) {
			System.out.println("Comparison: Specify version to compare to:");
			old_version = Utils.displayNotBlankInput("Version input", "Specify version to compare to. Enter 'cancel' to cancel.", "categories");

			// TODO: Add way to determine if other versions even exist to compare to.
			if (old_version.toLowerCase().equals("cancel")) {
				Utils.displayInfo("PatchGen: Comparison", "User canceled generation of release notes.");
				return;
			}

			file_new = Utils.requestFile("data", new_version, "json");
			file_old = Utils.requestFile("data", old_version, "json");

			if (file_new == null || !file_new.exists()) {
				System.out.println(String.format("File src/patchgen/data/%s.json does not exist", new_version));
				Utils.displayError("PatchGen: Comparison", String.format("File src/patchgen/data/%s.json does not exist", new_version));
				return;
			}

			if (file_old == null || !file_old.exists()) {
				System.out.println(String.format("File src/patchgen/data/%s.json does not exist", old_version));
				Utils.displayWarning("PatchGen: Comparison", String.format("File src/patchgen/data/%s.json does not exist", old_version));
			}
			else
				accepted =  true;
		}
		

		try ( // Auto-closes resources
				Reader reader_new = new BufferedReader(new FileReader(file_new));
				Reader reader_old = new BufferedReader(new FileReader(file_new));) {
			Gson gson = new Gson();

			PatchNoteData data_new = gson.fromJson(reader_new, PatchNoteData.class);
			PatchNoteData data_old = gson.fromJson(reader_old, PatchNoteData.class);
			data_new.genNotes(data_old, old_version, new_version);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
