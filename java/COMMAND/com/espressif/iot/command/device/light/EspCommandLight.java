package com.espressif.iot.command.device.light;

import com.espressif.iot.action.device.common.upgrade.EspDeviceUpgradeParser;
import com.espressif.iot.action.device.common.upgrade.IEspDeviceUpgradeInfo;
import com.espressif.iot.command.device.IEspCommandLight;
import com.espressif.iot.device.IEspDevice;

class EspCommandLight {
    private final int mProtocolVersionValue;

    public EspCommandLight() {
        IEspDeviceUpgradeInfo info =
            EspDeviceUpgradeParser.getInstance().parseUpgradeInfo(IEspCommandLight.PROTOCOL_NEW_VERSION);
        mProtocolVersionValue = info.getVersionValue();
    }

    public int getProtocolVersionValue() {
        return mProtocolVersionValue;
    }

    public int getDeviceVersionValue(IEspDevice device) {
        String version = device.getRom_version();
        if (version == null) {
            return 0;
        }

        IEspDeviceUpgradeInfo info = EspDeviceUpgradeParser.getInstance().parseUpgradeInfo(version);
        return info.getVersionValue();
    }
}
