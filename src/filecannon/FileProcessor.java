package filecannon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileProcessor {
    private byte[] fileContents;
    private String fileName;

    /**
     * Creates a placeholder processor
     */
    public FileProcessor() {
        this("", new byte[0]);
    }


    /**
     * Creates a file processor -- used by the client
     * @param fileName The file to use
     */
    public FileProcessor(String fileName) throws IOException {
        this.fileName = fileName;
        this.fileContents = this.read();
    }

    /**
     * Creates a file processor -- used by the server
     * @param fileName
     * @param fileContents
     */
    public FileProcessor(String fileName, byte[] fileContents) {
        this.fileContents = fileContents;
        this.fileName = fileName;
    }

    /**
     * writes the file to disk
     * @throws IOException If there is an issue writing the file
     */
    public void write() throws IOException {
        File newFile = new File(this.fileName);
        if(newFile.exists()) {
            newFile.delete();
        }
        if(!newFile.createNewFile()) {
            System.err.printf("File \"%s\" cannot be created.\n", this.fileName);
            throw new IOException();
        }
        FileOutputStream fos = new FileOutputStream(newFile);
        fos.write(this.fileContents);
        System.out.printf("Recieved new file: %s\n", this.fileName);
        fos.close();
    }


    /**
     * Gets the currently stored file contents
     * @return The file contents
     */
    public byte[] getFileContents() {
        return this.fileContents;
    }

    /**
     * used to read a file from disk
     * @return the file read from disk
     * @throws IOException If there is an issue reading the file
     */
    private byte[] read() throws IOException {
        File file = new File(this.fileName);
        FileInputStream fis = new FileInputStream(file);
        this.fileContents = new byte[(int) file.length()];
        fis.read(this.fileContents);
        fis.close();
        return this.fileContents;
    }


}
