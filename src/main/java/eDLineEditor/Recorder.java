package eDLineEditor;

import java.util.ArrayList;
import java.util.Stack;

/**
 * 历史记录器
 */
public class Recorder {

    /**
     * 初始化一个历史记录器
     */
    Recorder(){
        commandLineStack = new Stack<>();
        historyCount = 0;
    }

    /**
     * 记录一次的一组命令
     *
     * @param commandLines 命令组
     */
    public void record(ArrayList<SimpleCommandLine> commandLines){
        for(SimpleCommandLine commandLine : commandLines){
            //若上一条命令为切换模式，则将其删除
            if (!commandLineStack.empty() && commandLineStack.peek().getCommandType() == 's'){
                commandLineStack.pop();
                historyCount--;
            }
            //若此次命令为存储，则与上一组命令存在同一组，仅作为判断是否有存储行为的依据
            if (!commandLineStack.empty() && (commandLine.getCommandType() == 'w' || commandLine.getCommandType() == 'W')){
                commandLine.setGroup(historyCount - 1);
                commandLineStack.push(commandLine);
            }
            //若此次命令为插入，删除，切换模式，删除标记，则将此次的命令组作为新的一组存储
            if (commandLine.getCommandType() == 'i' || commandLine.getCommandType() == 'd' || commandLine.getCommandType() == 's'
                    || commandLine.getCommandType() == 'r') {
                commandLine.setGroup(historyCount);
                commandLineStack.push(commandLine);
            }
        }
        if (!commandLineStack.empty() && commandLineStack.peek().getGroup() == historyCount) {
            historyCount++;
        }
    }

    /**
     * 将最新的一组命令出栈
     *
     * @return the array list
     */
    public ArrayList<SimpleCommandLine> getRecord(){
        historyCount--;
        ArrayList<SimpleCommandLine> result = new ArrayList<>();
        while (!commandLineStack.empty() && commandLineStack.peek().getGroup() == historyCount){
            result.add(commandLineStack.pop());
        }
        return result;
    }

    /**
     * 得到最新命令的命令类型
     *
     * @return the char
     */
    public char getTopType(){
        return commandLineStack.peek().getCommandType();
    }

    /**
     * 历史记录是否为空
     *
     * @return the boolean
     */
    public boolean isEmpty(){
        return commandLineStack.empty();
    }

    /**
     * 清空历史记录
     */
    public void clear(){
        commandLineStack = new Stack<>();
        historyCount = 0;
    }

    private Stack<SimpleCommandLine> commandLineStack;

    private int historyCount;

}
