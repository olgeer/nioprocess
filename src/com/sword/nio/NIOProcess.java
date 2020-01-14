package com.sword.nio;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by Max on 2017/9/28.
 */
public class NIOProcess {
    private static HashMap<String, ArrayDeque<Object>> nioMessageMap = new HashMap<>();
    private static HashMap<String, Class> nioProcessMap = new HashMap<>();
    private static ExecutorService pool = Executors.newSingleThreadExecutor();

    //需要初始化的标志
    private static boolean NOT_INIT = true;

    //命令队列名
    private static String COMMAND_QUEUE = "CMD_QUEUE";

    //缓存是否写入文件
    private static boolean WRITE_TO_FILE = true;

    //写文件间隔，单位：秒
    private static int WRITE_INTERVAL = 360;

    //检查时间间隔，单位：秒
    private static int CHECK_INTERVAL = 5;

    //每个处理的时间间隔，单位：毫秒
    private static int SLEEP_INTERVAL = 50;

    //缓存文件存放路径
    private static String CACHE_DATA_PATH = "d:\\dev\\nio.dat";

    //最后一次写入的时间戳
    private static long LAST_TIMESTAMP_OF_WRITE = System.currentTimeMillis();

    //最近的配置
    private static Config config;

    public NIOProcess() {
        initQueue();
    }

    public static void setting(Config cfg) {
        COMMAND_QUEUE = cfg.getCommandQueue();
        WRITE_TO_FILE = cfg.isWriteToFile();
        CACHE_DATA_PATH = cfg.getCacheDataPath();
        WRITE_INTERVAL = cfg.getWriteInterval();
        CHECK_INTERVAL = cfg.getInterval();
        SLEEP_INTERVAL = cfg.getSleepInterval();
        config = cfg;
    }

    public static void init() {
        init(null);
    }

    public static void init(Config cfg) {
        if (cfg != null) {
            setting(cfg);
        }
        new NIOProcess();
    }

    private static void writeFile() {
        try {
            File file = new File(CACHE_DATA_PATH);
            if (!file.exists()) {
                boolean createResult = file.createNewFile();
                if (!createResult) {
                    System.out.println("Create file [" + CACHE_DATA_PATH + "] fail ! Cache cas't write to file.");
                }
            }
            PrintStream ps = new PrintStream(new FileOutputStream(file));
            ps.println(JSONObject.toJSONString(nioMessageMap));// 往文件里写入字符串
            ps.close();
            LAST_TIMESTAMP_OF_WRITE = System.currentTimeMillis();
            System.out.println("Write cache data to file [" + CACHE_DATA_PATH + "] success at " + LAST_TIMESTAMP_OF_WRITE + ".");
        } catch (Exception e) {
            System.out.println("Write cache data to file [" + CACHE_DATA_PATH + "] fail ! Casue:" + e.getMessage());
        }
    }

