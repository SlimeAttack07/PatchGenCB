package slimeattack07.patchgencb.generators;

import java.io.File;

import slimeattack07.patchgencb.Utils;

/** Patch note generator: .txt output.
 * 
 */
public class PlainTextGenerator extends AbstractPatchNoteGenerator implements PatchNoteGenerator {
	private final File FILE;
	private final boolean IS_VALID;
	
	/** Constructor.
	 * 
	 * @param project The project to generate patch notes for.
	 */
	public PlainTextGenerator(String version) {
		this.FILE = Utils.requestUniqueFile("patchnotes", version, "txt");
		this.IS_VALID = this.FILE != null;
	}
	
	@Override
	public void addContent(String content, int depth, boolean bulleted) {
		String real_text = bulleted ? "* " + content : content;
		String indented = indent(real_text, depth);
		addToFile(FILE, indented + System.lineSeparator());
	}

	@Override
	public boolean isValid() {
		return IS_VALID;
	}

	@Override
	public void addCategory(String name, int depth) {
		addContent("--[[" + name.toUpperCase() + "]]--", depth, false); // TODO: Temporarily using upper case for testing.
	}

	@Override
	public void addText(String text, int depth, boolean is_developer_comment) {
		if(is_developer_comment)
			addContent("-{Developer Comments}- " + text, depth, false);
		else
			addContent(text, depth, false);
	}
	
	@Override
	public String indent(String text, int depth) {
		String indented = "";
		
		for(int i = 0; i < depth; i++)
			indented += "\t";
		
		return indented + text;
	}

	@Override
	public void finish() {}
}
