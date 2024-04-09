package slimeattack07.patchgencb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import slimeattack07.patchgencb.filters.DirectoryFileFilter;
import slimeattack07.patchgencb.filters.NotTextFileFilter;

/**
 * Handler for the 'generate' button in the view screen. Handles generation of
 * patch notes as well as updating the monitored data.
 */
public class GenerateHandler {
	// TODO: Make log to expose what program is doing and if it has encountered
	// errors.
	public static void execute() {
		System.out.println(String.format("Running from directory '%s'", PatchGenCB.working_dir));

		File root = new File(PatchGenCB.working_dir + "/src");
		
		if(!root.isDirectory()) {
			System.out.println("This is not a folder! I can't start from here.");
			return;
		}
		
		processProject(root, true);

		Utils.displayInfo("Generate patch notes", String.format("Generated patch notes for project '%s'", PatchGenCB.working_dir));
	}

	/**
	 * Processes a project, generating patch notes.
	 * 
	 */
	private static JsonArray processProject(File dir, boolean is_root) {
		JsonArray data = new JsonArray();
		
		for (File file : dir.listFiles(new NotTextFileFilter())) {
			System.out.println(String.format("Found file %s", file.getName()));
			data.addAll(processFile(file));
		}
		
		for (File file : dir.listFiles(new DirectoryFileFilter())) {
//			System.out.println(String.format("Found dir %s", file.getName()));
			data.addAll(processProject(file, false));
		}
		
		if(is_root) {
			if (!data.isEmpty()) {
				JsonObject data_object = new JsonObject();
				data_object.add(PatchNoteData.DATA, data);
				createFiles(data_object);
			} else{
				System.out.println("Nothing changed!");
				Utils.displayWarning("Generate patch notes", "Failed to detect any changes.");
			}
		}
		
		return data;
	}

	/** TODO: Rewrite stuff here.
	 * Process annotations on a field.
	 * 
	 * @param category The category id to overwrite the field's category with. Will not overwrite if the empty String is provided.
	 * @return A JsonObject holding the data related to the field.
	 */
	private static JsonObject processAnnotations(Object value, ArrayList<AnnotationPair> annotations, String category) {
		JsonObject outer = new JsonObject();

		// Store value of field.
		if (value instanceof Number)
			outer.addProperty(PatchNoteData.VALUE, (Number) value);
		else if (value instanceof Boolean)
			outer.addProperty(PatchNoteData.VALUE, (Boolean) value);
		else
			outer.addProperty(PatchNoteData.VALUE, value.toString());
		
		for(AnnotationPair annotation : annotations) {
			System.out.println(String.format("   Annotation info: %s", annotation));
			// Store info like category and name (if provided).
			switch (annotation.getName()) {
			case PatchNoteData.ID:
				outer.addProperty(PatchNoteData.ID, annotation.getValue().toString());
				break;
			case PatchNoteData.CATEGORY:
				if(category.isBlank())
					outer.addProperty(PatchNoteData.CATEGORY, annotation.getValue().toString());
				
				break;
				
			case PatchNoteData.NAME:
				outer.addProperty(PatchNoteData.NAME, annotation.getValue().toString());
				break;
			case PatchNoteData.BULLETED:
				outer.addProperty(PatchNoteData.BULLETED, annotation.getValue().equals("true"));
				break;
			case "after": break;
			case "until": break;
			default:
				System.out.println(String.format("Unknown memberpair: %s = %s", annotation.getName(),
						annotation.getValue()));
				break;
			}
		}
			
		if(!category.isBlank())
			outer.addProperty(PatchNoteData.CATEGORY, category);
			
		System.out.println("Generated following JSON:");
		System.out.println(outer);
		return outer;
	}

