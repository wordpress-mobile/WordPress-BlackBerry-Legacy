package com.wordpress.utils.http;

public class Cookie {

    private String name;
    
    private String value;
    
    private String domain;
    
    private String path;
    
    private boolean isSecure;
    
    private String cookie;

    public Cookie(String cookieString)throws CookieException {
        parseCookieString(cookieString);
        
        cookie = name;
        if (!isEmpty(value)) {
            cookie += "=" + value;
        }
    }
    


	public boolean matches(String urlString) throws CookieException {
        Url url = parseUrl(urlString);
        
        if (isSecure && !"https".equalsIgnoreCase(url.protocol)) {
            return false;
        }
        
        if (!isEmpty(domain) && (url.host == null || !url.host.endsWith(domain))) {
            return false;
        }
        
        if (!isEmpty(path) && (url.path == null || !url.path.startsWith(path))) {
            return false;
        }
        
        return true;
    }
    
    public String getCookieString() {
        return cookie;
    }
    
    public String getName() {
		return name;
	}
    
    private void parseCookieString(String cookieString) throws CookieException {
        AttributeValue av;
        
        av = getNextAttributeValue(cookieString, 0);
        name = av.attribute;
        value = av.value;
        
        while (av.lastIndex < cookieString.length()) {
            av = getNextAttributeValue(cookieString, av.lastIndex);
            setCookieAttribute(av);
        }
    }
    
    private void setCookieAttribute(AttributeValue av) {
        if ("Domain".equalsIgnoreCase(av.attribute)) {
            domain = av.value;
        } else if ("Path".equalsIgnoreCase(av.attribute)) {
            path = av.value;
        } else if ("Secure".equalsIgnoreCase(av.attribute)) {
            isSecure = true;
        }
    }
    
    private static AttributeValue getNextAttributeValue(String cookieString, int startIndex) 
            throws CookieException {
        
        int state = 0;
        int delimiterIndex = 0;
        int avEndIndex = 0;
        int valueStartIndex = 0;
        int valueEndIndex = 0;
        int lastIndex = 0;
        String attribute = null;
        String value = null;
        AttributeValue result = null;
        
        while (result == null) {
            switch (state) {
                case 0:
                    delimiterIndex = cookieString.indexOf('=', startIndex);
                    avEndIndex = cookieString.indexOf(';', startIndex);
                    state = (delimiterIndex == -1 || (avEndIndex != -1 && delimiterIndex > avEndIndex)) ? 1 : 2;
                    break;
                case 1:
                    // Attribute doesn't have a value
                    if (avEndIndex == -1) {
                        avEndIndex = cookieString.length();
                    }
                    attribute = cookieString.substring(startIndex, avEndIndex).trim();
                    result = new AttributeValue(attribute, avEndIndex+1);
                    break;
                case 2:
                    // Attribute has a value
                    
                    // Extract attribute
                    attribute = cookieString.substring(startIndex, delimiterIndex).trim();
                    
                    // Do check if value is quoted
                    valueStartIndex = delimiterIndex;
                    char ch;
                    
                    do {
                        // TODO: Check index range
                        ch = cookieString.charAt(++valueStartIndex);
                    } while (ch == ' ' || ch == '\t');
                    
                    state = (ch == '"') ? 3 : 4;
                    break;
                case 3:
                    // Value is quoted
                    valueEndIndex = cookieString.indexOf('"', valueStartIndex+1);
                    if (valueEndIndex == -1) {
                        throw new CookieException("Value in the cookie is not properly quoted: " + cookieString);
                    }
                    value = cookieString.substring(valueStartIndex+1, valueEndIndex);
                    lastIndex = cookieString.indexOf(';', valueEndIndex);
                    lastIndex = (lastIndex != -1) ? lastIndex + 1 : cookieString.length();
                    state = 7;
                    break;
                case 4:
                    // Value is not quoted
                    valueEndIndex = cookieString.indexOf(';', valueStartIndex);
                    state = (valueEndIndex != -1) ? 5 : 6; 
                    break;
                case 5:
                    // Attribute-Value seems to be not last
                    value  = cookieString.substring(valueStartIndex, valueEndIndex);
                    lastIndex = valueEndIndex + 1;
                    state = 7;
                    break;
                case 6:
                    // Attribute-Value is last
                    value = cookieString.substring(valueStartIndex);
                    lastIndex = cookieString.length();
                    state = 7;
                    break;
                case 7:
                    result = new AttributeValue(attribute, value, lastIndex);
                    break;
                default:
                    throw new CookieException("Internal error during parsing cookie: " + cookieString);
            }
        }
        
        return result;
    }
    
    private static Url parseUrl(String urlString) throws CookieException  {
        int state = 0;
        int protocolIndex = 0;
        int pathStartIndex = 0;
        String protocol = null;
        String host = null;
        String path = null;
        Url result = null;
        
        while (result == null) {
            switch (state) {
                case 0:
                    // Get protocol
                    protocolIndex = urlString.indexOf("://");
                    if (protocolIndex == -1) {
                        throw new CookieException("Protocol is missing: " + urlString);
                    }
                    protocol = urlString.substring(0, protocolIndex);
                    
                    pathStartIndex = urlString.indexOf('/', protocolIndex+3);
                    state = (pathStartIndex == -1) ? 1 : 2; 
                    break;
                case 1:
                    pathStartIndex = urlString.length();
                    state = 2;
                    break;
                case 2:
                    // Get host
                    int portIndex = urlString.indexOf(':', protocolIndex+3);
                    int hostEndIndex = (portIndex != -1 && portIndex < pathStartIndex) ? portIndex : pathStartIndex;
                    host = urlString.substring(protocolIndex+3, hostEndIndex);
                    
                    state = (hostEndIndex == urlString.length()) ? 4 : 3;
                    break;
                case 3:
                    // Get path
                    int pathEndIndex = urlString.indexOf('?', pathStartIndex);
                    pathEndIndex = (pathEndIndex == -1) ? urlString.length() : pathEndIndex;
                    path = urlString.substring(pathStartIndex, pathEndIndex);
                    state = 4;
                    break;
                case 4:
                    result = new Url(protocol, host, path);
                    break;
                default:
                    throw new CookieException("Internal error during parsing URL: " + urlString);
            }
        }
        
        return result;
    }
    
    private static boolean isEmpty(String s) {
        return (s == null || s.length() == 0);
    }
    
    private static class AttributeValue {
        
        public String attribute;
        
        public String value;
        
        public int lastIndex;

        public AttributeValue(String attribute, int lastIndex) {
            this.attribute = attribute;
            this.lastIndex = lastIndex;
        }

        public AttributeValue(String attribute, String value, int lastIndex) {
            this.attribute = attribute;
            this.value = value;
            this.lastIndex = lastIndex;
        }
    }
    
    private static class Url {
        
        public String protocol;
        
        public String host;
        
        public String path;

        public Url(String protocol, String host, String path) {
            this.protocol = protocol;
            this.host = host;
            this.path = path;
        }
    }
    
    public static class CookieException extends Exception {

        public CookieException() {
            super();
        }

        public CookieException(String s) {
            super(s);
        }
    }
}