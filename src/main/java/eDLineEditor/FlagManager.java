package eDLineEditor;


import java.util.HashMap;

/**
 * 标记管理器
 */
public class FlagManager {

    /**
     * 构造器
     */
    FlagManager(){
        flagMap = new HashMap<>();
    }

    /**
     * 增加一个标记，如果此标记已存在则替换之
     *
     * @param flag 标记符号
     * @param line 特定行
     */
    public void addFlag(char flag, int line){
        if (flagMap.containsKey(flag)){
            deleteFlag(flag);
        }
        flagMap.put(flag, line);
    }

    /**
     * 删除一个标记
     *
     * @param flag 标记符号
     */
    public void deleteFlag(char flag){
        flagMap.remove(flag);
    }

    /**
     * 根据行号删除一个标记
     *
     * @param line 行号
     */
    public void deleteFlag(int line){
        for (Character key : flagMap.keySet()) {
            if (flagMap.get(key) == line){
                flagMap.remove(key);
            }
        }
    }

    /**
     * 根据一对行号删除之间的标记
     *
     * @param startLine 起始行（包含）
     * @param endLine   结束行（包含）
     */
    public void deleteFlag(int startLine, int endLine){
        for (Character key : flagMap.keySet()) {
            if (flagMap.get(key) >= startLine && flagMap.get(key) <= endLine){
                flagMap.remove(key);
            }
        }
    }

    /**
     * 将某行之后的所有标记后移（前移为负值）
     *
     * @param startLine 起始行（包含）
     * @param offset    后移行数
     */
    public void moveFlagBackward(int startLine, int offset){
        for (Character key : flagMap.keySet()) {
            if (flagMap.get(key) >= startLine) {
                flagMap.replace(key, flagMap.get(key) + offset);
            }
        }
    }

    /**
     * 将某两行之间的所有标记后移（前移为负值）
     *
     * @param startLine 起始行（包含）
     * @param endLine   结束行（包含）
     * @param offset    后移行数
     */
    public void moveFlagBackward(int startLine, int endLine, int offset){
        for (Character key : flagMap.keySet()) {
            if (flagMap.get(key) >= startLine && flagMap.get(key) <= endLine) {
                flagMap.replace(key, flagMap.get(key) + offset);
            }
        }
    }

    /**
     * 将某行之后的所有标记前移
     *
     * @param startLine 起始行（包含）
     * @param offset    前移行数
     */
    public void moveFlagForward(int startLine, int offset){
        moveFlagBackward(startLine, -offset);
    }

    /**
     * 将某两行之间的所有标记前移
     *
     * @param startLine 起始行（包含）
     * @param endLine   结束行（包含）
     * @param offset    前移行数
     */
    public void moveFlagForward(int startLine, int endLine, int offset){
        moveFlagBackward(startLine, endLine, -offset);
    }

    /**
     * 根据一个标记符号得到对应行号
     *
     * @param flag 标记符号
     * @return the int
     * @throws EDLineException the ed line exception
     */
    public int get(char flag) throws EDLineException {
        if (flagMap.keySet().contains(flag)) {
            return flagMap.get(flag);
        } else {
            throw new EDLineException("?");
        }
    }

    /**
     * 得到两行之间所有的标记
     *
     * @param startLine 起始行（包含）
     * @param endLine   结束行（包含）
     * @return the hash map
     */
    public HashMap<Character, Integer> getFlagBetween(int startLine, int endLine){
        HashMap<Character, Integer> result = new HashMap<>();
        for (Character key : flagMap.keySet()){
            if (flagMap.get(key) >= startLine && flagMap.get(key) <= endLine){
                result.put(key, flagMap.get(key));
            }
        }
        return result;
    }

    private HashMap<Character, Integer> flagMap;

}
