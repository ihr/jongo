/*
 * Copyright (C) 2011 Benoit GUEROUT <bguerout at gmail dot com> and Yves AMSELLEM <amsellem dot yves at gmail dot com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jongo;

import com.mongodb.util.JSON;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParameterBinder {

    private static final String DEFAULT_TOKEN = "#";
    private final String token;
    private final Pattern pattern;

    public ParameterBinder() {
        this(DEFAULT_TOKEN);
    }

    public ParameterBinder(String token) {
        this.token = token;
        this.pattern = Pattern.compile(token);
    }

    public String bind(String template, Object... parameters) {
        assertThatParamsCanBeBound(template, parameters);
        return generateQueryFromTemplate(template, parameters);
    }

    private String generateQueryFromTemplate(String template, Object[] parameters) {
        String query = template;
        int paramIndex = 0;
        while (query.contains(token)) {
            String paramAsJson = serializeAsJson(parameters[paramIndex++]);
            query = query.replaceFirst(token, getMatcherWithEscapedDollar(paramAsJson));
        }
        return query;
    }

    private String serializeAsJson(Object parameter) {
        try {
            return JSON.serialize(parameter);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Unable to bind parameter: " + parameter + " into template. " +
                    "Parameter must be a BSON Primitive or a class supported by DBObject", e);
        }
    }

    private void assertThatParamsCanBeBound(String template, Object[] parameters) {
        int nbTokens = countTokens(template);
        if (nbTokens != parameters.length) {
            //TODO improve exception message
            throw new IllegalArgumentException("Tokens and parameters numbers mismatch " +
                    "[query: " + template + " / tokens:" + nbTokens + " / parameters:[" + parameters.length + "]");
        }
    }

    /**
     * http://veerasundar.com/blog/2010/01/java-lang-illegalargumentexception-illegal-group-reference-in-string-replaceall/
     */
    private String getMatcherWithEscapedDollar(String serialized) {
        return Matcher.quoteReplacement(serialized);
    }

    private int countTokens(String template) {
        return pattern.split(template, 0).length - 1;
    }
}
