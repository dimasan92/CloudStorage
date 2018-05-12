package storage;

import java.io.DataInputStream;
import java.io.File;

public interface FileStorage {
    boolean assignFolderToUser(String nickname);
    File getUserFolder(String nickname);
    boolean writeFileFromUser(DataInputStream inData, String path);
    boolean removeFile(String path);
    //    void sendFileToUser(DataOutputStream outData, String path, String filename);
//    boolean replaceFile(DataInputStream inData, String path);
}
