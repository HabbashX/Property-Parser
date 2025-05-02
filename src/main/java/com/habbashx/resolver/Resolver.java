package com.habbashx.resolver;

import java.util.Properties;

public sealed abstract class Resolver permits ExternalPropertyResolver, PlaceholderResolver {
    public abstract String resolve(String value , Properties properties);
}
