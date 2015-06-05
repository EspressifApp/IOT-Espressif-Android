package com.espressif.iot.object.db;

public interface IDownloadIdValueDB
{
     /**
      * get the id of the downloadIdValue
      * @return
      */
     Long getId();
    
     /**
      * set the id of the downloadIdValue
      * @param id the id of the downloadId value
      */
     void setId(Long id);
    
     /**
      * get the id value of the downloadIdValue
      * @return the id value of the downloadIdValue
      */
     long getIdValue();
    
     /**
      * set the id value of the downloadIdValue
      * @param idValue
      */
     void setIdValue(long idValue);
}
