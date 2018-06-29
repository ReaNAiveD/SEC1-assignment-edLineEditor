package eDLineEditor;


import java.util.ArrayList;

/**
 * 简单命令类，ed命令最终解析为一组简单命令执行
 */
public class SimpleCommandLine {

    /**
     * 初始化一条简单命令
     *
     * @param commandType       命令类型
     * @param lineStart         命令操作的起始行（包含），不使用则为-1
     * @param lineEnd           命令操作的结束行（包含），不使用则为-1
     * @param arg               命令附加的参数组
     * @param currentLineBefore 存储此次操作前所在行号
     */
    SimpleCommandLine(char commandType, int lineStart, int lineEnd, ArrayList<String> arg, int currentLineBefore){
        this.commandType = commandType;
        this.lineStart = lineStart;
        this.lineEnd = lineEnd;
        this.arg = arg;
        this.currentLineBefore = currentLineBefore;
        group = -1;
    }

    /**
     * 得到命令类型
     *
     * @return the command type
     */
    public char getCommandType() {
        return commandType;
    }

    /**
     * 得到起始行
     *
     * @return the line start
     */
    public int getLineStart() {
        return lineStart;
    }

    /**
     * 得到结束行
     *
     * @return the line end
     */
    public int getLineEnd() {
        return lineEnd;
    }

    /**
     * 得到参数组
     *
     * @return the arg
     */
    public ArrayList<String> getArg() {
        return arg;
    }

    /**
     * 得到命令执行前的当前行
     *
     * @return the current line before
     */
    public int getCurrentLineBefore() {
        return currentLineBefore;
    }

    /**
     * 得到存储组号
     *
     * @return the int
     */
    public int getGroup(){
        return group;
    }

    /**
     * 设置存储组号
     *
     * @param group the group
     */
    public void setGroup(int group){
        this.group = group;
    }

    private char commandType;
    private int lineStart;
    private int lineEnd;
    private ArrayList<String> arg;
    private int currentLineBefore;
    //组别，用于存储记录时分辨是不是一条命令所细分的
    private int group;
}
