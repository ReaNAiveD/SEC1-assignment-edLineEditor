package eDLineEditor;


import java.io.*;
import java.util.ArrayList;

/**
 * 文件缓存器
 */
public class FileBuffer {

    /**
     * 构造器，从空文件开始操作，新文件
     */
    FileBuffer(){
        lines = new ArrayList<String>();
        fileName = null;
        currentLine = 0;
    }

    /**
     * 构造器，含有一定行，新文件
     *
     * @param lines 原先含有的行
     */
    FileBuffer(ArrayList<String> lines){
        this.lines = new ArrayList<>(lines);
        fileName = null;
        currentLine = lines.size() - 1;
    }

    /**
     * 构造器，操作已有文件
     *
     * @param path 原文件路径
     */
    FileBuffer(String path){
        BufferedReader br = null;
        lines = new ArrayList<String>();
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }

        try {
            if (br != null) {
                String line;
                while ((line = br.readLine()) != null){
                    lines.add(line);
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        fileName = path;
        currentLine = lines.size() - 1;
    }

    /**
     * 行数
     *
     * @return the int
     */
    public int size(){
        return lines.size();
    }

    /**
     * 得到特定行之间的内容
     *
     * @param start 起始行（包含）
     * @param end   结束行（包含）
     * @return the array list
     */
    public ArrayList<String> getContent(int start, int end){
        ArrayList<String> result = new ArrayList<String>();
        for (int i = start; i < end + 1 && i < lines.size(); i++){
            result.add(lines.get(i));
        }
        return result;
    }

    /**
     * 删除某几行
     *
     * @param start 起始行（包含）
     * @param end   结束行（包含）
     * @return the array list
     * @throws EDLineException the ed line exception
     */
    public ArrayList<String> delete(int start, int end) throws EDLineException{
        if (end >= size()) throw new EDLineException("?");
        ArrayList<String> result = getContent(start, end);
        for (int i = start; i < end + 1; i++) {
            lines.remove(start);
        }
        return result;
    }

    /**
     * 在特定行后插入
     *
     * @param start         特定行
     * @param linesToInsert 插入的内容
     */
    public void insert(int start, ArrayList<String> linesToInsert){
        int i = 0;
        if (start > lines.size()) start = lines.size();
        for (String line : linesToInsert){
            lines.add(start + i, line);
            i++;
        }
    }

    /**
     * 在文件中找到含有特定内容的某几行，返回一个行号构成的列表
     *
     * @param s 查找的内容
     * @return the array list
     */
    public ArrayList<Integer> findLinesContains(String s){
        ArrayList<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < lines.size(); i++){
            if (lines.get(i).contains(s)){
                result.add(new Integer(i));
            }
        }

        return result;
    }

    /**
     * 取得当前行
     *
     * @return the current line
     */
    public int getCurrentLine() {
        return currentLine;
    }

    /**
     * 设置当前行
     *
     * @param currentLine 当前行
     */
    public void setCurrentLine(int currentLine) {
        this.currentLine = currentLine;
    }

    /**
     * 得到文件名
     *
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * 设置文件名
     *
     * @param fileName 文件名
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


    private int currentLine;
    private ArrayList<String> lines;
    private String fileName;

}
