package com.espressif.iot.object.db;

import com.espressif.iot.object.IEspDBManager;

public interface IDownloadIdValueDBManager extends IEspDBManager
{
    /**
     * insert download id value if it isn't exist in local db
     * 
     * @param downloadIdValue the download id value to indicate whether the user1.bin or user2.bin is exist local
     */
    void insertDownloadIdValueIfNotExist(long downloadIdValue);

    /**
     * delete download id value if it is exist in local db
     * @param downloadIdValue the download id value to indicate whether the user1.bin or user2.bin is exist local
     */
    void deleteDownloadIdValueIfExist(long downloadIdValue);
    
    /**
     * 
     * @param downloadIdValue the download id value to indicate whether the user1.bin or user2.bin is exist local
     * @return whether the download id value is exist in local db
     */
    boolean isDownloadIdValueExist(long downloadIdValue);
}
