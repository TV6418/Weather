package com.weihung.weather.bean;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class WeatherRequest {

    private String Authorization;
    private String format;
    private String locationName;
    private String elementName;
    private String sort;
    private String timeFrom;
    private String timeTo;
    private String limit;
    private String offset;

    // Getter 和 Setter 方法
    public String getAuthorization() {
        return Authorization;
    }

    public void setAuthorization(String authorization) {
        Authorization = authorization;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getTimeFrom() {
        return timeFrom;
    }

    public void setTimeFrom(String timeFrom) {
        this.timeFrom = timeFrom;
    }

    public String getTimeTo() {
        return timeTo;
    }

    public void setTimeTo(String timeTo) {
        this.timeTo = timeTo;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        try {
            if (Authorization != null) {
                sb.append("Authorization=").append(Authorization).append("&");
            }

            if (format != null) {
                sb.append("format=").append(format).append("&");
            }

            if (locationName != null) {
                sb.append("locationName=").append(URLEncoder.encode(locationName, "UTF-8")).append("&");
            }

            if (elementName != null) {
                sb.append("elementName=").append(elementName).append("&");
            }

            if (sort != null) {
                sb.append("sort=").append(sort).append("&");
            }

            if (timeFrom != null) {
                sb.append("timeFrom=").append(timeFrom).append("&");
            }

            if (timeTo != null) {
                sb.append("timeTo=").append(timeTo).append("&");
            }

            if (limit != null) {
                sb.append("limit=").append(limit).append("&");
            }

            if (offset != null) {
                sb.append("offset=").append(offset).append("&");
            }

            // 刪除最後一個"&"
            if (sb.charAt(sb.length() - 1) == '&') {
                sb.deleteCharAt(sb.length() - 1);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
}
