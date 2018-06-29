package eDLineEditor;


import java.io.*;
import java.util.ArrayList;

/**
 * ED行编辑器
 */
public class EDLineEditor {

	/**
	 * 初始化ED行编辑器
	 */
	EDLineEditor(){
		fileBuffer = new FileBuffer();
		flagManager = new FlagManager();
		recorder = new Recorder();
		commandResolver = new CommandResolver(fileBuffer, flagManager);
		printStream = System.out;
		bufferedReader = new BufferedReader(new InputStreamReader(System.in));
	}

	/**
	 * 初始化ED行编辑器
	 *
	 * @param printStream 输出所用的打印流
	 * @param inputStream 输入流
	 */
	EDLineEditor(PrintStream printStream, InputStream inputStream){
		fileBuffer = new FileBuffer();
		flagManager = new FlagManager();
		recorder = new Recorder();
		commandResolver = new CommandResolver(fileBuffer, flagManager);
		this.printStream = printStream;
		bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	}

	/**
	 * 初始化ED行编辑器
	 *
	 * @param printStream    初始化ED行编辑器
	 * @param bufferedReader the buffered reader
	 */
	EDLineEditor(PrintStream printStream, BufferedReader bufferedReader){
		fileBuffer = new FileBuffer();
		flagManager = new FlagManager();
		recorder = new Recorder();
		commandResolver = new CommandResolver(fileBuffer, flagManager);
		this.printStream = printStream;
		this.bufferedReader = bufferedReader;
	}

	/**
	 * 初始化ED行编辑器
	 *
	 * @param fileName 所编辑文件名
	 */
	EDLineEditor(String fileName){
		fileBuffer = new FileBuffer(fileName);
		flagManager = new FlagManager();
		recorder = new Recorder();
		commandResolver = new CommandResolver(fileBuffer, flagManager);
		printStream = System.out;
		bufferedReader = new BufferedReader(new InputStreamReader(System.in));
	}

	/**
	 * 初始化ED行编辑器
	 *
	 * @param fileName    所编辑文件名
	 * @param printStream 输出所用的打印流
	 * @param inputStream 输入流
	 */
	EDLineEditor(String fileName, PrintStream printStream, InputStream inputStream){
		fileBuffer = new FileBuffer(fileName);
		flagManager = new FlagManager();
		recorder = new Recorder();
		commandResolver = new CommandResolver(fileBuffer, flagManager);
		this.printStream = printStream;
		bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	}

	/**
	 * 初始化ED行编辑器
	 *
	 * @param fileName       所编辑文件名
	 * @param printStream    输出所用的打印流
	 * @param bufferedReader the buffered reader
	 */
	EDLineEditor(String fileName, PrintStream printStream, BufferedReader bufferedReader){
		fileBuffer = new FileBuffer(fileName);
		flagManager = new FlagManager();
		recorder = new Recorder();
		commandResolver = new CommandResolver(fileBuffer, flagManager);
		this.printStream = printStream;
		this.bufferedReader = bufferedReader;
	}

	/**
	 * 直接执行一条简单命令，不会加入记录
	 *
	 * @param commandLine 简单命令
	 * @throws EDLineException the ed line exception
	 */
	public void executeSimpleCommandLine(SimpleCommandLine commandLine) throws EDLineException {
		switch (commandLine.getCommandType()){
			case 's': switchMode(commandLine); break;
			case 'i': insertBefore(commandLine.getLineStart(), commandLine.getArg()); break;
			case 'd': delete(commandLine.getLineStart(), commandLine.getLineEnd()); break;
			case 'p': print(commandLine.getLineStart(), commandLine.getLineEnd()); break;
			case '=': printLineCount(commandLine.getLineStart()); break;
			case 'q': quit(); break;
			case 'Q': forceQuit(); break;
			case 'f': setFileName(commandLine.getArg().get(0)); break;
			case 'w': save(commandLine.getArg().get(0), commandLine.getLineStart(), commandLine.getLineEnd(), false); break;
			case 'W': save(commandLine.getArg().get(0), commandLine.getLineStart(), commandLine.getLineEnd(), true); break;
			case 'k': setFlag(commandLine.getArg().get(0).charAt(0), commandLine.getLineStart()); break;
			case 'r': removeFlag(commandLine.getArg().get(0).charAt(0)); break;
			case 'u': undo(); break;
		}
	}

