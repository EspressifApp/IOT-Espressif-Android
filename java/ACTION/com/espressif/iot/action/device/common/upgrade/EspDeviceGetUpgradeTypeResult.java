package com.espressif.iot.action.device.common.upgrade;

import com.espressif.iot.type.upgrade.EspUpgradeDeviceType;
import com.espressif.iot.type.upgrade.EspUpgradeDeviceTypeResult;

public class EspDeviceGetUpgradeTypeResult implements IEspDeviceGetUpgradeTypeResult
{
    @Override
    public EspUpgradeDeviceTypeResult getDeviceUpgradeTypeResult(String romVersion, String latestRomVersion)
    {
        IEspDeviceUpgradeParser parser = EspDeviceUpgradeParser.getInstance();
        IEspDeviceUpgradeInfo currentUpgradeInfo = parser.parseUpgradeInfo(romVersion);
        if (currentUpgradeInfo == null)
        {
            return EspUpgradeDeviceTypeResult.CURRENT_ROM_INVALID;
        }
        IEspDeviceUpgradeInfo latestUpgradeInfo = parser.parseUpgradeInfo(latestRomVersion);
        if (latestUpgradeInfo == null)
        {
            return EspUpgradeDeviceTypeResult.LATEST_ROM_INVALID;
        }
        if (latestUpgradeInfo.getVersionValue() <= currentUpgradeInfo.getVersionValue())
        {
            return EspUpgradeDeviceTypeResult.CURRENT_ROM_IS_NEWEST;
        }
        // only released rom is available to user
        boolean isUsedByDeveloper = IS_USED_BY_DEVELOPER;
        if (!isUsedByDeveloper && !latestUpgradeInfo.getIsReleased())
        {
            return EspUpgradeDeviceTypeResult.CURRENT_ROM_IS_NEWEST;
        }
        EspUpgradeDeviceType upgradeType = currentUpgradeInfo.getUpgradeDeviceTypeEnum();
        switch (upgradeType)
        {
            case NOT_SUPPORT_UPGRADE:
                return EspUpgradeDeviceTypeResult.NOT_SUPPORT_UPGRADE;
            case SUPPORT_LOCAL_ONLY:
                return EspUpgradeDeviceTypeResult.SUPPORT_LOCAL_ONLY;
            case SUPPORT_ONLINE_LOCAL:
                return EspUpgradeDeviceTypeResult.SUPPORT_ONLINE_LOCAL;
            case SUPPORT_ONLINE_ONLY:
                return EspUpgradeDeviceTypeResult.SUPPORT_ONLINE_ONLY;
            default:
                break;
        
        }
        throw new IllegalStateException();
    }
}
