package cn.com.higinet.tms.engine.core.dao;

import java.sql.SQLException;

import cn.com.higinet.tms.engine.core.cache.db_device;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc_obj;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;

public class dao_device extends batch_stmt_jdbc_obj<db_device> {
    
    dao_device_insert insert;
    dao_device_update update;
    dao_device_delete delete;
    dao_device_read select;
    
    public dao_device(data_source ds)
    {
        super(null, null, null);
        insert = new dao_device_insert(ds);
        update = new dao_device_update(ds);
        delete = new dao_device_delete(ds);
        select = new dao_device_read(ds);
    }
    
    @Override
    public void update(String batch_code, db_device e) throws SQLException
    {
        e.resolve();
        if (e.is_indb()) {
            update.update(batch_code, e);
        } else {
            insert.update(batch_code, e);
        }
        e.set_indb(true);
    }
    
    @Override
    public void delete(db_device e) throws SQLException
    {
        delete.delete(e);
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
        return "dao_device";
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
    public Object[] toArray(db_device s)
    {
        return (s.is_indb()) ? update.toArray(s) : insert.toArray(s);
    }
    
    @Override
    public db_device read(Object... p) throws SQLException
    {
        return select.read(String.valueOf(p[0]));
    }
}