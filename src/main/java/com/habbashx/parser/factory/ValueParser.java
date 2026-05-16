package com.habbashx.parser.factory;

interface ValueParser<T> {
    T parse(String value);
}
