package eDLineEditor;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * ed命令解析器
 */
public class CommandResolver{

    /**
     * 初始化一个ed命令解析器
     *
     * @param fileBuffer  文件缓存器
     * @param flagManager 标记管理器
     */
    CommandResolver(FileBuffer fileBuffer, FlagManager flagManager){
        this.fileBuffer = fileBuffer;
        addressResolver = new AddressResolver(fileBuffer, flagManager);
        this.flagManager = flagManager;
    }

    /**
     * 解析一条ed命令
     *
     * @param line ed命令
     * @return the array list
     * @throws EDLineException the ed line exception
     */
    public ArrayList<SimpleCommandLine> resolve(String line) throws EDLineException {
        ArrayList<SimpleCommandLine> resultLines = new ArrayList<>();
        ArrayList<String> commandSplit = addressResolver.filterAddress(line);
        int addrFirst = addressResolver.singleAddressConvert(commandSplit.get(0));
        int addrSecond = addressResolver.singleAddressConvert(commandSplit.get(1), addrFirst);
        //此时args含有命令类型
        String args = commandSplit.get(2);
        while (args.charAt(0) == ' '){
            args = args.substring(1);
        }
        char commandType = args.charAt(0);
        //分离出命令类型
        args = args.substring(1);
        while (!args.isEmpty() && args.charAt(0) == ' '){
            args = args.substring(1);
        }
        //单地址命令输入多地址报错
        if ((commandType == 'a' || commandType == 'i' || commandType == '=' || commandType == 'z' || commandType == 'k')
                && !commandSplit.get(1).isEmpty()) throw new EDLineException("?");
        //无地址命令输入地址报错
        if ((String.valueOf(commandType).matches("[qQfu]")) && !commandSplit.get(0).isEmpty()) throw new EDLineException("?");
        //较大范围的地址判断
        if ((addrFirst > addrSecond) || (addrFirst < -1) || (addrSecond > fileBuffer.size())) throw new EDLineException("?");
        //0a0i通过
        if (addrFirst == -1){
            if (commandType == 'a' || commandType == 'i'){
                addrFirst = 0;
            } else {
                throw new EDLineException("?");
            }
        }
        //第二地址超出
        if (addrSecond == fileBuffer.size() && commandType != 'i' && commandType != 'a') throw new EDLineException("?");
        if (commandType == 'a' && addrFirst == fileBuffer.size()) addrFirst -= 1;
        ArrayList<String> simpleCommandArgs = new ArrayList<>();

        int targetAddr;

        switch (commandType){
            case 'a':
                resultLines.add(new SimpleCommandLine('s', addrFirst + 1, -1, simpleCommandArgs, fileBuffer.getCurrentLine()));
                break;

            case 'i':
                resultLines.add(new SimpleCommandLine('s', addrFirst, -1, simpleCommandArgs, fileBuffer.getCurrentLine()));
                break;

            case 'c':
                simpleCommandArgs = fileBuffer.getContent(addrFirst, addrSecond);
                restoreDeleteFlagRecord(resultLines, addrFirst, addrSecond);
                resultLines.add(new SimpleCommandLine('d', addrFirst, addrSecond, simpleCommandArgs, fileBuffer.getCurrentLine()));
                resultLines.add(new SimpleCommandLine('s', addrFirst, -1, new ArrayList<>(), fileBuffer.getCurrentLine()));
                if (addrSecond == fileBuffer.size() - 1){
                    fileBuffer.setCurrentLine(addrFirst - 1);
                }else {
                    fileBuffer.setCurrentLine(addrFirst);
                }
                break;

            case 'd':
                restoreDeleteFlagRecord(resultLines, addrFirst, addrSecond);
                simpleCommandArgs = fileBuffer.getContent(addrFirst, addrSecond);
                resultLines.add(new SimpleCommandLine('d', addrFirst, addrSecond, simpleCommandArgs, fileBuffer.getCurrentLine()));
                if (addrSecond == fileBuffer.size() - 1){
                    fileBuffer.setCurrentLine(addrFirst - 1);
                }else {
                    fileBuffer.setCurrentLine(addrFirst);
                }
                break;

            case 'p':
                resultLines.add(new SimpleCommandLine('p', addrFirst, addrSecond, simpleCommandArgs, fileBuffer.getCurrentLine()));
                fileBuffer.setCurrentLine(addrSecond);
                break;

            case '=':
                resultLines.add(new SimpleCommandLine('=', addrFirst, -1, simpleCommandArgs, fileBuffer.getCurrentLine()));
                break;

            case 'z':
                addrFirst = addressResolver.singleAddressConvert(commandSplit.get(0), fileBuffer.getCurrentLine() + 1);
                int printEndLine;
                if (args.isEmpty()){
                    printEndLine = fileBuffer.size() - 1;
                }else {
                    printEndLine = addrFirst + Integer.parseInt(args);
                    if (printEndLine >= fileBuffer.size()) printEndLine = fileBuffer.size() - 1;
                }
                resultLines.add(new SimpleCommandLine('p', addrFirst, printEndLine, simpleCommandArgs, fileBuffer.getCurrentLine()));
                break;

            case 'q':
                resultLines.add(new SimpleCommandLine('q', -1, -1, simpleCommandArgs, fileBuffer.getCurrentLine()));
                break;

            case 'Q':
                resultLines.add(new SimpleCommandLine('Q', -1, -1, simpleCommandArgs, fileBuffer.getCurrentLine()));
                break;

            case 'f':
                simpleCommandArgs.add(args);
                resultLines.add(new SimpleCommandLine('f', -1, -1, simpleCommandArgs, fileBuffer.getCurrentLine()));
                break;

            case 'w':
            case 'W':
                addrFirst = addressResolver.singleAddressConvert(commandSplit.get(0), 0);
                addrSecond = addressResolver.singleAddressConvert(commandSplit.get(1), fileBuffer.size() - 1);
                if (!commandSplit.get(0).isEmpty() && commandSplit.get(1).isEmpty()) addrSecond = addrFirst;
                if (args.isEmpty()){
                    if (fileBuffer.getFileName() == null || fileBuffer.getFileName().isEmpty()) throw new EDLineException("?");
                    else simpleCommandArgs.add(fileBuffer.getFileName());
                }else {
                    simpleCommandArgs.add(args);
                }
                resultLines.add(new SimpleCommandLine(commandType, addrFirst, addrSecond, simpleCommandArgs, fileBuffer.getCurrentLine()));
                break;

            case 'm':
                targetAddr = addressResolver.singleAddressConvert(args);
                HashMap<Character, Integer> flagToMove = flagManager.getFlagBetween(addrFirst, addrSecond);
                if ((targetAddr > addrFirst && targetAddr < addrSecond) || (targetAddr == addrFirst && addrFirst < addrSecond)) throw new EDLineException("?");
                else if (targetAddr < addrFirst){
                    restoreDeleteFlagRecord(resultLines, addrFirst, addrSecond);
                    simpleCommandArgs = fileBuffer.getContent(addrFirst, addrSecond);
                    resultLines.add(new SimpleCommandLine('d', addrFirst, addrSecond, simpleCommandArgs, fileBuffer.getCurrentLine()));
                    resultLines.add(new SimpleCommandLine('i', targetAddr + 1, -1, simpleCommandArgs, fileBuffer.getCurrentLine()));
                    fileBuffer.setCurrentLine(targetAddr + addrSecond - addrFirst + 1);
                    for (Character key : flagToMove.keySet()){
                        ArrayList<String> temp = new ArrayList<>();
                        temp.add(key.toString());
                        resultLines.add(new SimpleCommandLine('k', flagToMove.get(key) - addrFirst + targetAddr + 1, -1, temp, fileBuffer.getCurrentLine()));
                    }
                }else {
                    simpleCommandArgs = fileBuffer.getContent(addrFirst, addrSecond);
                    resultLines.add(new SimpleCommandLine('i', targetAddr + 1, -1, simpleCommandArgs, fileBuffer.getCurrentLine()));
                    restoreDeleteFlagRecord(resultLines, addrFirst, addrSecond);
                    resultLines.add(new SimpleCommandLine('d', addrFirst, addrSecond, simpleCommandArgs, fileBuffer.getCurrentLine()));
                    fileBuffer.setCurrentLine(targetAddr);
                    for (Character key : flagToMove.keySet()){
                        ArrayList<String> temp = new ArrayList<>();
                        temp.add(key.toString());
                        resultLines.add(new SimpleCommandLine('k', flagToMove.get(key) - addrSecond + targetAddr, -1, temp, fileBuffer.getCurrentLine()));
                    }
                }
                break;

            case 't':
                targetAddr = addressResolver.singleAddressConvert(args);
                simpleCommandArgs = fileBuffer.getContent(addrFirst, addrSecond);
                resultLines.add(new SimpleCommandLine('i', targetAddr + 1, -1, simpleCommandArgs, fileBuffer.getCurrentLine()));
                fileBuffer.setCurrentLine(targetAddr + addrSecond - addrFirst + 1);
                break;

            case 'j':
                if (commandSplit.get(1).isEmpty()) addrSecond = addrFirst + 1;
                if (addrFirst < addrSecond && (!commandSplit.get(1).isEmpty() || (commandSplit.get(0).isEmpty() && commandSplit.get(1).isEmpty()))){
                    if (addrSecond >= fileBuffer.size()) throw new EDLineException("?");
                    ArrayList<String> content = fileBuffer.getContent(addrFirst, addrSecond);
                    StringBuilder builder = new StringBuilder();
                    for (String singleLine : content){
                        builder.append(singleLine);
                    }
                    restoreDeleteFlagRecord(resultLines, addrFirst, addrSecond);
                    resultLines.add(new SimpleCommandLine('d', addrFirst, addrSecond, content, fileBuffer.getCurrentLine()));
                    simpleCommandArgs.add(builder.toString());
                    resultLines.add(new SimpleCommandLine('i', addrFirst, -1, simpleCommandArgs, fileBuffer.getCurrentLine()));
                }
                fileBuffer.setCurrentLine(addrFirst);
                break;

            case 's':
                if (!args.isEmpty()) {
                    lastSArgs = args;
                }else {
                    args = lastSArgs;
                }
                if (args == null || args.isEmpty()) throw new EDLineException("?");
                ArrayList<String> replaceArgs = mSplit(args.substring(1), "/");
                int replacePos;
                if (replaceArgs.size() <= 2 || replaceArgs.get(2).equals("")) replacePos = 1;
                else if (replaceArgs.get(2).equals("g")) replacePos = 0;
                else if (replaceArgs.get(2).charAt(0) >= '0' && replaceArgs.get(2).charAt(0) <= '9') replacePos = Integer.parseInt(replaceArgs.get(2));
                else throw new EDLineException("?");
                ArrayList<String> originalContent = fileBuffer.getContent(addrFirst, addrSecond);
                if (replacePos == 0) {
                    for (String singleLine : originalContent) {
                        simpleCommandArgs.add(singleLine.replaceAll(replaceArgs.get(0), replaceArgs.get(1)));
                    }
                } else {
                    for (String singleLine : originalContent) {
                        ArrayList<String> lineSeparated = mSplit(singleLine, replaceArgs.get(0));
                        StringBuilder builder = null;
                        if (lineSeparated.size() > replacePos) {
                            builder = new StringBuilder();
                            for (int i = 0; i < lineSeparated.size() - 1; i++) {
                                builder.append(lineSeparated.get(i));
                                if (i + 1 != replacePos) {
                                    builder.append(replaceArgs.get(0));
                                } else {
                                    builder.append(replaceArgs.get(1));
                                }
                            }
                            builder.append(lineSeparated.get(lineSeparated.size() - 1));
                        }
                        if (builder != null) {
                            simpleCommandArgs.add(builder.toString());
                        } else {
                            simpleCommandArgs.add(singleLine);
                        }
                    }
                }
                if (replacePos == 0) replacePos = 1;
                boolean exist = false;
                int tempCurrentLineOffset = -1;
                for (int i = 0; i < originalContent.size(); i++) {
                    ArrayList<String> lineSeparated = mSplit(originalContent.get(i),replaceArgs.get(0));
                    if (lineSeparated.size() > replacePos) {
                        exist = true;
                        tempCurrentLineOffset = i;
                    }
                }
                if (!exist) throw new EDLineException("?");
                else {
                    restoreDeleteFlagRecord(resultLines, addrFirst, addrSecond);
                    resultLines.add(new SimpleCommandLine('d', addrFirst, addrSecond, originalContent, fileBuffer.getCurrentLine()));
                    resultLines.add(new SimpleCommandLine('i', addrFirst, -1, simpleCommandArgs, fileBuffer.getCurrentLine()));
                    fileBuffer.setCurrentLine(addrFirst + tempCurrentLineOffset);
                }
                break;

            case 'k':
                simpleCommandArgs.add(args);
                if (args.length() != 1 || args.charAt(0) < 'a' || args.charAt(0) > 'z') throw new EDLineException("?");
                resultLines.add(new SimpleCommandLine('k', addrFirst, -1, simpleCommandArgs, fileBuffer.getCurrentLine()));
                break;

            case 'u':
                resultLines.add(new SimpleCommandLine('u', -1, -1, simpleCommandArgs, fileBuffer.getCurrentLine()));
                break;

            default:
                throw new EDLineException("?");

        }

        return resultLines;
    }

