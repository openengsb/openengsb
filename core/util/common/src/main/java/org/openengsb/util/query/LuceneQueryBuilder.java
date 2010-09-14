/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.util.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * Simply query builder to create lucene queries from values. Mostly this is a
 * quite simple query builder with the simply rule that only one
 * {@link LuceneQueryBuilder} could contain one operator. To add additional
 * operator add additonal {@link LuceneQueryBuilder}. Every
 * {@link LuceneQueryBuilder} is surrounded with () which allows to build
 * subqueries with the intelligent combination of {@link LuceneQueryBuilder}s.
 */
public class LuceneQueryBuilder {

    /**
     * Meta-Characters for lucene queries. These characters have a special
     * meaning for lucene and should therefore not directly used in the values
     * sent to the database.
     */
    private static final String[] LUCENE_META = new String[] { "\\\\", "\\+", "-", "&", "\\|\\|", "!", "\\(", "\\)",
            "\\{", "\\}", "\\[", "]", "\\^", "\"", "~", "\\*", ":", " " };

    private static final String AND = "AND";
    private static final String OR = "OR";

    private List<LuceneQueryBuilder> luceneQueryParts = new ArrayList<LuceneQueryBuilder>();
    private HashMap<String, String> keyValuePairs = new HashMap<String, String>();
    private String operator = null;

    public LuceneQueryBuilder() {
        super();
    }

    /**
     * Procudes a {@link String} which could be used to query any lucene based
     * database.
     * 
     * @throws LuceneQueryBuilderException in the case that any values or
     *         methods are not called this exception is thrown.
     */
    public String buildQuery() {
        if (this.luceneQueryParts.size() == 0 && this.keyValuePairs.size() == 0) {
            throw new LuceneQueryBuilderException(
                    "Cant build query if the internal parts AND the key value pairs are null...");
        }
        if (this.operator == null) {
            throw new LuceneQueryBuilderException("Cant build a query with null operator...");
        }

        final StringBuilder builder = new StringBuilder();
        builder.append("(");
        if (this.keyValuePairs.size() != 0) {
            long counter = 0;
            for (final Entry<String, String> keyValuePair : this.keyValuePairs.entrySet()) {
                builder.append(keyValuePair.getKey());
                builder.append(":");
                builder.append(cleanupValue(keyValuePair).getValue());
                counter++;
                if (counter != this.keyValuePairs.size()) {
                    appendOperator(builder);
                }
            }
        }
        if (this.luceneQueryParts.size() != 0) {
            if (this.keyValuePairs.size() != 0) {
                appendOperator(builder);
            }
            long counter = 0;
            for (final LuceneQueryBuilder queryBuilder : this.luceneQueryParts) {
                builder.append(queryBuilder.buildQuery());
                counter++;
                if (counter != this.luceneQueryParts.size()) {
                    appendOperator(builder);
                }
            }
        }
        builder.append(")");
        return builder.toString();
    }

    public LuceneQueryBuilder and(final Entry<String, String> keyValue) {
        checkAndSetAndOperator();
        this.keyValuePairs.put(keyValue.getKey(), keyValue.getValue());
        return this;
    }

    public LuceneQueryBuilder and(final String key, final String value) {
        checkAndSetAndOperator();
        this.keyValuePairs.put(key, value);
        return this;
    }

    public LuceneQueryBuilder and(final LuceneQueryBuilder builder) {
        checkAndSetAndOperator();
        this.luceneQueryParts.add(builder);
        return this;
    }

    public LuceneQueryBuilder or(final Entry<String, String> keyValue) {
        checkAndSetOrOperator();
        this.keyValuePairs.put(keyValue.getKey(), keyValue.getValue());
        return this;
    }

    public LuceneQueryBuilder or(final String key, final String value) {
        checkAndSetOrOperator();
        this.keyValuePairs.put(key, value);
        return this;
    }

    public LuceneQueryBuilder or(final LuceneQueryBuilder builder) {
        checkAndSetOrOperator();
        this.luceneQueryParts.add(builder);
        return this;
    }

    private void checkAndSetAndOperator() {
        checkAndSetOperator(LuceneQueryBuilder.AND);
    }

    private void checkAndSetOrOperator() {
        checkAndSetOperator(LuceneQueryBuilder.OR);
    }

    private void checkAndSetOperator(final String operator) {
        if (this.operator != null && !this.operator.equals(operator)) {
            throw new LuceneQueryBuilderException("Cant do OR after something else was called...");
        }
        this.operator = operator;
    }

    /**
     * Removes fields specific for queries from the value field.
     */
    private Entry<String, String> cleanupValue(final Entry<String, String> value) {
        if (value.getValue() == null) {
            value.setValue("");
        }
        for (int i = 0; i < LuceneQueryBuilder.LUCENE_META.length; i++) {
            value.setValue(value.getValue().replaceAll(LuceneQueryBuilder.LUCENE_META[i], "?"));
        }
        if (value.getValue().equals("")) {
            value.setValue("*");
        }
        return value;
    }

    private void appendOperator(final StringBuilder builder) {
        builder.append(" ").append(this.operator).append(" ");
    }

}
