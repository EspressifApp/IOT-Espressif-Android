package com.espressif.iot.group;

import java.util.List;

import com.espressif.iot.R;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.object.IEspObject;

public interface IEspGroup extends IEspObject {
    public static final long ID_NEW = -1;

    public static enum State {
        NORMAL, DELETED, RENAMED;
    }

    public static enum Type {
        COMMON(R.drawable.group_type_common_normal, R.drawable.group_type_common_selected),
        BEDROOM(R.drawable.group_type_bedroom_normal, R.drawable.group_type_bedroom_selected),
        LIVINGROOM(R.drawable.group_type_livingroom_normal, R.drawable.group_type_livingroom_selected),
        KITCHEN(R.drawable.group_type_kitchen_normal, R.drawable.group_type_kitchen_selected),
        SHOP(R.drawable.group_type_shop_normal, R.drawable.group_type_shop_selected),
        BAR(R.drawable.group_type_bar_normal, R.drawable.group_type_bar_selected),
        FACTORY(R.drawable.group_type_factory_normal, R.drawable.group_type_factory_selected),
        PARKING(R.drawable.group_type_parking_normal, R.drawable.group_type_parking_selected),
        WC(R.drawable.group_type_wc_normal, R.drawable.group_type_wc_selected);

        private int mIconResNoraml;
        private int mIconResSelected;

        private Type(int iconResNormal, int iconResSelected) {
            mIconResNoraml = iconResNormal;
            mIconResSelected = iconResSelected;
        }

        public int getIconResNormal() {
            return mIconResNoraml;
        }

        public int getIconResSelected() {
            return mIconResSelected;
        }
    }

    /**
     * 
     * @return the id of group
     */
    long getId();

    /**
     * Set group id
     * 
     * @param id
     */
    void setId(long id);

    /**
     * 
     * @return the name of group
     */
    String getName();

    /**
     * Set group name
     * 
     * @param name
     */
    void setName(String name);

    /**
     * Add device in the group
     * 
     * @param device
     */
    void addDevice(IEspDevice device);

    /**
     * Remove device from group
     * 
     * @param device
     */
    void removeDevice(IEspDevice device);

    /**
     * 
     * @return device list of the group
     */
    List<IEspDevice> getDeviceList();

    /**
     * Generate the list of devices' BSSID
     * 
     * @return BSSID list
     */
    List<String> generateDeviceBssidList();

    /**
     * Set state
     */
    void setState(int stateValue);

    /**
     * 
     * @return state value
     */
    int getStateValue();

    /**
     * Add state
     * 
     * @param state
     */
    void addState(State state);

    /**
     * clear state
     * 
     * @param state
     */
    void clearState(State state);

    /**
     * Clear all state
     */
    void clearAllState();

    /**
     * 
     * @return is DELETED state or not
     */
    boolean isStateDeleted();

    /**
     * 
     * @return is RENAMED state or not
     */
    boolean isStateRenamed();

    /**
     * Set group type
     * 
     * @param type
     */
    void setType(Type type);

    /**
     * Set group type
     * 
     * @param typeOrdinal
     */
    void setType(int typeOrdinal);

    /**
     * 
     * @return group type
     */
    Type getType();
}
