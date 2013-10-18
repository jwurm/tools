import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This is a little helper that creates assert statements by reflection
 * 
 * @author Jens Wurm
 * 
 */
public class Assertifier {

	private static boolean includeIds = false;

	private static boolean isRelevantGetter(Method method) {
		String name = method.getName();
		return name.startsWith("get") && method.getParameterTypes().length == 0
				&& !name.equals("getClass")
				&& !name.equals("getCacheKeyAttributes")
				&& !name.equals("getProperties")
				&& !name.equals("getConstraints")
				&& !name.equals("getDataTypeConstraints")
				&& !name.equals("getPropertyType")
				&& !name.equals("get_AssociationHandler")
				&& !name.equals("getCacheKey")
				&& !name.equals("get_InstanceId")
				&& !name.equals("getDataTypeId")
				&& !name.equals("getInternalDate")
				&& !name.equals("getAnspInhaltAsBoolean")
				&& !name.equals("getTechVersion")
				&& (!name.equals("getId") || includeIds);
	}

	private boolean includeNull = false;

	private Set<Object> alreadyTraversed = new HashSet<Object>();

	private boolean includeEmptyLists = false;

	private Set<Class> ignoreClasses = new HashSet<Class>();

	/**
	 * Include Assert.assertNull statements for attributes that are null.
	 */
	public Assertifier includeNull() {
		includeNull = true;
		return this;
	}

	/**
	 * Include assert statements that verify if empty lists of the original
	 * still are empty
	 */
	public Assertifier includeEmptyLists() {
		includeEmptyLists = true;
		return this;
	}

	/**
	 * Include ids of objects - this may cause problems e.g. in case of runtime
	 * tests in which new object instances are created in a not empty database,
	 * as the ids may change with every test run
	 */
	public Assertifier includeIds() {
		includeIds = true;
		return this;
	}

	public Assertifier ignore(Class... classes) {
		ignoreClasses.addAll(Arrays.asList(classes));
		return this;
	}

