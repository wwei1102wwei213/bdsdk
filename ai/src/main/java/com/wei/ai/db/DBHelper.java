package com.wei.ai.db;

import android.text.TextUtils;
import android.util.Log;

import com.ta.TAApplication;
import com.ta.util.db.TASQLiteDatabase;

import java.util.List;

/**
 * Created by Administrator on 2016/3/30.
 * <p>
 * 数据表单帮助类
 *
 * @author wwei
 */
public class DBHelper {

    private DBHelper() {
    }

    private static DBHelper instance = null;

    /***
     * 单例模式，注意要加同步锁
     *
     * @return DBHelper
     */
    public static DBHelper getInstance() {
        if (instance == null) {
            synchronized (DBHelper.class) {
                if (instance == null) {
                    instance = new DBHelper();
                }
            }
        }
        return instance;
    }

    /**
     * 创建表
     * @param mClass 表标识
     */
    public void createTable(TAApplication application, Class<?> mClass){
        try {
            TASQLiteDatabase tasqLiteDatabase = application.getSQLiteDatabasePool().getSQLiteDatabase();
            if (!tasqLiteDatabase.hasTable(mClass)) {
                tasqLiteDatabase.creatTable(mClass);
                Log.e("DBHelper", mClass.getSimpleName()+"表已创建");
            }
            application.getSQLiteDatabasePool().releaseSQLiteDatabase(tasqLiteDatabase);
        } catch (Exception e){
            Log.e("DBHelper", e.getMessage());
        }
    }

    /**
     * 删除表
     * @param mClass 表标识
     */
    public void dropTable(TAApplication application, Class<?> mClass){
        try {
            TASQLiteDatabase tasqLiteDatabase = application.getSQLiteDatabasePool().getSQLiteDatabase();
            if (!tasqLiteDatabase.hasTable(mClass)) {
                tasqLiteDatabase.dropTable(mClass);
                Log.e("DBHelper", mClass.getSimpleName()+"表已删除");
            }
            application.getSQLiteDatabasePool().releaseSQLiteDatabase(tasqLiteDatabase);
        } catch (Exception e){
            Log.e("DBHelper", e.getMessage());
        }
    }

    /**
     * 插入数据
     * @param object 数据
     * @param mClass 表标识
     */
    public void insertObject(TAApplication application, Object object, Class<?> mClass) {
        try {
            TASQLiteDatabase tasqLiteDatabase = application.getSQLiteDatabasePool().getSQLiteDatabase();
            if (!tasqLiteDatabase.hasTable(mClass)) {
                tasqLiteDatabase.creatTable(mClass);
                Log.e("DBHelper", mClass.getSimpleName()+"表已创建");
            }
            tasqLiteDatabase.insert(object);
            application.getSQLiteDatabasePool().releaseSQLiteDatabase(tasqLiteDatabase);
        } catch (Exception e){
            Log.e("DBHelper", e.getMessage());
        }
    }

    /**
     * 查询数据
     * @param mClass  表标识
     * @param where   查询条件
     * @param orderBy 排序
     * @param limit   分页
     * @param <T>     返回类型
     * @return
     */
    public <T> List<T> queryObject (TAApplication application, Class<?> mClass, String where, String orderBy, String limit){
        try {
            TASQLiteDatabase tasqLiteDatabase = application.getSQLiteDatabasePool().getSQLiteDatabase();
            if (!tasqLiteDatabase.hasTable(mClass)) {
                return null;
            }
            List<T> list = tasqLiteDatabase.query(mClass, false, where, null, null, orderBy, limit);
            application.getSQLiteDatabasePool().releaseSQLiteDatabase(tasqLiteDatabase);
            return list;
        } catch (Exception e){
            Log.e("DBHelper", e.getMessage());
        }
        return null;
    }

    public InfoBean queryInfoData(TAApplication application, String cardNum) {
        try {
            TASQLiteDatabase tasqLiteDatabase = application.getSQLiteDatabasePool().getSQLiteDatabase();
            if (!tasqLiteDatabase.hasTable(InfoBean.class)) {
                return null;
            }
            List<InfoBean> list = tasqLiteDatabase.query(InfoBean.class, false, "card="+cardNum, null, null, null, null);
            InfoBean result = null;
            if (list!=null&&list.size()>0) {
                result = list.get(0);
            }
            application.getSQLiteDatabasePool().releaseSQLiteDatabase(tasqLiteDatabase);
            return result;
        } catch (Exception e){
            Log.e("DBHelper", e.getMessage());
        }
        return null;
    }

