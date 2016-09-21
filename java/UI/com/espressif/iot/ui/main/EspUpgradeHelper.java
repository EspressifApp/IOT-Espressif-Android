package com.espressif.iot.ui.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.IEspDeviceState;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;

public enum EspUpgradeHelper {
    INSTANCE;

    private static final Logger sLog = Logger.getLogger(EspUpgradeHelper.class);

    private IEspUser mUser;
    private List<UpgradeDevice> mDevices;

    public static class UpgradeDevice {
        public static final int UPGRADE_TYPE_AUTO = 0;
        public static final int UPGRADE_TYPE_LOCAL = 1;
        public static final int UPGRADE_TYPE_INTERNET = 2;

        private IEspDevice mDevice;
        private int mUpgradeType;

        public UpgradeDevice(IEspDevice device) {
            this(device, UPGRADE_TYPE_AUTO);
        }

        public UpgradeDevice(IEspDevice device, int upgradeType) {
            mDevice = device;
            mUpgradeType = upgradeType;
        }

        public IEspDevice getDevice() {
            return mDevice;
        }

        public int getUpgradeType() {
            return mUpgradeType;
        }
    }

    private EspUpgradeHelper() {
        mUser = BEspUser.getBuilder().getInstance();
        mDevices = new ArrayList<UpgradeDevice>();
    }

    /**
     * Add devices to upgrade
     * 
     * @param devices
     */
    public void addDevices(Collection<IEspDevice> devices) {
        addDevices(devices, UpgradeDevice.UPGRADE_TYPE_AUTO);
    }

    /**
     * Add devices to upgrade
     * 
     * @param devices
     * @param upgradeType One of {@link #UPGRADE_TYPE_AUTO}, {@link #UPGRADE_TYPE_LOCAL}, or
     *            {@link #UPGRADE_TYPE_INTERNET}.
     */
    public void addDevices(Collection<IEspDevice> devices, int upgradeType) {
        for (IEspDevice device : devices) {
            addDevice(device,upgradeType);
        }
    }

    /**
     * Add device to upgrade
     * 
     * @param device
     */
    public void addDevice(IEspDevice device) {
        addDevice(device, UpgradeDevice.UPGRADE_TYPE_AUTO);
    }

    /**
     * Add device to upgrade
     * 
     * @param device
     * @param upgradeType One of {@link #UPGRADE_TYPE_AUTO}, {@link #UPGRADE_TYPE_LOCAL}, or
     *            {@link #UPGRADE_TYPE_INTERNET}.
     */
    public void addDevice(IEspDevice device, int upgradeType) {
        if (!contain(device)) {
            mDevices.add(new UpgradeDevice(device, upgradeType));
        }
    }

    private boolean contain(IEspDevice device) {
        boolean result = false;
        for (UpgradeDevice ud : mDevices) {
            if (device.equals(ud.getDevice())) {
                result = true;
                break;
            }
        }

        return result;
    }

    public List<IEspDevice> getUpgradingDevices() {
        List<IEspDevice> result = new ArrayList<IEspDevice>();
        for (UpgradeDevice ud : mDevices) {
            result.add(ud.getDevice());
        }

        return result;
    }

    public void checkUpgradingDevices() {
        sLog.debug("checkUpgradingDevices()   size = " + mDevices.size());
        List<IEspDevice> userDevices = mUser.getAllDeviceList();
        for (IEspDevice device : userDevices) {
            IEspDeviceState state = device.getDeviceState();
            if (state.isStateUpgradingInternet() || state.isStateUpgradingLocal()) {
                return;
            }
        }

        UpgradeDevice updradeDevice = null;
        int childrenCount = 0;
        for (UpgradeDevice ud : mDevices) {
            IEspDevice device = ud.getDevice();
            if (updradeDevice == null) {
                updradeDevice = ud;
                childrenCount = device.getDeviceTreeElementList(userDevices).size();
            }

            if (updradeDevice != device) {
                int count = device.getDeviceTreeElementList(userDevices).size();
                if (count < childrenCount) {
                    childrenCount = count;
                    updradeDevice = ud;
                }
            }
        }
        if (updradeDevice != null) {
            sLog.info("upgrade device bssid " + updradeDevice.getDevice().getBssid() + " | upgradeType = "
                + updradeDevice.getUpgradeType());
            mDevices.remove(updradeDevice);
            switch (updradeDevice.getUpgradeType()) {
                case UpgradeDevice.UPGRADE_TYPE_AUTO:
                    if (updradeDevice.getDevice().getDeviceState().isStateLocal()) {
                        mUser.doActionUpgradeLocal(updradeDevice.getDevice());
                    } else {
                        mUser.doActionUpgradeInternet(updradeDevice.getDevice());
                    }
                    break;
                case UpgradeDevice.UPGRADE_TYPE_LOCAL:
                    mUser.doActionUpgradeLocal(updradeDevice.getDevice());
                    break;
                case UpgradeDevice.UPGRADE_TYPE_INTERNET:
                    mUser.doActionUpgradeInternet(updradeDevice.getDevice());
                    break;
                default:
                    sLog.warn("Unknow upgrade way");
                    break;
            }
        }
    }

    public void clear() {
        mDevices.clear();
    }
}
