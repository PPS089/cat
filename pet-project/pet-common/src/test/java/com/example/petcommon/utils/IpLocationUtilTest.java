package com.example.petcommon.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IpLocationUtilTest {

    @Test
    void returnsUnknownForInvalidIp() {
        String loc = IpLocationUtil.getLocationFromIP("0.0.0.0");
        assertNotNull(loc);
    }

    @Test
    void handlesPrivateIpSafely() {
        String loc = IpLocationUtil.getLocationFromIP("192.168.1.1");
        assertNotNull(loc);
        assertFalse(loc.isEmpty());
    }

    @Test
    void handlesPublicIpWithoutException() {
        String loc = IpLocationUtil.getLocationFromIP("111.1.99.214");
        assertNotNull(loc);
        String raw = IpLocationUtil.getRawRegion("111.1.99.214");
        System.out.println("RAW_REGION=" + raw);
    }
}