    private void loadFile() {
        try {
            File file = new File(CACHE_DATA_PATH);
            if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                JSONObject cacheObj = JSONObject.parseObject(br.readLine());
                for (String key : cacheObj.keySet()) {
                    JSONArray values = cacheObj.getJSONArray(key);
                    ArrayDeque<Object> queue = new ArrayDeque<>();
                    for (Object val : values.toArray()) {
                        queue.add(val);
                    }
                    nioMessageMap.put(key, queue);
                }
                br.close();
                System.out.println("Read cache data from file [" + CACHE_DATA_PATH + "] success at " + System.currentTimeMillis() + ".");
            } else {
                System.out.println("Read cache data from file [" + CACHE_DATA_PATH + "] fail ! Cache file was not found.");
            }
        } catch (Exception e) {
            System.out.println("Read cache data from file [" + CACHE_DATA_PATH + "] fail ! Casue:" + e.getMessage());
        }
    }

    private void initQueue() {
        if (NOT_INIT) {
            nioMessageMap.put(COMMAND_QUEUE, new ArrayDeque<>());
            if (WRITE_TO_FILE) {
                loadFile();
            }
            pool.execute(new StayProcess());
            NOT_INIT = false;
        }
    }

    public static void shutdown() {
        if (!NOT_INIT) {
            //putMessage(COMMAND_QUEUE, "exit");
            pool.shutdownNow();
            writeFile();
            nioMessageMap.clear();
            nioProcessMap.clear();
            NOT_INIT = true;
            //System.out.println("IsShutdown:"+pool.isShutdown());
        }
    }

    public static void reRegisterProcesser() {
        if (!NOT_INIT) {
            putMessage(COMMAND_QUEUE, "reload");
        }
    }

    public static void registerMessage(String messageName, Class processer) {
        if (nioMessageMap.get(messageName) == null) {
            nioMessageMap.put(messageName, new ArrayDeque<>());
        }
        if (nioProcessMap.get(messageName) == null) {
            nioProcessMap.put(messageName, processer);
        } else {
            System.out.println(messageName + " already register !");
        }
    }

    public static void putMessage(String messageName, Object message) {
        if (nioMessageMap.get(messageName) != null) {
            nioMessageMap.get(messageName).addLast(JSON.toJSON(message));
        }
    }

    public static Object getFirst(String messageName) {
        Object retObj = null;
        if (nioMessageMap.get(messageName) != null) {
            if (nioMessageMap.get(messageName).size() > 0) {
                retObj = nioMessageMap.get(messageName).getFirst();
            }
        }
        return retObj;
    }

    public static Object getLast(String messageName) {
        Object retObj = null;
        if (nioMessageMap.get(messageName) != null) {
            if (nioMessageMap.get(messageName).size() > 0) {
                retObj = nioMessageMap.get(messageName).getLast();
            }
        }
        return retObj;
    }

    public static Object removeFirst(String messageName) {
        Object retObj = null;
        if (nioMessageMap.get(messageName) != null) {
            if (nioMessageMap.get(messageName).size() > 0) {
                retObj = nioMessageMap.get(messageName).removeFirst();
            }
        }
        return retObj;
    }

    public static Object removeLast(String messageName) {
        Object retObj = null;
        if (nioMessageMap.get(messageName) != null) {
            if (nioMessageMap.get(messageName).size() > 0) {
                retObj = nioMessageMap.get(messageName).removeLast();
            }
        }
        return retObj;
    }

    public static HashMap<String, Class> getNioProcessMap() {
        return nioProcessMap;
    }

    public static void main(String[] args) {
        Config cfg = new Config();
        cfg.setWriteToFile(true);
        //cfg.setCommandQueue("New Queue");
        cfg.setCacheDataPath("d:\\dev\\nio.dat");
        cfg.setInterval(10);
        cfg.setWriteInterval(600);

        NIOProcess.init(cfg);
        NIOProcess.registerMessage("mytest", testProcesser.class);
        NIOProcess.putMessage("mytest", "Hello world !");
        NIOProcess.registerMessage("Second", testProcesser2.class);
        NIOProcess.putMessage("Second", "New message !");

        Scanner sc = new Scanner(System.in);
        String inp = null;
        do {
            System.out.print("Put something to queue mytest :");
            inp = sc.nextLine();
            if (inp.length() > 0) {
                NIOProcess.putMessage("mytest", inp);
            }
        } while (inp.length() > 0);

        //NIOProcess.shutdown();
        NIOProcess.putMessage(cfg.getCommandQueue(), "exit");
    }

    private class StayProcess extends Thread {
        //检查间隔，单位：毫秒
        private int INTERVAL = CHECK_INTERVAL * 1000;

        //注册处理队列大小
        private int PROCESS_MAP_SIZE = 16;

        private boolean doProcess = true;

        private HashMap<String, Processor> initProcessMap() {
            return initProcessMap(null);
        }

        private HashMap<String, Processor> initProcessMap(HashMap<String, Processor> processMap) {
            if (processMap == null) {
                processMap = new HashMap<String, Processor>(PROCESS_MAP_SIZE);
            }
            try {
                Iterator<String> keys = NIOProcess.getNioProcessMap().keySet().iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    if (processMap.get(key) == null) {
                        Processor newProcess = (Processor) NIOProcess.getNioProcessMap().get(key).newInstance();
                        if (newProcess.init()) {
                            //成功初始化后放入处理进程队列
                            processMap.put(key, newProcess);
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("StayProcess initProcessMap ERROR : " + e.getMessage());
                e.printStackTrace();
            }
            return processMap;
        }

        private void quitAll(HashMap<String, Processor> processMap) {
            Iterator<String> keys = NIOProcess.getNioProcessMap().keySet().iterator();
            while (keys.hasNext()) {
                //退出前调用处理进程的退出方法
                processMap.get(keys.next()).quit();
            }
            processMap.clear();
        }

        @Override
        public void run() {
            try {
                HashMap<String, Processor> processMap = null;
                while (true) {
                    processMap = initProcessMap(processMap);

                    Iterator<String> keys = NIOProcess.getNioProcessMap().keySet().iterator();
                    if (doProcess) {
                        while (keys.hasNext()) {
                            String key = keys.next();
                            Object value = null;
                            boolean processSuccess = true;
                            do {
                                //取出第一个
                                value = NIOProcess.removeFirst(key);
                                if (value != null) {
                                    //交由对应processer处理
                                    processSuccess = processMap.get(key).process(value);
                                    if (!processSuccess) {
                                        //重新添加到队列的最后
                                        NIOProcess.putMessage(key, value);
                                    }
                                }
                                //适当休眠，释放cpu资源
                                sleep(SLEEP_INTERVAL);
                            } while (value != null && processSuccess);
                            //队列处理完成或出错时退出该队列的处理循环，等待下一周期的处理
                        }
                    }
                    String cmd = (String) NIOProcess.removeFirst(COMMAND_QUEUE);
                    if (cmd != null) {
                        switch (cmd) {
                            case "reload": {
                                quitAll(processMap);
                                processMap = initProcessMap(processMap);
                                break;
                            }
                            case "stop":{
                                this.doProcess=false;
                                break;
                            }
                            case "start":{
                                this.doProcess=true;
                                break;
                            }
                            case "quit":
                            case "exit": {
                                quitAll(processMap);
                                throw new Exception("Get EXIT command, exiting .");
                            }
                            default:
                                //Unknow cmd, do nothing.
                        }
                    }
                    long l = System.currentTimeMillis() - LAST_TIMESTAMP_OF_WRITE;
                    if (WRITE_TO_FILE && l >= WRITE_INTERVAL * 1000) {
                        writeFile();
                    }
                    sleep(INTERVAL);
                }
            } catch (Exception e) {
                System.out.println("StayProcess ERROR : " + e.getMessage());
                e.printStackTrace();
                NIOProcess.shutdown();
            }
        }
    }
}
