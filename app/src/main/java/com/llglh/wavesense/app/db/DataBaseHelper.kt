package com.llglh.wavesense.app.db
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
class DataBaseHelper(
    context: Context?,
    name: String? = DB_NAME,      // 设置默认值为 todo.db
    factory: SQLiteDatabase.CursorFactory? = null,
    version: Int = DB_VERSION     // 设置默认值为 1
) : SQLiteOpenHelper(context, name, factory, version) {
    companion object {
        const val DB_NAME = "todo.db"
        const val DB_VERSION = 1

        //用户表和表的字段
        const val TABLE_USER = "user"
        const val COLUMN_USER_ID = "id"
        const val COLUMN_USER_NAME = "name"
        const val COLUMN_USER_PASSWORD = "password"
        //建表语句

        const val CREATE_TABLE_USER = """
            CREATE TABLE $TABLE_USER (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_NAME TEXT UNIQUE NOT NULL,
                $COLUMN_USER_PASSWORD TEXT NOT NULL)
                """
    }

    override fun onCreate(db: SQLiteDatabase?) {
        //创建用户表
        db?.execSQL(CREATE_TABLE_USER)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        //更新策略，给表增加一个年龄字段
        if (oldVersion < newVersion) {
            db?.execSQL("ALTER TABLE $TABLE_USER ADD COLUMN age INTEGER")
        }
    }

    //注册用户功能
    fun registerUser(name: String, password: String): Boolean {

        val db = writableDatabase
        val values = ContentValues()
        values.put(COLUMN_USER_NAME, name)
        values.put(COLUMN_USER_PASSWORD, password)
        val result = db.insert(TABLE_USER, null, values)
//        db.close()
        return result != -1L
    }

    // 检查用户是否已注册
    fun checkUserExist(name: String): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_USER WHERE $COLUMN_USER_NAME = ?"
        val cursor = db.rawQuery(query, arrayOf(name))
        val exists = cursor.count > 0
        cursor.close()
//        db.close()
        return exists
    }

    //删除用户功能
    fun deleteUser(name: String): String {
        //先查询是否存在，存在再删除，分三种情况，用户不存在，删除成功，删除失败
        val db = writableDatabase
        val result = when {
            !checkUserExist(name) -> "用户不存在"
            db.delete(TABLE_USER, "$COLUMN_USER_NAME = ?", arrayOf(name)) > 0 -> "删除成功"
            else -> "删除失败"
        }
        return result
    }

    //更新用户密码
    fun updateUserPassword(name: String, newPassword: String): String {
        val db = writableDatabase
        val result = when {
            !checkUserExist(name) -> "用户不存在"
            db.update(
                TABLE_USER,
                ContentValues().apply { put(COLUMN_USER_PASSWORD, newPassword) },
                "$COLUMN_USER_NAME = ?",
                arrayOf(name)
            ) > 0 -> "更新成功"

            else -> "更新失败"
        }
        return result
    }

    //查询所有用户
    @SuppressLint("Range")
    fun queryAllUsers(): List<User> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USER,
            arrayOf(COLUMN_USER_ID, COLUMN_USER_NAME, COLUMN_USER_PASSWORD),
            null,
            null,
            null,
            null,
            null,
            null
        )
        //遍历游标，创建User对象，添加到列表中
        val users = mutableListOf<User>()
        while (cursor.moveToNext()) {
            val user = User(
                cursor.getInt(cursor.getColumnIndex(COLUMN_USER_ID)),
                cursor.getString(cursor.getColumnIndex(COLUMN_USER_NAME)),
                cursor.getString(cursor.getColumnIndex(COLUMN_USER_PASSWORD)))
            users.add(user)
        }
        cursor.close()
        return users
    }
    //登录功能
    fun login(name: String, password: String): Boolean {
        val db = readableDatabase
        val query = "SELECT $COLUMN_USER_PASSWORD FROM $TABLE_USER WHERE $COLUMN_USER_NAME = ?"
        val cursor = db.rawQuery(query, arrayOf(name))
        var isLoginSuccess = false

        try {
            if (cursor.moveToFirst()) {
                val dbPassword = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PASSWORD))
                if (password == dbPassword) {
                    isLoginSuccess = true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor.close()
        }

        return isLoginSuccess
    }
}
