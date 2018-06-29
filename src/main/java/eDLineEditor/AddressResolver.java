package eDLineEditor;


import java.util.ArrayList;

/**
 * 地址解析器，主要将以命令的地址部分转化为真实地址行号
 */
public class AddressResolver {

    /**
     * 构造器
     *
     * @param fileContent 文件内容缓存
     * @param flagManager 相应文件的标记管理器
     */
    AddressResolver(FileBuffer fileContent, FlagManager flagManager){
        this.fileContent = fileContent;
        this.flagManager = flagManager;
    }

    /**
     * 切分一条命令中最靠前的地址，返回它结束的点
     *
     * @param commandLine 一条ed命令
     * @return the int
     * @throws EDLineException the ed line exception
     */
    public int splitAddress(String commandLine) throws EDLineException {
        if (commandLine.isEmpty()) throw new EDLineException("?");
        //分离点，作为返回值
        int splitPoint = 0;
        //附加+-可能开始的位置
        int appendBegin = 0;
        char type = commandLine.charAt(0);
        switch (type){
            case '.':
            case '$':
                splitPoint = 1;
                break;
            case '/':
                splitPoint = commandLine.indexOf('/', 1) + 1;
                break;
            case '?':
                splitPoint = commandLine.indexOf('?', 1) + 1;
                break;
            case '\'':
                splitPoint = 2;
                break;
            default:
                if ('0' <= type && type <= '9') {
                    char temp;
                    for (int i = 0; i < commandLine.length(); i++) {
                        temp = commandLine.charAt(i);
                        if (temp < '0' || temp > '9') {
                            splitPoint = i;
                            break;
                        }
                    }
                }
                break;
        }
        appendBegin = splitPoint;
        if (appendBegin < commandLine.length()) {
            char flag = commandLine.charAt(appendBegin);
            if (flag == '+' || flag == '-'){
                char temp;
                for (int i = appendBegin + 1; i < commandLine.length(); i++) {
                    temp = commandLine.charAt(i);
                    if (temp <= '0' || temp >= '9') {
                        splitPoint = i;
                        break;
                    }
                }
            }
        }

        return splitPoint;
    }

    /**
     * 过滤一条命令的地址部分，返回值为一个字符串列表，前两个元素为地址，后一个为命令及参数
     *
     * @param commandLine 一条ed命令
     * @return the array list
     * @throws EDLineException the ed line exception
     */
    public ArrayList<String> filterAddress(String commandLine) throws EDLineException {
        ArrayList<String> result = new ArrayList<>();
        boolean beginWithComma = false;
        //命令为空，即无输入直接回车
        if (commandLine.isEmpty()) throw new EDLineException("?");
        //若为;
        if (commandLine.charAt(0) == ';'){
            result.add(".");
            result.add("$");
            result.add(commandLine.substring(1));
            return result;
        }
        //若为,则可能是全部，可能是从默认开始
        if (commandLine.charAt(0) == ',') beginWithComma = true;
        //分离出第一个地址
        int splitPoint = splitAddress(commandLine);
        result.add(commandLine.substring(0, splitPoint));
        //若为纯地址
        if (commandLine.length() <= splitPoint) throw new EDLineException("?");
        if (commandLine.charAt(splitPoint) == ','){
            commandLine = commandLine.substring(splitPoint + 1);
            splitPoint = splitAddress(commandLine);
            result.add(commandLine.substring(0, splitPoint));
        } else {
            result.add("");
        }
        if (result.get(0).isEmpty() && result.get(1).isEmpty() && beginWithComma){
            result.remove(0);
            result.add(0, "1");
            result.remove(1);
            result.add(1, "$");
        }
        result.add(commandLine.substring(splitPoint));
        return result;
    }

    /**
     * 将单个命令中的地址转换为真实地址，若为空返回当前行
     *
     * @param address 单个命令地址
     * @return the int
     * @throws EDLineException the ed line exception
     */
    public int singleAddressConvert(String address) throws EDLineException {
        return singleAddressConvert(address, fileContent.getCurrentLine());
    }

