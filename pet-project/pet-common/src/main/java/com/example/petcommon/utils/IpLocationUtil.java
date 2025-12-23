package com.example.petcommon.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import lombok.Getter;
import org.lionsoul.ip2region.xdb.Searcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 33185
 */
public class IpLocationUtil {
    private static final Logger log = LoggerFactory.getLogger(IpLocationUtil.class);
    private static final AtomicReference<byte[]> C_BUFF_REF = new AtomicReference<>();
    private static final ThreadLocal<Searcher> TL = ThreadLocal.withInitial(() -> {
        try {
            ensureLoaded();
            byte[] cBuff = C_BUFF_REF.get();
            if (cBuff == null || cBuff.length == 0) {
                return null;
            }
            return Searcher.newWithBuffer(cBuff);
        } catch (Exception e) {
            log.error("ip2region初始化失败: {}", e.getMessage());
            return null;
        }
    });

    @Getter
    public static class LocationInfo {
        // Getters
        private String country;
        private String province;
        private String city;
        private String isp;
        final private boolean valid;

        public LocationInfo() { 
            this.valid = false; 
        }
        
        public LocationInfo(String country, String province, String city, String isp) { 
            this.country = country; 
            this.province = province; 
            this.city = city; 
            this.isp = isp; 
            this.valid = true; 
        }

        public String getSimpleLocation() {
            if (!valid) {
                return "未知";
            }
            StringBuilder sb = new StringBuilder(); 
            if (province != null && !province.isEmpty()) {
                sb.append(province);
            }
            if (city != null && !city.isEmpty() && !city.equals(province)) {
                sb.append(city);
            }
            return !sb.isEmpty() ? sb.toString() : "未知";
        }
    }

    public static String getLocationFromIP(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return "未知";
        }
        LocationInfo info = getDetailedLocation(ipAddress);
        return info.getSimpleLocation();
    }


    public static LocationInfo getDetailedLocation(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty() || Objects.equals(ipAddress, "0.0.0.0")) {
            return new LocationInfo();
        }
        
        // 使用ip2region本地数据库
        try {
            return getLocalLocation(ipAddress);
        } catch (Exception e) {
            log.error("IP定位查询失败: {}", e.getMessage());
            return new LocationInfo();
        }
    }

    private static LocationInfo getLocalLocation(String ipAddress) {
        try {
            Searcher searcher = TL.get();
            if (searcher == null) {
                log.warn("IP定位服务未初始化");
                return new LocationInfo();
            }
            
            String region = searcher.search(ipAddress);
            if (region == null || region.trim().isEmpty()) {
                return new LocationInfo();
            }
            
            String[] parts = region.split("\\|");
            // 确保数组有足够的元素，避免索引越界
            if (parts.length < 4) {
                // 如果数组长度不足，创建一个新数组并填充空字符串
                String[] newParts = new String[4];
                System.arraycopy(parts, 0, newParts, 0, parts.length);
                for (int i = parts.length; i < 4; i++) {
                    newParts[i] = "";
                }
                parts = newParts;
            }
            
            String country = norm(parts[0]);
            String province = norm(parts[1]);
            String city = norm(parts[2]);
            String isp = norm(parts[3]);
            
            return new LocationInfo(country, province, city, isp);
        } catch (Exception e) {
            log.error("ip2region查询失败: {}", e.getMessage());
            return new LocationInfo();
        }
    }

    private static void ensureLoaded() throws Exception {
        byte[] cBuff = C_BUFF_REF.get();
        if (cBuff != null) {
            return;
        }
        synchronized (IpLocationUtil.class) {
            cBuff = C_BUFF_REF.get();
            if (cBuff != null) {
                return;
            }
            try (InputStream is = IpLocationUtil.class.getClassLoader().getResourceAsStream("ip2region/ip2region_v4.xdb")) {
                if (is == null) {
                    log.warn("未在classpath找到 ip2region/ip2region_v4.xdb，已禁用离线IP定位");
                    C_BUFF_REF.set(null);
                    return;
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int n;
                while ((n = is.read(buf)) != -1) {
                    baos.write(buf, 0, n);
                }
                byte[] newBuff = baos.toByteArray();
                C_BUFF_REF.set(newBuff);
                try {
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    byte[] dig = md.digest(newBuff);
                    StringBuilder sb = new StringBuilder();
                    for (byte b : dig) {
                        sb.append("%02x".formatted(b));
                    }
                    log.info("ip2region xdb加载完成, path=classpath:ip2region/ip2region_v4.xdb, size={}, md5={}", newBuff.length, sb);
                } catch (java.security.NoSuchAlgorithmException ignore) {}
            }
        }
    }

    private static String norm(String s) {
        if (s == null) {
            return "";
        }
        s = s.trim();
        if ("0".equals(s) || "未知".equals(s)) {
            return "";
        }
        return s;
    }
}