	/**
	 * Generate JSON database.
	 * 
	 * @param project   Project to generate files for.
	 * @param result    The resulted text to put in the file.
	 * @param overwrite Whether an existing file should be overwritten.
	 */
	private static void createFiles(JsonObject result) {
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

			File file = Utils.requestUniqueFile("data", version, "json");

			if(file != null) {
				if(!file.exists()) {
					try {
						file.createNewFile();
					} catch (IOException | SecurityException e) {
						e.printStackTrace();
					}
				}
				
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
	private static void compareToVersion(String new_version) {
		// TODO: Add version check. Temporarily using System.in for testing.
		boolean accepted = false;
		File file_new = null;
		File file_old = null;
		String old_version = "";
		
		while(!accepted) {
			System.out.println("Comparison: Specify version to compare to:");
			old_version = Utils.displayNotBlankInput("Version input", "Specify version to compare to. Type 'cancel' to cancel.", "categories");

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
				Reader reader_old = new BufferedReader(new FileReader(file_old));) {
			Gson gson = new Gson();

			PatchNoteData data_new = gson.fromJson(reader_new, PatchNoteData.class);
			PatchNoteData data_old = gson.fromJson(reader_old, PatchNoteData.class);
			data_new.genNotes(data_old, old_version, new_version);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static JsonArray processFile(File f) {
		JsonArray results = new JsonArray();
		
		try(BufferedReader br = new BufferedReader(new FileReader(f))){
			String line = br.readLine();
			boolean ready = false;
			String category = "";
			ArrayList<AnnotationPair> ann_details = new ArrayList<>();
			
			while(line != null) {
				if(ready) {
					Object value = getValue(line, ann_details);
					results.add(processAnnotations(value, ann_details, category));
					ann_details.clear();
					ready = false;
				}
				else if(line.contains("@CategoryInfo(") && line.contains(")")) {
					String params = line.substring(line.indexOf("@CategoryInfo(") + 14, line.lastIndexOf(")"));
					params = params.replace(" ", "");
					String[] parts = params.split(",");
					
					for(String part : parts) {
						if(part.contains("id=\"")) {
							int start = part.indexOf("id=\"");
							category = part.substring(start + 4, part.indexOf("\"", start + 4));
						}
					}
				}
				else if(line.contains("@Watchable(") && line.contains(")")) {
					String params = line.substring(line.indexOf("@Watchable(") + 11, line.lastIndexOf(")"));
					// To use a comma inside quotes, it must be escaped.
					String replacement = "$$$PATCHGEN$$$";
					params = params.replace("\\,", replacement);
					
					String[] parts = params.split(",");
					
					for(String part : parts) {
						String[] components = part.replace(replacement, ",").split("=");
						
						if(components.length > 1)
							ann_details.add(new AnnotationPair(components[0].trim(), components[1].replace("\"", "").trim()));
					}
					
					ready = true;
				}
				
				line = br.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return results;
	}
	
	private static Object getValue(String line, ArrayList<AnnotationPair> anns) {
		String left = line;
		boolean modified = false;
		
		for(AnnotationPair pair : anns) {
			if(pair.isAfter()) {
				String[] parts = left.split(Pattern.quote(pair.getValue()));
				
				if(parts.length > 1) {
					left = parts[1];
					modified = true;
				}
			}
			else if(pair.isUntil()) {
				String[] parts = left.split(Pattern.quote(pair.getValue()));
				
				left = parts[0];
				modified = true;
			}
		}
		
		if(!modified) {
			String[] parts = left.split("=");
			
			if(parts.length > 1)
				left = parts[1];
		}
		
		left = left.trim();
		
		if(left.toLowerCase().equals("true"))
			return Boolean.TRUE;
		
		if(left.toLowerCase().equals("false"))
			return Boolean.FALSE;
		
		try {
			return Float.valueOf((Float.parseFloat(left)));
		} catch(NumberFormatException e) {
		}
		
		return left;
	}
	
	private static class AnnotationPair{
		private final String NAME;
		private final String VALUE;
		
		private AnnotationPair(String name, String value) {
			NAME = name;
			VALUE = value;
		}
		
		private String getName() {
			return NAME;
		}
		
		private String getValue() {
			return VALUE;
		}
		
		private boolean isAfter() {
			return NAME.equals("after");
		}
		
		private boolean isUntil() {
			return NAME.equals("until");
		}
		
		@Override
		public String toString() {
			return NAME + " = " + VALUE;
		}
	}
}