	/**
	 * 解析一条ed命令
	 *
	 * @param commandLine ed命令
	 * @return the array list
	 * @throws EDLineException the ed line exception
	 */
	public ArrayList<SimpleCommandLine> resolveCommandLine(String commandLine) throws EDLineException {
		return commandResolver.resolve(commandLine);
	}

	/**
	 * 执行一条ed命令
	 *
	 * @param commandLine ed命令
	 */
	public void executeCommandLine(String commandLine){
		ArrayList<SimpleCommandLine> lines = null;
		try {
			lines = resolveCommandLine(commandLine);
			for (SimpleCommandLine line : lines){
				executeSimpleCommandLine(line);
			}
			recorder.record(lines);
		} catch (EDLineException e) {
			if (e.isWithDescribe()){
				printStream.println(e.getDescribe());
			}
		}
	}

	/**
	 * 执行一条简单命令，会加入历史纪录
	 *
	 * @param commandLine 一条简单命令
	 * @throws EDLineException the ed line exception
	 */
	public void executeCommandLine(SimpleCommandLine commandLine) throws EDLineException {
		ArrayList<SimpleCommandLine> commandRecord = new ArrayList<>();
		commandRecord.add(commandLine);
		recorder.record(commandRecord);
		executeSimpleCommandLine(commandLine);
	}


	/**
	 * ed行编辑器是否已退出
	 *
	 * @return the boolean
	 */
	public boolean isQuit() {
		return quit;
	}

	/**
	 * 执行切换到输入模式命令
	 *
	 * @param simpleCommandLine 对应的简单命令
	 * @throws EDLineException the ed line exception
	 */
	public void switchMode(SimpleCommandLine simpleCommandLine) throws EDLineException {
		InputModeBuffer buffer = new InputModeBuffer(bufferedReader);
		//进入输入模式，并获取输入模式输入的内容
		ArrayList<String> inputContent = buffer.getInput();
		if (inputContent != null && inputContent.size() != 0) {
			//将输入内容解析为一条新的插入简单命令并执行
			executeCommandLine(commandResolver.resolveInputModeText(simpleCommandLine, inputContent));
			fileBuffer.setCurrentLine(simpleCommandLine.getLineStart() + inputContent.size() - 1);
		}
		normalize();
	}

	/**
	 * 在某一行前插入
	 *
	 * @param line    插入行
	 * @param content 插入内容
	 */
	public void insertBefore(int line, ArrayList<String> content){
		fileBuffer.insert(line, content);
		flagManager.moveFlagBackward(line, content.size());
		normalize();
	}

	/**
	 * 删除某几行
	 *
	 * @param lineStart 起始行（包含）
	 * @param lineEnd   结束行（包含）
	 * @throws EDLineException the ed line exception
	 */
	public void delete(int lineStart, int lineEnd) throws EDLineException {
		fileBuffer.delete(lineStart, lineEnd);
		flagManager.moveFlagForward(lineEnd + 1, lineEnd - lineStart + 1);
		normalize();
	}

	/**
	 * 打印某几行
	 *
	 * @param lineStart 起始行（包含）
	 * @param lineEnd   结束行（包含）
	 */
	public void print(int lineStart, int lineEnd){
		for (String s : fileBuffer.getContent(lineStart, lineEnd)){
			printStream.println(s);
		}
		normalize();
	}

	/**
	 * 打印外部行号
	 *
	 * @param line 内部行号
	 */
	public void printLineCount(int line){
		printStream.println(line + 1);
		normalize();
	}

	/**
	 * 未保存需确认的退出
	 */
	public void quit(){
		normalize();
		if(normalQuitCount == 0 && !recorder.isEmpty() && recorder.getTopType() != 'w' && recorder.getTopType() != 'W'){
			normalQuitCount++;
			printStream.println("?");
		}else {
			forceQuit();
		}
	}

	/**
	 * 强制退出
	 */
	public void forceQuit(){
		quit = true;
	}

	/**
	 * 设置文件名
	 *
	 * @param fileName 文件名
	 * @throws EDLineException the ed line exception
	 */
	public void setFileName(String fileName) throws EDLineException {
		if (fileName != null && !fileName.isEmpty()) {
			fileBuffer.setFileName(fileName);
		}else {
			if (fileBuffer.getFileName()!= null && !fileBuffer.getFileName().isEmpty()){
				printStream.println(fileBuffer.getFileName());
			}else {
				throw new EDLineException("?");
			}
		}
		normalize();
	}