    public List<CheckDataBean> queryCheckObject (TAApplication application, long dayTime, String name, String card, int page) {
        try {
            TASQLiteDatabase tasqLiteDatabase = application.getSQLiteDatabasePool().getSQLiteDatabase();
            if (!tasqLiteDatabase.hasTable(CheckDataBean.class)) {
                return null;
            }
            String where = "";
            if (dayTime!=0) {
                where += " AND data_zero="+dayTime;
            }
            if (!TextUtils.isEmpty(name)) {
                where += " AND name=" + name;
            }
            if (!TextUtils.isEmpty(card)) {
                where += " AND card_number=" + card;
            }
            if (where.startsWith(" AND ")) {
                where = where.substring(5, where.length());
            }
            List<CheckDataBean> list = tasqLiteDatabase.query(CheckDataBean.class, false, where,
                    null, null, "create_time desc", page+",10");
            application.getSQLiteDatabasePool().releaseSQLiteDatabase(tasqLiteDatabase);
            return list;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public List<CheckDataBean> queryCheckAll (TAApplication application) {
        try {
            TASQLiteDatabase tasqLiteDatabase = application.getSQLiteDatabasePool().getSQLiteDatabase();
            if (!tasqLiteDatabase.hasTable(CheckDataBean.class)) {
                return null;
            }
            List<CheckDataBean> list = tasqLiteDatabase.query(CheckDataBean.class, false, null,
                    null, null, "create_time desc", "10");
            application.getSQLiteDatabasePool().releaseSQLiteDatabase(tasqLiteDatabase);
            return list;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询聊天记录
     * @param lastTime
     * @param ChatID
     * @return
     */
    /*public List<ChatMessageBean> queryChatObject (TAApplication applicationString lastTime, String ChatID){
        try {
            TASQLiteDatabase tasqLiteDatabase = application.getSQLiteDatabasePool().getSQLiteDatabase();
            if (!tasqLiteDatabase.hasTable(ChatMessageBean.class)) {
                return null;
            }
            String where = "ChatID="+ChatID;
            if (!TextUtils.isEmpty(lastTime)) {
                where += " AND SendTime<" + lastTime;
            }
            List<ChatMessageBean> list = tasqLiteDatabase.query(ChatMessageBean.class, false, where,
                    null, null, "SendTime desc", "10");
            Collections.sort(list, new Comparator<ChatMessageBean>() {
                @Override
                public int compare(ChatMessageBean bean, ChatMessageBean t1) {
                    try {
                        long time1 = Long.parseLong(bean.getSendTime());
                        long time2 = Long.parseLong(t1.getSendTime());
                        int result = 0;
                        if (time1>time2) {
                            result = 1;
                        } else if (time1<time2) {
                            result = -1;
                        }
                        return result;
                    } catch (Exception e){

                    }
                    return 0;
                }
            });
            application.getSQLiteDatabasePool().releaseSQLiteDatabase(tasqLiteDatabase);
            return list;
        } catch (Exception e){
            ExceptionUtils.ExceptionSend(e);
        }
        return null;
    }

    public void insertChatHistory(List<ChatMessageBean> list) {
        try {
            TASQLiteDatabase tasqLiteDatabase = application.getSQLiteDatabasePool().getSQLiteDatabase();
            if (!tasqLiteDatabase.hasTable(ChatMessageBean.class)) {
                tasqLiteDatabase.creatTable(ChatMessageBean.class);
                Log.e("DBHelper", ChatMessageBean.class.getSimpleName()+"表已创建");
            }
            List<ChatMessageBean> find;
            String where;
            for (ChatMessageBean bean:list) {
                where = "ChatID="+bean.getChatID() + " AND SendTime=" + bean.getSendTime() + " AND ToID=" + bean.getToID();
                find = tasqLiteDatabase.query(ChatMessageBean.class, false, where, null, null, null, null);
                if (find==null||find.size()==0) {
                    tasqLiteDatabase.insert(bean);
                }
            }
            application.getSQLiteDatabasePool().releaseSQLiteDatabase(tasqLiteDatabase);
        } catch (Exception e){
            ExceptionUtils.ExceptionSend(e);
        }
    }*/

}
