package slimeattack07.patchgencb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import slimeattack07.patchgencb.generators.HtmlGenerator;
import slimeattack07.patchgencb.generators.MarkdownGenerator;
import slimeattack07.patchgencb.generators.PatchNoteGenerator;
import slimeattack07.patchgencb.generators.PlainTextGenerator;

/** Utility class for storing and handling data for patch note entries.
 * 
 */
public class PatchNoteData {
	private JsonArray data;
	
	public static final String ID = "id";
	public static final String VALUE = "value";
	public static final String NAME = "name";
	public static final String CATEGORY = "category";
	public static final String DATA = "data";
	public static final String META = "meta";
	public static final String BULLETED = "bulleted";
	public static final String DEVELOPER_COMMENT = "developer_comment";
	public static final String IS_TEXT = "is_text";
	
	/** Constructor.
	 * 
	 * @param data The initial data.
	 */
	public PatchNoteData(JsonArray data) {
		this.data = data;
	}
	
	/** Get the data stored in this instance.
	 * 
	 * @return The data.
	 */
	public JsonArray getData() {
		return data;
	}
	
	/** Ask the user what output generator should be used.
	 * 
	 * @param project The project to generate release notes for.
	 * @param name The name that will be given to the created file.
	 * @return
	 */
	@Nullable
	private PatchNoteGenerator askGenerator(String name) {
		String format = Utils.displayOutputVersionInput("PatchGen: Specify output format", "What should the output format be? Supported are 'txt', 'md' and 'html'.");
		switch(format) {
		case "txt": return new PlainTextGenerator(name);
		case "md": return new MarkdownGenerator(name);
		case "html": return new HtmlGenerator(name);
		default: return null;
		}
	}
	
	// TODO: Strategy would be as follow: Sort on (nested) categories first. If category priority unknown, ask user.
	// Save category info in file somewhere. Make method later for changing these afterwards.
	/** Generate patch notes. The instance this method is called for will serve as the newest data.
	 * 
	 * @param old_data The old data to compare to when searching for changes.
	 * @param old_version The old version.
	 * @param new_version The new version. Used together with old_version to determine the name of the generated result.
	 */
	public void genNotes(PatchNoteData old_data, String old_version, String new_version) {
		// Add the text entries to the data so we can also generate them.
		JsonArray text_array = retrieveText();
		
		if(text_array != null)
			data.addAll(text_array);
		
		CategoryData cats = saveCategories(); // Ensure categories are up-to-date
		
		if(cats == null) {
			System.out.println("Failed to update categories");
			return;
		}
		
		// Sort per category to make generation easier.
		sortData(cats);
		System.out.println("Data is now: " + data.toString());
		
		PatchNoteGenerator gen = askGenerator(String.format("%s_to_%s", old_version, new_version));
		
		if(!gen.isValid()) {
			System.out.println("Unable to generate patch notes.");
			Utils.displayError("PatchGen: Generate patch notes", "Unable to generate patch notes.");
			return;
		}
		
		String last_category = "";
		int last_depth = 0;
		
		for(JsonElement element : data) {
			if(element.isJsonObject()) {
				JsonObject entry = element.getAsJsonObject();
				
				if(!entry.has(ID)) {
					System.out.println(String.format("Invalid entry: %s", entry));
					continue;
				}
				
				String id = entry.get(ID).getAsString();
				
				if(entry.has(IS_TEXT) && entry.get(IS_TEXT).getAsBoolean()) {
					String value = entry.has(VALUE) ? entry.get(VALUE).getAsString() : "NOTEXT";
					boolean is_developer_comment = entry.has(DEVELOPER_COMMENT) ? entry.get(DEVELOPER_COMMENT).getAsBoolean() : false;
					
					// Generate needed categories that haven't been generated yet.
					if(entry.has(CATEGORY)) {
						String cat = entry.get(CATEGORY).getAsString();
						
						if(!cat.equals(last_category)) {
							last_depth = (int) cat.chars().filter(ch -> ch == '.').count();
							cats = genCategories(gen, cats, cat);
							last_category = cat;
						}
					}
					
					gen.addText(value, last_depth, is_developer_comment);
				}
				else if (old_data.contains(id)) {
					JsonObject match = old_data.get(id);
					String text = compareValues(entry, match);
						
					if(!text.isBlank()) {
						// Generate needed categories that haven't been generated yet.
						if(entry.has(CATEGORY)) {
							String cat = entry.get(CATEGORY).getAsString();
							
							if(!cat.equals(last_category)) {
								last_depth = (int) cat.chars().filter(ch -> ch == '.').count();
								cats = genCategories(gen, cats, cat);
								last_category = cat;
							}
						}
						
						boolean bulleted = entry.has(PatchNoteData.BULLETED) ? 
								entry.get(PatchNoteData.BULLETED).getAsBoolean() : true;
						gen.addContent(text, last_depth, bulleted);
					}
				}
			}
		}
		
		gen.finish();
		
		if(Utils.displayYesNo("PatchGen: Reset text file", "Would you like to clear the text.json file to start fresh for the next patch? You can always use the 'ResetText' button to do this at any time yourself.")) {
			File file = Utils.requestFile("data", "text", "json");
			
			if(file.exists())
				try (FileWriter fw = new FileWriter(file)){
					// Nothing to do here, just opening it in the try block is enough to reset the file. 
				} catch (IOException e) {
					e.printStackTrace();
				}
		}		
	}
	
