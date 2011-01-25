package org.openengsb.tooling.pluginsuite.openengsbplugin;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;

public class AbstractReleaseTest extends MojoPreparation {

    protected File releaseRepo = null;
    protected File snapshotRepo = null;

    @Before
    public void initRepos() {
        releaseRepo = new File("src/test/resources/release/dummyReleaseRepo");
        assertTrue(releaseRepo.mkdir());
        snapshotRepo = new File("src/test/resources/release/dummySnapshotRepo");
        assertTrue(snapshotRepo.mkdir());
    }

    @After
    public void destroyRepos() {
        FileUtils.deleteQuietly(releaseRepo);
        FileUtils.deleteQuietly(snapshotRepo);
    }

}
