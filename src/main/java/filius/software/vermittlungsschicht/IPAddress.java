package filius.software.vermittlungsschicht;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import filius.exception.InvalidParameterException;

/** This class represents an IP address alternatively with or without netmask. */
public class IPAddress {
    private static final String GROUP_NETMASK = "netmask";
    private static final String[] GROUP_SEGMENT = { "segment0", "segment1", "segment2", "segment3", "segment4",
            "segment5", "segment6", "segment7" };
    private static final String BEFORE_ZERO_SEQ_PLACEHOLDER = "before";
    private static final String AFTER_ZERO_SEQ_PLACEHOLDER = "after";
    private static final String[] GROUP_BYTE = { "byte0", "byte1", "byte2", "byte3" };
    private static final String IP_V4_REGEX = "(?<" + GROUP_BYTE[0] + ">[0-9]{1,3})\\.(?<" + GROUP_BYTE[1]
            + ">[0-9]{1,3})\\.(?<" + GROUP_BYTE[2] + ">[0-9]{1,3})\\.(?<" + GROUP_BYTE[3] + ">[0-9]{1,3})";

    private static final String IP_V6_START_DOUBLE_COLON_REGEX = ":(:|(:[0-9a-fA-F]{1,4}){1,7})";
    private static final String IP_V6_END_DOUBLE_COLON_REGEX = "[0-9a-fA-F]{1,4}(:[0-9a-fA-F]{1,4}){0,5}::";
    private static final String IP_V6_ZERO_REGEX = "::";
    private static final String IP_V6_INBETWEEN_DOUBLE_COLON_REGEX = "([0-9a-fA-F]{1,4}(:[0-9a-fA-F]{1,4}){0,4}:(:[0-9a-fA-F]{1,4}){1,2}"
            + "|[0-9a-fA-F]{1,4}(:[0-9a-fA-F]{1,4}){0,3}:(:[0-9a-fA-F]{1,4}){1,3}"
            + "|[0-9a-fA-F]{1,4}(:[0-9a-fA-F]{1,4}){0,2}:(:[0-9a-fA-F]{1,4}){1,4}"
            + "|[0-9a-fA-F]{1,4}(:[0-9a-fA-F]{1,4}){0,1}:(:[0-9a-fA-F]{1,4}){1,5})";
    private static final String IP_V6_NO_DOUBLE_COLON_REGEX = "[0-9a-fA-F]{1,4}(:[0-9a-fA-F]{1,4}){7}";
    private static final String IP_V6_WITH_DEC_NOTATION_REGEX = "([0-9a-fA-F]{1,4}" + "(:[0-9a-fA-F]{1,4}){5}"
            + "|:(:[0-9a-fA-F]{1,4}){0,5}" + "|[0-9a-fA-F]{1,4}:(:[0-9a-fA-F]{1,4}){0,4}"
            + "|([0-9a-fA-F]{1,4}:){2}(:[0-9a-fA-F]{1,4}){0,3}" + "|([0-9a-fA-F]{1,4}:){3}(:[0-9a-fA-F]{1,4}){0,2}"
            + "|([0-9a-fA-F]{1,4}:){4}(:[0-9a-fA-F]{1,4}){0,1}" + "|([0-9a-fA-F]{1,4}:){5})" + ":" + IP_V4_REGEX;
    private static final String IP_V6_REGEX = "(" + IP_V6_WITH_DEC_NOTATION_REGEX + "|" + IP_V6_ZERO_REGEX + "|"
            + IP_V6_START_DOUBLE_COLON_REGEX + "|" + IP_V6_NO_DOUBLE_COLON_REGEX + "|" + IP_V6_END_DOUBLE_COLON_REGEX
            + "|" + IP_V6_INBETWEEN_DOUBLE_COLON_REGEX + ")";

