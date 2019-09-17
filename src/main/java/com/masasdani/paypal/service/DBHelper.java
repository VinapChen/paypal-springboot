package com.masasdani.paypal.service;

import java.sql.*;

public class DBHelper {

    //mysql数据库驱动，固定写法。连接Oracle时又与之不同,为："oracle.jdbc.driver.OracleDriver"
    private static final String driver = "com.mysql.jdbc.Driver";

    /**
     * 如下是连接数据库的URL地址，
     * 其中，"jdbc:mysql://"   为固定写法
     * "localhost"是连接本机数据库时的写法，当不是连接本机数据库时，要写数据库所在计算机的IP地址。如：172.26.132.253
     * "shopping"是数据库的名称，一定要根据自己的数据库更改。
     * "?useUnicode=true&characterEncoding=UTF-8" 指定编码格式，无需时可省略，
     * 即地址直接为："jdbc:mysql://localhost:3306/shopping"
     */
    private static final String url="jdbc:mysql://localhost:3306/yunba?useUnicode=true&characterEncoding=UTF-8&useSSL=true";

    private static final String username="root";//数据库的用户名
    private static final String password="";//数据库的密码:这个是自己安装数据库的时候设置的，每个人不同。


    public static Connection conn=null;  //声明数据库连接对象


    // PreparedStatement对象用来执行SQL语句
    private static java.sql.PreparedStatement pst = null;

    //结果集
    private static ResultSet rs = null;


    //静态代码块负责加载驱动
    static
    {
        try
        {
            Class.forName(driver);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }

    //单例模式返回数据库连接对象，供外部调用
    public static Connection getConnection() throws Exception
    {
        Connection connection = null;
        try
        {
            connection = DriverManager.getConnection(url, username, password); //连接数据库
            System.out.println("数据库连接正常！");
            return connection;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return connection;
    }

    public static void init(){
        try {
            conn = getConnection();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static int select_data (String sql, String retcow){
//        System.out.println("sql exe:"+sql);
        try
        {
            if(conn!=null)
            {
                pst = conn.prepareStatement(sql);
                rs = pst.executeQuery();
                while (rs.next()) {
                    return rs.getInt(retcow);
                }
                try {
                    if (rs != null)
                        rs.close();
                    if (pst != null)
                        pst.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                System.out.println("数据库连接异常！");
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

        return 0;
    }

//    public static void update_balance(String sql, double balance) {
//        PreparedStatement updateEXP = null;
//        try {
//            updateEXP = conn.prepareStatement(sql);
//            updateEXP.setString(1, String.valueOf(balance));
//            int updateEXP_done = updateEXP.executeUpdate();
//            System.out.println(updateEXP_done);
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }

}