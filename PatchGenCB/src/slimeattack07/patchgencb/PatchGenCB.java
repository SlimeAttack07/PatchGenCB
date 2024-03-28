package slimeattack07.patchgencb;

/** Main class
 * 
 */
public class PatchGenCB {
	public static String working_dir = System.getProperty("user.dir");

	public static void main(String[] args) {
		for(String arg : args) {
			if(arg.startsWith("--dir")) {
				String[] parts = arg.split("=");
				
				if(parts.length == 2)
					working_dir = parts[1].replaceAll("\"", "");
			}
			
			else if(arg.toLowerCase().equals("--gencode"))
				GenCodeHandler.execute();
			else if(arg.toLowerCase().equals("--addtext"))
				AddTextHandler.execute();
			else if (arg.toLowerCase().equals("--resettext"))
				ResetTextHandler.execute();
			else if(arg.toLowerCase().equals("--generate"))
				GenerateHandler.execute();
			else if(arg.toLowerCase().equals("--help")) {
				System.out.println("Use --help to print these help messages.");
				System.out.println("Use --dir=\"path\\to\\dir\" to set a directory to work from. If left out, the current directory is used.");
				System.out.println("Use --genCode to generate code needed for this program to work. It also produces some helpful documentation.");
				System.out.println("Use --addText to add text to the next patch notes.");
				System.out.println("Use --resetText to reset the text file for the next patch notes.");
				System.out.println("Use --generate to generate patch notes.");
			}
			else
				System.out.println(String.format("Unknown argument: %s", arg));
		}
	}
}