    private static final String IP_V6_START_SEG_REGEX = "((?<" + BEFORE_ZERO_SEQ_PLACEHOLDER + GROUP_SEGMENT[0]
            + ">[0-9a-fA-F]{1,4})(:(?<" + BEFORE_ZERO_SEQ_PLACEHOLDER + GROUP_SEGMENT[1] + ">[0-9a-fA-F]{1,4})(:(?<"
            + BEFORE_ZERO_SEQ_PLACEHOLDER + GROUP_SEGMENT[2] + ">[0-9a-fA-F]{1,4})(:(?<" + BEFORE_ZERO_SEQ_PLACEHOLDER
            + GROUP_SEGMENT[3] + ">[0-9a-fA-F]{1,4})(:(?<" + BEFORE_ZERO_SEQ_PLACEHOLDER + GROUP_SEGMENT[4]
            + ">[0-9a-fA-F]{1,4})(:(?<" + BEFORE_ZERO_SEQ_PLACEHOLDER + GROUP_SEGMENT[5] + ">[0-9a-fA-F]{1,4})(:(?<"
            + BEFORE_ZERO_SEQ_PLACEHOLDER + GROUP_SEGMENT[6] + ">[0-9a-fA-F]{1,4})(:(?<" + BEFORE_ZERO_SEQ_PLACEHOLDER
            + GROUP_SEGMENT[7] + ">[0-9a-fA-F]{1,4}))?)?)?)?)?)?)?)?";
    private static final String IP_V6_START_SEG_WITH_DEC_NOTATION_REGEX = "((?<" + BEFORE_ZERO_SEQ_PLACEHOLDER
            + GROUP_SEGMENT[0] + ">[0-9a-fA-F]{1,4})(:(?<" + BEFORE_ZERO_SEQ_PLACEHOLDER + GROUP_SEGMENT[1]
            + ">[0-9a-fA-F]{1,4})(:(?<" + BEFORE_ZERO_SEQ_PLACEHOLDER + GROUP_SEGMENT[2] + ">[0-9a-fA-F]{1,4})(:(?<"
            + BEFORE_ZERO_SEQ_PLACEHOLDER + GROUP_SEGMENT[3] + ">[0-9a-fA-F]{1,4})(:(?<" + BEFORE_ZERO_SEQ_PLACEHOLDER
            + GROUP_SEGMENT[4] + ">[0-9a-fA-F]{1,4})(:(?<" + BEFORE_ZERO_SEQ_PLACEHOLDER + GROUP_SEGMENT[5]
            + ">[0-9a-fA-F]{1,4}))?)?)?)?)?)?";
    private static final String IP_V6_END_SEG_PART_REGEX = "(::(?<" + AFTER_ZERO_SEQ_PLACEHOLDER + GROUP_SEGMENT[0]
            + ">)?((((((((?<" + AFTER_ZERO_SEQ_PLACEHOLDER + GROUP_SEGMENT[1] + ">[0-9a-fA-F]{1,4}):)?(?<"
            + AFTER_ZERO_SEQ_PLACEHOLDER + GROUP_SEGMENT[2] + ">[0-9a-fA-F]{1,4}):)?(?<" + AFTER_ZERO_SEQ_PLACEHOLDER
            + GROUP_SEGMENT[3] + ">[0-9a-fA-F]{1,4}):)?(?<" + AFTER_ZERO_SEQ_PLACEHOLDER + GROUP_SEGMENT[4]
            + ">[0-9a-fA-F]{1,4}):)?(?<" + AFTER_ZERO_SEQ_PLACEHOLDER + GROUP_SEGMENT[5] + ">[0-9a-fA-F]{1,4}):)?(?<"
            + AFTER_ZERO_SEQ_PLACEHOLDER + GROUP_SEGMENT[6] + ">[0-9a-fA-F]{1,4}):)?(?<" + AFTER_ZERO_SEQ_PLACEHOLDER
            + GROUP_SEGMENT[7] + ">[0-9a-fA-F]{1,4}))?)?";
    private static final String IP_V6_END_SEG_PART_WITH_DEC_NOTATION_REGEX = "(::(?<" + AFTER_ZERO_SEQ_PLACEHOLDER
            + GROUP_SEGMENT[0] + ">)?((((((?<" + AFTER_ZERO_SEQ_PLACEHOLDER + GROUP_SEGMENT[1]
            + ">[0-9a-fA-F]{1,4}):)?(?<" + AFTER_ZERO_SEQ_PLACEHOLDER + GROUP_SEGMENT[2] + ">[0-9a-fA-F]{1,4}):)?(?<"
            + AFTER_ZERO_SEQ_PLACEHOLDER + GROUP_SEGMENT[3] + ">[0-9a-fA-F]{1,4}):)?(?<" + AFTER_ZERO_SEQ_PLACEHOLDER
            + GROUP_SEGMENT[4] + ">[0-9a-fA-F]{1,4}):)?(?<" + AFTER_ZERO_SEQ_PLACEHOLDER + GROUP_SEGMENT[5]
            + ">[0-9a-fA-F]{1,4}):)?|:)" + IP_V4_REGEX;
    private static final String IPV4_NETMASK_SUFFIX_REGEX = "(?<" + GROUP_NETMASK + ">[1-2]?[0-9]|3[0-2])";
    private static final String IPV6_NETMASK_SUFFIX_REGEX = "(?<" + GROUP_NETMASK
            + ">1([0-1][0-9]|2[0-8])|[1-9]?[0-9])";

