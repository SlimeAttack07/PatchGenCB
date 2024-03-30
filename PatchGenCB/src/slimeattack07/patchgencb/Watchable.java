package slimeattack07.patchgencb;

/** Class holding the @Watchable annotation code for the CodeGen handler.
 * 
 */
public class Watchable{
	// TODO: Test if we can change policy to SOURCE.
	/** Returns the code.
	 * 
	 * @return The code.
	 */
	public static String getCode() {
		return """
To use this annotation, use @Watchable.
To provide parameters, use @Watchable(paramname = value), for example: @Watchable(id = "object_one").
This annotation works for: Fields.

It requires the following parameters:
-	id, which should be a String.

It supports the following optional parameters:
-	name, which should be a String and is empty by default. Use this to assign a name to the field.
-	category, which should be a String and is empty by default. Use this to assign a category to the field.
-	bulleted, which should be a boolean and is true by default. Use this to determine whether a bullet point is generated.
-	after, Which should be a String and is empty by default. Use this to make the change detector start after a given String.
-	until, Which should be a String and is empty by default. Use this to make the change detector stop after a given String.
     For correct behavior, combining 'after' and 'until' requires 'after' to be placed BEFORE 'until'.
		""";
	}
}