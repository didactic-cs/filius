package filius.software.dns;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ResourceRecordTest {

    @Test
    public void testSetDomainname_validName_appendDotForRoot() throws Exception {
        ResourceRecord rr = new ResourceRecord("");
        rr.setDomainname("valid-domain");

        assertThat(rr.getDomainname(), is("valid-domain."));
    }

    @Test
    public void testSetDomainname_INVALIDName_IgnoreSetter() throws Exception {
        ResourceRecord rr = new ResourceRecord("");

        rr.setDomainname("@xyz");

        assertThat(rr.getDomainname(), is(""));
    }

    @Test
    public void testSetDomainname_StartWithDigit_IgnoreSetter() throws Exception {
        ResourceRecord rr = new ResourceRecord("");

        rr.setDomainname("123domain");

        assertThat(rr.getDomainname(), is(""));
    }

    @Test
    public void testSetDomainname_StartSubdomainWithDigit_IgnoreSetter() throws Exception {
        ResourceRecord rr = new ResourceRecord("");

        rr.setDomainname("valid-domain.123domain.example");

        assertThat(rr.getDomainname(), is(""));
    }
}
