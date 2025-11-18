package com.example.petcommon.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 简化版IP定位服务
 * 仅使用本地ip2region数据库进行IP定位
 */
@Service
public class EnterpriseIpLocationService {
    
    private static final Logger log = LoggerFactory.getLogger(EnterpriseIpLocationService.class);
    
    /**
     * IP定位结果
     */
    public static class PreciseLocation {
        private String country;
        private String province;
        private String city;
        private String district;  // 区县
        private String street;    // 街道
        private String isp;
        private String operator;  // 运营商
        private double latitude;  // 纬度
        private double longitude; // 经度
        private String adcode;    // 行政区划代码
        private String source;    // 数据来源
        private int accuracy;     // 精度等级
        private boolean success;
        
        public PreciseLocation() {
            this.success = false;
        }
        
        public PreciseLocation(String source) {
            this.source = source;
            this.success = true;
        }
        
        // Getters and setters
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        
        public String getProvince() { return province; }
        public void setProvince(String province) { this.province = province; }
        
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        
        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }
        
        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }
        
        public String getIsp() { return isp; }
        public void setIsp(String isp) { this.isp = isp; }
        
        public String getOperator() { return operator; }
        public void setOperator(String operator) { this.operator = operator; }
        
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }
        
        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
        
        public String getAdcode() { return adcode; }
        public void setAdcode(String adcode) { this.adcode = adcode; }
        
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        
        public int getAccuracy() { return accuracy; }
        public void setAccuracy(int accuracy) { this.accuracy = accuracy; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getFormattedAddress() {
            StringBuilder sb = new StringBuilder();
            if (country != null) sb.append(country);
            if (province != null) sb.append(province);
            if (city != null) sb.append(city);
            if (district != null) sb.append(district);
            if (street != null) sb.append(street);
            return sb.toString();
        }
        
        @Override
        public String toString() {
            return String.format("%s %s %s %s %s (%.6f, %.6f) [%s]", 
                    country, province, city, district, street, 
                    latitude, longitude, source);
        }
    }
    
    /**
     * 获取IP定位信息（使用本地ip2region数据库）
     */
    public PreciseLocation getPreciseLocation(String ip) {
        if (!IpUtil.isValidPublicIp(ip)) {
            log.warn("IP地址 {} 无效或为内网地址", ip);
            return new PreciseLocation();
        }
        
        try {
            // 使用本地ip2region数据库进行定位
            IpLocationUtil.LocationInfo locationInfo = IpLocationUtil.getDetailedLocation(ip);
            if (!locationInfo.isValid()) {
                return new PreciseLocation();
            }
            
            // 解析定位结果
            PreciseLocation location = new PreciseLocation("ip2region");
            location.setCountry(locationInfo.getCountry());
            location.setProvince(locationInfo.getProvince());
            location.setCity(locationInfo.getCity());
            location.setIsp(locationInfo.getIsp());
            
            // 设置精度等级（ip2region通常精确到城市级别）
            location.setAccuracy(3);
            location.setSuccess(true);
            
            return location;
        } catch (Exception e) {
            log.error("IP定位失败: {}", e.getMessage());
            return new PreciseLocation();
        }
    }
    
    /**
     * 批量IP定位
     */
    public java.util.List<PreciseLocation> batchLocate(java.util.List<String> ips) {
        return ips.parallelStream()
                .map(this::getPreciseLocation)
                .collect(java.util.stream.Collectors.toList());
    }
}