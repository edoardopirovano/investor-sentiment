import twitter4j._
import com.typesafe.config.ConfigFactory;
import java.util.Date
import java.sql.Date
import ox.CSO._
import java.io._
import java.lang._
import java.util.Arrays;
import scala.sys.process.Process

object TweetProcessor {

	/////////////////////////////////////////////////////
	/************  Concurrent Implementation ***********/
	/////////////////////////////////////////////////////

	/** takes a list of company names and returns a list of (company name,array) for each company,
	  * where each pair (a,b) in the arrays = (tweet object,sentiment score) where sentiment score = 0, 2, 4
	  * and the tweet object was generated during a search about that particular name
	  */
	def getTwitterSentimentScores(stocks : List[String]) : List[(String,Array[(Status,Int)])] = {
			var resultsList : List[(String,Array[(Status,Int)])] = List()
		    val mutexes = new Array[(Semaphore,Semaphore,Semaphore)](stocks.length)
		   	val tweets = new Array[List[Status]](stocks.length)

		   	// intialize the tmp directory that the tweet and results files will be written/read in
		   	val dir = new File("tmp")
			if (!dir.exists()) dir.mkdir()

		    for (i <- 0 until stocks.length) {
		    	val s1 , s2 , s3 = new Semaphore()
		    	s1.down(); s2.down(); s3.down()
		    	mutexes(i) = (s1,s2,s3)
		    }

		    // searches for tweets, for each stock, writing to tweets(i)
		    val tweetGenerator : PROC = {
		    	var i = 0
		    	for (stock <- stocks) {
		    		tweets(i) = TwitterSearch.getTweets(stock)
		    		mutexes(i)._1.up() // signal stock's tweets have been pulled from twitter
		    		i += 1
		    	}
		    }
		    
		    // writes tweets(i) to file, to be sent of to Sentiment140 API by the APICaller proc
		    val writer : PROC = proc {
		    	var i = 0
		    	for (stock <- stocks) {
		    		mutexes(i)._1.down()
		    		TweetProcessor.writeTweetsToFile(stock,tweets(i))()
		    		mutexes(i)._2.up()	// signal stock's tweets file has been written
		    		i += 1
		    	}
		    }

		    // sends the tweet file to the API and writes the results to file
		    val apiCaller : PROC = proc {
		    	var i = 0
		    	for (stock <- stocks) {
		    		mutexes(i)._2.down()	// wait for stock's tweets file to be written
		    		TweetProcessor.sendTweetsToAPI(stock,stock)() // writes output to a file named stock_results
		    		mutexes(i)._3.up() // signal stock's results file has been written
		    		i += 1
		    	}
		    }

		    // resultsList(i) contains the array (tweet,tweet score) pairs for stock(i)
		    val resultReader : PROC = proc {
		    	var i = 0
		    	for (stock <- stocks) {
		    		mutexes(i)._3.down() // wait for stock's results file to be written
		    		val results = new Array[(Status,Int)](tweets(i).length)
		    		TweetProcessor.readTweetSentimentsFromFile(stock+"_results",tweets(i),results)()
		    		resultsList = (stock,results) :: resultsList
		    		i += 1
		    	}
		    }

		val System : PROC = (tweetGenerator || writer || apiCaller || resultReader)
		System()
		return resultsList
		  
	}

	//////////////////////////////////////////////////
	/************  Utility Methods  *****************/
	//////////////////////////////////////////////////

	val conf = ConfigFactory.load();	
	val api_id = conf.getString("apikeys.sentiment140")

	/** returns the URL of a tweet
	  * https://twitter.com/[screen name of user]/status/[id of status] 
	  */
	def getTweetUrl(tweet : Status) : String = {
		return "http://twitter.com/"+ tweet.getUser().getScreenName() + "/status/" + tweet.getId()
	}

	def getTweetDate(tweet : Status) : java.sql.Date = {
		return new java.sql.Date(tweet.getCreatedAt().getTime())
	}
	
