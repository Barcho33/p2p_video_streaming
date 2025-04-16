package logic.video;

import java.io.File;

public class FileUtils {

    public static int countFiles(File directory, String extenstion) throws IllegalAccessException {

        if(directory == null || !directory.isDirectory()){
            throw new IllegalAccessException("File argument is not directory or doesn't exist!");
        }

        int count = 0;
        File[] files = directory.listFiles();
        if(files != null)
            for(File f : files)
                if(f.isFile() && f.getName().toLowerCase().endsWith(extenstion.toLowerCase()))
                    count++;

        return count;
    }

    public static void deleteDirectoryRecursively(File directory) throws IllegalAccessException {

        if(directory == null || !directory.isDirectory()){
            throw new IllegalAccessException("File argument is not directory or doesn't exist!");
        }

        File[] files = directory.listFiles();
        if(files != null)
            for (File file : files)
                if(file.isDirectory())
                    deleteDirectoryRecursively(file);
                else
                    file.delete();
        directory.delete();
    }
}
