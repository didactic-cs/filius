package filius.software.firewall;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FirewallRuleTest {

    @Test
    public void testValidateExistingAttributes_EmptyRule() throws Exception {
        FirewallRule rule = new FirewallRule();

        assertTrue(rule.validateExistingAttributes());
    }

    @Test
    public void testValidateExistingAttributes_ValidSource() throws Exception {
        FirewallRule rule = new FirewallRule();
        rule.srcIP = "192.168.2.2";
        rule.srcMask = "255.255.0.0";

        assertTrue(rule.validateExistingAttributes());
    }

    @Test
    public void testValidateExistingAttributes_INVALIDSourceAddress() throws Exception {
        FirewallRule rule = new FirewallRule();
        rule.srcIP = "192.168.2.256";

        assertFalse(rule.validateExistingAttributes());
    }

    @Test
    public void testValidateExistingAttributes_SourceSameNetwork() throws Exception {
        FirewallRule rule = new FirewallRule();
        rule.srcIP = FirewallRule.SAME_NETWORK;

        assertTrue(rule.validateExistingAttributes());
    }

    @Test
    public void testValidateExistingAttributes_INVALIDSourceMask() throws Exception {
        FirewallRule rule = new FirewallRule();
        rule.srcMask = "192.168.2.256";

        assertFalse(rule.validateExistingAttributes());
    }

    @Test
    public void testValidateExistingAttributes_ValidDest() throws Exception {
        FirewallRule rule = new FirewallRule();
        rule.destIP = "192.168.2.2";
        rule.destMask = "255.255.0.0";

        assertTrue(rule.validateExistingAttributes());
    }

    @Test
    public void testValidateExistingAttributes_INVALIDDestAddress() throws Exception {
        FirewallRule rule = new FirewallRule();
        rule.destIP = "192.168.2.256";

        assertFalse(rule.validateExistingAttributes());
    }

    @Test
    public void testValidateExistingAttributes_DestSameNetwork_Reject() throws Exception {
        FirewallRule rule = new FirewallRule();
        rule.destIP = FirewallRule.SAME_NETWORK;

        assertFalse(rule.validateExistingAttributes());
    }

    @Test
    public void testValidateExistingAttributes_INVALIDDestMask() throws Exception {
        FirewallRule rule = new FirewallRule();
        rule.destMask = "192.168.2.256";

        assertFalse(rule.validateExistingAttributes());
    }

    @Test
    public void testValidateExistingAttributes_ValidPortProtocolAction() throws Exception {
        FirewallRule rule = new FirewallRule();
        rule.port = 20;
        rule.protocol = FirewallRule.TCP;
        rule.action = FirewallRule.DROP;

        assertTrue(rule.validateExistingAttributes());
    }

    @Test
    public void testValidateExistingAttributes_INVALIDPort() throws Exception {
        FirewallRule rule = new FirewallRule();
        rule.port = 999999;

        assertFalse(rule.validateExistingAttributes());
    }

    @Test
    public void testValidateExistingAttributes_INVALIDProtocol() throws Exception {
        FirewallRule rule = new FirewallRule();
        rule.protocol = 999;

        assertFalse(rule.validateExistingAttributes());
    }

    @Test
    public void testValidateExistingAttributes_INVALIDAction() throws Exception {
        FirewallRule rule = new FirewallRule();
        rule.action = 999;

        assertFalse(rule.validateExistingAttributes());
    }
}
