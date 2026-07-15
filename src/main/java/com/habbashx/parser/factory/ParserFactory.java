package com.habbashx.parser.factory;

import com.habbashx.exception.UnSupportedTypeException;
import com.habbashx.parser.ObjectParser;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory responsible for parsing string values into strongly typed Java objects.
 *
 * <p>
 * This class provides a centralized registry of primitive and common type parsers,
 * along with fallback strategies for enums, constructors, and object parsing.
 * </p>
 *
 * <p>
 * Supported parsing strategies (in order):
 * </p>
 * <ol>
 *     <li>Pre-registered primitive/wrapper parsers</li>
 *     <li>Enum resolution via {@link Enum#valueOf(Class, String)}</li>
 *     <li>String constructor-based instantiation</li>
 *     <li>Fallback object parsing via {@code ObjectParser}</li>
 * </ol>
 *
 * <p>
 * If none of the strategies succeed, an {@code UnSupportedTypeException} is thrown.
 * </p>
 */
public class ParserFactory {

    /**
     * Registry of type-specific parsers for fast conversion.
     */
    private static final Map<Class<?>, ValueParser<?>> REGISTRY = new HashMap<>();

    /**
     * Cache of resolved single-{@code String}-arg constructors, keyed by target type.
     * Avoids re-resolving (and re-throwing NoSuchMethodException) on every
     * fallback parse call for the same unregistered type.
     */
    private static final Map<Class<?>, Constructor<?>> STRING_CTOR_CACHE = new ConcurrentHashMap<>();

    /**
     * Sentinel stored in {@link #STRING_CTOR_CACHE} to mark a type as having
     * no {@code String} constructor, so we don't repeatedly attempt (and fail)
     * the reflective lookup for it.
     */
    private static final Constructor<?> NO_STRING_CTOR;

    static {
        try {
            NO_STRING_CTOR = ParserFactory.class.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    static {
        REGISTRY.put(int.class, Integer::parseInt);
        REGISTRY.put(Integer.class, Integer::parseInt);
        REGISTRY.put(short.class, Short::parseShort);
        REGISTRY.put(Short.class, Short::parseShort);
        REGISTRY.put(long.class, Long::parseLong);
        REGISTRY.put(Long.class, Long::parseLong);
        REGISTRY.put(char.class, v -> v.charAt(0));
        REGISTRY.put(Character.class, v -> v.charAt(0));
        REGISTRY.put(boolean.class, Boolean::parseBoolean);
        REGISTRY.put(Boolean.class, Boolean::parseBoolean);

        REGISTRY.put(String.class, v -> v);
        REGISTRY.put(BigDecimal.class, BigDecimal::new);
        REGISTRY.put(BigInteger.class, BigInteger::new);

        REGISTRY.put(LocalDateTime.class, LocalDateTime::parse);
        REGISTRY.put(LocalDate.class, LocalDate::parse);
    }

    /**
     * Parses a string value into the specified target type.
     *
     * <p>
     * The method first checks the internal registry of parsers. If no parser is found,
     * it attempts enum conversion, then string constructor instantiation,
     * and finally a fallback object parser.
     * </p>
     *
     * @param type the target class type
     * @param value the raw string value to parse
     * @param <T> the target type
     * @return parsed instance of type {@code T}
     * @throws UnSupportedTypeException if no parsing strategy succeeds
     */
    @SuppressWarnings({"unchecked","rawtypes"})
    public static <T> T parse(Class<T> type, String value) {
        ValueParser<T> parser = (ValueParser<T>) REGISTRY.get(type);
        if (parser != null) {
            return parser.parse(value);
        }

        if (type.isEnum()) {
            return (T) Enum.valueOf((Class<? extends Enum>) type, value);
        }

        final Constructor<?> ctor = STRING_CTOR_CACHE.computeIfAbsent(type, t -> {
            try {
                final Constructor<?> c = t.getConstructor(String.class);
                c.setAccessible(true);
                return c;
            } catch (NoSuchMethodException e) {
                return NO_STRING_CTOR; // sentinel: no such constructor, cached to avoid retrying
            }
        });

        if (ctor != NO_STRING_CTOR) {
            try {
                return (T) ctor.newInstance(value);
            } catch (Exception ignored) {
                // fall through to ObjectParser below
            }
        }

        final @Nullable Object result = ObjectParser.parseObject(value);
        if (type.isInstance(result)) {
            return type.cast(result);
        }

        throw new UnSupportedTypeException("Cannot parse value to type: " + type.getName());
    }
}