	/**
	 * 保存部分或全部内容
	 *
	 * @param fileName  保存的文件名
	 * @param startLine 起始行（包含）
	 * @param endLine   结束行（包含）
	 * @param append    文件保存模式
	 */
	public void save(String fileName,int startLine, int endLine, boolean append){
		try {
			if (fileName != null && !fileName.isEmpty() && (fileBuffer.getFileName() == null || fileBuffer.getFileName().isEmpty())) {
				fileBuffer.setFileName(fileName);
			}
			if (fileName != null) {
				PrintStream saveStream = new PrintStream(new FileOutputStream(fileName, append));
				ArrayList<String> content = fileBuffer.getContent(startLine, endLine);
				for (String line : content) {
					saveStream.println(line);
				}

			}
		}catch (IOException e){
			e.printStackTrace();
		}
		normalize();
	}

	/**
	 * 设置标志
	 *
	 * @param flag 标志符号
	 * @param line 行号
	 */
	public void setFlag(char flag, int line){
		flagManager.addFlag(flag, line);
		normalize();
	}

	/**
	 * 移除标志
	 *
	 * @param key 标志符号
	 */
	public void removeFlag(char key){
		flagManager.deleteFlag(key);
		normalize();
	}

	/**
	 * 撤销一步
	 *
	 * @throws EDLineException the ed line exception
	 */
	public void undo() throws EDLineException {
		ArrayList<SimpleCommandLine> undoCommandLines = recorder.getRecord();
		for (SimpleCommandLine line : undoCommandLines){
			SimpleCommandLine reversedLine;
			if (line.getCommandType() == 'i') {
				reversedLine = new SimpleCommandLine('d', line.getLineStart(),
						line.getLineStart() + line.getArg().size() - 1, line.getArg(), line.getCurrentLineBefore());
				executeSimpleCommandLine(reversedLine);
			}
			else if (line.getCommandType() == 'd') {
				reversedLine = new SimpleCommandLine('i', line.getLineStart(), -1, line.getArg(), line.getCurrentLineBefore());
				executeSimpleCommandLine(reversedLine);
			}
			else if (line.getCommandType() == 'r') {
				reversedLine = new SimpleCommandLine('k', line.getLineStart(), -1, line.getArg(), line.getCurrentLineBefore());
				executeSimpleCommandLine(reversedLine);
			}
			fileBuffer.setCurrentLine(line.getCurrentLineBefore());
		}
		normalize();
	}

	private void normalize(){
		normalQuitCount = 0;
	}

	private FileBuffer fileBuffer;
	private CommandResolver commandResolver;
	private FlagManager flagManager;
	private Recorder recorder;
	private PrintStream printStream;
	private BufferedReader bufferedReader;
	private boolean quit = false;
	private int normalQuitCount = 0;

	/**
	 * 接收用户控制台的输入，解析命令，根据命令参数做出相应处理。
	 * 不需要任何提示输入，不要输出任何额外的内容。
	 * 输出换行时，使用System.out.println()。或者换行符使用System.getProperty("line.separator")。
	 * <p>
	 * 待测方法为public static void main(String[] args)方法。args不传递参数，所有输入通过命令行进行。
	 * 方便手动运行。
	 * <p>
	 * 说明：可以添加其他类和方法，但不要删除该文件，改动该方法名和参数，不要改动该文件包名和类名
	 *
	 * @param args the input arguments
	 */
	public static void main(String[] args) {

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		EDLineEditor edLineEditor;
		try{
			String getIn =  br.readLine();
			if (getIn.split(" ").length == 1){
				edLineEditor = new EDLineEditor(System.out, br);
			}else {
				edLineEditor = new EDLineEditor(getIn.split(" ")[1], System.out, br);
			}

			while(true){
				String commandLine = br.readLine();
				if (commandLine == null){
					break;
				}

				edLineEditor.executeCommandLine(commandLine);

				if (edLineEditor.isQuit()){
					break;
				}

			}
		}catch (IOException e){
			e.printStackTrace();
		}

	}


}
