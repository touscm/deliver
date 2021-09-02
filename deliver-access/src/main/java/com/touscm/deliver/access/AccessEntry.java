package com.touscm.deliver.access;

import java.util.Date;

public class AccessEntry {
    /**
     * 请求类型
     */
    private String accessType;
    /**
     * 请求名称
     */
    private String accessName;
    /**
     * 请求地址
     */
    private String accessUrl;

    /**
     * 用户
     */
    private String username;
    /**
     * 手机号
     */
    private String mobile;

    /**
     * 请求聂荣
     */
    private String reqBody;
    /**
     * 响应内容
     */
    private String resBody;

    /**
     * 经度
     */
    private double longitude;
    /**
     * 纬度
     */
    private double latitude;

    /**
     * 省份
     */
    private String province;
    /**
     * 城市
     */
    private String city;
    /**
     * 行政区
     */
    private String region;
    /**
     * 详细地址
     */
    private String address;

    /**
     * 请求方法
     */
    private String method;
    /**
     * IP地址
     */
    private String ip;
    /**
     * UserAgent
     */
    private String userAgent;
    /**
     * 设备类型
     */
    private String deviceType;
    /**
     * 设备型号
     */
    private String deviceModel;

    /**
     * 访问时间
     */
    private Date accessTime;

    /* ...... */

    public AccessEntry() {
    }

    public AccessEntry(String accessType, String accessName, String accessUrl, String username, String mobile, String ip, double longitude, double latitude, String userAgent, String deviceType, String deviceModel) {
        this.accessType = accessType;
        this.accessName = accessName;
        this.accessUrl = accessUrl;
        this.username = username;
        this.mobile = mobile;
        this.longitude = longitude;
        this.latitude = latitude;
        this.province = "";
        this.city = "";
        this.region = "";
        this.address = "";
        this.method = "POST";
        this.ip = ip;
        this.userAgent = userAgent;
        this.deviceType = deviceType;
        this.deviceModel = deviceModel;
        this.accessTime = new Date();
    }

    public AccessEntry(String accessType, String accessName, String accessUrl, String method, String username, String mobile, String ip, double longitude, double latitude, String province, String city, String region, String address, String userAgent, String deviceType, String deviceModel, Date accessTime) {
        this.accessType = accessType;
        this.accessName = accessName;
        this.accessUrl = accessUrl;
        this.username = username;
        this.mobile = mobile;
        this.longitude = longitude;
        this.latitude = latitude;
        this.province = province;
        this.city = city;
        this.region = region;
        this.address = address;
        this.method = method;
        this.ip = ip;
        this.userAgent = userAgent;
        this.deviceType = deviceType;
        this.deviceModel = deviceModel;
        this.accessTime = accessTime;
    }

    /* ...... */

    public String getAccessType() {
        return accessType;
    }

    public AccessEntry setAccessType(String accessType) {
        this.accessType = accessType;
        return this;
    }

    public String getAccessName() {
        return accessName;
    }

    public AccessEntry setAccessName(String accessName) {
        this.accessName = accessName;
        return this;
    }

    public String getAccessUrl() {
        return accessUrl;
    }

    public AccessEntry setAccessUrl(String accessUrl) {
        this.accessUrl = accessUrl;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public AccessEntry setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getMobile() {
        return mobile;
    }

    public AccessEntry setMobile(String mobile) {
        this.mobile = mobile;
        return this;
    }

    public String getReqBody() {
        return reqBody;
    }

    public AccessEntry setReqBody(String reqBody) {
        this.reqBody = reqBody;
        return this;
    }

    public String getResBody() {
        return resBody;
    }

    public AccessEntry setResBody(String resBody) {
        this.resBody = resBody;
        return this;
    }

    public double getLongitude() {
        return longitude;
    }

    public AccessEntry setLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }

    public double getLatitude() {
        return latitude;
    }

    public AccessEntry setLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public String getProvince() {
        return province;
    }

    public AccessEntry setProvince(String province) {
        this.province = province;
        return this;
    }

    public String getCity() {
        return city;
    }

    public AccessEntry setCity(String city) {
        this.city = city;
        return this;
    }

    public String getRegion() {
        return region;
    }

    public AccessEntry setRegion(String region) {
        this.region = region;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public AccessEntry setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getMethod() {
        return method;
    }

    public AccessEntry setMethod(String method) {
        this.method = method;
        return this;
    }

    public String getIp() {
        return ip;
    }

    public AccessEntry setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public AccessEntry setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public AccessEntry setDeviceType(String deviceType) {
        this.deviceType = deviceType;
        return this;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public AccessEntry setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
        return this;
    }

    public Date getAccessTime() {
        return accessTime;
    }

    public AccessEntry setAccessTime(Date accessTime) {
        this.accessTime = accessTime;
        return this;
    }

    /* ...... */

    /*public String toString() {
        return EntryUtils.toString(this);
    }*/
}
