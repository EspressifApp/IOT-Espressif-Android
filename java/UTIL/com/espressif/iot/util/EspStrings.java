package com.espressif.iot.util;

public final class EspStrings
{
    public static final class DB
    {
        /**
         * the local database's db name
         */
        public static final String DB_NAME = "device-db";
    }
    
    public static final class Action
    {
        /**
         * when some devices are added into @see IEspDeviceCache. after receiving the broadcast, the IUser should do
         * something relying on @see IEspDeviceCacheHandler, and isStateMachine=true
         * 
         */
        public static final String DEVICES_ARRIVE_STATEMACHINE = "DEVICES_ARRIVE_STATEMACHINE";
        
        /**
         * when some devices are added into @see IEspDeviceCache. after receiving the broadcast, the IUser should do
         * something relying on @see IEspDeviceCacheHandler, and isStateMachine=false
         * 
         */
        public static final String DEVICES_ARRIVE_PULLREFRESH = "DEVICES_ARRIVE_PULLREFRESH";
        
        /**
         * when login successfully in Settings, send this broadcast to refresh devices from server
         */
        public static final String LOGIN_NEW_ACCOUNT = "LOGIN_NEW_ACCOUNT";
        
        /**
         * Send local broadcast when create a group on server successfully
         */
        public static final String CREATE_NEW_CLOUD_GROUP = "CREATE_NEW_CLOUD_GROUP";
        
        /**
         * Send local broadcast when adding device by esptouch make one device conntected to the AP
         */
        public static final String ESPTOUCH_DEVICE_FOUND = "ESPTOUCH_DEVICE_FOUND";
        
        /**
         * Send local broadcast when adding device by esptouch try to connect to server
         */
        public static final String ESPTOUCH_CONTACTING_SERVER = "ESPTOUCH_CONTACTING_SERVER";
        
        /**
         * Send local broadcast when adding device by esptouch connect to server suc
         */
        public static final String ESPTOUCH_REGISTERING_DEVICES = "ESPTOUCH_REGISTERING_DEVICES";
        
        /**
         * Send local broadcast when adding device by esptouch make one device registered suc
         */
        public static final String ESPTOUCH_DEVICE_REGISTERED = "ESPTOUCH_DEVICE_REGISTERED";
        
        /**
         * Send local broadcast when a local devices are found
         */
        public static final String UI_REFRESH_LOCAL_DEVICES = "UI_REFRESH_DEVICES";
    }
    
    public static final class Key
    {
        /**
         * The Intent value name of extra account email that RegisterActivity return to LoginActivity when register
         * success
         */
        public static final String REGISTER_NAME_EMAIL = "register_name_email";
        
        /**
         * The Intent value name of extra account password that RegisterActivity return to LoginActivity when register
         * success
         */
        public static final String REGISTER_NAME_PASSWORD = "register_name_password";
        
        /**
         * The Intent value name of extra account phone number that RegisterActivity return to LoginActivity when
         * register success
         */
        public static final String REGISTER_NAME_PHONE = "register_name_phone";

        /**
         * The settings SharedPreferences xml name
         */
        public static final String SETTINGS_NAME = "settings";
        
        /**
         * The system config SharedPreferences xml name
         */
        public static final String SYSTEM_CONFIG = "system_config";
        
        /**
         * The key of shared value whether the device support https, the default value is true
         */
        public static final String HTTPS_SUPPORT = "is_https_support";
        
        /**
         * The key of shared value auto refresh device
         */
        public static final String SETTINGS_KEY_DEVICE_AUTO_REFRESH = "settings_device_auto_refresh";
        
        public static final String SETTINGS_KEY_SHOW_MESH_TREE = "settings_show_mesh_tree";
        
        public static final String SETTINGS_KEY_STORE_LOG = "settings_store_log";
        
        /**
         * The key of shared value auto configure device
         */
        public static final String SETTINGS_KEY_DEVICE_AUTO_CONFIGURE = "settings_device_auto_configure";
        
        public static final String SETTINGS_KEY_ESPPUSH = "settings_esppush";
        
        /**
         * The Intent value name of extra Device Bssid
         */
        public static final String DEVICE_BSSID = "device_bssid";
        
        /**
         * The Intent value name of extra Device Name
         */
        public static final String DEVICE_NAME = "device_name";
        
        /**
         * The Intent value name of extra Device Key
         */
        public static final String DEVICE_KEY_KEY = "device_key";
        
        /**
         * The Intent value name of extra Device IOTAddress
         */
        public static final String DEVICE_KEY_IOTADDRESS = "device_iotaddress";
        
        /**
         * The Intent value name of extra Device type string
         */
        public static final String DEVICE_KEY_TYPE = "device_type";
        
        /**
         * The Intent value name of extra array of Device Key
         */
        public static final String DEVICE_KEY_KEY_ARRAY = "device_key_array";
        
        /**
         * The Intent value name of extra whether show mesh tree view
         */
        public static final String DEVICE_KEY_SHOW_CHILDREN = "show_children";
        
        /**
         * The Intent value name of extra Device Timer ID
         */
        public static final String DEVICE_TIMER_ID_KEY = "device_timer_id";
        
        public static final String DEVICE_TIMER_BUNDLE_KEY = "device_timer_bundle";
        
        public static final String DEVICE_TIMER_PLUGS_VALUE_KEY = "plugs_value";
        
        public static final String KEY_AUTO_LOGIN = "auto_login";
        
        /**
         * The group id before created
         */
        public static final String KEY_GROUP_ID_OLD = "old_group_id";
        
        /**
         * The group id after created
         */
        public static final String KEY_GROUP_ID_NEW = "new_group_id";
        
        /**
         * The SharedPreferences name of new devices
         */
        public static final String NAME_NEW_ACTIVATED_DEVICES = "new_activated_devices";
        
        public static final String KEY_ESPBUTTON_TEMP_KEY = "espbutton_temp_key";
        
        public static final String KEY_ESPBUTTON_MAC = "espbutton_mac";
        
        public static final String KEY_ESPBUTTON_DEVICE_KEYS = "espbutton_device_keys";
    }
}
