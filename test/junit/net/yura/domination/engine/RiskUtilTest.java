package net.yura.domination.engine;

import java.util.Collections;
import java.util.List;
import junit.framework.TestCase;
import net.yura.domination.mapstore.Map;
import net.yura.domination.mapstore.MapChooser;
import net.yura.domination.mapstore.MapUpdateService;

public class RiskUtilTest extends TestCase {

    public void testIsValidName() {

        assertTrue(RiskUtil.isValidName("file"));
        assertTrue(RiskUtil.isValidName("file.txt"));
        assertTrue(RiskUtil.isValidName(".file"));
        assertTrue(RiskUtil.isValidName(".text.file"));
        assertTrue(RiskUtil.isValidName("my.file.text"));
        assertTrue(RiskUtil.isValidName("my file.text"));
        assertTrue(RiskUtil.isValidName(" !#$%&'()+,-09;=@AZ[]^_`az{}"));

        // bad windows chars
        assertFalse(RiskUtil.isValidName("file\\name"));
        assertFalse(RiskUtil.isValidName("file/name"));
        assertFalse(RiskUtil.isValidName("file:name.txt"));
        assertFalse(RiskUtil.isValidName("file*name.txt"));
        assertFalse(RiskUtil.isValidName("file?name"));
        assertFalse(RiskUtil.isValidName("file\"name"));
        assertFalse(RiskUtil.isValidName("file<name"));
        assertFalse(RiskUtil.isValidName("file>name"));
        assertFalse(RiskUtil.isValidName("file|name"));

        // other invalid names
        assertFalse(RiskUtil.isValidName(""));
        assertFalse(RiskUtil.isValidName("."));
        assertFalse(RiskUtil.isValidName("file."));
        assertFalse(RiskUtil.isValidName("con"));
        assertFalse(RiskUtil.isValidName("con.txt"));
        assertFalse(RiskUtil.isValidName("file€name.txt"));
    }
}
