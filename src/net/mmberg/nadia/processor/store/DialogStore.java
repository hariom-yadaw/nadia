package net.mmberg.nadia.processor.store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import net.mmberg.nadia.processor.dialogmodel.*;
import net.mmberg.nadia.processor.dialogmodel.actions.*;
import net.mmberg.nadia.processor.dialogmodel.aqd.*;
import net.mmberg.nadia.processor.dialogmodel.taskselectors.*;

public class DialogStore {

	private static DialogStore dialogstore= null;
	private HashMap<String, Dialog> store = new HashMap<String, Dialog>();
	
	private DialogStore(){
		store.put("dummy1", createDummyDialog());
		store.put("dummy2", createDummyDialog2());
		store.put("dummy3", createDummyDialog3());
	}
	
	public static DialogStore getInstance(){
		if(dialogstore==null){
			dialogstore=new DialogStore();
		}
		
		return dialogstore;
	}
	
	public static void main(String[] args){
		String test_dialog_name="dummy2";
		//test save
		DialogStore ds=DialogStore.getInstance();
		ds.getDialogFromStore(test_dialog_name).save();
		//test load
		Dialog.loadFromResourcesDir(test_dialog_name);
	}
	
	public Dialog getDialogFromStore(String key){
		return store.get(key);
	}
	
