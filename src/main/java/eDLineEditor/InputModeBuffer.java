package eDLineEditor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * 输入模式内容缓存器
 */
public class InputModeBuffer {

    /**
     * 构造器
     */
    InputModeBuffer(){
        buffer = new ArrayList<>();
        bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        inputModeResolver = new InputModeResolver();
    }

    /**
     * 构造器
     *
     * @param inputStream 输入流
     */
    InputModeBuffer(InputStream inputStream){
        buffer = new ArrayList<>();
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        inputModeResolver = new InputModeResolver();
    }

    /**
     * 构造器
     *
     * @param bufferedReader the bufferedReader
     */
    InputModeBuffer(BufferedReader bufferedReader){
        buffer = new ArrayList<>();
        this.bufferedReader = bufferedReader;
        inputModeResolver = new InputModeResolver();
    }

    /**
     * 启动输入模式，当结束输入模式时返回输入的内容
     *
     * @return the array list
     */
    public ArrayList<String> getInput(){
        try{
            String line;
            line = bufferedReader.readLine();
            while (line != null){
                //解析本行输入
                ArrayList<String> inputContent = inputModeResolver.resolve(line);
                if (inputContent.get(0).equals("t")) buffer.add(inputContent.get(1));
                //若为命令则相应执行
                else if (inputContent.get(0).equals("c")) {
                    if (inputContent.get(1).equals(".")){
                        break;
                    }
                }
                line = bufferedReader.readLine();
            }
            ArrayList<String> result = buffer;
            buffer = new ArrayList<>();
            return result;
        } catch (IOException e){
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private ArrayList<String> buffer;
    private BufferedReader bufferedReader;
    private InputModeResolver inputModeResolver;

}
