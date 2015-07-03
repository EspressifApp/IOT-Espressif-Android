package com.espressif.iot.type.device.state;

import com.espressif.iot.type.device.IEspDeviceState;

public class EspDeviceState implements IEspDeviceState, Cloneable
{
    
    /**
     * although these variables are final, but their mState could be changed. but please don't change them. or the bugs
     * will be produced
     */
    public static final IEspDeviceState NEW = new EspDeviceState(1 << IEspDeviceState.Enum.NEW.ordinal());
    
    public static final IEspDeviceState LOCAL = new EspDeviceState(1 << IEspDeviceState.Enum.LOCAL.ordinal());
    
    public static final IEspDeviceState INTERNET = new EspDeviceState(1 << IEspDeviceState.Enum.INTERNET.ordinal());
    
    public static final IEspDeviceState OFFLINE = new EspDeviceState(1 << IEspDeviceState.Enum.OFFLINE.ordinal());
    
    public static final IEspDeviceState CONFIGURING = new EspDeviceState(
        1 << IEspDeviceState.Enum.CONFIGURING.ordinal());
    
    public static final IEspDeviceState UPGRADING_LOCAL = new EspDeviceState(
        1 << IEspDeviceState.Enum.UPGRADING_LOCAL.ordinal());
    
    public static final IEspDeviceState UPGRADING_INTERNET = new EspDeviceState(
        1 << IEspDeviceState.Enum.UPGRADING_INTERNET.ordinal());
    
    public static final IEspDeviceState ACTIVATING = new EspDeviceState(1 << IEspDeviceState.Enum.ACTIVATING.ordinal());
    
    public static final IEspDeviceState DELETED = new EspDeviceState(1 << IEspDeviceState.Enum.DELETED.ordinal());
    
    public static final IEspDeviceState RENAMED = new EspDeviceState(1 << IEspDeviceState.Enum.RENAMED.ordinal());
    
    public static final IEspDeviceState CLEAR = new EspDeviceState(0);
    
    
    // check whether the state which should be final is final
    private void __checkvalid()
    {
        if (this == NEW)
        {
            throw new IllegalArgumentException(
                "EspDeviceState.NEW can't be changed or the statemachine will make supprise");
        }
        else if (this == LOCAL)
        {
            throw new IllegalArgumentException(
                "EspDeviceState.LOCAL can't be changed or the statemachine will make supprise");
        }
        else if (this == INTERNET)
        {
            throw new IllegalArgumentException(
                "EspDeviceState.INTERNET can't be changed or the statemachine will make supprise");
        }
        else if (this == OFFLINE)
        {
            throw new IllegalArgumentException(
                "EspDeviceState.OFFLINE can't be changed or the statemachine will make supprise");
        }
        else if (this == CONFIGURING)
        {
            throw new IllegalArgumentException(
                "EspDeviceState.CONFIGURING can't be changed or the statemachine will make supprise");
        }
        else if (this == UPGRADING_LOCAL)
        {
            throw new IllegalArgumentException(
                "EspDeviceState.UPGRADING_LOCAL can't be changed or the statemachine will make supprise");
        }
        else if (this == UPGRADING_INTERNET)
        {
            throw new IllegalArgumentException(
                "EspDeviceState.UPGRADING_INTERNET can't be changed or the statemachine will make supprise");
        }
        else if (this == ACTIVATING)
        {
            throw new IllegalArgumentException(
                "EspDeviceState.ACTIVATING can't be changed or the statemachine will make supprise");
        }
        else if (this == DELETED)
        {
            throw new IllegalArgumentException(
                "EspDeviceState.DELETED can't be changed or the statemachine will make supprise");
        }
        else if (this == RENAMED)
        {
            throw new IllegalArgumentException(
                "EspDeviceState.RENAMED can't be changed or the statemachine will make supprise");
        }
        else if (this == CLEAR)
        {
            throw new IllegalArgumentException(
                "EspDeviceState.CLEAR can't be changed or the statemachine will make supprise");
        }
    }
    
