package com.evcs.app.config;

public class ApiConfig {
    
    // API Configuration
    public static final class Endpoints {

        public static final String LOCALHOST_BASE_URL = "http://10.0.2.2:59272/"; // Direct localhost
        public static final String EMULATOR_BASE_URL = "http://10.0.2.2:59272/"; // For Android emulator (maps to localhost)
        public static final String DEVICE_BASE_URL = "http://192.168.1.34:5000/"; // For real device (update IP)

        public static final String PRODUCTION_BASE_URL = "https://your-api-server.com/"; // For production
        
        // Use this for current configuration - change based on your setup
        // For emulator testing: use EMULATOR_BASE_URL
        // For real device: use DEVICE_BASE_URL 
        // Make sure the IP matches your computer's IP where backend is running
        public static final String CURRENT_BASE_URL = DEVICE_BASE_URL;
    }
    
    public static final class Timeouts {
        public static final int CONNECT_TIMEOUT_SECONDS = 30;
        public static final int READ_TIMEOUT_SECONDS = 30;
        public static final int WRITE_TIMEOUT_SECONDS = 30;
    }
    
    public static final class Headers {
        public static final String AUTHORIZATION = "Authorization";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String APPLICATION_JSON = "application/json";
        public static final String BEARER_PREFIX = "Bearer ";
        public static final String X_OWNER_ID = "X-OwnerId";
    }
    
    public static final class Validation {
        public static final int MIN_PASSWORD_LENGTH = 6;
        public static final int MIN_NAME_LENGTH = 2;
        public static final int MIN_NIC_LENGTH = 9;
        public static final String PHONE_REGEX = "^[+]?[0-9]{10,15}$";
    }
}