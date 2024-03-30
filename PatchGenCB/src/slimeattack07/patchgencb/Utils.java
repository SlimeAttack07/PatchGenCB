package slimeattack07.patchgencb;

import java.awt.HeadlessException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JOptionPane;

/** Utility class with convenience methods used by other classes.
 * 
 */
public class Utils {
	/** Request a unique file to dump data in. If file already exists, user will be asked if it can be overwritten.
	 * 
	 * @param dir The patchgen subdirectory to put the file in.
	 * @param name The name of the file.
	 * @param extension The extension of the file.
	 * @return The file to dump data in, or null if an error occurred or user refuses to overwrite.
	 */
	@Nullable
	public static File requestUniqueFile(String dir, String name, String extension) {
		File file = requestFile(dir, name, extension);
		
		if(file.exists()) {
			boolean overwrite = Utils.displayYesNo("PatchGen: File Request", 
					String.format("File 'patchgen/%s/%s.%s' already exists. Would you like to overwrite it?", dir, name, extension));
			
			// Clear contents if overwrite is allowed.
			if(overwrite) {
				try (FileWriter fw = new FileWriter(file)){
					// Will open in non-append so clears contents without having to do anything.
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				return file;
			}
			
			return null;
		}
		else {
			try {
				file.createNewFile();
			} catch(IOException | SecurityException e) {
			}
		}
		
		return file;
	}
	
	/** Request a file to dump data in.
	 * 
	 * @param dir The patchgen subdirectory to put the file in.
	 * @param name The name of the file.
	 * @param extension The extension of the file.
	 * @return The file to dump data in, or null if an error occurred
	 */
	@Nullable
	public static File requestFile(String dir, String name, String extension) {
		try {
			// TODO: Make folder/file gen run on plugin load?
			// Check if patchgen folder exists, create if it doesn't exist.
		
			File folder_patchgen = new File(PatchGenCB.working_dir, "src/patchgen");

			if (!folder_patchgen.exists())
				folder_patchgen.mkdir();

			File folder_dir = new File(PatchGenCB.working_dir, String.format("src/patchgen/%s", dir));

			// Check if patchgen/dir folder exists, create if it doesn't exist.
			if (!folder_dir.exists())
				folder_dir.mkdir();
			
			String real_name = (name == null || name.isBlank()) ? "NONAMEPROVIDED" : name;
			
			File file = new File(PatchGenCB.working_dir, String.format("src/patchgen/%s/%s.%s", dir, real_name, extension));
			return file;
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/** Request to make a new directory.
	 * 
	 * @param dir The parent directory to put the file in.
	 * @param name The name of the file.
	 * @return The created directory. If directory already exists, nothing will have changed.
	 */
	@Nullable
	public static File requestDir(String dir, String name) {
		try {
			// TODO: Make folder/file gen run on plugin load?
			// Check if patchgen folder exists, create if it doesn't exist.
		
			File folder_parent = new File(PatchGenCB.working_dir, String.format("src/%s", dir));

			if (!folder_parent.exists())
				folder_parent.mkdir();

			File folder_dir = new File(PatchGenCB.working_dir, String.format("src/%s/%s", dir, name));

			// Check if patchgen/dir folder exists, create if it doesn't exist.
			if (!folder_dir.exists())
				folder_dir.mkdir();
			
			return folder_dir;
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/** Display error to user.
	 * 
	 * @param title Title of the display window.
	 * @param message The message in the display window.
	 */
	public static void displayError(String title, String message) {
		try {
		JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
		} catch(HeadlessException e) {
			System.out.println("Encountered error displaying error:");
			e.printStackTrace();
		}
	}
	
	/** Display warning to user.
	 * 
	 * @param title Title of the display window.
	 * @param message The message in the display window.
	 */
	public static void displayWarning(String title, String message) {
		try {
			JOptionPane.showMessageDialog(null, message, title, JOptionPane.WARNING_MESSAGE);
			} catch(HeadlessException e) {
				System.out.println("Encountered error displaying warning:");
				e.printStackTrace();
			}
	}
	
	/** Display information to user.
	 * 
	 * @param title Title of the display window.
	 * @param message The message in the display window.
	 */
	public static void displayInfo(String title, String message) {
		try {
			JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
			} catch(HeadlessException e) {
				System.out.println("Encountered error displaying info:");
				e.printStackTrace();
			}
	}
	
	/** Display yes/no question to user.
	 * 
	 * @param title Title of the display window.
	 * @param message The message in the display window.
	 */
	public static boolean displayYesNo(String title, String message) {
		try {
		return JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
		} catch(HeadlessException e) {
			System.out.println("Encountered error displaying info:");
			e.printStackTrace();
		}
		
		return false;
	}

	
	/** Display a window requesting user for a positive integer. If the user presses 'Cancel', then -1 is returned.
	 * 
	 * @param title The title of the display window.
	 * @param message The message in the display window.
	 * @return The input positive integer, or -1 if user pressed 'Cancel'.
	 */
	public static int displayPositiveIntInput(String title, String message) {
		int input_value = -1; // Cancel will default to -1.
		
		try {
			while(input_value < 0) {
				String input = JOptionPane.showInputDialog(null, message, title, JOptionPane.PLAIN_MESSAGE);
				String feedback = isValidPosInt(input);
				
				if(feedback == null)
					input_value = toInt(input);
				else
					displayError("PatchGen: Positive integer request", feedback);
			}
		} catch(IllegalStateException | HeadlessException e) {
			System.out.println("Encountered error displaying input:");
			e.printStackTrace();
		}
		
		return input_value;
	}
	
	/** Display a window requesting user for non-empty input. If the user presses 'Cancel', then "NOTHING" is returned.
	 * 
	 * @param title The title of the display window.
	 * @param message The message in the display window.
	 * @param banned The Strings that are not allowed as input.
	 * @return The input text, or "NOTHING" if user pressed 'Cancel'.
	 */
	public static String displayNotBlankInput(String title, String message, String... banned) {
		String input_value = "NOTHING"; // Cancel will default to "NOTHING".
		
		try {			
			while(input_value.equals("NOTHING")) {
				String input = JOptionPane.showInputDialog(null, message, title, JOptionPane.PLAIN_MESSAGE);
				String feedback = isValidNotBlank(new ArrayList<>(Arrays.asList(banned)), input);
				
				if(feedback == null)
					input_value = input;
				else
					displayError("PatchGen: Non-blank input request", feedback);
			}
		} catch(HeadlessException e) {
			System.out.println("Encountered error displaying input:");
			e.printStackTrace();
		}
		
		return input_value;
	}
	
	/** Display a window requesting user for output format. If the user presses 'Cancel', then "NOTHING" is returned.
	 * 
	 * @param title The title of the display window.
	 * @param message The message in the display window.,\
	 * @return The input text, or "NOTHING" if user pressed 'Cancel'.
	 */
	public static String displayOutputVersionInput(String title, String message) {
		String input_value = "NOTHING"; // Cancel will default to NOTHING"".
		
		try {
			String[] options = {"txt", "md", "html"};
			
			return JOptionPane.showInputDialog(null, message, title, JOptionPane.PLAIN_MESSAGE, null, options, "txt").toString();
		} catch(HeadlessException e) {
			System.out.println("Encountered error displaying input:");
			e.printStackTrace();
		}
		
		return input_value;
	}
	
	/** Convert a String to an integer. Returns -1 if the input String is not an integer.
	 * 
	 * @param s The String to convert.
	 * @return The String as an integer, or -1 if String is not an integer.
	 */
	public static int toInt(String s) {
		try {
			return Integer.parseInt(s);
		} catch(NumberFormatException e) {}
		
		return -1;
	}
	
	/** Validator for positive integers.
	 * 
	 * @param new_text The new text that the user has input.
	 * @return Null if the input is valid, an error message otherwise.
	 */
	@Nullable
	public static String isValidPosInt(String new_text) {
		try {
			int i = Integer.parseInt(new_text);
			return i >= 0 ? null : "Integer must be positive (>= 0).";
		} catch(NumberFormatException e) {}

		return "Must be a positive integer.";
	}
	
	/** Validator for non-blank input.
	 * Also supports banning certain input.
	 * 
	 * @param illegal An ArrayList of banned input.
	 * @param new_text The new text that the user has input.
	 * @return Null if the input is valid, an error message otherwise.
	 */
	@Nullable
	public static String isValidNotBlank(ArrayList<String> illegal, String newText) {
		return newText == null || newText.isBlank() ? "Can't be blank" : 
			illegal.contains(newText) ? String.format("'%s' is not permitted", newText) : null;
	}
}
