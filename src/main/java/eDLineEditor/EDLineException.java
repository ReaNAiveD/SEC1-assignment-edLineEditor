package eDLineEditor;

/**
 * 一个edLine的异常类，凡是出现相应错误都会被抛出
 */
public class EDLineException extends Exception {

    /**
     * 构造函数，不描述异常
     */
    EDLineException(){
        withDescribe = false;
        describe = "";
    }

    /**
     * 构造函数，描述异常，可以将此描述输出给用户看
     *
     * @param des 描述
     */
    EDLineException(String des){
        withDescribe = true;
        describe = des;
    }

    /**
     * 取得描述的内容
     *
     * @return the describe
     */
    public String getDescribe() {
        return describe;
    }

    /**
     * 是否带有描述
     *
     * @return the boolean
     */
    public boolean isWithDescribe() {
        return withDescribe;
    }

    private boolean withDescribe;
    private String describe;

}
