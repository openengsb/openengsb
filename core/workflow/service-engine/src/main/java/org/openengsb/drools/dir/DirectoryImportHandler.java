package org.openengsb.drools.dir;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.openengsb.drools.DirectoryRuleSource;
import org.openengsb.drools.RuleBaseException;

public class DirectoryImportHandler extends ResourceHandler<DirectoryRuleSource> {

    public static final String IMPORTS_FILE = "imports";

    private File importsFile;

    public DirectoryImportHandler(DirectoryRuleSource source) {
        super(source);
        importsFile = new File(source.getPath() + File.separator + IMPORTS_FILE);
    }

    @Override
    public void create(String name, String code) throws RuleBaseException {
        Collection<String> imports = readImports();
        imports.add(name);
        writeImports(imports);
        System.err.println("reread rulebase");
        source.readRuleBase();

    }

    @Override
    public void delete(String name) throws RuleBaseException {
        Collection<String> imports = readImports();
        imports.remove(name);
        writeImports(imports);
        source.getRulebase().getPackage("org.openengsb").removeImport(name);
    }

    @Override
    public String get(String name) throws RuleBaseException {
        // TODO Auto-generated method stub
        return null;
    }

    private Set<String> readImports() throws RuleBaseException {
        try {
            return doReadImports();
        } catch (IOException e) {
            throw new RuleBaseException("cannot read imports", e);
        }
    }

    private Set<String> doReadImports() throws IOException {
        Set<String> result = new TreeSet<String>();
        BufferedReader reader = new BufferedReader(new FileReader(importsFile));
        String line;
        while ((line = reader.readLine()) != null) {
            result.add(line);
        }
        reader.close();
        return result;
    }

    private void writeImports(Collection<String> list) throws RuleBaseException {
        try {
            doWriteImports(list);
        } catch (IOException e) {
            throw new RuleBaseException("cannot write imports", e);
        }
    }

    private void doWriteImports(Collection<String> list) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(importsFile));
        for (String line : list) {
            bw.write(line);
            bw.newLine();
        }
        bw.close();
    }
}
