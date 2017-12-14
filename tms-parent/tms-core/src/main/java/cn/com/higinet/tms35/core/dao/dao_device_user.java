package cn.com.higinet.tms35.core.dao;

import java.sql.SQLException;

import cn.com.higinet.tms35.core.cache.db_device_user;
import cn.com.higinet.tms35.core.cache.linear;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc_obj;
import cn.com.higinet.tms35.core.dao.stmt.data_source;

public class dao_device_user extends batch_stmt_jdbc_obj<db_device_user> {
    dao_device_user_insert insert;
    dao_device_user_update update;
    dao_device_user_delete delete;
    dao_device_user_read select;
    
    public dao_device_user(data_source ds)
    {
        super(null, null, null);
        insert = new dao_device_user_insert(ds);
        update = new dao_device_user_update(ds);
        delete = new dao_device_user_delete(ds);
        select = new dao_device_user_read(ds);
    }
    
    @Override
    public void update(String batch_code, db_device_user e) throws SQLException
    {
        
        if (e.is_indb()) {
            update.update(batch_code, e);
        } else {
            insert.update(batch_code, e);
        }
        e.set_indb(true);
    }
    
    public void delete(String batch_code, db_device_user e) throws SQLException
    {
        delete.delete(batch_code, e);
    }

    @Override
    public void close()
    {
        insert.close();
        update.close();
        delete.close();
        select.close();
    }

    @Override
    public String name()
    {
        return "dao_device_user";
    }
    
    @Override
    public void reset_update_pos()
    {
        insert.reset_update_pos();
        update.reset_update_pos();
        delete.reset_update_pos();
    }

    @Override
    public void flush() throws SQLException
    {
        delete.flush();
        update.flush();
        insert.flush();
    }

    @Override
    public Object[] toArray(db_device_user s)
    {
        return (s.is_indb()) ? update.toArray(s) : insert.toArray(s);
    }
    
    @Override
    public db_device_user read(Object... p) throws SQLException
    {
        return select.read((Long)p[0], (String)p[1]);
    }
    
    @Override
    public linear<db_device_user> read_list(Object... p) throws SQLException
    {
        return select.read_list((Long)p[0]);
    }
    
    public linear<db_device_user> readListByUser(String userId) throws SQLException{
    	return select.readListByUser(userId);
    }
}