    /**
     * 将单个命令中的地址转换为真实地址，若为空返回默认行
     *
     * @param address     单个命令地址
     * @param defaultLine 默认行
     * @return the int
     * @throws EDLineException the ed line exception
     */
    public int singleAddressConvert(String address, int defaultLine) throws EDLineException {
        int currentLine = fileContent.getCurrentLine();
        if (address != null && !address.isEmpty()){
            //地址部份地址表示方法类型
            char addressType = address.charAt(0);
            //参考行，针对参考行附加增减行数
            int refLine = currentLine;
            //附加+-可能开始的位置
            int appendBegin = 0;
            switch (addressType){
                case '.':
                    refLine = currentLine;
                    appendBegin = 1;
                    break;
                case '$':
                    refLine = fileContent.size() - 1;
                    appendBegin = 1;
                    break;
                case '/':
                    int endIndex = address.indexOf('/', 1);
                    String strMatched = address.substring(1, endIndex);
                    ArrayList<Integer> linesMatched = fileContent.findLinesContains(strMatched);
                    if (linesMatched.isEmpty()) throw new EDLineException("?");
                    for (Integer i : linesMatched){
                        if (i > currentLine){
                            refLine = i;
                            break;
                        }
                    }
                    if (refLine == currentLine) refLine = linesMatched.get(0);
                    appendBegin = endIndex + 1;
                    break;
                case '?':
                    endIndex = address.indexOf('?', 1);
                    strMatched = address.substring(1, endIndex);
                    linesMatched = fileContent.findLinesContains(strMatched);
                    if (linesMatched.isEmpty()) throw new EDLineException();
                    for (int i = 0; i < linesMatched.size(); i++){
                        if (linesMatched.get(i) >= currentLine){
                            if (i > 0){
                                refLine = linesMatched.get(i - 1);
                            }else{
                                refLine = linesMatched.get(linesMatched.size() - 1);
                            }
                            break;
                        }
                    }
                    if (refLine == currentLine) refLine = linesMatched.get(linesMatched.size() - 1);
                    appendBegin = endIndex + 1;
                    break;
                case '\'':
                    refLine = flagManager.get(address.charAt(1));
                    appendBegin = 2;
                    break;
                default:
                    if ('0' <= addressType && addressType <= '9'){
                        char temp;
                        refLine = 0;
                        for (int i = 0; i < address.length(); i++){
                            temp = address.charAt(i);
                            if ('0' <= temp && temp <= '9'){
                                refLine = refLine * 10 + temp - '0';
                            }else {
                                appendBegin = i;
                                break;
                            }
                        }
                        refLine--;
                    }
            }

            //考虑+-的情况
            if (appendBegin == address.length() - 1 && (address.charAt(appendBegin) == '+' || address.charAt(appendBegin) == '-')) throw new EDLineException("?");
            if (appendBegin < address.length() - 1) {
                char flag = address.charAt(appendBegin);
                if (address.charAt(appendBegin + 1) >= '0' && address.charAt(appendBegin + 1) <= '9') {
                    int offset = Integer.parseInt(address.substring(appendBegin + 1));
                    switch (flag) {
                        case '+':
                            refLine += offset;
                            break;
                        case '-':
                            refLine -= offset;
                            break;
                    }
                }else {
                    throw new EDLineException("?");
                }
            }
            return refLine;
        }
        return defaultLine;
    }


    private FileBuffer fileContent;
    private FlagManager flagManager;

    /**
     * 测试
     *
     * @param args the args
     */
    public static void main(String[] args){
        FileBuffer fb = new FileBuffer("test");
        System.out.println(fb.getContent(0,1).get(1));
        ArrayList<String> temp = new ArrayList<>();
        temp.add("a");
        temp.add("b");
        fb.insert(0, temp);

        AddressResolver addressResolver = new AddressResolver(fb, new FlagManager());
        try {
            System.out.println(addressResolver.singleAddressConvert("?es?-1"));
        } catch (EDLineException e) {
            e.printStackTrace();
        }
    }

}
