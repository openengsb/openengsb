package org.openengsb.core.edbi.jdbc.sql;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * TableElementCompilerTest
 */
public class TableElementCompilerTest {
    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void toSql_withNoElements_returnsCorrectSql() throws Exception {
        Table table = new Table("FOO");

        compileAndAssert("", table);
    }

    @Test
    public void toSql_withSingleColumn_returnsCorrectSql() throws Exception {
        Table table = new Table("FOO", new Column("COL", "VARCHAR"));

        compileAndAssert("COL VARCHAR", table);
    }

    @Test
    public void toSql_withMultipleColumns_returnsCorrectSql() throws Exception {
        Table table =
            new Table("FOO", new Column("COL", "VARCHAR"), new Column("COL2", "INT"), new Column("COL3", "BOOL"));

        compileAndAssert("COL VARCHAR,COL2 INT,COL3 BOOL", table);
    }

    @Test
    public void toSql_withColumnsThatContainsOptions_returnsCorrectSql() throws Exception {
        Table table =
            new Table("FOO", new Column("COL", "VARCHAR", Column.Option.NOT_NULL), new Column("COL2", "VARCHAR",
                Column.Option.NULL), new Column("COL3", "INT", Column.Option.AUTO_INCREMENT));

        compileAndAssert("COL VARCHAR NOT NULL,COL2 VARCHAR,COL3 INT AUTO_INCREMENT", table);
    }

    @Test
    public void toSql_withUniqueConstraint_returnsCorrectSql() throws Exception {
        Table table = new Table("FOO", new Column("A", "INT"), new Column("B", "INT"), new UniqueConstraint("A"));

        compileAndAssert("A INT,B INT,UNIQUE (A)", table);
    }

    @Test
    public void toSql_withMultiColumnConstraint_returnsCorrectSql() throws Exception {
        Table table = new Table("FOO", new Column("A", "INT"), new Column("B", "INT"), new UniqueConstraint("A", "B"));

        compileAndAssert("A INT,B INT,UNIQUE (A,B)", table);
    }

    @Test
    public void toSql_withPrimaryKey_returnsCorrectSql() throws Exception {
        Table table = new Table("FOO", new Column("A", "INT"), new PrimaryKeyConstraint("A"));

        compileAndAssert("A INT,PRIMARY KEY (A)", table);
    }

    @Test
    public void toSql_withForeignKey_returnsCorrectSql() throws Exception {
        Table table = new Table("FOO", new Column("A", "INT"), new ReferentialConstraint("A", "R", "B"));

        compileAndAssert("A INT,(A) REFERENCES R (B)", table);
    }

    public static void compileAndAssert(String expected, Table table) {
        assertEquals(expected, new TableElementCompiler(table).toSql());
    }

}
