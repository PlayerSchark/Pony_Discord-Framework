package io.schark.pony.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Supplier;
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

	public static void setValue(Object object, String fieldName, Object newValue) throws NoSuchFieldException, IllegalAccessException {
		PonyUtils.setValue(object, fieldName, newValue, object.getClass());
	}

	public static void setValue(Object object, String field, Object value, Class<?> clazz) throws IllegalAccessException, NoSuchFieldException {
		Field f = clazz.getDeclaredField(field);
		f.setAccessible(true);
		f.set(object, value);
	}

	public static <T> T awaitNonNull(Supplier<T> getter) {
		while (getter.get() == null) {
			try {
				Thread.sleep(10);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return getter.get();
	}

	public static void await(Supplier<Boolean> isTrue) {
		while (!isTrue.get()) {
			try {
				Thread.sleep(25L);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
