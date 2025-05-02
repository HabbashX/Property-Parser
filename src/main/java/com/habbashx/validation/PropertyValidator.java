package com.habbashx.validation;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface PropertyValidator {

     boolean isValid(String value);

     static @NotNull PropertyValidator notEmpty() {
         return value -> value != null && !value.trim().isEmpty();
     }

     static @NotNull PropertyValidator regex(String regex) {
         return value -> value != null && value.matches(regex);
     }

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
