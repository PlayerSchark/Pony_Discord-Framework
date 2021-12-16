package io.schark.pony.utils;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Player_Schark
 */
public class PonyUtils {
	public static Set<Long> toUpperLong(long[] values) {
		return Arrays.stream(values).boxed().collect(Collectors.toSet());
	}

	public static String getFileContent(InputStream input) throws IOException {
		return PonyUtils.getFileContent(input, "UTF-8");
	}

	public static String getFileContent(InputStream input, String encoding) throws IOException {
			BufferedReader reader = new BufferedReader(new InputStreamReader(input, encoding));
			StringBuilder builder = new StringBuilder();
			String line;
			while((line = reader.readLine()) != null) {
				builder.append(line);
				builder.append('\n');
			}
			return builder.toString();
	}

	public static void setValue(Object object, String field, Object value) throws IllegalAccessException, NoSuchFieldException {
		Class<?> clazz = object.getClass();
		Field f = clazz.getDeclaredField(field);
		f.setAccessible(true);
		f.set(object, value);
	}
}