	private Dialog createDummyDialog(){
		
		Dialog dialog = new Dialog("dummy1");
				
		//a dialog consists of tasks
		Task task1=new Task("bsp");
		dialog.addTask(task1);
		
		//a task consists of ITOs
		ITO ito;
		AQD aqd;
		
		//1
		ito=new ITO("getDrinkDecision", "Would you like a drink?", false);	
		task1.addITO(ito);
		//an ITO is associated with AQDs
		aqd=new AQD();
		aqd.setType(new AQDType("decision"));
		ito.setAQD(aqd);	
		
		//2
		ito=new ITO("getCity", "Where do you want to go?", false);	
		task1.addITO(ito);
		//an ITO is associated with AQDs
		aqd=new AQD();
		aqd.setType(new AQDType("fact.named_entity.non_animated.location.city"));
		ito.setAQD(aqd);	
				
		//3
		ito=new ITO("getDate", "When do you want to leave?");	
		task1.addITO(ito);
		aqd=new AQD(new AQDType("fact.temporal.date"), new AQDContext("begin","trip"), new AQDForm()); //TODO politeness and formality set on dialogue level
		ito.setAQD(aqd);	
		
		return dialog;			
	}	
	
private Dialog createDummyDialog2(){
		
		Dialog dialog = new Dialog("dummy2");
		dialog.setGlobal_politeness(4);
		dialog.setGlobal_formality(4);
		dialog.setStart_task_name("start");
		ITO ito;
		AQD aqd;
		
		//Task 0
		//----------------------------------------------
		Task task0=new Task("start");
		ArrayList<String> bagOfWords = new ArrayList<String>(Arrays.asList("hello"));
		task0.setSelector(new BagOfWordsTaskSelector(bagOfWords));
		dialog.addTask(task0);
	
		//ITO 1
		ito=new ITO("welcome", "Hello! How may I help you?", false);	
		task0.addITO(ito);
		//an ITO is associated with AQDs
		aqd=new AQD();
		aqd.setType(new AQDType("open_ended"));
		ito.setAQD(aqd);
		
		
		//Task 1
		//----------------------------------------------
		Task task1=new Task("getTripInformation");
		bagOfWords = new ArrayList<String>(Arrays.asList("travel","book", "journey","trip"));
		task1.setSelector(new BagOfWordsTaskSelector(bagOfWords));
		
//		JavaAction jaction=new JavaAction("This trip from %getDepartureCity to %getDestinationCity costs #temperature Euros.");
//		try{
//			jaction.setPath("/Users/markus/");
//			jaction.setClassName("net.mmberg.nadia.processor.nlu.actions.TestExtJavaAction");
//		}
//		catch(Exception ex){
//			ex.printStackTrace();
//		}
//		task1.setAction(jaction);
		
		GroovyAction gaction = new GroovyAction("This trip from %getDepartureCity to %getDestinationCity costs #price Euros.");
		gaction.setCode("executionResults.put(\"price\",\"257\")");
		//gaction.setReturnAnswer(false);
		task1.setAction(gaction);
		
		dialog.addTask(task1);
		
		//ITO 1
		ito=new ITO("getDepartureCity", "Where do you want to start?", false);	
		task1.addITO(ito);
		aqd=new AQD();
		aqd.setType(new AQDType("fact.named_entity.non_animated.location.city"));
		ito.setAQD(aqd);		
		
		//ITO 2
		ito=new ITO("getDestinationCity", "Where do you want to go?", false);	
		task1.addITO(ito);
		aqd=new AQD();
		aqd.setType(new AQDType("fact.named_entity.non_animated.location.city"));
		ito.setAQD(aqd);
		
		//ITO 3
		ito=new ITO("getNumberOfPersons", "For how many persons?", false);	
		task1.addITO(ito);
		aqd=new AQD();
		aqd.setType(new AQDType("fact.quantity"));
		ito.setAQD(aqd);	
		
		
		//Task2
		//----------------------------------------------
		Task task2=new Task("getWeatherInformation");
		bagOfWords = new ArrayList<String>(Arrays.asList("weather","forecast", "temperature"));
		task2.setSelector(new BagOfWordsTaskSelector(bagOfWords));
		gaction = new GroovyAction("The temperature in %getWeatherCity is #temperature degrees.");
		gaction.setCode("" +
				"import groovyx.net.http.*\r\n"+
				"import javax.xml.xpath.*\r\n"+
				"def http = new HTTPBuilder('http://weather.yahooapis.com')\r\n"+
				"http.get( path: '/forecastrss', query:[w:'2502265',u:'c'],  contentType: ContentType.XML) { resp, xml -> \r\n"+
				"   def temp = xml.channel.item.condition[0].@temp\r\n"+
				"	executionResults.put(\"temperature\",temp.toString())\r\n"+
				"}"
		);
		task2.setAction(gaction);
		dialog.addTask(task2);
		
		//ITO 1
		ito=new ITO("getWeatherCity", "For which city do you want to know the weather?", false);	
		task2.addITO(ito);
		//an ITO is associated with AQDs
		aqd=new AQD();
		aqd.setType(new AQDType("fact.named_entity.non_animated.location.city"));
		ito.setAQD(aqd);		
		
		//Task3
		//-----------------------------------------------
		Task task3 = new Task("getWikipediaCityInfo");
		bagOfWords = new ArrayList<String>(Arrays.asList("wikipedia","tell me about", "know about"));
		task3.setSelector(new BagOfWordsTaskSelector(bagOfWords));
		HTTPAction httpaction=new HTTPAction("#result");
		httpaction.setUrl("http://en.wikipedia.org/w/api.php");
		httpaction.setMethod("get");
		httpaction.setParams("format=xml&action=query&prop=extracts&explaintext&exsentences=3&titles=%getWikiCity");
		httpaction.setXpath("//extract");
		task3.setAction(httpaction);
		dialog.addTask(task3);
		
		//ITO1
		ito=new ITO("getWikiCity", "What city do you want to know more about?",false);
		task3.addITO(ito);
		aqd=new AQD();
		aqd.setType(new AQDType("fact.named_entity.non_animated.location.city"));
		ito.setAQD(aqd);
		
		//FollowUp ITO
		ITO followito=new ITO("anotherOne", "Do you want to know about other cities?",false);
		aqd=new AQD();
		aqd.setType(new AQDType("decision"));
		followito.setAQD(aqd);
		FollowUp follow=new FollowUp();
		follow.setIto(followito);
		HashMap<String,String> answerMapping=new HashMap<String, String>();
		answerMapping.put("YES", "getWikipediaCityInfo");
		follow.setAnswerMapping(answerMapping);
		task3.setFollowup(follow);
		
		//Task4
		//-----------------------------------------------
		Task task4 = new Task("setLightbulb");
		task4.setAct("action");
		bagOfWords = new ArrayList<String>(Arrays.asList("bulb","switch"));
		task4.setSelector(new BagOfWordsTaskSelector(bagOfWords));
		httpaction=new HTTPAction("#result");
		httpaction.setUrl("http://mmt.et.hs-wismar.de:8080/Lightbulb/Lightbulb");
		httpaction.setMethod("post");
		httpaction.setParams("state=%getLightAction");
		httpaction.setXpath("//message");
		task4.setAction(httpaction);
		dialog.addTask(task4);
		
		//ITO1
		ito=new ITO("getLightAction", "Do you want to switch it on or off?",false);
		task4.addITO(ito);
		aqd=new AQD();
		aqd.setType(new AQDType("onoff"));
		ito.setAQD(aqd);	
		
		//Task5 (this task has no ITOs!!!)
		//-----------------------------------------------
		Task task5 = new Task("getLightbulb");
		task5.setAct("seek");
		bagOfWords = new ArrayList<String>(Arrays.asList("bulb","switch"));
		task5.setSelector(new BagOfWordsTaskSelector(bagOfWords));
		httpaction=new HTTPAction("#result");
		httpaction.setUrl("http://mmt.et.hs-wismar.de:8080/Lightbulb/Lightbulb");
		httpaction.setMethod("get");
		httpaction.setParams("getstate");
		httpaction.setXpath("//message");
		task5.setAction(httpaction);
		dialog.addTask(task5);
		
		
		return dialog;			
	}	

private Dialog createDummyDialog3(){
	
	Dialog dialog = new Dialog("dummy3");
	dialog.setGlobal_politeness(4);
	dialog.setGlobal_formality(4);
	dialog.setStart_task_name("getWeatherInformation");
	ITO ito;
	AQD aqd;
	
	Task task2=new Task("getWeatherInformation");
	ArrayList<String> bagOfWords = new ArrayList<String>(Arrays.asList("weather","forecast", "temperature","trip"));
	task2.setSelector(new BagOfWordsTaskSelector(bagOfWords));
	GroovyAction gaction = new GroovyAction("The temperature in %getWeatherCity is #temperature degrees.");
	gaction.setCode("" +
			"import groovyx.net.http.*\r\n"+
			"import javax.xml.xpath.*\r\n"+
			"def http = new HTTPBuilder('http://weather.yahooapis.com')\r\n"+
			"http.get( path: '/forecastrss', query:[w:'2502265',u:'c'],  contentType: ContentType.XML) { resp, xml -> \r\n"+
			"   def temp = xml.channel.item.condition[0].@temp\r\n"+
			"	executionResults.put(\"temperature\",temp.toString())\r\n"+
			"}"
	);
	task2.setAction(gaction);
	dialog.addTask(task2);
	
	//ITO 1
	ito=new ITO("getWeatherCity", "For which city do you want to know the weather?", false);	
	task2.addITO(ito);
	//an ITO is associated with AQDs
	aqd=new AQD();
	aqd.setType(new AQDType("fact.named_entity.non_animated.location.city"));
	ito.setAQD(aqd);		
	
	return dialog;			
}	
}