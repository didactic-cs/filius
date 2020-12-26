package filius.software.system;

import static filius.software.system.Dateisystem.FILE_SEPARATOR;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.swing.tree.DefaultMutableTreeNode;

import org.junit.Test;

public class DateisystemTest {
    private static final String TEST_DIR = "test";
    private Dateisystem filesystem = new Dateisystem();

    @Test
    public void testChangeDirectory_AbsolutePath() throws Exception {
        filesystem.erstelleVerzeichnis(FILE_SEPARATOR, TEST_DIR);

        DefaultMutableTreeNode newDir = filesystem.changeDirectory(FILE_SEPARATOR + TEST_DIR);

        assertThat(newDir.getUserObject(), is(TEST_DIR));
    }

    @Test
    public void testChangeDirectory_RelativePath() throws Exception {
        filesystem.erstelleVerzeichnis(filesystem.getRoot(), TEST_DIR);

        DefaultMutableTreeNode newDir = filesystem.changeDirectory(TEST_DIR);

        assertThat(newDir.getUserObject(), is(TEST_DIR));
    }

    @Test
    public void testToAbsolutePath_RelPath() throws Exception {
        filesystem.erstelleVerzeichnis(filesystem.getRoot(), TEST_DIR);
        DefaultMutableTreeNode testDir = filesystem.changeDirectory(TEST_DIR);

        String relPath = "a" + FILE_SEPARATOR + "directory";
        String path = filesystem.toAbsolutePath(testDir, relPath);

        assertThat(path, is(FILE_SEPARATOR + TEST_DIR + FILE_SEPARATOR + relPath));
    }

    @Test
    public void testToAbsolutePath_AbsPath() throws Exception {
        String absPath = FILE_SEPARATOR + "this" + FILE_SEPARATOR + "is" + FILE_SEPARATOR + "a" + FILE_SEPARATOR
                + "directory";
        String path = filesystem.toAbsolutePath(absPath);

        assertThat(path, is(absPath));
    }
}
