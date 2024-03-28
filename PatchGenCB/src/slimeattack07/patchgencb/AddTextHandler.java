package slimeattack07.patchgencb;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

public class AddTextHandler {
	public void execute() {
		//TODO: Check id
		
		String category = Utils.displayNotBlankInput("PatchGen: Add Text", "What category should this text be placed in?");
		
		if(category.equals("NOTHING")) {
			Utils.displayWarning("PatchGen: Add Text", "User cancelled addition of text.");
			return;
		}
		
		boolean is_developer_comment = Utils.displayYesNo("PatchGen: Add Text", "Should this be marked as a developer comment?");
		
		// TODO: Add support for formatting like bold, italics, etcetera.
		String text = Utils.displayNotBlankInput("PatchGen: Add Text", "Please add your text");
		
		if(text.equals("NOTHING")) {
			Utils.displayWarning("PatchGen: Add Text", "User cancelled addition of text.");
			return;
		}
		
		String id = Utils.displayNotBlankInput("PatchGen: Add Text", "Please assign an id to this text."
				+ "This will help you determine whether another developer already added text for the change you are "
				+ "about to describe.");
		
		if(id.equals("NOTHING")) {
			Utils.displayWarning("PatchGen: Add Text", "User cancelled addition of text.");
			return;
		}
		
		if(processText(id, category, is_developer_comment, text))
			Utils.displayInfo("PatchGen: Add Text", "Added text to file.");
		
		return;
	}

	/** Process text
	 * 
	 * @param id The id of the text.
	 * @param category The category the text should be placed under.
	 * @param devcom Whether this should be marked as developer comments.
	 * @param text The text to process.
	 * @return True if processing was successful, false otherwise.
	 */
	private boolean processText(String id, String category, boolean devcom, String text) {
		File file = Utils.requestFile("data", "text", ".json");
		JsonArray arr = new JsonArray();
		
		if(file.exists()) {
			try ( // Auto-closes resources
					Reader reader = new BufferedReader(new FileReader(file));) {
				Gson gson = new Gson();

				PatchNoteData data = gson.fromJson(reader, PatchNoteData.class);
				
				if(data.contains(id)) {
					boolean accepted = Utils.displayYesNo("PatchGen: Add Text", String.format(
							"Id '%s' already exists. Would you like to overwrite it? It currently says the following: \"%s\"",
							id, data.get(id).get(PatchNoteData.VALUE)));
					if(accepted)
						arr.remove(data.get(id));
					else {
						Utils.displayWarning("PatchGen: Add Text", "User canceled addition of text.");
						return false;
					}
				}
				
				arr = data.getData();				
			} catch (IOException | JsonSyntaxException | JsonIOException e) {
				e.printStackTrace();
			}
		}
		
		JsonObject entry = new JsonObject();
		entry.addProperty(PatchNoteData.ID, id);
		entry.addProperty(PatchNoteData.CATEGORY, category);
		entry.addProperty(PatchNoteData.DEVELOPER_COMMENT, devcom);
		entry.addProperty(PatchNoteData.IS_TEXT, true);
		entry.addProperty(PatchNoteData.VALUE, text);
		
		arr.add(entry);
		JsonObject data = new JsonObject();
		data.add(PatchNoteData.DATA, arr);
		
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file));) {
			if(file.exists())
				bw.append(data.toString());
			else if(file.createNewFile())
				bw.append(data.toString());
			
			return true;
		} catch (IOException e) {
			Utils.displayError("PatchGen: Add Text", "Failed to create/modify file.");
		}
		
		return false;
	}
}