	/** Generating categories in the patch notes.
	 * 
	 * @param gen The patch note generator to use.
	 * @param cats The category data containing the categories to generate.
	 * @param id The category to generate. This method will generate any parent categories for this id that have not
	 * been generated yet.
	 * @return The remaining category data with the generated entries removed.
	 */
	private CategoryData genCategories(PatchNoteGenerator gen, CategoryData cats, String id) {
		String[] parts = id.split("\\.");
		String temp = "";
		int depth = 0;
		
		for(String part : parts) {
			temp += part;
			
			// Contains check needed to avoid generating categories that already exist.
			if(cats.contains(temp)) {
				System.out.println(String.format("Generating category: %s", temp));
				gen.addCategory(cats.getName(temp), depth);
				cats.remove(temp); // Only need to generate each category once, so should be safe to remove.
			}
			
			temp += ".";
			depth++;
		}
		
		return cats;
	}
	
	/** Save all detected categories to file. Will ask the user to provide a name and priority for any categories that 
	 * were not saved to file before.
	 * 
	 * @return The detected category data.
	 */
	@Nullable
	private CategoryData saveCategories() {
		File file = Utils.requestFile("data", "categories", "json");
		
		if(file == null)
			return null;
		
		CategoryData cats = new CategoryData(new JsonArray());
		
		if(file.exists()) {
			try(Reader reader = new BufferedReader(new FileReader(file));){
				Gson gson = new Gson();
				cats = gson.fromJson(reader, CategoryData.class);
			} catch (IOException | JsonSyntaxException | JsonIOException e) {
				e.printStackTrace();
			}
		}
		else {
			try {
				file.createNewFile();
			} catch (IOException | SecurityException e) {
			}
		}
		
		cats = saveCategoryHelper(cats, data);
		
		JsonObject data_object = new JsonObject();
		data_object.add(DATA, cats.getData());
		
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))){
			if(file.exists())
				bw.append(data_object.toString());
			else if(file.createNewFile())
				bw.append(data_object.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return cats;
	}
	
	/** Helper method for saving category data. Method will ask user to provide name and priority for any categories for which
	 * these are not known yet.
	 * 
	 * @param categories The known category data.
	 * @param data_array The data array to process.
	 * @return The updated category data.
	 */
	private CategoryData saveCategoryHelper(CategoryData categories, JsonArray data_array) {
		CategoryData cats = categories;
		
		for(JsonElement element : data_array) {
			if(element.isJsonObject()) {
				JsonObject jo = element.getAsJsonObject();
				
				if(jo.has(CATEGORY)) {
					String cat = jo.get(CATEGORY).getAsString();
					String temp = "";
					
					for(String part : cat.split("\\.")) {
						temp += part;
						
						if(!cats.contains(temp)) {
							System.out.println(String.format("Asking for name for category '%s'", temp));
							String name = Utils.displayNotBlankInput("Category definition", 
									String.format("Please specify name for category '%s'", temp));
							System.out.println(String.format("Asking for priority for category '%s'", temp));
							int prio = Utils.displayPositiveIntInput("Category definition", 
									String.format("Please specify priority for category '%s'", temp));
							cats.addCategory(temp, name, prio);
						}
						
						temp += ".";
					}
				}
			}
		}
		
		return cats;
	}
	
	// TODO: Add way to clear text
	/** Retrieve text written by developers.
	 * 
	 * @return The text data, or null if none is found.
	 */
	@Nullable
	private JsonArray retrieveText() {
		File file = Utils.requestFile("data", "text", "json");
		
		if(file.exists()) {
			try(Reader reader = new BufferedReader(new FileReader(file));){
				Gson gson = new Gson();
				PatchNoteData data = gson.fromJson(reader, PatchNoteData.class);
				return data.getData();
			} catch (IOException | JsonSyntaxException | JsonIOException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	// TODO: Change system.out to log file and patch note generation.
	/** Compares values between two entries.
	 * 
	 * @param entry The entry to compare with match.
	 * @param match The old version of the entry.
	 * @return The generated String detailing the detected change, or an empty String if no change was detected.
	 */
	private String compareValues(JsonObject entry, JsonObject match) {
		JsonElement old_value = match.get(VALUE);
		JsonElement new_value = entry.get(VALUE);
				
		if(old_value != null && new_value != null) {
			if(old_value.getAsJsonPrimitive().isBoolean() && new_value.getAsJsonPrimitive().isBoolean()) {
				if(old_value.getAsBoolean() != new_value.getAsBoolean()) {
					String name = getNameOrId(entry);
					String text = String.format("%s changed from %s to %s", name, old_value.getAsBoolean(), new_value.getAsBoolean());
					System.out.println(text);
					return text;
				}
			}
			else if(old_value.getAsJsonPrimitive().isNumber() && new_value.getAsJsonPrimitive().isNumber()) {
				float old_number = old_value.getAsNumber().floatValue();
				float new_number = new_value.getAsNumber().floatValue();
				
				String change = old_number > new_number ? "decreased" : "increased";
				
				if(old_number != new_number) {
					String name = getNameOrId(entry);
					String text = String.format("%s %s from %s to %s", name, change, old_value.getAsNumber(), new_value.getAsNumber());
					System.out.println(text);
					return text;
				}
			}
			else {
				if(!old_value.getAsString().equals(new_value.getAsString())) {
					String name = getNameOrId(entry);
					String text = String.format("%s changed from '%s' to '%s'", name, old_value.getAsString(), new_value.getAsString());
					System.out.println(text);
					return text;
				}
			}
		}
		
		return "";
	}
	
	/** Get the name of an entry, or the id if no name is present.
	 * 
	 * @param entry The entry to return the name for.
	 * @return The name if present, the id otherwise.
	 */
	private String getNameOrId(JsonObject entry) {
		if(entry.has(NAME))
			return entry.get(NAME).getAsString();
		
		return entry.get(ID).getAsString();
	}
	
	/** Check if the data contains an entry with the specified id.
	 * 
	 * @param id The id to check for.
	 * @return True if id is contained in data, false otherwise.
	 */
	public boolean contains(String id) {
		for(JsonElement element : data) {
			if(element.isJsonObject()) {
				JsonObject entry = element.getAsJsonObject();
				
				if(entry.has(ID) && entry.get(ID).getAsString().equals(id))
					return true;
			}
		}
		
		return false;
	}
	
	/** Get JsonObject by id.
	 * 
	 * @param id Id of the object.
	 * @return The object with the given id, or null if no such object exists.
	 */
	@Nullable
	public JsonObject get(String id) {
		for(JsonElement element : data) {
			if(element.isJsonObject()) {
				JsonObject entry = element.getAsJsonObject();
				
				if(entry.has(ID) && entry.get(ID).getAsString().equals(id))
					return entry;
			}
		}
		
		return null;
	}

	/** Sorts the data in this entry to enable easier handling of category generation.
	 * 
	 * @param cats The category data to use for sorting.
	 */
	public void sortData(CategoryData cats) {
		System.out.println("Sorting data: " + data);
		List<JsonElement> list = data.asList();
		Collections.sort(list, new CategoryComparator(cats));
		Gson gson = new Gson();
		data = gson.toJsonTree(list).getAsJsonArray();
	}
	
	/** Comparator class for sorting with category data.
	 * 
	 */
	private class CategoryComparator implements Comparator<JsonElement>{
		private CategoryData cats;
		
		/** Constructor.
		 * 
		 * @param cats The category data to use.
		 */
		public CategoryComparator(CategoryData cats) {
			this.cats = cats;
		}
		
		/** Get the priority of a given category id.
		 * 
		 * @param id The id of the category.
		 * @return The priority of the category, or -1 of no such category exists.
		 */
		private int getPrio(String id) {
			if (cats.contains(id)){
				return cats.getPriority(id);
			}
			
			return -1;
		}
		
		/** Get category id from a JsonObject.
		 * 
		 * @param jo The object to get the category id from.
		 * @return The category id, or the empty String if no category id is present.
		 */
		private String getCat(JsonObject jo) {
			if (jo.has(CATEGORY))
				return jo.get(CATEGORY).getAsString();
			
			return "";
		}
		
		/** Get display name from a JsonObject.
		 * 
		 * @param jo The object to get the display name from.
		 * @return The display name, or the empty String if no category id is present.
		 */
		private String getName(JsonObject jo) {
			if (jo.has(NAME)){
				return jo.get(NAME).getAsString();
			}
			
			return "";
		}
		
		@Override
		public int compare(JsonElement o1, JsonElement o2) {
			if(o1.isJsonObject() && o2.isJsonObject()) {
				JsonObject jo1 = o1.getAsJsonObject();
				JsonObject jo2 = o2.getAsJsonObject();
				String cat1 = getCat(jo1);
				String cat2 = getCat(jo2);
				
				// If categories are equal, than do lexicographically
				if(cat1.equals(cat2)) {
					// Text is more important.
					boolean is_jo1_text = jo1.has(IS_TEXT) ? jo1.get(IS_TEXT).getAsBoolean() : false;
					boolean is_jo2_text = jo2.has(IS_TEXT) ? jo2.get(IS_TEXT).getAsBoolean() : false;
					
					if(is_jo1_text && !is_jo2_text)
						return -1;
					
					if(!is_jo1_text && is_jo2_text)
						return 1;
					
					// If both text, developer comments are lower priority than regular text.
					if(is_jo1_text && is_jo2_text) {
						boolean is_jo1_devcom = jo1.has(DEVELOPER_COMMENT) ? jo1.get(DEVELOPER_COMMENT).getAsBoolean() : false;
						boolean is_jo2_devcom = jo2.has(DEVELOPER_COMMENT) ? jo2.get(DEVELOPER_COMMENT).getAsBoolean() : false;
						
						if(is_jo1_devcom && !is_jo2_devcom)
							return 1;
						
						if(!is_jo1_devcom && is_jo2_devcom)
							return -1;
					}
					
					return getName(jo1).compareToIgnoreCase(getName(jo2));
				}
				
				// Cat1 is sub-category of cat2, so it should come later
				if(cat1.startsWith(cat2))
					return 1;
				
				// Cat 1 is parent category of cat2, so it should come earlier
				if(cat2.startsWith(cat1))
					return -1;
				
				// Objects belong to different categories, so find out where they differ
				String[] parts1 = cat1.split("\\.");
				String[] parts2 = cat2.split("\\.");
				String temp1 = "";
				String temp2 = "";
				
				int lowest = parts1.length > parts2.length ? parts2.length : parts1.length;
				
				for(int i = 0; i < lowest; i++) {
					temp1 += parts1[i];
					temp2 += parts2[i];
					
					// If id mismatches at this point, then compare priority
					if(!temp1.equals(temp2)) {
						int prio1 = getPrio(temp1);
						int prio2 = getPrio(temp2);
						int result = Integer.compare(prio1, prio2);
						
						if(result != 0)
							return result;
						
						return cats.getName(temp1).compareToIgnoreCase(cats.getName(temp2));
					}
					
					temp1 += ".";
					temp2 += ".";
				}
			}
			
			// Should only happen if users manually break the patch note data, in which case all bets are off.
			// If nothing messed with category data, this statement should never be reached as all cases are covered above.
			return -1; 
		}
	}
}
