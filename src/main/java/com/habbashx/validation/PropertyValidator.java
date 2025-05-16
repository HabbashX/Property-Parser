package com.habbashx.validation;

import org.jetbrains.annotations.NotNull;

/**
 * A functional interface representing a validation mechanism for properties. It allows defining
 * custom validation logic for string values.
 *
 * The interface provides built-in static validation methods such as:
 * - Checking if a string is not empty or null
 * - Validating a string against a regular expression
 * - Checking if a string represents an integer within a specified range
 *
 * This interface can be implemented to provide additional validation logic, if needed.
 */
@FunctionalInterface
public interface PropertyValidator {

     /**
      * Validates the provided string value based on the implemented validation logic.
      *
      * @param value the string value to be validated; may be null or empty depending on the implementation
      * @return {@code true} if the value satisfies the validation criteria; {@code false} otherwise
      */
     boolean isValid(String value);

     /**
      * Creates a PropertyValidator that checks if a given String is not null
      * and not empty after trimming whitespace.
      *
      * @return a PropertyValidator ensuring the value is neither null nor empty
      */
     static @NotNull PropertyValidator notEmpty() {
         return value -> value != null && !value.trim().isEmpty();
     }

     /**
      * Creates a PropertyValidator that validates if a given String matches the specified regular expression.
      *
      * @param regex the regular expression the string should match; must not be null
      * @return a PropertyValidator ensuring the value matches the specified regular expression
      */
     static @NotNull PropertyValidator regex(String regex) {
         return value -> value != null && value.matches(regex);
     }

     /**
      * Creates a {@link PropertyValidator} that validates if a given string represents
      * an integer within a specified inclusive range.
      *
      * @param minimum the minimum value of the range (inclusive)
      * @param maximum the maximum value of the range (inclusive)
      * @return a {@link PropertyValidator} that checks whether the string is a valid integer
      *         within the specified range
      */
     static @NotNull PropertyValidator integerRange(int minimum , int maximum) {
         return value -> {
             try {
                 int intValue = Integer.parseInt(value);
                 return intValue >= minimum && intValue <= maximum;
             } catch (NumberFormatException ignored) {
                 return false;
             }
         };
     }


}
