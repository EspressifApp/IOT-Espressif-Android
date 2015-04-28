package com.espressif.iot.db;

import org.apache.log4j.Logger;

import com.espressif.iot.db.greenrobot.daos.DaoSession;
import com.espressif.iot.db.greenrobot.daos.DownloadIdValueDB;
import com.espressif.iot.db.greenrobot.daos.DownloadIdValueDBDao;
import com.espressif.iot.object.IEspSingletonObject;
import com.espressif.iot.object.db.IDownloadIdValueDBManager;

import de.greenrobot.dao.query.Query;

public class IOTDownloadIdValueDBManager implements IDownloadIdValueDBManager, IEspSingletonObject
{
    private static final Logger log = Logger.getLogger(IOTDownloadIdValueDBManager.class);
    
    private DownloadIdValueDBDao downloadIdValueDao;
    
    // Singleton Pattern
    private static IOTDownloadIdValueDBManager instance = null;
    
    private IOTDownloadIdValueDBManager(DaoSession daoSession)
    {
        downloadIdValueDao = daoSession.getDownloadIdValueDBDao();
    }
    
    public static void init(DaoSession daoSession)
    {
        instance = new IOTDownloadIdValueDBManager(daoSession);
    }
    
    public static IOTDownloadIdValueDBManager getInstance()
    {
        return instance;
    }
    
    @Override
    public void insertDownloadIdValueIfNotExist(long downloadIdValue)
    {
        log.debug(Thread.currentThread().toString() + "##insertDownloadIdValueIfNotExist(downloadIdValue=["
            + downloadIdValue + "])");
        DownloadIdValueDB downloadIdValueDB = getDownloadIdValueDB(downloadIdValue);
        if (downloadIdValueDB == null)
        {
            downloadIdValueDB = new DownloadIdValueDB(null, downloadIdValue);
            downloadIdValueDao.insert(downloadIdValueDB);
        }
    }
    
    @Override
    public void deleteDownloadIdValueIfExist(long downloadIdValue)
    {
        log.debug(Thread.currentThread().toString() + "##deleteDownloadIdValueIfExist(downloadIdValue=["
            + downloadIdValue + "])");
        DownloadIdValueDB downloadIdValueDB = getDownloadIdValueDB(downloadIdValue);
        if (downloadIdValueDB != null)
        {
            downloadIdValueDao.deleteInTx(downloadIdValueDB);
        }
    }
    
    @Override
    public boolean isDownloadIdValueExist(long downloadIdValue)
    {
        boolean result = getDownloadIdValueDB(downloadIdValue) != null;
        log.debug(Thread.currentThread().toString() + "##isDownloadIdValueExist(downloadIdValue=[" + downloadIdValue
            + "]): " + result);
        return result;
    }
    
    private DownloadIdValueDB getDownloadIdValueDB(long downloadIdValue)
    {
        Query<DownloadIdValueDB> query =
            downloadIdValueDao.queryBuilder()
                .where(DownloadIdValueDBDao.Properties.IdValue.eq(downloadIdValue))
                .build();
        DownloadIdValueDB downloadIdValueDB = query.unique();
        log.debug(Thread.currentThread().toString() + "##getDownloadIdValueDB(downloadIdValue=[" + downloadIdValue
            + "]): " + downloadIdValueDB);
        return downloadIdValueDB;
    }
}
