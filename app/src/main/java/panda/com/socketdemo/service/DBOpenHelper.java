package panda.com.socketdemo.service;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库SQLite的service,用于打开连接SQL操作
 *
 * Created by Administrator on 2015/9/21:9:30.
 */
public class DBOpenHelper extends SQLiteOpenHelper {

    private static final String DBNAME="download.db";
    private static final int VERSION=1;

    /**
     * 构造函数,设置数据库和上下文context
     *
     * @param context
     *           传入需要创建数据库的上下文context
     */
    public DBOpenHelper(Context context) {
        super(context, DBNAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建filedownloadlog数据表,存储断点下载记录
        db.execSQL("CREATE TABLE filedownloadlog (id integer primary key autoincrement,downloadpath varchar(100),threadid INTEGER,downlength INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 删除数据表
        db.execSQL("drop table if exists filedownloadlog");
        onCreate(db);
    }
}