    private static final Pattern IP_V4_OPTIONAL_NETMASK_VALIDATION_PATTERN = Pattern
            .compile(IP_V4_REGEX + "(/" + IPV4_NETMASK_SUFFIX_REGEX + ")?");
    private static final Pattern IP_V4_NO_NETMASK_VALIDATION_PATTERN = Pattern.compile(IP_V4_REGEX);
    private static final Pattern IP_V6_SEGMENT_PATTERN = Pattern
            .compile(IP_V6_START_SEG_REGEX + IP_V6_END_SEG_PART_REGEX);
    private static final Pattern IP_V6_WITH_DEC_NOTATION_SEGMENT_PATTERN = Pattern
            .compile(IP_V6_START_SEG_WITH_DEC_NOTATION_REGEX + IP_V6_END_SEG_PART_WITH_DEC_NOTATION_REGEX);
    private static final Pattern IP_V6_OPTIONAL_NETMASK_VALIDATION_PATTERN = Pattern
            .compile(IP_V6_REGEX + "(/" + IPV6_NETMASK_SUFFIX_REGEX + ")?");
    private static final Pattern IP_V6_NO_NETMASK_VALIDATION_PATTERN = Pattern.compile(IP_V6_REGEX);

    private final IPVersion version;
    private final short[] addressBytes;
    private final int netmaskLength;

    private boolean ipv6WithDecimalNotation;

    public IPAddress(String address) throws InvalidParameterException {
        Matcher ipv4matcher = IP_V4_OPTIONAL_NETMASK_VALIDATION_PATTERN.matcher(address);
        Matcher ipv6matcher = IP_V6_OPTIONAL_NETMASK_VALIDATION_PATTERN.matcher(address);

        String netmaskString = null;
        if (ipv4matcher.matches()) {
            version = IPVersion.IPv4;
            addressBytes = extractIPv4Segments(ipv4matcher);
            netmaskString = ipv4matcher.group(GROUP_NETMASK);
        } else if (ipv6matcher.matches()) {
            version = IPVersion.IPv6;
            ipv6WithDecimalNotation = ipv4matcher.find();
            addressBytes = extractIPv6Segments(address);
            netmaskString = ipv6matcher.group(GROUP_NETMASK);
        } else {
            throw new InvalidParameterException("invalid IP address: " + address);
        }
        if (StringUtils.isNoneBlank(netmaskString)) {
            netmaskLength = Integer.parseInt(netmaskString);
        } else {
            netmaskLength = 0;
        }
    }