	/* writes each tweet to filename, which is placed in the directory "tmp" */
	def writeTweetsToFile(filename : String , tweets : List[Status]) : PROC = {
		proc {
			val dir = new File("tmp")
			if (!dir.exists()) { dir.mkdir() }
			val file = new File("tmp/"+filename)
			file.createNewFile()
			val bufWriter = new BufferedWriter(new FileWriter(file))
			for (tweet <- tweets) {
				bufWriter.write(tweet.getText())
				bufWriter.newLine()
			}
			bufWriter.flush()
			bufWriter.close()
		}
	}
	/* queries the Sentiment140 API and stores the output in tmp/filename_results */
	def sendTweetsToAPI(filename : String, query : String) : PROC = {
		proc {
			val file = new File("tmp/"+filename)
			file.createNewFile()
			if (file.exists()) {
				// curl --data-binary @filename "http://www.sentiment140.com/api/bulkClassify?query= ....."
				// doesn't use the API key as of yet
				try { 
					val nospaces_query = query.replaceAll(" ","%20") // http url format
					val url = "http://www.sentiment140.com/api/bulkClassify?query="
					val processBuilder : ProcessBuilder = new ProcessBuilder(
						"curl","--data-binary","@tmp/"+filename,
						"http://www.sentiment140.com/api/bulkClassify?query="+nospaces_query)
					processBuilder.redirectErrorStream(true);
					val process : java.lang.Process = processBuilder.start()
					val inStream : InputStream = process.getInputStream()
					val outStream : FileOutputStream = new FileOutputStream("tmp/"+filename+"_results")
					var line = ""
   	 				val bufIn : BufferedInputStream = new BufferedInputStream(inStream);
    				val bytes = new Array[scala.Byte](140)
    				var bytesRead = 0
    				val singleByte : scala.Byte = 0
   					bytesRead = bufIn.read(bytes,0,140)
    				while (bytesRead != -1) {
        				outStream.write(bytes, 0, bytesRead)
        				Arrays.fill(bytes, singleByte);
        				bytesRead = bufIn.read(bytes,0,140)
        			}
        			outStream.flush()
        			outStream.close()
				} catch {
					case e : Exception => throw new Exception("Couldn't execute curl") 
				}
			}
			else {
				throw new FileNotFoundException("tmp/"+filename+" not found...")
			}
		}
	}
	/* reads the output stored in tmp/filename and writes it to the results array */
	def readTweetSentimentsFromFile(filename : String, tweets : List[Status], results : Array[(Status,Int)]) : PROC = {
		proc {
			val file = new File("tmp/"+filename)
			if (file.exists()) {
				val reader = new BufferedReader(new FileReader(file))
				var line = ""
				var i = 0
				var score = 0
				while (line == null || !(line.length > 0) || !(line(0) == '"') ) { line = reader.readLine() } // 'ignores file's header
				for (tweet <- tweets) {
					if (line != null && line.length > 0) score = line(1).toInt // API's output is of the form "s","tweet's text..." where s = 0,2,4
					else score = -1
					results(i) = (tweet,mapScore(score))
					i += 1
					line = reader.readLine()
				}
			}
			else {
				throw new FileNotFoundException("tmp/"+filename+" not found...")
			}
		}
	}

	/** applies a mapping for the Sentiment140 output scoring to our own */
	private def mapScore(x : Int) : Int = {
		x match {
			case 0 | 48 => return 0
			case 50 => return 2
			case 52 => return 4
			case _ => return -1
		} 
	}

	// could base this on the number of followers of that particular User?
	def getTweetImportance(tweet: Status) : Int = {	
		// to do ...
		return 1 // temporary
	}


	def getTweetPopularity(tweet : Status) : Int = {
		val retweets = tweet.getRetweetCount()
		val favorites = tweet.getFavoriteCount()
		// to do ...
		return 1 // temporary
	}

}