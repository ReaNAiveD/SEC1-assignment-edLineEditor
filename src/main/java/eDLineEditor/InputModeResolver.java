package eDLineEditor;


import java.util.ArrayList;

/**
 * 输入模式解析器
 */
public class InputModeResolver {

    /**
     * 初始化一个输入模式解析器
     */
    InputModeResolver(){
    }

    /**
     * 解析一行输入，返回两元列表，第一个为类型，命令为"c"，文本为"t"；若为文本，第二个存具体内容
     *
     * @param line 输入内容
     * @return the array list
     */
    public ArrayList<String> resolve(String line){
        ArrayList<String> result = new ArrayList<>();
        if (line.equals(".")){
            result.add("c");
            result.add(line);
        }else {
            result.add("t");
            result.add(line);
        }
        return result;
    }
}