    public IPAddress(String address, String netmask) throws InvalidParameterException {
        Matcher ipv4matcher = IP_V4_NO_NETMASK_VALIDATION_PATTERN.matcher(address);
        Matcher ipv6matcher = IP_V6_NO_NETMASK_VALIDATION_PATTERN.matcher(address);

        if (ipv4matcher.matches()) {
            version = IPVersion.IPv4;
            addressBytes = extractIPv4Segments(ipv4matcher);
            netmaskLength = defineNetmaskWidthFromAddressPattern(netmask);
        } else if (ipv6matcher.matches()) {
            version = IPVersion.IPv6;
            ipv6WithDecimalNotation = ipv4matcher.find();
            addressBytes = extractIPv6Segments(address);
            netmaskLength = Integer.parseInt(netmask);
        } else {
            throw new InvalidParameterException("invalid IP address: " + address);
        }

    }

    private static int defineNetmaskWidthFromAddressPattern(String netmask) throws InvalidParameterException {
        Matcher netmaskMatcher = IP_V4_NO_NETMASK_VALIDATION_PATTERN.matcher(netmask);
        if (!netmaskMatcher.matches()) {
            throw new InvalidParameterException("invalid network mask: " + netmask);
        }
        short[] netmaskBytes = extractIPv4Segments(netmaskMatcher);
        int oneBitCount = 0;
        for (short currentByte : netmaskBytes) {
            int currentBitCount = Integer.bitCount(currentByte);
            if ((currentByte << currentBitCount & 0xFF) != 0) {
                throw new InvalidParameterException("invalid network mask: " + netmask);
            }
            oneBitCount += currentBitCount;
        }
        return oneBitCount;
    }

    private static short[] extractIPv6Segments(String address) {
        short[] ipv6AddressBytes = new short[16];

        Matcher ipv6segmentMatcher = IP_V6_SEGMENT_PATTERN.matcher(address);
        Matcher ipv6withDecNotationSegmentMatcher = IP_V6_WITH_DEC_NOTATION_SEGMENT_PATTERN.matcher(address);

        if (ipv6withDecNotationSegmentMatcher.find()) {
            initIPv6SegmentBytes(ipv6withDecNotationSegmentMatcher, ipv6AddressBytes, 0, 6);
            initIPv6SegmentBytesFromDecimalNotation(ipv6withDecNotationSegmentMatcher, ipv6AddressBytes);
        } else if (ipv6segmentMatcher.find()) {
            initIPv6SegmentBytes(ipv6segmentMatcher, ipv6AddressBytes, 0, 8);
        }
        return ipv6AddressBytes;
    }

    private static void initIPv6SegmentBytesFromDecimalNotation(Matcher ipv6matcher, short[] ipv6AddressBytes) {
        short[] ipv4Bytes = extractIPv4Segments(ipv6matcher);

        for (int i = 0; i < ipv4Bytes.length; i++) {
            ipv6AddressBytes[12 + i] = ipv4Bytes[i];
        }
    }

    private static void initIPv6SegmentBytes(Matcher ipv6matcher, short[] ipv6AddressBytes, int startIdx,
            int stopIdxExcluded) {
        short[] segmentBytes;
        for (int i = startIdx; i < stopIdxExcluded; i++) {
            segmentBytes = parseIPv6Segment(ipv6matcher, GROUP_SEGMENT[i]);
            ipv6AddressBytes[2 * i] = segmentBytes[0];
            ipv6AddressBytes[2 * i + 1] = segmentBytes[1];
        }
    }

    private static short[] parseIPv6Segment(Matcher matcher, String groupName) {
        String segmentString = matcher.group(BEFORE_ZERO_SEQ_PLACEHOLDER + groupName);
        if (StringUtils.isBlank(segmentString)) {
            segmentString = matcher.group(AFTER_ZERO_SEQ_PLACEHOLDER + groupName);
        }
        int segment = StringUtils.isBlank(segmentString) ? 0 : Integer.parseInt(segmentString, 16);
        short[] byteValues = new short[2];
        byteValues[0] = (short) (segment / 256);
        byteValues[1] = (short) (segment % 256);
        return byteValues;
    }

