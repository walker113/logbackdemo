package com.example.logbackdemo.sql;


import android.util.Log;

import com.jingang.ad_fabuyun.log.LogTool;
import com.socks.library.KLog;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.List;

import ch.qos.logback.classic.spi.ILoggingEvent;

public class TraceLog extends DataSupport {
	
	private String thread;
	private String time;
	private String msg;
	private String caller_filename_index;
	private String caller_class_index;
	private String caller_method_index;
	private String caller_line_index;
	private String level;

	private int count;
	private String sha1hash;
	private String type;

	private int id;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public String getThread() {
		return thread;
	}

	public void setThread(String thread) {
		this.thread = thread;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getCaller_filename_index() {
		return caller_filename_index;
	}

	public void setCaller_filename_index(String caller_filename_index) {
		this.caller_filename_index = caller_filename_index;
	}

	public String getCaller_class_index() {
		return caller_class_index;
	}

	public void setCaller_class_index(String caller_class_index) {
		this.caller_class_index = caller_class_index;
	}

	public String getCaller_method_index() {
		return caller_method_index;
	}

	public void setCaller_method_index(String caller_method_index) {
		this.caller_method_index = caller_method_index;
	}

	public String getCaller_line_index() {
		return caller_line_index;
	}

	public void setCaller_line_index(String caller_line_index) {
		this.caller_line_index = caller_line_index;
	}

	public void setLevel(String level) {
		this.level = level;
	}
	public String getLevel() {
		return level;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getSha1hash() {
		return sha1hash;
	}

	public void setSha1hash(String sha1hash) {
		this.sha1hash = sha1hash;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	private final static String THREAD_KEY = "thread";
	private final static String TIME_KEY = "time";
	private final static String MSG_KEY = "msg";
	private final static String CALLER_FILENAME_INDEX_KEY = "caller_filename_index";
	private final static String CALLER_METHOD_INDEX_KEY = "caller_method_index";
	private final static String CALLER_CLASS_INDEX_KEY = "caller_class_index";
	private final static String CALLER_LINE_INDEX_KEY = "caller_line_index";
	private final static String LEVEL_KEY = "level";
	private final static String COUNT_KEY = "count";
	private final static String SHA1HASH_KEY = "sha1hash";
	private final static String TYPE_KEY = "type";


	public JSONObject toJSON() {
		final JSONObject json = new JSONObject();
		try {
			json.put(THREAD_KEY, thread);
			json.put(TIME_KEY, time);
			json.put(MSG_KEY, msg);
			json.put(CALLER_FILENAME_INDEX_KEY, caller_filename_index);
			json.put(CALLER_METHOD_INDEX_KEY, caller_method_index);
			json.put(CALLER_CLASS_INDEX_KEY, caller_class_index);
			json.put(CALLER_LINE_INDEX_KEY, caller_line_index);
			json.put(LEVEL_KEY, level);
			json.put(COUNT_KEY, count);
			json.put(SHA1HASH_KEY, sha1hash);
			json.put(TYPE_KEY, type);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}


	public static void parse(ILoggingEvent event, boolean isException) {

		String sha1Hash = LogTool.sha1Hash(event.getMessage());
		if (isException) {
			List datas = findSha1Hash(TraceLog.class, sha1Hash);
			// 异常信息排重，计数字段加1
			if (datas.size() == 1) {
				TraceLog log = (TraceLog) datas.get(0);
				log.setCount(log.getCount() + 1);
				log.save();
				return;
			}
		}

		TraceLog log = new TraceLog();
		if (isException)
			log.setType(LogTool.TYPE_EXCEPTION);

		log.setMsg(event.getMessage());
		log.setTime("" + event.getTimeStamp());
		log.setThread(event.getThreadName());
		log.setLevel(event.getLevel().levelStr);
		log.setSha1hash(sha1Hash);

		if (event.getCallerData() != null && event.getCallerData().length >=1) {
			StackTraceElement stack = event.getCallerData()[0];
			log.setCaller_filename_index(stack.getFileName());
			log.setCaller_class_index(stack.getClassName());
			log.setCaller_method_index(stack.getMethodName());
			log.setCaller_line_index(Integer.toString(stack.getLineNumber()));
		}
		log.save();

	}

	private static List findSha1Hash(Class cl, String sha1hash) {
		return DataSupport.where("sha1hash = ?", sha1hash).find(cl);
	}
}