    /**
     * 解析输入模式得到的文本
     *
     * @param simpleCommandLine 原简单命令
     * @param text              插入的文本命令
     * @return the simple command line
     */
    public SimpleCommandLine resolveInputModeText(SimpleCommandLine simpleCommandLine, ArrayList<String> text){
        return new SimpleCommandLine('i', simpleCommandLine.getLineStart(), -1, text, simpleCommandLine.getCurrentLineBefore());
    }

    private void restoreDeleteFlagRecord(ArrayList<SimpleCommandLine> lines, int startLine, int endLine) throws EDLineException {
        for (char key : flagManager.getFlagBetween(startLine, endLine).keySet()){
            ArrayList<String> simpleCommandArgs = new ArrayList<>();
            simpleCommandArgs.add(String.valueOf(key));
            lines.add(new SimpleCommandLine('r', flagManager.get(key), -1, simpleCommandArgs, fileBuffer.getCurrentLine()));
        }
    }

    private ArrayList<String> mSplit(String origin, String cut){
        ArrayList<String> result = new ArrayList<>();
        while (origin.contains(cut)){
            int index = origin.indexOf(cut);
            result.add(origin.substring(0, index));
            origin = origin.substring(index + cut.length());
        }
        result.add(origin);
        return result;
    }

    private AddressResolver addressResolver;
    private FileBuffer fileBuffer;
    private FlagManager flagManager;
    private String lastSArgs;
}
