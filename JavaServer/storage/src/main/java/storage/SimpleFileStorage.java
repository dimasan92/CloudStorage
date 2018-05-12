package storage;

import common.Constants;
import database.Database;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class SimpleFileStorage implements FileStorage {
    private Connection connection;
    private String mainDirectory;

    private PreparedStatement psGetUserFolder;

    public SimpleFileStorage(Database database) {
        this.mainDirectory = Constants.SERVER_MAIN_DIRECTORY;
        this.connection = database.getConnection();
        preparedStatements();
    }

    @Override
    public boolean assignFolderToUser(String nickname) {
        return getUserFolder(nickname).mkdir();
    }

    @Override
    public File getUserFolder(String nickname) {
        ResultSet rs = null;
        int userId = -1;
        try {
            psGetUserFolder.setString(1, nickname);
            rs = psGetUserFolder.executeQuery();
            if (!rs.next()) {
                return null;
            }
            userId = rs.getInt("id");
            if (userId == -1) return null;
        } catch (SQLException e) {
            System.out.println("Сервис работы с файлами пользователя: ошибка доступа к БД при добавлении нового пользователя: " + e.getMessage());
        } finally {
            try {
                Objects.requireNonNull(rs).close();
            } catch (SQLException e) {
                System.out.println("Сервис работы с файлами пользователя: ошибка закрытия обращения к БД: " + e.getMessage());
            }
        }
        return new File(mainDirectory + userId);
    }

    @Override
    public boolean writeFileFromUser(DataInputStream inData, String path) {
        try (FileOutputStream outFile = new FileOutputStream(path)) {
            byte[] buffer = new byte[8 * 1024];
            long remainedSize = inData.readLong();
            while (remainedSize > 0) {
                int packSize = (int) Math.min(remainedSize, buffer.length);
                int count = inData.read(buffer, 0, packSize);
                if (count <= 0) {
                    return false;
                }
                outFile.write(buffer, 0, count);
                remainedSize -= count;
            }
            outFile.flush();
            return true;
        } catch (IOException e) {
            System.out.println("Ошибка при добавлении файла " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean removeFile(String path) {
        return new File(path).delete();
    }

//    @Override
//    public void sendFileToUser(DataOutputStream outData, String path, String filename) {
//        File file = new File(path + filename);
//        try (FileInputStream inFile = new FileInputStream(file)) {
//            outData.writeUTF(filename);         // имя файла
//            outData.writeLong(file.length());   // длина файла
//
//            byte[] buffer = new byte[8 * 1024];
//            int count;
//            while ((count = inFile.read(buffer)) != -1) {
//                outData.write(buffer, 0, count);
//            }
//            outData.flush();
//        } catch (IOException e) {
//            System.out.println("Ошибка при отправке файла пользователю " + e.getMessage());
//        }
//    }

//    @Override
//    public boolean replaceFile(DataInputStream inData, String path) {
//        File file = new File(path);
//        if (file.delete()) {
//            return writeFileFromUser(inData, path);
//        }
//        return false;
//    }

    private void preparedStatements() {
        try {
            psGetUserFolder = connection.prepareStatement("SELECT * FROM users WHERE nickname = ?;");
        } catch (SQLException e) {
            System.out.println("Сервис работы с файлами пользователя: ошибка доступа к БД: " + e.getMessage());
        }
    }
}
