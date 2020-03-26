package com.javaxl.miaosha_05.util;

import com.javaxl.miaosha_05.domain.MiaoshaUser;
import com.javaxl.miaosha_05.shiro.PasswordHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserUtil {

    private static void createUser(int count) throws Exception {
        List<MiaoshaUser> users = new ArrayList<MiaoshaUser>(count);
        //生成用户
        for (int i = 0; i < count; i++) {
            //生成随机盐
            String salt = PasswordHelper.createSalt();
            MiaoshaUser user = new MiaoshaUser();
            user.setId(15200000000L + i);
            user.setLoginCount(0);
            user.setNickname("user" + i);
            user.setRegisterDate(new Date());
            user.setSalt(salt);
            //模拟将用户输入的密码进行MD5(明文+固定Salt)加密后，再次进行MD5（第一次加密后的密码+随机Salt）加密
            user.setPassword(PasswordHelper.createCredentials(MD5Util.inputPassToFormPass("123456"), salt));
            users.add(user);
        }
        System.out.println("create user");
        //插入数据库
        Connection conn = DBUtil.getConn();
        String sql = "insert into miaosha_user(login_count, nickname, register_date, salt, password, id)values(?,?,?,?,?,?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        for (int i = 0; i < users.size(); i++) {
            MiaoshaUser user = users.get(i);
            pstmt.setInt(1, user.getLoginCount());
            pstmt.setString(2, user.getNickname());
            pstmt.setTimestamp(3, new Timestamp(user.getRegisterDate().getTime()));
            pstmt.setString(4, user.getSalt());
            pstmt.setString(5, user.getPassword());
            pstmt.setLong(6, user.getId());
            pstmt.addBatch();
        }
        pstmt.executeBatch();
        pstmt.close();
        conn.close();
        System.out.println("insert to db");

        //登录，生成token
        /*String urlString = "http://localhost:8080/login/do_login";
        File file = new File("D:/tokens.txt");
        if (file.exists()) {
            file.delete();
        }
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        file.createNewFile();
        raf.seek(0);
        for (int i = 0; i < users.size(); i++) {
            MiaoshaUser user = users.get(i);
            URL url = new URL(urlString);
            HttpURLConnection co = (HttpURLConnection) url.openConnection();
            co.setRequestMethod("POST");
            co.setDoOutput(true);
            OutputStream out = co.getOutputStream();
            String params = "mobile=" + user.getId() + "&password=" + MD5Util.inputPassToFormPass("123456");
            out.write(params.getBytes());
            out.flush();
            InputStream inputStream = co.getInputStream();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte buff[] = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buff)) >= 0) {
                bout.write(buff, 0, len);
            }
            inputStream.close();
            bout.close();
            String response = new String(bout.toByteArray());
            JSONObject jo = JSON.parseObject(response);
            String token = jo.getString("data");
            System.out.println("create token : " + user.getId());

            String row = user.getId() + "," + token;
            raf.seek(raf.length());
            raf.write(row.getBytes());
            raf.write("\r\n".getBytes());
            System.out.println("write to file : " + user.getId());
        }
        raf.close();*/

        System.out.println("over");
    }

    public static void main(String[] args) throws Exception {
        createUser(10);
    }
}
