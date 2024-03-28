package slimeattack07.patchgencb;

/** Class holding the @CategoryInfo annotation code for the CodeGen handler.
 * 
 */
public class CategoryInfo{
	/** Returns the code.
	 * 
	 * @return The code.
	 */
	public static String getCode() {
		return """
To use this annotation, use @CategoryInfo.
To provide parameters, use @Category(paramname = value), for example: @Watchable(id = "category_one").
This annotation works for: Classes.

It requires the following parameters:
-	id, which should be a String.

In case your programming language does not support classes, the following is the 
exact behavior of this annotation: When this annotation is encountered, all category ids for subsequent @Watchable
annotations are replaced with the id of this annotation's category id, until either another @CategoryInfo is encountered
or the end of the file has been reached.
		""";
	}
}