    private static short[] extractIPv4Segments(Matcher matcher) {
        String group0 = matcher.group(GROUP_BYTE[0]);
        String group1 = matcher.group(GROUP_BYTE[1]);
        String group2 = matcher.group(GROUP_BYTE[2]);
        String group3 = matcher.group(GROUP_BYTE[3]);
        short[] ipv4AddressBytes = null;
        if (StringUtils.isNoneBlank(group0) && StringUtils.isNoneBlank(group1) && StringUtils.isNoneBlank(group2)
                && StringUtils.isNoneBlank(group3)) {
            ipv4AddressBytes = new short[4];
            ipv4AddressBytes[0] = Short.parseShort(group0);
            ipv4AddressBytes[1] = Short.parseShort(group1);
            ipv4AddressBytes[2] = Short.parseShort(group2);
            ipv4AddressBytes[3] = Short.parseShort(group3);
        }
        return ipv4AddressBytes;
    }

    public IPVersion getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return asString(addressBytes, true, false, ipv6WithDecimalNotation);
    }

    private String asString(short[] bytes, boolean appendNetmask, boolean ipv6Compact, boolean ipv6DecimalNotation) {
        StringBuffer address;
        if (IPVersion.IPv4 == version) {
            address = v4StringBuffer(bytes);
        } else {
            address = v6StringBuffer(bytes, ipv6Compact, ipv6DecimalNotation);
        }
        if (appendNetmask) {
            address.append("/" + netmaskLength);
        }
        return address.toString();
    }

    private StringBuffer v4StringBuffer(short[] bytes) {
        StringBuffer address;
        address = new StringBuffer();
        address.append(createIpv4AddressString(bytes));
        return address;
    }

    private StringBuffer v6StringBuffer(short[] bytes, boolean normalize, boolean ipv6DecimalNotation) {
        StringBuffer address;
        address = new StringBuffer();
        int maxZeroSeqPos = -1;
        int maxZeroSeqLength = 0;
        int currentZeroSeqPos = -1;
        int currentZeroSeqLength = 0;

        int[] segments = new int[ipv6DecimalNotation ? 6 : 8];
        for (int i = 0; i < segments.length; i++) {
            segments[i] = 256 * bytes[2 * i] + bytes[2 * i + 1];
            if (segments[i] == 0) {
                if (currentZeroSeqPos < 0) {
                    currentZeroSeqPos = i;
                    currentZeroSeqLength = 1;
                } else {
                    currentZeroSeqLength++;
                }
            } else {
                currentZeroSeqPos = -1;
                currentZeroSeqLength = 0;
            }
            if (currentZeroSeqLength > maxZeroSeqLength) {
                maxZeroSeqPos = currentZeroSeqPos;
                maxZeroSeqLength = currentZeroSeqLength;
            }
        }
        for (int i = 0; i < segments.length; i++) {
            if (!normalize || i < maxZeroSeqPos || i >= maxZeroSeqPos + maxZeroSeqLength) {
                address.append(String.format((i == 0 ? "" : ":") + "%x", segments[i]));
            } else if (normalize && i == maxZeroSeqPos && maxZeroSeqPos + maxZeroSeqLength >= 8) {
                address.append(IP_V6_ZERO_REGEX);
            } else if (normalize && i == maxZeroSeqPos) {
                address.append(":");
            }
        }
        if (ipv6DecimalNotation) {
            address.append(":").append(createIpv4AddressString(ArrayUtils.subarray(bytes, 12, 16)));
        }
        return address;
    }

    private String createIpv4AddressString(short[] segments) {
        return StringUtils.join(segments, '.');
    }

    /** Check whether this address is a loopback address such as 127.0.0.1 (localhost) */
    public boolean isLoopbackAddress() {
        boolean loopbackAddress;
        if (IPVersion.IPv4 == version) {
            loopbackAddress = addressBytes[0] == 127;
        } else {
            loopbackAddress = true;
            for (int i = 0; i < 15; i++) {
                if (addressBytes[i] != 0) {
                    loopbackAddress = false;
                    break;
                }
            }
            if (loopbackAddress && 1 != addressBytes[15]) {
                loopbackAddress = false;
            }
        }
        return loopbackAddress;
    }

    /**
     * Check whether the address is the unspecified address (IPv6: "::/128", IPv4: "0.0.0.0/8") - with or without
     * network mask - which is used as source if the IP address is not known yet.
     */
    public boolean isUnspecifiedAddress() {
        if (version == IPVersion.IPv4 && addressBytes[0] == 0 && addressBytes[1] == 0 && addressBytes[2] == 0
                && addressBytes[3] == 0 && (netmaskLength == 0 || netmaskLength == 8)) {
            return true;
        } else if (version == IPVersion.IPv6 && addressBytes[0] == 0 && addressBytes[1] == 0 && addressBytes[2] == 0
                && addressBytes[3] == 0 && addressBytes[4] == 0 && addressBytes[5] == 0 && addressBytes[6] == 0
                && addressBytes[7] == 0 && addressBytes[8] == 0 && addressBytes[9] == 0 && addressBytes[10] == 0
                && addressBytes[11] == 0 && addressBytes[12] == 0 && addressBytes[13] == 0 && addressBytes[14] == 0
                && addressBytes[15] == 0 && (netmaskLength == 0 || netmaskLength == 128)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof IPAddress)) {
            return false;
        } else {
            short[] otherAddressBytes = ((IPAddress) other).addressBytes;
            if (addressBytes.length != otherAddressBytes.length) {
                return false;
            }
            for (int i = 0; i < addressBytes.length && i < otherAddressBytes.length; i++) {
                if (addressBytes[i] != otherAddressBytes[i]) {
                    return false;
                }
            }
            if (netmaskLength != ((IPAddress) other).netmaskLength) {
                return false;
            }
            return true;
        }
    }

    @Override
    public int hashCode() {
        return addressBytes.hashCode();
    }

    public static IPVersion defineVersion(String ipAddress) {
        if (IP_V4_OPTIONAL_NETMASK_VALIDATION_PATTERN.matcher(ipAddress).matches()
                || IP_V4_NO_NETMASK_VALIDATION_PATTERN.matcher(ipAddress).matches()) {
            return IPVersion.IPv4;
        } else if (IP_V6_OPTIONAL_NETMASK_VALIDATION_PATTERN.matcher(ipAddress).matches()
                || IP_V6_NO_NETMASK_VALIDATION_PATTERN.matcher(ipAddress).matches()) {
            return IPVersion.IPv6;
        }
        return null;
    }

    public static boolean verifyAddress(String ipAddress) {
        return verifyAddress(ipAddress, IPVersion.IPv4) || verifyAddress(ipAddress, IPVersion.IPv6);
    }

    public static boolean verifyAddress(String ipAddress, IPVersion ipVersion) {
        return IPVersion.IPv4 == ipVersion && IP_V4_NO_NETMASK_VALIDATION_PATTERN.matcher(ipAddress).matches()
                || IPVersion.IPv6 == ipVersion && IP_V6_NO_NETMASK_VALIDATION_PATTERN.matcher(ipAddress).matches();
    }

    public static boolean verifyNetmaskDefinition(String netmaskDefinition) {
        return verifyNetmaskDefinition(netmaskDefinition, IPVersion.IPv4)
                || verifyNetmaskDefinition(netmaskDefinition, IPVersion.IPv6);
    }

    public static boolean verifyNetmaskDefinition(String netmaskDefinition, IPVersion ipVersion) {
        boolean verified = false;
        Matcher ipv4Matcher = IP_V4_NO_NETMASK_VALIDATION_PATTERN.matcher(netmaskDefinition);
        if (IPVersion.IPv4 == ipVersion && ipv4Matcher.matches()) {
            try {
                defineNetmaskWidthFromAddressPattern(netmaskDefinition);
                verified = true;
            } catch (InvalidParameterException e) {}
        } else if (IPVersion.IPv6 == ipVersion) {
            try {
                int netmaskSuffixAsInt = Integer.parseInt(netmaskDefinition);
                verified = netmaskSuffixAsInt > 0 && netmaskSuffixAsInt <= 128;
            } catch (NumberFormatException e) {}
        }
        return verified;
    }

    /** Retrieve IP address without netmask as string. */
    public String address() {
        return asString(addressBytes, false, true, ipv6WithDecimalNotation);
    }

    /** Retrieve network part of IP address (without netmask) as String */
    public String networkAddress() {
        short[] networkBytes = new short[addressBytes.length];
        short[] netmaskBytes = netmaskBytes(addressBytes.length, netmaskLength);
        for (int i = 0; i < networkBytes.length; i++) {
            networkBytes[i] = (short) (addressBytes[i] & netmaskBytes[i]);
        }
        return asString(networkBytes, false, false, false);
    }

    /** Retrieve netmask as String representation, e.g. 255.255.0.0 (IPv4) or 64 (IPv6) */
    public String netmask() {
        String netmaskAsString;
        if (IPVersion.IPv4 == version) {
            short[] netmaskBytes = netmaskBytes(addressBytes.length, netmaskLength);
            netmaskAsString = asString(netmaskBytes, false, false, false);
        } else {
            netmaskAsString = String.valueOf(netmaskLength);
        }
        return netmaskAsString;
    }

    public int netmaskLength() {
        return netmaskLength;
    }

    private short[] netmaskBytes(int totalBytes, int netmaskBits) {
        short[] netmaskBytes = new short[totalBytes];
        for (int i = 0; i < netmaskBytes.length; i++) {
            int byteMask;
            int maskRemainder = netmaskBits - i * 8;
            if (maskRemainder >= 8) {
                byteMask = 0xFF;
            } else if (maskRemainder > 0) {
                byteMask = 0xFF << (8 - maskRemainder);
            } else {
                byteMask = 0;
            }
            netmaskBytes[i] = (short) (byteMask % 0x100);
        }
        return netmaskBytes;
    }

    /**
     * Returns a normalized representation of the address:
     * <ul>
     * <li>IPv4: four segments and the network mask, e.g. 10.0.49.3/8</li>
     * <li>IPv6: address without 0-sequence remaining IPv4 representation and network mask, e.g. 1::44:33/64 or
     * 1::10.0.49.3/64</li>
     * </ul>
     * 
     * @return
     */
    public String normalizedString() {
        return asString(addressBytes, true, true, ipv6WithDecimalNotation);
    }

    public String normalizedAddress() {
        return asString(addressBytes, false, true, ipv6WithDecimalNotation);
    }

    /**
     * Returns the standard representation of the address:
     * <ul>
     * <li>IPv4: four segments, e.g. 10.0.49.3</li>
     * <li>IPv6: address with 0-sequence, e.g. 1:0:0:0:0:0:44:33</li>
     * </ul>
     * 
     * @return
     */
    public String standardAddress() {
        return asString(addressBytes, false, false, false);
    }

    public static IPAddress defaultAddress(IPVersion ipVersion) {
        IPAddress defaultAddress = null;
        try {
            if (IPVersion.IPv4 == ipVersion) {
                defaultAddress = new IPAddress("192.168.0.1", "255.255.255.0");
            } else {
                defaultAddress = new IPAddress("::192.168.0.1", "120");
            }
        } catch (InvalidParameterException e) {}
        return defaultAddress;
    }

    public static IPAddress unspecifiedAddress(IPVersion ipVersion) {
        IPAddress unspecifiedAddress = null;
        try {
            if (IPVersion.IPv4 == ipVersion) {
                unspecifiedAddress = new IPAddress("0.0.0.0", "255.255.255.255");
            } else {
                unspecifiedAddress = new IPAddress("::", "128");
            }
        } catch (InvalidParameterException e) {}
        return unspecifiedAddress;
    }

    public static IPAddress globalBroadcast(IPVersion ipVersion) {
        IPAddress globalBroadcast = null;
        try {
            if (IPVersion.IPv4 == ipVersion) {
                globalBroadcast = new IPAddress("255.255.255.255", "0.0.0.0");
            } else {
                globalBroadcast = new IPAddress("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff", "8");
            }
        } catch (InvalidParameterException e) {}
        return globalBroadcast;
    }

    public static IPAddress defaultRoute(IPVersion ipVersion) {
        IPAddress unspecifiedAddress = null;
        try {
            if (IPVersion.IPv4 == ipVersion) {
                unspecifiedAddress = new IPAddress("0.0.0.0", "0.0.0.0");
            } else {
                unspecifiedAddress = new IPAddress("::", "0");
            }
        } catch (InvalidParameterException e) {}
        return unspecifiedAddress;
    }

    public static IPAddress localhost(IPVersion ipVersion) {
        IPAddress unspecifiedAddress = null;
        try {
            if (IPVersion.IPv4 == ipVersion) {
                unspecifiedAddress = new IPAddress("127.0.0.1", "255.0.0.0");
            } else {
                unspecifiedAddress = new IPAddress("::1", "128");
            }
        } catch (InvalidParameterException e) {}
        return unspecifiedAddress;
    }

    public static IPAddress localhostNetwork(IPVersion ipVersion) {
        IPAddress unspecifiedAddress = null;
        try {
            if (IPVersion.IPv4 == ipVersion) {
                unspecifiedAddress = new IPAddress("127.0.0.0", "255.0.0.0");
            } else {
                unspecifiedAddress = new IPAddress("::1", "128");
            }
        } catch (InvalidParameterException e) {}
        return unspecifiedAddress;
    }

    public boolean equalNetwork(String address) {
        IPAddress other = null;
        try {
            other = new IPAddress(address, this.netmask());
        } catch (InvalidParameterException e) {}
        return other != null && this.networkAddress().equals(other.networkAddress());
    }

    public boolean equalAddress(String address) {
        IPAddress other = null;
        try {
            other = new IPAddress(address, this.netmask());
        } catch (InvalidParameterException e) {}
        return other != null && this.standardAddress().equalsIgnoreCase(other.standardAddress());
    }

    public boolean isGlobalBroadcast() {
        boolean isGlobalBroadcast = false;
        try {
            IPAddress globalBroadcast;
            if (IPVersion.IPv4 == version) {
                globalBroadcast = new IPAddress("255.255.255.255", "0.0.0.0");
            } else {
                globalBroadcast = new IPAddress("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff", "8");
            }
            isGlobalBroadcast = normalizedAddress().equalsIgnoreCase(globalBroadcast.normalizedAddress());
        } catch (InvalidParameterException e) {}
        return isGlobalBroadcast;
    }

    public boolean isLocalBroadcast() {
        boolean isLocalBroadcast = true;
        int numberOfBitsToCompare;
        if (IPVersion.IPv4 == version) {
            numberOfBitsToCompare = 32 - netmaskLength();
        } else {
            numberOfBitsToCompare = 128 - netmaskLength();
        }
        for (int currentByteIdx = 1; numberOfBitsToCompare > 0
                && isLocalBroadcast; currentByteIdx++, numberOfBitsToCompare -= 8) {
            int currentByte = addressBytes[addressBytes.length - currentByteIdx];
            if (numberOfBitsToCompare >= 8) {
                if (currentByte != 255) {
                    isLocalBroadcast = false;
                }
            } else {
                int byteMask = (int) Math.pow(2, numberOfBitsToCompare) - 1;
                if ((currentByte & byteMask) != (255 & byteMask)) {
                    isLocalBroadcast = false;
                }
            }
        }
        return isLocalBroadcast;
    }
}