    /**
     * 
     * @param state
     * @param permittedStates permitted States, only pure state of {@link #NEW},{@link #LOCAL} ,{@link #INTERNET},
     *            {@link #OFFLINE},{@link #CONFIGURING},{@link #UPGRADING_LOCAL},{@link #UPGRADING_INTERNET},
     *            {@link #ACTIVATING},{@link #DELETED},{@link #RENAMED},{@link #CLEAR}
     * @return whether the state is valid
     */
    public static boolean checkValidWithPermittedStates(IEspDeviceState state, IEspDeviceState... permittedStates)
    {
        int len = permittedStates.length;
        if (len == 0)
        {
            throw new NullPointerException("checkValidWithPermittedStates() permittedStates shouldn't be length of 0");
        }
        EspDeviceState deviceState = (EspDeviceState)state;
        IEspDeviceState.Enum[] stateEnums = IEspDeviceState.Enum.values();
        boolean isPermitted = false;
        // for each all of states
        for (int i = 0; i < stateEnums.length; i++)
        {
            IEspDeviceState.Enum stateEnum = stateEnums[i];
            // check whether the state is permitted
            if (deviceState.isStateXXX(stateEnum))
            {
                isPermitted = false;
                for (int j = 0; j < len; j++)
                {
                    EspDeviceState permittedState = (EspDeviceState)permittedStates[j];
                    if (permittedState.isStateXXX(stateEnum))
                    {
                        isPermitted = true;
                        break;
                    }
                }
                // the state isn't permitted
                if (!isPermitted)
                {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * 
     * @param state
     * @param forbiddenStates forbidden States, only pure state of {@link #NEW},{@link #LOCAL} ,{@link #INTERNET},
     *            {@link #OFFLINE},{@link #CONFIGURING},{@link #UPGRADING_LOCAL},{@link #UPGRADING_INTERNET},
     *            {@link #ACTIVATING},{@link #DELETED},{@link #RENAMED},{@link #CLEAR}
     * @return whether the state is valid
     */
    public static boolean checkValidWithForbiddenStates(IEspDeviceState state, IEspDeviceState... forbiddenStates)
    {
        int len = forbiddenStates.length;
        if (len == 0)
        {
            throw new NullPointerException("checkValidWithForbiddenStates() forbiddenStates shouldn't be length of 0");
        }
        EspDeviceState deviceState = (EspDeviceState)state;
        // for each forbidden states
        for (int i = 0; i < len; i++)
        {
            EspDeviceState forbiddenState = (EspDeviceState)forbiddenStates[i];
            // state is forbidden
            if (deviceState.isStateXXX(forbiddenState.getDeviceState()))
            {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 
     * @param state
     * @param necessaryStates necessary States, only pure state of {@link #NEW},{@link #LOCAL} ,{@link #INTERNET},
     *            {@link #OFFLINE},{@link #CONFIGURING},{@link #UPGRADING_LOCAL},{@link #UPGRADING_INTERNET},
     *            {@link #ACTIVATING},{@link #DELETED},{@link #RENAMED},{@link #CLEAR}
     * @return whether the state is valid
     */
    public static boolean checkValidWithNecessaryStates(IEspDeviceState state, IEspDeviceState... necessaryStates)
    {
        int len = necessaryStates.length;
        if (len == 0)
        {
            throw new NullPointerException("checkValidWithNecessaryStates() necessaryStates shouldn't be length of 0");
        }
        boolean isContained = false;
        EspDeviceState deviceState = (EspDeviceState)state;
        // for each necessary states
        for (int i = 0; i < len; i++)
        {
            EspDeviceState necessaryState = (EspDeviceState)necessaryStates[i];
            // check whether the state is contained
            isContained = deviceState.isStateXXX(necessaryState.getDeviceState());
            // the state isn't permitted
            if (!isContained)
            {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 
     * @param state
     * @param specificStates specific States, only pure state of {@link #NEW},{@link #LOCAL} ,{@link #INTERNET},
     *            {@link #OFFLINE},{@link #CONFIGURING},{@link #UPGRADING_LOCAL},{@link #UPGRADING_INTERNET},
     *            {@link #ACTIVATING},{@link #DELETED},{@link #RENAMED},{@link #CLEAR}
     * @return whether the state is valid
     */
    public static boolean checkValidWithSpecificStates(IEspDeviceState state, IEspDeviceState... specificStates)
    {
        return checkValidWithNecessaryStates(state, specificStates)
            && checkValidWithPermittedStates(state, specificStates);
    }
    
    private int mState = 0;
    
    public EspDeviceState()
    {
        
    }
    
    public EspDeviceState(int state)
    {
        this.mState = state;
    }
    
    private void addStateXXX(IEspDeviceState.Enum stateEnum)
    {
        __checkvalid();
        this.mState |= (1 << stateEnum.ordinal());
    }
    
    private void clearStateXXX(IEspDeviceState.Enum stateEnum)
    {
        __checkvalid();
        this.mState &= (~(1 << stateEnum.ordinal()));
    }
    
    private boolean isStateXXX(IEspDeviceState.Enum stateEnum)
    {
        return (this.mState & (1 << stateEnum.ordinal())) != 0;
    }
    
    @Override
    public int getStateValue()
    {
        return mState;
    }
    
    @Override
    public void setStateValue(int state)
    {
        __checkvalid();
        this.mState = state;
    }
    
    @Override
    public void addStateNew()
    {
        addStateXXX(IEspDeviceState.Enum.NEW);
    }
    
    @Override
    public void clearStateNew()
    {
        clearStateXXX(IEspDeviceState.Enum.NEW);
    }
    
    @Override
    public boolean isStateNew()
    {
        return isStateXXX(IEspDeviceState.Enum.NEW);
    }
    
    @Override
    public void addStateLocal()
    {
        addStateXXX(IEspDeviceState.Enum.LOCAL);
    }
    
    @Override
    public void clearStateLocal()
    {
        clearStateXXX(IEspDeviceState.Enum.LOCAL);
    }
    
    @Override
    public boolean isStateLocal()
    {
        return isStateXXX(IEspDeviceState.Enum.LOCAL);
    }
    
    @Override
    public void addStateInternet()
    {
        addStateXXX(IEspDeviceState.Enum.INTERNET);
    }
    
    @Override
    public void clearStateInternet()
    {
        clearStateXXX(IEspDeviceState.Enum.INTERNET);
    }
    
    @Override
    public boolean isStateInternet()
    {
        return isStateXXX(IEspDeviceState.Enum.INTERNET);
    }
    
    @Override
    public void addStateOffline()
    {
        addStateXXX(IEspDeviceState.Enum.OFFLINE);
    }
    
    @Override
    public void clearStateOffline()
    {
        clearStateXXX(IEspDeviceState.Enum.OFFLINE);
    }
    
    @Override
    public boolean isStateOffline()
    {
        return isStateXXX(IEspDeviceState.Enum.OFFLINE);
    }
    
    @Override
    public void addStateConfiguring()
    {
        addStateXXX(IEspDeviceState.Enum.CONFIGURING);
    }
    
    @Override
    public void clearStateConfiguring()
    {
        clearStateXXX(IEspDeviceState.Enum.CONFIGURING);
    }
    
    @Override
    public boolean isStateConfiguring()
    {
        return isStateXXX(IEspDeviceState.Enum.CONFIGURING);
    }
    
    @Override
    public void addStateActivating()
    {
        addStateXXX(IEspDeviceState.Enum.ACTIVATING);
    }
    
    @Override
    public void clearStateActivating()
    {
        clearStateXXX(IEspDeviceState.Enum.ACTIVATING);
    }
    
    @Override
    public boolean isStateActivating()
    {
        return isStateXXX(IEspDeviceState.Enum.ACTIVATING);
    }
    
    @Override
    public void addStateUpgradingLocal()
    {
        addStateXXX(IEspDeviceState.Enum.UPGRADING_LOCAL);
    }
    
    @Override
    public void clearStateUpgradingLocal()
    {
        clearStateXXX(IEspDeviceState.Enum.UPGRADING_LOCAL);
    }
    
    @Override
    public boolean isStateUpgradingLocal()
    {
        return isStateXXX(IEspDeviceState.Enum.UPGRADING_LOCAL);
    }
    
    @Override
    public void addStateUpgradingInternet()
    {
        addStateXXX(IEspDeviceState.Enum.UPGRADING_INTERNET);
    }
    
    @Override
    public void clearStateUpgradingInternet()
    {
        clearStateXXX(IEspDeviceState.Enum.UPGRADING_INTERNET);
    }
    
    @Override
    public boolean isStateUpgradingInternet()
    {
        return isStateXXX(IEspDeviceState.Enum.UPGRADING_INTERNET);
    }
    
    @Override
    public void addStateDeleted()
    {
        addStateXXX(IEspDeviceState.Enum.DELETED);
    }
    
    @Override
    public void clearStateDeleted()
    {
        clearStateXXX(IEspDeviceState.Enum.DELETED);
    }
    
    @Override
    public boolean isStateDeleted()
    {
        return isStateXXX(IEspDeviceState.Enum.DELETED);
    }
    
    @Override
    public void addStateRenamed()
    {
        addStateXXX(IEspDeviceState.Enum.RENAMED);
        
    }
    
    @Override
    public void clearStateRenamed()
    {
        clearStateXXX(IEspDeviceState.Enum.RENAMED);
    }
    
    @Override
    public boolean isStateRenamed()
    {
        return isStateXXX(IEspDeviceState.Enum.RENAMED);
    }
    
    @Override
    public void clearState()
    {
        this.mState = 0;
    }
    
    @Override
    public boolean isStateClear()
    {
        return this.mState == 0;
    }
    
    @Override
    public IEspDeviceState.Enum getDeviceState()
    {
        if (this.isStateUpgradingLocal())
        {
            return IEspDeviceState.Enum.UPGRADING_LOCAL;
        }
        else if (this.isStateUpgradingInternet())
        {
            return IEspDeviceState.Enum.UPGRADING_INTERNET;
        }
        else if (this.isStateOffline())
        {
            return IEspDeviceState.Enum.OFFLINE;
        }
        else if (this.isStateNew())
        {
            return IEspDeviceState.Enum.NEW;
        }
        // LOCAL must be front of INTERNET
        // for the UI display priority to Local
        else if (this.isStateLocal())
        {
            return IEspDeviceState.Enum.LOCAL;
        }
        else if (this.isStateInternet())
        {
            return IEspDeviceState.Enum.INTERNET;
        }
        else if (this.isStateDeleted())
        {
            return IEspDeviceState.Enum.DELETED;
        }
        else if (this.isStateConfiguring())
        {
            return IEspDeviceState.Enum.CONFIGURING;
        }
        else if (this.isStateActivating())
        {
            return IEspDeviceState.Enum.ACTIVATING;
        }
        // RENAMED and CLEAR should be in the end
        else if (this.isStateRenamed())
        {
            return IEspDeviceState.Enum.RENAMED;
        }
        else if (this.isStateClear())
        {
            return IEspDeviceState.Enum.CLEAR;
        }
        return null;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof EspDeviceState) || o == null)
        {
            return false;
        }
        EspDeviceState other = (EspDeviceState)o;
        return this.mState == other.mState;
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        if (this.isStateUpgradingLocal())
        {
            sb.append(IEspDeviceState.Enum.UPGRADING_LOCAL + ",");
        }
        if (this.isStateUpgradingInternet())
        {
            sb.append(IEspDeviceState.Enum.UPGRADING_INTERNET + ",");
        }
        if (this.isStateOffline())
        {
            sb.append(IEspDeviceState.Enum.OFFLINE + ",");
        }
        if (this.isStateRenamed())
        {
            sb.append(IEspDeviceState.Enum.RENAMED + ",");
        }
        if (this.isStateNew())
        {
            sb.append(IEspDeviceState.Enum.NEW + ",");
        }
        if (this.isStateLocal())
        {
            sb.append(IEspDeviceState.Enum.LOCAL + ",");
        }
        if (this.isStateInternet())
        {
            sb.append(IEspDeviceState.Enum.INTERNET + ",");
        }
        if (this.isStateDeleted())
        {
            sb.append(IEspDeviceState.Enum.DELETED + ",");
        }
        if (this.isStateConfiguring())
        {
            sb.append(IEspDeviceState.Enum.CONFIGURING + ",");
        }
        if (this.isStateClear())
        {
            sb.append(IEspDeviceState.Enum.CLEAR + ",");
        }
        if (this.isStateActivating())
        {
            sb.append(IEspDeviceState.Enum.ACTIVATING + ",");
        }
        return "EspStateDeviceState=[" + sb.substring(0, sb.length() - 1) + "]";
    }
    
    @Override
    public Object clone()
        throws CloneNotSupportedException
    {
        return super.clone();
    }
    
}