	/**
	 * Creates assert statements for any object.
	 * 
	 * Example: Reservation myReservation=new Reservation(); new
	 * Assertifier(Assertifier.INCLUDE_NULL).assertify(myReservation,
	 * "myReservation");
	 * 
	 * @param object
	 *            any object
	 * @param name
	 *            the name of the object in the calling method
	 */
	public void assertify(Object object, String name) {
		try {
			System.out.println(processResult(null, object, name, true));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private StringBuilder buildEquals(String result, String path) {

		return new StringBuilder("Assert.assertEquals(" + result + ", " + path
				+ ");\r\n");
	}

	private StringBuilder buildNull(String path) {
		if (includeNull) {
			return new StringBuilder("Assert.assertNull(" + path + ");\r\n");
		} else {
			return new StringBuilder();
		}
	}

	private String buildPathListEntry(String path, int i, Object result) {
		return "((" + result.getClass().getSimpleName() + ")" + path + ".get("
				+ i + "))";
	}

	private String buildPathPrimitive(Method method, Object result, String path) {
		if (method == null) {
			return path;
		}
		return path + "." + method.getName() + "()";
	}

	private String buildPathWithCast(Method method, Object result, String path) {
		if (method == null) {
			return path;
		}
		return "((" + result.getClass().getSimpleName() + ")" + path + "."
				+ method.getName() + "())";
	}

	private StringBuilder processNull(String path) {
		return buildNull(path);

	}

	private StringBuilder processResult(Enum result, String path) {
		return buildEquals(result.getClass().getSimpleName() + "." + result,
				path);
	}

	private StringBuilder processResult(Integer result, String path) {
		return buildEquals("Integer.valueOf(" + result.toString() + ")", path);
	}

	private StringBuilder processResult(Long result, String path) {
		return buildEquals("Long.valueOf(" + result.toString() + "L)", path);
	}

	private StringBuilder processResult(Method currMethod, Object result,
			String path, boolean root) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		if (result != null) {
			if (alreadyTraversed.contains(result)) {
				return new StringBuilder();
			} else {
				addToAlreadyTraversed(result);
			}
		}

		StringBuilder ret = new StringBuilder();
		if (root) {
			// root element
			ret.append(processNotNull(path));
		}

		if (result == null) {
			ret.append(processNull(buildPathPrimitive(currMethod, result, path)));
		} else if (result instanceof Integer) {
			ret.append(processResult((Integer) result,
					buildPathWithCast(currMethod, result, path)));
		} else if (result instanceof Long) {
			ret.append(processResult((Long) result,
					buildPathWithCast(currMethod, result, path)));
		} else if (result instanceof String) {
			ret.append(processResult((String) result,
					buildPathPrimitive(currMethod, result, path)));
		} else if (result instanceof Enum) {
			ret.append(processResult((Enum) result,
					buildPathPrimitive(currMethod, result, path)));
		} else if (result instanceof Boolean) {
			ret.append(processResult((Boolean) result,
					buildPathPrimitive(currMethod, result, path)));
		} else if (result instanceof Date) {
			// nothing
		} else {
			ret.append(traverse(result,
					buildPathWithCast(currMethod, result, path)));
		}

		return ret;

	}

	/**
	 * @param result
	 * @param buildPathPrimitive
	 * @return
	 */
	private StringBuilder processResult(Boolean result, String path) {
		return buildEquals("Boolean.valueOf(" + result + ")",
				"Boolean.valueOf(" + path + ")");
	}

	/**
	 * This method adds object to the list of already traversed results, and
	 * checks if they are eligible for that list in first instance. E.g. Strings
	 * are not, as the same value may be used in different places, but they are
	 * not supposed to be considered object identical
	 * 
	 * @param result
	 */
	@SuppressWarnings("unchecked")
	private void addToAlreadyTraversed(Object result) {
		if (result instanceof String) {
			return;
		}
		if (result instanceof Integer) {
			return;
		}
		if (result instanceof Long) {
			return;
		}
		if (result instanceof Enum) {
			return;
		}
		alreadyTraversed.add(result);
	}

	private StringBuilder processNotNull(String path) {
		return new StringBuilder("Assert.assertNotNull(" + path + ");\r\n");
	}

	private StringBuilder processResult(String result, String path) {
		return buildEquals("\"" + result + "\"", path);
	}

	// private static String buildEquals(Integer result, String path) {
	//
	// return "Assert.assertEquals(" + result + ", " + path + ");";
	// }

	/**
	 * Traverses an object's attributes by reflection
	 * 
	 * @param object
	 * @param path
	 * @return a StringBuilder containing the generated Assert statements
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	@SuppressWarnings("unchecked")
	private StringBuilder traverse(Object object, String path)
			throws IllegalAccessException, InvocationTargetException {
		StringBuilder ret = new StringBuilder();
		if (isSkipObject(object)) {
			return ret;
		}
		Method[] methods = object.getClass().getMethods();
		if (object instanceof List) {
			List list = (List) object;
			int size = list.size();
			if (size > 0 || includeEmptyLists) {
				ret.append(buildEquals(String.valueOf(size), path + ".size()"));
			}
			for (int i = 0; i < list.size(); i++) {
				Object listElement = list.get(i);
				ret.append(processResult(null, listElement,
						buildPathListEntry(path, i, listElement), false));

			}
		} else {
			for (Method currMethod : methods) {
				if (isRelevantGetter(currMethod)) {
					Object result = null;
					try {
						result = currMethod.invoke(object, (Object[]) null);
					} catch (Exception e) {
						// getter exceptions ignorieren
					}
					try {
						// ret.append(processResult(result, buildPath(curr,
						// result,
						// path)));
						ret.append(processResult(currMethod, result, path,
								false));

					} catch (Exception e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					}

				}
			}
		}
		return ret;
	}

	/**
	 * @param object
	 * @return
	 */
	private boolean isSkipObject(Object object) {
		String canonicalName = object.getClass().getCanonicalName();
		if (canonicalName.startsWith("javax")) {
			return true;
		} else if (canonicalName.startsWith("com.sun.org")) {
			return true;
		} else if (ignoreClasses.contains(object.getClass())) {
			return true;
		}
		return false;
	}

	// public static void main(String[] args) {
	// TestClass tc = new Assertifier.TestClass();
	// new Assertifier().assertify(tc, "tc");
	// }
	//
	// private static class TestClass{
	// private Boolean bool=false;
	// /**
	// * @return the bool
	// */
	// public boolean getBool() {
	// return bool;
	// }
	//
	// }

}
