package filius.software.vermittlungsschicht;

import filius.exception.InvalidParameterException;

public enum IPVersion {
    IPv4, IPv6;

    public static IPVersion fromString(String versionString) throws InvalidParameterException {
        if ("IPv4".equalsIgnoreCase(versionString)) {
            return IPv4;
        } else if ("IPv6".equalsIgnoreCase(versionString)) {
            return IPv6;
        }
        throw new InvalidParameterException("The parameter '" + versionString + "' is an invalid IP version.");
    }
}
