package com.waaar.oom;

import sun.rmi.runtime.Log;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {

    private static final long MAX_FILE_SIZE = 500 * 1024 * 1024L;

    public static void main(String[] args) throws IOException {
        File largeFile = createLargeFile("./large.log");
//        catByAllFile(largeFile);
        flowReadFile(largeFile,1000);
    }


    public static File createLargeFile(String fileName) throws IOException {
        System.out.println("正在构造大型日志");
        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        if(file.length() > MAX_FILE_SIZE) {
            System.out.println("已存在文件");
            return  file;
        }

        String msg = "2025-01-01 00:00:00 INFO User login success, userId=123\n" +
                "2025-01-01 00:00:01 ERROR Failed to connect to database";

        BufferedWriter  bw = new BufferedWriter(new FileWriter(file));

        long fileSize = file.length(),lineCount = 0;

        while(fileSize < MAX_FILE_SIZE) {
            bw.write(msg);
            fileSize += msg.getBytes().length;
            lineCount += 2;
            if(lineCount % 10000 == 0) {
                System.out.printf("已写入%d行，占用内存大小%.2fMB\n", lineCount, fileSize / 1024.0 / 1024.0);
            }
        }

        bw.close();
        return file;
    }

    public static  void catByAllFile(File file){
        System.out.println("Method A : 通过cat完全的读取到内存");
        long startTime = System.currentTimeMillis();
        long startMemory = getCurrentMemory();

        try{
            List<String> strings = Files.readAllLines(Paths.get(file.getPath()));
            long endMemory = getCurrentMemory();
            long endTime = System.currentTimeMillis();
            System.out.println("文件前十行内容：\n");
            System.out.println("\n[Method A] 统计结果:");
            System.out.printf("[Method A] Total time: %d ms\n", endTime - startTime);
            System.out.printf("[Method A] Start memory: %dMB\n", startMemory);
            System.out.printf("[Method A] After read memory: %dMB\n", endMemory);
            System.out.printf("[Method A] Total lines read: %d\n", strings.size());
            System.out.println("[Method A] Exception: None");

        }catch (OutOfMemoryError e){
            long endMemory = getCurrentMemory();
            long endTime = System.currentTimeMillis();
            System.out.println("\n[Method A] 统计结果:");
            System.out.printf("[Method A] Total time: %d ms\n", endTime - startTime);
            System.out.printf("[Method A] Start memory: %dMB\n", startMemory);
            System.out.printf("[Method A] After read memory: %dMB\n", endMemory);
            System.out.printf("[Method A] Exception: %s\n",e.getMessage());
        }catch (Exception e){
            long endTime = System.currentTimeMillis();
            long endMemory = getCurrentMemory();
            System.out.println("\n[Method B] 统计结果:");
            System.out.printf("[Method B] Total time: %d ms\n", endTime - startTime);
            System.out.printf("[Method B] Start memory: %dMB\n", startMemory);
            System.out.printf("[Method B] After read memory: %dMB\n", endMemory);
            System.out.printf("[Method A] Exception: %s\n",e.getMessage());
        }


    }

    public static void flowReadFile(File file,long lines) {

        long startTime = System.currentTimeMillis();
        long startMemory = getCurrentMemory(),endMemory = getCurrentMemory();
        String line = null;
        long lineCount = 0;
        System.out.println("输出日志前十行内容");
        try(BufferedReader br = new BufferedReader(new FileReader(file));){


            while((line = br.readLine()) != null  && lineCount <= lines) {
                endMemory = Math.max(getCurrentMemory(),endMemory);
                lineCount += 1;
                if(lineCount <= 10){
                    System.out.println(line);
                }

            }
            long endTime = System.currentTimeMillis();
            System.out.println("\n[Method B] 统计结果:");
            System.out.printf("[Method B] Total time: %d ms\n", endTime - startTime);
            System.out.printf("[Method B] Start memory: %dMB\n", startMemory);
            System.out.printf("[Method B] After read memory: %dMB\n", endMemory);
            System.out.println("[Method B] Exception: None");
        }catch (Exception e){
            long endTime = System.currentTimeMillis();
            endMemory = getCurrentMemory();
            System.out.println("\n[Method B] 统计结果:");
            System.out.printf("[Method B] Total time: %d ms\n", endTime - startTime);
            System.out.printf("[Method B] Start memory: %dMB\n", startMemory);
            System.out.printf("[Method B] After read memory: %dMB\n", endMemory);
            System.out.printf("[Method A] Exception: %s\n",e.getMessage());
        }


    }

    public static long getCurrentMemory(){
        return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024;
    }


}